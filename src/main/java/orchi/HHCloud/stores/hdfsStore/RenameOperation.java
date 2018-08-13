package orchi.HHCloud.stores.hdfsStore;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.*;

import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;

public class RenameOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(RenameOperation.class);
	private JSONObject arg;
	private Path srcPath;
	private Path dstPath;
	private String root;
	
	private Path srcpathWithRoot;
	private Path dstpathWithRoot;

	public  RenameOperation(JSONObject arg){
		this.arg = arg;
	
		srcPath = new Path((arg.getString("path")));
		dstPath = new Path(Paths.get(arg.getString("dstPath")).normalize().toString());
		root = arg.getString("root");
		srcpathWithRoot = new Path(HdfsManager.newPath(root, srcPath.toString()).toString());
		dstpathWithRoot = new Path(HdfsManager.newPath(root, dstPath.toString()).toString());
		log.debug("nueva operacio de renombrado");

	}

	@Override
	public JSONObject call() {
		log.debug("	renombrando {} a {}",srcPath.toString(),dstPath.toString());
		JSONObject response = new JSONObject();
		try {
			if(HdfsManager.getInstance().fs.exists(srcpathWithRoot)){
				if(HdfsManager.getInstance().fs.exists(dstpathWithRoot)){
					response.put("args", arg)
					.put("status", "error")
					.put("error", "dstpath_is_used")
					.put("errorMsg","la rruta de destino ya existe "+dstPath.toString());
					log.debug("la rruta de destino ya existe  {}",dstPath.toString());
				}else{
					HdfsManager.getInstance().fs.rename(srcpathWithRoot, dstpathWithRoot);
					response.put("args", arg).put("status", "ok");
					log.debug("	renombrado exitoso ");
				}
				
			}else{
				response
				.put("status","error")
				.put("error", "srcpath_no_found")
				.put("errorMsg", "la ruta a la que quiere renombrar no existe");
				log.debug("	falla al renombrar, '{}' no existe",srcPath.toString());
			}
			
		} catch (IOException e) {
			response
			.put("status", "error")
			.put("error", "server_error")
			.put("errorMsg", e.getMessage());
			e.printStackTrace();
		}
		log.debug("operacion de renombrado terminada.");
		return response;
	}

}
