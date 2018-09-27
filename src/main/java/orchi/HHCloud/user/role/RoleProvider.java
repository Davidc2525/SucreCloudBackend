package orchi.HHCloud.user.role;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.User;

public interface RoleProvider {
    static boolean lazy = Start.conf.getBoolean("user.usermanager.provider.user.role.lazy");

    public void init();

    public Role getRoleByUser(User user);

    public Role setRoleUser(User user,Role role);
}
