package orchi.HHCloud.HHCloudAdmin;

import orchi.HHCloud.HHCloudAdmin.model.Person;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;

public class Util {

    public static Person userToPerson(User user){
        Person p = null;
        DataUser u = (DataUser) user;
        p = new Person();
        p.setId(u.getId());
        p.setEmail(u.getEmail());
        p.setCreateAt(u.getCreateAt());
        p.setUsername(u.getUsername());
        p.setFirstName(u.getFirstName());
        p.setLastName(u.getLastName());
        p.setIsVerified(u.isEmailVerified());
        p.setPassword(u.getPassword());
        p.setGender(u.getGender());
        return p;
    }

    public static User personToUser(Person person){
        DataUser u = null;
        u = new DataUser();
        u.setId(person.getId());
        u.setEmail(person.getEmail());
        u.setCreateAt(person.getCreateAt());
        u.setUsername(person.getUsername());
        u.setFirstName(person.getFirstName());
        u.setLastName(person.getLastName());
        u.setEmailVerified(person.isIsVerified());
        u.setPassword(person.getPassword());
        u.setGender(person.getGender());
        return u;
    }
}
