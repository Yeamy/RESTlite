package yeamy.restlite.annotation;

public enum SupportPatch {

	/**
	 * by default servlet not support PATCH
	 */
	undefined,
	/**
	 * support Tomcat via <b>reflection</b>
	 */
	tomcat;

}
