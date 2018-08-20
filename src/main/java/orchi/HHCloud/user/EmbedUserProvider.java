package orchi.HHCloud.user;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.json.JSONObject;

import com.google.api.client.util.Base64;

import orchi.HHCloud.Start;
import orchi.HHCloud.auth.Exceptions.TokenException;
import orchi.HHCloud.cipher.CipherProvider;
import orchi.HHCloud.database.ConnectionProvider;
import orchi.HHCloud.database.DbConnectionManager;
import orchi.HHCloud.mail.MailProvider;
import orchi.HHCloud.mail.Exceptions.SendEmailException;
import orchi.HHCloud.user.Exceptions.UserAleardyExistsException;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.Exceptions.UserMutatorException;
import orchi.HHCloud.user.Exceptions.UserMutatorPassword;
import orchi.HHCloud.user.Exceptions.UserNotExistException;
import org.slf4j.*;

/**
 * Proveedor de usuarios con base de datos empotrada
 * tambien sirve para jdbc:mysql, modificando la configuracion, con el proveedor de base de dato 
 * @author Colmenares David
 *
 */
public class EmbedUserProvider implements UserProvider {
	private static final Logger log = LoggerFactory.getLogger(EmbedUserProvider.class);
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
	private final ThreadPoolExecutor executor;
	private CipherProvider ciplherProvider = Start.getCipherManager().getCipherProvider();
	private ConnectionProvider provider;
	//private Connection conn;
	private UserValidator userValidator = new DefaultUserValidator();
	private String templateEmailVerify;
	private String templateRecoveryPassword;

	public EmbedUserProvider() {
		log.debug("Iniciando EmbedUserProvider");
		try {
			templateEmailVerify = Streams.asString(EmbedUserProvider.class.getResourceAsStream("/templateVerifyEmail.html"));
			templateRecoveryPassword = Streams.asString(EmbedUserProvider.class.getResourceAsStream("/templateRecoveryPasswordEmail.html"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Start.getDbConnectionManager();
		provider = DbConnectionManager.getInstance().getConnectionProvider();
		//conn = provider.getConnection();
		executor = new ThreadPoolExecutor(100, 100, 10L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(1000000));
	}


	@Override
	public User getUserById(String userId) throws UserNotExistException ,UserException{
		log.debug("getUserById {}",userId);
		User user = null;
		ResultSet result;
		Connection conn = null;
		PreparedStatement stm = null;
		try {
			conn = provider.getConnection();
			
			stm = conn.prepareStatement(SELECT_USERS_WHERE_ID);
			stm.setString(1, (userId));
			result = stm.executeQuery();

			if (result.next()) {
				user = buildUserFromResult(result);
				log.debug("User found {}",user);
			} else {
				throw new UserNotExistException("Usuario con id: " + userId + ", no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		} finally {
			try {
				stm.close();
				conn.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		return user;
	}

	@Override
	public User getUserByEmail(String userEmail) throws UserNotExistException,UserException {
		log.debug("getUserByEmail {}",userEmail);
		User user = null;
		ResultSet result;
		Connection conn =null;
		PreparedStatement stm = null;
		try {
			conn = provider.getConnection();
			stm = conn.prepareStatement(SELECT_USERS_WHERE_EMAIL);
			stm.setString(1, escape(userEmail));
			result = stm.executeQuery();
			
			if (result.next()) {
				user = buildUserFromResult(result);
				log.debug("User found {}",user);
			} else {
				throw new UserNotExistException("Usuario con " + userEmail + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		} finally {
			try {
				stm.close();
				conn.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		System.err.println("UserByEmail: " + user);
		return user;
	}

	@Override
	public User getUserByUsername(String userName) throws UserNotExistException,UserException {
		log.debug("getUserByUsername {}",userName);
		User user = null;
		ResultSet result;
		Connection conn =null;
		PreparedStatement stm = null;
		try {
			conn = provider.getConnection();
			stm = conn.prepareStatement(SELECT_USERS_WHERE_USERNAME);
			stm.setString(1, escape(userName));
			result = stm.executeQuery();

			if (result.next()) {
				user = buildUserFromResult(result);
				log.debug("User found {}",user);
			} else {
				throw new UserNotExistException("Usuario con " + userName + " no existe.");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		}  finally {
			try {
				stm.close();
				conn.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		System.err.println("UserByEmail: " + user);
		return user;
	}

	@Override
	public void createUser(User newUser) throws UserException {
		log.debug("createUser {}",newUser);
		DataUser user = ((DataUser) newUser);
		try {
			getUserById(newUser.getId());
			throw new UserAleardyExistsException("Ya existe un usuario registrado con ese id: " + user.getId());
		} catch (UserNotExistException e1) {
			try {
				getUserByEmail(newUser.getEmail());
				throw new UserAleardyExistsException("Ya existe un usuario registrado con ese email: " + user.getEmail());
			} catch (UserNotExistException e2) {
				Connection conn =null;
				PreparedStatement userInsert = null;
				try {
					conn = provider.getConnection();
					userInsert = conn.prepareStatement(INSERT_INTO_USERS);

					userInsert.setString(1, escape(user.getId()));
					userInsert.setString(2, escape(user.getEmail()));
					userInsert.setBoolean(3, (user.isEmailVerified()));
					userInsert.setString(4, escape(user.getUsername()));
					userInsert.setString(5, escape(user.getFirstName()));
					userInsert.setString(6, escape(user.getLastName()));
					userInsert.setString(7, escape(user.getGender()));
					userInsert.setBigDecimal(8, new BigDecimal(user.getCreateAt()));
					userInsert.setString(9, ciplherProvider.encrypt(user.getPassword()));
					userInsert.executeUpdate();
					sendVerifyEmail(user);
					log.debug("User created {}, sendind email to veridy account",user);
				} catch (SQLException e) {
					e.printStackTrace();
					throw new UserException(e.getMessage());
				} finally {
					try {
						userInsert.close();
						conn.close();
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				}
			}

		}

	}

	@Override
	public void deleteUser(User user) throws UserException {
		log.debug("deleteUser {}",user);
		PreparedStatement userDelete = null;
		Connection conn =null;
		try {
			 conn = provider.getConnection();
			userDelete = conn.prepareStatement(DELETE_USERS_WHERE_ID);
			userDelete.setString(1, escape(user.getId()));
			userDelete.executeUpdate();
			log.debug("User deleted {}",user);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		} finally {
			try {
				userDelete.close();
				conn.close();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

	}

	@Override
	public User changePasswordUser(UserMutatorPassword userMutator) throws UserMutatorException, UserException {
		log.debug("changePasswordUser {}",userMutator.getUser());
		User user = userMutator.getUser();
		String nPassword = userMutator.getPassword();
		String nPasswordEncrypt = ciplherProvider.encrypt(nPassword);
		Connection conn =null;
		PreparedStatement updatePass = null;
		try {
			conn = provider.getConnection();
			updatePass = conn.prepareStatement(UPDATE_PASS_USER);
			updatePass.setString(1, nPasswordEncrypt);
			updatePass.setString(2, user.getId());
			updatePass.executeUpdate();

			log.debug("Password changued to {}",user);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());

		} finally {
			try {
				updatePass.close();
				conn.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return getUserById(user.getId());

	}


	@Override
	public User editUser(User userWithChanges) throws UserException {
		log.debug("editUser {}",userWithChanges);
		DataUser editUser = (DataUser) userWithChanges;
		DataUser oldUser = (DataUser)getUserById(editUser.getId());
		System.out.println("editUser "+editUser);
		Connection conn =null;
		PreparedStatement userUpdate = null;
		try {
			conn = provider.getConnection();
			userUpdate = conn.prepareStatement(UPDATE_USER);

			userUpdate.setString(1, escape(editUser.getEmail()));
			//userUpdate.setBoolean(2, editUser.isEmailVerified());
			userUpdate.setString(2, escape(editUser.getUsername()));
			userUpdate.setString(3, escape(editUser.getFirstName()));
			userUpdate.setString(4, escape(editUser.getLastName()));
			userUpdate.setString(5, escape(editUser.getGender()));
			userUpdate.setString(6, escape(oldUser.getId()));
			userUpdate.executeUpdate();
			log.debug("User chaged from: {}\nto: {}",oldUser,editUser);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException(e.getMessage());
		} finally {
			try {
				userUpdate.close();
				conn.close();
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
		log.debug("setVerifyEmail {}",user);
		Connection conn =null;
		PreparedStatement verifyEmail = null;;
		if(isEmailVerified(user)){
			return getUserById(user.getId());
		}
		try {
			conn = provider.getConnection();
			verifyEmail = conn.prepareStatement(UPDATE_EMAIL_VERIFIED);
			verifyEmail.setBoolean(1, true);
			verifyEmail.setString(2, user.getId());;
			verifyEmail.executeUpdate();
			log.debug("ACCOUNT VERIFIED {}",user);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserException("No se pudo verificar el email. "+e.getMessage());
		} finally {
			try {
				verifyEmail.close();
				conn.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return getUserById(user.getId());
	}

	private boolean isEmailVerified(User user) throws UserNotExistException, UserException{
		boolean isVerified = false;
		isVerified = ((DataUser) getUserById(user.getId())).isEmailVerified();
		log.debug("isEmailVerified {} {}",isVerified,user);
		return isVerified;
	}


	@Override
	public User sendVerifyEmail(User user) throws UserException {
		log.debug("sendVerifyEmail {}", user);
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

		if(log.isDebugEnabled()){
			log.debug("" +
					"\tTo {}\n" +
					"\tValues {}\n" +
					"\tSubject {}\n" +
					"\tBody {}\n",

					user.getEmail(),values,subject,templateBody);
		}
		CompletableFuture.runAsync(()->{
			log.debug("enviado correo de verificacion.");
			try {
				mp.sendEmail(APPLICATION_ADMIN, user.getEmail()+"", subject, templateBody);
				log.debug("Correo de verificacion enviado.");
			} catch (SendEmailException e) {
				log.error("No se pudo enviar el correo de verificacion.");
				Start.getAuthProvider().revokeTokenToVerifyEmail(idToken);
				e.printStackTrace();
			}

		},executor);
		return user;
	}


	@Override
	public User sendRecoveryPasswordEmail(User user) throws UserException, SendEmailException {
		log.debug("sendRecoveryPasswordEmail {}",user);
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
		if(log.isDebugEnabled()){
			log.debug("" +
							"\tTo {}\n" +
							"\tValues {}\n" +
							"\tSubject {}\n" +
							"\tBody {}\n",

					user.getEmail(),values,subject,templateBody);
		}
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