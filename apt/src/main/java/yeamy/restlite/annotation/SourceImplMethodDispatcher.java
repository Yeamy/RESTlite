package yeamy.restlite.annotation;

import yeamy.restlite.utils.TextUtils;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static yeamy.restlite.annotation.SourceParamProcessor.SUPPORT_PARAM_TYPE;
import static yeamy.restlite.annotation.SupportType.*;

class SourceImplMethodDispatcher {
    private final ProcessEnvironment env;
    private final SourceServlet servlet;
    private final ExecutableElement method;
    private final List<? extends VariableElement> arguments;
    private final SourceImplMethodName methodName;
    private final boolean async;
    private final long asyncTimeout;
    private final String permission;
    private final HashSet<String> throwTypes = new HashSet<>();

    private final SourceArguments args = new SourceArguments();

    public SourceImplMethodDispatcher(ProcessEnvironment env, SourceServlet servlet, ExecutableElement method,
                                      boolean async, long asyncTimeout, String permission) {
        this.env = env;
        this.servlet = servlet;
        this.method = method;
        this.arguments = method.getParameters();
        this.methodName = new SourceImplMethodName(servlet.getRESTfulResource(), arguments);
        this.async = async;
        this.asyncTimeout = asyncTimeout;
        this.permission = permission;
        arguments.forEach(a -> {
            if (doRequest(a) || doAttribute(a) || doHeader(a) || doCookie(a) || doInject(a) || doBody(a) || doPart(a)) {
                return;
            }
            doParam(a);
        });
        throwTypes.addAll(ProcessEnvironment.getThrowType(method));
    }

    final String orderKey() {
        return methodName.params;
    }

    public void createInSwitch(String httpMethod) {
        String key = env.addImplMethod(httpMethod, methodName);
        // check arguments
        if (methodName.isNoParam()) {
            servlet.append("default:{");
        } else {
            servlet.append("case \"").append(key).append("\":{");
        }
        create(httpMethod);
        servlet.append("break;}");
    }

    public void create(String httpMethod) {
        for (SourcePermissionHandle h : env.getPermissionHandle()) {
            h.write(servlet, permission);
        }
        if (async) {
            servlet.append(servlet.imports("jakarta.servlet.AsyncContext")).append(" _asyncContext = _req.startAsync();");
            if (asyncTimeout > 0) {
                servlet.append("_asyncContext.setTimeout(").append(asyncTimeout).append("\");\"");
            }
            servlet.append("_asyncContext.start(() -> {try {");
        }
        // get arguments
        args.getNormal().forEach(servlet::append);
        // try
        ArrayList<CharSequence> closeable = args.getCloseable();
        if (!closeable.isEmpty()) {
            servlet.append("try(");
            for (CharSequence g : closeable) {
                servlet.append(g);
            }
            servlet.deleteLast(1).append("){");
            for (CharSequence g : args.getInTry()) {
                servlet.append(g);
            }
            // return
            doReturn(env, servlet, httpMethod);
            if (servlet.containsError() && args.hasThrow()) {
                servlet.append("}catch(Exception e){onError(_req, _resp, e);}");
            } else {
                servlet.append('}');
            }
        } else {
            ArrayList<CharSequence> inTry = args.getInTry();
            if (!inTry.isEmpty()) {
                servlet.append("try{");
                for (CharSequence g : inTry) {
                    servlet.append(g);
                }
                // return
                doReturn(env, servlet, httpMethod);
                servlet.append("}catch(Exception e){onError(_req, _resp, e);}");
            } else {
                // return
                doReturn(env, servlet, httpMethod);
            }
        }
        if (async) {
            servlet.append("}catch(Exception e){onError(_req,_resp,e);}finally{_asyncContext.complete();}});");
        }
    }

    private boolean doRequest(VariableElement p) {
        String type = p.asType().toString();
        if (T_RESTfulRequest.equals(type)) {
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
        Attribute ann = p.getAnnotation(Attribute.class);
        if (ann == null) {
            return false;
        }
        String alias = p.getSimpleName().toString();
        String name = name(ann, alias);
        TypeMirror tm = p.asType();
        String type = tm.toString();
        String exist = args.getAttributeAlias(type, name);
        if (exist != null) {
            args.addExist(exist);
            return true;
        }
        if (tm.getKind().isPrimitive()) {
            args.addFallback(p);
            env.warning("not support attribute type " + type + " without annotation Creator");
        } else {
            String iType = servlet.imports(tm);
            args.addAttribute(name, alias).write(iType, " ", alias, " = (_req.getAttribute(\"", name, "\") instanceof ", iType, " _a) ? _a : null;");
        }
        return true;
    }

    private boolean doHeader(VariableElement p) {
        Header ann = p.getAnnotation(Header.class);
        if (ann == null) {
            return false;
        }
        String alias = p.getSimpleName().toString();
        String name = name(ann, alias);
        String type = p.asType().toString();
        String exist = args.getHeaderAlias(type, name);
        if (exist != null) {
            args.addExist(exist);
            return true;
        }
        SourceHeader header = SourceVariableHelper.getHeader(env, servlet, p, ann);
        if (header != null) {
            throwTypes.addAll(header.throwTypes());
            args.addHeader(type, name, alias).write(header.write(servlet, name, alias));
        } else {
            args.addFallback(p);
            env.warning("not support header type " + type + " without annotation Creator");
        }
        return true;
    }

    private boolean doCookie(VariableElement p) {
        String type = p.asType().toString();
        Cookies ann = p.getAnnotation(Cookies.class);
        if (ann != null) {
            String alias = p.getSimpleName().toString();
            String name = name(ann, alias);
            SourceCookie cookie = SourceVariableHelper.getCookie(env, servlet, p, ann);
            if (cookie != null) {
                throwTypes.addAll(cookie.throwTypes());
                args.addCookie(type, name, alias).write(cookie.write(servlet, name, alias));
            } else {
                args.addFallback(p);
                env.warning("Not support cookie type " + type + " without annotation Creator");
            }
            return true;
        }
        SourceFactory<CookieFactory> factory = ProcessEnvironment.getCookieFactory(p);
        if (factory != null) {
            String alias = p.getSimpleName().toString();
            String name = name(factory, alias);
            SourceCookie cookie = SourceVariableHelper.getCookieByFactory(env, p, factory);
            if (cookie != null) {
                throwTypes.addAll(cookie.throwTypes());
                args.addCookie(type, name, alias).write(cookie.write(servlet, name, alias));
            } else {
                args.addFallback(p);
            }
            return true;
        }
        if (TextUtils.in(type, T_Cookie, T_CookieArray)) {
            String name = p.getSimpleName().toString();
            String exist = args.getCookieAlias(type, name);
            if (exist != null) {
                args.addExist(exist);
                return true;
            }
            SourceCookie cookie = new SourceCookieDefault(env, p, type);
            throwTypes.addAll(cookie.throwTypes());
            args.addCookie(type, name, name).write(cookie.write(servlet, name, name));
            return true;
        }
        return false;
    }

    private boolean doInject(VariableElement p) {
        Inject ann = p.getAnnotation(Inject.class);
        if (ann == null) return false;
        SourceInject inject = SourceVariableHelper.getInject(env, servlet, p, ann);
        String name = p.getSimpleName().toString();
        throwTypes.addAll(inject.throwTypes());
        args.addInject(name, inject.isThrowable(), inject.isCloseable(), inject.isCloseThrow())
                .write(inject.writeArg(servlet));
        return true;
    }

    private boolean doBody(VariableElement p) {
        Body ann = p.getAnnotation(Body.class);
        if (ann != null) {
            if (args.containsBodyOrPart()) {
                args.addFallback(p);
                env.error("cannot read body twice");
                return true;
            }
            SourceBody body = SourceVariableHelper.getBody(env, servlet, p, ann);
            if (body != null) {
                String name = p.getSimpleName().toString();
                throwTypes.addAll(body.throwTypes());
                args.addBody(name, body.isThrowable(), body.isCloseable(), body.isCloseThrow()).write(body.write(servlet, name));
            } else {
                args.addFallback(p);
            }
            return true;
        }
        String type = p.asType().toString();
        if (TextUtils.in(type, T_ServletInputStream, T_PartArray, T_HttpRequestFileArray)) {
            if (args.containsBodyOrPart()) {
                args.addFallback(p);
                env.error("cannot read body twice");
                return true;
            }
            SourceBody body = new SourceBodyDefault(env, p, type);
            String name = p.getSimpleName().toString();
            throwTypes.addAll(body.throwTypes());
            args.addBody(name, body.isThrowable(), body.isCloseable(), body.isCloseThrow()).write(body.write(servlet, name));
            return true;
        }
        BodyFactory factory = ProcessEnvironment.getBodyFactory(p);
        if (factory != null) {
            if (args.containsBodyOrPart()) {
                args.addFallback(p);
                env.error("cannot read body twice");
                return true;
            }
            SourceBody body = SourceVariableHelper.getBody(env, p, factory);
            if (body != null) {
                String name = p.getSimpleName().toString();
                throwTypes.addAll(body.throwTypes());
                args.addBody(name, body.isThrowable(), body.isCloseable(), body.isCloseThrow()).write(body.write(servlet, name));
            } else {
                args.addFallback(p);
            }
            return true;
        }
        return false;
    }

    private boolean doPart(VariableElement p) {
        Parts ann = p.getAnnotation(Parts.class);
        if (ann != null) {
            if (args.containsBody()) {
                args.addFallback(p);
                env.error("cannot read body twice");
                return true;
            }
            String alias = p.getSimpleName().toString();
            String name = name(ann, alias);
            if (args.containsPart(name)) {
                args.addFallback(p);
                env.error("cannot read part twice");
                return true;
            }
            SourcePart part = SourceVariableHelper.getPart(env, servlet, p, ann);
            if (part != null) {
                throwTypes.addAll(part.throwTypes());
                args.addPart(name, alias, part.isThrowable(), part.isCloseable(), part.isCloseThrow())
                        .write(part.write(servlet, name, alias));
            } else {
                args.addFallback(p);
            }
            return true;
        }
        String type = p.asType().toString();
        if (TextUtils.in(type, T_InputStream, T_Part, T_HttpRequestFile)) {
            if (args.containsBody()) {
                args.addFallback(p);
                env.error("cannot read body twice");
                return true;
            }
            String name = p.getSimpleName().toString();
            if (args.containsPart(name)) {
                args.addFallback(p);
                env.error("cannot read part twice");
                return true;
            }
            SourcePart part = new SourcePartDefault(env, p, type);
            throwTypes.addAll(part.throwTypes());
            args.addPart(name, name, part.isThrowable(), part.isCloseable(), part.isCloseThrow())
                    .write(part.write(servlet, name, name));
            return true;
        }
        SourceFactory<PartFactory> factory = ProcessEnvironment.getPartFactory(p);
        if (factory != null) {
            if (args.containsBody()) {
                args.addFallback(p);
                env.error("cannot read body twice");
                return true;
            }
            String alias = p.getSimpleName().toString();
            String name = name(factory, alias);
            if (args.containsPart(name)) {
                args.addFallback(p);
                env.error("cannot read part twice");
                return true;
            }
            SourcePart part = SourceVariableHelper.getPartByFactory(env, p, factory);
            if (part != null) {
                throwTypes.addAll(part.throwTypes());
                args.addPart(name, alias, part.isThrowable(), part.isCloseable(), part.isCloseThrow())
                        .write(part.write(servlet, name, alias));
            } else {
                args.addFallback(p);
            }
            return true;
        }
        return false;
    }

    private void doParam(VariableElement p) {
        String type = p.asType().toString();
        Param ann = p.getAnnotation(Param.class);
        if (ann != null) {
            String alias = p.getSimpleName().toString();
            String name = name(ann, alias);
            SourceParam param = SourceVariableHelper.getParam(env, servlet, p);
            if (param != null) {
                throwTypes.addAll(param.throwTypes());
                args.addParam(type, name, alias).write(param.write(servlet, name, alias));
            } else {
                args.addFallback(p);
                env.warning("Not support param type " + type + " without annotation Creator");
            }
            return;
        }
        SourceFactory<ParamFactory> factory = ProcessEnvironment.getParamFactory(p);
        if (factory != null) {
            String alias = p.getSimpleName().toString();
            String name = name(factory, alias);
            SourceParam param = SourceVariableHelper.getParam(env, p, factory);
            if (param != null) {
                throwTypes.addAll(param.throwTypes());
                args.addParam(type, name, alias).write(param.write(servlet, name, alias));
            } else {
                args.addFallback(p);
            }
            return;
        }
        String name = p.getSimpleName().toString();
        if (TextUtils.notIn(type, SUPPORT_PARAM_TYPE)) {
            SourceParam param = SourceVariableHelper.getParam(env, servlet, p);
            if (param != null) {
                throwTypes.addAll(param.throwTypes());
                args.addParam(type, name, name).write(param.write(servlet, name, name));
            } else {
                args.addFallback(p);
                env.warning("Not support param type " + type + " without annotation Creator");
            }
            return;
        }
        SourceParam param = new SourceParamDefault(env, p, type);
        throwTypes.addAll(param.throwTypes());
        args.addParam(type, name, name).write(param.write(servlet, name, name));
    }

    private void doReturn(ProcessEnvironment env, SourceServlet servlet, String httpMethod) {
        TypeMirror t = method.getReturnType();
        TypeKind kind = t.getKind();
        switch (kind) {
            case ARRAY -> doReturnSerialize(env, servlet, httpMethod, t);
            case VOID -> servlet.append(servlet.imports("yeamy.restlite.addition.VoidResponse"))
                    .append("DELETE".equals(httpMethod) ? ".RESET_CONTENT.write(_resp);" : ".NO_CONTENT.write(_resp);");
            default -> {// base type
                if (env.isHttpResponse(t)) {
                    servlet.append("this._impl.").append(method.getSimpleName()).append('(');
                    doReturnArguments(servlet);
                    servlet.append(").write(_resp);");
                } else if (env.responseAllType()) {
                    doReturnSerialize(env, servlet, httpMethod, t);
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
                    doReturnSerialize(env, servlet, httpMethod, t);
                }
            }
        }
    }

    private void doReturnSerialize(ProcessEnvironment env, SourceServlet servlet, String httpMethod, TypeMirror rt) {
        SourceResponse resp = env.getResponse();
        resp.write(env, servlet, httpMethod, rt, () -> {
            servlet.append("this._impl.").append(method.getSimpleName()).append('(');
            doReturnArguments(servlet);
            servlet.append(')');
        });
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

    public boolean isNoParam() {
        return methodName.isNoParam();
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

    public Collection<String> throwTypes() {
        return throwTypes;
    }

    private static String name(Attribute ann, String alias) {
        return !ann.value().isEmpty() ? ann.value() : !ann.name().isEmpty() ? ann.name() : alias;
    }

    private static String name(Header ann, String alias) {
        return !ann.value().isEmpty() ? ann.value() : !ann.name().isEmpty() ? ann.name() : alias;
    }

    private static String name(Cookies ann, String alias) {
        return !ann.value().isEmpty() ? ann.value() : !ann.name().isEmpty() ? ann.name() : alias;
    }

    private static String name(Param ann, String alias) {
        return !ann.value().isEmpty() ? ann.value() : !ann.name().isEmpty() ? ann.name() : alias;
    }

    private static String name(Parts ann, String alias) {
        return !ann.value().isEmpty() ? ann.value() : !ann.name().isEmpty() ? ann.name() : alias;
    }

    private static String name(SourceFactory<?> factory, String alias) {
        return !factory.name().isEmpty() ? factory.name() : alias;
    }
}
