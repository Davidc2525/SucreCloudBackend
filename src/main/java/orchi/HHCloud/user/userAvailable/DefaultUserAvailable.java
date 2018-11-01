package orchi.HHCloud.user.userAvailable;

import orchi.HHCloud.Start;
import orchi.HHCloud.cache.Cache;
import orchi.HHCloud.cache.CacheAleardyExistException;
import orchi.HHCloud.cache.CacheFactory;
import orchi.HHCloud.database.ConnectionProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.userAvailable.Exceptions.DisablingException;
import orchi.HHCloud.user.userAvailable.Exceptions.EnablingException;
import orchi.HHCloud.user.userAvailable.Exceptions.UserUnAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class DefaultUserAvailable implements UserAvailableProvider {
    private static Logger log = LoggerFactory.getLogger(DefaultUserAvailable.class);
    private final String INSERT_UA = "INSERT INTO USER_AVAILABLE (IDUSER,REASON,CREATEDAT) VALUES (?,?,?)";
    private final String DELETE_UA = "DELETE FROM USER_AVAILABLE WHERE IDUSER = (?)";
    private final String SELECT_UA = "SELECT * FROM USER_AVAILABLE WHERE IDUSER = (?)";
    private final String LOAD_UA   = "SELECT * FROM USER_AVAILABLE";
    private final String UPDATE_UA = "UPDATE USER_AVAILABLE SET REASON = (?) WHERE IDUSER = (?)";
    private ConnectionProvider dbP;

    private Cache<String,AvailableDescriptor> cache;

    @Override
    public void init() {
        log.info("INICIANDO PROVEEDOR DE DISPONIBILIDAD DE USUARIO: LAZY({})",lazy);
        dbP = Start.getDbConnectionManager().getConnectionProvider();
        try {
            cache = CacheFactory.createLRUCache("USER_AVAILABLE");
        } catch (CacheAleardyExistException e) {
            e.printStackTrace();

            System.exit(1);
        }
        if(!lazy){
            try {
                loadAllAvailable();
            } catch (UserUnAvailableException e) {
                e.printStackTrace();
            }
        }

    }

    private void loadAllAvailable() throws UserUnAvailableException {
        log.debug("LOADALLAVAILABLE");
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = dbP.getConnection();

            stm = conn.prepareStatement(LOAD_UA);
            result = stm.executeQuery();

            while(result.next()){
                load(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new UserUnAvailableException(e);

        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void disableUser(User user, String reason) throws DisablingException {
        log.debug("DISABLEUSER {}", user.getId());
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            long createdAt = System.currentTimeMillis();
            DataUser u = new DataUser();
            u.setId(user.getId());

            conn = dbP.getConnection();
            if(userIsEnable(user)){
                stm = conn.prepareStatement(INSERT_UA);
                stm.setString(1, (user.getId()));
                stm.setString(2, reason);
                stm.setLong(3, createdAt);
                int updates = stm.executeUpdate();

                AvailableDescriptor avd = new AvailableDescriptor(u, reason, false, createdAt);
                cache.put(u.getId(),avd);

            }else{
                stm = conn.prepareStatement(UPDATE_UA);
                stm.setString(1, reason);
                stm.setString(2, (user.getId()));
                //stm.setLong(3, createdAt);
                int updates = stm.executeUpdate();

                AvailableDescriptor avd = cache.get(u.getId());
                avd.setAvailable(false).setReason(reason);
                cache.put(u.getId(),avd);
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new DisablingException(e);

        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void enableUser(User user) throws EnablingException {
        log.debug("ENABLEUSER {}", user.getId());

        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = dbP.getConnection();

            stm = conn.prepareStatement(DELETE_UA);
            stm.setString(1, (user.getId()));
            int updates = stm.executeUpdate();

            AvailableDescriptor avd = cache.get(user.getId());
            avd.setAvailable(true);
            cache.put(user.getId(),avd);

        } catch (Exception e) {
            e.printStackTrace();
            throw new EnablingException(e);

        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean userIsEnable(User user) throws UserException {
        boolean isEnable = true;
        log.debug("USERISENABLE {}", user.getId());
        AvailableDescriptor inCache = cache.get(user.getId());
        if(inCache!=null){

            log.debug("- DESCRIPTOR IN CACHE {}",inCache);
            return inCache.isAvailable();
        }
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = dbP.getConnection();

            stm = conn.prepareStatement(SELECT_UA);
            stm.setString(1, (user.getId()));
            result = stm.executeQuery();

            if(result.next()){
                isEnable = false;
                AvailableDescriptor avd = load(result);
                log.debug("- DESCRIPTOR IN DB {}",avd);
            }else{
                isEnable = true;
                DataUser u = new DataUser();
                u.setId(user.getId());
                AvailableDescriptor avd = new AvailableDescriptor(u, "", true, 0L);
                cache.put(user.getId(),avd);
                log.debug("LOADED {}",avd);
                log.debug("- DESCRIPTOR IN DB {}",avd);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new UserUnAvailableException(e);

        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        return isEnable;
    }

    @Override
    public AvailableDescriptor getDescriptor(User user) throws UserException {
        userIsEnable(user);
        return cache.get(user.getId());
    }


    private AvailableDescriptor load(ResultSet result) throws Exception {
        String IDUSER = result.getString("IDUSER");
        String REASON = result.getString("REASON");
        long CREATEDAT = result.getLong("CREATEDAT");
        DataUser u = new DataUser();
        u.setId(IDUSER);
        AvailableDescriptor avd = new AvailableDescriptor(u, REASON, false, CREATEDAT);
        cache.put(IDUSER,avd);
        log.debug("LOADED {}",avd);
        return avd;
    }
}
