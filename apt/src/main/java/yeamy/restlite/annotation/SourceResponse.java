package yeamy.restlite.annotation;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;

import static yeamy.restlite.annotation.SupportType.T_TextPlainResponse;

class SourceResponse {
    private final String name;
    private final List<ExecutableElement> constructors = new ArrayList<>();
    private final TypeMirror STR;

    public SourceResponse(ProcessEnvironment env, String name) {
        this.name = name;
        this.STR = env.getTypeElement("java.lang.String").asType();
        TypeElement type = env.getTypeElement(name);
        for (Element li : type.getEnclosedElements()) {
            if (li.getKind() != ElementKind.CONSTRUCTOR) continue;
            if (!li.getModifiers().contains(Modifier.PUBLIC)) continue;
            ExecutableElement constructor = (ExecutableElement) li;
            List<? extends VariableElement> params = constructor.getParameters();
            if (params.size() != 2) continue;
            constructors.add(constructor);
        }
    }

    public void write(ProcessEnvironment env, SourceServlet servlet, String httpMethod, TypeMirror rt, Runnable writeImpl) {
        for (ExecutableElement constructor : constructors) {
            TypeMirror p0 = constructor.getParameters().get(0).asType();
            TypeMirror p1 = constructor.getParameters().get(1).asType();
            if (env.isAssignable(rt, p0) && p1.toString().equals("int")) {
                servlet.append("new ").append(servlet.imports(name)).append('(');
                writeImpl.run();
                servlet.append(',').append(getCode(httpMethod)).append(").write(_resp);");
                return;
            } else if (p0.toString().equals("int") && env.isAssignable(rt, p1)) {
                servlet.append("new ").append(servlet.imports(name)).append('(').append(getCode(httpMethod)).append(',');
                writeImpl.run();
                servlet.append(").write(_resp);");
                return;
            }
        }
        String msg = "Cannot find target constructor of " + name + " accept parameter: " + rt;
        env.error(msg);
        servlet.append("new ").append(servlet.imports(T_TextPlainResponse))
                .append("(500,\"").append(msg).append("\").write(_resp);");
    }

    private static int getCode(String httpMethod) {
        return switch (httpMethod) {
            case "PUT", "POST" -> 201;
            default -> 200;
        };
    }

    public void writeError(ProcessEnvironment env, SourceServlet servlet, int code, String msg) {
        for (ExecutableElement constructor : constructors) {
            TypeMirror p0 = constructor.getParameters().get(0).asType();
            TypeMirror p1 = constructor.getParameters().get(1).asType();
            if (env.isAssignable(STR, p0) && p1.toString().equals("int")) {
                servlet.append("new ").append(servlet.imports(name)).append("(\"").append(msg).append("\",").append(code)
                        .append(").write(_resp);");
                return;
            } else if (p0.toString().equals("int") && env.isAssignable(STR, p1)) {
                servlet.append("new ").append(servlet.imports(name)).append('(').append(code).append(",\"").append(msg)
                        .append("\").write(_resp);");
                return;
            }
        }
        servlet.append("new ").append(servlet.imports(T_TextPlainResponse)).append('(').append(code).append(",\"")
                .append(msg).append("\").write(_resp);");
    }

}
