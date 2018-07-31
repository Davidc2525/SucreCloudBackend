package orchi.auth.logIO;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import orchi.SucreCloud.Start;
import orchi.SucreCloud.RestApi.App;
import orchi.auth.Exceptions.AuthException;

import orchi.user.User;

public class LogInAndOut implements LoginAndLogoutInterface {
	private static LogInAndOut instance = null;

	/*private Runnable createTask(Class<? extends Runnable> taskClass,Class<?>[] typeArgs,Object ...rest){
		Runnable task=null; 
		try {
			Constructor<? extends Runnable> nc = taskClass.getConstructor(typeArgs);
			task = nc.newInstance(rest);
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;		
	}*/
	
	/**
	 * mando a cerrar las conecciones de chat en el cluster de chat
	 * a los host by conposer (sessionid:userid) {@link orchi.ClusterSession.ComposeID}
	 * */
	/*private void sendTaskLogOutToClusterChat(HttpSession session,User user){
		try {

			String sessionid = session.getId();
			String userid = user.getId();
			ComposeID composeid = new ComposeID(userid,sessionid);
			IExecutorService executor = AppOrchi.getHazelcastInstance().getExecutorService("message");
			//Collection<String> members = ClusterSessionDataManagerImplIMDG.getInstance().getHostsByUserId(user.getId());
			
			Collection<String> hosts = ClusterFullQualifiedIDSessionDataManagerInterface.getHostsByUserID(composeid);
			System.err.println("host para mandar a cerrar la session en el cluster chat: "+hosts);
			if(hosts.size()>0){
				try{
					executor.executeOnMembers(new CloseSessionsByComposeID(composeid), new MemberSelector() {
						
						@Override
						public boolean select(Member member) {
						
							return hosts.contains(member.getUuid());
						}
					});
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		} catch (NoModeClusterStartException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	

	@Override
	public void logInCallBack(AsyncContext ctx, User user, LoginCallback callback) throws AuthException {

		Start.getAuthProvider().authenticate(user, (authUser) -> {

			HttpSession session = ((HttpServletRequest) ctx.getRequest()).getSession(true);
			
			/*ClusterSessionDataManagerImplIMDG.lock.lock();
			try{
				HttpSession session = ((HttpServletRequest) ctx.getRequest()).getSession(true);
				ClusterSessionDataManagerImplIMDG.getInstance().putSessionIdByUserId(authUser.getId(), session.getId());
				ClusterSessionDataManagerImplIMDG.getInstance().putUserIdBySessionId(authUser.getId(), session.getId());
			}finally {
				ClusterSessionDataManagerImplIMDG.lock.unlock();
			}*/
			
			callback.call(new LoginDataSuccess(authUser, ctx));
			
			
		});
	}

	@Override
	public void logIn(String username, String password) throws AuthException {

		Start.getAuthProvider().authenticate(username, password);

	}

	@Override
	public void logOut(HttpSession session) {
		session.invalidate();
		//String user = ClusterSessionDataManagerImplIMDG.getInstance().getUserBySessionId(session.getId());
		//User userData = new BasicUser().bind(user, null,null,null);
		
		//sendTaskLogOutToClusterChat(session,userData);
		
		//ClusterSessionDataManagerImplIMDG.lock.lock();		
		/*try{
			
			String userId = ClusterSessionDataManagerImplIMDG.getInstance().removeUserIdBySessionId(session.getId());
			ClusterSessionDataManagerImplIMDG.getInstance().removeSessionIdByUserId(userId, session.getId());
			
		}finally {
			ClusterSessionDataManagerImplIMDG.lock.unlock();
		}*/
	}

	@Override
	public void logOut(HttpSession session, Boolean skipListener) {
		session.setAttribute("skipListenerTiggered", skipListener);
		logOut(session);		
	}

	@Override
	public void logOutCallBack(AsyncContext ctx, Boolean skipListener, LogOutCallBack callback) {
		HttpSession session = ((HttpServletRequest) ctx.getRequest()).getSession(false);		
		logOut(session, skipListener);
		callback.call();
	}

	@Override
	public void logOutCallBack(AsyncContext ctx, LogOutCallBack callback) {
		logOut(((HttpServletRequest) ctx.getRequest()).getSession(false));
		callback.call();
	}

	public static LogInAndOut getInstance() {
		if (instance == null)
			instance = new LogInAndOut();
		return instance;
	}

}
