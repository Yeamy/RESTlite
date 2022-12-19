package yeamy.restlite.annotation;

import java.util.ArrayList;

import static yeamy.restlite.annotation.SupportType.T_HttpRequest;

class SourceMethodHttpMethod {
	protected final ProcessEnvironment env;
	protected final SourceServlet servlet;
	private final ArrayList<SourceHttpMethodComponent> components = new ArrayList<>();
	protected final String httpMethod;

	public SourceMethodHttpMethod(ProcessEnvironment env, SourceServlet servlet, String httpMethod) {
		this.env = env;
		this.servlet = servlet;
		this.httpMethod = httpMethod;
	}

	public final void addMethod(SourceHttpMethodComponent method) {
		components.add(method);
	}

	protected void create(ArrayList<SourceHttpMethodComponent> methods) {
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

	public void create() {
		components.sort((m1, m2) -> {
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
		create(components);
	}

	public boolean hasNoArgs() {
		for (SourceHttpMethodComponent component : components) {
			if (component.orderKey().length() == 0) {
				return true;
			}
		}
		return false;
	}
}
