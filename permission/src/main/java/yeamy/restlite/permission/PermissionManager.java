package yeamy.restlite.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class PermissionManager {
    public static final String NO_LOGIN_ROLE = "NO_LOGIN_ROLE";
    private static PermissionManager instance;

    public static PermissionManager getInstance() {
        return instance;
    }

    public static void setInstance(PermissionManager instance) {
        PermissionManager.instance = instance;
    }

    public abstract void destroy();

    public void setAccount(String id, Collection<String> roles) {
        String[] array = new String[roles.size()];
        roles.toArray(array);
        setAccount(id, array);
    }

    public abstract void setAccount(String id, String... roles);

    public abstract Account getAccount(String id);

    public abstract void deleteAccount(String id);

    public abstract void setRole(String id, Collection<String> permissions);

    public void setRole(String id, String... permissions) {
        setRole(id, Arrays.asList(permissions));
    }

    public abstract void deleteRole(String id);

    public abstract Role getRole(String role);

    public abstract Collection<Role> getRole(String[] roles);

    public abstract void setPermission(String rule);

    public abstract void deletePermission(String id);

    public abstract Permission getPermission(String id);

    public abstract Collection<Permission> getPermission(Collection<String> id);

    public boolean isAllow(Account account, String resource, String method, Collection<String> params) {
        if (account != null) {
            ArrayList<String> permissions = new ArrayList<>();
            for (Role role : getRole(account.getRoles())) {
                permissions.addAll(role.getPermissions());
            }
            boolean allow = false;
            for (Permission permission : getPermission(permissions)) {
                switch (permission.isAllow(resource, method, params)) {
                    case deny:
                        return false;
                    case allow:
                        allow = true;
                }
            }
            if (allow) return true;
        }
        Role role = getRole(NO_LOGIN_ROLE);
        if (role == null) return false;
        for (Permission permission : getPermission(role.getPermissions())) {
            if (permission.isAllow(resource, method, params) == CheckResult.allow) {
                return true;
            }
        }
        return false;
    }
}
