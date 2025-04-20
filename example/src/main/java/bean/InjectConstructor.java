package bean;

import yeamy.restlite.annotation.InjectProvider;

import java.io.Closeable;
import java.io.IOException;

public class InjectConstructor implements Closeable {
    @InjectProvider
    public InjectConstructor() {
    }

    @Override
    public void close() throws IOException {
    }
}
