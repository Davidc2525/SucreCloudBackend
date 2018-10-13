package orchi.HHCloud.share;

import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Users;

import java.nio.file.Path;
import java.util.List;

/**
 * Con esta interface se podra crear crea un api, para administrar las carpetas
 * y archivos q un usuario quiere compartir, se hace de modo interfas para poder
 * dar la opcion de cambiar la implementacion,
 *
 * @author Colmenares David
 */
public interface ShareProvider {

    /**
     * metodo llamado luego de crear una instancia de el proveedor, para inciar o preparar
     * cosas que se necesiten dentro de el objecto.
     */
    public void init();

    /**
     * Listar las rutas que se encuentran compartidas dentro de un directorio de un usuario espesifico
     */
    public Shared sharedInDirectory(User user, Path path);

    /**
     * @deprecated
     */
    public void deleteShares(User user, List<Path> paths);

    /**
     * Comprobar si una ruta se encuentra compartida
     */
    public boolean isShared(User user, Path path);

    /**
     * Comprobar si una rruta se encuetra compartida con un usuario espesifico
     */
    public boolean isSharedWith(User ownerUser, User to, Path path);

    /**
     * Compartir una nueva ruta
     *
     * @param user usuario dueño de la rruta a compartir
     * @param path rruta a compartir
     */
    public void createShare(User user, Path path);

    /**
     * Compartir una nueva ruta
     *
     * @param user usuario dueño de la rruta a compartir
     * @param with usuario con quien se compartira la rruta
     * @param path rruta a compartir
     */
    public void createShare(User user, Users with, Path path);

    /**
     * Compartir una nueva ruta
     *
     * @param user usuario dueño de la rruta a compartir
     * @param with usuario con quien se compartira la rruta
     * @param path rruta a compartir
     * @param mode modo en el que se compartira la rruta {@link Mode}
     */
    public void createShare(User user, Users with, Path path, Mode mode);

    /**
     * Configurar un nuevo modo a una rruta espesifica de un usuario
     */
    public void setMode(User user, Path path, Mode mode);

    /**
     * Obtener modo en que una rruta se encuentra compartida
     */
    public Mode getMode(User user, Path path);

    /**
     * Eliminar una rruta compartida, elimina las rrutas hijas
     */
    public void deleteShare(User user, Path path);

    /**
     * A una rruta compartida se puede compartir con un usuario espesifico
     */
    public void setSharedWith(User ownerUser, User to, Path path);


    /**
     * Obtener rrutas compartidas con un usuario espesifico
     */
    public Shared getSharedWithMe(User user);

    /**
     * Obtener usuarios que tienen una rruta comaprtida en comun
     */
    public Users getUsersBySharedPath(User ownerUser, Path path);

    /**
     * Eliminar rruta compartidas a usuarios espesificos
     */
    public void deleteSharedWith(User ownerUser, Users withUsers, Path path);

    /**
     * Eliminar ruta a un usuario espesifico
     */
    public void deleteSharedWith(User ownerUser, User withUser, Path path);
}
