package orchi.HHCloud.share;

import orchi.HHCloud.user.User;

import java.nio.file.Path;
import java.util.List;

/**
 * Con esta interface se podra crear crea un api, para administrar las carpetas
 * y archivos q un usuario quiere compartir, se hace de modo interfas para poder
 * dar la opcion de cambiar el modo en q se almacenan las rrutas conpartidas,
 * pudiendo usar otro tipo de almacenamiento (base de datos) como administrador
 * de las mismas.
 *
 * @author Colmenares David
 */
public interface ShareProvider {

    public void init();

    public Shared sharedInDirectory(User user, Path path);

    public void deleteShares(User user, List<Path> paths);

    public boolean isShared(User user, Path path);

    public void createShare(User user, Path path);

    public void deleteShare(User user, Path path);
}
