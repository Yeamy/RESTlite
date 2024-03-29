package yeamy.restlite.annotation;

import javax.lang.model.element.VariableElement;

class SourceProcessorFail extends SourceProcessor {

    final static SourceProcessorFail INSTANCE = new SourceProcessorFail();

    private SourceProcessorFail() {
        super(null, null, null, null);
    }

    @Override
    public void addToArgs(SourceArguments args, SourceServlet servlet, VariableElement p, String name) {
        switch (p.asType().getKind()) {
            case BOOLEAN -> args.addFallback("false");
            case BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE -> args.addFallback("0");
            default -> args.addFallback("null");
        }
    }
}
