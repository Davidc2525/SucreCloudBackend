package orchi.HHCloud.Api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import orchi.HHCloud.Start;
import orchi.HHCloud.store.Store;
import orchi.HHCloud.user.BasicUser;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.UserValidator;
import orchi.HHCloud.user.Exceptions.EmailValidationException;
import orchi.HHCloud.user.Exceptions.FirstNameValidationException;
import orchi.HHCloud.user.Exceptions.LastNameValidationException;
import orchi.HHCloud.user.Exceptions.PasswordValidationException;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserMutatorException;
import orchi.HHCloud.user.Exceptions.UserMutatorPassword;
import orchi.HHCloud.user.Exceptions.UserNotExistException;
import orchi.HHCloud.user.Exceptions.UsernameValidationException;
import orchi.HHCloud.user.Exceptions.ValidationException;

public class Users extends HttpServlet {

	/**
	 * 
	 */
	private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");
	
	private static final long serialVersionUID = 3632921692211341012L;
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
		executor = new ThreadPoolExecutor(10, 1000000, 10L, TimeUnit.SECONDS,
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

	public static class OpUpdateJsonResponse {
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

	public static class Task implements Runnable {
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

			HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
			HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
			resp.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
			resp.setHeader("Content-type", "application/json");
			String op = req.getParameter("op");
			op = op != null ? op : "none";

			if (op.equalsIgnoreCase("none")) {
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

			if (op.equalsIgnoreCase("changepassword")) {
				JsonResponse response = new JsonResponse();
				UserValidator validator = up.getValidator();
				User user = null;
				boolean hasError = false;
				String id = req.getParameter("id");
				String nPassword = req.getParameter("password");
				nPassword = nPassword != null ? nPassword : "";
				BasicUser tmpPassUser = new BasicUser();
				tmpPassUser.setPassword(nPassword);

				try {
					validator.validatePassword(tmpPassUser);
				} catch (PasswordValidationException e) {
					hasError = true;
					response.setStatus("error");
					response.setError("password_validation");
					response.setMsg(e.getMessage());
					e.printStackTrace();
				} catch (ValidationException e1) {
					hasError = true;
					response.setStatus("error");
					response.setError("validation_exception");
					response.setMsg(e1.getMessage());
					e1.printStackTrace();
				}

				if (id != null) {
					try {
						user = up.getUserById(id);
					} catch (UserNotExistException e) {
						hasError = true;
						response.setStatus("error");
						response.setError("user_not_exist");
						response.setMsg(e.getMessage());
						e.printStackTrace();
					} catch (UserException e) {
						hasError = true;
						response.setStatus("error");
						response.setError("user_exception");
						response.setMsg(e.getMessage());
						e.printStackTrace();
					}

				} else {
					hasError = true;
					response.setStatus("error");
					response.setError("uid");
					response.setMsg("El id no puede estar vacio o ser nulo.");

				}

				if (!hasError) {
					try {
						user = up.changePasswordUser(new UserMutatorPassword(user, nPassword));
					} catch (UserMutatorException e) {
						hasError = true;
						response.setStatus("error");
						response.setError("user_mutator");
						response.setMsg(e.getMessage());
					} catch (UserException e) {
						hasError = true;
						response.setStatus("error");
						response.setError("user_exception");
						response.setMsg(e.getMessage());
						e.printStackTrace();
					}
				}

				if (hasError) {

					resp.getWriter().println(om.writeValueAsString(response));

				} else {
					response.setStatus("ok");
					response.setMsg("Contraseña cambiada satisfactoriamente.");
					response.setPayload(user);
					resp.getWriter().println(om.writeValueAsString(response));

				}
				ctx.complete();

			}
			if (op.equalsIgnoreCase("create")) {
				boolean hasError = false;
				OpCreateJsonResponse response = new OpCreateJsonResponse();
				UserValidator validator = up.getValidator();
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

			if (op.equalsIgnoreCase("update")) {

				boolean hasError = false;
				OpUpdateJsonResponse response = new OpUpdateJsonResponse();
				UserValidator validator = up.getValidator();
				String id = req.getParameter("id");
				String email = req.getParameter("email");
				String username = req.getParameter("username");
				String firstName = req.getParameter("fname");
				String lastName = req.getParameter("lname");
				// String password = req.getParameter("password");

				id = id != null ? id : "";

				if (id == "") {
					hasError = true;
					response.appendError("uid", "Debe suministrar un id.");
				}

				DataUser oldUser = null;
				try {
					oldUser = (DataUser) up.getUserById(id);
				} catch (UserNotExistException e1) {
					response.setError("user_not_exist");
					response.setStatus("error");
					response.setMsg(e1.getMessage());

					e1.printStackTrace();
				} catch (UserException e1) {
					response.setError("user_exception");
					response.setStatus("error");
					response.setMsg(e1.getMessage());
					e1.printStackTrace();
				}

				if (oldUser == null) {
					resp.getWriter().println(om.writeValueAsString(response));
					ctx.complete();
					return;
				}

				email = email != null ? email : oldUser.getEmail();
				username = username != null ? username : oldUser.getUsername();
				firstName = firstName != null ? firstName : oldUser.getFirstName();
				lastName = lastName != null ? lastName : oldUser.getLastName();

				DataUser userParam = new DataUser();
				userParam.setId(id);
				userParam.setEmail(email);
				userParam.setUsername(username);
				userParam.setFirstName(firstName);
				userParam.setLastName(lastName);

				if (oldUser != null) {
					if (!email.equalsIgnoreCase(oldUser.getEmail())) {
						try {
							validator.validateEmail(userParam);
						} catch (EmailValidationException maile) {

							response.appendError("email", maile.getMessage());

							hasError = true;

						} catch (ValidationException e) {
							response.appendError("email", e.getMessage());

							hasError = true;
						}
					}

					if (!username.equals(oldUser.getUsername())) {
						try {
							validator.validateUsername(userParam);
						} catch (UsernameValidationException usernamee) {

							response.appendError("username", usernamee.getMessage());

							hasError = true;
						} catch (ValidationException e) {
							e.printStackTrace();
						}
					}

					if (!firstName.equalsIgnoreCase(oldUser.getFirstName())) {
						try {
							validator.validateFirstName(userParam);
						} catch (FirstNameValidationException fnamee) {

							response.appendError("firstname", fnamee.getMessage());

							hasError = true;
						} catch (ValidationException e) {
							e.printStackTrace();
						}
					}

					if (!lastName.equalsIgnoreCase(oldUser.getLastName())) {
						try {
							validator.validateLastName(userParam);
						} catch (LastNameValidationException lnamee) {

							response.appendError("lastname", lnamee.getMessage());

							hasError = true;
						} catch (ValidationException e) {
							e.printStackTrace();
						}
					}
				}

				if (!hasError) {
					try {
						userParam = (DataUser) up.editUser(userParam);
					} catch (UserException e) {
						response.setStatus("error");
						response.setError("edit_exception");
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
					response.setMsg("usuario modificado");
					response.setPayload(userParam);
					resp.getWriter().println(om.writeValueAsString(response));
				}

				ctx.complete();
			}

			if (op.equalsIgnoreCase("delete")) {
				boolean hasError = false;
				OpUpdateJsonResponse response = new OpUpdateJsonResponse();
				String id = req.getParameter("id");

				id = id != null ? id : "";

				if (id == "") {
					hasError = true;
					response.appendError("uid", "Debe suministrar un id.");
				}

				try {
					up.getUserById(id);
				} catch (UserNotExistException e1) {
					hasError = true;
					response.appendError("user", e1.getMessage());
					e1.printStackTrace();
				} catch (UserException e1) {
					hasError = true;
					response.appendError("user", "Error al obtener informacion de usuario. (" + e1.getMessage() + ")");
					e1.printStackTrace();
				}

				User user = new BasicUser();
				user.setId(id);
				if (!hasError) {
					try {
						up.deleteUser(user);
					} catch (UserException e) {
						response.setStatus("error");
						response.setError("edit_exception");
						response.setMsg(e.getMessage());

						hasError = true;
						e.printStackTrace();
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
					response.setMsg("usuario eliminado");
					response.setPayload(user);
					resp.getWriter().println(om.writeValueAsString(response));

				}
				ctx.complete();

			}

		}
	}
}
