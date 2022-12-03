package yeamy.restlite.annotation;

class SourceParamFail extends SourceParamCreator {

    @Override
    public CharSequence toCharSequence(SourceParamChain chain, String name) {
        return "";
    }

    @Override
    public CharSequence toCharSequence(SourceParamChain chain) {
        return "";
    }

}
