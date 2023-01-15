package yeamy.restlite.annotation;

import java.io.IOException;
import java.util.Map;

class SourceWebListener extends SourceClass {
    private final ProcessEnvironment env;
    private final String className, parentName;

    SourceWebListener(ProcessEnvironment env) throws IOException {
        this.env = env;
        this.pkg = env.getPackage();
        this.className = env.getFileName(pkg, "RESTliteWebListener");
        String parent;
        switch (env.supportPatch()) {
            case tomcat:
                parent = "yeamy.restlite.addition.TomcatListener";
                parentName = "TomcatListener";
                break;
            case undefined:
            default:
                parent = "yeamy.restlite.RESTfulListener";
                parentName = "RESTfulListener";
        }
        imports.put(parentName, parent);
        imports("jakarta.servlet.annotation.WebListener");
        imports("yeamy.restlite.RESTfulRequest");
    }

    @Override
    public void create() throws IOException {
        StringBuilder sb = new StringBuilder("package ").append(pkg).append(';');
        for (String clz : imports.values()) {
            sb.append("import ").append(clz).append(';');
        }
        sb.append("@WebListener(\"*\") public class ").append(className).append(" extends ")
                .append(parentName).append(" {");
        imports("yeamy.restlite.RESTfulRequest");
        sb.append("@Override public String createServerName(RESTfulRequest r) {switch (super.createServerName(r)) {");
        for (Map.Entry<String, Map<String, String>> e1 : env.serverNames()) {
            sb.append("case \"").append(e1.getKey()).append("\":");// resource + ':' + httpMethod
            boolean delLast = false;
            for (Map.Entry<String, String> e2 : e1.getValue().entrySet()) {
                String ifHas = e2.getValue();
                String name = e2.getKey();// resource + ':' + httpMethod + ':' + params
                if (ifHas.length() > 0) {
                    sb.append(ifHas).append("){return \"").append(name).append("\";} else ");
                    delLast = true;
                } else {
                    sb.append("{return \"").append(name).append("\";}");
                    delLast = false;
                }
            }
            if (delLast) {
                int l = sb.length();
                sb.delete(l - 6, l).append("break;");
            }
        }
        sb.append("}return super.createServerName(r);}@Override public boolean isEmbed(){return ")
                .append(env.getTypeElement("yeamy.restlite.annotation.TomcatConfig") != null ? "true" : "false")
                .append(";}}");
        try {
            env.createSourceFile(pkg, className, sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
