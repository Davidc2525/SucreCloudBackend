package orchi.HHCloud.user;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.jasper.tagplugins.jstl.core.Url;
import org.json.JSONObject;

import com.google.api.client.util.Base64;

import orchi.HHCloud.Start;
import orchi.HHCloud.auth.Exceptions.TokenException;
import orchi.HHCloud.database.ConnectionProvider;
import orchi.HHCloud.database.DbConnectionManager;
import orchi.HHCloud.mail.MailProvider;
import orchi.HHCloud.mail.Exceptions.SendEmailException;
import orchi.HHCloud.user.Exceptions.UserAleardyExistsException;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserMutatorException;
import orchi.HHCloud.user.Exceptions.UserMutatorPassword;
import orchi.HHCloud.user.Exceptions.UserNotExistException;

/**
 * Proveedor de usuarios con base de datos empotrada
 * tambien sirve para jdbc:mysql, modificando la configuracion, con el proveedor de base de dato 
 * @author Colmenares David
 *
 */
public class EmbedUserProvider implements UserProvider {

	private static final String APPLICATION_ADMIN = Start.conf.getString("mail.mailmanager.admin");
	private static final String UPDATE_PASS_USER = "UPDATE USERS SET PASS=(?) WHERE ID=(?)";
	private static final String DELETE_USERS_WHERE_ID = "DELETE FROM USERS WHERE ID=(?)";
	private static final String SELECT_USERS_WHERE_USERNAME = "SELECT * FROM USERS where username=(?)";
	private static final String SELECT_USERS_WHERE_EMAIL = "SELECT * FROM USERS where email=(?)";
	private static final String SELECT_USERS_WHERE_ID = "SELECT * FROM USERS where id=(?)";
	private static final String INSERT_INTO_USERS = "INSERT INTO USERS VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_USER = "UPDATE USERS SET "
											+ "EMAIL=(?),"
											//+ "EMAILVERIFIED=(?),"
											+ "USERNAME=(?),"
											+ "FIRSTNAME=(?),"
											+ "LASTNAME=(?),"
											+ "GENDER=(?)"
											+ "WHERE ID=(?)";
	private static final String UPDATE_EMAIL_VERIFIED = ""
											+ "UPDATE USERS SET "
											+ "EMAILVERIFIED=(?)"
											+ "WHERE ID=(?)";
	private ConnectionProvider provider;
	private Connection conn;
	private UserValidator userValidator = new DefaultUserValidator();
	private String templateEmailVerify;
	private String templateRecoveryPassword;

	public EmbedUserProvider() {
		try {
			templateEmailVerify = Streams.asString(EmbedUserProvider.class.getResourceAsStream("/templateVerifyEmail.html"));
			templateRecoveryPassword = Streams.asString(EmbedUserProvider.class.getResourceAsStream("/templateRecoveryPasswordEmail.html"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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
		PreparedStatement stm = null;
		try {
			stm = conn.prepareStatement(SELECT_USERS_WHERE_ID);
			stm.setString(1, (userId));
			result = stm.executeQuery();

			if (result.next()) {
				user = buildUserFromResult(result);
			} else {
				throw new UserNotExistException("Usuario con id: " + userId + ", no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		} finally {
			try {
				stm.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		System.err.println("UserById " + user);
		return user;
	}

	@Override
	public User getUserByEmail(String userEmail) throws UserNotExistException,UserException {
		User user = null;
		ResultSet result;
		PreparedStatement stm = null;
		try {
			stm = conn.prepareStatement(SELECT_USERS_WHERE_EMAIL);
			stm.setString(1, escape(userEmail));
			result = stm.executeQuery();
			
			if (result.next()) {
				user = buildUserFromResult(result);
			} else {
				throw new UserNotExistException("Usuario con " + userEmail + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		} finally {
			try {
				stm.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		System.err.println("UserByEmail: " + user);
		return user;
	}

	@Override
	public User getUserByUsername(String userName) throws UserNotExistException,UserException {
		User user = null;
		ResultSet result;
		PreparedStatement stm = null;
		try {
			stm = conn.prepareStatement(SELECT_USERS_WHERE_USERNAME);
			stm.setString(1, escape(userName));
			result = stm.executeQuery();

			if (result.next()) {
				user = buildUserFromResult(result);
			} else {
				throw new UserNotExistException("Usuario con " + userName + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		}  finally {
			try {
				stm.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
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
				PreparedStatement userInsert = null;
				try {
					userInsert = conn
							.prepareStatement(INSERT_INTO_USERS);

					userInsert.setString(1, escape(user.getId()));
					userInsert.setString(2, escape(user.getEmail()));
					userInsert.setBoolean(3, (user.isEmailVerified()));
					userInsert.setString(4, escape(user.getUsername()));
					userInsert.setString(5, escape(user.getFirstName()));
					userInsert.setString(6, escape(user.getLastName()));
					userInsert.setString(7, escape(user.getGender()));
					userInsert.setBigDecimal(8, new BigDecimal(user.getCreateAt()));
					userInsert.setString(9, escape(user.getPassword()));
					userInsert.executeUpdate();
					sendVerifyEmail(user);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new UserException(e.getMessage());
				} finally {
					try {
						userInsert.close();
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				}
			}

		}

	}

	@Override
	public void deleteUser(User user) throws UserException {
		PreparedStatement userDelete = null;
		try {
			userDelete = conn.prepareStatement(DELETE_USERS_WHERE_ID);
			userDelete.setString(1, escape(user.getId()));
			userDelete.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		} finally {
			try {
				userDelete.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

	}

	@Override
	public User changePasswordUser(UserMutatorPassword userMutator) throws UserMutatorException, UserException {
		User user = userMutator.getUser();
		String nPassword = userMutator.getPassword();

		PreparedStatement updatePass = null;
		try {
			updatePass = conn.prepareStatement(UPDATE_PASS_USER);
			updatePass.setString(1, nPassword);
			updatePass.setString(2, user.getId());
			updatePass.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		} finally {
			try {
				updatePass.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return getUserById(user.getId());

	}


	@Override
	public User editUser(User userWithChanges) throws UserException {
		
		DataUser editUser = (DataUser) userWithChanges;
		DataUser oldUser = (DataUser)getUserById(editUser.getId());
		System.out.println("editUser "+editUser);
		PreparedStatement userUpdate = null;
		try {
			userUpdate = conn.prepareStatement(UPDATE_USER);

			userUpdate.setString(1, escape(editUser.getEmail()));
			//userUpdate.setBoolean(2, editUser.isEmailVerified());
			userUpdate.setString(2, escape(editUser.getUsername()));
			userUpdate.setString(3, escape(editUser.getFirstName()));
			userUpdate.setString(4, escape(editUser.getLastName()));
			userUpdate.setString(5, escape(editUser.getGender()));
			userUpdate.setString(6, escape(oldUser.getId()));
			userUpdate.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		} finally {
			try {
				userUpdate.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return getUserById(oldUser.getId());
	}

	@Override
	public UserValidator getValidator() {
		return userValidator;
	}

	public String escape(String in){
		return StringEscapeUtils.escapeSql(in);
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
		((DataUser) user).setGender(result.getString("gender"));
		return user;
	}


	@Override
	public User setVerifyEmail(User user) throws UserException {
		PreparedStatement verifyEmail = null;;
		if(isEmailVerified(user)){
			return getUserById(user.getId());
		}
		try {
			verifyEmail = conn.prepareStatement(UPDATE_EMAIL_VERIFIED);
			verifyEmail.setBoolean(1, true);
			verifyEmail.setString(2, user.getId());;
			verifyEmail.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException("No se pudo verificar el email. "+e.getMessage());
		} finally {
			try {
				verifyEmail.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return getUserById(user.getId());
	}

	private boolean isEmailVerified(User user) throws UserNotExistException, UserException{
		return ((DataUser) getUserById(user.getId())).isEmailVerified();
	}


	@Override
	public User sendVerifyEmail(User user) throws UserException {
		String idToken;
		try {
			idToken = Start.getAuthProvider().createTokenToVerifyEmail(user);
		} catch (TokenException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		}

		DataUser dUser 	= (DataUser) user;
		MailProvider mp = Start.getMailManager().getProvider();
		String host 	= Start.conf.getString("api.host");
		int port 		= Start.conf.getInt("api.port");
		String appUrl	= "http://"+host+":"+port+"/";
		String args 	= new JSONObject().put("token", idToken).toString();
			   args 	= Base64.encodeBase64String(args.getBytes());
		String url		= "http://"+host+":"+port+"/api/auth?op=verifyemail&args="+args;
		String appName 	= Start.conf.getString("app.name");;
		String subject 	= "Verificar tu correo para "+appName;

		Map<String,String> values = new HashMap<String,String>();
		values.put("uid", 		dUser.getId());
		values.put("email", 	dUser.getEmail());
		values.put("isverified",Boolean.toString(dUser.isEmailVerified()));
		values.put("firstName", dUser.getFirstName().toUpperCase());
		values.put("lastName", 	dUser.getLastName().toUpperCase());
		values.put("appUrl", 	appUrl);
		values.put("url", 		url);
		values.put("appName", 	appName);
		String templateBody = createBody(values,templateEmailVerify);

		System.err.println(user.getEmail());
		System.err.println(subject);
		System.err.println(templateBody);
		new Thread(()->{
			System.out.println("enviado correo de verificacion.");
			try {
				mp.sendEmail(APPLICATION_ADMIN, user.getEmail()+"", subject, templateBody);
				System.out.println("Correo de verificacion enviado.");
			} catch (SendEmailException e) {
			    System.err.println("No se pudo enviar el correo de verificacion.");
			    Start.getAuthProvider().revokeTokenToVerifyEmail(idToken);
				e.printStackTrace();
			}

		}).start();
		return user;
	}


	@Override
	public User sendRecoveryPasswordEmail(User user) throws UserException, SendEmailException {
		
		String token = Start.getAuthProvider().createTokenToRecoveryPassword(user);
		DataUser dUser = (DataUser) user;
		MailProvider mp = Start.getMailManager().getProvider();
		String host 	= Start.conf.getString("api.host");
		int port 		= Start.conf.getInt("api.port");
		String appUrl	= "http://"+host+":"+port+"/";
		String appName 	= Start.conf.getString("app.name");;
		String subject = "Codigo para recuperacion de contraseña.";

		Map<String,String> values = new HashMap<String,String>();
		values.put("uid", 		dUser.getId());
		values.put("email", 	dUser.getEmail());
		values.put("isverified",Boolean.toString(dUser.isEmailVerified()));
		values.put("firstName", dUser.getFirstName().toUpperCase());
		values.put("lastName", 	dUser.getLastName().toUpperCase());
		values.put("appUrl", 	appUrl);
		values.put("appName", 	appName);
		values.put("token",token);
		
		String templateBody = createBody(values,templateRecoveryPassword);
		System.out.println(subject);
		System.out.println(templateBody);
		System.out.println(user.getEmail());
		try {
			mp.sendEmail(APPLICATION_ADMIN, user.getEmail()+"", subject, templateBody);
		} catch (SendEmailException e) {
			Start.getAuthProvider().revokeTokenToRecoveryPassword(token,user);
			e.printStackTrace();
			throw new SendEmailException(e);
		}
		return user;
	}

	private String createBody(Map<String, String> values,String template){
		 StrSubstitutor sub = new StrSubstitutor(values);
		 String resolvedString = sub.replace(template);
		 return resolvedString;
	}
}
