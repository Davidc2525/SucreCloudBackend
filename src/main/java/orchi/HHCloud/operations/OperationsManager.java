package orchi.HHCloud.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONException;
import org.json.JSONObject;

import orchi.HHCloud.ParseParamsMultiPart;
import orchi.HHCloud.Start;
import orchi.HHCloud.store.Store;

/**
 * @author david
 *
 *         Se encarga de manejar las operaciones
 */
public class OperationsManager {

	/**
	 * Instancia de la clase, es una clase singLeton
	 */
	private static OperationsManager instance;

	//private static List<Class<? extends IOperation>> opsDefault = Arrays.asList(ListOperation.class);

	/** typo de contenido de la respuesta */
	private String contentType;

	private Store store;

	public OperationsManager() {
		store = Start.getStoreManager().getStoreProvider();
		setContentType("application/json");

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
	public JSONObject processOperation(AsyncContext ctx, JSONObject arg, ParseParamsMultiPart params) {
		setContentType("application/json");
		HttpServletRequest r = ((HttpServletRequest) ctx.getRequest());


		JSONObject response = null;
		String operation = arg.getString("op");

		/**Operacion de listar directorio*/
		if (Operation.LIST.equalsName(operation)) {//TODO hacer multiple
			response = store.ls(arg);
			//response = new ListOperation(arg).call();
		}
		/**Operacion de obtener informacion de un archivo*/
		if (Operation.GETSTATUS.equalsName(operation)) {//multiple
			response = store.status(arg);
			//response = new GetStatusOperation(arg).call();
		}
		if (Operation.RENAME.equalsName(operation)) {//TODO hacer multiple
			response = store.rename(arg);
			//response = new RenameOperation(arg).call();
		}
		/**Operacion para copiar de una rruta a otra*/
		if (Operation.COPY.equalsName(operation)) {//TODO hacer multiple
			response = store.copy(arg);
			//response = new MoveOrCopyOperation(arg,false).call();
		}
		/**operacion para mover de una rruta a otra*/
		if (Operation.MOVE.equalsName(operation)) {//TODO hacer multiple
			response = store.move(arg);
			//response = new MoveOrCopyOperation(arg,true).call();
		}
		/**Operacion para descargar un archivo o carpeta en formato ZIP, cambia en contentype a
		 * 'application/octet-stream'*/
		if (Operation.DOWNLOAD.equalsName(operation)) {//mutiple
			setContentType("application/octet-stream");
			store.download(ctx,arg);
			//new DownloadOperation(ctx, arg);
			return null;
		}
		/**Operacin para crear directorio*/
		if (Operation.MKDIR.equalsName(operation)) {//no creo q sea necesario hacerlo multiple
			response = store.mkdir(arg);
			//response = new CreateDirectoryOperation(arg).call();
		}

		/**operacion para eliminar de una rruta a otra*/
		if (Operation.DELETE.equalsName(operation)) {//multiple
			response = store.delete(arg);
			//response = new DeleteOperation(ctx, arg).call();
		}
		/**operacion pendiente por validad*/
		if (Operation.PUT.equalsName(operation)) {
			if (r.getMethod().equalsIgnoreCase("get")) {
				return new JSONObject().put("error", "invalid method");
			}
			try {
				response = store.upload(ctx,arg,params);
				//response = new UploadOperation(ctx,arg,params).call();
			} catch (FileUploadException | IOException e) {
				e.printStackTrace();
			}
		}

		try {
			ctx.getResponse().setContentType(getContentType());
			ctx.getResponse().getWriter().println(response.toString(2));
			response = null;
			arg = null;
			params = null;
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ctx.complete();
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
