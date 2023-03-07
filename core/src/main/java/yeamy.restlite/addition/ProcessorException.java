package yeamy.restlite.addition;

import jakarta.servlet.ServletException;

public class ProcessorException extends ServletException {
    private final String name;

    public ProcessorException(String name, String msg) {
        super(msg);
        this.name = name;
    }

    public ProcessorException(String name, Throwable throwable) {
        super(throwable);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
