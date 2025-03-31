package yeamy.restlite.permission;

import yeamy.utils.SingletonPool;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

/**
 * access rule for account
 */
public class Permission {
    public static final String[] NO_PARAM = SingletonPool.EMPTY_STRING_ARRAY;
    public static final String[] ALL_PARAM = SingletonPool.EMPTY_STRING_ARRAY;
    private boolean allow;
    private String resource;
    private String[] method;
    private String[] param;

    public Permission(String rule) {
        set(rule);
    }

    public synchronized void update(String rule) {
        set(rule);
    }

    public void set(String rule) {
        String[] rs = rule.split(":");
        this.allow = (rs[0].charAt(0) == 'A');
        this.resource = rs[1];
        char[] ms = rs[2].toCharArray();
        this.method = new String[ms.length];
        for (int i = 0; i < ms.length; i++) {
            method[i] = char2method(ms[i]);
        }
        String rp = rs[3];
        if (rp.isEmpty()) {
            this.param = NO_PARAM;
        } else if (rp.equals("*")) {
            this.param = ALL_PARAM;
        } else if (rp.contains(",")) {
            this.param = rp.split(",");
        } else {
            this.param = new String[]{rp};
        }
    }

    public Permission(boolean allow, String resource, String[] method, String[] param) {
        set(allow, resource, method, param);
    }

    public synchronized void update(boolean allow, String resource, String[] method, String[] param) {
        set(allow, resource, method, param);
    }

    private void set(boolean allow, String resource, String[] method, String[] param) {
        this.allow = allow;
        this.resource = resource;
        TreeSet<String> set = new TreeSet<>();
        for (String m : method) {
            switch (m) {
                case "DELETE":
                case "GET":
                case "PATCH":
                case "POST":
                case "PUT":
                    set.add(m);
                    break;
                default:
                    throw new RuntimeException("Invalid method");
            }
        }
        this.method = new String[set.size()];
        set.toArray(this.method);
        if (param == null) {
            this.param = NO_PARAM;
        } else if (param.length > 0 || param == ALL_PARAM) {
            this.param = param;
        } else {
            this.param = NO_PARAM;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(allow ? 'D' : 'A').append(':');
        sb.append(resource).append(':');
        for (String m : method) {
            sb.append(method2char(m));
        }
        sb.append(resource).append(':');
        boolean first = true;
        if (param == ALL_PARAM) {
            sb.append("*");
        } else {
            for (String p : param) {
                sb.append(p);
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
            }
        }
        return sb.toString();
    }

    private static String char2method(char c) {
        return switch (c) {
            case 'D' -> "DELETE";
            case 'G' -> "GET";
            case 'A' -> "PATCH";
            case 'O' -> "POST";
            case 'U' -> "PUT";
            default -> throw new RuntimeException("Invalid method");
        };
    }

    private static char method2char(String c) {
        return switch (c) {
            case "DELETE" -> 'D';
            case "GET" -> 'G';
            case "PATCH" -> 'A';
            case "POST" -> 'O';
            case "PUT" -> 'U';
            default -> throw new RuntimeException("Invalid method");
        };
    }

    public CheckResult isAllow(String resource, String method, Collection<String> params) {
        if (this.resource.equals("*") || this.resource.equals(resource)) {
            if (Arrays.binarySearch(this.method, method) >= 0) {
                if (checkParam(params)) {
                    return allow ? CheckResult.allow : CheckResult.deny;
                }
            }
        }
        return CheckResult.undefined;
    }

    private boolean checkParam(Collection<String> params) {
        if (this.param == ALL_PARAM) {
            return true;
        } else if (this.param == NO_PARAM && params.size() == 0) {
            return true;
        }
        for (String necessary : this.param) {
            if (!params.contains(necessary)) {
                return false;
            }
        }
        return true;
    }
}
