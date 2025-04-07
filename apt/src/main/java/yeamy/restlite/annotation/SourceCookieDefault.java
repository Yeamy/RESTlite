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
            case T_String -> "String " + alias + " = _req.getCookieValue(\"" + name + "\");";
            case T_Cookie -> servlet.imports(T_Cookie) + " " + alias + " = _req.getCookie(\"" + name + "\");";
            case T_CookieArray -> servlet.imports(T_Cookie) + "[] " + alias + " = _req.getCookies();";
            default -> "";
        };
    }

}
