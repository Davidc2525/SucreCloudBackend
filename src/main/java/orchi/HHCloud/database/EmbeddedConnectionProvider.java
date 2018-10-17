package orchi.HHCloud.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author David Colmenares
 */
public class EmbeddedConnectionProvider implements ConnectionProvider {

    private static final Logger Log = LoggerFactory.getLogger(EmbeddedConnectionProvider.class);

    private Properties settings;

    private String driver = !true ? "org.hsqldb.jdbcDriver" : "org.apache.derby.jdbc.EmbeddedDriver";
    private String protocol = !true ? "jdbc:hsqldb:file:" : "jdbc:derby:";
    private Connection conn;
    private MiniConnectionPoolManager poolMgr;

    @Override
    public void start() {
        Log.debug("Iniciando base de datos empotrada.");

        org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource dataSource = new org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource();
        dataSource.setDatabaseName("resources/db/HHCloud");

        // dataSource.setCreateDatabase("create");
        poolMgr = new MiniConnectionPoolManager(dataSource, 2000);
    }

    @Override
    public boolean isPooled() {
        return true;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Log.debug("Octener conexion de base datos empotrada.");
        Connection con = poolMgr.getConnection();
        Log.debug("Connexion {}", con);
        return con;
    }

    @Override
    public void restart() {

        destroy();

        start();
    }

    @Override
    public void destroy() {

        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement("DISCONNECT");
            pstmt.execute();
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        } finally {

        }

        settings = null;
        conn = null;
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        destroy();
    }
}