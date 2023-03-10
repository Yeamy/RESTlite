package yeamy.restlite.annotation;

import java.io.IOException;

class SourceWebFilter extends SourceClass {
    private final ProcessEnvironment env;
    private final String className;
    private final boolean embed;

    SourceWebFilter(ProcessEnvironment env, boolean embed) {
        super(env.getPackage());
        this.env = env;
        this.embed = embed;
        this.className = env.getFileName(pkg, "RESTliteWebFilter");
        imports("jakarta.servlet.annotation.WebFilter");
        imports("yeamy.restlite.DispatchFilter");
    }

    @Override
    public void create() throws IOException {
        StringBuilder sb = new StringBuilder("import static jakarta.servlet.DispatcherType.*;");
        if (embed) {
            sb.append('@').append(imports("yeamy.restlite.annotation.Position")).append("(1)");
        }
        sb.append("@WebFilter(value=\"*\",dispatcherTypes={FORWARD,INCLUDE,REQUEST,ASYNC,ERROR}) public class ")
                .append(className).append(" extends ").append("DispatchFilter");
        sb.append(" {}");
        try {
            createSourceFile(env.processingEnv, pkg + '.' + className, sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
