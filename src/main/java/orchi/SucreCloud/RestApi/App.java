package orchi.SucreCloud.RestApi;

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

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.fs.FileSystem;
import org.json.JSONObject;
import org.mortbay.log.Log;
import orchi.SucreCloud.ParseParamsMultiPart;
import orchi.SucreCloud.hdfs.HdfsManager;
import orchi.SucreCloud.operations.OperationsManager;

public class App extends HttpServlet {

	private static String root = "/mi_dfs/";
	private ThreadPoolExecutor executor;

	public static String getRoot(){
		return root+"david";
	}
	public static String getRoot(String user){
		return root+user;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {

		executor = new ThreadPoolExecutor(10, 1000, 50000L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(100000));

	}

    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	req.getSession(true);
		doPost(req,resp);
	}
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getSession(true);		
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
			FileSystem fs = HdfsManager.getInstance().fs;

			HttpServletRequest reqs = (HttpServletRequest) ctx.getRequest();
			HttpServletResponse resps = (HttpServletResponse) ctx.getResponse();
			resps.addHeader("Access-Control-Allow-Origin", "http://localhost:9090");
			resps.addHeader("Access-Control-Allow-Credentials", "true");

			ParseParamsMultiPart params = null;
			try {
				params = new ParseParamsMultiPart(reqs);
			} catch (Exception e1) {
				// TODO Auto-generated catch block

				/*try {
			        ctx.getResponse().setContentType("application/json");
				    ctx.getResponse().getWriter().println( new JSONObject().put("error","invalid_method").put("status","error").put("errorMsg","nada"));
				    ctx.complete();
		        } catch (JSONException | IOException e) {

			        e.printStackTrace();
		        }*/
				e1.printStackTrace();
			}
			String args = null;
			if (reqs.getMethod().equalsIgnoreCase("post")) {
				try {
					args = params.getAsString("args");
				} catch (IOException | NullPointerException e) {

					e.printStackTrace();
				}
			} else if (reqs.getMethod().equalsIgnoreCase("get")) {
				args = new String(Base64.decodeBase64(reqs.getParameter("args")));
			}
			//reqs.getParameter("args");

			//Log.info("params {}",params);

			//String args = reqs.getParameter("args");

			Log.info("{}",args);
			JSONObject JsonArgs = new  JSONObject(args);


			String user = null;
			if(JsonArgs.has("user")){
				user = JsonArgs.getString("user");
			}

			if(user== null){
				JsonArgs.put("root", getRoot("david"));
			}else{
				JsonArgs.put("root", getRoot(user));
			}
			String path = JsonArgs.getString("path");
			String operation = JsonArgs.getString("op");

			OperationsManager.getInstance().processOperation(ctx,JsonArgs,params);


		}

	}

}