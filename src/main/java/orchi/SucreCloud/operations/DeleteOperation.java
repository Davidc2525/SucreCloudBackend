package orchi.SucreCloud.operations;

import orchi.SucreCloud.Util;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.AsyncContext;

import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.SucreCloud.hdfs.HdfsManager;

public class DeleteOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(DeleteOperation.class);
	private JSONObject arg;
	private List<Object> paths;
	public DeleteOperation(AsyncContext ctx, JSONObject arg) {
		this.arg = arg;
		paths = (arg.has("paths") && !arg.isNull("paths")) ?arg.getJSONArray("paths").toList():null;
		log.debug("Nueva operacion de eliminacion");
	}

	@Override
	public JSONObject call() {
		String root = arg.getString("root");
		String path = arg.getString("path");


		try {
			if(paths!=null){
				Path opath = null;
				String lastPath = "/";
				for(Object p:paths){
				    lastPath = (String) p;
					opath  = new Path(HdfsManager.newPath(root, p+"").toString());
					log.debug("Eliminando {}",opath);
					HdfsManager.getInstance().deletePath(opath);
					log.debug("{} eliminando",opath);
				}
                String parentOfLastPath = (Paths.get(lastPath).getParent().toString());
				return new JSONObject().put("args",arg).put("status","ok").put("parent", parentOfLastPath);
			}else{
				Path opath = new Path(HdfsManager.newPath(root, path).toString());
				log.debug("Eliminando {}",path);
				HdfsManager.getInstance().deletePath(opath);
				log.debug("{} eliminando",path);
			}

		} catch (IOException e) {
			e.printStackTrace();
			return new JSONObject().put("args",arg).put("status","error").put("error", e.getMessage());
			//
		}
		return new JSONObject().put("args",arg).put("status","ok").put("parent", Paths.get(path).getParent().toString());
	}

}
