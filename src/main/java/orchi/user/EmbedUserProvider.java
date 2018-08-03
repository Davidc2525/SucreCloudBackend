package orchi.user;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import orchi.SucreCloud.Start;
import orchi.SucreCloud.database.ConnectionProvider;
import orchi.SucreCloud.database.DbConnectionManager;
import orchi.user.Exceptions.UserAleardyExistsException;
import orchi.user.Exceptions.UserException;
import orchi.user.Exceptions.UserMutatorException;
import orchi.user.Exceptions.UserMutatorPassword;
import orchi.user.Exceptions.UserNotExistException;

/**
 * @author Colmenares David
 *
 */
public class EmbedUserProvider implements UserProvider {

	private static final String SELECT_USERS_WHERE_USERNAME = "SELECT * FROM USERS where username=(?)";
	private static final String SELECT_USERS_WHERE_EMAIL = "SELECT * FROM USERS where email=(?)";
	private static final String SELECT_USERS_WHERE_ID = "SELECT * FROM USERS where id=(?)";
	private static final String INSERT_INTO_USERS = "INSERT INTO USERS VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private ConnectionProvider provider;
	private Connection conn;
	private UserValidator userValidator = new DefaultUserValidator();

	public EmbedUserProvider() {
		try {
			Start.getDbConnectionManager();
			provider = DbConnectionManager.getInstance().getConnectionProvider();
			conn = provider.getConnection();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public User getUserById(String userId) throws UserNotExistException ,UserException{
		User user = null;
		ResultSet result;
		try {
			PreparedStatement stm = conn.prepareStatement(SELECT_USERS_WHERE_ID);
			stm.setString(1, (userId));
			result = stm.executeQuery();

			if (result.next()) {
				user = buildUserFromResult(result);
			} else {
				throw new UserNotExistException("Usuario con " + userId + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		}
		System.err.println("UserById " + user);
		return user;
	}

	@Override
	public User getUserByEmail(String userEmail) throws UserNotExistException,UserException {
		User user = null;
		ResultSet result;
		try {
			PreparedStatement stm = conn.prepareStatement(SELECT_USERS_WHERE_EMAIL);
			stm.setString(1, (userEmail));
			result = stm.executeQuery();

			if (result.next()) {
				user = buildUserFromResult(result);
			} else {
				throw new UserNotExistException("Usuario con " + userEmail + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		}
		System.err.println("UserByEmail: " + user);
		return user;
	}

	@Override
	public User getUserByUsername(String userName) throws UserNotExistException,UserException {
		User user = null;
		ResultSet result;
		try {
			PreparedStatement stm = conn.prepareStatement(SELECT_USERS_WHERE_USERNAME);
			stm.setString(1, (userName));
			result = stm.executeQuery();

			if (result.next()) {
				user = buildUserFromResult(result);
			} else {
				throw new UserNotExistException("Usuario con " + userName + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		}
		System.err.println("UserByEmail: " + user);
		return user;
	}

	@Override
	public void createUser(User newUser) throws UserException {
		DataUser user = ((DataUser) newUser);
		try {
			getUserById(newUser.getId());
			throw new UserAleardyExistsException("Ya existe un usuario registrado con ese id: " + user.getId());
		} catch (UserNotExistException e1) {
			try {
				getUserByEmail(newUser.getEmail());
				throw new UserAleardyExistsException("Ya existe un usuario registrado con ese email: " + user.getEmail());
			} catch (UserNotExistException e2) {
				try {
					PreparedStatement userInsert = conn
							.prepareStatement(INSERT_INTO_USERS);

					userInsert.setString(1, user.getId());
					userInsert.setString(2, user.getEmail());
					userInsert.setBoolean(3, user.isEmailVerified());
					userInsert.setString(4, user.getUsername());
					userInsert.setString(5, user.getFirstName());
					userInsert.setString(6, user.getLastName());
					userInsert.setBigDecimal(7, new BigDecimal(user.getCreateAt()));
					userInsert.setString(8, user.getPassword());					
					userInsert.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
					throw new UserException(e.getMessage());
				}
			}

		}

	}

	@Override
	public void deleteUser(User user) throws UserException {
		//TODO
	}

	@Override
	public void changePasswordUser(UserMutatorPassword userMutator) throws UserMutatorException {
		//TODO
	}
	
	private User buildUserFromResult(ResultSet result) throws SQLException{
		BigDecimal createAtbi = result.getBigDecimal("createat");
		Long createAt = Long.valueOf(createAtbi.toString());
		User user = new DataUser().bind(
				result.getString("id"),
				result.getString("username"),
				result.getString("email"),
				result.getBoolean("emailverified"),
				result.getString("pass"), 
				result.getString("firstname"),
				result.getString("lastname"),
				createAt);
		return user;
	}


	@Override
	public UserValidator getUserValidator() {
		return userValidator;
	}

}
