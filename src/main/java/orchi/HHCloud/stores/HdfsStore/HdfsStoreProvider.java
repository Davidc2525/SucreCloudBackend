package orchi.HHCloud.stores.HdfsStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;

import orchi.HHCloud.Start;
import orchi.HHCloud.store.Range;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.store.arguments.MoveOrCopyArguments;
import orchi.HHCloud.store.arguments.DeleteArguments;
import orchi.HHCloud.store.arguments.DownloadArguments;
import orchi.HHCloud.store.arguments.GetStatusArguments;
import orchi.HHCloud.store.arguments.ListArguments;
import orchi.HHCloud.store.arguments.MkDirArguments;
import orchi.HHCloud.store.arguments.RenameArguments;
import orchi.HHCloud.store.response.CreateDirectoryResponse;
import orchi.HHCloud.store.response.DeleteResponse;
import orchi.HHCloud.store.response.GetStatusResponse;
import orchi.HHCloud.store.response.ListResponse;
import orchi.HHCloud.store.response.MoveOrCopyResponse;
import orchi.HHCloud.store.response.RenameResponse;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;
import orchi.HHCloud.user.User;

public class HdfsStoreProvider implements StoreProvider {
		
	public HdfsStoreProvider(){
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		HdfsManager.getInstance();
	}

	

	@Override
	public CreateDirectoryResponse mkdir(MkDirArguments args) {
		
		return new CreateDirectoryOperation(args).call();
	}

	@Override
	public DeleteResponse delete(DeleteArguments args) {
		return new DeleteOperation(args).call();
	}

	@Override
	public ListResponse list(ListArguments args) {
		
		return new ListOperation(args).call();
	}

	@Override
	public GetStatusResponse status(GetStatusArguments args) {
		return new GetStatusOperation(args).call();
	}

	@Override
	public MoveOrCopyResponse copy(MoveOrCopyArguments args) {
		args.setMove(false);
		return new MoveOrCopyOperation(args).call();
	}

	@Override
	public MoveOrCopyResponse move(MoveOrCopyArguments args) {
		args.setMove(true);
		return new MoveOrCopyOperation(args).call();
	}

	@Override
	public RenameResponse rename(RenameArguments args) {
		return new RenameOperation(args).call();
	}

	@Override
	public void read(Path path, OutputStream out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void read(Path path, Range range, OutputStream out) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createStoreContextToUser(User user) throws IOException {
		org.apache.hadoop.fs.Path pathRoot = HdfsManager.newPath(user.getId(),"");
		HdfsManager.getInstance().fs.mkdirs(pathRoot);	
		String appName = Start.conf.getString("app.name");
		
		org.apache.hadoop.fs.Path  p = new org.apache.hadoop.fs.Path (pathRoot,"BIENVENIDO A "+appName);
		
		create(Paths.get(p.toString()),new ByteArrayInputStream(("Biencenido a "+appName).getBytes()));
	}

	@Override
	public void create(Path path, InputStream in) {
		try {
			HdfsManager.getInstance().writeFile(new org.apache.hadoop.fs.Path(path.toString()), in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void touch(Path path) {
		try {
			if(!HdfsManager.getInstance().fs.exists(new org.apache.hadoop.fs.Path(path.toString())))
				create(path,new ByteArrayInputStream("".getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void download(DownloadArguments args) {
		new DownloadOperation(args).call();
	}
	
	

}
