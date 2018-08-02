package orchi.user;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import orchi.SucreCloud.Start;
import orchi.SucreCloud.database.ConnectionProvider;
import orchi.SucreCloud.database.DbConnectionManager;
import orchi.user.Exceptions.UserException;
import orchi.user.Exceptions.UserMutatorException;
import orchi.user.Exceptions.UserMutatorPassword;
import orchi.user.Exceptions.UserNotExistException;

public class EmbedUserProvider implements UserProvider {

	private ConnectionProvider provider;
	private Connection conn;

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
	public User getUserById(String userId) throws UserNotExistException {
		User user = null;
		ResultSet result;
		try {
			PreparedStatement stm = conn.prepareStatement("SELECT * FROM USERS where id=(?)");
			stm.setBigDecimal(1, new BigDecimal(userId));
			result = stm.executeQuery();
			
			if (result.next()) {
				BigDecimal id = result.getBigDecimal("id");

				user = new BasicUser().bind(id + "", result.getString("username"), result.getString("email"),
						result.getString("pass"));
			} else {
				throw new UserNotExistException("Usuario con " + userId + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserNotExistException(e.getMessage());

		}
		System.err.println("UserById "+user);
		return user;
	}

	@Override
	public User getUserByEmail(String userEmail) throws UserNotExistException {
		User user = null;
		ResultSet result;
		try {
			PreparedStatement stm = conn.prepareStatement("SELECT * FROM USERS where email=(?)");
			stm.setString(1, (userEmail));
			result = stm.executeQuery();
			
			if (result.next()) {
				BigDecimal id = result.getBigDecimal("id");

				user = new BasicUser().bind(id + "", result.getString("username"), result.getString("email"),
						result.getString("pass"));
			} else {
				throw new UserNotExistException("Usuario con " + userEmail + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserNotExistException(e.getMessage());

		}
		System.err.println("UserByEmail: "+user);
		return user;
	}

	@Override
	public User getUserByUsername(String userName) throws UserNotExistException {
		User user = null;
		ResultSet result;
		try {
			PreparedStatement stm = conn.prepareStatement("SELECT * FROM USERS where username=(?)");
			stm.setString(1, (userName));
			result = stm.executeQuery();
			
			if (result.next()) {
				BigDecimal id = result.getBigDecimal("id");

				user = new BasicUser().bind(id + "", result.getString("username"), result.getString("email"),
						result.getString("pass"));
			} else {
				throw new UserNotExistException("Usuario con " + userName + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserNotExistException(e.getMessage());

		}
		System.err.println("UserByEmail: "+user);
		return user;
	}

	@Override
	public void createUser(CreateUser newUser) throws UserException {
		// TODO Auto-generated method stub
	}

	@Override
	public void deleteUser(String userId) throws UserException {
		// TODO Auto-generated method stub

	}

	@Override
	public void changePasswordUser(UserMutatorPassword userMutator) throws UserMutatorException {
		// TODO Auto-generated method stub

	}

}
