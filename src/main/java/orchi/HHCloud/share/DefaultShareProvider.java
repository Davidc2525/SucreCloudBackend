package orchi.HHCloud.share;

import orchi.HHCloud.Start;
import orchi.HHCloud.database.ConnectionProvider;
import orchi.HHCloud.store.RestrictedNames;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserNotExistException;
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
import java.util.List;

/**
 * Proveedor para compartir rutas por defecto
 *
 * @author david 14 ago. 2018
 */
public class DefaultShareProvider implements ShareProvider {
    private static final String SHAREDS_DESCRIPTOR = "._.SHAREDS._.";
    private static Logger log = LoggerFactory.getLogger(DefaultShareProvider.class);
    private StoreProvider sp;
    private ConnectionProvider db;

    @Override
    public void init() {
        log.info("Iniciando proveedor de compartir.");
        RestrictedNames.registerName(SHAREDS_DESCRIPTOR);
        //sp = Start.getStoreManager().getStoreProvider();
        //Start.getDbConnectionManager().getConnection();
        db = Start.getDbConnectionManager().getConnectionProvider();
    }

    @Override
    public Shared sharedInDirectory(User user, Path path) {
        Shared shared = new Shared();
        try {

            log.debug("Obtener rutas compartidas en directorio {} para {}", path + "", user.getId());
            path = normaizePaht(path);
            if (true /*sp.isDirectory(user, path)*/) {


                Connection con = db.getConnection();
                String SQL = "SELECT * FROM SHARE WHERE PATH like (?) AND OWNERUSER = (?) --FETCH FIRST ROW ONLY";
                /*String sql = ""
                        + "SELECT DISTINCT SHARE.* FROM SHARE LEFT JOIN SHAREDPARENT "
                        + " ON SHARE.PPATH = SHAREDPARENT.PATH "
                        + " WHERE SHAREDPARENT.PATH=(?) AND SHARE.OWNERUSER=(?)";
                        */
                PreparedStatement stm = con.prepareStatement(SQL);
                stm.setString(1, path.toString() + "%");
                stm.setString(2, user.getId());
                ResultSet r = stm.executeQuery();
                while (r.next()) {
                    log.debug("Ruta compartida {} en {}", r.getString("PATH"), path + "");
                    DataUser newUser = new DataUser();
                    newUser.setId(r.getString("OWNERUSER"));
                    Share share = BuildShare.createShare("", newUser, Paths.get(r.getString("PATH")), r.getLong("CREATEAT"), Mode.valueOf(r.getString("MODE")));
                    if (!share.getPath().equals(path)) {
                        shared.addShare(share);
                    }
                }
                con.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shared;
    }

    @Override
    public void deleteShares(User user, List<Path> paths) {

    }

    @Override
    public boolean isShared(User user, Path path) {
        boolean shared = false;
        try {
            path = normaizePaht(path);
            log.debug("Comprobar si {} esta compartida, user {}", path + "", user.getId());
            Connection con = db.getConnection();
            PreparedStatement stm = con.prepareStatement("SELECT * FROM SHARE WHERE PATH=(?) AND OWNERUSER=(?) FETCH FIRST ROW ONLY");
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

    @Override
    public boolean isSharedWith(User ownerUser, User to, Path path) {
        boolean shared = false;
        try {
            path = normaizePaht(path);
            log.debug("Comprobar si {} esta compartida con {}, user {}", path + "",to.getId(), ownerUser.getId());
            Connection con = db.getConnection();
            PreparedStatement stm = con.prepareStatement("SELECT * FROM SHARE_WITH WHERE PATH=(?) AND OWNERUSER=(?) AND SHAREDWITH = (?) FETCH FIRST ROW ONLY");
            stm.setString(1, path.toString());
            stm.setString(2, ownerUser.getId());
            stm.setString(3, to.getId());
            ResultSet r = stm.executeQuery();

            shared = r.next();
            log.debug("La rruta {} {} compartida", path + "", shared ? "esta" : "no esta");
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shared;
    }

    @Override
    public Share getShare(User ownerUser, Path path) throws NotShareException {
        return getShare(ownerUser, path, false);
    }

    /**
     * @param aditionalData si es true, se agregara en el valor debuelto {@link Share} informacion adicional de con
     *                      quien esta compartida dicha rruta (path), ya que lleva mas carga al recuperar esos datos
     */
    @Override
    public Share getShare(User ownerUser, Path path, boolean additionalData) throws NotShareException {

        Share share = new Share();

        if (!isShared(ownerUser, path)) {
            throw new NotShareException(path + "no se encuentra compartida");
        }
        try {
            path = normaizePaht(path);
            log.debug("Obtener informacion de una rruta compartida, path: {}, user: {}", path + "", ownerUser.getId());
            Connection con = db.getConnection();
            PreparedStatement stm = con.prepareStatement("SELECT * FROM SHARE WHERE PATH=(?) AND OWNERUSER=(?) FETCH FIRST ROW ONLY");
            stm.setString(1, path.toString());
            stm.setString(2, ownerUser.getId());

            ResultSet r = stm.executeQuery();

            if (r.next()) {
                share.setMode(Mode.valueOf(r.getString("MODE")));
                share.setOwner(ownerUser);
                share.setPath(path);
                share.setSharedAt(r.getLong("CREATEAT"));
                if (additionalData) {
                    share.setShareWith(getUsersBySharedPath(ownerUser, path));
                }

            } else {
                throw new NotShareException(path + "no se encuentra compartida");
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return share;
    }

    @Override
    public void createShare(User user, Path path) throws ShareException {
        createShare(user, null, path, Mode.P);
    }

    @Override
    public void createShare(User user, Users with, Path path) throws ShareException {
        createShare(user, with, path, Mode.P);
    }

    @Override
    public void createShare(User user, Users with, Path path, Mode mode) throws ShareException {

        if (isShared(user, path)) {
            return;
        }
        Connection con;
        try {
            path = normaizePaht(path);

            log.debug("creando share en {} para usuario {}", path.toString(), user.getId());
            if(with!=null) log.debug("     -| compartida con {}",with.getUsers());

            if (isShared(user, path))
                return;

            con = db.getConnection();
            PreparedStatement stm = con.prepareStatement("INSERT INTO SHARE (PATH, OWNERUSER, PPATH, CREATEAT,MODE) VALUES(?,?,?,?,?)");
            stm.setString(1, path.toString());
            stm.setString(2, user.getId());
            stm.setString(3, path.getParent().toString());
            stm.setBigDecimal(4, new BigDecimal(System.currentTimeMillis()));
            stm.setString(5, mode.name());
            stm.executeUpdate();
            log.debug("Comparticion exitosa de {}", path.toString());


            con.close();

            if (with != null) {
                setSharedWith(user, with, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ShareException(e);
        }

    }

    @Override
    public void setMode(User user, Path path, Mode mode) {
        if (mode == getMode(user, path)) {
            return;
        }

        try {
            path = normaizePaht(path);

            Connection con = db.getConnection();

            PreparedStatement stm = con.prepareStatement("UPDATE SHARE SET MODE=(?) WHERE PATH=(?)  AND OWNERUSER = (?)");
            stm.setString(1, mode.name());
            stm.setString(2, path + "");
            stm.setString(3, user.getId());

            int updates = stm.executeUpdate();

            if (updates <= 0) {
                log.error("No se modifico ningun registro al modificar MODO {} en {} de {}", mode, path, user.getId());
            } else {
                log.debug("Se modifico el MODO {} en {}, de {}", mode, path, user.getId());
            }

            stm.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mode getMode(User user, Path path) {
        Mode mode = Mode.P;
        try {
            path = normaizePaht(path);
            //log.debug("Comprobar si {} esta compartida, user {}", path + "", ownerUser.getId());
            Connection con = db.getConnection();
            PreparedStatement stm = con.prepareStatement("SELECT MODE FROM SHARE WHERE PATH=(?) AND OWNERUSER=(?)");
            stm.setString(1, path.toString());
            stm.setString(2, user.getId());

            ResultSet r = stm.executeQuery();

            if (r.next()) {
                mode = Mode.valueOf(r.getString("MODE"));
            }

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mode;
    }

    @Override
    public void deleteShare(User user, Path path) {
        deleteShare(user, path, false);
    }

    @Override
    public void deleteShare(User user, Path path, boolean recursive) {
        log.debug("Eliminar rruta compartida {} en user {}", path + "", user.getId());

        if (recursive) {
            sharedInDirectory(user, path).getShared().forEach((Share s) -> {
                deleteShare(user, s.getPath());
            });
        }

        try {
            path = normaizePaht(path);

            Connection con = db.getConnection();

            String sqlDeleteShared = "DELETE FROM SHARE WHERE PATH = ? AND OWNERUSER = ?";


            log.debug("La rruta es un directorio {}", path + "");

            PreparedStatement stm = con.prepareStatement(sqlDeleteShared);
            stm.setString(1, path.toString());
            stm.setString(2, user.getId());
            int countChildrens = stm.executeUpdate();
            log.debug("Eliminadas rutas {}", countChildrens);


            deleteSharedWith(user, getUsersBySharedPath(user, path), path);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSharedWith(User ownerUser, User to, Path path) {
        log.debug("Set  share with: {} to: {}, owner {}", path, to.getId(), ownerUser.getId());
        if(ownerUser.equals(to)){
            log.debug("Not can't shared with you");
            return;
        }
        if (isSharedWith(ownerUser, to, path)) {
            return;
        }
        Connection con = null;
        try {

            path = normaizePaht(path);

            String SQL = "INSERT INTO SHARE_WITH (PATH, OWNERUSER, SHAREDWITH, CREATEAT) VALUES (?,?,?,?)";

            con = db.getConnection();
            PreparedStatement stm = con.prepareStatement(SQL);
            stm.setString(1, path + "");
            stm.setString(2, ownerUser.getId());
            stm.setString(3, to.getId());
            stm.setBigDecimal(4, new BigDecimal(System.currentTimeMillis()));

            stm.executeUpdate();
            con.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setSharedWith(User ownerUser, Users to, Path path) {
        log.debug("Set share with: {} to(s): {}, owner {}", path, to, ownerUser);
        Users ua1 = getUsersBySharedPath(ownerUser, path);

        deleteSharedWith(ownerUser, ua1, path);

        to.getUsers().forEach((User u) -> {
            setSharedWith(ownerUser, u, path);
        });
    }

    @Override
    public Shared getSharedWithMe(User user) {
        Shared shared = new Shared();

        try {
            Connection con = db.getConnection();

            PreparedStatement stm = con.prepareStatement("SELECT * FROM SHARE_WITH WHERE SHAREDWITH = (?)");
            stm.setString(1, user.getId());
            ResultSet r = stm.executeQuery();

            while (r.next()) {
                DataUser newUser = new DataUser();
                newUser.setId(r.getString("OWNERUSER"));

                User uu = Start.getUserManager().getUserProvider().getUserById(newUser.getId());
                uu.setPassword("");
                Share share = BuildShare.createShare("", uu, Paths.get(r.getString("PATH")), r.getLong("CREATEAT"), null);
                shared.addShare(share);
            }
            stm.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shared;
    }

    @Override
    public Users getUsersBySharedPath(User ownerUser, Path path) {

        Users users = new Users();

        try {
            Connection con = db.getConnection();

            PreparedStatement stm = con.prepareStatement("SELECT SHAREDWITH FROM SHARE_WITH WHERE PATH = (?) AND OWNERUSER = (?)");
            stm.setString(1, path + "");
            stm.setString(2, ownerUser.getId());

            ResultSet r = stm.executeQuery();

            while (r.next()) {
                //DataUser u = new DataUser();
                //u.setId(r.getString("SHAREDWITH"));
                try {
                    User uu = Start.getUserManager().getUserProvider().getUserById(r.getString("SHAREDWITH"));
                    uu.setPassword("");
                    users.add(uu);
                } catch (UserNotExistException e) {
                    DataUser u = new DataUser();
                    u.setId(r.getString("SHAREDWITH"));
                    users.add(u);
                    e.printStackTrace();
                } catch (UserException e) {
                    e.printStackTrace();
                }
            }

            stm.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public void deleteSharedWith(User ownerUser, Users withUsers, Path path) {
        log.debug("DELETESHAREDWITH  owner {}, to: {}, path: {}", ownerUser, withUsers, path);
        if (withUsers != null) {
            withUsers.getUsers().forEach((User u) -> {
                deleteSharedWith(ownerUser, u, path);
            });
        }
    }

    @Override
    public void deleteSharedWith(User ownerUser, User withUser, Path path) {
        try {
            Connection con = db.getConnection();

            PreparedStatement stm = con.prepareStatement("DELETE FROM SHARE_WITH WHERE OWNERUSER = (?)  AND PATH = (?) AND SHAREDWITH = (?) ");
            stm.setString(1, ownerUser.getId());
            stm.setString(2, path + "");
            stm.setString(3, withUser.getId());
            int delete = stm.executeUpdate();

            log.debug("DELETESHAREDWITH delete: {}, owner {}, to: {}, path: {}", delete, ownerUser, withUser, path);


            stm.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Path normaizePaht(Path path) {
        if (!path.isAbsolute()) {
            path = Paths.get("/", path.toString()).normalize();
        }
        path = path.normalize();

        return path;
    }
}
