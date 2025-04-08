package bean;

import yeamy.restlite.annotation.InjectProvider;

public class InjectB extends InjectA {

    @InjectProvider(provideFor = InjectA.class)
    public static InjectB get() {
        return new InjectB();
    }
}
