package orchi.HHCloud.stores.HdfsStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import orchi.HHCloud.store.*;
import org.apache.hadoop.fs.FileStatus;

import orchi.HHCloud.Start;
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
import org.apache.hadoop.fs.FileSystem;

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
		} catch ( Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void read(User user,Path path, Range range, OutputStream out) {
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			HdfsManager.getInstance().readFile(p, out, range);
		} catch ( Exception e) {

			e.printStackTrace();
		}
	}

	@Override
	public void createStoreContextToUser(User user)  {
		try {
			DataUser dUser = (DataUser) user;
			String appName = Start.conf.getString("app.name");
			org.apache.hadoop.fs.Path rootUserPath = HdfsManager.newPath(user.getId(), "");
			HdfsManager.getInstance().fs.mkdirs(rootUserPath);

			setQuota(user,Paths.get(""), StoreManager.SPACE_QUOTA_SIZE_NO_VERIFIED_USER);
			Start.conf.getList("app.folders.wellcome").forEach(folder->{
				try {
					HdfsManager.getInstance().fs.mkdirs(HdfsManager.newPath(user.getId(), folder+""));
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			String msgWellcome = "Bienvenido a " + appName+" "+dUser.getFirstName();
			String pathWellcome = msgWellcome+".md";
			create(user,Paths.get( pathWellcome ), new ByteArrayInputStream(msgWellcome.getBytes()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void create(User user,Path path, InputStream in) throws Exception {
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
		HdfsManager.getInstance().writeFile(p, in);
	}

	@Override
	public void touch(User user,Path path) {
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			if (!HdfsManager.getInstance().fs.exists(p))
				create(user,path, new ByteArrayInputStream("".getBytes()));
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isDirectory;
	}

	public Status getFileStatus(User user,Path path){
		Status status = new Status();
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
		try {
			FileStatus fsStatus = HdfsManager.getInstance().fs.getFileStatus(p);
		} catch (Exception e) {
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
		} catch (Exception e) {

			e.printStackTrace();
		}

		return size;
	}

	@Override
	public ContentSummary getContentSummary(User user, Path path) {
		ContentSummary cs = new ContentSummary();
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			FileSystem fs = HdfsManager.getInstance().getFs();
			org.apache.hadoop.fs.ContentSummary fsCs = fs.getContentSummary(p);

			cs.setDirectoryCount(fsCs.getDirectoryCount());
			cs.setFileCount(fsCs.getFileCount());
			cs.setSpaceQuota(fsCs.getSpaceQuota());
			cs.setSpaceConsumed(fsCs.getSpaceConsumed());
			cs.setLength(fsCs.getLength());
			cs.setQuota(fsCs.getQuota());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cs;
	}

	@Override
	public void setQuota(User user, Path path, long size) {
		if (HdfsManager.isLocalFileSystem) {
			return;
		}
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path+"" );
		try {
			HdfsManager.getInstance().dfsAdmin.setSpaceQuota(p, size);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeQuota(User user, Path path) {
		if (HdfsManager.isLocalFileSystem) {
			return;
		}
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path+"" );
		try {
			HdfsManager.getInstance().dfsAdmin.clearSpaceQuota(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
