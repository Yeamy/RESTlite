package yeamy.restlite.annotation;

import java.io.IOException;

class SourceWebFilter extends SourceClass {

    private final String className;

    SourceWebFilter(ProcessEnvironment env) throws IOException {
        super(env);
        this.pkg = env.getPackage();
        this.className = env.getFileName(pkg, "RESTliteWebFilter");
        imports("jakarta.servlet.annotation.WebFilter");
        imports("yeamy.restlite.DispatchFilter");
    }

    @Override
    public void create() throws IOException {
        StringBuilder sb = new StringBuilder("package ").append(pkg).append(";");
        sb.append("import static jakarta.servlet.DispatcherType.*;");
        for (String clz : imports.values()) {
            sb.append("import ").append(clz).append(";");
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
