package yeamy.restlite.permission;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class Role {
    private HashSet<String> permissions;

    public Role(String[] permissions) {
        this.permissions = new HashSet<>();
        Collections.addAll(this.permissions, permissions);
    }

    public Role(Collection<String> permissions) {
        this.permissions = new HashSet<>(permissions);
    }

    public void setPermission(Collection<String> permissions) {
        this.permissions = new HashSet<>(permissions);
    }

    public Collection<String> getPermissions() {
        return permissions;
    }

}
