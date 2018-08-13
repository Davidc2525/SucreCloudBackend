package orchi.HHCloud.stores.HdfsStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Util;
import orchi.HHCloud.store.Status;
import orchi.HHCloud.store.arguments.GetStatusArguments;
import orchi.HHCloud.store.arguments.ListArguments;
import orchi.HHCloud.store.response.GetStatusResponse;
import orchi.HHCloud.store.response.ListResponse;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;

public class ListOperation {
	private static Logger log = LoggerFactory.getLogger(ListOperation.class);
	private FileSystem fs;
	private String root;

	private Path opath;
	private String path;
	private ListResponse response;
	private ListArguments args;

	public ListOperation(ListArguments args) {
		this.args = args;
		response = new ListResponse();
		fs = HdfsManager.getInstance(true).fs;
		root = args.getUserId();
		path = args.getPath().toString();
		opath = new Path(HdfsManager.newPath(root, path).toString());

		log.info("Nueva operacion de listado {}", opath.toString());

	}

	public ListResponse call() {

		try {
			if (!fs.exists(opath)) {
				response.setStatus("error");
				response.setError("path_no_found");
				response.setMsg("la rruta no existe " + Util.getPathWithoutRootPath(opath.toString()));

				log.error("	file dont exists {} ", opath);
				log.info("Fin de operacion de listado");
				return response;
			}
			if (fs.isDirectory(opath)) {
				List<FileStatus> ls = Arrays
						.asList(fs.listStatus(new Path(HdfsManager.newPath(root, path).toString())));

				List<Status> statues = new ArrayList<>();

				ls.stream().forEach(x -> {

					try {
						Status status = new Status();
						status.setName(x.getPath().getName());
						status.setPath(Paths.get(Util.getPathWithoutRootPath(x.getPath().toString())));
						status.setMime(Files.probeContentType(Paths.get(x.getPath().getName())));
						status.setFile(x.isFile());
						status.setReplication(Long.valueOf(x.getReplication()));
						status.setPermission(x.getPermission() + "");
						status.setModificacionTime(x.getModificationTime());

						log.debug("\t{} in path", x.getPath().getName());

						if (x.isFile()) {
							status.setSize(x.getLen());
						} else {
							ContentSummary contentSumary = fs.getContentSummary(x.getPath());
							status.setDirectoryCount(contentSumary.getDirectoryCount());
							status.setSize(contentSumary.getLength());
							status.setElements(Long.valueOf(fs.listStatus(x.getPath()).length));

							contentSumary = null;
						}

						statues.add(status);
						status = null;

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});

				ContentSummary contentSumary = fs.getContentSummary(opath);

				response.setPayload(statues);
				response.setPath(Paths.get(path));
				response.setSize(contentSumary.getLength());
				response.setDirectoryCount(contentSumary.getDirectoryCount());
				response.setFileCount(contentSumary.getFileCount());
				response.setFile(false);
				response.setStatus("ok");

				log.info("Fin de operacion de listado");
			} else if (fs.isFile(new Path(HdfsManager.newPath(root, path).toString()))) {
				log.warn("transfer operation, get status file", opath);
				GetStatusArguments gsa = new GetStatusArguments();
				gsa.setPath(Paths.get(path));
				gsa.setUser(args.getUse());
				GetStatusResponse gs = new GetStatusOperation(gsa).call();
				ListResponse lr = new ListResponse();
				lr.setStatus(gs.getStatus());
				lr.setError(gs.getError());
				lr.setMsg(gs.getMsg());
				lr.setMultiple(gs.isMultiple());
				lr.setPayload(gs.getPayload());
				lr.setPath(gs.getPath());
				lr.setFile(gs.isFile());
				lr.setSize(gs.getSize());
				
				return lr;// new GetStatusOperation(args).call();
			}

		} catch (IllegalArgumentException | IOException e) {
			response.setStatus("error");
			response.setError("server");
			response.setMsg(e.getMessage());
			e.printStackTrace();
		}

		return response;
	}

}
