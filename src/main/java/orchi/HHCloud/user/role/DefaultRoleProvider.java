package orchi.HHCloud.user.role;

import orchi.HHCloud.Start;
import orchi.HHCloud.database.ConnectionProvider;
import orchi.HHCloud.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class DefaultRoleProvider implements RoleProvider {
    private static Logger log = LoggerFactory.getLogger(DefaultRoleProvider.class);
    private final String INSERT_ROLE = "INSERT INTO ROLES (IDUSER,ROLE) VALUES (?,?)";
    private final String SELECT_ROLE = "SELECT * FROM ROLES WHERE IDUSER = (?)";
    private final String UPDATE_ROLE = "UPDATE ROLES SET ROLE = (?) WHERE IDUSER = (?)";
    private final String DELETE_ROLE = "DELETE FROM ROLES WHERE IDUSER = (?)";
    private ConnectionProvider dbP;
    /**
     * Map idUser -> Role
     */
    private HashMap<String, Role> roleMap = new HashMap<>();

    @Override
    public void init() {
        log.info("INICIANDO PROVEEDOR DE ROLES DE USUARIO: LAZY({})", lazy);
        dbP = Start.getDbConnectionManager().getConnectionProvider();
    }

    @Override
    public Role getRoleByUser(User user) {
        Role rr = null;
        log.debug("GETROLEBYUSER {}", user.getId());
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;

        if (roleMap.containsKey(user.getId())) {
            Role r = roleMap.get(user.getId());
            log.debug("ROLE IN CACHE: {} {}", user, r);
            return r;
        }

        try {
            conn = dbP.getConnection();

            stm = conn.prepareStatement(SELECT_ROLE);
            stm.setString(1, (user.getId()));
            result = stm.executeQuery();

            if (result.next()) {
                String idUser = result.getString("IDUSER");
                String roleUser = result.getString("ROLE");
                Role role = new Role(Roles.of(roleUser));
                rr = role;
                roleMap.put(user.getId(), role);
                log.debug("ROLE IN DB: {} {}", user, role);
            } else {
                rr = new Role(Roles.USER);
                roleMap.put(user.getId(), rr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        return rr;
    }

    @Override
    public Role setRoleUser(User user, Role role) {
        Role rr = role;
        log.debug("SETROLEUSER {} ROLE {}", user.getId(), role);
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;

        try {

            conn = dbP.getConnection();

            stm = conn.prepareStatement(UPDATE_ROLE);
            stm.setString(1, role.getRole().toString());
            stm.setString(2, (user.getId()));
            int rm = stm.executeUpdate();

            if (rm < 1) {
                PreparedStatement stmInsert = conn.prepareStatement(INSERT_ROLE);
                stmInsert.setString(1, user.getId());
                stmInsert.setString(2, role.getRole().toString());

                stmInsert.executeUpdate();
            }

            roleMap.put(user.getId(), role);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        return rr;
    }
}
