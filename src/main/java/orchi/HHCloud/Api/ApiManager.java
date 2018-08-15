
package orchi.HHCloud.Api;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;

import org.eclipse.jetty.servlet.ServletHolder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Api.ApiManager.ApiDescriptor;
import orchi.HHCloud.Api.annotations.Ignore;
import orchi.HHCloud.Api.annotations.SessionRequired;


/**
 * Permite llevar una recopilacion de datos de cada una de las apis, como nombre y operaciones,
 * cada api puede dar permisos de ejecucion dependiendo de la existencia de una session de usuario
 * de manera global u espesifica a cada operacion.
 * 
 * @author david*/
public class ApiManager {
	private static Logger log = LoggerFactory.getLogger(ApiManager.class);
	public static Map<String, ApiDescriptor> api = new HashMap<String, ApiDescriptor>();
	public static ApiManager instance = new ApiManager();

	public ApiManager() {
		log.debug("Iniciando api manager");
	}
	
	public static ApiDescriptor getApid(String name){
		return api.get(name);
	}
	
	public static ApiDescriptor updateDescription(String apiName, String operation) {
		ApiDescriptor apid = null;
		if (apiName != null && !apiName.equals("")) {
			apid = getApid(apiName);

			if (apid != null) {
				apid.calls++;
				if (log.isDebugEnabled()) {
					log.debug(new JSONObject(apid).toString(2));
				}

				if (operation != null) {
					if(!operation.equals("")){
						Operation op = apid.getOperation(operation);
						if(op!=null){
							op.calls++;
						}
					}
				}
				return apid;
			}
		}
		return apid;
	}
	
	public static ServletHolder addApi(Class<? extends API> clazz, String name) {

		ServletHolder context = null;
		try {
			ApiDescriptor apid = new ApiDescriptor(name);
			context = new ServletHolder((Servlet) clazz.newInstance());
			context.setAsyncSupported(true);
			log.debug("Add api: {} {}",name, context.getName());
			log.info("- {}", name);
			
			SessionRequired gsr = clazz.getDeclaredAnnotation(SessionRequired.class);
			if(gsr!=null){
				apid.setGsr(true);
			}
			
			Ignore ignore = clazz.getDeclaredAnnotation(Ignore.class);
			if(ignore!=null){
				apid.setIgnored(true);
			}
			
			orchi.HHCloud.Api.annotations.Operation[] opsInClass = clazz
					.getDeclaredAnnotationsByType(orchi.HHCloud.Api.annotations.Operation.class);
			for (orchi.HHCloud.Api.annotations.Operation o : opsInClass) {
				log.info("	|-{}", o.name());
				Operation op = new Operation();
				if (o.isRequired()) {
					log.info("	   |-session is required");
					op.sr = true;
					apid.addOperation(o.name(), op);
				}else{
					apid.addOperation(o.name(), op);
				}
				
			}
			for (Method m : clazz.getDeclaredMethods()) {
				orchi.HHCloud.Api.annotations.Operation[] ops = m
						.getDeclaredAnnotationsByType(orchi.HHCloud.Api.annotations.Operation.class);

				for (orchi.HHCloud.Api.annotations.Operation o : ops) {
					SessionRequired sr = m.getDeclaredAnnotation(orchi.HHCloud.Api.annotations.SessionRequired.class);
					log.info("	|-{}", o.name());
					Operation op = new Operation();
					op.name = o.name();

					if (sr != null) {
						log.info("	   |-session is required");
						op.sr = true;
					}
					apid.addOperation(o.name(), op);

				}
			}

			api.put(name, apid);

		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return context;
	}

	public static class Operation {

		public Long calls = 0L;
		public String name;
		public boolean sr = false;

		public Long getCalls() {
			return calls;
		}

		public void setCalls(Long calls) {
			this.calls = calls;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isSr() {
			return sr;
		}

		public void setSr(boolean sr) {
			this.sr = sr;
		}
	}

	public static class ApiDescriptor {

		public Map<String, Operation> operations = new HashMap<String, Operation>();
		public Long wrongCalls = 0l;
		public Long calls = 0l;
		public String name;
		public boolean ignored = false;
		/**
		 * requiere session en todas las operaciones, global session required
		 * */
		private boolean gsr = false;

		public ApiDescriptor(String name) {
			this.name = name;
		}

		public boolean hasOperation(String name){
			return operations.containsKey(name);
		}
			
		public void addOperation(String name, Operation op) {
			operations.put(name, op);
		}

		public Operation getOperation(String name) {
			return operations.get(name);
		}

		public Map<String, Operation> getOperations() {
			return operations;
		}

		public void setOperations(Map<String, Operation> operations) {
			this.operations = operations;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isIgnored() {
			return ignored;
		}

		public void setIgnored(boolean ignored) {
			this.ignored = ignored;
		}

		public Long getCalls() {
			return calls;
		}

		public void setCalls(Long calls) {
			this.calls = calls;
		}

		public Long getWrongCalls() {
			return wrongCalls;
		}

		public void setWrongCalls(Long wrongCalls) {
			this.wrongCalls = wrongCalls;
		}

		public boolean isGsr() {
			return gsr;
		}

		public void setGsr(boolean gsr) {
			this.gsr = gsr;
		}
	}

	public static void showDescriptions() {
		log.info("{}", new JSONObject(api).toString(2));
	}
}
