package yeamy.restlite.annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;

class ProcessorMap<T> {
    private final HashMap<String, HashMap<String, T>> map = new HashMap<>();

    private String getType(Element element) {
        return switch (element.getKind()) {
            case FIELD -> element.asType().toString();
            case CONSTRUCTOR -> element.getEnclosingElement().asType().toString();
            case METHOD -> {
                TypeMirror rt = ((ExecutableElement) element).getReturnType();
                yield rt.getKind().equals(TypeKind.TYPEVAR)
                        ? "?"
                        : rt.toString();
            }
            default -> "";
        };
    }

    public void add(Element element, String name, T t) {
        String type = getType(element);
        HashMap<String, T> subMap = map.computeIfAbsent(type, k -> new HashMap<>());
        subMap.put(name, t);
    }

    public T get(String type, String name) {
        HashMap<String, T> subMap = map.get(type);
        if (subMap == null) {
            subMap = map.get("?");
            if (subMap == null) {
                return null;
            }
        }
        T t = subMap.get(name);
        if (t != null) {
            return t;
        } else if ("".equals(name) && subMap.size() == 1) {
            return subMap.values().stream().iterator().next();
        } else {
            return null;
        }
    }

}
