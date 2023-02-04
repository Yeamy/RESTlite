package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

/**
 * @see SourceParamFactory
 * @see SourceParamConstructor
 * @see SourceParamFail
 */
abstract class SourceParam {
    protected final ProcessEnvironment env;
    protected final TypeElement type;
    protected final boolean isBody;
    private boolean throwable = false;
    private boolean closeable = false;
    private boolean closeThrow = false;

    public SourceParam(ProcessEnvironment env, TypeElement type, boolean isBody) {
        this.env = env;
        this.type = type;
        this.isBody = isBody;
    }

    protected void init(ExecutableElement method, boolean samePackage, List<? extends Element> elements) {
        if (method != null) {
            this.throwable = method.getThrownTypes().size() > 0;
        }
        this.closeable = env.isCloseable(type.asType());
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

    public boolean isCloseable() {
        return closeable;
    }

    public boolean isCloseThrow() {
        return closeThrow;
    }

    public boolean isThrowable() {
        return throwable;
    }

    public abstract CharSequence toCharSequence(SourceServlet servlet, SourceArguments args, String name);

    public final void appendParam(List<? extends VariableElement> arguments,
                                  SourceServlet servlet,
                                  SourceArguments args,
                                  StringBuilder b) {
        boolean first = true;
        if (arguments != null) {
            for (VariableElement p : arguments) {
                if (first) {
                    first = false;
                } else {
                    b.append(",");
                }
                appendArgument(servlet, args, p, b);
            }
        }
    }

    private void appendArgument(SourceServlet servlet, SourceArguments args, VariableElement param, StringBuilder b) {
        if (doRequest(param, b)
                || doHeader(args, param, b)
                || doCookie(args, param, b)
                || doParam(args, param, b)
                || doBody(args, param, b)) {
            return;
        }
        switch (param.asType().getKind()) {
            case BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE -> b.append("0");
            case BOOLEAN -> b.append("false");
            case DECLARED -> b.append(declaredArgument(servlet, param));
//            case ARRAY -> b.append("null");
            default -> b.append("null");
        }
    }

    protected abstract String declaredArgument(SourceServlet servlet, VariableElement param);

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

    private boolean doHeader(SourceArguments args, VariableElement p, StringBuilder b) {
        Header header = p.getAnnotation(Header.class);
        if (header == null) {
            return false;
        }
        String name = header.value();
        if ("".equals(name)) {
            name = p.getSimpleName().toString();
        }
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
            env.error("Not support header type " + type);
        }
        return true;
    }

    private boolean doCookie(SourceArguments args, VariableElement p, StringBuilder b) {
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
        String alias = args.getCookieAlias(type, name);
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
                env.error("Not support cookies type " + type);
            }
            return true;
        }
    }

    private boolean doBody(SourceArguments args, VariableElement p, StringBuilder b) {
        if (!isBody) return true;
        if (args.containsBody()) {
            args.addFallback("null");
            env.error("cannot read body twice");
            return true;
        }
        Body body = p.getAnnotation(Body.class);
        TypeMirror t = p.asType();
        String type = t.toString();
        if ((body == null) || TextUtils.in(type, T_Part, T_Parts, T_File, T_Files, T_InputStream, T_ServletInputStream)) {
            switch (type) {
                case T_Part -> b.append("_req.getPart(\"").append(p.getSimpleName()).append("\")");
                case T_Parts -> b.append("_req.getParts()");
                case T_File -> b.append("_req.getFile(\"").append(p.getSimpleName()).append("\")");
                case T_Files -> b.append("_req.getFiles()");
                case T_InputStream, T_ServletInputStream -> b.append("_req.getBody()");
                default -> {
                    return false;
                }
            }
            return true;
        }
        switch (type) {
            case T_Bytes -> b.append("_req.getBodyAsByte()");
            case T_String -> b.append("_req.getBodyAsText(\"").append(env.charset(body.charset())).append("\")");
            default -> b.append("null");
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
                    b.append("\", ").append(Long.valueOf(fallback)).append(")");
                } else {
                    b.append("\")");
                }
                return true;
            case INT:
                b.append("_req.getIntParam(\"").append(name);
                if (!"".equals(fallback)) {
                    b.append("\", ").append(Integer.valueOf(fallback)).append(")");
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
                if (T_String.equals(type)) {
                    b.append("_req.getParameter(\"").append(name);
                    if (!"".equals(fallback)) {
                        b.append("\", \"").append(SourceClass.convStr(fallback)).append("\")");
                    } else {
                        b.append("\")");
                    }
                    return true;
                } else if (T_Decimal.equals(type)) {
                    b.append("_req.getDecimalParam(\"").append(name).append('"');
                    if (!"".equals(fallback)) {
                        b.append("\", new BigDecimal(\"").append(fallback).append("\")");
                    }
                    b.append(')');
                    return true;
                }
            }
            case ARRAY:
                switch (type) {
                    case T_Booleans:
                        b.append("_req.getBooleanParams(\"").append(name).append("\")");
                        return true;
                    case T_Integers:
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

    protected static boolean checkParam(ExecutableElement method) {
        return checkParam(method, null);
    }

    protected static boolean checkParam(ExecutableElement method, String typeVar) {
        for (VariableElement param : method.getParameters()) {
            String type = param.asType().toString();
            if (!type.equals(typeVar)
                    && TextUtils.notIn(type, BODY_SUPPORT_TYPE)
                    && param.getAnnotation(Header.class) == null
                    && param.getAnnotation(Cookies.class) == null
                    && param.getAnnotation(Param.class) == null) {
                return false;
            }
        }
        return true;
    }
}
