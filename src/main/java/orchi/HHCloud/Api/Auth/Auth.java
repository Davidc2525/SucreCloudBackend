package orchi.HHCloud.Api.Auth;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.log.Log;

import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.Util;
import orchi.HHCloud.Api.annotations.Operation;
import orchi.HHCloud.Api.annotations.SessionRequired;
import orchi.HHCloud.auth.Exceptions.VerifyException;


public class Auth extends HttpServlet {

	
	
	private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
	private static ThreadPoolExecutor executorw2;
	private static Logout logout;
	private static Login login;
	private static ObjectMapper om;

	public Auth() {
		login = new Login();
		logout = new Logout();
		executorw2 = new ThreadPoolExecutor(10, 100, 50000L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(100));
		om = new ObjectMapper();
		om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		om.getJsonFactory();
	}

	@Override
	public void init() throws ServletException {
		login.init();
		logout.init();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doPost(req,resp);

	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		executorw2.execute(new Task(req.startAsync()));
		System.err.println("tarea en proceso " + Thread.currentThread());

	}

	public static class JsonResponse {
		private String status = "ok";
		private String error = null;
		private Boolean vrified = false;
		private String msg = "N/A";

		public JsonResponse(Boolean verified, String msg) {
			this.setVrified(verified);
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}

		public String getStatus() {
			return status;
		}

		public JsonResponse setStatus(String status) {
			this.status = status;
			return this;
		}

		public String getError() {
			return error;
		}

		public JsonResponse setError(String error) {
			this.error = error;
			return this;
		}

		public Boolean getVrified() {
			return vrified;
		}

		public void setVrified(Boolean vrified) {
			this.vrified = vrified;
		}

	}
	public Task getTask(){
		return new Task(null);
	}

	public static class Task implements Runnable {

		private static AsyncContext ctx;

		public Task(AsyncContext ctx) {
			this.ctx = ctx;
		}

		public static void writeResponse(JsonResponse responseContent) {
			try {

				((HttpServletResponse) ctx.getResponse()).setHeader("Access-Control-Allow-Origin",
						ACCESS_CONTROL_ALLOW_ORIGIN);
				((HttpServletResponse) ctx.getResponse()).setHeader("Content-type", "application/json");
				((HttpServletResponse) ctx.getResponse()).setHeader("Access-Control-Allow-Origin", "*");
				((HttpServletResponse) ctx.getResponse()).setHeader("Content-encoding", "gzip");

				// ctx.getResponse().getWriter().print(om.writeValueAsString(data));
				om.writeValue(new GzipCompressorOutputStream(ctx.getResponse().getOutputStream()), responseContent);
				ctx.complete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ctx.complete();
			}

		}

		@Override
		public void run() {

			HttpServletRequest req = ((HttpServletRequest) ctx.getRequest());
			HttpServletResponse resp = ((HttpServletResponse) ctx.getResponse());
			ParseParamsMultiPart2  p =(ParseParamsMultiPart2) req.getAttribute("params");

			JSONObject jsonArgs = null;;
			try {
				jsonArgs = new JSONObject(p.getString("args"));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			Log.info("{}", jsonArgs.toString(2));

			String op = null;
			op = p.getString("op");
			op = op != null ? op :"none";
			
			switch (op) {
			case "login":
				;
				executorw2.execute(new orchi.HHCloud.Api.Auth.Login.Task(ctx));
				
				break;
			case "logout":
				executorw2.execute(new orchi.HHCloud.Api.Auth.Logout.Task(ctx));
				break;

			case "verifyemail":
				verifyEmailOperation(jsonArgs);				
				break;

			default:
				writeResponse(
						new JsonResponse(false, "debe espesificar una operacion")
						.setError("operation_missing")
						.setStatus("error"));
				break;
			}

		}

		
	}
	@Operation(name = "login")
	private static void loginOperation(JSONObject jsonArgs){
		
	}
	
	@Operation(name = "logout")
	@SessionRequired
	private static void logoutOperation(JSONObject jsonArgs){
		
	}
	
	@Operation(name = "verifyemail")
	private static void verifyEmailOperation(JSONObject jsonArgs) {
		String idVeryfy = jsonArgs.has("token") ? jsonArgs.getString("token") : null;
		if (idVeryfy == null) {
			Task.writeResponse(new JsonResponse(false, "No se ha resivido ningun token")
					.setError("token_missing")
					.setStatus("error"));
			return;
		}

		try {
			Start.getAuthProvider().verifyEmail(idVeryfy);
			Task.writeResponse(new JsonResponse(true, "email verificado"));
		} catch (VerifyException e) {

			Task.writeResponse(
					new JsonResponse(false, e.getMessage())
					.setError("verify_exception")
					.setStatus("error"));

			e.printStackTrace();
		}
	}
	
}
