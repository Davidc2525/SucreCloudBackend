/**
 * OldDefaultShareProvider.java
 */
package orchi.HHCloud.share;

import orchi.HHCloud.Start;
import orchi.HHCloud.database.ConnectionProvider;
import orchi.HHCloud.store.RestrictedNames;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;




/**
 * @author david 14 ago. 2018
 *
 */
public class OldDefaultShareProvider {
    private static final String SHAREDS_DESCRIPTOR = "._.SHAREDS._.";
    private static Logger log = LoggerFactory.getLogger(OldDefaultShareProvider.class);
    private StoreProvider sp;
    private ConnectionProvider db;


    public void init() {
        log.info("Iniciando proveedor de compartir.");
        RestrictedNames.registerName(SHAREDS_DESCRIPTOR);
        sp = Start.getStoreManager().getStoreProvider();
        //Start.getDbConnectionManager().getConnection();
        db = Start.getDbConnectionManager().getConnectionProvider();
    }

    /**
     * por hacer
     */
    public void getSharedDescriptor(User user, Path path) {
        try {
            String pathwr = path.toString();
            if (sp.exists(user, Paths.get(pathwr, SHAREDS_DESCRIPTOR))) {

            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void createShareParent(User user, Path path) {
        Connection con;
        try {// SHAREDPARENT
            path = normaizePaht(path);
            log.debug("Creando shared parent en user {}, {}", user.getId(), path.toString());

            con = db.getConnection();
            PreparedStatement stmSP = con.prepareStatement("INSERT INTO SHAREDPARENT VALUES(?,?,?)");

            stmSP.setString(1, path.getParent().toString());
            stmSP.setString(2, path.toString());
            stmSP.setString(3, user.getId());
            stmSP.executeUpdate();

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void createShare(User user, Path path) {
        createShare(user,null,path);
    }


    public void createShare(User user, Users with, Path path) {
        Connection con;
        try {// SHAREDPARENT
            path = normaizePaht(path);

            log.debug("creando share en {} para usuario {}", path.toString(), user.getId());

            if (isShared(user, path))
                return;

            //createShareParent(user, path);
            con = db.getConnection();
            PreparedStatement stm = con.prepareStatement("INSERT INTO SHARE (PATH, OWNERUSER, PPATH, CREATEAT) VALUES(?,?,?,?)");
            stm.setString(1, path.toString());
            stm.setString(2, user.getId());
            stm.setString(3, path.getParent().toString());
            stm.setBigDecimal(4, new BigDecimal(System.currentTimeMillis()));
            stm.executeUpdate();
            log.debug("Comparticion exitosa de {}", path.toString());
            Path parent = path.getParent();
            if (parent.getParent() != null) {
                //createShare(user,parent);
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void createShare(User user, Users with, Path path, Mode mode) {

    }


    public void setMode(User user, Path path, Mode mode) {

    }


    public Mode getMode(User user, Path path) {
        return null;
    }


    public void deleteShare(User user, Path path) {
        log.debug("Eliminar rruta compartida {} en user {}", path + "", user.getId());

        try {
            path = normaizePaht(path);

            Connection con = db.getConnection();
            String sqlDeleteInSharedParent = "" //SI ES DIRECTORIO
                    + "DELETE FROM SHAREDPARENT WHERE PATH IN( "
                    + "	SELECT PATH FROM SHAREDPARENT "
                    + "		WHERE PATH LIKE ? AND OWNERUSER=? "//like %
                    + ")";
            String sqlDeleteInShared = "" // SI ES DIRECTORIO
                    + "DELETE FROM SHARE WHERE PATH IN( "
                    + "	SELECT PATH FROM SHARE "
                    + "		WHERE PPATH LIKE ? AND OWNERUSER=? "//like %
                    + ")";

            String sqlDeleteSharedParent = "DELETE FROM SHAREDPARENT WHERE PATH = ? AND CSPATH = ? AND OWNERUSER=?";

            String sqlDeleteShared = "DELETE FROM SHARE WHERE PATH = ? AND PPATH = ? AND OWNERUSER=?";

            if (sp.isDirectory(user, path)) {
                log.debug("La rruta es un directorio {}", path + "");
                log.debug("Eliminando todos las rrutas padre q coinsidan con {} de user {}", path + "%", user.getId());

                //PreparedStatement stmParent = con.prepareStatement(sqlDeleteInSharedParent);
                //stmParent.setString(1, path.toString() + "%");
                //stmParent.setString(2, user.getId());
                //int countParents = stmParent.executeUpdate();
                //log.debug("Eliminadas rutas padre {}", countParents);

                log.debug("Eliminando todos las rrutas hijas (en share) q coinsidan con {} de user {}", path + "%", user.getId());
                PreparedStatement stm = con.prepareStatement(sqlDeleteInShared);
                stm.setString(1, path.toString() + "%");
                stm.setString(2, user.getId());
                int countChildrens = stm.executeUpdate();
                log.debug("Eliminadas rrutas hijas", countChildrens);

            } else if (sp.isFile(user, path)) {

                log.debug("La rruta es un archivo {}", path + "");
                log.debug("Eliminando la rruta padre q coinsidan con {}", path + "");
                PreparedStatement stmParent = con.prepareStatement(sqlDeleteSharedParent);
                stmParent.setString(1, path.getParent().toString());
                stmParent.setString(2, path.toString());
                stmParent.setString(3, user.getId());
                int countParents = stmParent.executeUpdate();
                log.debug("Eliminando la rruta padre q coinsidan con {}, {}", path + "", countParents);

                log.debug("Eliminando la rruta hija q coinsidan con {}", path + "");
                PreparedStatement stm = con.prepareStatement(sqlDeleteShared);
                stm.setString(1, path.toString());
                stm.setString(2, path.getParent().toString());
                stm.setString(3, user.getId());
                int countChildrens = stm.executeUpdate();
                log.debug("Eliminando la rruta padre q coinsidan con {}, {}", path + "", countChildrens);
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getSharedWithMe(User user) {

    }


    public void getUsersBySharedPath(User ownerUser, Path path) {

    }


    public void deleteSharedWith(User ownerUser, Users withUsers, Path path) {

    }


    public void deleteSharedWith(User ownerUser, User withUser, Path path) {

    }


    public Shared sharedInDirectory(User user, Path path) {

        Shared shared = new Shared();
        try {

            log.debug("Obtener rutas compartidas en directorio {} para {}", path + "", user.getId());
            path = normaizePaht(path);
            if (sp.isDirectory(user, path)) {

                log.debug("La rruta es un directorio {}", path + "");
                //System.out.println(path);

                Connection con = db.getConnection();
                String sql = ""
                        + "SELECT DISTINCT SHARE.* FROM SHARE LEFT JOIN SHAREDPARENT "
                        + " ON SHARE.PPATH = SHAREDPARENT.PATH "
                        + " WHERE SHAREDPARENT.PATH=(?) AND SHARE.OWNERUSER=(?)";
                PreparedStatement stm = con.prepareStatement(sql);
                stm.setString(1, path.toString());
                stm.setString(2, user.getId());
                ResultSet r = stm.executeQuery();
                while (r.next()) {
                    log.debug("Ruta compartida {} en {}", r.getString("PATH"), path + "");
                    DataUser newUser = new DataUser();
                    newUser.setId(r.getString("OWNERUSER"));
                    Share share = BuildShare.createShare("", newUser, Paths.get(r.getString("PATH")), r.getLong("CREATEAT"),Mode.P);
                    shared.addShare(share);
                }
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shared;
    }


    public boolean isShared(User user, Path path) {

        boolean shared = false;
        try {
            path = path.normalize();
            log.debug("Comprobar si {} esta compartida, user {}", path + "", user.getId());
            Connection con = db.getConnection();
            PreparedStatement stm = con.prepareStatement("SELECT * FROM SHARE WHERE PATH=(?) AND OWNERUSER=(?) ");
            stm.setString(1, path.toString());
            stm.setString(2, user.getId());
            ResultSet r = stm.executeQuery();

            shared = r.next();
            log.debug("La rruta {} {} compartida", path + "", shared ? "esta" : "no esta");
            con.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return shared;
    }


    public void deleteShares(User user, List<Path> paths) {
    }


    private Path normaizePaht(Path path) {
        if (!path.isAbsolute()) {
            path = Paths.get("/", path.toString()).normalize();
        }
        path = path.normalize();

        return path;
    }
}
