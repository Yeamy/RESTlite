package yeamy.restlite.annotation;

import java.util.ArrayList;

import static yeamy.restlite.annotation.SupportType.T_HttpRequest;

class SourceServletHttpMethod {
	protected final ProcessEnvironment env;
	protected final SourceServlet servlet;
	private final ArrayList<SourceHttpMethodComponent> methods = new ArrayList<>();
	protected final String httpMethod;

	public SourceServletHttpMethod(ProcessEnvironment env, SourceServlet servlet, String httpMethod) {
		this.env = env;
		this.servlet = servlet;
		this.httpMethod = httpMethod;
	}

	public final void addMethod(SourceHttpMethodComponent method) {
		methods.add(method);
	}

	protected void create(ArrayList<SourceHttpMethodComponent> methods) throws ClassNotFoundException {
		servlet.imports(T_HttpRequest);
		servlet.imports("jakarta.servlet.http.HttpServletResponse");
		servlet.imports("jakarta.servlet.ServletException");
		servlet.imports("java.io.IOException");
		servlet.append("@Override public void do").append(httpMethod.charAt(0))
				.append(httpMethod.toLowerCase(), 1, httpMethod.length())
				.append("(RESTfulRequest _req, HttpServletResponse _resp) throws ServletException, IOException {");
		servlet.append("try{");
		for (SourceHttpMethodComponent method : methods) {
			method.create(httpMethod);
			servlet.append(" else ");
		}
		if (methods.size() > 1 && !hasNoArgs()) {
			servlet.imports("yeamy.restlite.addition.NoMatchMethodException");
			servlet.append("{ onError(_req, _resp, new NoMatchMethodException(_req));}");
		} else {
			servlet.deleteLast(6);
		}
		servlet.append("}catch(Exception ex){onError(_req,_resp,ex);}}");
	}

	public void create() throws ClassNotFoundException {
		methods.sort((m1, m2) -> {
			String k1 = m1.orderKey();
			String k2 = m2.orderKey();
			if (k1.length() < k2.length()) {
				return 1;
			} else if (k1.length() > k2.length()) {
				return -1;
			} else {
				for (int i = 0, l = k2.length(); i < l; i++) {
					char c1 = k1.charAt(i);
					char c2 = k2.charAt(i);
					if (c1 < c2) {
						return 1;
					} else if (c1 > c2) {
						return -1;
					}
				}
				return 0;
			}
		});
		create(methods);
	}

	public boolean hasNoArgs() {
		for (SourceHttpMethodComponent method : methods) {
			if (method.orderKey().length() == 0) {
				return true;
			}
		}
		return false;
	}
}
