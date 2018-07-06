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

import orchi.SucreCloud.hdfs.HdfsManager;

public class App extends HttpServlet {

	private static String root = "/mi_dfs/david";
	private ThreadPoolExecutor executor;

	@Override
	public void init(ServletConfig config) throws ServletException {

		executor = new ThreadPoolExecutor(1000, 100000, 50000L, TimeUnit.MILLISECONDS,
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
			this.json = new JSONObject();

		}

		@Override
		public void run() {
			FileSystem fs = HdfsManager.getInstance().fs;

			HttpServletRequest reqs = (HttpServletRequest) ctx.getRequest();
			HttpServletResponse resps = (HttpServletResponse) ctx.getResponse();

			String path = reqs.getParameter("path");
			String operation = reqs.getParameter("op");
			List<FileStatus> ls = null;
			List<String> lsm = null;
			final JSONObject  lsJson = new JSONObject();;

			try {
				ls = Arrays.asList(fs.listStatus(new Path(HdfsManager.newPath(root, path).toString())));
				//lsJson = new JSONObject();
				ls.stream().forEach(x->{
					lsJson.put(x.getPath().getName(),new JSONObject(x).put("path",nc(x.getPath().toString())));
				});
			} catch (IllegalArgumentException | IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				resps.getWriter().println(json
						.put("path", HdfsManager.newPath(root, path))
						.put("ls", lsJson)
						.put("operation", operation).toString(2));
				
				ctx.complete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}

	}
	private static String type(FileStatus i) {
		// TODO Auto-generated method stub
		return i.isFile() ? "file":"folder";
	}
	public static String nc(String in) {
		java.nio.file.Path p = Paths.get(in);

		p = p.subpath(4, p.getNameCount());
		return Paths.get(p + "").normalize() + "";
	}
}
