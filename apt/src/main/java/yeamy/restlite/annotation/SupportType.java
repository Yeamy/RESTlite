package yeamy.restlite.annotation;

class SupportType {
    public static final String T_HttpServletRequest = "jakarta.servlet.http.HttpServletRequest";
    public static final String T_ServletInputStream = "jakarta.servlet.ServletInputStream";
    public static final String T_Cookie = "jakarta.servlet.http.Cookie";
    public static final String T_Cookies = T_Cookie + "[]";
    public static final String T_HttpRequest = "yeamy.restlite.RESTfulRequest";
    public static final String T_File = "yeamy.restlite.HttpRequestFile";
    public static final String T_Files = T_File + "[]";
    public static final String T_Decimal = "java.math.BigDecimal";
    public static final String T_DecimalArray = T_Decimal + "[]";
    public static final String T_String = "java.lang.String";
    public static final String T_Date = "java.util.Date";
    public static final String T_Part = "jakarta.servlet.http.Part";
    public static final String T_Parts = T_Part + "[]";
    public static final String T_InputStream = "java.io.InputStream";
    public static final String T_Integer = "java.lang.Integer";
    public static final String T_Long = "java.lang.Long";
    public static final String T_Float = "java.lang.Float";
    public static final String T_Double = "java.lang.Double";
    public static final String T_Boolean = "java.lang.Boolean";
    public static final String T_ByteArray = "byte[]";
    public static final String T_BooleanArray = T_Boolean + "[]";
    public static final String T_IntegerArray = T_Integer + "[]";
    public static final String T_LongArray = T_Long + "[]";
    public static final String T_FloatArray = T_Float + "[]";
    public static final String T_DoubleArray = T_Double + "[]";
    public static final String T_StringArray = T_String + "[]";
    public static final String T_TextPlainResponse = "yeamy.restlite.addition.TextPlainResponse";
}
