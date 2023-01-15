package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourceHttpMethodComponent {
    private final ProcessEnvironment env;
    private final SourceServlet servlet;
    private final ExecutableElement method;
    private final List<? extends VariableElement> arguments;
    private final SourceServiceName serverName;
    private final boolean async;
    private final long asyncTimeout;

    private final SourceArguments args = new SourceArguments();

    public SourceHttpMethodComponent(ProcessEnvironment env, SourceServlet servlet, ExecutableElement method,
                                     boolean async, long asyncTimeout) {
        this.env = env;
        this.servlet = servlet;
        this.method = method;
        this.arguments = method.getParameters();
        this.serverName = new SourceServiceName(servlet.getResource(), arguments);
        this.async = async;
        this.asyncTimeout = asyncTimeout;
    }

    final String orderKey() {
        return serverName.params;
    }

    public void create(String httpMethod) {
        for (VariableElement a : arguments) {
            if (doRequest(a)
                    || doHeader(a)
                    || doCookie(a)
                    || doBody(a)
                    || doParam(a)) {
                continue;
            }
        }
        String key = env.addServerName(httpMethod, serverName);
        // check arguments
        if (serverName.isNoParam()) {
            servlet.append("default:{");
        } else {
            servlet.append("case \"").append(key).append("\":{");
        }
        int begin = servlet.length();
        try {
            if (async) {
                servlet.imports("jakarta.servlet.AsyncContext");
                servlet.append("AsyncContext _asyncContext = _req.startAsync();");
                if (asyncTimeout > 0) {
                    servlet.append("_asyncContext.setTimeout(").append(asyncTimeout).append("\");\"");
                }
                servlet.append("_asyncContext.start(() -> {try {");
            }
            // get arguments
            for (CharSequence g : args) {
                servlet.append(g);
            }
            // return
            doReturn(env, servlet);
            if (async) {
                servlet.append("}catch(Exception e){onError(_req,_resp,e);}finally{_asyncContext.complete();}});");
            }
        } catch (Exception e) {
            env.error(e);
            servlet.deleteLast(servlet.length() - begin);
            servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                    .append("(500, \"Server error!\");");
        }
        servlet.append("break;}");
    }

    private boolean doRequest(VariableElement p) {
        String type = p.asType().toString();
        if (T_HttpRequest.equals(type)) {
            args.addExist("_req");
            return true;
        } else if (T_HttpServletRequest.equals(type)) {
            args.addExist("_req.getRequest()");
            return true;
        } else {
            return false;
        }
    }

    private boolean doHeader(VariableElement p) {
        Header header = p.getAnnotation(Header.class);
        if (header == null) {
            return false;
        }
        String alias = p.getSimpleName().toString();
        String name = header.value();
        if ("".equals(name)) {
            name = alias;
        }
        String type = p.asType().toString();
        if (T_String.equals(type)) {
            String exist = args.getHeaderAlias(type);
            if (exist != null) {
                args.addExist(exist);
                return true;
            }
            args.addHeader(name, alias).write("String ", alias, " = _req.getHeader(\"", name, "\");");
        } else {
            args.addFallback("null/* not support type */");
            env.warning("not support header type " + type + " without annotation Creator");
        }
        return true;
    }

    private boolean doCookie(VariableElement p) {
        TypeMirror t = p.asType();
        String type = t.toString();
        if (T_Cookies.equals(type)) {
            args.addExist("_req.getCookies()");
            return true;
        }
        Cookies cookie = p.getAnnotation(Cookies.class);
        String alias;
        String name;
        if (cookie == null) {
            if (T_Cookie.equals(type)) {
                name = alias = p.getSimpleName().toString();
            } else {
                return false;
            }
        } else {
            alias = p.getSimpleName().toString();
            name = cookie.value();
            if ("".equals(name)) {
                name = alias;
            }
        }
        String exist = args.getCookieAlias(type, name);
        if (exist != null) {
            args.addExist(exist);
            return true;
        } else if (T_Cookie.equals(type)) {
            String clz = servlet.imports(T_Cookie);
            args.addCookie(type, name, alias).write(clz, "Cookie ", alias, " = _req.getCookie(\"", name, "\");");
            return true;
        } else if (T_String.equals(type)) {
            args.addCookie(type, name, alias).write("String ", alias, " = _req.getCookieValue(\"", name, "\");");
        } else {
            args.addCookie(type, name, alias).write(type, " ", alias, " = null;/* not support type */");
            env.warning("Not support cookie type " + type + " without annotation Creator");
        }
        return true;
    }

    private boolean doBody(VariableElement p) {
        TypeMirror t = p.asType();
        String type = t.toString();
        Body body = p.getAnnotation(Body.class);
        if ((body == null) && TextUtils.notIn(type, T_File, T_Files, T_InputStream, T_ServletInputStream)) {
            body = ProcessEnvironment.getBody(p);
            if (body != null) {
                SourceParamCreator creator = env.getBodyCreator(servlet, t, body);
                if (creator instanceof SourceParamFail) {
                    addNoType(t.getKind());
                    env.error("Not support body type " + type + " without annotation Creator");
                } else {
                    String name = p.getSimpleName().toString();
                    args.addBody(name).write(creator.toCharSequence(servlet, args, name));
                }
                return true;
            }
            return false;
        }
        if (args.containsBody()) {
            args.addFallback("null");
            env.error("cannot read body twice");
            return true;
        }
        String name = p.getSimpleName().toString();
        switch (type) {
            case T_File:
                args.addBody(name).write(servlet.imports(T_File), " ", name, " = _req.getFile(\"", name, "\");");
                break;
            case T_Files:
                args.addBody(name).write(servlet.imports(T_File), "[] ", name, " = _req.getFiles();");
                break;
            case T_InputStream:
            case T_ServletInputStream:
                args.addBody(name).write(servlet.imports(T_ServletInputStream), " ", name, " = _req.getBody();");
                break;
            case T_Bytes:
                args.addBody(name).write("byte[] ", name, " = _req.getBodyAsByte();");
                break;
            case T_String:
                assert body != null;
                String charset = env.charset(body.charset());
                args.addBody(name).write("String ", name, " = _req.getBodyAsText(\"", charset, "\");");
                break;
            default:
                assert body != null;
                SourceParamCreator creator = env.getBodyCreator(servlet, t, body);
                if (creator instanceof SourceParamFail) {
                    addNoType(t.getKind());
                    env.error("Not support body type " + type + " without annotation Creator");
                } else {
                    args.addBody(name).write(creator.toCharSequence(servlet, args, name));
                }
        }
        return true;
    }

    private void addNoType(TypeKind k) {
        switch (k) {
            case BOOLEAN:
                args.addFallback("false");
                break;
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                args.addFallback("0");
                break;
            default:
                args.addFallback("null");
        }
    }

    private boolean doParam(VariableElement p) {
        TypeMirror t = p.asType();
        String type = t.toString();
        Param param = p.getAnnotation(Param.class);
        String alias = p.getSimpleName().toString();
        String fallback, name;
        if (param != null) {
            fallback = param.fallback();
            name = param.value().length() > 0 ? param.value() : alias;
        } else {
            fallback = "";
            name = alias;
        }
        switch (t.getKind()) {
            case INT: {
                String[] vs = "".equals(fallback.trim())
                        ? new String[]{"int ", alias, " = _req.getIntParam(\"", name, "\");"}
                        : new String[]{"int ", alias, " = _req.getIntParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias).write(vs);
                break;
            }
            case LONG: {
                String[] vs = "".equals(fallback.trim())
                        ? new String[]{"long ", alias, " = _req.getLongParam(\"", name, "\");"}
                        : new String[]{"long ", alias, " = _req.getLongParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias).write(vs);
                break;
            }
            case BOOLEAN: {
                String[] vs = "".equals(fallback.trim())
                        ? new String[]{"boolean ", alias, " = _req.getBoolParam(\"", name, "\");"}
                        : new String[]{"boolean ", alias, " = _req.getBoolParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias).write(vs);
                break;
            }
            case DECLARED: {
                if (T_String.equals(type)) {
                    String[] vs = "".equals(fallback.trim())
                            ? new String[]{"String ", alias, " = _req.getParameter(\"", name, "\");"}
                            : new String[]{"String ", alias, " = _req.getParameter(\"", name, "\", \"", fallback, "\");"};
                    args.addParam(type, name, alias).write(vs);
                } else if (T_Decimal.equals(type)) {
                    String clz = servlet.imports(T_Decimal);
                    String[] vs = "".equals(fallback.trim())
                            ? new String[]{clz, " ", alias, " = _req.getDecimalParam(\"", name, "\");"}
                            : new String[]{clz, " ", alias, " = _req.getDecimalParam(\"", name, "\", new " + clz
                            + "(\"" + fallback, "\");"};
                    args.addParam(type, name, alias).write(vs);
                } else {
                    args.addParam(type, name, alias).write(type, " ", alias, " = null;/* not support type */");
                    env.warning("not support param type " + type + " without annotation Creator");
                }
                break;
            }
            case ARRAY:
                switch (type) {
                    case T_Bools:
                        args.addParam(type, name, alias)
                                .write("boolean[] ", alias, " = _req.getBoolParams(\"", name, "\");");
                        break;
                    case T_Ints:
                        args.addParam(type, name, alias)
                                .write("int[] ", alias, " = _req.getIntParams(\"", name, "\");");
                        break;
                    case T_Longs:
                        args.addParam(type, name, alias)
                                .write("long[] ", alias, " = _req.getLongParams(\"", name, "\");");
                        break;
                    case T_Decimals:
                        args.addParam(type, name, alias)
                                .write(servlet.imports(T_Decimal), "[] ", alias, " = _req.getDecimalParams(\"", name, "\");");
                        break;
                    case T_Strings:
                        args.addParam(type, name, alias)
                                .write("String[] ", alias, " = _req.getParams(\"", name, "\");");
                        break;
                    default:
                        args.addFallback("null");
                        break;
                }
            default:
        }
        return true;
    }

    private void doReturn(ProcessEnvironment env, SourceServlet servlet) {
        ArrayList<CharSequence> throwList = args.throwList();
        boolean needTry = throwList.size() > 0;
        if (needTry) {
            servlet.append("try{");
        }
        TypeMirror t = method.getReturnType();
        TypeKind kind = t.getKind();
        switch (kind) {
            case ARRAY:
                doReturnSerialize(env, servlet, t);
                break;
            case VOID:
                servlet.imports("yeamy.restlite.addition.VoidResponse");
                servlet.append("new VoidResponse();}");
                break;
            default:// base type
                if (env.isHttpResponse(t)) {
                    servlet.append("this._impl.").append(method.getSimpleName()).append('(');
                    doReturnArguments(servlet);
                    servlet.append(").write(_resp);");
                } else if (env.responseAllType()) {
                    doReturnSerialize(env, servlet, t);
                } else if (env.isStream(t)) {
                    doReturnStream(servlet);
                } else if (T_String.equals(t.toString())) {
                    servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                            .append("(this._impl.").append(method.getSimpleName()).append('(');
                    doReturnArguments(servlet);
                    servlet.append(")).write(_resp);");
                } else if (T_Decimal.equals(t.toString())) {
                    servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                            .append("(this._impl.").append(method.getSimpleName()).append('(');
                    doReturnArguments(servlet);
                    servlet.append(").toPlainString()).write(_resp);");
                } else if (kind.isPrimitive()){// base type
                    servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                            .append("(String.valueOf(this._impl.").append(method.getSimpleName()).append('(');
                    doReturnArguments(servlet);
                    servlet.append("))).write(_resp);");
                } else {
                    doReturnSerialize(env, servlet, t);
                }
        }
        if (needTry) {
            servlet.append(");}catch(Exception e1){doError(_resp, e1);}");
            if (args.closeNoThrow().size() + args.closeThrow().size() > 0) {
                servlet.append("finally{");
                for (String name : args.closeNoThrow()) {
                    servlet.append(name).append(".close();");
                }
                for (String name : args.closeThrow()) {
                    servlet.append("try{").append(name).append(".close();}catch(Exception e2){doError(e2);}");
                }
                servlet.append('}');
            }
        }
    }

    private void doReturnSerialize(ProcessEnvironment env, SourceServlet servlet, TypeMirror rt) {
        String resp = env.getResponse();
        TypeElement type = env.getTypeElement(resp);
        ExecutableElement constructor;
        constructor = getConstructor(env, servlet, type, rt);
        if (constructor != null) {
            servlet.append("new ").append(servlet.imports(resp)).append("(this._impl.").append(method.getSimpleName())
                    .append('(');
            doReturnArguments(servlet);
            servlet.append(")).write(_resp);");
        } else {
            env.error("Cannot find target constructor of " + resp + " accept parameter: "+ rt);
            servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                    .append("(500,\"Cannot find target constructor\").write(_resp);");
        }
    }

    private ExecutableElement getConstructor(ProcessEnvironment env, SourceServlet servlet,
                                             TypeElement resp, TypeMirror rt) {
        String fpk = ((PackageElement) resp.getEnclosingElement()).getQualifiedName().toString();
        boolean samePackage = fpk.equals(servlet.getPackage());
        for (Element li : resp.getEnclosedElements()) {
            if (li.getKind() == ElementKind.CONSTRUCTOR
                    && (samePackage || li.getModifiers().contains(Modifier.PUBLIC))) {
                ExecutableElement constructor = (ExecutableElement) li;
                List<? extends VariableElement> params = constructor.getParameters();
                if (params.size() == 1 && env.isAssignable(rt, params.get(0).asType())) {
                    return constructor;
                }
            }
        }
        return null;
    }

    private void doReturnStream(SourceServlet servlet) {
        servlet.imports("yeamy.restlite.addition.StreamResponse");
        servlet.append("new StreamResponse(this._impl.").append(method.getSimpleName()).append('(');
        doReturnArguments(servlet);
        servlet.append(")).write(_resp);");
    }

    private void doReturnArguments(SourceServlet servlet) {
        int l = servlet.length();
        for (String name : args.getAlias()) {
            if (servlet.length() > l) {
                servlet.append(',');
            }
            servlet.append(name);
        }
    }

    public String name() {
        StringBuilder name = new StringBuilder(servlet.getImplClass()).append('.')
                .append(method.getSimpleName()).append('(');
        for (VariableElement a : arguments) {
            TypeMirror t = a.asType();
            if (t.getKind().isPrimitive()) {
                name.append(t).append(',');
            } else {
                name.append(env.getTypeElement(a.asType().toString()).getSimpleName()).append(',');
            }
        }
        name.deleteCharAt(name.length() - 1);
        name.append(')');
        return name.toString();
    }
}
