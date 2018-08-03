package orchi.auth.logIO;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import orchi.SucreCloud.Start;
import orchi.SucreCloud.RestApi.App;
import orchi.auth.Exceptions.AuthException;

import orchi.user.User;

public class LogInAndOut implements LoginAndLogoutInterface {
	private static LogInAndOut instance = null;

	

	@Override
	public void logInCallBack(AsyncContext ctx, User user, LoginCallback callback) throws AuthException {

		Start.getAuthProvider().authenticate(user, (authUser) -> {

			HttpSession session = ((HttpServletRequest) ctx.getRequest()).getSession(true);

			callback.call(new LoginDataSuccess(authUser, ctx));		
			
		});
	}

	@Override
	public void logIn(String username, String password) throws AuthException {

		Start.getAuthProvider().authenticate(username, password);

	}

	@Override
	public void logOut(HttpSession session) {
		session.invalidate();
	}

	@Override
	public void logOut(HttpSession session, Boolean skipListener) {
		session.setAttribute("skipListenerTiggered", skipListener);
		logOut(session);		
	}

	@Override
	public void logOutCallBack(AsyncContext ctx, Boolean skipListener, LogOutCallBack callback) {
		HttpSession session = ((HttpServletRequest) ctx.getRequest()).getSession(false);		
		logOut(session, skipListener);
		callback.call();
	}

	@Override
	public void logOutCallBack(AsyncContext ctx, LogOutCallBack callback) {
		logOut(((HttpServletRequest) ctx.getRequest()).getSession(false));
		callback.call();
	}

	public static LogInAndOut getInstance() {
		if (instance == null)
			instance = new LogInAndOut();
		return instance;
	}

}
