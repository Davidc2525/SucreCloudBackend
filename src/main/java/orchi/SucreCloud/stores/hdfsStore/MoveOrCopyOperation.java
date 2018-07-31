package orchi.SucreCloud.stores.hdfsStore;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.SucreCloud.operations.IOperation;
import orchi.SucreCloud.stores.hdfsStore.HdfsManager;

public class MoveOrCopyOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(RenameOperation.class);
	private JSONObject arg;
	private boolean move;
	private Path srcPath;
	private Path dstPath;
	private String root;
	private Path srcpathWithRoot;
	private Path dstpathWithRoot;
	private FileSystem fs;

	public MoveOrCopyOperation(JSONObject arg, boolean move) {
		this.arg = arg;
		this.move = move;
		fs = HdfsManager.getInstance().fs;
		srcPath = new Path((arg.getString("path")));
		dstPath = new Path(Paths.get(arg.getString("dstPath")).normalize().toString());
		root = arg.getString("root");
		srcpathWithRoot = new Path(HdfsManager.newPath(root, srcPath.toString()).toString());
		dstpathWithRoot = new Path(HdfsManager.newPath(root, dstPath.toString()).toString());
		log.debug("nueva operacion de {} ", move ? "move ruta" : "copiar ruta");
	}

	@Override
	public JSONObject call() {
		boolean can = false;
		log.debug("	{} {} a {}",move?"Moviendo":"Copiando",srcPath.toString(),dstPath.toString());
		JSONObject response = new JSONObject();
		try {
			if(HdfsManager.getInstance().fs.exists(srcpathWithRoot)){
				if(HdfsManager.getInstance().fs.exists(dstpathWithRoot)){
					response.put("args", arg)
					.put("status", "error")
					.put("error", "dstpath_is_used")
					.put("errorMsg","la rruta de destino ya existe "+dstPath.toString());
					log.debug("la rruta de destino ya existe {}",dstPath.toString());
				}else{

					can = FileUtil.copy(fs, srcpathWithRoot, fs, dstpathWithRoot, move, true, fs.getConf());

					if(can){
						response.put("args", arg).put("status", "ok");
						log.debug("	{} exitoso ",move ? "movido":"copiado");
					}else{
						//(fs, srcpathWithRoot, dstpathWithRoot, move, fs.getConf());
						response.put("args", arg).put("status", "error")
						.put("error", String.format("no pudo {} ", move?"mover":"copiar"));
						log.debug("no pudo {} ", move?"mover":"copiar");
					}

				}

			}else{
				response
				.put("status","error")
				.put("error", "srcpath_no_found")
				.put("errorMsg", String.format("la ruta que quiere %s no existe",move?"mover":"copiar"));

				log.debug("	falla al {}, '{}' no existe",move?"mover":"copiar",srcPath.toString());
			}

		} catch (IOException e) {
			response
			.put("status", "error")
			.put("error", "server_error")
			.put("errorMsg", e.getMessage());
			e.printStackTrace();
		}
		log.debug("operacion de {} terminada.",move ? "mover ruta":"copiar rruta");
		return response;
	}

}
