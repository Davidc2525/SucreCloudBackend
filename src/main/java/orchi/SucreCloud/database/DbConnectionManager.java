package orchi.SucreCloud.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.MissingResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConnectionManager {

	private static final Logger Log = LoggerFactory.getLogger(DbConnectionManager.class);
	private static final Object providerLock = new Object();
	private static String defaultProvider = "orchi.SucreCloud.database.EmbeddedConnectionProvider";
	private static ConnectionProvider connectionProvider;
	private static DbConnectionManager instance;

	public DbConnectionManager() {
		this(defaultProvider);
	}

	public DbConnectionManager(String nameProvider) {
		Log.debug("Iniciando administrador de base de datos con proveedor: {}", nameProvider);
		try {
			connectionProvider = (ConnectionProvider) Class.forName(nameProvider).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			Log.debug("Error al instanciar al proveedor: {}", nameProvider);
			e.printStackTrace();
		}
	}

	public DbConnectionManager(Class<? extends ConnectionProvider> classProvider) {
		Log.debug("Iniciando administrador de base de datos con proveedor: {}", classProvider.getName());
		try {
			connectionProvider = (ConnectionProvider) classProvider.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			Log.debug("Error al instanciar al proveedor: {}", classProvider.getName());
			e.printStackTrace();
		}
	}

	public ConnectionProvider getConnectionProvider() {
		Log.debug("obtener proveedor: {}", connectionProvider.getClass().getName());
		return connectionProvider;
	}

	public Connection getConnection() throws SQLException {
		Log.debug("Obtener conexion de proveedor: {}", connectionProvider.getClass().getName());
		Integer currentRetryCount = 0;
		Integer maxRetries = 10;
		Integer retryWait = 250; // milliseconds
		SQLException lastException = null;
		do {
			try {
				Connection con = connectionProvider.getConnection();
				if (con != null) {

					return con;
				}
			} catch (SQLException e) {

				lastException = e;
				Log.info("Unable to get a connection from the database pool " + "(attempt " + currentRetryCount
						+ " out of " + maxRetries + ").", e);
			}

			try {
				Thread.sleep(retryWait);
			} catch (Exception e) {

			}
			currentRetryCount++;
		} while (currentRetryCount <= maxRetries);

		throw new SQLException(
				"ConnectionManager.getConnection() " + "failed to obtain a connection after " + currentRetryCount
						+ " retries. " + "The exception from the last attempt is as follows: " + lastException);
	}

	public void setConnectionProvider(ConnectionProvider provider) {
		synchronized (providerLock) {
			if (connectionProvider != null) {
				connectionProvider.destroy();
				connectionProvider = null;
			}
			connectionProvider = provider;
			connectionProvider.start();
			Connection con = null;
			try {
				con = connectionProvider.getConnection();

			} catch (MissingResourceException mre) {
				Log.error(mre.getMessage());
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			} finally {
				closeConnection(con);
			}
		}

	}

	public void closeConnection(Connection con) {
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				Log.error(e.getMessage(), e);
			}
		}
	}

	public void destroyConnectionProvider() {
		synchronized (providerLock) {
			if (connectionProvider != null) {
				connectionProvider.destroy();
				connectionProvider = null;
			}
		}
	}

	public static DbConnectionManager getInstance() {
		if (instance == null) {
			instance = new DbConnectionManager();
		}
		return instance;
	}

}
