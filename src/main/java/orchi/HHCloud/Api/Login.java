package orchi.HHCloud.Api;

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

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import orchi.HHCloud.Start;
import orchi.HHCloud.auth.DefaultAuthProvider;
import orchi.HHCloud.auth.Exceptions.AuthExceededCountFaildException;
import orchi.HHCloud.auth.Exceptions.AuthException;
import orchi.HHCloud.auth.Exceptions.AuthPasswordException;
import orchi.HHCloud.auth.Exceptions.AuthUserNotExistsException;
import orchi.HHCloud.auth.Exceptions.AuthUsernameException;
import orchi.HHCloud.auth.logIO.LoginCallback;
import orchi.HHCloud.auth.logIO.LoginDataSuccess;
import orchi.HHCloud.user.LoginDataUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserManager;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserNotExistException;

public class Login extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private static ObjectMapper om;
	/** proveedor de auttenticacion */
	private static DefaultAuthProvider authProvider;
	public static ThreadPoolExecutor executorw2;

	@Override
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub

		executorw2 = new ThreadPoolExecutor(10, 1000, 50000L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(100000));

		om = new ObjectMapper();
		om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		om.getJsonFactory();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// delegate long running process to an "async" thread
		executorw2.execute(new Task(req.startAsync()));


	}

	public static class AuthJsonResponse {
		public String status = "ok";
		public String sesid = null;
		public String userid = null;
		public boolean auth = false;
		public boolean exist = false;
		public String msg = "";
		public String username = null;
		public String password = null;

		public AuthJsonResponse(String m, boolean auth) {

			msg = m;
			this.auth = auth;
		}

		public AuthJsonResponse setUserid(String userid) {
			this.userid = userid;
			return this;
		}

		public AuthJsonResponse setSesId(String id) {
			sesid = id;
			return this;
		}

		public AuthJsonResponse setExist(boolean e) {
			exist = e;
			return this;
		}

		public AuthJsonResponse setUsernameError(String error) {
			username = error;
			return this;
		}

		public AuthJsonResponse setPasswordError(String error) {
			password = error;
			return this;
		}

		public String getSesid() {
			return sesid;
		}

		public String getUserid() {
			return userid;
		}

		public boolean isAuth() {
			return auth;
		}

		public boolean isExist() {
			return exist;
		}

		public String getMsg() {
			return msg;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		public String getStatus() {
			return status;
		}

		public AuthJsonResponse setStatus(String status) {
			this.status = status;
			return this;
		}
	}


	public static class Task implements Runnable {

		private AsyncContext ctx;

		public Task(AsyncContext asyncCtx) {
			this.ctx = asyncCtx;
		}

		public void writeResponse(AuthJsonResponse data) {
			try {
				((HttpServletResponse) ctx.getResponse()).setHeader("Content-type", "application/json");
				((HttpServletResponse) ctx.getResponse()).setHeader("Access-Control-Allow-Credentials", "true");
				((HttpServletResponse) ctx.getResponse()).setHeader("Access-Control-Allow-Origin", "http://localhost:9090");
				((HttpServletResponse) ctx.getResponse()).setHeader("Content-encoding", "gzip");

				om.writeValue(new  GzipCompressorOutputStream(ctx.getResponse().getOutputStream()), data);
				ctx.complete();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ctx.complete();
			}

		};

		@Override
		public void run() {
			try {
				this.process();
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		public LoginDataUser  createUserLoginWithRequest(){
			HttpServletRequest req = ((HttpServletRequest) ctx.getRequest());
			String username = req.getParameter("username");
			String password = req.getParameter("password");
			String remember = req.getParameter("remember");
			boolean isRemember = Boolean.valueOf(remember);

			return new LoginDataUser().bind(username, password, isRemember);
		}

		public void process() throws JsonGenerationException, JsonMappingException, IOException, InterruptedException {
			HttpServletRequest req = ((HttpServletRequest) ctx.getRequest());
			HttpSession s = req.getSession(false);
			System.err.println("Dentro: " + Thread.currentThread());
			//Thread.sleep(10000);
			if (s == null) {

				if (req.getParameter("username") != null && req.getParameter("password") != null) {

					try {
						LoginDataUser newUser = createUserLoginWithRequest();

						Start.getLoginAndOut().logInCallBack(ctx, newUser, (/** LoginDataSuccess */ loginData) -> {
							HttpSession session = ((HttpServletRequest) loginData.getCtx().getRequest())
									.getSession(false);

							session.setAttribute("uid", loginData.getUser().getId());

							if (newUser.isRemember()) {
								System.err.println("User is remember: " + newUser.isRemember());
								session.setMaxInactiveInterval(0);
							}

							writeResponse(new AuthJsonResponse("login", true).setUserid(loginData.getUser().getId())
									.setSesId(session.getId()));

						});

					} catch (AuthUsernameException e) {
						writeResponse(new AuthJsonResponse(e.getMessage(), false).setUsernameError(e.getMessage()).setStatus("error"));
					} catch (AuthPasswordException e) {
						writeResponse(new AuthJsonResponse(e.getMessage(), false).setPasswordError(e.getMessage()).setStatus("error"));
					} catch (AuthUserNotExistsException e) {
						writeResponse(new AuthJsonResponse(e.getMessage(), false).setUsernameError(e.getMessage()).setStatus("error"));
					} catch (AuthExceededCountFaildException e) {
						writeResponse(new AuthJsonResponse(e.getMessage(), false).setPasswordError(e.getMessage()).setStatus("error"));
					} catch (AuthException e) {
						writeResponse(new AuthJsonResponse(e.getMessage(), false).setStatus("error"));
					}

				} else {
					writeResponse(new AuthJsonResponse("you need send data", false)
							.setUsernameError("username is required")
							.setPasswordError("password is required")
							.setStatus("error"));
				}
			} else {
				User user = null;//= createUserLoginWithRequest();
				try {
					user = UserManager.getInstance().getUserProvider().getUserById((String) s.getAttribute("uid"));
				} catch (UserNotExistException e) {
					e.printStackTrace();
					writeResponse(new AuthJsonResponse(e.getMessage(), false)
							.setUsernameError(e.getMessage())
							.setStatus("error"));
				} catch (UserException e) {
					e.printStackTrace();
					writeResponse(new AuthJsonResponse(e.getMessage(), false)
							.setExist(false)
							.setStatus("error"));
				}

				writeResponse(new AuthJsonResponse("session aleardy create", false)
						.setExist(true)
						.setUserid(user.getId())
						.setSesId(s.getId()));

			}

		}

	}
}
