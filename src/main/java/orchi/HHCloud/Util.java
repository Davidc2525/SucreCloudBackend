package orchi.HHCloud;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

public abstract class Util {
	public static String type(FileStatus i) {
		// TODO Auto-generated method stub
		return i.isFile() ? "file" : "folder";
	}
	/**
	 * con el valor de subset se espesifica q tanto de va a cortar la rruta dependiendo de donde
	 * se guarden los archivos de los usuarios
	 * <br>
	 * Si isLocalFileSystem es true
	 * 	suponiendo q se tiene esta rruta como root '/home/user/HHCloudFsStoreProvider/mi_dfs/'
	 * 	más el id del usuario 'david' quedaria '/home/user/HHCloudFsStoreProvider/mi_dfs/david/'
	 * 	<br>
	 * 	pero el usuario solo tiene q ver desde la ultima rruta hacia delante y no ver la rruta absoluta completa
	 * 	para eso se extrae la porcion desde el incio hasta el id del usuario
	 * 	<pre>
	 *   suponiendo q el usuario quiere acceder a '/home/user/HHCloudFsStoreProvider/mi_dfs/david/mis imagenes/img1.jpg'
	 *   se cortada desde la root de la rruta el id de usuario.
	 *                    Porcion a cortar
	 *    ________________________|_______________________
	 *    |                                               |
	 *    |/home/user/HHCloudFsStoreProvider/mi_dfs/david/| |/mis imagenes/img1.jpg|
	 *                                                      |______________________|
	 *                                                                 |
	 *                                                            porcion q 
	 *                                                           se entregara
	 *                                                            al usuario
	 * 	</pre>
	 * <br>
	 * SI isLocalFileSystem es false
	 * 	asumiendo q la rruta root es 'hdfs://${host}:${port}/mi_dfs/' mas la id de usuario
	 * <br>
	 * teniendo 'hdfs://localhost:9000/mi_dfs/david/mis imagenes/img1.jpg',  se tiene q extraer 
	 * <pre>
	 *               Porcion a cortar
	 *    __________________|_________________
	 *    |                                   |
	 *    |hdfs://localhost:9000/mi_dfs/david/| |/mis imagenes/img1.jpg|
	 *                                          |______________________|
	 *                                                     |
	 *                                                porcion que 
	 *                                                 se entrega
	 *                                                 al usuario
	 * </pre>
	 * 
	 * */
	public static String getPathWithoutRootPath(String in) {
		String contentFiles = Start.conf.getString("store.contextstore.root.path.store.fs.user.files", "files");
		java.nio.file.Path p = null;
		Path p2 = new Path(in);

		p = Paths.get(Path.getPathWithoutSchemeAndAuthority(p2).toString());
	
		String path = "/";
		Boolean add = false;
		Iterator<java.nio.file.Path> iter = p.iterator();
		while (iter.hasNext()) {
			java.nio.file.Path part = iter.next();

			if (add) {
				path += "/" + part.toString();
			}

			if (part.toString().equals(contentFiles)) {
				add = true;
				//iter.next();// to /user/
				//iter.next();// to /user/files/
			}

		}

		return Paths.get("/", path + "").normalize() + "";
	}
	
	public static JSONObject parseParams(HttpServletRequest req){
		ParseParamsMultiPart2 params = null;
		try {
			params = new ParseParamsMultiPart2(req);
		} catch (Exception e1) {
			System.err.println(e1.getMessage());
		}
		String args = null;
		if (req.getMethod().equalsIgnoreCase("post")) {
			try {
				args = params.getString("args");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		} else if (req.getMethod().equalsIgnoreCase("get")) {
			
				if(Base64.isBase64(req.getParameter("args"))){
					args = new String(Base64.decodeBase64(req.getParameter("args")));
				}else{
					args = new String((req.getParameter("args")));
				}
				
		}
		return new JSONObject(args);		
	}
}
