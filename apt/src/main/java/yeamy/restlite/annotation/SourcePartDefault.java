package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

import static yeamy.restlite.annotation.SupportType.*;

class SourcePartDefault extends SourcePart {
    private final String returnType;

    SourcePartDefault(ProcessEnvironment env, VariableElement param, String returnType) {
        super(env, param);
        this.returnType = returnType;
    }

    @Override
    public CharSequence write(SourceServlet servlet, String name, String alias) {
        return switch (returnType) {
            case T_Part -> servlet.imports(T_Part) + " " + alias + " = _req.getPart(\"" + name + "\");";
            case T_HttpRequestFile -> servlet.imports(T_HttpRequestFile) + " " + alias + " = _req.getFile(\"" + name + "\");";
            case T_InputStream -> servlet.imports(T_InputStream) + " " + alias + " = "
                    + servlet.imports("yeamy.utils.IfNotNull") + ".invoke(_req.getFile(\"" + name + "\"),a->a.get());";
            case T_ByteArray -> "byte[] " + alias + " = " + servlet.imports("yeamy.utils.IfNotNull")
                    + ".invoke(_req.getFile(\"" + name + "\"),a->a.getAsByte());";
            case T_String -> "String " + alias + " = " + servlet.imports("yeamy.utils.IfNotNull")
                    + ".invoke(_req.getFile(\"" + name + "\"),a->a.getAsText());";
            default -> "";
        };
    }

}
