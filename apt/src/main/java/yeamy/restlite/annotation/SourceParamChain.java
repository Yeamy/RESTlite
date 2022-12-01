package yeamy.restlite.annotation;

import java.util.HashMap;

class SourceParamChain {
    private final ProcessEnvironment env;
    private final SourceServlet servlet;
    private final SourceArguments args;
    private final HashMap<String, SourceParamCreator> chain = new HashMap<>();

    public SourceParamChain(ProcessEnvironment env, SourceServlet servlet, SourceArguments args) {
        this.env = env;
        this.servlet = servlet;
        this.args = args;
    }

    public boolean add(SourceParamCreator creator) {
        String id = creator.getID();
        if (chain.containsKey(id)) {
            return false;
        }
        chain.put(id, creator);
        return true;
    }

    public ProcessEnvironment getEnvironment() {
        return env;
    }

    public SourceServlet getServlet() {
        return servlet;
    }

    public SourceArguments getArguments() {
        return args;
    }
}
