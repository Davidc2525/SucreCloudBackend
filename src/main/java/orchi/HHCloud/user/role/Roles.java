package orchi.HHCloud.user.role;

/**
 * Roles de usuario, 0 = ADMIN, 1 = USER (usuario normal)
 * */
public enum Roles {
    ADMIN("0"), USER("1");

    public final String role;

    Roles(String role) {
        this.role = role;
    }

    public boolean equalsName(String otherName) {
        // (otherName == null) check is not needed because role.equals(null)
        // returns false
        return role.equalsIgnoreCase(otherName);
    }

    public String toString() {
        return this.role;
    }

    public static Roles of(String n){
        switch (n){
            case "0":
                return Roles.ADMIN;
            case "1":
                return Roles.USER;
            default:
                return Roles.USER;
        }
    }
}