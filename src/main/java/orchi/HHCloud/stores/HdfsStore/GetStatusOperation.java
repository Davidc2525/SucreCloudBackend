package orchi.HHCloud.stores.HdfsStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONObject;
import org.slf4j.Logger;

import orchi.HHCloud.Start;
import orchi.HHCloud.Util;
import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.share.ShareProvider;
import orchi.HHCloud.share.Shared;
import orchi.HHCloud.store.RestrictedNames;
import orchi.HHCloud.store.Status;
import orchi.HHCloud.store.arguments.GetStatusArguments;
import orchi.HHCloud.store.response.GetStatusResponse;

public class GetStatusOperation implements IOperation {
    private static Logger log = org.slf4j.LoggerFactory.getLogger(GetStatusOperation.class);
    private FileSystem fs;
    private String root;
    private Path opath;
    private String path;
    private List<java.nio.file.Path> paths;
    private GetStatusResponse response;
    private GetStatusArguments args;


    private ShareProvider shareProvider;
    private Shared shared;
    private boolean thisIsShared = false;
    private boolean shareInfo = false;

    public GetStatusOperation(GetStatusArguments args) {

        this.args = args;
        response = new GetStatusResponse();
        fs = HdfsManager.getInstance().fs;
        root = args.getUserId();
        path = args.getPath().toString();
        paths = args.getPaths();
        opath = new Path(HdfsManager.newPath(root, path).toString());

        shareProvider = Start.getShareManager().getShareProvider();
        shareInfo = args.getShareInfo();

        if(shareInfo){
            thisIsShared = shareProvider.isShared(args.getUse(), Paths.get(path));
            shared = shareProvider.sharedInDirectory(args.getUse(), Paths.get(path).getParent());
        }


        log.info("Nueva operacion de estatus de archivo {}", opath.toString());
    }

    public GetStatusResponse call() {
        if (paths != null) {// Multiple
            try {
                response.setMultiple(true);
                response.setPath(Paths.get(path));
                response.setPaths(paths);

                List<Status> payloadList = new ArrayList<>();
                Long sizeSum = 0l;
                for (java.nio.file.Path p : paths) {

                    Path item = new Path(HdfsManager.newPath(root, p.toString()).toString());
                    String itemPath = Util.getPathWithoutRootPath(item.toString());

                    // System.out.println(" itemPath: " + itemPath);
                    GetStatusArguments arguments = new GetStatusArguments();
                    arguments.setPath(Paths.get(itemPath));
                    arguments.setUser(args.getUse());
                    GetStatusResponse contentItem = new GetStatusOperation(arguments).call();
                    Status status = null;
                    if (contentItem.getPayload() != null) {
                        status = (Status) contentItem.getPayload();
                        payloadList.add(status);
                        sizeSum += status.getSize();
                    }

                }
                response.setPayload(payloadList);
                response.setStatus("ok");
                response.setSize(sizeSum);
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus("error");
                response.setError("fetch_info");
                response.setMsg("Error al intentar obtener informacion de las rrutas");

                return response;
            }
        }

        try {

            if (!fs.exists(opath) || RestrictedNames.isRestricted(opath.getName())) {
                response.setStatus("error");
                response.setError("path_no_found");
                response.setMsg("la rruta no existe " + Util.getPathWithoutRootPath(opath.toString()));
                response.setPath(Paths.get(Util.getPathWithoutRootPath(opath.toString())));

                log.error("	file dont exists {}", opath);
                log.info("Fin de operacion de listado");
                return response;
            }

            if (RestrictedNames.isRestricted(opath.getName())) {
                response.setStatus("error");
                response.setError("path_no_found");
                response.setMsg("la rruta no existe " + Util.getPathWithoutRootPath(opath.toString()));
                response.setPath(Paths.get(Util.getPathWithoutRootPath(opath.toString())));

                log.error("	file dont exists {}", opath);
                log.info("Fin de operacion de listado");
                return response;
            }

            FileStatus fileStatus = fs.getFileLinkStatus(opath);
            Status status = new Status();
            if(shareInfo){status.setShared(shared.isShared(args.getPath()));}
            status.setName(fileStatus.getPath().getName());
            status.setPath(Paths.get(Util.getPathWithoutRootPath(fileStatus.getPath().toString())));
            status.setMime(Files.probeContentType(Paths.get(fileStatus.getPath().getName())));
            status.setFile(fileStatus.isFile());
            status.setReplication(Long.valueOf(fileStatus.getReplication()));
            status.setPermission(fileStatus.getPermission() + "");
            status.setModificacionTime(fileStatus.getModificationTime());
            status.setAccessTime(fileStatus.getAccessTime());
            status.setSize(fileStatus.getLen());
            log.debug("{}", fileStatus.getPath());

            if (fs.isDirectory(opath)) {
                ContentSummary contentSumary = fs.getContentSummary(fileStatus.getPath());
                status.setDirectoryCount(contentSumary.getDirectoryCount());
                status.setSize(contentSumary.getLength());
                status.setElements(Long.valueOf(fs.listStatus(fileStatus.getPath()).length));
                status.setFileCount(contentSumary.getFileCount());
                status.setSpaceQuota(contentSumary.getSpaceQuota());
            }

            response.setPath(Paths.get(path));
            if (fs.isFile(opath)) {
                response.setFile(true);
                response.setSize(status.getSize());
            } else {
                response.setFile(false);

            }
            response.setShared(thisIsShared);
            response.setPayload(status);
            response.setStatus("ok");

            log.info("Fin de operacion de estatus de archivo {}", opath.toString());
        } catch (IllegalArgumentException | IOException e) {
            response.setStatus("error");
            response.setError("server");
            response.setMsg(e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

}
