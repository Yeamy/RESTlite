package yeamy.restlite.addition;

import yeamy.restlite.RESTfulRequest;

public final class NoProcessor implements ValueProcessor<Object> {

    @Override
    public void process(Object o, RESTfulRequest req) throws ProcessorException {
    }
}
