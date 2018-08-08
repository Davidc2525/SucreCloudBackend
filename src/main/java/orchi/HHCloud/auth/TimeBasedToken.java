package orchi.HHCloud.auth;

import java.util.concurrent.TimeUnit;

import orchi.HHCloud.Start;

/**
* Token basado en tiempo, permite asignarle un tiempo de vida a un determinado token, haciendo q el token se revoke
* luego de un tiempo establesido.
**/
public class TimeBasedToken {
	private String idtoken;
	private Long TTL = TimeUnit.MINUTES.toMillis(Start.conf.getLong("auth.verifyEmail.tokens.ttl",1800000L));
	private Long timeExpire = System.currentTimeMillis()+(TTL); //tiempo actual mas lo espesificado en la configuracion
	public TimeBasedToken(String idtoken){
		this.idtoken = idtoken;

	}
	public String getIdtoken() {
		return idtoken;
	}
	public Long getTimeExpire() {
		return timeExpire;
	}
}