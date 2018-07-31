package orchi.auth.logIO;

import orchi.user.User;


/**
 * Envolver un {@link LoginCallback}, anteponiendo la logica de negocio, 
 * antes de pasar el cotrol a {@link LoginCallback} 
 * que contiene la funcionalidad del metodo que lo invoca
 * 
 * 
 * */
@FunctionalInterface
public interface WraperLoginCallback {
	/**
	 * Llamar a {@link LoginCallback}, pasando como parametro un {@link User}
	 * */
	public void call(User authUser);
}

