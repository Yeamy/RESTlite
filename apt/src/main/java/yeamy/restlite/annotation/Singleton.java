package yeamy.restlite.annotation;

/**
 * singleton instance option
 */
public enum Singleton {
    /**
     * Determined by the framework
     */
    auto,

    /**
     * save to SingletonPool
     */
    yes,

    /**
     * create new instance
     */
    no
}
