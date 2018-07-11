package orchi.SucreCloud.operations;

import java.io.IOException;
import java.nio.file.Paths;

import javax.servlet.AsyncContext;

import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.SucreCloud.hdfs.HdfsManager;

public class DeleteOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(DeleteOperation.class);
	private AsyncContext ctx;
	private JSONObject arg;

	public DeleteOperation(AsyncContext ctx, JSONObject arg) {
		this.ctx = ctx;
		this.arg = arg;
		log.debug("Nueva operacion de eliminacion");
	}

	@Override
	public JSONObject call() {
		String root = arg.getString("root");
		String path = arg.getString("path");
		Path opath = new Path(HdfsManager.newPath(root, path).toString());
		try {
			
			log.debug("Eliminando {}",path);
			HdfsManager.getInstance().deletePath(opath);
			log.debug("{} eliminando",path);
		} catch (IOException e) {
			e.printStackTrace();
			return new JSONObject().put("args",arg).put("status","error").put("error", e.getMessage());
			//
		}
		return new JSONObject().put("args",arg).put("status","ok").put("parent", Paths.get(path).getParent().toString());
	}

}
