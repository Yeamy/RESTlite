package yeamy.restlite.annotation;

import java.io.IOException;

class SourceWebFilter extends SourceClass {
    private final ProcessEnvironment env;
    private final String className;
    private final boolean embed;

    SourceWebFilter(ProcessEnvironment env, boolean embed) throws IOException {
        this.env = env;
        this.embed = embed;
        this.pkg = env.getPackage();
        this.className = env.getFileName(pkg, "RESTliteWebFilter");
        imports("jakarta.servlet.annotation.WebFilter");
        imports("yeamy.restlite.DispatchFilter");
    }

    @Override
    public void create() throws IOException {
        StringBuilder sb = new StringBuilder("package ").append(pkg).append(';');
        sb.append("import static jakarta.servlet.DispatcherType.*;");
        for (String clz : imports.values()) {
            sb.append("import ").append(clz).append(';');
        }
        if (embed) {
            sb.append('@').append(imports("yeamy.restlite.annotation.Position")).append("(1)");
        }
        sb.append("@WebFilter(value=\"*\",dispatcherTypes={FORWARD,INCLUDE,REQUEST,ASYNC,ERROR}) public class ")
                .append(className).append(" extends ").append("DispatchFilter");
        sb.append(" {}");
        try {
            env.createSourceFile(pkg, className, sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
