package orchi.HHCloud.stores.HdfsStore;

import java.io.IOException;

import orchi.HHCloud.Start;
import orchi.HHCloud.share.ShareProvider;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.store.arguments.RenameArguments;
import orchi.HHCloud.store.response.RenameResponse;

public class RenameOperation implements IOperation {
    private static Logger log = LoggerFactory.getLogger(RenameOperation.class);
    private final ShareProvider shp;
    private Path srcPath;
    private Path dstPath;
    private String root;
    private Path srcpathWithRoot;
    private Path dstpathWithRoot;
    private RenameArguments arg;
    private RenameResponse response;

    public RenameOperation(RenameArguments arg) {
        this.arg = arg;
        response = new RenameResponse();
        srcPath = new Path(arg.getSrcPath().toString());
        dstPath = new Path(arg.getDstPath().normalize().toString());
        root = arg.getUserId();
        srcpathWithRoot = new Path(HdfsManager.newPath(root, srcPath.toString()).toString());
        dstpathWithRoot = new Path(HdfsManager.newPath(root, dstPath.toString()).toString());
        shp = Start.getShareManager().getShareProvider();
        log.debug("nueva operacio de renombrado");

    }

    public RenameResponse call() {
        log.debug("	renombrando {} a {}", srcPath.toString(), dstPath.toString());
        try {
            if (HdfsManager.getInstance().fs.exists(srcpathWithRoot)) {
                if (HdfsManager.getInstance().fs.exists(dstpathWithRoot)) {
                    response.setStatus("error");
                    response.setError("dstpath_is_used");
                    response.setMsg("la rruta de destino ya existe " + dstPath.toString());

                    log.debug("la rruta de destino ya existe  {}", dstPath.toString());
                } else {
                    HdfsManager.getInstance().fs.rename(srcpathWithRoot, dstpathWithRoot);
                    response.setStatus("ok");
                    log.debug("	renombrado exitoso ");

                    log.debug("{} eliminando de rrutas compartidas", arg.getSrcPath());
                    shp.deleteShare(arg.getUse(), arg.getSrcPath(), true);
                }

            } else {
                response.setStatus("error");
                response.setError("srcpath_no_found");
                response.setMsg("la ruta que quiere renombrar no existe");

                log.debug("	falla al renombrar, '{}' no existe", srcPath.toString());
            }

        } catch (IOException e) {
            response.setStatus("error");
            response.setError("server_error");
            response.setMsg(e.getMessage());
            e.printStackTrace();
        }
        log.debug("operacion de renombrado terminada.");
        return response;
    }

}
