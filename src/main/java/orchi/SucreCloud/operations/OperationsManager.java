package orchi.SucreCloud.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.json.JSONException;
import org.json.JSONObject;

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

	private static List<Class<? extends IOperation>> opsDefault = Arrays.asList(ListOperation.class);

	/** typo de contenido de la respuesta */
	private String contentType;

	public OperationsManager() {

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
	 */
	public JSONObject processOperation(AsyncContext ctx, JSONObject arg) {
		setContentType("application/json");
		HttpServletRequest r = ((HttpServletRequest) ctx.getRequest());

		
		JSONObject response = null;
		String operation = arg.getString("op");

		/**Operacion de listar directorio*/
		if (Operation.LIST.equalsName(operation)) {
			response = new ListOperation(arg).call();
		}
		/**Operacion de obtener informacion de un archivo*/
		if (Operation.GETSTATUS.equalsName(operation)) {
			response = new GetStatusOperation(arg).call();
		}
		/**Operacion para copiar de una rruta a otra*/
		if (Operation.COPY.equalsName(operation)) {
		}

		/**Operacion para descargar un archivo o carpeta en formato ZIP, cambia en contentype a 
		 * 'application/octet-stream'*/
		if (Operation.DOWNLOAD.equalsName(operation)) {
			setContentType("application/octet-stream");
			new DownloadOperation(ctx, arg);
			return null;
		}
		/**Operacin para crear directorio*/
		if (Operation.MKDIR.equalsName(operation)) {
		}
		/**operacion para mover de una rruta a otra*/
		if (Operation.MOVE.equalsName(operation)) {
		}
		/**operacion para eliminar de una rruta a otra*/
		if (Operation.DELETE.equalsName(operation)) {
			response = new DeleteOperation(ctx, arg).call();
		}
		/**operacion pendiente por validad*/
		if (Operation.PUT.equalsName(operation)) {
			if (r.getMethod().equalsIgnoreCase("get")) {
				return new JSONObject().put("error", "invalid method");
			}
			try {
				response = new UploadOperation(ctx,arg).call();
			} catch (FileUploadException | IOException e) {
				e.printStackTrace();
			}
		} 

		try {
			ctx.getResponse().setContentType(getContentType());
			ctx.getResponse().getWriter().println(response.toString());
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
