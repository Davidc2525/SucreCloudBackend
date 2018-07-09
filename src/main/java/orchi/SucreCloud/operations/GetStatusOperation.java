
package orchi.SucreCloud.operations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.spi.LoggerFactory;
import org.json.JSONObject;
import org.slf4j.*;

import orchi.SucreCloud.Util;
import orchi.SucreCloud.hdfs.HdfsManager;

public class GetStatusOperation implements IOperation {
	private static Logger log = org.slf4j.LoggerFactory.getLogger(GetStatusOperation.class);
	private FileSystem fs;
	private String root;
	private JSONObject args;
	private Path opath;
	private String path;

	public GetStatusOperation(JSONObject args) {
		this.args = args;
		fs = HdfsManager.getInstance().fs;
		root = args.getString("root");
		path = args.getString("path");
		opath = new Path(HdfsManager.newPath(root, path).toString());
		log.info("Nueva operacion de estatus de archivo {}", opath.toString());
	}

	@Override
	public JSONObject call() {
		JSONObject json = new JSONObject();
		
		

		try {
			if(!fs.exists(opath)){
				json.put("error", "file dont exists");
				log.error("file dont exists {}",opath);
			}
			/*if (fs.isDirectory(opath)) {
				return new ListOperation(args).call();
			}*/
			
			JSONObject file = new JSONObject();
			
			FileStatus fileStatus = fs.getFileLinkStatus(opath);
			log.error("{}",fileStatus.getPath());
			file.put("size",fileStatus.getLen())
			.put("file", fileStatus.isFile())
			.put("name", fileStatus.getPath().getName())
			//.put("path","/"+ Util.nc(fileStatus.getPath().toString())  )
			.put("mime", Files.probeContentType(Paths.get(opath.toString())));
			if (fs.isDirectory(opath)) {
				
				//file.put("spaceQuota", fs.getContentSummary(fileStatus.getPath()).getSpaceQuota());
				//file.put("spaceConsumed", fs.getContentSummary(fileStatus.getPath()).getSpaceConsumed());
				file.put("list", new ListOperation(args).call());
			}
			
			json.put("path",  HdfsManager.newPath(root, path))
			
			.put("args", args);
			
			if(fs.isFile(opath)){
				json.put("file",true);				
			}else{
				json.put("file",false);
			}
			
			
			json.put("data",file);
			log.info("Fin de operacion de estatus de archivo {}",opath.toString());
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
