package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.*;

/**
 * @see SourceArgsHeader
 * @see SourceArgsCookies
 * @see SourceArgsParam
 * @see SourceArgsPart
 * @see SourceArgsBody
 * @see SourceArgsInject
 * @see SourceArgsFail
 */
abstract class SourceArgs {
    protected final ProcessEnvironment env;
    private final TypeElement factoryType;
    private final ExecutableElement exec;
    protected final TypeMirror returnType;
    private final String charset;
    private boolean throwable = false;
    private boolean closeable = false;
    private boolean closeThrow = false;

    //----------------------------------------------------------------------

    protected static boolean isSamePackage(SourceServlet servlet, TypeElement te) {
        String fpk = ((PackageElement) te.getEnclosingElement()).getQualifiedName().toString();
        return fpk.equals(servlet.pkg);
    }

    protected static ExecutableElement findMethodByTag(List<? extends Element> elements, String tag, boolean samePackage) {
        for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                LinkTag[] tags = method.getAnnotationsByType(LinkTag.class);
                if (tags != null) {
                    for (LinkTag li : tags) {
                        if (TextUtils.in(tag, li.value())) {
                            Set<Modifier> mfs = method.getModifiers();
                            if ((samePackage || mfs.contains(Modifier.PUBLIC)) && mfs.contains(Modifier.STATIC)) {
                                return method;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    protected static LinkedList<ExecutableElement> findMethodByType(ProcessEnvironment env, TypeMirror type, List<? extends Element> elements, boolean samePackage) {
        LinkedList<ExecutableElement> methods = new LinkedList<>();
        for (Element element : elements) {
            if (element.getKind() == ElementKind.METHOD) {
                Set<Modifier> modifiers = element.getModifiers();
                if (modifiers.contains(Modifier.STATIC) && (modifiers.contains(Modifier.PUBLIC) || (samePackage && !modifiers.contains(Modifier.PRIVATE)))) {
                    ExecutableElement method = (ExecutableElement) element;
                    TypeMirror rt = method.getReturnType();
                    TypeKind rtk = rt.getKind();
                    if (rtk == TypeKind.TYPEVAR) {
                        Element e = env.asElement(rt);
                        if (e instanceof TypeParameterElement ee) {
                            List<? extends TypeMirror> bounds = ee.getBounds();
                            for (TypeMirror bound : bounds) {
                                if (env.isAssignable(type, bound)) {
                                    methods.add(method);
                                    break;
                                }
                            }
                        }
                    } else if (rtk != TypeKind.VOID && env.isAssignable(type, rt)) {
                        methods.add(method);
                    }
                }
            }
        }
        return methods;
    }

    protected static ExecutableElement findConstructor(List<? extends Element> list, String tag, boolean samePackage) {
        for (Element element : list) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) element;
                LinkTag[] tags = constructor.getAnnotationsByType(LinkTag.class);
                if (tags != null) {
                    for (LinkTag li : tags) {
                        if (TextUtils.in(tag, li.value())) {
                            Set<Modifier> mfs = constructor.getModifiers();
                            if (mfs.contains(Modifier.PUBLIC) || (samePackage && !mfs.contains(Modifier.PRIVATE))) {
                                return constructor;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    protected static LinkedList<ExecutableElement> allConstructor(List<? extends Element> list, boolean samePackage) {
        LinkedList<ExecutableElement> methods = new LinkedList<>();
        for (Element enclosed : list) {// find constructor
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                Set<Modifier> mfs = enclosed.getModifiers();
                if (mfs.contains(Modifier.PUBLIC) || (samePackage && !mfs.contains(Modifier.PRIVATE))) {
                    methods.add((ExecutableElement) enclosed);
                }
            }
        }
        return methods;
    }

    //----------------------------------------------------------------------

    public SourceArgs(ProcessEnvironment env, TypeElement factoryType, ExecutableElement exec, TypeMirror returnType) {
        this(env, factoryType, exec, returnType, null);
    }

    public SourceArgs(ProcessEnvironment env, TypeElement factoryType, ExecutableElement exec, TypeMirror returnType, String charset) {
        this.env = env;
        this.factoryType = factoryType;
        this.exec = exec;
        this.returnType = returnType;
        this.charset = charset;
    }

    protected void init(ExecutableElement method, boolean samePackage, List<? extends Element> elements) {
        if (method != null) {
            this.throwable = method.getThrownTypes().size() > 0;
        }
        this.closeable = env.isCloseable(factoryType.asType());
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

    public final CharSequence toCharSequence(SourceServlet servlet, SourceArguments args, String name) {
        String typeName = servlet.imports(returnType);
        StringBuilder b = new StringBuilder(typeName).append(" ").append(name);
        if (exec.getKind().equals(ElementKind.CONSTRUCTOR)) {
            b.append(" = new ").append(typeName).append("(");
        } else {
            b.append(" = ").append(servlet.imports(factoryType)).append('.').append(exec.getSimpleName()).append("(");
        }
        appendParam(exec.getParameters(), servlet, args, b);
        b.append(");");
        return b;
    }

    protected final void appendParam(List<? extends VariableElement> arguments,
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

    protected void appendArgument(SourceServlet servlet, SourceArguments args, VariableElement param, StringBuilder b) {
        if (doRequest(param, b)
                || doAttribute(servlet, args, param, b)
                || doHeader(servlet, args, param, b)
                || doCookie(args, param, b)
                || doParam(args, param, b)
                || doBody(args, param, b)
                || doPart(servlet, args, param, b)) {
            return;
        }
        switch (param.asType().getKind()) {
            case BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE -> b.append("0");
            case BOOLEAN -> b.append("false");
            case DECLARED -> {
                TypeMirror rt = exec.getReturnType();
                if (rt.getKind() == TypeKind.TYPEVAR) {
                    String typeVar = "java.lang.Class<" + rt + ">";
                    if (param.asType().toString().equals(typeVar)) {
                        b.append(servlet.imports(returnType)).append(".class");
                    }
                } else {
                    b.append("null");
                }
            }
            default -> b.append("null");
        }
    }


    protected boolean doBody(SourceArguments args, VariableElement p, StringBuilder b) {
        if (args.containsBody()) {
            args.addFallback("null");
            env.error("cannot read body twice");
            return true;
        }
        TypeMirror t = p.asType();
        String type = t.toString();
        switch (type) {
            case T_ServletInputStream -> b.append("_req.getBody()");
            case T_Parts -> b.append("_req.getParts()");
            case T_Files -> b.append("_req.getFiles()");
            default -> {
                Body body = p.getAnnotation(Body.class);
                if (body == null) {
                    return false;
                }
                switch (type) {
                    case T_InputStream -> b.append("_req.getBody()");
                    case T_ByteArray -> b.append("_req.getBodyAsByte()");
                    case T_String ->
                            b.append("_req.getBodyAsText(\"").append(env.charset(charset, body.charset())).append("\")");
                    default -> b.append("null");
                }
            }
        }
        return true;
    }

    protected boolean doPart(SourceServlet servlet, SourceArguments args, VariableElement p, StringBuilder b) {
        TypeMirror t = p.asType();
        String type = t.toString();
        Part part = p.getAnnotation(Part.class);
        String alias = p.getSimpleName().toString();
        if (part == null) {
            switch (type) {
                case T_Part -> b.append("_req.getPart(\"").append(alias).append("\")");
                case T_File -> b.append("_req.getFile(\"").append(alias).append("\")");
                default -> part = ProcessEnvironment.getPart(p);
            }
        }
        if (part == null) return false;
        if (TextUtils.isNotEmpty(part.value())) alias = part.value();
        if (args.containsPart(alias)) {
            args.addFallback("null");
            env.error("cannot read part \"" + alias + "\" twice");
            return true;
        }
        switch (type) {
            case T_InputStream -> b.append(servlet.imports("yeamy.utils.IfNotNull"))
                    .append(".invoke(_req.getFile(\"").append(alias).append("\"),a->a.get()");
            case T_ByteArray -> b.append(servlet.imports("yeamy.utils.IfNotNull")).append(".invoke(_req.getFile(\"")
                    .append(alias).append("\"),a->a.getAsByte()");
            case T_String -> b.append(servlet.imports("yeamy.utils.IfNotNull")).append(".invoke(_req.getFile(\"")
                    .append(alias).append("\"),a->a.getAsText(\"").append(env.charset(charset, part.charset())).append("\")");
            default -> {
                return false;
            }
        }
        return true;
    }

    protected boolean doRequest(VariableElement p, StringBuilder b) {
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

    protected boolean doAttribute(SourceServlet servlet, SourceArguments args, VariableElement p, StringBuilder b) {
        Attribute attribute = p.getAnnotation(Attribute.class);
        if (attribute == null) {
            return false;
        }
        String name = attribute.value();
        if ("".equals(name)) {
            name = p.getSimpleName().toString();
        }
        TypeMirror tm = p.asType();
        String type = tm.toString();
        String exist = args.getAttributeAlias(type, name);
        if (exist != null) {
            b.append(exist);
            return true;
        }
        if (tm.getKind().isPrimitive()) {
            args.addFallback("null/* not support primitive type */");
            env.warning("not support attribute type " + type + " without annotation Creator");
        } else {
            b.append("(_req.getAttribute(\"").append(name).append("\") instanceof ").append(servlet.imports(type))
                    .append("_a) ? _a : null;");
        }
        return true;
    }

    protected boolean doHeader(SourceServlet servlet, SourceArguments args, VariableElement p, StringBuilder b) {
        Header header = p.getAnnotation(Header.class);
        if (header == null) {
            return false;
        }
        String name = header.value();
        if ("".equals(name)) {
            name = p.getSimpleName().toString();
        }
        TypeMirror tm = p.asType();
        String type = tm.toString();
        String exist = args.getHeaderAlias(type, name);
        if (exist != null) {
            b.append(exist);
            return true;
        }
        TypeKind kind = tm.getKind();
        if (kind.equals(TypeKind.INT)) {
            b.append("_req.getIntHeader(\"").append(name).append("\")");
            return true;
        } else if (kind.equals(TypeKind.LONG)) {
            b.append("_req.getDateHeader(\"").append(name).append("\")");
            return true;
        } else if (T_String.equals(type)) {
            b.append("_req.getHeader(\"").append(name).append("\")");
        } else if (T_Date.equals(type)) {
            b.append("_req.getDateHeader(new ").append(servlet.imports(T_Date))
                    .append("(\"").append(name).append("\"))");
        } else {
            b.append("null/* not support type */");
            env.error("Not support header type " + type);
        }
        return true;
    }

    protected boolean doCookie(SourceArguments args, VariableElement p, StringBuilder b) {
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

    protected boolean doParam(SourceArguments args, VariableElement p, StringBuilder b) {
        Param param = p.getAnnotation(Param.class);
        if (param == null) {
            return false;
        }
        TypeMirror t = p.asType();
        String type = t.toString();
        String name = p.getSimpleName().toString();
        String alias = args.getParamAlias(type, name);
        if (alias != null) {
            b.append(alias);
            return true;
        }
        switch (t.getKind()) {
            case INT -> b.append("_req.getIntParam(\"").append(name).append("\", 0)");
            case LONG -> b.append("_req.getLongParam(\"").append(name).append("\")");
            case FLOAT -> b.append("_req.getFloatParam(\"").append(name).append("\", 0)");
            case DOUBLE -> b.append("_req.getDoubleParam(\"").append(name).append("\", 0)");
            case BOOLEAN -> b.append("_req.getBoolParam(\"").append(name).append("\",false)");
            case DECLARED -> {
                switch (type) {
                    case T_String -> b.append("_req.getParameter(\"");
                    case T_Decimal -> b.append("_req.getDecimalParam(\"");
                    case T_Integer -> b.append("_req.getIntegerParam(\"");
                    case T_Long -> b.append("_req.getLongParam(\"");
                    case T_Float -> b.append("_req.getFloatParam(\"");
                    case T_Double -> b.append("_req.getDoubleParam(\"");
                    case T_Boolean -> b.append("_req.getBooleanParam(\"");
                    default -> {
                        return false;
                    }
                }
                b.append(name).append("\")");
            }
            case ARRAY -> {
                switch (type) {
                    case T_IntegerArray -> b.append("_req.getIntegerParams(\"");
                    case T_LongArray -> b.append("_req.getLongParams(\"");
                    case T_FloatArray -> b.append("_req.getFloatParams(\"");
                    case T_DoubleArray -> b.append("_req.getDoubleParams(\"");
                    case T_BooleanArray -> b.append("_req.getBooleanParams(\"");
                    case T_DecimalArray -> b.append("_req.getDecimalParams(\"");
                    case T_StringArray -> b.append("_req.getParameters(\"");
                    default -> {
                        return false;
                    }
                }
                b.append(name).append("\")");
            }
        }
        return true;
    }

    public abstract void addToArgs(SourceArguments args, SourceServlet servlet, VariableElement p, String name);
}
