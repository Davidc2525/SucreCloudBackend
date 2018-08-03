package orchi.SucreCloud.RestApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import orchi.SucreCloud.Start;
import orchi.SucreCloud.store.Store;
import orchi.user.DataUser;
import orchi.user.User;
import orchi.user.UserProvider;
import orchi.user.UserValidator;
import orchi.user.Exceptions.EmailValidationException;
import orchi.user.Exceptions.FirstNameValidationException;
import orchi.user.Exceptions.LastNameValidationException;
import orchi.user.Exceptions.PasswordValidationException;
import orchi.user.Exceptions.UserException;
import orchi.user.Exceptions.UserNotExistException;
import orchi.user.Exceptions.UsernameValidationException;
import orchi.user.Exceptions.ValidationException;

public class Users extends HttpServlet {

	private ThreadPoolExecutor executor;
	private static UserProvider up;
	private static ObjectMapper om;
	private static Store sp;

	@Override
	public void init(ServletConfig config) throws ServletException {
		up = Start.getUserManager().getUserProvider();
		sp = Start.getStoreManager().getStoreProvider();
		om = new ObjectMapper();
		om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		om.getJsonFactory();
		executor = new ThreadPoolExecutor(10,1000000, 10L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(1000000));
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		executor.execute(new Task(req.startAsync()));

	}

	public static class JsonResponse {
		public String status = "ok";
		private String error;
		public String msg = "ok";
		public User payload;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public User getPayload() {
			return payload;
		}

		public void setPayload(User payload) {
			this.payload = payload;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

	}

	public static class OpCreateJsonResponse {
		public String status = "ok";
		private String error;
		private Map<String, String> errors = new HashMap<String, String>();
		public String msg = "ok";
		public User payload;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public User getPayload() {
			return payload;
		}

		public void setPayload(User payload) {
			this.payload = payload;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public Map<String, String> getErrors() {
			return errors;
		}

		public Map<String, String> appendError(String field, String error) {
			errors.put(field, error);
			return errors;
		}

	}

	public static class Task implements Runnable{
		private AsyncContext ctx;

		public Task(AsyncContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			try {
				process();
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void process() throws JsonGenerationException, JsonMappingException, IOException {

			HttpServletRequest req = (HttpServletRequest)ctx.getRequest();
			HttpServletResponse resp = (HttpServletResponse)ctx.getResponse();
			resp.setHeader("Access-Control-Allow-Origin", "http://localhost:9090");
			resp.setHeader("Content-type", "application/json");
			String op = req.getParameter("op");
			op = op != null ? op :"none";

			if(op.equalsIgnoreCase("none")){
				JsonResponse response = new JsonResponse();
				response.setError("op_no_espesified");
				response.setMsg("No espesifico operacion");

				resp.getWriter().println(om.writeValueAsString(response));
				ctx.complete();
				return;
			}

			if (op.equalsIgnoreCase("get")) {
				String by = req.getParameter("by");
				String identifier = req.getParameter("id");

				JsonResponse response = new JsonResponse();

				switch (by) {
				case "email":

					try {
						DataUser user = (DataUser) up.getUserByEmail(identifier);
						response.setPayload(user);
						response.setStatus("ok");

					} catch (UserNotExistException e) {

						response.setStatus("error");
						response.setError("user_not_exist");
						response.setMsg(e.getMessage());

						e.printStackTrace();
					} catch (UserException e) {

						response.setStatus("error");
						response.setMsg(e.getMessage());

						e.printStackTrace();
					}

					resp.getWriter().println(om.writeValueAsString(response));
					break;
				case "id":
					try {
						DataUser user = (DataUser) up.getUserById(identifier);

						response.setPayload(user);
						response.setStatus("ok");

					} catch (UserNotExistException e) {

						response.setStatus("error");
						response.setError("user_not_exist");
						response.setMsg(e.getMessage());

						e.printStackTrace();
					} catch (UserException e) {

						response.setStatus("error");
						response.setError("user_exception");
						response.setMsg(e.getMessage());

						e.printStackTrace();
					}

					resp.getWriter().println(om.writeValueAsString(response));
					break;

				case "username":
					try {
						DataUser user = (DataUser) up.getUserByUsername(identifier);

						response.setPayload(user);
						response.setStatus("ok");

					} catch (UserNotExistException e) {

						response.setStatus("error");
						response.setError("user_not_exist");
						response.setMsg(e.getMessage());

						e.printStackTrace();
					} catch (UserException e) {

						response.setStatus("error");
						response.setError("user_exception");
						response.setMsg(e.getMessage());

						e.printStackTrace();
					}

					resp.getWriter().println(om.writeValueAsString(response));
					break;

				default:
					break;
				}

				ctx.complete();
			}

			if (op.equalsIgnoreCase("create")) {
				boolean hasError = false;
				OpCreateJsonResponse response = new OpCreateJsonResponse();
				UserValidator validator = up.getUserValidator();
				String email = req.getParameter("email");
				String username = req.getParameter("username");
				String firstName = req.getParameter("fname");
				String lastName = req.getParameter("lname");
				String password = req.getParameter("password");

				email = email != null ? email : "";
				username = username != null ? username : "";
				firstName = firstName != null ? firstName : "";
				lastName = lastName != null ? lastName : "";
				password = password != null ? password : "";

				DataUser user = new DataUser();
				user.setId(System.currentTimeMillis() + "");
				user.setEmail(email);
				user.setUsername(username);
				user.setFirstName(firstName);
				user.setLastName(lastName);
				user.setPassword(password);
				user.setCreateAt(System.currentTimeMillis());

				try {
					validator.validateEmail(user);
				} catch (EmailValidationException maile) {

					response.appendError("email", maile.getMessage());

					hasError = true;
				} catch (ValidationException e) {
					response.appendError("email", e.getMessage());

					hasError = true;
				}

				try {
					validator.validateUsername(user);
				} catch (UsernameValidationException usernamee) {

					response.appendError("username", usernamee.getMessage());

					hasError = true;
				} catch (ValidationException e) {
					e.printStackTrace();
				}

				try {
					validator.validatePassword(user);
				} catch (PasswordValidationException passworde) {

					response.appendError("password", passworde.getMessage());

					hasError = true;
				} catch (ValidationException e) {
					e.printStackTrace();
				}

				try {
					validator.validateFirstName(user);
				} catch (FirstNameValidationException fnamee) {

					response.appendError("firstname", fnamee.getMessage());

					hasError = true;
				} catch (ValidationException e) {
					e.printStackTrace();
				}

				try {
					validator.validateLastName(user);
				} catch (LastNameValidationException lnamee) {

					response.appendError("lastname", lnamee.getMessage());

					hasError = true;
				} catch (ValidationException e) {
					e.printStackTrace();
				}

				if (!hasError) {
					try {
						sp.createStoreContextToUser(user);
						up.createUser(user);
					} catch (UserException | IOException e) {
						response.setStatus("error");
						response.setError("create_exception");
						response.setMsg(e.getMessage());

						hasError = true;
					}
				}

				if (hasError) {
					response.setStatus("error");
					if (response.getErrors().size() > 0) {
						response.setError("fields");
						response.setMsg("Los campos suministrados deben tener algun problema.");
					}
					resp.getWriter().println(om.writeValueAsString(response));

				} else {
					response.setStatus("ok");
					response.setMsg("usuario resgistrado");
					response.setPayload(user);
					resp.getWriter().println(om.writeValueAsString(response));

				}

				ctx.complete();
			}

			if(op.equalsIgnoreCase("update")){
			    //TODO
			}

		}
	}
}
