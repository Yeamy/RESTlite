package yeamy.restlite.annotation;

import yeamy.utils.TextUtils;

import java.util.ArrayList;

class SourceArguments {

    private final ArrayList<Impl> list = new ArrayList<>();

    public static class Impl {
        private final ArgType kind;
        private final String type, hName, jName;
        private CharSequence vs;// out string e.g. String a = _req.getParam("a");
        private final boolean throwable, closeable, closeThrow;// db

        private Impl(ArgType kind, String type, String hName, String jName, boolean throwable, boolean closeable, boolean closeThrow) {
            this.kind = kind;
            this.type = type;// class type
            this.hName = hName;// http name
            this.jName = jName;// java name
            this.throwable = throwable;
            this.closeable = closeable;
            this.closeThrow = closeThrow;
        }

        private Impl(ArgType kind, String type, String hName, String jName) {
            this(kind, type, hName, jName, false, false, false);
        }

        public void write(CharSequence... vs) {
            if (vs.length == 1) {
                this.vs = vs[0];
            } else {
                StringBuilder b = new StringBuilder();
                for (CharSequence s : vs) {
                    b.append(s);
                }
                this.vs = b;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Impl impl) {
                return impl.kind == kind && impl.hName.equals(hName);
            }
            return false;
        }

        public String name() {
            return jName.length() > 0 ? jName : hName;
        }
    }

    public Impl addHeader(String type, String name, String alias) {
        Impl impl = new Impl(ArgType.header, type, name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addAttribute(String name, String alias) {
        Impl impl = new Impl(ArgType.attribute, "", name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addCookie(String type, String name, String alias) {
        Impl impl = new Impl(ArgType.cookie, type, name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addParam(String type, String name, String alias) {
        Impl impl = new Impl(ArgType.param, type, name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addPart(String name, String alias, boolean throwable, boolean closeable, boolean closeThrow) {
        Impl impl = new Impl(ArgType.part, "", name, alias, throwable, closeable, closeThrow);
        list.add(impl);
        return impl;
    }

    public Impl addBody(String name, boolean throwable, boolean closeable, boolean closeThrow) {
        Impl impl = new Impl(ArgType.body, "", name, name, throwable, closeable, closeThrow);
        list.add(impl);
        return impl;
    }

    public Impl addInject(String name, boolean throwable, boolean closeable, boolean closeThrow) {
        Impl impl = new Impl(ArgType.inject, "", name, name, throwable, closeable, closeThrow);
        list.add(impl);
        return impl;
    }

    public void addFallback(String name) {
        Impl impl = new Impl(ArgType.fallback, "", name, name);
        list.add(impl);
    }

    public void addExist(String name) {
        Impl impl = new Impl(ArgType.exist, "", name, name);
        list.add(impl);
    }

    /**
     * @param kind only: header cookie, param
     */
    private Impl get(ArgType kind, String name) {
        for (Impl a : list) {
            if (a.kind == kind && a.hName.equals(name)) {
                return a;
            }
        }
        return null;
    }

    public String getAttributeAlias(String type, String hName) {
        Impl cell = get(ArgType.attribute, hName);
        return cell == null
                ? null
                : TextUtils.equals(type, cell.type) ? cell.jName : null;
    }

    public String getHeaderAlias(String type, String hName) {
        Impl cell = get(ArgType.header, hName);
        return cell == null
                ? null
                : TextUtils.equals(type, cell.type) ? cell.jName : null;
    }

    public String getCookieAlias(String type, String name) {
        Impl cell = get(ArgType.cookie, name);
        return cell == null ? null : cell.type.equals(type) ? cell.jName : null;
    }

    public String getParamAlias(String type, String name) {
        Impl cell = get(ArgType.param, name);
        return cell == null ? null : cell.type.equals(type) ? cell.jName : null;
    }

    public boolean containsBodyOrPart() {
        for (Impl a : list) {
            if (a.kind == ArgType.body || a.kind == ArgType.part) {
                return true;
            }
        }
        return false;
    }

    public boolean containsBody() {
        for (Impl a : list) {
            if (a.kind == ArgType.body) {
                return true;
            }
        }
        return false;
    }

    public boolean containsPart(String name) {
        for (Impl a : list) {
            if (a.kind == ArgType.part && a.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<CharSequence> getNormal() {
        ArrayList<CharSequence> out = new ArrayList<>(list.size());
        for (Impl impl : list) {
            if (impl.kind != ArgType.fallback // without fallback
                    && impl.kind != ArgType.body // body at the end
                    && impl.kind != ArgType.part // part at the end
                    && impl.kind != ArgType.inject) { // inject at the end
                out.add(impl.vs);
            }
        }
        for (Impl impl : list) {
            if ((impl.kind == ArgType.body || impl.kind == ArgType.part || impl.kind == ArgType.inject)
                    && !impl.throwable && !impl.closeable && !impl.closeThrow) {
                out.add(impl.vs);
            }
        }
        return out;
    }

    public ArrayList<CharSequence> getCloseable() {
        ArrayList<CharSequence> out = new ArrayList<>(list.size());
        for (Impl impl : list) {
            if (impl.closeable || impl.closeThrow) {
                out.add(impl.vs);
            }
        }
        return out;
    }

    public ArrayList<CharSequence> getInTry() {
        ArrayList<CharSequence> out = new ArrayList<>(list.size());
        for (Impl impl : list) {
            if (impl.throwable && !impl.closeable) {
                out.add(impl.vs);
            }
        }
        return out;
    }

    public boolean hasThrow() {
        for (Impl impl : list) {
            if (impl.throwable || impl.closeable) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<String> getAlias() {
        ArrayList<String> out = new ArrayList<>(list.size());
        for (Impl impl : list) {
            out.add(impl.name());
        }
        return out;
    }

}
