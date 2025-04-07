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
        String name = p.getSimpleName().toString();
        args.addInject(name, inject.isThrowable(), inject.isCloseable(), inject.isCloseThrow())
                .write(inject.writeArg(servlet));
        return true;
    }

    private boolean doBody(VariableElement p) {
        Body ann = p.getAnnotation(Body.class);
        if (ann != null) {
            if (args.containsBodyOrPart()) {
                args.addFallback("null");
                env.error("cannot read body twice");
                return true;
            }
            SourceBody body = SourceVariableHelper.getBody(env, servlet, p, ann);
            if (body != null) {
                String name = p.getSimpleName().toString();
                args.addBody(name, body.isThrowable(), body.isCloseable(), body.isCloseThrow()).write(body.write(servlet, name));
            } else {
                args.addFallback("null");
            }
            return true;
        }
        String type = p.asType().toString();
        if (TextUtils.in(type, T_ServletInputStream, T_PartArray, T_FileArray)) {
            if (args.containsBodyOrPart()) {
                args.addFallback("null");
                env.error("cannot read body twice");
                return true;
            }
            SourceBody body = new SourceBodyDefault(env, p, type);
            String name = p.getSimpleName().toString();
            args.addBody(name, body.isThrowable(), body.isCloseable(), body.isCloseThrow()).write(body.write(servlet, name));
            return true;
        }
        BodyFactory factory = ProcessEnvironment.getBodyFactory(p);
        if (factory != null) {
            if (args.containsBodyOrPart()) {
                args.addFallback("null");
                env.error("cannot read body twice");
                return true;
            }
            SourceBody body = SourceVariableHelper.getBody(env, p, factory);
            if (body != null) {
                String name = p.getSimpleName().toString();
                args.addBody(name, body.isThrowable(), body.isCloseable(), body.isCloseThrow()).write(body.write(servlet, name));
            } else {
                args.addFallback("null");
            }
            return true;
        }
        return false;
    }

    private boolean doPart(VariableElement p) {
        Parts ann = p.getAnnotation(Parts.class);
        if (ann != null) {
            if (args.containsBody()) {
                args.addFallback("null");
                env.error("cannot read body twice");
                return true;
            }
            String alias = p.getSimpleName().toString();
            String name = TextUtils.isEmpty(ann.value()) ? alias : ann.value();
            if (args.containsPart(name)) {
                args.addFallback("null");
                env.error("cannot read part twice");
                return true;
            }
            SourcePart part = SourceVariableHelper.getPart(env, servlet, p, ann);
            if (part != null) {
                args.addPart(name, alias, part.isThrowable(), part.isCloseable(), part.isCloseThrow())
                        .write(part.write(servlet, name, alias));
            } else {
                args.addFallback("null");
            }
            return true;
        }
        String type = p.asType().toString();
        if (TextUtils.in(type, T_InputStream, T_Part, T_File)) {
            if (args.containsBody()) {
                args.addFallback("null");
                env.error("cannot read body twice");
                return true;
            }
            String name = p.getSimpleName().toString();
            if (args.containsPart(name)) {
                args.addFallback("null");
                env.error("cannot read part twice");
                return true;
            }
            SourcePart part = new SourcePartDefault(env, p, type);
            args.addPart(name, name, part.isThrowable(), part.isCloseable(), part.isCloseThrow())
                    .write(part.write(servlet, name, name));
            return true;
        }
        PartFactoryBean factory = PartFactoryBean.get(p);
        if (factory != null) {
            if (args.containsBody()) {
                args.addFallback("null");
                env.error("cannot read body twice");
                return true;
            }
            String alias = p.getSimpleName().toString();
            String name = TextUtils.isEmpty(factory.name()) ? alias : factory.name();
            if (args.containsPart(name)) {
                args.addFallback("null");
                env.error("cannot read part twice");
                return true;
            }
            SourcePartByExecutable part = SourceVariableHelper.getPartByFactory(env, p, factory);
            if (part != null) {
                args.addPart(name, alias, part.isThrowable(), part.isCloseable(), part.isCloseThrow())
                        .write(part.write(servlet, name, alias));
            } else {
                args.addFallback("null");
            }
            return true;
        }
        return false;
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
