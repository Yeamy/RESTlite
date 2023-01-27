package yeamy.restlite.annotation;

import java.util.ArrayList;

class SourceArguments {

    private final ArrayList<Impl> list = new ArrayList<>();

    public static class Impl {
        private final Kind kind;
        private final String type, hName, jName;
        private CharSequence vs;// out string e.g. String a = _req.getParam("a");
        private final boolean throwable, closeable, closeThrow;// db

        private Impl(Kind kind, String type, String hName, String jName, boolean throwable, boolean closeable, boolean closeThrow) {
            this.kind = kind;
            this.type = type;// class type
            this.hName = hName;// http name
            this.jName = jName;// java name
            this.throwable = throwable;
            this.closeable = closeable;
            this.closeThrow = closeThrow;
        }

        private Impl(Kind kind, String type, String hName, String jName) {
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
            if (obj instanceof Impl n) {
                return n.kind == kind && n.hName.equals(hName);
            }
            return false;
        }

        public String name() {
            return jName.length() > 0 ? jName : hName;
        }
    }

    private enum Kind {
        header, cookie, param, body, inject, exist, fallback
    }

    public Impl addHeader(String name, String alias) {
        Impl impl = new Impl(Kind.header, "", name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addCookie(String type, String name, String alias) {
        Impl impl = new Impl(Kind.cookie, type, name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addParam(String type, String name, String alias) {
        Impl impl = new Impl(Kind.param, type, name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addBody(String name, boolean throwable, boolean closeable, boolean closeThrow) {
        Impl impl = new Impl(Kind.body, "", name, name, throwable, closeable, closeThrow);
        list.add(impl);
        return impl;
    }

    public Impl addInject(String name, boolean throwable, boolean closeable, boolean closeThrow) {
        Impl impl = new Impl(Kind.inject, "", name, name, throwable, closeable, closeThrow);
        list.add(impl);
        return impl;
    }

    public void addFallback(String name) {
        Impl impl = new Impl(Kind.fallback, "", name, name);
        list.add(impl);
    }

    public void addExist(String name) {
        Impl impl = new Impl(Kind.exist, "", name, name);
        list.add(impl);
    }

    /**
     * @param kind only: header cookie, param
     */
    private Impl get(Kind kind, String name) {
        for (Impl a : list) {
            if (a.kind == kind && a.hName.equals(name)) {
                return a;
            }
        }
        return null;
    }

    public String getHeaderAlias(String hName) {
        Impl cell = get(Kind.header, hName);
        return cell == null ? null : cell.jName;
    }

    public String getCookieAlias(String type, String name) {
        Impl cell = get(Kind.cookie, name);
        return cell == null ? null : cell.type.equals(type) ? cell.jName : null;
    }

    public String getParamAlias(String type, String name) {
        Impl cell = get(Kind.param, name);
        return cell == null ? null : cell.type.equals(type) ? cell.jName : null;
    }

    public boolean containsBody() {
        for (Impl a : list) {
            if (a.kind == Kind.body) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<CharSequence> getNormal() {
        ArrayList<CharSequence> out = new ArrayList<>(list.size());
        for (Impl impl : list) {
            if (impl.kind != Kind.fallback // without fallback
                    && impl.kind != Kind.body // body at the end
                    && impl.kind != Kind.inject) { // inject at the end
                out.add(impl.vs);
            }
        }
        for (Impl impl : list) {
            if ((impl.kind == Kind.body || impl.kind == Kind.inject)
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
