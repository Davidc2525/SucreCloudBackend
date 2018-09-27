package orchi.HHCloud.user;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.role.Role;
import orchi.HHCloud.user.role.RoleProvider;

public class RoledUser extends DataUser{

    public Role getRole(){
        Role role = Start.getUserManager().getUserRoleProvider().getRoleByUser(this);
        return role;
    }

    public Role setRole(Role role){
        Role nRole = Start.getUserManager().getUserRoleProvider().setRoleUser(this,role);
        return nRole;
    }

    public static RoledUser byUser(DataUser user){
        RoledUser ru = new RoledUser();
        ru.setId(user.getId());
        ru.setUsername(user.getUsername());
        ru.setEmailVerified(user.isEmailVerified());
        ru.setEmail(user.getEmail());
        ru.setFirstName(user.getFirstName());
        ru.setLastName(user.getLastName());
        ru.setGender(user.getGender());
        ru.setCreateAt(user.getCreateAt());
        ru.setPassword(user.getPassword());
        return ru;
    }

}
