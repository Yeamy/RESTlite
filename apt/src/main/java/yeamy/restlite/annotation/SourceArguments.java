package yeamy.restlite.annotation;

import java.util.ArrayList;
import java.util.Iterator;

class SourceArguments implements Iterable<CharSequence> {

    private final ArrayList<Impl> list = new ArrayList<>();

    public static class Impl {
        private final Kind kind;
        private final String type, hName, jName;
        private CharSequence vs;
        private boolean required;// param
        private final boolean throwable, close, closeThrow, autoClose;// db

        private Impl(Kind kind, String type, String hName, String jName, boolean throwable, boolean close,
                     boolean closeThrow, boolean autoClose) {
            this.kind = kind;
            this.type = type;
            this.hName = hName;
            this.jName = jName;
            this.throwable = throwable;
            this.close = close;
            this.closeThrow = closeThrow;
            this.autoClose = autoClose;
        }

        private Impl(Kind kind, String type, String hName, String jName) {
            this(kind, type, hName, jName, false, false, false, false);
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

        void setRequired(boolean required) {
            this.required = required;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Impl) {
                Impl n = (Impl) obj;
                return n.kind == kind && n.hName.equals(hName);
            }
            return false;
        }

        public String name() {
            return hName;
        }
    }

    private enum Kind {
        header, cookie, param, body, none
    }

    public Impl addHeader(String name, String alias) {
        Impl impl = new Impl(Kind.header, "", name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addCookie(String name, String type, String alias) {
        Impl impl = new Impl(Kind.cookie, type, name, alias);
        list.add(impl);
        return impl;
    }

    public Impl addParam(String type, String name, String alias, boolean required) {
        Impl impl = new Impl(Kind.param, type, name, alias);
        impl.setRequired(required);
        list.add(impl);
        return impl;
    }

    public Impl addBody(String name) {
        Impl impl = new Impl(Kind.body, "", name, name);
        list.add(impl);
        return impl;
    }

    public void addFallback(String vs) {
        Impl impl = new Impl(Kind.none, "", vs, vs);
        list.add(impl);
    }

    public void addExist(String vs) {
        Impl impl = new Impl(Kind.none, "", vs, vs);
        list.add(impl);
    }

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

    @Override
    public Iterator<CharSequence> iterator() {
        ArrayList<CharSequence> out = new ArrayList<>(list.size());
        for (Impl impl : list) {
            if (impl.kind != Kind.none && impl.kind != Kind.body) {
                out.add(impl.vs);
            }
        }
        for (Impl impl : list) {
            if (impl.kind == Kind.body) {
                out.add(impl.vs);
            }
        }
        return out.iterator();
    }

    public ArrayList<String> names() {
        ArrayList<String> out = new ArrayList<>(list.size());
        for (Impl impl : list) {
            if (impl.kind == Kind.none) {
                out.add(impl.vs.toString());
            } else {
                out.add(impl.hName);
            }
        }
        return out;
    }

    public ArrayList<String> getRequiredParams() {
        ArrayList<String> out = new ArrayList<>(list.size());
        for (Impl a : list) {
            if (a.required) {
                out.add(a.hName);
            }
        }
        return out;
    }

    public ArrayList<CharSequence> throwList() {
        ArrayList<CharSequence> out = new ArrayList<>(list.size());
        for (Impl a : list) {
            if (a.throwable) {
                out.add(a.vs);
            }
        }
        return out;
    }

    public ArrayList<String> closeNoThrow() {
        ArrayList<String> out = new ArrayList<>(list.size());
        for (Impl a : list) {
            if (a.autoClose && a.close && !a.closeThrow) {
                out.add(a.hName);
            }
        }
        return out;
    }

    public ArrayList<String> closeThrow() {
        ArrayList<String> out = new ArrayList<>(list.size());
        for (Impl a : list) {
            if (a.autoClose && a.closeThrow) {
                out.add(a.hName);
            }
        }
        return out;
    }

}