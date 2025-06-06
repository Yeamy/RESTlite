package yeamy.restlite.addition;

import java.io.File;

/**
 * learn more about <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/ETag">ETag</a>
 */
public class ETag {

	/**
	 * Strong validation
	 * @param eTagReq eTag from http request
	 * @param eTagData eTag from local data
	 * @return strong validation equals
	 */
	public static boolean compareStrong(String eTagReq, String eTagData) {
		return eTagReq != null && eTagReq.equals(eTagData);
	}

	/**
	 * Weak validation
	 * @param eTagReq eTag from http request
	 * @param eTagData eTag from local data
	 * @return part validation equals
	 */
	public static boolean compareWeak(String eTagReq, String eTagData) {
		return eTagReq != null && comparePart(eTagReq).equals(comparePart(eTagData));
	}

	private static String comparePart(String eTag) {
		if (eTag != null && eTag.startsWith("W/")) {
			return eTag.substring(2);
		}
		return eTag;
	}

	/**
	 * @param file target file
	 * @return generate file ETag
	 */
	public static String fileETag(File file) {
		return "W/\"" + file.length() + "-" + file.lastModified() + '"';
	}

}
