package orchi.HHCloud.user;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Externalizable;
import java.util.Comparator;

/**
 * Todas las clases tengan q ver con contenido de usuario deben heredar esta
 * clase, para poder funcionar en el programa ya q en to-do el programa se hace
 * referencia a esta. Es abstracta asi q para poder hacer instancias puedes usar
 * {@link DataUser}, si se necesita algo mas espesifico, se puede espesialisar
 * heredando esta.
 * Todas las clases q herenden esta clase, tienen q ser serialisables
 */
public abstract class User implements Externalizable,Comparator<User>{

    protected String id = "";
    protected String username = "";
    protected String email = "";
    @JsonIgnore //ocultar la clave a la api de usuario
    protected String password = "";

    public User bind(String id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email.toLowerCase();
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User {id=" + id + ", username=" + username + ", email=" + email + ", password=" + password + "}";
    }

    @Override
    public int compare(User o1, User o2) {
        return o1.equals(o2.getId()) ? 1:0;
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if(obj instanceof User){
            User cUser = (User) obj;

            eq = id.equalsIgnoreCase(cUser.getId());

        }

        if(obj instanceof String){
            eq = id.equalsIgnoreCase((String) obj);
        }

        return eq;

    }
}
