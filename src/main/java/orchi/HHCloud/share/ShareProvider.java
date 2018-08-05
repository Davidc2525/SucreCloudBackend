package orchi.HHCloud.share;

import java.nio.file.Path;

/**
 * Con esta interface se podra crear crea un api, para administrar las carpetas y archivos q un usuario 
 * quiere compartir, se hace de modo interfas para poder dar la opcion de cambiar el modo en q se almacenan
 * las rrutas conpartidas, pudiendo usar otro tipo de almacenamiento (base de datos) como administrador de las mismas.
 * 
 * @author Colmenares David
 */
public interface ShareProvider {
	public void createShare(Path path);
}
