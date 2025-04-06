package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

import static yeamy.restlite.annotation.SupportType.*;

class SourceCookieDefault extends SourceCookie {
    private final String returnType;

    SourceCookieDefault(ProcessEnvironment env, VariableElement param, String returnType) {
        super(env, param);
        this.returnType = returnType;
    }

    @Override
    public CharSequence write(SourceServlet servlet, String name, String alias) {
        return switch (returnType) {
            case T_int, T_Integer -> "int " + alias + " = _req.getIntHeader(\"" + name + "\");";
            case T_long, T_Long -> "long " + alias + " = _req.getDateHeader(\"" + name + "\");";
            case T_String -> "String " + alias + " = _req.getHeader(\"" + name + "\");";
            case T_Date -> {
                String iType = servlet.imports(T_Date);
                yield iType + " " + alias + " = new " + iType + "(_req.getDateHeader(\"" + name + "\"));";
            }
            default -> "";
        };
    }

}
