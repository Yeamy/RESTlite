package yeamy.restlite.annotation;

import java.util.ArrayList;

class SourceServletOnError extends SourceServletMethod<SourceDispatchOnError> {

    SourceServletOnError(ProcessEnvironment env, SourceServlet servlet) {
        super(env, servlet);
    }

    @Override
    protected void create(ArrayList<SourceDispatchOnError> methods) throws ClassNotFoundException {
        if (methods.size() == 0) {
            servlet.imports("jakarta.servlet.http.HttpServletResponse");
            servlet.imports("java.io.IOException");
            servlet.imports("yeamy.restlite.addition.ExceptionResponse");
            servlet.append("public void onError(HttpServletResponse _resp, Exception e) throws IOException")//
                    .append("{new ExceptionResponse(e).write(_resp);}");
        } else {
            servlet.imports("java.lang.Exception");
            servlet.append("public void onError(HttpServletResponse _resp, Exception e) {");
            for (SourceDispatchOnError method : methods) {
                method.create();
            }
            servlet.append('}');
        }
    }

}
