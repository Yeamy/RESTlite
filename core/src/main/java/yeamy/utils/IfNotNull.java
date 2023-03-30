package yeamy.utils;

public class IfNotNull {

    public static <T, R> R invoke(T in, Function<T, R> run) {
        if (in == null) return null;
        try {
            return run.apply(in);
        } catch (Exception e) {
            return null;
        }
    }

    //import java.util.function.Function;
    public interface Function<I, R> {
        R apply(I t) throws Exception;
    }

}
