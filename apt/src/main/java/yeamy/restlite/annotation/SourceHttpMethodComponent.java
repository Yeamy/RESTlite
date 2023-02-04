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
                    || doInject(a)
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
            for (CharSequence g : args.getNormal()) {
                servlet.append(g);
            }
            // try
            ArrayList<CharSequence> closeable = args.getCloseable();
            if (closeable.size() > 0) {
                servlet.append("try(");
                for (CharSequence g : closeable) {
                    servlet.append(g);
                }
                servlet.deleteLast(1).append("){");
                for (CharSequence g : args.getInTry()) {
                    servlet.append(g);
                }
                // return
                doReturn(env, servlet);
                if (servlet.containsError() && args.hasThrow()) {
                    servlet.append("}catch(Exception e){onError(_req, _resp, e);}");
                }
            } else {
                ArrayList<CharSequence> inTry = args.getInTry();
                if (inTry.size() > 0) {
                    servlet.append("try{");
                    for (CharSequence g : inTry) {
                        servlet.append(g);
                    }
                    // return
                    doReturn(env, servlet);
                    servlet.append("}catch(Exception e){onError(_req, _resp, e);}");
                } else {
                    // return
                    doReturn(env, servlet);
                }
            }
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
        if (args.containsBody()) {
            args.addFallback("null");
            env.error("cannot read body twice");
            return true;
        }
        TypeMirror t = p.asType();
        String type = t.toString();
        Body body = p.getAnnotation(Body.class);
        if ((body == null) || TextUtils.in(type, T_Part, T_Parts, T_File, T_Files, T_InputStream, T_ServletInputStream)) {
            String name = p.getSimpleName().toString();
            switch (type) {
                case T_Part -> args.addBody(name, false, false, false)
                        .write(servlet.imports(T_File), " ", name, " = _req.getPart(\"", name, "\");");
                case T_Parts -> args.addBody(name, false, false, false)
                        .write(servlet.imports(T_File), "[] ", name, " = _req.getParts();");
                case T_File -> args.addBody(name, false, false, false)
                        .write(servlet.imports(T_File), " ", name, " = _req.getFile(\"", name, "\");");
                case T_Files -> args.addBody(name, false, false, false)
                        .write(servlet.imports(T_File), "[] ", name, " = _req.getFiles();");
                case T_InputStream, T_ServletInputStream -> args.addBody(name, true, true, true)
                        .write(servlet.imports(T_ServletInputStream), " ", name, " = _req.getBody();");
                default -> {
                    body = ProcessEnvironment.getBody(p);
                    if (body == null) return false;
                    SourceParam creator = env.getBodyCreator(servlet, t, body);
                    if (creator instanceof SourceParamFail) {
                        addNoType(t.getKind());
                        env.error("Not support body type " + type + " without annotation Creator");
                    } else {
                        args.addBody(name, creator.isThrowable(), creator.isCloseable(), creator.isCloseThrow())
                                .write(creator.toCharSequence(servlet, args, name));
                    }
                    return true;
                }
            }
            return true;
        }
        String name = p.getSimpleName().toString();
        switch (type) {
            case T_Bytes -> args.addBody(name, false, false, false)
                    .write("byte[] ", name, " = _req.getBodyAsByte();");
            case T_String -> args.addBody(name, false, false, false)
                    .write("String ", name, " = _req.getBodyAsText(\"", env.charset(body.charset()), "\");");
            default -> {
                SourceParam creator = env.getBodyCreator(servlet, t, body);
                if (creator instanceof SourceParamFail) {
                    addNoType(t.getKind());
                    env.error("Not support body type " + type + " without annotation Creator");
                } else {
                    args.addBody(name, creator.isThrowable(), creator.isCloseable(), creator.isCloseThrow())
                            .write(creator.toCharSequence(servlet, args, name));
                }
            }
        }
        return true;
    }

    private boolean doInject(VariableElement p) {
        TypeMirror t = p.asType();
        String type = t.toString();
        Inject inject = p.getAnnotation(Inject.class);
        if (inject == null) return false;
        String name = p.getSimpleName().toString();
        SourceParam creator = env.getInjectParam(servlet, p, inject);
        if (creator instanceof SourceParamFail) {
            addNoType(t.getKind());
            env.error("Not support body type " + type + " without annotation Creator");
        } else {
            args.addInject(name, creator.isThrowable(), creator.isCloseable(), creator.isCloseThrow())
                    .write(creator.toCharSequence(servlet, args, name));
        }
        return true;
    }

    private void addNoType(TypeKind k) {
        switch (k) {
            case BOOLEAN -> args.addFallback("false");
            case BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE -> args.addFallback("0");
            default -> args.addFallback("null");
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
                String[] vs = "".equals(fallback)
                        ? new String[]{"int ", alias, " = _req.getIntParam(\"", name, "\");"}
                        : new String[]{"int ", alias, " = _req.getIntParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias).write(vs);
                break;
            }
            case LONG: {
                String[] vs = "".equals(fallback)
                        ? new String[]{"long ", alias, " = _req.getLongParam(\"", name, "\");"}
                        : new String[]{"long ", alias, " = _req.getLongParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias).write(vs);
                break;
            }
            case BOOLEAN: {
                String[] vs = "".equals(fallback)
                        ? new String[]{"boolean ", alias, " = _req.getBoolParam(\"", name, "\");"}
                        : new String[]{"boolean ", alias, " = _req.getBoolParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias).write(vs);
                break;
            }
            case DECLARED: {
                if (T_String.equals(type)) {
                    String[] vs = "".equals(fallback)
                            ? new String[]{"String ", alias, " = _req.getParameter(\"", name, "\");"}
                            : new String[]{"String ", alias, " = _req.getParameter(\"", name, "\", \"", SourceClass.convStr(fallback).toString(), "\");"};
                    args.addParam(type, name, alias).write(vs);
                } else if (T_Decimal.equals(type)) {
                    String clz = servlet.imports(T_Decimal);
                    String[] vs = "".equals(fallback)
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
                    case T_Booleans -> args.addParam(type, name, alias)
                            .write("boolean[] ", alias, " = _req.getBoolParams(\"", name, "\");");
                    case T_Integers -> args.addParam(type, name, alias)
                            .write("int[] ", alias, " = _req.getIntParams(\"", name, "\");");
                    case T_Longs -> args.addParam(type, name, alias)
                            .write("long[] ", alias, " = _req.getLongParams(\"", name, "\");");
                    case T_Decimals -> args.addParam(type, name, alias)
                            .write(servlet.imports(T_Decimal), "[] ", alias, " = _req.getDecimalParams(\"", name, "\");");
                    case T_Strings -> args.addParam(type, name, alias)
                            .write("String[] ", alias, " = _req.getParams(\"", name, "\");");
                    default -> args.addFallback("null");
                }
            default:
        }
        return true;
    }

    private void doReturn(ProcessEnvironment env, SourceServlet servlet) {
        TypeMirror t = method.getReturnType();
        TypeKind kind = t.getKind();
        switch (kind) {
            case ARRAY -> doReturnSerialize(env, servlet, t);
            case VOID -> servlet.append(servlet.imports("yeamy.restlite.addition.VoidResponse"))
                    .append(".instance.write(_resp);");
            default -> {// base type
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
                } else if (kind.isPrimitive()) {// base type
                    servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                            .append("(String.valueOf(this._impl.").append(method.getSimpleName()).append('(');
                    doReturnArguments(servlet);
                    servlet.append("))).write(_resp);");
                } else {
                    doReturnSerialize(env, servlet, t);
                }
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
            env.error("Cannot find target constructor of " + resp + " accept parameter: " + rt);
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
