package orchi.HHCloud.stores.hdfsStore;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Util;
import orchi.HHCloud.operations.IOperation;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;

public class ListOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(ListOperation.class);
	private FileSystem fs;
	private String root;
	private JSONObject args;
	private Path opath;
	private String path;

	public ListOperation(JSONObject args) {

		this.args = args;
		fs = HdfsManager.getInstance().fs;
		root = args.getString("root");
		path = args.getString("path");
		opath = new Path(HdfsManager.newPath(root, path).toString());

		log.info("Nueva operacion de listado {}",opath.toString());

	}

	@Override
	public JSONObject call() {
		JSONObject json = new JSONObject();




		try {
			if(!fs.exists(opath)){
				json.put("status", "error");
				json.put("error", "path_no_found")
				.put("errorMsg", "la rruta no existe "+Util.getPathWithoutRootPath(opath.toString()) );
				log.error("	file dont exists {} ",opath);
				log.info("Fin de operacion de listado");
				return json;
			}
			if (fs.isDirectory(opath)) {
				List<FileStatus> ls = Arrays
						.asList(fs.listStatus(new Path(HdfsManager.newPath(root, path).toString())));
				List<JSONObject> lsyet = new ArrayList<>();


				// lsJson = new JSONObject();

				ls.stream().forEach(x -> {

					try {

						log.debug("\t{} in path",x.getPath().getName());
						JSONObject lsJson = new JSONObject();
						lsJson.put("name",x.getPath().getName())
						.put("replication", x.getReplication())
						.put("file",x.isFile())
						.put("name",x.getPath().getName() )
						.put("mime", Files.probeContentType(Paths.get( x.getPath().getName()  )) )
								//.put("mayme_mime", org.apache.http.entity.ContentType.parse(x.getPath().getName())    )
						.put("path",  Util.getPathWithoutRootPath(x.getPath().toString()))
						.put("persission",x.getPermission())
						.put("accessTime",x.getAccessTime())
						.put("modificationTime",x.getModificationTime());
						if(x.isFile()){
							lsJson.put("size", x.getLen());
						}else{
						    ContentSummary contentSumary = fs.getContentSummary(x.getPath());
						    lsJson.put("size",contentSumary.getLength());
							//lsJson.put("size", fs.listStatus(x.getPath()).length );
							lsJson.put("elements", fs.listStatus(x.getPath()).length );
							contentSumary = null;
						}

						lsyet.add(lsJson);
						lsJson = null;
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
				json.put("data", lsyet);
				json.put("path",  path)
				.put("file", false)
				.put("args", args);


				ContentSummary contentSumary = fs.getContentSummary(opath);
				json.put("size", contentSumary.getLength());
				json.put("directoryCount", contentSumary.getDirectoryCount());
				json.put("fileCount", contentSumary.getFileCount());

				//json.put("totalSize",new orchi.HHCloud.operations.DownloadOperation.Tree(opath).totalSize );
				json.put("status", "ok");
				log.info("Fin de operacion de listado");
			}else if(fs.isFile(new Path(HdfsManager.newPath(root, path).toString()))){
				log.warn("transfer operation, get status file",opath);
				return new GetStatusOperation(args).call();
			}

		} catch (IllegalArgumentException e) {
			json.put("status", "error");
			json.put("error", "server");
			json.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			json.put("status", "error");
			json.put("error", "server");
			json.put("errorMsg", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			json.put("status", "error");
			json.put("error", "server");
			json.put("errorMsg", e.getMessage());
			e.printStackTrace();
		}

		return json;
	}

}
