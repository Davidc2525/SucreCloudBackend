package orchi.HHCloud.user;

import java.io.Externalizable;

/**
 * Todas las clases tengan q ver con contenido de usuario deben heredar esta
 * clase, para poder funcionar en el programa ya q en todo el programa se hace
 * referencia a esta. Es abstracta asi q para poder hacer instancias puedes usar
 * {@link BasicUser}, si se necesita algo mas espesifico, se puede espesialisar
 * heredando esta.
 * Todas las clases q herenden esta clase, tienen q ser serialisables
 */
public abstract class User implements Externalizable{

	protected String id = "";
	protected String username = "";
	protected String email = "";
	//@JsonIgnore //Descomentar para ocultar la clave a la api de usuario
	protected String password = "";

	public User bind(String id,String username, String email, String password) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		return this;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "User {id=" + id + ", username=" + username + ", email=" + email + ", password=" + password + "}";
	}

}
