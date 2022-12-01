package yeamy.restlite.annotation;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

class SourceWebFilter extends SourceClass {

    private class Filter {
        private final int index;
        private final CharSequence name;

        private Filter(TypeElement element) {
            Interceptor i = element.getAnnotation(Interceptor.class);
            index = i.index();
            name = imports(element);
        }
    }

    private final String className;
    private final ArrayList<Filter> filters = new ArrayList<>();

    SourceWebFilter(ProcessEnvironment env, RoundEnvironment roundEnv) throws IOException {
        super(env);
        this.pkg = env.getPackage();
        this.className = env.getFileName(pkg, "RESTliteWebFilter");
        imports("jakarta.servlet.annotation.WebFilter");
        imports("yeamy.restlite.DispatchFilter");
        imports("yeamy.restlite.RESTliteFilter");
        for (Element element : roundEnv.getElementsAnnotatedWith(Interceptor.class)) {
            Filter filter = new Filter((TypeElement) element);
            filters.add(filter);
        }
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
        if (filters.size() > 0) {
            filters.sort(Comparator.comparingInt(o -> o.index));
            sb.append(" {@Override protected RESTliteFilter[] createFilters() {return new RESTliteFilter[]{");
            for (Filter filter : filters) {
                sb.append("new ").append(filter.name).append("(),");
            }
            sb.append("};}}");
        } else {
            sb.append(" {}");
        }
        try {
            env.createSourceFile(pkg, className, sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
