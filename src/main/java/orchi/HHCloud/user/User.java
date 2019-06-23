package orchi.HHCloud.user;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.avatar.AvatarDescriptor;
import orchi.HHCloud.user.avatar.AvatarProvider;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Externalizable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Todas las clases tengan q ver con contenido de usuario deben heredar esta
 * clase, para poder funcionar en el programa ya q en to-do el programa se hace
 * referencia a esta. Es abstracta asi q para poder hacer instancias puedes usar
 * {@link DataUser}, si se necesita algo mas espesifico, se puede espesialisar
 * heredando esta.
 * Todas las clases q herenden esta clase, tienen q ser serialisables
 */
public abstract class User implements Externalizable, Comparator<User> {

    @JsonIgnore
    private static List<String> bounds = Arrays.asList("50x50", "100x100", "290x290", "500x500");
    protected String id = "";
    protected String username = "";
    protected String email = "";
    @JsonIgnore //ocultar la clave a la api de usuario
    protected String password = "";
        
      
    @JsonProperty(value = "avatars")
    public HashMap<String, String> getAvatarsJson() {
        HashMap<String, String> avatars = new HashMap<>();
        try {
            if (/*ap.isSet(au)*/true) {
                AvatarProvider ap = Start.getUserManager().getUserProvider().getAvatarProvider();
                AvatarDescriptor ad = ap.getADescriptor(this);
                long currentTime = System.currentTimeMillis();
                String protocol = Start.conf.getString("api.protocol");
                String host = Start.conf.getString("api.host");
                String port = Start.conf.getString("api.port");

                if(ad!=null){
                    avatars.put("has",ad.isHashAvatar()?"true":"false");
                }else{
                    avatars.put("has","false");
                }
                bounds.forEach((String bound) -> {
                    if (ad == null) {
                        avatars.put(bound, String.format("%s://%s:%s/api/avatar?id=%s&size=%s", protocol, host, port, id, bound));
                    } else {
                        avatars.put(bound, String.format("%s://%s:%s/api/avatar?id=%s&size=%s&lm=%s", protocol, host, port, id, bound, ad.getLastModified()));
                    }

                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return avatars;
    }

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
        return o1.equals(o2.getId()) ? 1 : 0;
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if (obj instanceof User) {
            User cUser = (User) obj;

            eq = id.equalsIgnoreCase(cUser.getId());

        }

        if (obj instanceof String) {
            eq = id.equalsIgnoreCase((String) obj);
        }

        return eq;

    }
}
