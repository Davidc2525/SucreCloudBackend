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

import eu.medsea.mimeutil.MimeUtil;
import orchi.SucreCloud.Util;
import orchi.SucreCloud.hdfs.HdfsManager;

public class ListOperation implements IOperation {

	private FileSystem fs;
	private String root;
	private JSONObject args;

	public ListOperation(JSONObject args) {
		
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
			if(!fs.exists(new Path(HdfsManager.newPath(root, path).toString()))){
				json.put("error", "file dont exists");
			}
			if (fs.isDirectory(new Path(HdfsManager.newPath(root, path).toString()))) {
				List<FileStatus> ls = Arrays
						.asList(fs.listStatus(new Path(HdfsManager.newPath(root, path).toString())));
				JSONObject lsJson = new JSONObject();
				// lsJson = new JSONObject();
				ls.stream().forEach(x -> {
					 
					try { 
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

			}else if(fs.isFile(new Path(HdfsManager.newPath(root, path).toString()))){
				return new GetStatOperation(args).call();
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
