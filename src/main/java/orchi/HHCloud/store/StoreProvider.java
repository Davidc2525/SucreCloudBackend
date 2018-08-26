package orchi.HHCloud.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

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

public interface StoreProvider extends QuotaProvider {

	public void init();

	public void start();

	public void createStoreContextToUser(User user) throws IOException;

	/** api de alto nivel */
	public CreateDirectoryResponse mkdir(MkDirArguments args);

	public DeleteResponse delete(DeleteArguments args);

	public ListResponse list(ListArguments args);

	public GetStatusResponse status(GetStatusArguments args);

	public MoveOrCopyResponse copy(MoveOrCopyArguments args);

	public MoveOrCopyResponse move(MoveOrCopyArguments args);

	public RenameResponse rename(RenameArguments args);

	public void download(DownloadArguments args);

	/** Api de bajo nivel */
	public void read(User user,Path path, OutputStream out);

	public void read(User user,Path path, Range range, OutputStream out);

	public void create(User user,Path path, InputStream in);

	public void touch(User user,Path path);

	public boolean exists(User user,Path path);

	public boolean isFile(User user,Path path);

	public boolean isDirectory(User user,Path path);

	public Long getSize(User user, Path path);

	public ContentSummary getContentSummary(User user, Path path);
}
