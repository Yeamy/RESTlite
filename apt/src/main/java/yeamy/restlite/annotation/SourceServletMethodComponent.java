package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.*;

class SourceServletMethodComponent {
    private final ProcessEnvironment env;
    private final SourceServlet servlet;
    private final ExecutableElement method;
    private final List<? extends VariableElement> arguments;
    private final SourceServiceName serverName;
    private final boolean async;
    private final long asyncTimeout;

    private final SourceArguments args = new SourceArguments();

    public SourceServletMethodComponent(ProcessEnvironment env, SourceServlet servlet, ExecutableElement method,
                                        boolean async, long asyncTimeout) {
        this.env = env;
        this.servlet = servlet;
        this.method = method;
        this.arguments = method.getParameters();
        this.serverName = new SourceServiceName(servlet.getRESTfulResource(), arguments);
        this.async = async;
        this.asyncTimeout = asyncTimeout;
    }

    final String orderKey() {
        return serverName.params;
    }

    public void create(String httpMethod) {
        for (VariableElement a : arguments) {
            if (!doRequest(a)
                    && !doAttribute(a)
                    && !doHeader(a)
                    && !doCookie(a)
                    && !doInject(a)
                    && !doBody(a)
                    && !doPart(a)) {
                doParam(a);
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

    private boolean doAttribute(VariableElement p) {
        Attribute attribute = p.getAnnotation(Attribute.class);
        if (attribute == null) {
            return false;
        }
        String alias = p.getSimpleName().toString();
        String name = attribute.value();
        if ("".equals(name)) {
            name = alias;
        }
        TypeMirror tm = p.asType();
        String type = tm.toString();
        String exist = args.getAttributeAlias(type, name);
        if (exist != null) {
            args.addExist(exist);
            return true;
        }
        if (tm.getKind().isPrimitive()) {
            args.addFallback("null/* not support primitive type */");
            env.warning("not support attribute type " + type + " without annotation Creator");
        } else {
            String iType = servlet.imports(tm);
            args.addAttribute(name, alias).write(iType, " ", alias, " = (_req.getAttributeAs(\"", name, "\") instanceof ", iType, "_a) ? _a : null;");
        }
        return true;
    }

    private boolean doHeader(VariableElement p) {
        Header ann = p.getAnnotation(Header.class);
        if (ann == null) {
            return false;
        }
        String alias = p.getSimpleName().toString();
        String name = ann.value();
        if ("".equals(name)) {
            name = alias;
        }
        TypeMirror tm = p.asType();
        String type = tm.toString();
        String exist = args.getHeaderAlias(type, name);
        if (exist != null) {
            args.addExist(exist);
            return true;
        }
        SourceHeader header = SourceVariableHelper.getHeader(env, servlet, p, ann);
        if (header != null) {
            args.addHeader(type, name, alias).write(header.write(servlet, name, alias));
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
        String name, alias;
        if (cookie != null) {
            alias = p.getSimpleName().toString();
            name = TextUtils.isEmpty(cookie.value()) ? alias : cookie.value();
        } else if (T_Cookie.equals(type)) {
            name = alias = p.getSimpleName().toString();
        } else {
            return false;
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

    private boolean doInject(VariableElement p) {
        Inject ann = p.getAnnotation(Inject.class);
        if (ann == null) return false;
        SourceInject inject = SourceVariableHelper.getInject(env, servlet, p, ann);
        args.addInject(p.getSimpleName().toString(), inject.isThrowable(), inject.isCloseable(), inject.isCloseThrow())
                .write(inject.writeArg(servlet));
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
        if (body == null) {
            if (!T_ServletInputStream.equals(type)) {
                body = ProcessEnvironment.getBody(p);
                if (body == null) return false;
            }
        }
        String name = p.getSimpleName().toString();
        switch (type) {
            case T_InputStream, T_ServletInputStream -> args.addBody(name, true, true, true)
                    .write(servlet.imports(T_ServletInputStream), " ", name, " = _req.getBody();");
            case T_ByteArray -> args.addBody(name, false, false, false)
                    .write("byte[] ", name, " = _req.getBodyAsByte();");
            case T_String -> args.addBody(name, false, false, false)
                    .write("String ", name, " = _req.getBodyAsText(\"", env.charset(body.charset()), "\");");
            default -> SourceArgsBody.get(env, servlet, p, body).addToArgs(args, servlet, p, name);
        }
        return true;
    }

    private boolean doPart(VariableElement p) {
        if (args.containsBody()) {
            args.addFallback("null");
            env.error("cannot read part twice");
            return true;
        }
        String name = p.getSimpleName().toString();
        TypeMirror t = p.asType();
        String type = t.toString();
        if (type.equals(T_Parts)) {
            args.addPart(name, name, false, false, false)
                    .write(servlet.imports(T_File), "[] ", name, " = _req.getParts();");
            return true;
        } else if (type.equals(T_Files)) {
            args.addPart(name, name, false, false, false)
                    .write(servlet.imports(T_File), "[] ", name, " = _req.getFiles();");
            return true;
        }
        Part part = p.getAnnotation(Part.class);
        if (part == null) {
            if (type.equals(T_Part)) {
                args.addPart(name, name, false, false, false)
                        .write(servlet.imports(T_File), " ", name, " = _req.getPart(\"", name, "\");");
                return true;
            } else if (type.equals(T_File)) {
                args.addPart(name, name, false, false, false)
                        .write(servlet.imports(T_File), " ", name, " = _req.getFile(\"", name, "\");");
                return true;
            } else {
                part = ProcessEnvironment.getPart(p);
            }
        }
        if (part == null) return false;
        String alias = TextUtils.isEmpty(part.value()) ? name : part.value();
        switch (type) {
            case T_Part -> args.addPart(name, alias, false, false, false)
                    .write(servlet.imports(T_File), " ", name, " = _req.getPart(\"", alias, "\");");
            case T_File -> args.addPart(name, alias, false, false, false)
                    .write(servlet.imports(T_File), " ", name, " = _req.getFile(\"", alias, "\");");
            case T_InputStream -> args.addPart(name, alias, true, true, true)
                    .write(servlet.imports(T_InputStream), " ", name, " = ", servlet.imports("yeamy.utils.IfNotNull"), ".invoke(_req.getFile(\"", alias, "\"),a->a.get());");
            case T_ByteArray -> args.addPart(name, alias, false, false, false)
                    .write("byte[] ", name, " = ", servlet.imports("yeamy.utils.IfNotNull"), ".invoke(_req.getFile(\"", alias, "\"),a->a.getAsByte());");
            case T_String -> args.addPart(name, alias, false, false, false)
                    .write("String ", name, " = ", servlet.imports("yeamy.utils.IfNotNull"), ".invoke(_req.getFile(\"", alias, "\"),a->a.getAsText(\"", env.charset(part.charset()), "\");");
            default -> {
                SourceArgsPart.get(env, servlet, p, part).addToArgs(args, servlet, p, name);
                return true;
            }
        }
        return true;
    }

    private void doParam(VariableElement p) {
        TypeMirror t = p.asType();
        String type = t.toString();
        String alias = p.getSimpleName().toString();
        String name;
        Param param = p.getAnnotation(Param.class);
        if (param != null) {
            name = param.value().length() > 0 ? param.value() : alias;
            SourceArgs processor = SourceArgsParam.get(env, servlet, p, param);
            if (processor != null) {
                processor.addToArgs(args, servlet, p, name);
                return;
            }
        } else {
            name = alias;
        }
        SourceArguments.Impl arg = args.addParam(type, name, alias);
        switch (t.getKind()) {
            case INT -> arg.write("int ", alias, " = _req.getIntParam(\"", name, "\",0);");
            case LONG -> arg.write("long ", alias, " = _req.getLongParam(\"", name, "\",0);");
            case FLOAT -> arg.write("float ", alias, " = _req.getFloatParam(\"", name, "\",0);");
            case DOUBLE -> arg.write("double ", alias, " = _req.getDoubleParam(\"", name, "\",0);");
            case BOOLEAN -> arg.write("boolean ", alias, " = _req.getBoolParam(\"", name, "\",false);");
            case DECLARED -> {
                switch (type) {
                    case T_String -> arg.write("String ", alias, " = _req.getParameter(\"", name, "\");");
                    case T_Integer -> arg.write("Integer ", alias, " = _req.getIntegerParam(\"", name, "\");");
                    case T_Long -> arg.write("Long ", alias, " = _req.getLongParam(\"", name, "\");");
                    case T_Float -> arg.write("Float ", alias, " = _req.getFloatParam(\"", name, "\");");
                    case T_Double -> arg.write("Double ", alias, " = _req.getDoubleParam(\"", name, "\");");
                    case T_Boolean -> arg.write("Boolean ", alias, " = _req.getBooleanParam(\"", name, "\");");
                    case T_Decimal ->
                            arg.write(servlet.imports(T_Decimal), " ", alias, " = _req.getDecimalParam(\"", name, "\");");
                    default -> {
                        //TODO
                        arg.write(type, " ", alias, " = null;/* not support type */");
                        env.warning("not support param type " + type + " without annotation Creator");
                    }
                }
            }
            case ARRAY -> {
                switch (type) {
                    case T_IntegerArray -> arg.write("Integer[] ", alias, " = _req.getIntegerParams(\"", name, "\");");
                    case T_LongArray -> arg.write("Long[] ", alias, " = _req.getLongParams(\"", name, "\");");
                    case T_FloatArray -> arg.write("Float[] ", alias, " = _req.getFloatParams(\"", name, "\");");
                    case T_DoubleArray -> arg.write("Double[] ", alias, " = _req.getDoubleParams(\"", name, "\");");
                    case T_StringArray -> arg.write("String[] ", alias, " = _req.getParams(\"", name, "\");");
                    case T_BooleanArray -> arg.write("Boolean[] ", alias, " = _req.getBoolParams(\"", name, "\");");
                    case T_DecimalArray ->
                            arg.write(servlet.imports(T_Decimal), "[] ", alias, " = _req.getDecimalParams(\"", name, "\");");
                    default -> args.addFallback("null");
                    //TODO
                }
            }
        }
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
        String pkg = ((PackageElement) resp.getEnclosingElement()).getQualifiedName().toString();
        boolean samePackage = pkg.equals(servlet.pkg);
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
