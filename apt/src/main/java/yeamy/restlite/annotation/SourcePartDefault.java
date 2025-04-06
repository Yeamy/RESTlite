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
            case T_Part -> servlet.imports(T_File) + " " + name + " = _req.getPart(\"" + alias + "\");";
            case T_File -> servlet.imports(T_File) + " " + name + " = _req.getFile(\"" + alias + "\");";
            case T_InputStream -> servlet.imports(T_InputStream) + " " + name + " = "
                    + servlet.imports("yeamy.utils.IfNotNull") + ".invoke(_req.getFile(\"" + alias + "\"),a->a.get());";
            case T_ByteArray -> "byte[] " + name + " = " + servlet.imports("yeamy.utils.IfNotNull")
                    + ".invoke(_req.getFile(\"" + alias + "\"),a->a.getAsByte());";
            case T_String -> "String " + name + " = " + servlet.imports("yeamy.utils.IfNotNull")
                    + ".invoke(_req.getFile(\"" + alias + "\"),a->a.getAsText());";
            default -> "";
        };
    }

}
