package orchi.HHCloud.Api.Fs;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.slf4j.*;

import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.Api.API;
import orchi.HHCloud.Api.ServiceTaskAPIImpl;
import orchi.HHCloud.Api.Fs.operations.OperationsManager;
import orchi.HHCloud.Api.annotations.Operation;

@Operation(name = "list", isRequired = true)
@Operation(name = "getstatus", isRequired = true)
@Operation(name = "mkdir", isRequired = true)
@Operation(name = "delete", isRequired = true)
@Operation(name = "move", isRequired = true)
@Operation(name = "copy", isRequired = true)
@Operation(name = "rename", isRequired = true)
@Operation(name = "download", isRequired = true)
public class Fs extends API {

	private static Logger log = org.slf4j.LoggerFactory.getLogger(Fs.class);
	public static String apiName = "/fs";
	private static final long serialVersionUID = -7283584531584394004L;
	private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
	private ThreadPoolExecutor executor;

	public static String getRoot() {
		return "david";
	}

	public static String getRoot(String user) {
		return user;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

		executor = new ThreadPoolExecutor(10000, 10000, 50000L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(100000));

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//executor.execute(new Task(req.startAsync()));
		CompletableFuture.runAsync(new Task(req.startAsync()),executor);
		
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//executor.execute(new Task(req.startAsync()));
		 CompletableFuture.runAsync(new Task(req.startAsync()),executor);
	}

	public static class Task extends ServiceTaskAPIImpl implements Runnable {

		public Task(AsyncContext ctx) {
			super(ctx);
			getCtx().setTimeout(Long.MAX_VALUE);
		}

		@Override
		public void run() {
			
			HttpServletRequest reqs = (HttpServletRequest) getCtx().getRequest();
			HttpServletResponse resps = (HttpServletResponse) getCtx().getResponse();
			HttpSession session = reqs.getSession(false);
			resps.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
			//resps.setHeader("Content-type", "application/json");
			resps.setHeader("Access-Control-Allow-Credentials", "true");

			// ParseParamsMultiPart2 p = (ParseParamsMultiPart2)
			// reqs.getAttribute("params");
			JSONObject JsonArgs = null;
			ParseParamsMultiPart2 p = null;
			try {
				p = new ParseParamsMultiPart2(reqs);
				checkAvailability(apiName, p.getString("op"));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			try {
				JsonArgs = new JSONObject(p.getString("args"));
			} catch (Exception e1) {
				try {
					sendError("server_error", e1);
				} catch (Exception e) {
					getCtx().complete();
					e.printStackTrace();
					return;
				}
				e1.printStackTrace();
				return;

			}

			if (session != null) {
				JsonArgs.put("rootSession", getRoot((String) session.getAttribute("uid")));
				JsonArgs.put("root", getRoot((String) session.getAttribute("uid")));
			} else {
				JsonArgs.put("rootSession", getRoot("1234"));
				JsonArgs.put("root", getRoot("1234"));
			}

			JsonArgs.put("op", p.getString("op"));

			OperationsManager.getInstance().processOperation(getCtx(), JsonArgs);

		}

	}

}
