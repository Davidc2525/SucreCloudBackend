package orchi.HHCloud;

import orchi.HHCloud.AdminService.ServiceImpl;
import orchi.HHCloud.mail.Exceptions.SendEmailException;
import orchi.HHCloud.mail.MailProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.RoledUser;
import orchi.HHCloud.user.role.Role;
import orchi.HHCloud.user.role.Roles;

import java.io.IOException;

public class Prepare {
    public static void main(String[] args) throws IOException, UserException {
        /**preparar la base de datos embebida*/
        String[] params = {"./src/main/resources/schema.sql"};
        org.apache.derby.tools.ij.main(params);

        DataUser ua = new DataUser();
        ua.setId(Long.toString(System.currentTimeMillis()));
        ua.setEmail(Start.conf.getString("admin.security.admin.email"));
        ua.setUsername(Start.conf.getString("admin.security.admin.username"));
        ua.setEmailVerified(true);
        ua.setFirstName(Start.conf.getString("admin.security.admin.firstName"));
        ua.setLastName(Start.conf.getString("admin.security.admin.lastName"));
        ua.setPassword(Start.conf.getString("admin.security.admin.pass"));

        ServiceImpl s = new ServiceImpl();
        s.createUser(ua);
        RoledUser.byUser(ua).setRole(new Role(Roles.ADMIN));
        System.out.println("------------------------------------------");
        System.out.println("             ADMIN USER                  ");
        System.out.println();
        System.out.println(String.format("ID %s",ua.getId()));
        System.out.println(String.format("EMAIL %s",ua.getEmail()));
        System.out.println(String.format("USERNAME %s",ua.getUsername()));
        System.out.println();
        System.out.println("------------------------------------------");


        /**preparar el api de gmail*/
        MailProvider mp = Start.getMailManager().getProvider();
        try {
            String admin = Start.conf.getString("mail.mailmanager.mail.admin");
            mp.sendEmail(admin, admin, "activate service", "active service.");
        } catch (SendEmailException e1) {
            e1.printStackTrace();
        }

        System.exit(0);
    }
}
