package yeamy.restlite.addition;

import java.io.IOException;
import java.util.ArrayList;

public class Range {
	private static final String CRLF = "\r\n";

	public final long first;
	public final long last;
	public final long total;

	public Range(long first, long end, long total) {
		this.first = first;
		this.last = end;
		this.total = total;
	}

	public static ArrayList<Range> parse(String range, long total) throws NumberFormatException {
		if (range != null && range.startsWith("bytes=")) {
			String[] rs = range.substring(6).split(",");
			ArrayList<Range> list = new ArrayList<>(rs.length);
			for (String r : rs) {
				r = r.trim();
				long first, last;
				if (r.charAt(0) == '-') {
					first = total + Long.parseLong(r);
					last = total - 1;
				} else {
					int end = r.indexOf("-");
					first = Long.parseLong(r.substring(0, end));
					last = Long.parseLong(r.substring(end + 1, r.length()));
				}
				list.add(new Range(first, last, total));
			}
			return list;
		}
		return null;
	}

	public long length() {
		return last - first + 1;
	}

	public String contentLength() {
		return String.valueOf(length());
	}

	/** response header */
	public String contentRange() {
		StringBuilder sb = new StringBuilder();
		contentRange(sb);
		return sb.toString();
	}

	private void contentRange(StringBuilder sb) {
		sb.append("bytes ").append(first).append("-").append(last).append("/").append(total);
	}

	/** one part one beginning */
	public String beginningOfMultiPart(String boundary, String contentType) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(CRLF).append("--").append(boundary).append(CRLF);
		sb.append(CRLF).append("Content-Type: ").append(contentType).append(CRLF);
		sb.append(CRLF).append("Content-Range: ");
		contentRange(sb);
		sb.append(CRLF);
		return sb.toString();
	}

	public static String endOfMultiPart(String boundary) {
		return new StringBuilder(CRLF).append("--").append(boundary).append("--").toString();
	}
}