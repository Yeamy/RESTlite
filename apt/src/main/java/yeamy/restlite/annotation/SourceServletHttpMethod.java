package yeamy.restlite.annotation;

import java.util.ArrayList;

import static yeamy.restlite.annotation.SupportType.T_HttpRequest;

class SourceServletHttpMethod extends SourceServletMethod<SourceDispatchService> {
	protected final String httpMethod;

	public SourceServletHttpMethod(ProcessEnvironment env, SourceServlet servlet, String httpMethod) {
		super(env, servlet);
		this.httpMethod = httpMethod;
	}

	protected void create(ArrayList<SourceDispatchService> methods) throws ClassNotFoundException {
		servlet.imports(T_HttpRequest);
		servlet.imports("jakarta.servlet.http.HttpServletResponse");
		servlet.imports("jakarta.servlet.ServletException");
		servlet.imports("java.io.IOException");
		servlet.append("@Override public void do").append(httpMethod.charAt(0))
				.append(httpMethod.toLowerCase(), 1, httpMethod.length())
				.append("(RESTfulRequest _req, HttpServletResponse _resp) throws ServletException, IOException {");
		for (SourceDispatchService method : methods) {
			method.create(httpMethod);
			servlet.append(" else ");
		}
		if (methods.size() > 1 && !hasNoArgs()) {
			servlet.imports("yeamy.restlite.addition.NoMatchMethodException");
			servlet.append("{ onError(_resp, new NoMatchMethodException(_req));}");
		} else {
			servlet.deleteLast(6);
		}
		servlet.append('}');
	}

}
