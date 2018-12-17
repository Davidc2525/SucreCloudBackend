package orchi.HHCloud.store;

import orchi.HHCloud.quota.QuotaProvider;
import orchi.HHCloud.store.arguments.*;
import orchi.HHCloud.store.response.*;
import orchi.HHCloud.user.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Map;

public interface StoreProvider extends QuotaProvider {

    public void init();

    public void start();

    public void createStoreContextToUser(User user) throws IOException;

    /**
     * api de alto nivel
     */
    public CreateDirectoryResponse mkdir(MkDirArguments args);

    public DeleteResponse delete(DeleteArguments args);

    public ListResponse list(ListArguments args);

    public GetStatusResponse status(GetStatusArguments args);

    public MoveOrCopyResponse copy(MoveOrCopyArguments args);

    public MoveOrCopyResponse move(MoveOrCopyArguments args);

    public RenameResponse rename(RenameArguments args);

    public void download(DownloadArguments args);

    /**
     * Api de bajo nivel con usuario
     */
    public void read(User user, Path path, OutputStream out);

    public void read(User user, Path path, Range range, OutputStream out);

    public void create(User user, Path path, InputStream in) throws Exception;

    public void delete(User user, Path path);

    public void touch(User user, Path path);

    public boolean exists(User user, Path path);

    public boolean isFile(User user, Path path);

    public boolean isDirectory(User user, Path path);

    public Long getSize(User user, Path path);

    public ContentSummary getContentSummary(User user, Path path);


    /**
     * Api de bajo nivel
     */
    public void read(Path path, OutputStream out);

    public void read(Path path, Range range, OutputStream out);

    public void create(Path path, InputStream in) throws Exception;

    public void delete(Path path);

    public void touch(Path path);

    public boolean exists(Path path);

    public boolean isFile(Path path);

    public boolean isDirectory(Path path);

    public Long getSize(Path path);

    public ContentSummary getContentSummary(Path path);



    public byte[] getAttr(Path path,String name);

    public Map<String, byte[]> getAttr(Path path);

    public void setAttr(Path path,String name,byte[] value);

    public void deleteAttr(Path path,String name);

}
