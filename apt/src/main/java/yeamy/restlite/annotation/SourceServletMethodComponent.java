package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static yeamy.restlite.annotation.SourceCookieProcessor.SUPPORT_COOKIE_TYPE;
import static yeamy.restlite.annotation.SourceParamProcessor.SUPPORT_PARAM_TYPE;
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
        String type = p.asType().toString();
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
        String type = p.asType().toString();
        Cookies ann = p.getAnnotation(Cookies.class);
        if (ann == null) {
            String name = p.getSimpleName().toString();
            String exist = args.getCookieAlias(type, name);
            if (exist != null) {
                args.addExist(exist);
                return true;
            }
            if (TextUtils.notIn(type, SUPPORT_COOKIE_TYPE)) {
                return false;
            }
            SourceCookie cookie = new SourceCookieDefault(env, p, type);
            args.addCookie(type, name, name).write(cookie.write(servlet, name, name));
            return true;
        }
        String alias = p.getSimpleName().toString();
        String name = TextUtils.isEmpty(ann.value()) ? alias : ann.value();
        String exist = args.getCookieAlias(type, name);
        if (exist != null) {
            args.addExist(exist);
            return true;
        }
        SourceCookie cookie = SourceVariableHelper.getCookie(env, servlet, p, ann);
        if (cookie != null) {
            args.addCookie(type, name, alias).write(cookie.write(servlet, name, alias));
        } else {
            args.addFallback("null/* not support cookie type */");
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
        String type = p.asType().toString();
        Param ann = p.getAnnotation(Param.class);
        if (ann == null) {
            String name = p.getSimpleName().toString();
            String exist = args.getParamAlias(type, name);
            if (exist != null) {
                args.addExist(exist);
                return;
            }
            if (TextUtils.notIn(type, SUPPORT_PARAM_TYPE)) {
                return;
            }
            SourceParam param = new SourceParamDefault(env, p, type);
            args.addParam(type, name, name).write(param.write(servlet, name, name));
            return;
        }
        String alias = p.getSimpleName().toString();
        String name = TextUtils.isEmpty(ann.value()) ? alias : ann.value();
        String exist = args.getParamAlias(type, name);
        if (exist != null) {
            args.addExist(exist);
            return;
        }
        SourceParam param = SourceVariableHelper.getParam(env, servlet, p, ann);
        if (param != null) {
            args.addParam(type, name, alias).write(param.write(servlet, name, alias));
        } else {
            args.addFallback("null/* not support param type */");
            env.warning("Not support param type " + type + " without annotation Creator");
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
