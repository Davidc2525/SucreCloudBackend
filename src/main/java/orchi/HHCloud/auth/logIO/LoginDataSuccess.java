package orchi.HHCloud.auth.logIO;

import javax.servlet.AsyncContext;

import orchi.HHCloud.user.User;

public class LoginDataSuccess {
	
	private User user;
	private AsyncContext ctx;

	public LoginDataSuccess(User user, AsyncContext ctx){
		this.user = user;
		this.ctx = ctx;
	}

	public User getUser() {
		return user;
	}

	public AsyncContext getCtx() {
		return ctx;
	}

	
}
