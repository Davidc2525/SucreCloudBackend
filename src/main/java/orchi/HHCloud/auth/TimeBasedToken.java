package orchi.HHCloud.auth;

import java.util.concurrent.TimeUnit;

import orchi.HHCloud.Start;
import orchi.HHCloud.user.User;

/**
* Token basado en tiempo, permite asignarle un tiempo de vida a un determinado token, haciendo q el token se revoke
* luego de un tiempo establesido.
**/
public class TimeBasedToken {
	private String idtoken;
	private Long TTL = TimeUnit.MINUTES.toMillis(Start.conf.getLong("auth.verifyEmail.tokens.ttl",1800000L));
	private Long timeExpire = System.currentTimeMillis()+(TTL); //tiempo actual mas lo espesificado en la configuracion
	private User owner;
	public TimeBasedToken(String idtoken,User owner){
		this.idtoken = idtoken;
		this.owner = owner;

	}
	public String getIdtoken() {
		return idtoken;
	}
	public Long getTimeExpire() {
		return timeExpire;
	}
	public User getOwner(){return owner;}
}
