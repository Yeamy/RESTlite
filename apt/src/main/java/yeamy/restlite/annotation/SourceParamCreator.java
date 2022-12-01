package yeamy.restlite.annotation;

import yeamy.restlite.annotation.SourceArguments.Impl;
import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

/**
 * @see SourceParamFactory
 * @see SourceParamConstructor
 * @see SourceParamFail
 */
abstract class SourceParamCreator {
    private String id;
    private boolean throwable = false;
    private boolean closeable = false;
    private boolean closeThrow = false;

    protected void init(ProcessEnvironment env, TypeMirror t, ExecutableElement method, boolean samePackage,
                        List<? extends Element> elements) {
        if (method != null) {
            this.throwable = method.getThrownTypes().size() > 0;
        }
        this.closeable = env.isCloseable(t);
        if (closeable) {
            for (Element element : elements) { // find close method
                if (element.getKind() == ElementKind.METHOD//
                        && element.getSimpleName().toString().equals("close")) {
                    Set<Modifier> modifiers = element.getModifiers();
                    if (modifiers.contains(Modifier.STATIC) || modifiers.contains(Modifier.PRIVATE)) {
                        continue;
                    }
                    if (samePackage || modifiers.contains(Modifier.PUBLIC)) {
                        ExecutableElement close = (ExecutableElement) element;
                        if (close.getParameters().size() == 0) {
                            this.closeThrow = close.getThrownTypes().size() > 0;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }

    public boolean isCloseable() {
        return closeable;
    }

    public boolean isCloseThrow() {
        return closeThrow;
    }

    public boolean isThrowable() {
        return throwable;
    }

    public abstract CharSequence toCharSequence(SourceParamChain chain, String name);

    public abstract CharSequence toCharSequence(SourceParamChain chain);

    public final void appendParam(List<? extends VariableElement> arguments, SourceParamChain chain, StringBuilder b) {
        boolean first = true;
        if (arguments != null) {
            for (VariableElement p : arguments) {
                if (first) {
                    first = false;
                } else {
                    b.append(",");
                }
                appendArgument(chain, p, b);
            }
        }
    }

    protected abstract boolean supportBody();

    private void appendArgument(SourceParamChain chain, VariableElement param, StringBuilder b) {
        if (doRequest(param, b)
                || doHeader(chain, param, b)
                || doCookie(chain, param, b)
                || doParam(chain.getArguments(), param, b)
                || doExtra(chain, param, b)
                || (supportBody() && doBody(chain, param, b))) {
            return;
        }
        doNoType(param.asType().getKind(), b);
    }

    private void doNoType(TypeKind kind, StringBuilder b) {
        switch (kind) {
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                b.append("0");
                break;
            case BOOLEAN:
                b.append("false");
                break;
            case DECLARED:
            case ARRAY:
            default:
                b.append("null");
        }
    }

    private boolean doRequest(VariableElement p, StringBuilder b) {
        TypeMirror t = p.asType();
        String clz = t.toString();
        if (T_HttpRequest.equals(clz)) {
            b.append("_req");
            return true;
        } else if (T_HttpServletRequest.equals(clz)) {
            b.append("_req.getRequest()");
            return true;
        }
        return false;
    }

    private boolean doHeader(SourceParamChain chain, VariableElement p, StringBuilder b) {
        Header header = p.getAnnotation(Header.class);
        if (header == null) {
            return false;
        }
        String name = header.value();
        if ("".equals(name)) {
            name = p.getSimpleName().toString();
        }
        SourceArguments args = chain.getArguments();
        String alias = args.getHeaderAlias(name);
        if (alias != null) {
            b.append(alias);
            return true;
        }
        String type = p.asType().toString();
        if (T_String.equals(type)) {
            String exist = args.getHeaderAlias(type);
            if (exist != null) {
                args.addExist(exist);
                return true;
            }
            b.append("_req.getHeader(\"").append(name).append("\")");
        } else {
            b.append("null/* not support type */");
            chain.getEnvironment().error("Not support header type " + type);
        }
        return true;
    }

    private boolean doCookie(SourceParamChain chain, VariableElement p, StringBuilder b) {
        String type = p.asType().toString();
        if (T_Cookies.equals(type)) {
            b.append("_req.getCookies()");
            return true;
        }
        Cookies cookie = p.getAnnotation(Cookies.class);
        String name;
        if (cookie == null) {
            if (T_Cookie.equals(type)) {
                name = p.getSimpleName().toString();
            } else {
                return false;
            }
        } else {
            name = cookie.value();
            if ("".equals(name)) {
                name = p.getSimpleName().toString();
            }
        }
        String alias = chain.getArguments().getCookieAlias(type, name);
        if (alias != null) {
            b.append(alias);
            return true;
        } else if (T_Cookie.equals(type)) {
            b.append("_req.getCookie(\"").append(name).append("\")");
            return true;
        } else {
            if (T_String.equals(type)) {
                b.append("_req.getCookieValue(\"").append(name).append("\")");
            } else {
                b.append("null/* not support type */");
                chain.getEnvironment().error("Not support cookies type " + type);
            }
            return true;
        }
    }

    private boolean doBody(SourceParamChain chain, VariableElement p, StringBuilder b) {
        Body body = p.getAnnotation(Body.class);
        TypeMirror t = p.asType();
        String type = t.toString();
        if (body != null && TextUtils.notIn(type, T_ServletInputStream, T_File, T_Files)) {
            return false;
        }
        ProcessEnvironment env = chain.getEnvironment();
        SourceArguments args = chain.getArguments();
        if (args.containsBody()) {
            args.addFallback("null");
            env.error("cannot read body twice");
            return true;
        }
        switch (type) {
            case T_File:
                b.append("_req.getFile(\"").append(p.getSimpleName()).append("\")");
                break;
            case T_Files:
                b.append("_req.getFiles()");
                break;
            case T_InputStream:
            case T_ServletInputStream:
                b.append("_req.getBody()");
                break;
            case T_Bytes:
                b.append("_req.getBodyAsByte()");
                break;
            case T_String:
                assert body != null;
                String charset = env.charset(body.charset());
                b.append("_req.getBodyAsText(\"").append(charset).append("\")");
                break;
            default:
                assert body != null;
                SourceParamCreator creator = env.getBodyCreator(chain.getServlet(), t, body);
                if (creator instanceof SourceParamFail) {
                    b.append("null/*not support body type*/");
                    env.warning("not support body type " + type + " without annotation Creator");
                } else {
                    b.append(creator.toCharSequence(chain));
                }
        }
        return true;
    }

    private boolean doExtra(SourceParamChain chain, VariableElement p, StringBuilder b) {
        Extra extra = p.getAnnotation(Extra.class);
        if (extra == null) {
            return false;
        }
        TypeMirror t = p.asType();
        ProcessEnvironment env = chain.getEnvironment();
        SourceServlet servlet = chain.getServlet();
        SourceArguments args = chain.getArguments();
        SourceParamCreator creator = env.getExtraCreator(servlet, t, extra);
        if (creator instanceof SourceParamFail) {
            b.append("null/*not support type*/");
        } else if (chain.add(this)) {
            if (creator.throwable || creator.closeable) {
                Impl impl = args.addSubExtra(creator.id, creator.throwable, creator.closeable, creator.closeThrow,
                        extra.autoClose());
                impl.write(creator.toCharSequence(chain, impl.name()));
                b.append(impl.name());
            } else {
                b.append(creator.toCharSequence(chain));
            }
        } else {
            b.append("null/*cycle reference*/");
            env.error("cycle reference");
        }
        return true;
    }

    private static boolean doParam(SourceArguments args, VariableElement p, StringBuilder b) {
        Param param = p.getAnnotation(Param.class);
        if (param == null) {
            return false;
        }
        String fallback = param.fallback();
        TypeMirror t = p.asType();
        String type = t.toString();
        String name = p.getSimpleName().toString();
        String alias = args.getParamAlias(type, name);
        if (alias != null) {
            b.append(alias);
            return true;
        }
        switch (t.getKind()) {
            case LONG:
                b.append("_req.getLongParam(\"").append(name);
                if (!"".equals(fallback)) {
                    b.append("\", ").append(fallback).append(")");
                } else {
                    b.append("\")");
                }
                return true;
            case INT:
                b.append("_req.getIntParam(\"").append(name);
                if (!"".equals(fallback)) {
                    b.append("\", ").append(fallback).append(")");
                } else {
                    b.append("\")");
                }
                return true;
            case BOOLEAN:
                b.append("_req.getBoolParam(\"").append(name);
                if ("".equals(fallback)) {
                    b.append("\")");
                } else {
                    boolean fb = "true".equalsIgnoreCase(fallback);
                    b.append("\", ").append(fb).append(")");
                }
                return true;
            case DECLARED: {
                switch (type) {
                    case T_String:
                        b.append("_req.getParameter(\"").append(name);
                        if (!"".equals(fallback.trim())) {
                            b.append("\", \"").append(fallback).append("\")");
                        } else {
                            b.append("\")");
                        }
                        return true;
                    case T_Decimal:
                        b.append("_req.getDecimalParam(\"").append(name).append('"');
                        if (!"".equals(fallback.trim())) {
                            b.append("\", new BigDecimal(\"").append(fallback).append("\")");
                        }
                        b.append(')');
                        return true;
                }
            }
            case ARRAY:
                switch (type) {
                    case T_Bools:
                        b.append("_req.getBooleanParams(\"").append(name).append("\")");
                        return true;
                    case T_Ints:
                        b.append("_req.getIntParams(\"").append(name).append("\")");
                        return true;
                    case T_Longs:
                        b.append("_req.getLongParams(\"").append(name).append("\")");
                        return true;
                    case T_Decimals:
                        b.append("_req.getDecimalParams(\"").append(name).append("\")");
                        return true;
                    case T_Strings:
                        b.append("_req.getParameters(\"").append(name).append("\")");
                        return true;
                }
            default:
        }
        return false;
    }

    // -----------
    private static final String[] BODY_SUPPORT_TYPE = {T_HttpRequest, T_HttpServletRequest, T_Cookie, T_Cookies,
            T_InputStream, T_ServletInputStream, T_File, T_Files};

    private static final String[] EXTRA_SUPPORT_TYPE = {T_HttpRequest, T_HttpServletRequest, T_Cookie, T_Cookies};

    protected static boolean checkParam(ExecutableElement method, boolean supportBody) {
        return checkParam(method, supportBody, null);
    }

    protected static boolean checkParam(ExecutableElement method, boolean supportBody, String typeVar) {
        for (VariableElement param : method.getParameters()) {
            if (param.getAnnotation(Header.class) != null || param.getAnnotation(Cookies.class) != null
                    || param.getAnnotation(Param.class) != null || param.getAnnotation(Extra.class) != null) {
                continue;
            }
            String type = param.asType().toString();
            if (TextUtils.in(type, supportBody ? BODY_SUPPORT_TYPE : EXTRA_SUPPORT_TYPE)) {
                continue;
            } else if (typeVar != null && param.asType().toString().equals(typeVar)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
