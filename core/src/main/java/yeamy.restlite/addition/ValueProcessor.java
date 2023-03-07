package yeamy.restlite.addition;

import yeamy.restlite.RESTfulRequest;

public interface ValueProcessor<T> {

    void process(T t, RESTfulRequest req) throws ProcessorException;
}
