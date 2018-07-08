package orchi.SucreCloud.operations;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.medsea.mimeutil.MimeUtil;
import orchi.SucreCloud.Util;
import orchi.SucreCloud.hdfs.HdfsManager;

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
			if(!fs.exists(new Path(HdfsManager.newPath(root, path).toString()))){
				json.put("error", "file dont exists");
				log.error("file dont exists {} ",opath);
			}
			if (fs.isDirectory(new Path(HdfsManager.newPath(root, path).toString()))) {
				List<FileStatus> ls = Arrays
						.asList(fs.listStatus(new Path(HdfsManager.newPath(root, path).toString())));
				JSONObject lsJson = new JSONObject();
				// lsJson = new JSONObject();
				ls.stream().forEach(x -> {
					 
					try { 
						log.debug("\t{} in path",x.getPath().getName());
						lsJson.put(x.getPath().getName(),
								new JSONObject(x)
								.put("mime", Files.probeContentType(Paths.get( x.getPath().getName()  )) )
								//.put("mayme_mime", org.apache.http.entity.ContentType.parse(x.getPath().getName())    )
								.put("path", "/" + Util.nc(x.getPath().toString())));
								
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

				json.put("path", HdfsManager.newPath(root, path)).put("data", lsJson).put("args", args);
				json.put("spaceConsumed", fs.getContentSummary(opath).getSpaceConsumed());
				
				log.info("Fin de operacion de listado");
			}else if(fs.isFile(new Path(HdfsManager.newPath(root, path).toString()))){
				log.warn("transfer operation, get status file",opath);
				return new GetStatusOperation(args).call();
			}

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
