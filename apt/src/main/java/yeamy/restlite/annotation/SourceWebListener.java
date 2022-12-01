package yeamy.restlite.annotation;

import java.io.IOException;

class SourceWebListener extends SourceClass {

    private final String className, parentName;

    SourceWebListener(ProcessEnvironment env) throws IOException {
        super(env);
        this.pkg = env.getPackage();
        this.className = env.getFileName(pkg, "RESTliteWebListener");
        String parent;
        switch (env.supportPatch()) {
            case tomcat:
                parent = "yeamy.restlite.annotation.TomcatListener";
                parentName = "TomcatListener";
                break;
            case undefined:
            default:
                parent = "yeamy.restlite.RESTfulListener";
                parentName = "RESTfulListener";
        }
        imports.put(parentName, parent);
        imports("jakarta.servlet.annotation.WebListener");
    }

    @Override
    public void create() throws IOException {
        StringBuilder sb = new StringBuilder("package ").append(pkg).append(";");
        for (String clz : imports.values()) {
            sb.append("import ").append(clz).append(";");
        }
        sb.append("@WebListener(\"*\") public class ").append(className).append(" extends ")
                .append(parentName).append(" {}");
        try {
            env.createSourceFile(pkg, className, sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
