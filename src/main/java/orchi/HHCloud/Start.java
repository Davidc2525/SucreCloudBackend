package orchi.HHCloud;

import orchi.HHCloud.AdminService.AdminServer;
import orchi.HHCloud.Api.ApiManager;
import orchi.HHCloud.Api.Auth.Auth;
import orchi.HHCloud.Api.Fs.Fs;
import orchi.HHCloud.Api.Opener.Opener;
import orchi.HHCloud.Api.Uploader.Uploader;
import orchi.HHCloud.Api.User.Users;
import orchi.HHCloud.auth.AuthProvider;
import orchi.HHCloud.auth.DefaultAuthProvider;
import orchi.HHCloud.auth.logIO.LogInAndOut;
import orchi.HHCloud.cipher.CipherManager;
import orchi.HHCloud.conf.ConfManager;
import orchi.HHCloud.database.DbConnectionManager;
import orchi.HHCloud.mail.MailManager;
import orchi.HHCloud.quota.QuotaManager;
import orchi.HHCloud.share.ShareManager;
import orchi.HHCloud.store.StoreManager;
import orchi.HHCloud.user.UserManager;
import org.apache.commons.configuration2.Configuration;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.SessionCookieConfig;
import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author david
 */
public class Start {
    public static Server server;
    public static Configuration conf = ConfManager.getInstance().getConfig();
    private static Logger log = LoggerFactory.getLogger(Start.class);

    public static void main(String[] args) throws Exception {

        int port = conf.getInt("api.port");
        String host = conf.getString("api.host");

        if (args.length == 1) {
            host = args[0];
        }
        if (args.length == 2) {
            host = args[0];
            port = Integer.valueOf(args[1]);
        }


        QueuedThreadPool threadPool = new QueuedThreadPool(100, 10000, 100000, new LinkedBlockingQueue<Runnable>(10000));
        threadPool.setName(conf.getString("app.name"));

        server = new Server(threadPool);

        // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();

        http_config.setSecureScheme("https");
        http_config.setSecurePort(2525);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(false);
        http_config.setSendDateHeader(false);

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setPort(port);
        http.setHost(host);
        http.setIdleTimeout(Long.MAX_VALUE);

        server.addConnector(http);

        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);

        servletContext.setContextPath("/api");

        //servletContext.addFilter(ApiFilter.class, "/*", null);

        servletContext.addServlet(ApiManager.addApi(Fs.class, Fs.apiName), Fs.apiName);
        servletContext.addServlet(ApiManager.addApi(Users.class, Users.apiName), Users.apiName);
        servletContext.addServlet(ApiManager.addApi(Auth.class, Auth.apiName), Auth.apiName);
        servletContext.addServlet(ApiManager.addApi(Opener.class, Opener.apiName), Opener.apiName);
        servletContext.addServlet(ApiManager.addApi(Uploader.class, Uploader.apiName), Uploader.apiName);

        servletContext.addServlet(TEST.class, "/test").setAsyncSupported(true);

        HashSessionIdManager idManager = new HashSessionIdManager();
        server.setSessionIdManager(idManager);
        HashSessionManager manager = new HashSessionManager();
        manager.setStoreDirectory(new File("./sessions"));
        SessionCookieConfig cc = manager.getSessionCookieConfig();
        cc.setName(conf.getString("app.name") + "-S");
        SessionHandler sessions = new SessionHandler(manager);

        servletContext.setSessionHandler(sessions);
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        sessions.getSessionManager();
        contexts.setHandlers(new Handler[]{servletContext});

        server.setHandler(contexts);

        log.info("Iniciando servidor");
        server.start();
        new AdminServer();
        ApiManager.showDescriptions();
        server.join();
    }

    public static UserManager getUserManager() {
        return UserManager.getInstance();
    }

    public static AuthProvider getAuthProvider() {
        return DefaultAuthProvider.getInstance();
    }

    public static LogInAndOut getLoginAndOut() {
        return LogInAndOut.getInstance();
    }

    public static StoreManager getStoreManager() {
        return StoreManager.getInstance();
    }

    public static QuotaManager getQuotaManager() {
        return QuotaManager.getInstance();
    }

    public static DbConnectionManager getDbConnectionManager() {
        return DbConnectionManager.getInstance();
    }

    public static MailManager getMailManager() {
        return MailManager.getInstance();
    }

    public static CipherManager getCipherManager() {
        return CipherManager.getInstance();
    }

    public static ShareManager getShareManager() {
        return ShareManager.getInstance();
    }
}