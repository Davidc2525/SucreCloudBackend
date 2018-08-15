package orchi.HHCloud.Api.Uploader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.hadoop.fs.Path;
import org.json.JSONException;
import org.json.JSONObject;

import orchi.HHCloud.Start;
import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.annotations.Ignore;
import orchi.HHCloud.Api.annotations.SessionRequired;
import orchi.HHCloud.stores.HdfsStore.HdfsManager;

@Ignore
@SessionRequired
public class Uploader extends API {
	public static String apiName = "/uploader";
	private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
	private ThreadPoolExecutor executor;

	@Override
	public void init(ServletConfig config) throws ServletException {
		executor = new ThreadPoolExecutor(10000, 10000, 50000L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(100000));

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		executor.execute(new Task(req.startAsync()));
	}

	public static class Task extends ServiceTaskAPIImpl implements Runnable {

		public Task(AsyncContext ctx) {
			super(ctx);

		}

		private void proceess() {

			HttpServletRequest req = (HttpServletRequest) getCtx().getRequest();
			HttpServletResponse resp = (HttpServletResponse) getCtx().getResponse();
			boolean isMultipart = ServletFileUpload.isMultipartContent(req);

			resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
			resp.setHeader("Content-type", "application/json");

			// crear un nuevo manejador de subida
			ServletFileUpload upload = new ServletFileUpload();

			// parse request
			try {
				checkAvailability(apiName, null);
				FileItemIterator iter = upload.getItemIterator(req);
				Process process = new Process();
				while (iter.hasNext()) {
					FileItemStream item = iter.next();
					String name = item.getFieldName();
					String path = item.getName();
					InputStream stream = item.openStream();

					if (item.isFormField()) {
						if (name.equalsIgnoreCase("args")) {
							JSONObject args = new JSONObject(Streams.asString(stream));
							process.setPathArgs(args.getString("path"));
						}
						System.out.println("Form field " + name + " with value " + " detected.");
					} else {
						if (name.equalsIgnoreCase("f")) {
							System.out
									.println("File field " + name + " with file name " + item.getName() + " detected.");
							process.toStore(path, stream, req);
						}

					}
				}
				getCtx().complete();
			} catch (Exception e) {
				try {
					sendError("server_error", e);
					getCtx().complete();
				} catch (Exception e1) {
					getCtx().complete();
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			proceess();
		}

	}

	/**
	 * clase para proceso de subida, en el proceso de parseo, los parametros,
	 * args,op,f, se leen en ese order, pudiendo haci crear un proceso de
	 * armado, donde primero se obtiene de args la propiedad path, q es donde se
	 * va ha almacenar dicho archivo, luego de tener el path de args, se llama
	 * el metodo, toStore q con pathArgs y el path parasado por parametro se
	 * arma la rruta de destino completa,
	 * 
	 * <pre>
	 * pathArgs = rruta donde se encuentra el usuario, /mi musica
	 * path = nombre del archivo, junto con la rruta relativa, artistas/coldplay/albums/parachute/trouble.mp3
	 *
	 * juntas = /mi musica/artistas/coldplay/albums/parachute/trouble.mp3
	 *
	 * teniendo esa rruta, se crea una nueva rruta de hdfs y se obtiene la rruta completa, con el contexto de almacenamiento de el usuario
	 * quedando = /mi_dfs/${user_id}/files/mi musica/artistas/coldplay/albums/parachute/trouble.mp3
	 * q sera la rruta a usarse para pasar a el metodo create de el proveedor de almacenamiento.
	 * </pre>
	 */
	private static class Process {
		private String pathArgs;
		private String pathFilename;

		public void toStore(String path, InputStream in, HttpServletRequest req) {
			HttpSession session = req.getSession(false);

			if (session != null) {
				java.nio.file.Path p = Paths.get(pathArgs, path);
				String root = (String) session.getAttribute("uid");
				Path opath = new Path(HdfsManager.newPath(root, p.toString()).toString());
				Start.getStoreManager().getStoreProvider().create(Paths.get(opath.toString()), in);
			}
		}

		public String getPathArgs() {
			return pathArgs;
		}

		public void setPathArgs(String pathArgs) {
			this.pathArgs = pathArgs;
		}

		public String getPathFilename() {
			return pathFilename;
		}

		public void setPathFilename(String pathFilename) {
			this.pathFilename = pathFilename;
		}

	}
}
