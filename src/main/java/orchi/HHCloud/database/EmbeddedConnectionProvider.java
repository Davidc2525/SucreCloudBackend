package orchi.HHCloud.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Colmenares
 */
public class EmbeddedConnectionProvider implements ConnectionProvider {

	private static final Logger Log = LoggerFactory.getLogger(EmbeddedConnectionProvider.class);

	private Properties settings;

	private String driver =  !true ? "org.hsqldb.jdbcDriver" : "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = !true ? "jdbc:hsqldb:file:" : "jdbc:derby:";

	private Connection conn;

	public EmbeddedConnectionProvider() {
		Log.debug("Iniciando base de datos empotrada.");
	}

	@Override
	public boolean isPooled() {
		return false;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Log.debug("Octener conexion de base datos empotrada.");
		try {

			if (conn == null) {
				Class.forName(driver).newInstance();
				
				conn = DriverManager.getConnection(protocol + "db/HHCloud;create=false");
			}

		} catch (ClassNotFoundException e) {
			throw new SQLException("EmbeddedConnectionProvider: Unable to find driver: " + e);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conn;
	}

	@Override
	public void start() {

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