package orchi.SucreCloud.store;

import java.io.IOException;

import javax.servlet.AsyncContext;

import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONObject;

import orchi.SucreCloud.ParseParamsMultiPart;

public interface Store {
	public JSONObject mkdir(JSONObject args);
	
	public JSONObject delete(JSONObject args);
	
	public JSONObject ls(JSONObject args);
	
	public JSONObject status(JSONObject args);
	
	public JSONObject copy(JSONObject args);
	
	public JSONObject move(JSONObject args);
	
	public JSONObject rename(JSONObject args);
	
	public void download(AsyncContext ctx, JSONObject arg);
	
	public JSONObject download(JSONObject args);
	
	public JSONObject upload(JSONObject args);

	public JSONObject upload(AsyncContext ctx, JSONObject arg, ParseParamsMultiPart params) throws FileUploadException, IOException;

	
}
