package orchi.HHCloud.stores.HdfsStore;

import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.quota.Quota;
import orchi.HHCloud.store.*;
import orchi.HHCloud.store.arguments.*;
import orchi.HHCloud.store.response.*;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class HdfsStoreProvider implements StoreProvider {

    public HdfsStoreProvider() {

    }

    @Override
    public void init() {

    }

    @Override
    public void start() {
        HdfsManager.getInstance();
    }

    @Override
    public CreateDirectoryResponse mkdir(MkDirArguments args) {

        return new CreateDirectoryOperation(args).run();
    }

    @Override
    public DeleteResponse delete(DeleteArguments args) {
        return new DeleteOperation(args).run();
    }

    @Override
    public ListResponse list(ListArguments args) {

        return new ListOperation(args).run();
    }

    @Override
    public GetStatusResponse status(GetStatusArguments args) {
        return new GetStatusOperation(args).run();
    }

    @Override
    public MoveOrCopyResponse copy(MoveOrCopyArguments args) {
        args.setMove(false);
        return new MoveOrCopyOperation(args).run();
    }

    @Override
    public MoveOrCopyResponse move(MoveOrCopyArguments args) {
        args.setMove(true);
        return new MoveOrCopyOperation(args).run();
    }

    @Override
    public RenameResponse rename(RenameArguments args) {
        return new RenameOperation(args).run();
    }

    @Override
    public void read(User user, Path path, OutputStream out) {
        try {
            org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
            HdfsManager.getInstance().readFile(p, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(User user, Path path, Range range, OutputStream out) {
        try {
            org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
            HdfsManager.getInstance().readFile(p, out, range);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @Override
    public void createStoreContextToUser(User user) {
        try {
            DataUser dUser = (DataUser) user;
            String appName = Start.conf.getString("app.name");
            org.apache.hadoop.fs.Path rootUserPath = HdfsManager.newPath(user.getId(), "");
            HdfsManager.getInstance().fs.mkdirs(rootUserPath);

            Start.getQuotaManager()
                    .getProvider()
                    .setQuota(user, Paths.get(""), ((DataUser) user).isEmailVerified() ? StoreManager.SPACE_QUOTA_SIZE : StoreManager.SPACE_QUOTA_SIZE_NO_VERIFIED_USER);

            Start.conf.getList("app.folders.wellcome").forEach(folder -> {
                try {
                    HdfsManager.getInstance().fs.mkdirs(HdfsManager.newPath(user.getId(), folder + ""));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            String msgWellcome = "Bienvenido a " + appName + " " + dUser.getFirstName();
            String pathWellcome = msgWellcome + ".md";
            create(user, Paths.get(pathWellcome), new ByteArrayInputStream(msgWellcome.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(User user, Path path, InputStream in) throws Exception {
        org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
        HdfsManager.getInstance().writeFile(p, in);
    }

    @Override
    public void delete(User user, Path path) {
        org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
        try {
            HdfsManager.getInstance().fs.delete(p, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void touch(User user, Path path) {
        try {
            org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
            if (!HdfsManager.getInstance().fs.exists(p))
                create(user, path, new ByteArrayInputStream("".getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void download(DownloadArguments args) {
        new DownloadOperation(args).run();
    }

    @Override
    public boolean exists(User user, Path path) {
        boolean exists = false;
        try {
            org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
            exists = HdfsManager.getInstance().fs.exists(p);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exists;
    }

    @Override
    public boolean isFile(User user, Path path) {
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
    public boolean isDirectory(User user, Path path) {
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

    public Status getFileStatus(User user, Path path) {
        Status status = new Status();
        org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
        try {
            FileStatus fsStatus = HdfsManager.getInstance().fs.getFileStatus(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long getSize(User user, Path path) {
        Long size = 0L;
        try {
            org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
            size = HdfsManager.getInstance().fs.getFileStatus(p).getLen();
        } catch (Exception e) {

            e.printStackTrace();
        }

        return size;
    }

    @Override
    public ContentSummary getContentSummary(User user, Path path) {
        ContentSummary cs = new ContentSummary();
        DataUser dUser = (DataUser) user;
        boolean verified = dUser.isEmailVerified();
        Quota spaceQuota = null;
        try {
            org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
            FileSystem fs = HdfsManager.getInstance().getFs();
            org.apache.hadoop.fs.ContentSummary fsCs = fs.getContentSummary(p);

            spaceQuota = Start.getQuotaManager().getProvider().getQuota(user);

            cs.setDirectoryCount(fsCs.getDirectoryCount());
            cs.setFileCount(fsCs.getFileCount());
            cs.setSpaceQuota(spaceQuota.getQuota()/*verified ? Start.getStoreManager().SPACE_QUOTA_SIZE : Start.getStoreManager().SPACE_QUOTA_SIZE_NO_VERIFIED_USER*/);
            // cs.setSpaceQuota(fsCs.getSpaceQuota());
            cs.setSpaceConsumed(fsCs.getSpaceConsumed());
            cs.setLength(fsCs.getLength());
            cs.setQuota(fsCs.getQuota());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cs;
    }


    @Override
    public Quota setQuota(User user, Path path, long size) throws QuotaException {
        org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path + "");
        try {
            HdfsManager.getInstance().dfsAdmin.setSpaceQuota(p, size * 2);
        } catch (Exception e) {
            e.printStackTrace();
            throw new QuotaException(e);
        }
        return null;
    }

    @Override
    public boolean quotaIsSet(User user) throws QuotaException {
        return false;
    }

    @Override
    public Quota getQuota(User user) throws QuotaException {
        return null;
    }

    @Override
    public void removeQuota(User user, Path path) throws QuotaException {
        org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path + "");
        try {
            HdfsManager.getInstance().dfsAdmin.clearSpaceQuota(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Api de bajo nivel
     *
     * @param path
     * @param out
     */
    @Override
    public void read(Path path, OutputStream out) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            HdfsManager.getInstance().readFile(p, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(Path path, Range range, OutputStream out) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            HdfsManager.getInstance().readFile(p, out,range);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void create(Path path, InputStream in) throws Exception {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            HdfsManager.getInstance().writeFile(p,in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Path path) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            HdfsManager.getInstance().deletePath(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void touch(Path path) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            HdfsManager.getInstance().writeFile(p,new ByteArrayInputStream("".getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean exists(Path path) {
        try {
            //System.out.println("        PATH: "+path);
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());
            //System.out.println("        PATH: "+path);
            //System.out.println("        PATH HDFS: "+p);
            return HdfsManager.getInstance().fs.exists(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isFile(Path path) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            return HdfsManager.getInstance().fs.isFile(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isDirectory(Path path) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            return HdfsManager.getInstance().fs.isDirectory(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Long getSize(Path path) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());

            return HdfsManager.getInstance().fs.getFileStatus(p).getLen();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public ContentSummary getContentSummary(Path path) {
       return null;

    }

    @Override
    public byte[] getAttr(Path path, String name) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());
            FileSystem fs = HdfsManager.getInstance().getFs();

            return fs.getXAttr(p,name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Map<String, byte[]> getAttr(Path path) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());
            FileSystem fs = HdfsManager.getInstance().getFs();

            return fs.getXAttrs(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setAttr(Path path, String name, byte[] value) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());
            FileSystem fs = HdfsManager.getInstance().getFs();

            fs.setXAttr(p,name,value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAttr(Path path, String name) {
        try {
            Path pp = Paths.get(HdfsManager.root, path+"");
            org.apache.hadoop.fs.Path p = new org.apache.hadoop.fs.Path(pp.toString());
            FileSystem fs = HdfsManager.getInstance().getFs();

            fs.removeXAttr(p,name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
