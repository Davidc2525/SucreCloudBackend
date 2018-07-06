
package orchi.SucreCloud.operations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import orchi.SucreCloud.Util;
import orchi.SucreCloud.hdfs.HdfsManager;

public class GetStatOperation implements IOperation {

	private FileSystem fs;
	private String root;
	private JSONObject args;

	public GetStatOperation(JSONObject args) {
		this.args = args;
		fs = HdfsManager.getInstance().fs;
	}

	@Override
	public JSONObject call() {
		JSONObject json = new JSONObject();
		root = args.getString("root");
		String path = args.getString("path");
		Path opath = new Path(HdfsManager.newPath(root, path).toString());

		try {
			if(!fs.exists(opath)){
				json.put("error", "file dont exists");
			}
			if (fs.isDirectory(opath)) {
				return new ListOperation(args).call();
			}
			
			JSONObject file = new JSONObject();
			
			FileStatus fileStatus = fs.getFileLinkStatus(opath);
			file.put("size",fileStatus.getLen())
			.put("name", fileStatus.getPath().getName())
			.put("path",fileStatus.getPath().toString() )
			.put("mime", Files.probeContentType(Paths.get(opath.toString())));
			
			json.put("path", opath.toString()).put("args", args);

			
			json.put("data",file);

		} catch (IllegalArgumentException e) {
			json.put("error", e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			json.put("error", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			json.put("error", e.getMessage());
			e.printStackTrace();
		}

		return json;
	}

}
