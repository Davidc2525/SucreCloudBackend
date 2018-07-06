package orchi.SucreCloud.operations;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class OperationsManager {
	
	private static OperationsManager instance;
	private static List<Class< ? extends IOperation>> opsDefault = Arrays.asList(ListOperation.class);

	
	
	public OperationsManager(){
		
	}
	
	public JSONObject processOperation(AsyncContext ctx, JSONObject arg){
		String contentType = "application/json";
		JSONObject response = null;
		String operation = arg.getString("op");
		
		if(Operation.LIST.equalsName(operation) ){
			 response = new ListOperation(arg).call();
		}
		if(Operation.GETSTAT.equalsName(operation) ){
			 response = new GetStatOperation(arg).call();
		}
		if(Operation.COPY.equalsName(operation) ){}
		if(Operation.DOWNLOAD.equalsName(operation) ){
			contentType = "application/octet-stream";			
			new DownloadOperation(ctx,arg);
			return null;
		}
		if(Operation.MKDIR.equalsName(operation) ){}
		if(Operation.MOVE.equalsName(operation) ){}
		if(Operation.PUT.equalsName(operation) ){}
		
		try {
			ctx.getResponse().setContentType(contentType);
			ctx.getResponse().getWriter().println(response.toString(2));
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ctx.complete();
		return response;
	}
	
	
	public static OperationsManager getInstance(){
			
		if(instance == null){
			instance = new OperationsManager();
		}
		return instance;
		
	}
}
