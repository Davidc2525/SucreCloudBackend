package orchi.HHCloud.stores.HdfsStore;

import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.store.RestrictedNames;
import orchi.HHCloud.store.arguments.MkDirArguments;
import orchi.HHCloud.store.response.CreateDirectoryResponse;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class CreateDirectoryOperation implements IOperation {
    private static Logger log = LoggerFactory.getLogger(CreateDirectoryOperation.class);
    private String root;
    private Path pathWithRoot;
    private FileSystem fs;
    private String newDirectory;
    private MkDirArguments args;
    private String path;

    private CreateDirectoryResponse response;

    public CreateDirectoryOperation(MkDirArguments args) {
        this.args = args;
        response = new CreateDirectoryResponse();
        fs = HdfsManager.getInstance().fs;
        root = args.getUserId();
        path = args.getPath().toString();
        pathWithRoot = new Path(HdfsManager.newPath(root, path).toString());

        log.debug("nueva operacio de crear directorio");
    }

    public CreateDirectoryResponse run() {

        try {
            if (fs.exists(pathWithRoot) || RestrictedNames.isRestricted(pathWithRoot.getName())) {
                response.setStatus("error");
                response.setError("directry_is_used");
                response.setMsg("la rruta ya existe: " + path.toString());
                response.setPath(Paths.get(path));
                log.debug("	la rruta ya existe {}", path.toString());
            } else {
                fs.mkdirs(pathWithRoot);
                response.setStatus("ok");
                response.setPath(Paths.get(path));

                log.debug("	nuevo directorio creado {} {}", path.toString(), pathWithRoot.toString());
            }
        } catch (IOException e) {
            response.setPath(Paths.get(path));
            response.setStatus("error");
            response.setError("server_error");
            response.setMsg(e.getMessage());

            e.printStackTrace();
        }
        log.debug("operacion de crear directorio terminada.");
        return response;
    }

}
