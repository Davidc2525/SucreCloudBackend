package orchi.auth.logIO;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import orchi.auth.Exceptions.AuthException;
import orchi.user.User;

/**
 * proveedor de inicio de session y invalidacion de session
 * @author Colmenares David
 * */
public interface LoginAndLogoutInterface {
	
	
	public void logIn(String username, String password) throws AuthException;
	
	/**
	 * 
	 * llama a authenticate pasando un {@link WraperLoginCallback} para ejecutar
	 * la logica de negocio antes de la funcion dentro de {@link LoginCallback}
	 * 
	 * @param ctx
	 *            contexto asincrono de {@link HttpServletRequest}
	 * @param user
	 *            usuario con correo y clave, para pasarlo a authenticate con
	 *            los datos del login del formulario
	 * @param callback
	 *            {@link LoginCallback}, funcion a ejecutar luego de autenticar
	 *            y hacer logica de negocio.
	 */
	public  void logInCallBack(AsyncContext ctx,User user, LoginCallback callback) throws AuthException;
	
	
	
	/**
	 * Cerrar session con {@link HttpSession}.
	 *  
	 * esta manda al cluster q ciere las conecciones de chat
	 * 
	 * */
	public void logOut(HttpSession session);
	
	/**
	 * Cierra la session con la session, con administrador de session del servidor
	 * y hace q el {@link orchi.servletContextListeners.SessionListener} de session, no aga nada
	 * <pre><code>
...
Boolean sskip = (Boolean) se.getSession().getAttribute("skipListenerTiggered");
Boolean skip = false;
skip = sskip != null ? (boolean) ( se.getSession().getAttribute("skipListenerTiggered")):false; ;
if(!skip){
	logger.info("sessionDestroyed {} {}",se,se.getSession());
	
	AppOrchi.getLoginAndOut().logOut(se.getSession());
}
logger.info("skipListenerTiggered: {}",skip);
...
	 * </code></pre>
	 * 
	 * */
	public void logOut(HttpSession session,Boolean skipListener);
	
	
	public void logOutCallBack(AsyncContext ctx,Boolean skipListener,LogOutCallBack callback);
	
	
	public void logOutCallBack(AsyncContext ctx,LogOutCallBack callback);
	
}
