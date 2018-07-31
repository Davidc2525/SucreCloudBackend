package orchi.SucreCloud.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 *
 * @author Colmenares David
 */
public interface ConnectionProvider {

	
	public boolean isPooled();

	
	public Connection getConnection() throws SQLException;

	
	public void start();

	
	public void restart();

	
	public void destroy();
}