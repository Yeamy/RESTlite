package yeamy.utils;

import java.math.BigDecimal;

public class ValueUtils {

    public static int toInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Integer toInteger(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static long toLong(String value, long fallback) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Long toLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static float toFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Float toFloat(String value) {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static double toDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Double toDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean toBoolean(String value, boolean fallback) {
        try {
            return Boolean.parseBoolean(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Boolean toBoolean(String value) {
        try {
            return Boolean.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static short toShort(String value, short fallback) {
        try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Short toShort(String value) {
        try {
            return Short.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal toBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static BigDecimal[] allToBigDecimal(String[] values) {
        if (values == null) {
            return null;
        }
        int l = values.length;
        BigDecimal[] out = new BigDecimal[l];
        for (int i = 0; i < l; i++) {
            try {
                out[i] = new BigDecimal(values[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    public static Boolean[] allToBoolean(String[] values) {
        if (values == null) {
            return null;
        }
        int l = values.length;
        Boolean[] out = new Boolean[l];
        for (int i = 0; i < l; i++) {
            out[i] = Boolean.valueOf(values[i]);
        }
        return out;
    }

    public static Integer[] allToInteger(String[] values) {
        if (values == null) {
            return null;
        }
        int l = values.length;
        Integer[] out = new Integer[l];
        for (int i = 0; i < l; i++) {
            try {
                out[i] = Integer.valueOf(values[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    public static Long[] allToLong(String[] values) {
        if (values == null) {
            return null;
        }
        int l = values.length;
        Long[] out = new Long[l];
        for (int i = 0; i < l; i++) {
            try {
                out[i] = Long.valueOf(values[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    public static Float[] allToFloat(String[] values) {
        if (values == null) {
            return null;
        }
        int l = values.length;
        Float[] out = new Float[l];
        for (int i = 0; i < l; i++) {
            try {
                out[i] = Float.valueOf(values[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

    public static Double[] allToDouble(String[] values) {
        if (values == null) {
            return null;
        }
        int l = values.length;
        Double[] out = new Double[l];
        for (int i = 0; i < l; i++) {
            try {
                out[i] = Double.valueOf(values[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return out;
    }

}
