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
    public static final String T_Decimals = T_Decimal + "[]";
    public static final String T_String = "java.lang.String";
    public static final String T_Date = "java.util.Date";
    public static final String T_Part = "jakarta.servlet.http.Part";
    public static final String T_Parts = T_Part + "[]";
    public static final String T_InputStream = "java.io.InputStream";
    public static final String T_Bytes = "byte[]";
    public static final String T_Booleans = "boolean[]";
    public static final String T_Integers = "int[]";
    public static final String T_Longs = "long[]";
    public static final String T_Strings = T_String + "[]";
    public static final String T_TextPlainResponse = "yeamy.restlite.addition.TextPlainResponse";
}
