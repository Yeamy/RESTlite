package yeamy.restlite.annotation;

class SupportType {
    public static final String T_HttpServletRequest = "javax.servlet.http.HttpServletRequest";
    public static final String T_ServletInputStream = "javax.servlet.ServletInputStream";
    public static final String T_Cookie = "javax.servlet.http.Cookie";
    public static final String T_Cookies = T_Cookie + "[]";
    public static final String T_HttpRequest = "yeamy.restlite.RESTfulRequest";
    public static final String T_File = "yeamy.restlite.HttpRequestFile";
    public static final String T_Files = T_File + "[]";
    public static final String T_Decimal = "java.math.BigDecimal";
    public static final String T_Decimals = T_Decimal + "[]";
    public static final String T_String = "java.lang.String";
    public static final String T_InputStream = "java.io.InputStream";
    public static final String T_Bytes = "byte[]";
    public static final String T_Bools = "boolean[]";
    public static final String T_Ints = "int[]";
    public static final String T_Longs = "long[]";
    public static final String T_Strings = T_String + "[]";
}
