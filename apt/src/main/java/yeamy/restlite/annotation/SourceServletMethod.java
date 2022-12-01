package yeamy.restlite.annotation;

import java.util.ArrayList;

/**
 * @see SourceServletHttpMethod
 * @see SourceServletOnError
 * @author Yeamy
 *
 */
abstract class SourceServletMethod<T extends SourceClause> {
	protected final ProcessEnvironment env;
	protected final SourceServlet servlet;
	private final ArrayList<T> methods = new ArrayList<>();

	SourceServletMethod(ProcessEnvironment env, SourceServlet servlet) {
		this.env = env;
		this.servlet = servlet;
	}

	public final void addMethod(T method) {
		methods.add(method);
	}

	public final void create() throws ClassNotFoundException {
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

	protected abstract void create(ArrayList<T> methods) throws ClassNotFoundException;

	public boolean hasNoArgs() {
		for (SourceClause method : methods) {
			if (method.orderKey().length() == 0) {
				return true;
			}
		}
		return false;
	}

}
