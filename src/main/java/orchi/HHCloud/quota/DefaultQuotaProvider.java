package orchi.HHCloud.quota;

import orchi.HHCloud.Start;
import orchi.HHCloud.database.DbConnectionManager;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.store.StoreManager;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultQuotaProvider extends ProxyQuotaProvider {
    private static final Logger log = LoggerFactory.getLogger(DefaultQuotaProvider.class);
    private final String INSERT_QUOTA = "INSERT INTO SPACE_QUOTA VALUES (?,?)";
    private final String DELETE_QUOTA = "DELETE FROM SPACE_QUOTA WHERE IDUSER = (?)";
    private final String GET_QUOTA_USER = "SELECT * FROM SPACE_QUOTA WHERE IDUSER = (?)";
    private final String UPDATE_QUOTA_USER = "UPDATE SPACE_QUOTA SET SIZE = (?) WHERE IDUSER = (?)";
    private StoreProvider sp;
    private DbConnectionManager db;

    public void init() {
        super.init();
        db = Start.getDbConnectionManager();
        log.info("Iniciando QuotaProvider");
    }

    @Override
    public Quota setQuota(User user, Path path, long size) throws QuotaException {
        log.debug("setQuota {} {} {}", user, path, size);
        super.setQuota(user, path, size);
        Quota q = new Quota(size);
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = db.getConnection();

            if (quotaIsSet(user)) {
                log.debug("updateQuota {} {} {}", user, path, size);
                stm = conn.prepareStatement(UPDATE_QUOTA_USER);
                stm.setLong(1, q.getQuota());
                stm.setString(2, user.getId());
                stm.executeUpdate();
            } else {
                log.debug("insertQuota {} {} {}", user, path, size);
                stm = conn.prepareStatement(INSERT_QUOTA);
                stm.setString(1, user.getId());
                stm.setLong(2, q.getQuota());
                stm.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new QuotaException(e);
        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
                //throw new QuotaException(e);
            }
        }
        return q;
    }

    @Override
    public void removeQuota(User user, Path path) throws QuotaException {
        log.debug("removeQuota {} {} ", user, path);
        super.removeQuota(user, path);
        PreparedStatement stm = null;
        Connection conn = null;
        try {
            conn = db.getConnection();
            stm = conn.prepareStatement(DELETE_QUOTA);
            stm.setString(1, user.getId());
            stm.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new QuotaException(e);
        } finally {
            try {
                stm.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean quotaIsSet(User user) throws QuotaException {
        boolean qset = false;
        log.debug("quotaIsSet {}", user);
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = db.getConnection();

            stm = conn.prepareStatement(GET_QUOTA_USER);
            stm.setString(1, user.getId());

            result = stm.executeQuery();

            if (result.next()) {
                qset = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new QuotaException(e);
        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {
                throw new QuotaException(e);
            }
        }

        return qset;
    }

    @Override
    public Quota getQuota(User user) throws QuotaException {
        log.debug("getQuota {}", user);
        super.getQuota(user);
        Quota q = new Quota();
        ResultSet result;
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = db.getConnection();

            stm = conn.prepareStatement(GET_QUOTA_USER);
            stm.setString(1, user.getId());

            result = stm.executeQuery();

            if (result.next()) {
                long size = result.getLong("size");
                q.setQuota(size);
            } else {
                q.setQuota(StoreManager.SPACE_QUOTA_SIZE_NO_VERIFIED_USER);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new QuotaException(e);
        } finally {
            try {
                stm.close();
                conn.close();
            } catch (Exception e) {
                throw new QuotaException(e);
            }
        }

        return q;
    }
}
