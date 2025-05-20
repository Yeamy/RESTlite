package yeamy.restlite.annotation;

import yeamy.restlite.utils.TextUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static yeamy.restlite.annotation.SupportType.T_Decimal;
import static yeamy.restlite.annotation.SupportType.T_String;

class SourceService extends SourceClass {
    private final ProcessingEnvironment env;
    private final Types typeUtils;
    private final TypeElement type;
    private final Element executor;
    private final NacosRemoteServer server;

    public SourceService(ProcessingEnvironment env, TypeElement type, NacosRemoteServer server, Element executor) {
        super(((PackageElement) type.getEnclosingElement()).getQualifiedName().toString());
        this.env = env;
        this.type = type;
        this.server = server;
        this.executor = executor;
        imports("java.util.Properties");
        imports("yeamy.restlite.annotation.InjectProvider");
        typeUtils = env.getTypeUtils();
    }

    @Override
    public void create() throws IOException {
        String clzName = type.getSimpleName() + "Impl";
        Object ifName = type.getSimpleName();
        StringBuilder clz = new StringBuilder("public class ").append(clzName).append(" implements ").append(ifName).append('{');
        StringBuilder sb = new StringBuilder("@InjectProvider(provideFor=").append(ifName).append(".class)");
        sb.append("public ").append(clzName).append("(){Properties properties = new Properties();");
        addProperties(sb, "SERVER_ADDR", server.serverAddr());
        addProperties(sb, "NAMESPACE", server.namespace());
        addProperties(sb, "USERNAME", server.username());
        addProperties(sb, "PASSWORD", server.password());
        addProperties(sb, "ACCESS_KEY", server.accessKey());
        addProperties(sb, "SECRET_KEY", server.secretKey());
        addProperties(sb, "ENCODE", server.encode());
        addProperties(sb, "ENDPOINT", server.endpoint());
        addProperties(sb, "CONTEXT_PATH", server.contextPath());
        addProperties(sb, "CLUSTER_NAME", server.clusterName());
        addProperties(sb, "CONFIG_LONG_POLL_TIMEOUT", server.configLongPollTimeout());
        addProperties(sb, "CONFIG_RETRY_TIME", server.configRetryTime());
        addProperties(sb, "MAX_RETRY", server.maxRetry());
        addProperties(sb, "ENABLE_REMOTE_SYNC_CONFIG", server.enableRemoteSyncConfig());
        sb.append("try{");
        // name server start
        NacosDiscovery[] nameServers = server.enableDiscovery();
        boolean containsNamingService = nameServers.length > 0;
        if (containsNamingService) {
            imports("com.alibaba.nacos.api.naming.NamingService");
            imports("com.alibaba.nacos.client.naming.NacosNamingService");
            for (NacosDiscovery nameServer : nameServers) {
                clz.append("private NamingService namingService;");
                sb.append("NamingService namingService = new NacosNamingService(properties);namingService.registerInstance(\"")
                        .append(nameServer.serviceName()).append("\", \"").append(nameServer.serviceIP()).append("\",")
                        .append(nameServer.servicePort()).append(",\"").append(convStr(nameServer.clusterName()))
                        .append("\");this.namingService = namingService;");
            }
        }
        // --------------- name server end | config server start --------------
        StringBuilder ms = new StringBuilder();
        SourceFields fieldNames = new SourceFields();
        // add getter
        ArrayList<ExecutableElement> methods = getMethods();
        Iterator<ExecutableElement> iterator = methods.iterator();
        while (iterator.hasNext()) {
            ExecutableElement method = iterator.next();
            NacosGet getter = method.getAnnotation(NacosGet.class);
            if (getter != null) {
                addGetter(getter, method, clz, fieldNames, ms);
                iterator.remove();
            }
        }
        boolean containsConfigService = ms.length() > 0;
        if (containsConfigService) {
            clz.append("private ")
                    .append(imports("com.alibaba.nacos.api.config.ConfigService"))
                    .append(" configService;");
            clz.append("private interface L extends ")
                    .append(imports("com.alibaba.nacos.api.config.listener.Listener"))
                    .append("{@Override default ")
                    .append(imports("java.util.concurrent.Executor"))
                    .append(" getExecutor(){return ")
                    .append(getExecutor())
                    .append(";}}");
            clz.append("private String getConfigAndSignListener(String d, String g, long t, L l){try {return configService.getConfigAndSignListener(d, g, t, l);}catch(")
                    .append(imports("com.alibaba.nacos.api.exception.NacosException"))
                    .append(" e) {e.printStackTrace();return null;}}");
            sb.append("ConfigService configService = this.configService = new ")
                    .append(imports("com.alibaba.nacos.client.config.NacosConfigService"))
                    .append("(properties);")
                    .append(ms);
        }
        // --------------- config method end | add methods --------------
        Elements utils = env.getElementUtils();
        TypeMirror T_NamingService = utils.getTypeElement("com.alibaba.nacos.api.naming.NamingService").asType();
        TypeMirror T_ConfigService = utils.getTypeElement("com.alibaba.nacos.api.config.ConfigService").asType();
        for (ExecutableElement method : methods) {
            NacosSet setter = method.getAnnotation(NacosSet.class);
            if (setter != null) {
                SourceFields.NacosField field = fieldNames.get(setter.dataId(), setter.group());
                if (field == null) {
                    addSetter(setter, method, fieldNames, clz);
                } else {
                    addSetter(setter, method, field.type, field.name, clz);
                }
            } else if (typeUtils.isSubtype(method.getReturnType(), T_NamingService)) {
                clz.append("public NamingService ").append(method.getSimpleName()).append('(');
                List<? extends VariableElement> params = method.getParameters();
                if (params.size() > 0) {
                    for (VariableElement p : method.getParameters()) {
                        clz.append(imports(p.asType())).append(' ').append(p.getSimpleName()).append(',');
                    }
                    clz.deleteCharAt(clz.length() - 1);
                }
                clz.append(containsNamingService
                        ? "){return this.namingService;}"
                        : "){return null;}");
            } else if (typeUtils.isSubtype(method.getReturnType(), T_ConfigService)) {
                clz.append("public ConfigService ").append(method.getSimpleName()).append('(');
                List<? extends VariableElement> params = method.getParameters();
                if (params.size() > 0) {
                    for (VariableElement p : method.getParameters()) {
                        clz.append(imports(p.asType())).append(' ').append(p.getSimpleName()).append(',');
                    }
                    clz.deleteCharAt(clz.length() - 1);
                }
                clz.append(containsConfigService
                        ? "){return this.configService;}"
                        : "){return null;}");
            } else {
                clz.append("public ").append(imports(method.getReturnType())).append(' ').append(method.getSimpleName()).append('(');
                List<? extends VariableElement> params = method.getParameters();
                if (params.size() > 0) {
                    for (VariableElement p : params) {
                        clz.append(imports(p.asType())).append(' ').append(p.getSimpleName()).append(',');
                    }
                    clz.deleteCharAt(clz.length() - 1);
                }
                clz.append("){return null;}");
            }
        }
        sb.append("}catch(")
                .append(imports("com.alibaba.nacos.api.exception.NacosException"))
                .append(" e){e.printStackTrace();}}");
        clz.append(sb).append('}');
        createSourceFile(env, pkg + '.' + clzName, clz);
    }

    private void addSetter(NacosSet setter, ExecutableElement method, SourceFields fields, StringBuilder clz) {
        String fieldName = "arg" + fields.index();
        String fieldType = addSetter(setter, method, null, fieldName, clz);
        if (fieldType != null) {
            fields.put(setter, fieldType, fieldName);
        }
    }

    private String addSetter(NacosSet setter, ExecutableElement method, String fieldType, String fieldName, StringBuilder clz) {
        List<? extends VariableElement> params = method.getParameters();
        if (!method.getReturnType().getKind().equals(TypeKind.VOID)) {
            showError("@NacosSet method's return type must be void: " + type.getQualifiedName() + "." + method.getSimpleName());
        } else if (params.size() != 1) {
            showError("@NacosSet method's must have only one param: " + type.getQualifiedName() + "." + method.getSimpleName());
        } else {
            Object methodName = method.getSimpleName();
            VariableElement param = params.get(0);
            Object paramName = param.getSimpleName();
            String paramType = param.asType().toString();
            if (fieldType == null) {
                fieldType = imports(paramType);
                clz.append("private ").append(imports(paramType)).append(' ').append(fieldName).append(';');
            }
            clz.append("public void ").append(methodName).append('(').append(imports(paramType)).append(' ').append(paramName).append(')');
            List<? extends TypeMirror> ts = method.getThrownTypes();
            boolean noThrown = true, throwSev = false;
            if (ts.size() > 0) {
                clz.append(" throws ");
                for (int i = 0; i < ts.size(); i++) {
                    String throwClz = ts.get(i).toString();
                    if (i > 0) clz.append(',');
                    clz.append(imports(throwClz));
                    if (noThrown) {
                        if (TextUtils.in(throwClz, "java.lang.Exception", "java.lang.Throwable",
                                "com.alibaba.nacos.api.exception.NacosException")) {
                            noThrown = false;
                        } else if (!throwSev && throwClz.equals("jakarta.servlet.ServletException")) {
                            throwSev = true;
                        }
                    }
                }
            }
            if (fieldType.equals(paramType)) {
                clz.append('{');
                if (noThrown) clz.append("try{");
                clz.append("this.").append(fieldName).append('=').append(paramName).append(';');
                clz.append("configService.publishConfig(\"").append(convStr(setter.dataId())).append("\",\"")
                        .append(convStr(setter.group())).append("\",");
                if (fieldType.equals(T_String)) {
                    clz.append(paramName);
                } else {
                    clz.append("String.valueOf(").append(paramName).append(')');
                }
                clz.append(",\"").append(setter.type()).append("\");");
                if (noThrown) {
                    clz.append("}catch(").append(imports("com.alibaba.nacos.api.exception.NacosException"))
                            .append(" e)");
                    if (throwSev) {
                        clz.append("{throw new ").append(imports("jakarta.servlet.ServletException")).append("(e);}");
                    } else {
                        clz.append("{e.printStackTrace();}");
                    }
                }
                clz.append('}');
                return paramType;
            } else {
                showError("param of @NacosSet method not the same with @NacosGet method: " + type.getQualifiedName()
                        + "." + method.getSimpleName());
                clz.append("{}");
            }
        }
        return null;
    }

    private void addGetter(NacosGet getter, ExecutableElement method, StringBuilder ms, SourceFields fieldNames, StringBuilder sb) {
        String fieldName = "arg" + fieldNames.index();
        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() > 0) {
            showError("@NacosGet must be no parameter: " + type.getQualifiedName() + "." + method.getSimpleName());
            String rt = "null";
            TypeKind kind = method.getReturnType().getKind();
            if (kind.isPrimitive()) {
                rt = kind.equals(TypeKind.BOOLEAN) ? "false" : "0";
            }
            for (VariableElement p : parameters) {
                TypeMirror pm = p.asType();
                if (!pm.getKind().isPrimitive()) {
                    imports(pm);
                }
            }
            ms.append("public ").append(method)
                    .deleteCharAt(ms.length() - 1)
                    .append("{return ")
                    .append(rt)
                    .append(";}");
            return;
        }
        TypeMirror rtm = method.getReturnType();
        TypeKind kind = rtm.getKind();
        String returnType = "";
        String[] expression = new String[]{"", ""};
        switch (kind) {
            case BOOLEAN -> {
                returnType = "boolean";
                expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toBoolean(", ",false)"};
            }
            case SHORT -> {
                returnType = "short";
                expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toShort(", ",0)"};
            }
            case INT -> {
                returnType = "int";
                expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toInt(", ",0)"};
            }
            case LONG -> {
                returnType = "long";
                expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toLong(", ",0)"};
            }
            case FLOAT -> {
                returnType = "float";
                expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toFloat(", ",0)"};
            }
            case DOUBLE -> {
                returnType = "double";
                expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toDouble(", ",0)"};
            }
            case DECLARED -> {
                returnType = rtm.toString();
                switch (returnType) {
                    case T_String -> returnType = "String";
                    case T_Decimal -> {
                        returnType = imports("java.math.BigDecimal");
                        expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toBigDecimal(", ")"};
                    }
                    case "java.lang.Boolean" -> {
                        returnType = "Boolean";
                        expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toBoolean(", ")"};
                    }
                    case "java.lang.Short" -> {
                        returnType = "Short";
                        expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toShort(", ")"};
                    }
                    case "java.lang.Integer" -> {
                        returnType = "Integer";
                        expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toInteger(", ")"};
                    }
                    case "java.lang.Long" -> {
                        returnType = "Long";
                        expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toLong(", ")"};
                    }
                    case "java.lang.Float" -> {
                        returnType = "Float";
                        expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toFloat(", ")"};
                    }
                    case "java.lang.Double" -> {
                        returnType = "Double";
                        expression = new String[]{imports("yeamy.utils.ValueUtils") + ".toDouble(", ")"};
                    }
                }
            }
        }
        Object methodName = method.getSimpleName();
        if (returnType.equals("")) {
            showError("Not support " + returnType + " yet: " + type.getQualifiedName() + "." + methodName);
            ms.append("public ").append(returnType).append(' ').append(methodName).append("(){ return null;}");
            return;
        }
        ms.append("private ").append(returnType).append(' ').append(fieldName).append("; public ").append(returnType)
                .append(' ').append(methodName).append("(){ return ").append(fieldName).append(";}");
        if (getter.autoRefreshed()) {
            sb.append(fieldName).append('=').append(expression[0]).append("getConfigAndSignListener(\"")
                    .append(convStr(getter.dataId())).append("\",\"").append(convStr(getter.group())).append("\",")
                    .append(getter.timeoutMs()).append("L,v-> ").append(fieldName).append('=').append(expression[0])
                    .append('v').append(expression[1]).append(")")
                    .append(expression[1]).append(';');
        } else {
            sb.append(fieldName).append('=').append(expression[0]).append("configService.getConfig(\"")
                    .append(convStr(getter.dataId())).append("\",\"")
                    .append(convStr(getter.group())).append("\",")
                    .append(getter.timeoutMs()).append("L)").append(expression[1]).append(';');
        }
        fieldNames.put(getter, rtm.toString(), fieldName);
    }

    private ArrayList<ExecutableElement> getMethods() {
        ArrayList<ExecutableElement> list = new ArrayList<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind().equals(ElementKind.METHOD) && !element.getModifiers().contains(Modifier.STATIC)) {
                list.add((ExecutableElement) element);
            }
        }
        return list;
    }

    private void addProperties(StringBuilder sb, String key, String value) {
        if (TextUtils.isNotEmpty(value)) {
            sb.append("properties.put(").append(imports("com.alibaba.nacos.api.PropertyKeyConst"))
                    .append(".").append(key).append(',').append(value).append(");");
        }
    }

    private String executorTxt;

    private String getExecutor() {
        if (executorTxt == null) {
            executorTxt = createExecutor();
        }
        return executorTxt;
    }

    private String createExecutor() {
        if (executor == null) {
            showError("Cannot find @NacosExecutor for type:" + type.getQualifiedName());
            return "null";
        }
        TypeMirror typeMirror = executor.asType();
        Set<Modifier> modifiers = executor.getModifiers();
        if (!modifiers.contains(Modifier.STATIC) || !modifiers.contains(Modifier.PUBLIC)) {
            showError("@NacosExecutor must be a public static field/method: " + typeMirror);
            return "null";
        }
        TypeMirror T_Executor = env.getElementUtils().getTypeElement("java.util.concurrent.Executor").asType();
        if (executor instanceof VariableElement) {// field
            if (typeUtils.isSubtype(typeMirror, T_Executor)) {
                TypeElement typeElement = (TypeElement) executor.getEnclosingElement();
                return imports(typeElement) + "." + executor.getSimpleName();
            } else {
                showError("@NacosExecutor field must be instance of java.util.concurrent.Executor: " + typeMirror);
                return "null";
            }
        } else if (executor instanceof ExecutableElement element) {// method
            if (element.getParameters().size() > 0) {
                showError("@NacosExecutor method must have no parameter: " + typeMirror);
                return "null";
            }
            if (typeUtils.isSubtype(element.getReturnType(), T_Executor)) {
                TypeElement typeElement = (TypeElement) executor.getEnclosingElement();
                return imports(typeElement) + "." + executor.getSimpleName() + "()";
            } else {
                showError("@NacosExecutor method must return a instance of java.util.concurrent.Executor: " + typeMirror);
                return "null";
            }
        }
        return "null";
    }

    private void showError(String msg) {
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

}
