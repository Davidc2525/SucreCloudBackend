package orchi.SucreCloud.stores.hdfsStore;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.SucreCloud.operations.IOperation;
import orchi.SucreCloud.stores.hdfsStore.HdfsManager;

public class CreateDirectoryOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(CreateDirectoryOperation.class);
	private JSONObject arg;
	private Path path;
	private String root;
	private Path pathWithRoot;
	private FileSystem fs;
	private String newDirectory;

	public CreateDirectoryOperation(JSONObject arg) {
		this.arg = arg;
		fs = HdfsManager.getInstance().fs;
		path = new Path(Paths.get(arg.getString("path")).normalize().toString());
		// newDirectory = arg.getString("newDirectory");
		root = arg.getString("root");
		pathWithRoot = new Path(HdfsManager.newPath(root, path.toString()).toString());

		log.debug("nueva operacio de crear directorio");
	}

	@Override
	public JSONObject call() {
		JSONObject response = new JSONObject();
		try {
			if (fs.exists(pathWithRoot)) {
				response.put("status", "error").put("error", "directry_is_used").put("errorMsg",
						"la rruta ya existe " + path.toString());
				log.debug("	la rruta ya existe {}", path.toString());
			} else {
				fs.mkdirs(pathWithRoot);
				response.put("status", "ok").put("path", path.toString());
				log.debug("	nuevo directorio creado {} {}", path.toString(), pathWithRoot.toString());
			}
		} catch (IOException e) {
			response.put("status", "error").put("error", "server_error").put("errorMsg", e.getMessage());
			e.printStackTrace();
		}
		log.debug("operacion de crear directorio terminada.");
		return response;
	}

}
