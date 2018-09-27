package orchi.HHCloud.user.role;

public class Role {

    private Roles role = Roles.USER;

    public Role(){}

    public Role(Roles role) {
        this.role = role;
    }
    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

}
