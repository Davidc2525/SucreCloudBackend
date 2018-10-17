package orchi.HHCloud.stores.HdfsStore;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import orchi.HHCloud.Start;
import orchi.HHCloud.share.ShareProvider;
import orchi.HHCloud.store.StoreProvider;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.store.arguments.DeleteArguments;
import orchi.HHCloud.store.response.DeleteResponse;

public class DeleteOperation implements IOperation {
    private static Logger log = LoggerFactory.getLogger(DeleteOperation.class);
    private final ShareProvider shp;

    private List<java.nio.file.Path> paths;
    private DeleteArguments arg;
    private String path;
    private String root;
    private DeleteResponse response;

    public DeleteOperation(DeleteArguments arg) {
        HdfsManager.getInstance();
        this.arg = arg;
        response = new DeleteResponse();
        paths = arg.getPaths();
        path = arg.getPath().toString();
        root = arg.getUserId();
        shp = Start.getShareManager().getShareProvider();
        log.debug("Nueva operacion de eliminacion");

    }

    public DeleteResponse call() {
        try {
            if (paths != null) {
                Path opath = null;
                String lastPath = "/";
                for (java.nio.file.Path p : paths) {
                    lastPath = p.toString();
                    opath = new Path(HdfsManager.newPath(root, p.toString()).toString());
                    log.debug("Eliminando {}", opath);
                    HdfsManager.getInstance().deletePath(opath);
                    log.debug("{} eliminando", opath);

                    log.debug("{} eliminando de rrutas compartidas", p);
                    shp.deleteShare(arg.getUse(),p,true);

                }
                String parentOfLastPath = (Paths.get(lastPath).getParent().toString());
                response.setStatus("ok");
                response.setMultiple(true);
                response.setParent(Paths.get(parentOfLastPath));
                return response;
            } else {
                Path opath = new Path(HdfsManager.newPath(root, path).toString());
                log.debug("Eliminando {}", opath.toString());
                HdfsManager.getInstance().deletePath(opath);
                log.debug("{} eliminando", path);

                log.debug("{} eliminando de rrutas compartidas", path);
                shp.deleteShare(arg.getUse(),Paths.get(path),true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus("error");
            response.setError("server_error");
            response.setMsg(e.getMessage());
            return response;
        }
        response.setStatus("ok");
        response.setParent(Paths.get(path).getParent());
        response.setPath(Paths.get(path));
        return response;
    }

}
