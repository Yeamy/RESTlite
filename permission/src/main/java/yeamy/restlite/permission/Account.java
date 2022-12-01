package yeamy.restlite.permission;

public class Account {
    private String[] roles;

    public Account(String... roles) {
        this.roles = roles;
    }

    public void setRoles(String... roles) {
        this.roles = roles;
    }

    public String[] getRoles() {
        return this.roles;
    }
}
