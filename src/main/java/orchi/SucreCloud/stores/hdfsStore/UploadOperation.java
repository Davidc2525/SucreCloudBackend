package orchi.SucreCloud.stores.hdfsStore;

import java.io.IOException;
import java.nio.file.Paths;

import javax.servlet.AsyncContext;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import orchi.SucreCloud.ParseParamsMultiPart;
import orchi.SucreCloud.operations.IOperation;
import orchi.SucreCloud.stores.hdfsStore.HdfsManager;

public class UploadOperation implements IOperation {

	private String root;
	private String path;
	private Path opath;

	public UploadOperation(AsyncContext ctx, JSONObject arg, ParseParamsMultiPart params)
			throws FileUploadException, IOException {

		FileItemStream fileItem = params.getParam("f");
		java.nio.file.Path p = Paths.get(arg.getString("root"), arg.getString("path"), params.getParam("f").getName());
		HdfsManager.getInstance().writeFile(new Path(p.toString()), fileItem.openStream());

	}

	@Override
	public JSONObject call() {
		// TODO Auto-generated method stub
		return new JSONObject().put("msg", "listo");
	}

}
