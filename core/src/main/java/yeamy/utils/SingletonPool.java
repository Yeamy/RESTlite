package yeamy.utils;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Singleton Pool
 */
public final class SingletonPool {
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final ConcurrentHashMap<String, Object> POOL = new ConcurrentHashMap<>();

    private SingletonPool() {
    }

    /**
     * create instance but not cache.
     *
     * @param clz    class to create instance.
     * @param params constructor parameters
     * @param <T>    class type
     * @return new instance
     */
    public static <T> T createInstance(Class<T> clz, Class<?>... params) {
        try {
            Object[] args = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                args[i] = POOL.get(getKey(params[i]));
            }
            return clz.getConstructor(params).newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param clz    class to create instance.
     * @param params constructor's params
     * @return key of instance:<br>
     * <b>no param</b> -> className<br>
     * <b>1 param</b> -> className:arg1<br>
     * <b>2 params</b> -> className:arg1,arg2<br>
     * ...
     */
    public static String getKey(Class<?> clz, Class<?>... params) {
        if (params.length > 0) {
            StringBuilder sb = new StringBuilder(clz.getName()).append('(');
            for (Class<?> p : params) {
                sb.append(p.getName()).append(',');
            }
            sb.setCharAt(sb.length() - 1, ')');
            return sb.toString();
        }
        return clz.getName();
    }
    //----------------------------------

    /**
     * cache the object with its class(no parameter constructor)
     *
     * @param obj object to cache
     */
    public static void add(Object obj) {
        POOL.put(getKey(obj.getClass()), obj);
    }

    /**
     * cache the object with the given key
     *
     * @param key given key
     * @param obj class instance to cache
     */
    public static void add(String key, Object obj) {
        POOL.put(key, obj);
    }

    /**
     * cache the object with the given class(no parameter constructor)
     *
     * @param clz given class(no parameter constructor)
     * @param obj class instance to cache
     */
    public static void add(Class<?> clz, Object obj) {
        POOL.put(getKey(clz), obj);
    }

    /**
     * cache the object with the given class and given constructor parameter
     *
     * @param clz    given class
     * @param params constructor parameter
     * @param obj    class instance to cache
     */
    public static void add(Class<?> clz, Class<?>[] params, Object obj) {
        POOL.put(getKey(clz, params), obj);
    }
    //----------------------------------

    /**
     * get the single instance, if not exist return null;
     *
     * @return the instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) POOL.get(key);
    }

    /**
     * get the single instance, if not exist return null;
     *
     * @return the instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clz, Class<?>... params) {
        return (T) POOL.get(getKey(clz, params));
    }

    /**
     * get the single instance, if not exist, create it.
     *
     * @param key      given key
     * @param function create instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrCreate(String key, Function<String, T> function) {
        return (T) POOL.computeIfAbsent(key, function);
    }

    /**
     * get the single instance, if not exist, create it.
     *
     * @param clz    given class
     * @param params constructor parameter
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrCreate(Class<T> clz, Class<?>... params) {
        return (T) POOL.computeIfAbsent(getKey(clz, params), k -> createInstance(clz, params));
    }

    /**
     * get the single instance, if not exist, invoke the given function to create it.
     *
     * @param clz      given class
     * @param params   constructor parameter
     * @param function create instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrCreate(Class<T> clz, Class<?>[] params, Function<String, T> function) {
        return (T) POOL.computeIfAbsent(getKey(clz, params), function);
    }

    /**
     * get the single instance, if not exist, invoke the given function to create it.
     *
     * @param clz      given class
     * @param function create instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrCreate(Class<T> clz, Function<String, T> function) {
        return (T) POOL.computeIfAbsent(getKey(clz), function);
    }

    /**
     * check if contains given instance
     *
     * @param key given key
     */
    public static boolean contains(String key) {
        return POOL.containsKey(key);
    }

    /**
     * check if contains given instance
     *
     * @param clz    given class
     * @param params constructor parameter
     */
    public static boolean contains(Class<?> clz, Class<?>... params) {
        return POOL.containsKey(getKey(clz, params));
    }

    /**
     * get all cache instance of given type
     *
     * @param clz given type
     * @return No duplicate set
     */
    public static Set<Object> getAll(Class<?> clz) {
        LinkedHashSet<Object> out = new LinkedHashSet<>();
        for (Object obj : POOL.values()) {
            if (obj.getClass().equals(clz)) {
                out.add(obj);
            }
        }
        return out;
    }

    /**
     * remove instance
     *
     * @param key given key
     */
    public static void remove(String key) {
        POOL.remove(key);
    }


    /**
     * remove instance
     *
     * @param clz    given class
     * @param params constructor parameter
     */
    public static void remove(Class<?> clz, Class<?>... params) {
        POOL.remove(getKey(clz, params));
    }

    /**
     * remove all cached instance
     */
    public static void clear() {
        POOL.clear();
    }

}