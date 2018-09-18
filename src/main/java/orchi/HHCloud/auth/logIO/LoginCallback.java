package orchi.HHCloud.auth.logIO;

/**
 * Ejecutar tarea despoes la logica de {@link WraperLoginCallback}
 */
@FunctionalInterface
public interface LoginCallback {
    /**
     * esta es la funcion lambda q se pasa a el metodo logInCallBack en {@link orchi.HHCloud.auth.logIO.LogInAndOut}
     * se envuelve en un {@link WraperLoginCallback}, para pasarla a {@link orchi.HHCloud.auth.AuthProvider.authenticate}	 *
     * y se ejecuta luego de q se ejecute la logica de negocio
     * <pre>
     *  orchi.HHCloud.auth.AuthProvider.authenticate
     *                             to
     *               |              |
     * ______(User)__|____________  |
     * |                         |  |
     * | WraperLoginCallback     |  |
     * |                         |  |
     * |   _(LoginDataSuccess)_  |  |
     * |   |                   | |  |
     * |   |LoginCallback      | |  |
     * |___|___________________|_|  |
     *           |                  |
     *           |                 from
     *     orchi.HHCloud.auth.logIO.LogInAndOut.LogInCallBack
     * </pre>
     *
     * @param data {@link LoginDataSuccess}
     */
    public void call(LoginDataSuccess data);
}
