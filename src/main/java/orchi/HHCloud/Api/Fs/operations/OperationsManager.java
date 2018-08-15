package orchi.HHCloud.Api.Fs.operations;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import orchi.HHCloud.Start;
import orchi.HHCloud.store.Store;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.store.arguments.DeleteArguments;
import orchi.HHCloud.store.arguments.DownloadArguments;
import orchi.HHCloud.store.arguments.GetStatusArguments;
import orchi.HHCloud.store.arguments.ListArguments;
import orchi.HHCloud.store.arguments.MkDirArguments;
import orchi.HHCloud.store.arguments.MoveOrCopyArguments;
import orchi.HHCloud.store.arguments.RenameArguments;
import orchi.HHCloud.store.response.Response;
import orchi.HHCloud.user.BasicUser;

/**
 * @author david
 *
 *         Se encarga de manejar las operaciones de la api Fs
 */
public class OperationsManager {

	/**
	 * Instancia de la clase, es una clase singLeton
	 */
	private static OperationsManager instance;

	// private static List<Class<? extends IOperation>> opsDefault =
	// Arrays.asList(ListOperation.class);

	/** tipo de contenido de la respuesta */
	private String contentType;

	private Store store;// FIXME elminar implementacion

	private StoreProvider sp;

	private ObjectMapper om;

	public OperationsManager() {
		//store = Start.getStoreManager().getStoreProvider();
		setContentType("application/json");
		sp = Start.getStoreManager().getStoreProvider();

		om = new ObjectMapper();
		om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		om.getJsonFactory();
	}

	/**
	 *
	 * metodo para procesar la operacion debuelve un {@link JSONObject} de la
	 * respuesta <br/>
	 * para la operacin de descarga (download), hace uso del contexto directo
	 * para enviar el archivo por el OutPutStream de la respuesta. <br/>
	 * la cabesera de respuesta es 'application/json' por defecto, para
	 * descargar se cambia a 'application/octet-stream'
	 *
	 * @param ctx
	 *            {@link AsyncContext} contexto asincrono de la peticion actual
	 * @param arg
	 *            {@link JSONObject} argumento json con la peticion al api
	 * @author david
	 * @param params
	 */
	public JSONObject processOperation(AsyncContext ctx, JSONObject arg) {
		setContentType("application/json");
		HttpServletRequest r = ((HttpServletRequest) ctx.getRequest());

		JSONObject response = null;
		String operation = arg.getString("op");
		Response res = null;
		BasicUser u;
		try {
			/** Operacion de listar directorio */
			if (Operation.LIST.equalsName(operation)) {
				// response = store.ls(arg);

				ListArguments listArgs = new ListArguments();
				listArgs.setPath(Paths.get(arg.getString("path")));
				u = new BasicUser();
				u.setId(arg.getString("root"));
				listArgs.setUser(u);
				res = sp.list(listArgs);
				// response = new ListOperation(arg).call();
			}
			/** Operacion de obtener informacion de un archivo */
			if (Operation.GETSTATUS.equalsName(operation)) {// multiple
				// response = store.status(arg);
				arg.remove("op");
				GetStatusArguments getArgs = new GetStatusArguments();
				u = new BasicUser();
				u.setId(arg.getString("root"));
				getArgs.setPath(Paths.get(arg.getString("path")));
				getArgs.setUser(u);
				if (arg.has("paths")) {
					if (!arg.isNull("paths")) {
						List<Path> paths = new ArrayList<>();

						arg.getJSONArray("paths").toList().forEach(item -> {
							paths.add(Paths.get(item + ""));
						});

						getArgs.setPaths(paths.size() > 0 ? paths : null);
					}

				}
				res = sp.status(getArgs);
				// response = new GetStatusOperation(arg).call();
			}
			if (Operation.RENAME.equalsName(operation)) {// TODO hacer multiple
				// response = store.rename(arg);
				// response = new RenameOperation(arg).call();
				RenameArguments renameArgs = new RenameArguments(Paths.get(arg.getString("srcPath")),
						Paths.get(arg.getString("dstPath")));
				u = new BasicUser();
				u.setId(arg.getString("root"));
				renameArgs.setUser(u);
				res = sp.rename(renameArgs);
			}
			/** Operacion para copiar de una rruta a otra */
			if (Operation.COPY.equalsName(operation)) {// TODO hacer multiple
				// response = store.copy(arg);
				// response = new MoveOrCopyOperation(arg,false).call();
				MoveOrCopyArguments copyArgs = new MoveOrCopyArguments();
				if (arg.has("stcPaths")) {
					// movido o cuapiado multiple
				}
				copyArgs.setSrcPath(Paths.get(arg.getString("srcPath")));
				copyArgs.setDstPath(Paths.get(arg.getString("dstPath")));
				u = new BasicUser();
				u.setId(arg.getString("root"));
				copyArgs.setUser(u);
				res = sp.copy(copyArgs);
			}
			/** operacion para mover de una rruta a otra */
			if (Operation.MOVE.equalsName(operation)) {// TODO hacer multiple
				// response = store.move(arg);
				MoveOrCopyArguments copyArgs = new MoveOrCopyArguments();
				if (arg.has("stcPaths")) {
					// movido o cuapiado multiple
				}
				copyArgs.setSrcPath(Paths.get(arg.getString("srcPath")));
				copyArgs.setDstPath(Paths.get(arg.getString("dstPath")));
				u = new BasicUser();
				u.setId(arg.getString("root"));
				copyArgs.setUser(u);
				res = sp.move(copyArgs);
				// response = new MoveOrCopyOperation(arg,true).call();
			}
			/**
			 * Operacion para descargar un archivo o carpeta en formato ZIP,
			 * cambia en contentype a 'application/octet-stream'
			 */
			if (Operation.DOWNLOAD.equalsName(operation)) {// mutiple
				setContentType("application/octet-stream");
				((HttpServletResponse)ctx.getResponse()).setHeader("Content-type",getContentType());
				DownloadArguments downArgs = new DownloadArguments();
				u = new BasicUser();
				u.setId(arg.getString("root"));
				downArgs.setUser(u);
				downArgs.setCtx(ctx);
				downArgs.setPath(Paths.get(arg.getString("path")));
				if (arg.has("paths")) {
					if (!arg.isNull("paths")) {
						List<Path> paths = new ArrayList<>();
						;
						arg.getJSONArray("paths").toList().forEach(item -> {
							paths.add(Paths.get(item + ""));
						});

						downArgs.setPaths(paths.size() > 0 ? paths : null);
					}
				}
				sp.download(downArgs);

				// store.download(ctx, arg);
				// new DownloadOperation(ctx, arg);
				return null;
			}
			/** Operacin para crear directorio */
			if (Operation.MKDIR.equalsName(operation)) {// no creo q sea
														// necesario hacerlo
														// multiple
				u = new BasicUser();
				u.setId(arg.getString("root"));
				MkDirArguments mkdirArgs = new MkDirArguments();
				mkdirArgs.setUser(u);
				mkdirArgs.setPath(Paths.get(arg.getString("path")));
				res = sp.mkdir(mkdirArgs);
				// response = store.mkdir(arg);
				// response = new CreateDirectoryOperation(arg).call();
			}

			/** operacion para eliminar de una rruta a otra */
			if (Operation.DELETE.equalsName(operation)) {// multiple
				u = new BasicUser();
				u.setId(arg.getString("root"));
				DeleteArguments deleteArgs = new DeleteArguments();
				deleteArgs.setUser(u);
				deleteArgs.setPath(Paths.get(arg.getString("path")));
				if (arg.has("paths")) {
					if(!arg.isNull("paths")){
						List<Path> paths = new ArrayList<>();

						arg.getJSONArray("paths").toList().forEach(item -> {
							paths.add(Paths.get(item + ""));
						});

						deleteArgs.setPaths(paths.size() > 0 ? paths : null);
					}
				}
				res = sp.delete(deleteArgs);
				// response = store.delete(arg);
				// response = new DeleteOperation(ctx, arg).call();
			}
			/** operacion pendiente por validad */
			if (Operation.PUT.equalsName(operation)) {
				if (r.getMethod().equalsIgnoreCase("get")) {
					return new JSONObject().put("error", "invalid method");
				}
			}

			((HttpServletResponse)ctx.getResponse()).setHeader("Content-type",getContentType());
			ctx.getResponse().getWriter().println(om.writeValueAsString(res));
			ctx.complete();
			response = null;
			arg = null;
			// params = null;
		} catch (Exception e) {
			ctx.getResponse().setContentType(getContentType());
			try {
				ctx.getResponse().getWriter().println(new JSONObject().put("status", "error")
						.put("error", "server_error").put("msg", e.getMessage()));
				ctx.complete();
			} catch (IOException e1) {
				ctx.complete();
				e1.printStackTrace();
			} 
			e.printStackTrace();
		}
		//ctx.complete();
		return response;
	}

	/** Obtener instancia de clase, si no tiene, la crea */
	public static OperationsManager getInstance() {

		if (instance == null) {
			instance = new OperationsManager();
		}
		return instance;

	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
