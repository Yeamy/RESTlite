package example;

import jakarta.servlet.annotation.WebFilter;
import yeamy.restlite.DispatchFilter;
import yeamy.restlite.RESTliteFilter;

@WebFilter("/*")
public class TestFilter extends DispatchFilter {

    @Override
    protected RESTliteFilter[] createFilters() {
        return new RESTliteFilter[]{new MyInterceptor()};
    }
}

