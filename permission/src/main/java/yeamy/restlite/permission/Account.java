package yeamy.restlite.permission;

/**
 * permission account
 */
public class Account {
    private String[] roles;

    /**
     * @param roles permission roles
     */
    public Account(String... roles) {
        this.roles = roles;
    }

    /**
     * replace roles
     */
    public void setRoles(String... roles) {
        this.roles = roles;
    }

    public String[] getRoles() {
        return this.roles;
    }
}
