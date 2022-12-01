package yeamy.restlite.permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPermissionManager extends PermissionManager {
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Role> roles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Permission> permissions = new ConcurrentHashMap<>();

    @Override
    public void destroy() {
    }

    @Override
    public void setAccount(String id, String... roles) {
        Account account = accounts.get(id);
        if (account == null) {
            accounts.put(id, new Account(roles));
        } else {
            account.setRoles(roles);
        }
    }

    @Override
    public void deleteAccount(String id) {
        accounts.remove(id);
    }

    @Override
    public Account getAccount(String id) {
        return accounts.get(id);
    }

    @Override
    public void setRole(String id, Collection<String> permissions) {
        Role role = getRole(id);
        if (role == null) {
            roles.put(id, new Role(permissions));
        } else {
            role.setPermission(permissions);
        }
    }

    @Override
    public void deleteRole(String id) {
        roles.remove(id);
    }

    @Override
    public Role getRole(String id) {
        return roles.get(id);
    }

    @Override
    public Collection<Role> getRole(String[] ids) {
        ArrayList<Role> rs = new ArrayList<>();
        for (String id : ids) {
            Role r = roles.get(id);
            if (r != null) {
                rs.add(r);
            }
        }
        return rs;
    }

    @Override
    public void setPermission(String rule) {
        Permission permission = permissions.get(rule);
        if (permission == null) {
            permissions.put(rule, new Permission(rule));
        } else {
            permission.update(rule);
        }
    }

    @Override
    public void deletePermission(String rule) {
        permissions.remove(rule);
    }

    @Override
    public Permission getPermission(String rule) {
        return permissions.get(rule);
    }

    @Override
    public Collection<Permission> getPermission(Collection<String> rules) {
        ArrayList<Permission> permissions = new ArrayList<>();
        for (String rule : rules) {
            this.permissions.get(rule);
        }
        return permissions;
    }

}
