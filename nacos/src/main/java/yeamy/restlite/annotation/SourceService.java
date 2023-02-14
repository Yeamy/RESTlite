package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

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
        imports("com.alibaba.nacos.api.PropertyKeyConst");
        imports("com.alibaba.nacos.api.NacosFactory");
        imports("yeamy.restlite.annotation.InjectProvider");
        typeUtils = env.getTypeUtils();
    }

    @Override
    public void create() throws IOException {
        String clzName = type.getSimpleName() + "Impl";
        Object ifName = type.getSimpleName();
        StringBuilder sb = new StringBuilder("public class ").append(clzName).append(" implements ").append(ifName).append('{')
                .append("@InjectProvider(provideFor=").append(ifName).append(".class)")
                .append("public ").append(clzName).append("{Properties properties = new Properties();");
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
//              sb.append("NamingService namingService = NacosFactory.createNamingService(properties);namingService.registerInstance(\"")
                sb.append("NamingService namingService = new NacosNamingService(properties);namingService.registerInstance(\"")
                        .append(nameServer.serviceName()).append("\", \"").append(nameServer.serviceIP()).append("\",")
                        .append(nameServer.serviceIP()).append(",\"").append(convStr(nameServer.clusterName()))
                        .append("\");this.namingService = namingService;");
            }
        }
        // --------------- name server end | config server start --------------
        StringBuilder ms = new StringBuilder();
        SourceFields fieldNames = new SourceFields();
        // add pull
        ArrayList<ExecutableElement> methods = getMethods();
        Iterator<ExecutableElement> iterator = methods.iterator();
        while (iterator.hasNext()) {
            ExecutableElement method = iterator.next();
            NacosPullValue pull = method.getAnnotation(NacosPullValue.class);
            if (pull != null) {
                addConfigPull(pull, method, ms, fieldNames, sb);
                iterator.remove();
            }
        }
        sb.append('}');
        boolean containsConfigService = ms.length() > 0;
        if (containsConfigService) {
            imports("com.alibaba.nacos.api.config.ConfigService");
            imports("com.alibaba.nacos.client.config.NacosConfigService");
//            sb.append("ConfigService configService = NacosFactory.createConfigService(properties);this.configService = configService;");
            sb.append("ConfigService configService = new NacosConfigService(properties);this.configService = configService;");
        }
        sb.append('}');
        // --------------- config method end | add methods --------------
        sb.append(ms);
        Elements utils = env.getElementUtils();
        TypeMirror T_NamingService = utils.getTypeElement("com.alibaba.nacos.api.naming.NamingService").asType();
        TypeMirror T_ConfigService = utils.getTypeElement("com.alibaba.nacos.api.config.ConfigService").asType();
        for (ExecutableElement method : methods) {
            NacosPushValue push = method.getAnnotation(NacosPushValue.class);
            if (push != null) {
                SourceFields.NacosField field = fieldNames.get(push.dataId(), push.group());
                if (field == null) {
                    addConfigPush(push, method, fieldNames, sb);
                } else {
                    addConfigPush(push, method, field.type, field.name, sb);
                }
            } else if (typeUtils.isSubtype(method.getReturnType(), T_NamingService)) {
                sb.append("public NamingService ").append(method.getSimpleName()).append('(');
                List<? extends VariableElement> params = method.getParameters();
                if (params.size() > 0) {
                    for (VariableElement p : method.getParameters()) {
                        sb.append(imports(p.asType())).append(' ').append(p.getSimpleName()).append(',');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append(containsNamingService
                        ? "){return this.namingService;}"
                        : "){return null;}");
            } else if (typeUtils.isSubtype(method.getReturnType(), T_ConfigService)) {
                sb.append("public ConfigService ").append(method.getSimpleName()).append('(');
                List<? extends VariableElement> params = method.getParameters();
                if (params.size() > 0) {
                    for (VariableElement p : method.getParameters()) {
                        sb.append(imports(p.asType())).append(' ').append(p.getSimpleName()).append(',');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append(containsConfigService
                        ? "){return this.configService;}"
                        : "){return null;}");
            } else {
                sb.append("public ").append(imports(method.getReturnType())).append(' ').append(method.getSimpleName()).append('(');
                List<? extends VariableElement> params = method.getParameters();
                if (params.size() > 0) {
                    for (VariableElement p : params) {
                        sb.append(imports(p.asType())).append(' ').append(p.getSimpleName()).append(',');
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("){return null;}");
            }
        }
        sb.append("}catch(NacosException e){e.printStackTrace();}}");
        createSourceFile(env, pkg + '.' + clzName, sb);
    }

    private void addConfigPush(NacosPushValue push, ExecutableElement method, SourceFields fields, StringBuilder sb) {
        String fieldName = "arg" + fields.index();
        String fieldType = addConfigPush(push, method, null, fieldName, sb);
        if (fieldType != null) {
            fields.put(push, fieldType, fieldName);
        }
    }

    private String addConfigPush(NacosPushValue push, ExecutableElement method, String fieldType, String fieldName, StringBuilder sb) {
        List<? extends VariableElement> params = method.getParameters();
        if (method.getReturnType().getKind().equals(TypeKind.VOID)) {
            showError("@NacosPushValue method's return type must be void: " + type.getQualifiedName() + "." + method.getSimpleName());
        } else if (params.size() == 1) {
            showError("@NacosPushValue method's must have only one param: " + type.getQualifiedName() + "." + method.getSimpleName());
        } else {
            Object methodName = method.getSimpleName();
            VariableElement param = params.get(0);
            Object paramName = param.getSimpleName();
            String paramType = imports(param.asType());
            if (fieldType == null) {
                fieldType = paramType;
                sb.append("private ").append(paramType).append(' ').append(fieldName).append(';');
            }
            if (fieldType.equals(paramType)) {
                sb.append("public void ").append(methodName).append('(').append(paramType).append(' ').append(paramName)
                        .append("){this.").append(fieldName).append('=').append(paramName).append(";configService.publishConfig(\"")
                        .append(convStr(push.dataId())).append("\",\"").append(convStr(push.group())).append("\",")
                        .append(paramName).append(",\"").append(push.type()).append("\");}");
                return paramType;
            } else {
                showError("param of @NacosPushValue method not the same with @NacosPullValue method: " + type.getQualifiedName() + "." + method.getSimpleName());
                sb.append("public void ").append(methodName).append('(').append(paramType).append(' ').append(paramName).append("){}");
            }
        }
        return null;
    }

    private void addConfigPull(NacosPullValue pull, ExecutableElement method, StringBuilder ms, SourceFields fieldNames, StringBuilder sb) {
        String fieldName = "arg" + fieldNames.index();
        List<? extends VariableElement> parameters = method.getParameters();
        if (parameters.size() > 0) {
            showError("@NacosPullValue must have no parameter: " + type.getQualifiedName() + "." + method.getSimpleName());
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
        String expression = "";
        switch (kind) {
            case INT -> {
                returnType = "int";
                expression = "Integer.parseInt(configInfo)";
            }
            case LONG -> {
                returnType = "long";
                expression = "Long.parseLong(configInfo)";
            }
            case BOOLEAN -> {
                returnType = "boolean";
                expression = "Boolean.parseBoolean(configInfo)";
            }
            case SHORT -> {
                returnType = "short";
                expression = "Short.parseShort(configInfo)";
            }
            case FLOAT -> {
                returnType = "float";
                expression = "Float.parseFloat(configInfo)";
            }
            case DOUBLE -> {
                returnType = "double";
                expression = "Double.parseDouble(configInfo)";
            }
            case DECLARED -> {
                returnType = rtm.toString();
                if (returnType.equals("java.math.BigDecimal")) {
                    returnType = imports("java.math.BigDecimal");
                    expression = "new BigDecimal(configInfo)";
                } else if (returnType.equals("java.lang.String")) {
                    returnType = "String";
                    expression = "configInfo";
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
        if (pull.autoRefreshed()) {
            sb.append(fieldName).append(" = configService.getConfigAndSignListener(").append(convStr(pull.dataId()))
                    .append(',').append(convStr(pull.group())).append(',').append(pull.timeoutMs())//
                    .append("L,new Listener() {@Override public Executor getExecutor(){return ")//
                    .append(getExecutor()).append(";}@Override public void receiveConfigInfo(String configInfo){this.")//
                    .append(fieldName).append('=').append(expression).append(";}});");
        } else {
            sb.append(fieldName).append(" = configService.getConfig(").append(convStr(pull.dataId())).append(',')
                    .append(convStr(pull.group())).append(',').append(pull.timeoutMs()).append("L);");
        }
        fieldNames.put(pull, rtm.toString(), fieldName);
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
            sb.append("properties.put(PropertyKeyConst.").append(key).append(',').append(value).append(");");
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
        return null;
    }

    private void showError(String msg) {
        env.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

}
