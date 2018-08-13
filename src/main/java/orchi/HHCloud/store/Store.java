package orchi.HHCloud.store;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.AsyncContext;

import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import orchi.HHCloud.user.User;

/**
 * @deprecated
 * @see StoreProvider*/
public interface Store {

	public void init() ;

	public void start();

	/**metodos genericos*/
	public void createStoreContextToUser(User user) throws IOException;

	public JSONObject mkdir(JSONObject args);

	public JSONObject delete(JSONObject args);

	public JSONObject ls(JSONObject args);

	public JSONObject status(JSONObject args);

	public JSONObject copy(JSONObject args);

	public JSONObject move(JSONObject args);

	public JSONObject rename(JSONObject args);

	public void download(AsyncContext ctx, JSONObject arg);

	public JSONObject download(JSONObject args);

    /**metodos espesificos*/

	public void create(Path path, InputStream in);

	public void touch(Path path);

	//public JSONObject upload(JSONObject args);

	//public JSONObject upload(AsyncContext ctx, JSONObject arg, ParseParamsMultiPart params) throws FileUploadException, IOException;

	//pulic void writeFile(InputStream in, JSONObject arg);
}
