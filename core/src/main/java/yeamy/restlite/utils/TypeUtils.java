package yeamy.restlite.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeUtils<T> {

    public Type getType() {
        Type type = getClass().getGenericSuperclass();
        System.out.println(type.getClass());
        if (type instanceof ParameterizedType pType) {
            Type[] typeArgs = pType.getActualTypeArguments();
            return typeArgs[0];
        }
        return null;
    }

}
