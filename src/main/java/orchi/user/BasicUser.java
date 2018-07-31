package orchi.user;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class BasicUser extends User{
	
	@Override
	public User bind(String id, String username, String email, String password) {
		// TODO Auto-generated method stub
		this.id = id == null ? "" : id;
		this.username = username == null ? "" : username;
		this.email = email == null ? "" : email;
		this.password = password == null ? "" : password;
		return this;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(id);
		out.writeUTF(username);
		out.writeUTF(email);
		out.writeUTF(password);		
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = in.readUTF();
		username = in.readUTF();
		email = in.readUTF();
		password = in.readUTF();
	}

}
