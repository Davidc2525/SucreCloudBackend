package orchi.HHCloud.Api;

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
import org.json.JSONObject;
import org.mortbay.log.Log;

import orchi.HHCloud.Start;
import orchi.HHCloud.Util;
import orchi.HHCloud.auth.Exceptions.VerifyException;

public class Auth extends HttpServlet {

	private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
	private ThreadPoolExecutor executorw2;
	private static ObjectMapper om;

	public Auth() {
		executorw2 = new ThreadPoolExecutor(10, 100, 50000L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(100));
		om = new ObjectMapper();
		om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		om.getJsonFactory();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

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

	public static class Task implements Runnable {

		private AsyncContext ctx;

		public Task(AsyncContext ctx) {
			this.ctx = ctx;
		}

		public void writeResponse(JsonResponse responseContent) {
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
			JSONObject jsonArgs = Util.parseParams(req);
			Log.info("{}", jsonArgs.toString(2));

			String op = jsonArgs.has("op") ? jsonArgs.getString("op") : "none";
			
			switch (op) {
			case "login":
				ctx.dispatch("/login");
				break;
			case "logout":
				ctx.dispatch("/logout");
				break;

			case "verifyemail":
				String idVeryfy = jsonArgs.has("token") ? jsonArgs.getString("token") : null;
				if (idVeryfy == null) {
					writeResponse(new JsonResponse(false, "No se ha resivido ningun token")
							.setError("token_missing")
							.setStatus("error"));
					return;
				}

				try {
					Start.getAuthProvider().verifyEmail(idVeryfy);
					writeResponse(new JsonResponse(true, "email verificado"));
				} catch (VerifyException e) {

					writeResponse(
							new JsonResponse(false, e.getMessage())
							.setError("verify_exception")
							.setStatus("error"));

					e.printStackTrace();
				}
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
}
