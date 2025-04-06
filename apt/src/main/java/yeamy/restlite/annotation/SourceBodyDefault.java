package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

import static yeamy.restlite.annotation.SupportType.*;

class SourceBodyDefault extends SourceBody {
    private final String returnType;

    SourceBodyDefault(ProcessEnvironment env,
                      VariableElement param,
                      String returnType) {
        super(env, param);
        this.returnType = returnType;
    }

    @Override
    public CharSequence write(SourceServlet servlet, String name) {
        return switch (returnType) {
            case T_InputStream, T_ServletInputStream ->
                    servlet.imports(T_ServletInputStream) + " " + name + " = _req.getBody();";
            case T_ByteArray -> "byte[] " + name + " = _req.getBodyAsByte();";
            case T_String -> "String " + name + " = _req.getBodyAsText(\"" + env.charset() + "\");";
            case T_PartArray -> servlet.imports(T_File) + "[] " + name + " = _req.getParts();";
            case T_FileArray -> servlet.imports(T_File) + "[] " + name + " = _req.getFiles();";
            default -> "";
        };
    }

}
