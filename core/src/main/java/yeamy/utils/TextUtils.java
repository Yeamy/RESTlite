package yeamy.utils;

public class TextUtils {

	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static boolean isEmpty(CharSequence... charSequences) {
		for (CharSequence cs : charSequences) {
			if (cs != null && cs.length() > 0) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasEmpty(CharSequence... charSequences) {
		for (CharSequence cs : charSequences) {
			if (cs == null || cs.length() == 0) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNotEmpty(CharSequence cs) {
		return cs != null && cs.length() > 0;
	}

	public static boolean isNotEmpty(CharSequence... charSequences) {
		if (charSequences == null) {
			return false;
		}
		for (CharSequence cs : charSequences) {
			if (cs == null || cs.length() == 0) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(CharSequence c1, CharSequence c2) {
		return c1 != null && c1.equals(c2);
	}

	public static boolean notEquals(CharSequence c1, CharSequence c2) {
		if (c1 == null) {
			return c2 != null;
		}
		return !c1.equals(c2);
	}

	public static boolean in(CharSequence cs, CharSequence... in) {
		for (CharSequence li : in) {
			if (equals(cs, li)) {
				return true;
			}
		}
		return false;
	}

	public static boolean notIn(CharSequence cs, CharSequence... in) {
		return !in(cs, in);
	}

	public static void replace(StringBuilder sb, String target, String replacement) {
		int from = 0;
		while (true) {
			int start = sb.indexOf(target, from);
			if (start == -1) {
				break;
			}
			int end = start + target.length();
			sb.replace(start, end, replacement);
			from = start + replacement.length();
		}
	}
}
