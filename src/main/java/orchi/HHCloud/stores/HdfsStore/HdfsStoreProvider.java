package orchi.HHCloud.stores.HdfsStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.hadoop.fs.FileStatus;

import orchi.HHCloud.Start;
import orchi.HHCloud.store.Range;
import orchi.HHCloud.store.Status;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.store.arguments.DeleteArguments;
import orchi.HHCloud.store.arguments.DownloadArguments;
import orchi.HHCloud.store.arguments.GetStatusArguments;
import orchi.HHCloud.store.arguments.ListArguments;
import orchi.HHCloud.store.arguments.MkDirArguments;
import orchi.HHCloud.store.arguments.MoveOrCopyArguments;
import orchi.HHCloud.store.arguments.RenameArguments;
import orchi.HHCloud.store.response.CreateDirectoryResponse;
import orchi.HHCloud.store.response.DeleteResponse;
import orchi.HHCloud.store.response.GetStatusResponse;
import orchi.HHCloud.store.response.ListResponse;
import orchi.HHCloud.store.response.MoveOrCopyResponse;
import orchi.HHCloud.store.response.RenameResponse;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.DataUser;

public class HdfsStoreProvider implements StoreProvider {

	public HdfsStoreProvider() {

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
	public void read(User user,Path path, OutputStream out) {
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			HdfsManager.getInstance().readFile(p, out);
		} catch (IllegalArgumentException | IOException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void read(User user,Path path, Range range, OutputStream out) {
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			HdfsManager.getInstance().readFile(p, out, range);
		} catch (IllegalArgumentException | IOException e) {

			e.printStackTrace();
		}
	}

	@Override
	public void createStoreContextToUser(User user) throws IOException {
		DataUser dUser = (DataUser) user;
		String appName = Start.conf.getString("app.name");

		HdfsManager.getInstance().fs.mkdirs(HdfsManager.newPath(user.getId(), ""));
		
		Start.conf.getList("app.folders.wellcome").forEach(folder->{
			try {
				HdfsManager.getInstance().fs.mkdirs(HdfsManager.newPath(user.getId(), folder+""));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		String msgWellcome = "Bienvenido a " + appName+" "+dUser.getFirstName();
		String pathWellcome = msgWellcome+".md";
		create(user,Paths.get( pathWellcome ), new ByteArrayInputStream(msgWellcome.getBytes()));
	}

	@Override
	public void create(User user,Path path, InputStream in) {
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			HdfsManager.getInstance().writeFile(p, in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void touch(User user,Path path) {
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			if (!HdfsManager.getInstance().fs.exists(p))
				create(user,path, new ByteArrayInputStream("".getBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void download(DownloadArguments args) {
		new DownloadOperation(args).call();
	}

	@Override
	public boolean exists(User user,Path path) {
		boolean exists = false;
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			exists = HdfsManager.getInstance().fs.exists(p);
		} catch (IllegalArgumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return exists;
	}

	@Override
	public boolean isFile(User user,Path path) {
		boolean isfile = false;
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
		//org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(HdfsManager.root, path.toString());
		try {
			isfile = HdfsManager.getInstance().fs.isFile(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isfile;
	}

	@Override
	public boolean isDirectory(User user,Path path) {
		boolean isDirectory = false;
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
		//org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(HdfsManager.root, path.toString());
		try {
			isDirectory = HdfsManager.getInstance().fs.isDirectory(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isDirectory;
	}

	public Status getFileStatus(User user,Path path){
		Status status = new Status();
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
		try {
			FileStatus fsStatus = HdfsManager.getInstance().fs.getFileStatus(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Long getSize(User user, Path path) {
		Long size = 0L;
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			size=  HdfsManager.getInstance().fs.getFileStatus(p).getLen();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return size;
	}

}
