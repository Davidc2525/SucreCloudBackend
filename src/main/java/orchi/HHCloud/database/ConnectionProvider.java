package orchi.HHCloud.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * con esta interfase se da la posivilidad de poder implementar distitos administradores de base de datos (JDBC), por defecto se implementa con una base de datos empotrada para un rapido montaje.
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