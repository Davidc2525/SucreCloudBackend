package orchi.SucreCloud.operations;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import orchi.SucreCloud.hdfs.HdfsManager;

public class UploadOperation implements IOperation{

		
	private String root;
	private String path;
	private Path opath;

	public  UploadOperation(AsyncContext ctx, JSONObject arg) throws FileUploadException, IOException {
		//Streams
		
		
		boolean isMultipart = ServletFileUpload.isMultipartContent((HttpServletRequest) ctx.getRequest());
		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload();
		if(isMultipart){
			FileItemIterator iter = upload.getItemIterator((HttpServletRequest) ctx.getRequest());
			while (iter.hasNext()) {
			    FileItemStream item = iter.next();
			    String name = item.getFieldName();
			    //InputStream stream = item.getInputStream();
			    if(name.equals("f")){
			    	//System.out.println();
			    	java.nio.file.Path p = Paths.get(arg.getString("root"), arg.getString("path"),item.getName());
			    	HdfsManager.getInstance().writeFile(new Path(p.toString()), item.openStream());
			    }
			   
			}
		}
		// Parse the request
		
	}	
	
	@Override
	public JSONObject call() {
		// TODO Auto-generated method stub
		return new JSONObject().put("msg", "listo");
	}

}
