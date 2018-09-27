package orchi.HHCloud.user.role;

import orchi.HHCloud.user.User;

public interface RoleProvider {

    public void init();

    public Role getRoleByUser(User user);

    public Role setRoleUser(User user,Role role);

    public boolean hasRole(User user);
}
