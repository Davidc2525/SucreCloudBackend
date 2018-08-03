package orchi.SucreCloud.RestApi;

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

import orchi.SucreCloud.Start;

public class Logout extends HttpServlet {
	


	private ThreadPoolExecutor executorw2;
	private static ObjectMapper om;

	public Logout(){
		executorw2 = new ThreadPoolExecutor(10, 100, 50000L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(100));
		om = new ObjectMapper();
		om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		om.getJsonFactory();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		executorw2.execute(new Task(req.startAsync()));
		System.err.println("tarea en proceso "+Thread.currentThread());
		

	}
	
	public static class JsonResponse {
		private String status = "ok";
		

		private Boolean logout = false;
		private String msg = "N/A";
		
		public JsonResponse(Boolean logout,String msg){
			this.logout = logout;
			this.msg = msg;
		}
		
		public Boolean getLogout() {
			return logout;
		}
		
		public String getMsg(){return msg;}
		
		public String getStatus() {
			return status;
		}

		public JsonResponse setStatus(String status) {
			this.status = status;
			return this;
		}
		
	}
	
	public static class Task implements Runnable{
		
		
		private AsyncContext ctx;
		public Task(AsyncContext ctx){
			this.ctx = ctx;
		}
		
		public void writeResponse(JsonResponse responseContent){
			try {
				
				
				((HttpServletResponse) ctx.getResponse()).setHeader("Content-type", "application/json");
				((HttpServletResponse) ctx.getResponse()).setHeader("Access-Control-Allow-Origin", "*");
				((HttpServletResponse) ctx.getResponse()).setHeader("Content-encoding", "gzip");
				
				//ctx.getResponse().getWriter().print(om.writeValueAsString(data));				
				om.writeValue(new  GzipCompressorOutputStream(ctx.getResponse().getOutputStream()), responseContent);
				ctx.complete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ctx.complete();
			}
			
		}
		
		@Override
		public void run() { 
			if(((HttpServletRequest)ctx.getRequest()).getSession(false) == null){
				writeResponse(new JsonResponse(false,"no session create before").setStatus("error"));
				return;
			}
			
			System.err.println("dentro de runnable tarea en proceso "+Thread.currentThread());
			Start.getLoginAndOut().logOutCallBack(ctx, true, ()->{
				
				System.err.println("dentro de labmda "+Thread.currentThread());
				writeResponse(new JsonResponse(true,"session close"));
				
			});
			
		}
		
	}
}