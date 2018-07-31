package orchi.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class LoginDataUser extends BasicUser{
	
	@Override
	public String toString() {
		return "LoginDataUser {remember=" + remember + ", id=" + id + ", username=" + username + ", email=" + email
				+ ", password=" + password + "}";
	}

	private boolean remember;

	public LoginDataUser bind( String username, String password, Boolean remember) {		
		super.bind("", username, username, password);
		 
		this.remember = remember == null ? false:remember;
		return this;
	}
	
	public boolean isRemember() {
		return remember;
	}

	public void setRemember(boolean remember) {
		this.remember = remember;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(id);
		out.writeUTF(username);
		out.writeUTF(email);
		out.writeUTF(password);	
		out.writeUTF(Boolean.toString(remember));	
		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readUTF();
		username = in.readUTF();
		email = in.readUTF();
		password = in.readUTF();
		remember = Boolean.parseBoolean(in.readUTF());
	}
	
}
