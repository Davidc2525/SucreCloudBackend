package orchi.HHCloud.stores.hdfsStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.AsyncContext;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;

import orchi.HHCloud.ParseParamsMultiPart;
import orchi.HHCloud.Start;
import orchi.HHCloud.store.Store;
import orchi.HHCloud.user.User;

public class HdfsStoreProvider implements Store {
		
	public HdfsStoreProvider(){
		
	}
	
	@Override
	public void init(){
		
	}
	
	@Override
	public void start(){
		HdfsManager.getInstance();
	}

	@Override
	public JSONObject mkdir(JSONObject args) {		
		return new CreateDirectoryOperation(args).call();
	}

	@Override
	public JSONObject delete(JSONObject args) {
		return new DeleteOperation(args).call();
	}

	@Override
	public JSONObject ls(JSONObject args) {
		return new ListOperation(args).call();
	}

	@Override
	public JSONObject status(JSONObject args) {
		return new GetStatusOperation(args).call();
	}

	@Override
	public JSONObject copy(JSONObject args) {
		return new MoveOrCopyOperation(args, false).call();
	}

	@Override
	public JSONObject move(JSONObject args) {
		return new MoveOrCopyOperation(args, true).call();
	}

	@Override
	public JSONObject rename(JSONObject args) {
		return new RenameOperation(args).call();
	}

	@Override
	public void download(AsyncContext ctx, JSONObject arg) {
		new DownloadOperation(ctx, arg);
	}

	@Override
	public JSONObject download(JSONObject args) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public JSONObject upload(AsyncContext ctx, JSONObject arg, ParseParamsMultiPart params)
			throws FileUploadException, IOException {
		return new UploadOperation(ctx,arg,params).call();
	}

	@Override
	public void createStoreContextToUser(User user) throws IOException {
		Path pathRoot = HdfsManager.newPath(user.getId(),"");
		HdfsManager.getInstance().fs.mkdirs(pathRoot);	
		String appName = Start.conf.getString("app.name");
		
		Path p = new Path(pathRoot,"BIENVENIDO A "+appName);
		create(p,new ByteArrayInputStream(("Biencenido a "+appName).getBytes()));
	}

	@Override
	public void create(Path path, InputStream in) {
		try {
			HdfsManager.getInstance().writeFile(path, in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void touch(Path path) {
		try {
			if(!HdfsManager.getInstance().fs.exists(path))
				create(path,new ByteArrayInputStream("".getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	

}
