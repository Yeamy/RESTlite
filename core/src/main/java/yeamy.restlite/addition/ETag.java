package yeamy.restlite.addition;

import java.io.File;

public class ETag {

	public static boolean compareStrong(String etagReq, String etagData) {
		return etagReq != null && etagReq.equals(etagData);
	}

	public static boolean compareWeak(String etagReq, String etagData) {
		return etagReq != null && comparePart(etagReq).equals(comparePart(etagData));
	}

	private static String comparePart(String etag) {
		if (etag != null && etag.startsWith("W/")) {
			return etag.substring(2);
		}
		return etag;
	}

	public static String fileETag(File file) {
		return "W/\"" + file.length() + "-" + file.lastModified() + '"';
	}

}
