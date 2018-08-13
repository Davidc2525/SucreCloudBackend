package orchi.HHCloud.stores.HdfsStore;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Util;
import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.store.arguments.MoveOrCopyArguments;
import orchi.HHCloud.store.response.MoveOrCopyResponse;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;

public class MoveOrCopyOperation {
	private static Logger log = LoggerFactory.getLogger(MoveOrCopyOperation.class);
	private boolean move;
	private Path srcPath;
	private Path dstPath;
	private String root;
	private Path srcpathWithRoot;
	private Path dstpathWithRoot;
	private FileSystem fs;
	private MoveOrCopyArguments args;
	private MoveOrCopyResponse response;

	public MoveOrCopyOperation(MoveOrCopyArguments arg) {
		response = new MoveOrCopyResponse();
		this.args = arg;
		this.move = arg.isMove();
		fs = HdfsManager.getInstance(true).fs;
		srcPath = new Path(args.getSrcPath().toString());
		dstPath = new Path(args.getDstPath().normalize().toString());
		root = args.getUserId();
		srcpathWithRoot = new Path(HdfsManager.newPath(root, srcPath.toString()).toString());
		dstpathWithRoot = new Path(HdfsManager.newPath(root, dstPath.toString()).toString());
		log.debug("nueva operacion de {} ", move ? "mover ruta" : "copiar ruta");
	}

	public MoveOrCopyResponse call() {
		boolean can = false;
		log.debug("	{} {} a {}", move ? "Moviendo" : "Copiando", srcPath.toString(), dstPath.toString());

		try {
			if (HdfsManager.getInstance().fs.exists(srcpathWithRoot)) {
				if (HdfsManager.getInstance().fs.exists(dstpathWithRoot)) {
					response.setStatus("error");
					response.setError("dstpath_is_used");
					response.setMsg("la rruta de destino ya existe " + dstPath.toString());
					log.debug("la rruta de destino ya existe {}", dstPath.toString());
				} else {

					can = FileUtil.copy(fs, srcpathWithRoot, fs, dstpathWithRoot, move, true, fs.getConf());

					if (can) {
						response.setStatus("ok");
						response.setPath(Paths.get(Util.getPathWithoutRootPath(srcpathWithRoot.toString())).normalize()
								.getParent());

						log.debug("	{} exitoso ", move ? "movido" : "copiado");
					} else {
						// (fs, srcpathWithRoot, dstpathWithRoot, move,
						// fs.getConf());
						response.setStatus("error");
						response.setError("dstpath_is_used");
						response.setMsg(String.format("no pudo {} ", move ? "mover" : "copiar"));

						log.debug("no pudo {} ", move ? "mover" : "copiar");
					}

				}

			} else {
				response.setStatus("error");
				response.setError("srcpath_no_found");
				response.setMsg(String.format("la ruta que quiere %s no existe", move ? "mover" : "copiar"));

				log.debug("	falla al {}, '{}' no existe", move ? "mover" : "copiar", srcPath.toString());
			}

		} catch (IOException e) {
			response.setStatus("error");
			response.setError("server_error");
			response.setMsg(e.getMessage());
			e.printStackTrace();
		}
		log.debug("operacion de {} terminada.", move ? "mover ruta" : "copiar rruta");
		return response;
	}

}
