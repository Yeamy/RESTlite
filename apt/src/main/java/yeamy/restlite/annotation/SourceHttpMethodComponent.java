package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static yeamy.restlite.annotation.SupportType.*;

class SourceHttpMethodComponent {
    private final ProcessEnvironment env;
    private final SourceServlet servlet;
    private final ExecutableElement method;
    private final List<? extends VariableElement> arguments;
    private String orderKey;

    private final SourceArguments args = new SourceArguments();

    public SourceHttpMethodComponent(ProcessEnvironment env, SourceServlet servlet, ExecutableElement method) {
        this.env = env;
        this.servlet = servlet;
        this.method = method;
        this.arguments = method.getParameters();
    }

    final String orderKey() {
        if (orderKey != null) {
            return orderKey;
        }
        TreeSet<String> set = new TreeSet<>();
        for (VariableElement a : arguments) {
            Param pa = a.getAnnotation(Param.class);
            if (pa != null && pa.required()) {
                set.add(a.getSimpleName().toString());
            } else if (a.getAnnotation(Header.class) == null//
                    && a.getAnnotation(Cookies.class) == null//
                    && a.getAnnotation(Body.class) == null//
                    && ProcessEnvironment.getBody(a) == null) {
                set.add(a.getSimpleName().toString());
            } else {
                continue;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            sb.append(s).append(',');
        }
        int l = sb.length();
        orderKey = (l == 0) ? "" : sb.substring(0, l - 1);
        return orderKey;
    }

    public void create(String httpMethod) {
        boolean async;
        long asyncTimeout;
        switch (httpMethod) {
            case "GET":
                GET get = method.getAnnotation(GET.class);
                async = get.async();
                asyncTimeout = get.asyncTimeout();
                break;
            case "POST":
                POST post = method.getAnnotation(POST.class);
                async = post.async();
                asyncTimeout = post.asyncTimeout();
                break;
            case "DELETE":
                DELETE delete = method.getAnnotation(DELETE.class);
                async = delete.async();
                asyncTimeout = delete.asyncTimeout();
                break;
            case "PUT":
                PUT put = method.getAnnotation(PUT.class);
                async = put.async();
                asyncTimeout = put.asyncTimeout();
                break;
            case "PATCH":
                PATCH patch = method.getAnnotation(PATCH.class);
                async = patch.async();
                asyncTimeout = patch.asyncTimeout();
                break;
            default:
                async = false;
                asyncTimeout = 0L;
        }
        for (VariableElement a : arguments) {
            if (doRequest(a)
                    || doHeader(a)
                    || doCookie(a)
                    || doBody(a)
                    || doParam(a)) {
                continue;
            }
        }
        // check arguments
        ArrayList<String> rParams = args.getRequiredParams();
        if (rParams.size() == 0) {
            servlet.append('{');
            for (CharSequence g : args) {
                servlet.append(g);
            }
            doReturn(env, servlet);
            servlet.append('}');
            return;
        }
        servlet.append("if (");
        boolean first = true;
        for (String param : rParams) {
            if (first) {
                first = false;
            } else {
                servlet.append("&&");
            }
            servlet.append("_req.has(\"").append(param).append("\")");
        }
        servlet.append("){");
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
            servlet.append('}');
        } catch (Exception e) {
            env.error(e);
            servlet.deleteLast(servlet.length() - begin);
            servlet.append("return new ").append(servlet.imports(T_TextPlainResponse))
                    .append("(500, \"Server error!\");");
        }
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
        boolean required;
        String fallback, name;
        if (param != null) {
            required = param.required();
            fallback = param.fallback();
            name = param.value().length() > 0 ? param.value() : alias;
        } else {
            required = true;
            fallback = "";
            name = alias;
        }
        switch (t.getKind()) {
            case INT: {
                String[] vs = "".equals(fallback.trim())
                        ? new String[]{"int ", alias, " = _req.getIntParam(\"", name, "\");"}
                        : new String[]{"int ", alias, " = _req.getIntParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias, required).write(vs);
                break;
            }
            case LONG: {
                String[] vs = "".equals(fallback.trim())
                        ? new String[]{"long ", alias, " = _req.getLongParam(\"", name, "\");"}
                        : new String[]{"long ", alias, " = _req.getLongParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias, required).write(vs);
                break;
            }
            case BOOLEAN: {
                String[] vs = "".equals(fallback.trim())
                        ? new String[]{"boolean ", alias, " = _req.getBoolParam(\"", name, "\");"}
                        : new String[]{"boolean ", alias, " = _req.getBoolParam(\"", name, "\", ", fallback, ");"};
                args.addParam(type, name, alias, required).write(vs);
                break;
            }
            case DECLARED: {
                if (T_String.equals(type)) {
                    String[] vs = "".equals(fallback.trim())
                            ? new String[]{"String ", alias, " = _req.getParameter(\"", name, "\");"}
                            : new String[]{"String ", alias, " = _req.getParameter(\"", name, "\", \"", fallback, "\");"};
                    args.addParam(type, name, alias, required).write(vs);
                } else if (T_Decimal.equals(type)) {
                    String clz = servlet.imports(T_Decimal);
                    String[] vs = "".equals(fallback.trim())
                            ? new String[]{clz, " ", alias, " = _req.getDecimalParam(\"", name, "\");"}
                            : new String[]{clz, " ", alias, " = _req.getDecimalParam(\"", name, "\", new " + clz
                            + "(\"" + fallback, "\");"};
                    args.addParam(type, name, alias, required).write(vs);
                } else {
                    args.addParam(type, name, alias, required).write(type, " ", alias, " = null;/* not support type */");
                    env.warning("not support param type " + type + " without annotation Creator");
                }
                break;
            }
            case ARRAY:
                switch (type) {
                    case T_Bools:
                        args.addParam(type, name, alias, required)
                                .write("boolean[] ", alias, " = _req.getBoolParams(\"", name, "\");");
                        break;
                    case T_Ints:
                        args.addParam(type, name, alias, required)
                                .write("int[] ", alias, " = _req.getIntParams(\"", name, "\");");
                        break;
                    case T_Longs:
                        args.addParam(type, name, alias, required)
                                .write("long[] ", alias, " = _req.getLongParams(\"", name, "\");");
                        break;
                    case T_Decimals:
                        args.addParam(type, name, alias, required)
                                .write(servlet.imports(T_Decimal), "[] ", alias, " = _req.getDecimalParams(\"", name, "\");");
                        break;
                    case T_Strings:
                        args.addParam(type, name, alias, required)
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
        switch (t.getKind()) {
            case ARRAY:
                doReturnSerialize(env, servlet, t);
                break;
            case DECLARED:
                if (env.isHttpResponse(t)) {
                    servlet.append("this._impl.").append(method.getSimpleName()).append('(');
                    doReturnArguments(servlet);
                    servlet.append(").write(_resp);");
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
                } else {
                    doReturnSerialize(env, servlet, t);
                }
                break;
            case VOID:
                servlet.imports("yeamy.restlite.addition.VoidResponse");
                servlet.append("new VoidResponse();}");
                break;
            default:
                servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                        .append("(String.valueOf(this._impl.").append(method.getSimpleName()).append('(');
                doReturnArguments(servlet);
                servlet.append("))).write(_resp);");
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
            servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                    .append("(500,\"Cannot find target constructor\").write(_resp);");
        }
    }

    private ExecutableElement getConstructor(ProcessEnvironment env, SourceServlet servlet,
                                             TypeElement resp, TypeMirror rt) {
        TypeElement te = env.getTypeElement(rt.toString());
        String fpk = ((PackageElement) te.getEnclosingElement()).getQualifiedName().toString();
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
        for (VariableElement a: arguments) {
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
