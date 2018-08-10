package orchi.HHCloud.stores.hdfsStore;

import java.io.IOException;
import java.nio.file.Paths;

import javax.servlet.AsyncContext;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import orchi.HHCloud.ParseParamsMultiPart;
import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;

public class UploadOperation implements IOperation {

	private String root;
	private String path;
	private Path opath;

	public UploadOperation(AsyncContext ctx, JSONObject arg, ParseParamsMultiPart params)
			throws FileUploadException, IOException {

		FileItemStream fileItem = params.getParam("f");
		
		java.nio.file.Path p = Paths.get( arg.getString("path"), params.getParam("f").getName());
		root = arg.getString("root");
		opath = new Path(HdfsManager.newPath(root, p.toString()).toString());

		HdfsManager.getInstance().writeFile(opath, fileItem.openStream());

	}

	@Override
	public JSONObject call() {
		return new JSONObject().put("msg", "listo");
	}

}
