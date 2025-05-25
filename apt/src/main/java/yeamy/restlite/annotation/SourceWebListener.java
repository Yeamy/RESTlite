package yeamy.restlite.annotation;

import java.io.IOException;

import static yeamy.restlite.annotation.SupportType.T_RESTfulRequest;

class SourceWebListener extends SourceClass {
    private final ProcessEnvironment env;
    private final String className, parentName;

    SourceWebListener(ProcessEnvironment env) {
        super(env.getPackage());
        this.env = env;
        this.className = env.getFileName(pkg, "RESTliteWebListener");
        parentName = imports("yeamy.restlite.RESTfulListener");
        imports("jakarta.servlet.annotation.WebListener");
        imports(T_RESTfulRequest);
    }

    @Override
    public void create() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append('@').append(imports("jakarta.annotation.Priority")).append("(1)");
        sb.append("@WebListener(\"*\") public class ").append(className).append(" extends ")
                .append(parentName).append(" {");
        sb.append("@Override public String createServerName(RESTfulRequest r) {switch (super.createServerName(r)) {");
        env.implMethodNames().forEach((key, map) -> {// key = resource + ':' + httpMethod
            sb.append("case \"").append(key).append("\":");
            map.forEach((name, ifHas) -> {// name = resource + ':' + httpMethod + ':' + params
                if (!ifHas.isEmpty()) {
                    sb.append(ifHas).append("){return \"").append(name).append("\";} else ");
                } else {
                    sb.append("{return \"").append(name).append("\";}");
                }
            });
            if (sb.charAt(sb.length() - 1) != '}') {
                int l = sb.length();
                sb.delete(l - 6, l).append("break;");
            }
        });
        sb.append("}return super.createServerName(r);}}");
        createSourceFile(env.processingEnv, pkg + '.' + className, sb);
    }

}
