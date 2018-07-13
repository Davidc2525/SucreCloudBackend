
package orchi.SucreCloud.operations;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.PUT;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.fs.ContentSummary;
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
	private boolean withContent;

	public GetStatusOperation(JSONObject args) {
		this.args = args;
		fs = HdfsManager.getInstance().fs;
		root = args.getString("root");
		path = Paths.get(args.getString("path")).normalize().toString();
		withContent = args.has("withContent") ? args.getBoolean("withContent"):false;
		opath = new Path(HdfsManager.newPath(root, path).toString());
		log.info("Nueva operacion de estatus de archivo {}", opath.toString());
	}

	@Override
	public JSONObject call() {
		JSONObject json = new JSONObject();



		try {
			if(!fs.exists(opath)){
				json.put("status", "error");
				json.put("error", "path_no_found")
				.put("errorMsg", "la rruta no existe "+Util.getPathWithoutRootPath(opath.toString()) );
				log.error("	file dont exists {}",opath);
				log.info("Fin de operacion de listado");
				return json;
			}
			/*if (fs.isDirectory(opath)) {
				return new ListOperation(args).call();
			}*/

			JSONObject file = new JSONObject();

			FileStatus fileStatus = fs.getFileLinkStatus(opath);
			log.debug("{}",fileStatus.getPath());
			file.put("size",fileStatus.getLen())
			.put("file", fileStatus.isFile())
			.put("name", fileStatus.getPath().getName())
			.put("path", Util.getPathWithoutRootPath(opath.toString())  )
			.put("mime", Files.probeContentType(Paths.get(opath.toString())))
			.put("persission",fileStatus.getPermission())
			.put("accessTime",fileStatus.getAccessTime())
			.put("modificationTime",fileStatus.getModificationTime());
			if(fileStatus.isFile() && withContent){
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				HdfsManager.getInstance().readFile(fileStatus.getPath(), baos);
				String fileBase64Content = Base64.encodeBase64String((baos.toByteArray()));
				//log.info("content {}",new String(Base64.decodeBase64(fileBase64Content.getBytes())));
				file.put("fileBase64Content", fileBase64Content);
			}
			if (fs.isDirectory(opath)) {
				
				ContentSummary contentSumary = fs.getContentSummary(fileStatus.getPath());
				file.put("size", contentSumary.getLength());
				file.put("elements", fs.listStatus(fileStatus.getPath()).length );
				file.put("fileCount", contentSumary.getFileCount());
				file.put("directoryCount", contentSumary.getDirectoryCount());
				file.put("spaceQuota", contentSumary.getSpaceQuota());

				//file.put("size", fs.getContentSummary(fileStatus.getPath()).getLength());
				//file.put("list", new ListOperation(args).call());
			}

			json.put("path",  path)

			.put("args", args);

			if(fs.isFile(opath)){
				json.put("file",true);
			}else{
				json.put("file",false);
			}


			json.put("data",file);
			json.put("status", "ok");
			log.info("Fin de operacion de estatus de archivo {}",opath.toString());
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
