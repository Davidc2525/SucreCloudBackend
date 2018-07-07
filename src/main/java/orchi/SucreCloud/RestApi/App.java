package orchi.SucreCloud.RestApi;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.mortbay.util.ajax.JSON;

import orchi.SucreCloud.hdfs.HdfsManager;
import orchi.SucreCloud.operations.ListOperation;
import orchi.SucreCloud.operations.Operation;
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

		executor = new ThreadPoolExecutor(1000, 100000, 50000L, TimeUnit.MICROSECONDS,
				new LinkedBlockingQueue<Runnable>(100000));

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
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
			String args = reqs.getParameter("args");
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
			
			OperationsManager.getInstance().processOperation(ctx,JsonArgs);
			

		}

	}
	
}
