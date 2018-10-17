package orchi.HHCloud.user.search;

import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Users;

/**
 * Permite crear impleentaciones para proveer de una busqueda de usuario,
 * El proveedor de usuario {@link orchi.HHCloud.user.UserProvider}
 * ofrece una manera de obtener usuarios dependiendo de su id o email, con esta interface
 * se puede generar busqueda de texto completo o terminos,
 *
 * @author david */
public interface SearchUserProvider {

    /**
     * Metodo opcional para preparar el proveedor de busqueda
     * */
    public void prepare();

    /**
     * Metodo para inicializar el proveedor de busqueda
     * */
    public void init();

    /**
     * Agregar un usuario al indice de busqueda
     * */
    public void addUserToIndex(User user);

    /**
     * Editar un usuario ya indexado
     * */
    public void editUserInIndex(User oldUser, User newUser);

    /**
     * Eliminar un usuario del indice
     * */
    public void removeUserToIndex(User user);

    /**
     * Agregar usuarios
     * */
    public void addAll(Users users);

    /**
     * Eliminar usuarios
     * */
    public void removeAll(Users users);

    /**
     * Limpiar indice
     * */
    public void clear();

    /**
     * Buscar usuario, Busca en FirstName, LastName, Username y Email campos de la clase User
     * */
    public UsersFound search(String queryString);
}
