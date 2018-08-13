package orchi.HHCloud.Api.Fs;

import java.io.IOException;

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

import org.json.JSONException;
import org.json.JSONObject;
import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.Api.Fs.operations.OperationsManager;
import orchi.HHCloud.Api.annotations.Operation;


@Operation(name = "list", isRequired = false)
@Operation(name = "getstatus", isRequired = true)
@Operation(name = "mkdir", isRequired = true)
@Operation(name = "delete", isRequired = true)
@Operation(name = "move", isRequired = true)
@Operation(name = "copy", isRequired = true)
@Operation(name = "rename", isRequired = true)
@Operation(name = "download", isRequired = true)
public class Fs extends HttpServlet {

	private static final long serialVersionUID = -7283584531584394004L;
	private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
	private ThreadPoolExecutor executor;

	public static String getRoot() {
		return  "david";
	}

	public static String getRoot(String user) {
		return  user;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

		executor = new ThreadPoolExecutor(10, 1000, 50000L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(100000));

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		executor.execute(new Task(req.startAsync()));
	}

	public static class Task implements Runnable {

		private AsyncContext ctx;
		private JSONObject json;

		public Task(AsyncContext ctx) {

			this.ctx = ctx;
			this.ctx.setTimeout(Long.MAX_VALUE);
			this.json = new JSONObject();

		}

		@Override
		public void run() {
			// FileSystem fs = HdfsManager.getInstance().fs;

			HttpServletRequest reqs = (HttpServletRequest) ctx.getRequest();
			HttpServletResponse resps = (HttpServletResponse) ctx.getResponse();
			HttpSession session = reqs.getSession(false);
			//resps.addHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
			//resps.addHeader("Access-Control-Allow-Credentials", "true");

			ParseParamsMultiPart2  p = (ParseParamsMultiPart2) reqs.getAttribute("params");

			JSONObject JsonArgs = null;
			try {
				JsonArgs = new JSONObject(p.getString("args"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(session!=null){
				JsonArgs.put("rootSession", getRoot((String)session.getAttribute("uid")));
				JsonArgs.put("root", getRoot((String)session.getAttribute("uid")));
			}else{
				JsonArgs.put("rootSession", getRoot("1234"));
				JsonArgs.put("root", getRoot("1234"));
			}

			String path = JsonArgs.getString("path");
			String operation = reqs.getParameter("op");
			JsonArgs.put("op",p.getString("op"));
			
			OperationsManager.getInstance().processOperation(ctx, JsonArgs);

		}

	}

}
