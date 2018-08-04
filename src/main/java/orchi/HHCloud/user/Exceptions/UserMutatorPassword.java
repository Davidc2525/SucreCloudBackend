package orchi.HHCloud.user.Exceptions;

import orchi.HHCloud.user.User;

public class UserMutatorPassword {
	private User user;
	private String password;
	public UserMutatorPassword(User user, String newPassword){
		this.user = user;
		password = newPassword;
	}
	public User getUser() {
		return user;
	}
	public String getPassword() {
		return password;
	}
}
