package orchi.HHCloud.user.role;

import orchi.HHCloud.user.User;

public class DefaultRoleProvider implements  RoleProvider{
    /*
    *
    * SQL
        INSER INTO ROLES (IDUSER,ROLE) VALUES (?,?);
        SELECT * FROM ROLES WHERE IDUSER = (?);
        DELETE FROM ROLES WHERE IDUSER = (?);
        UPDATE ROLES SET ROLE = (?) WHERE IDUSER = (?);
    * */
    private final String INSERT_ROLE = "INSER INTO ROLES (IDUSER,ROLE) VALUES (?,?)";
    private final String SELECT_ROLE = "SELECT * FROM ROLES WHERE IDUSER = (?)";
    private final String UPDATE_ROLE = "UPDATE ROLES SET ROLE = (?) WHERE IDUSER = (?)";
    private final String DELETE_ROLE = "DELETE FROM ROLES WHERE IDUSER = (?)";

    @Override
    public void init() {

    }

    @Override
    public Role getRoleByUser(User user) {
        return new Role(Roles.ADMIN);
    }

    @Override
    public Role setRoleUser(User user, Role role) {
        return null;
    }

    @Override
    public boolean hasRole(User user) {
        return false;
    }
}
