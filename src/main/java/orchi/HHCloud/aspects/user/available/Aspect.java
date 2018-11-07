package orchi.HHCloud.aspects.user.available;

import orchi.HHCloud.Ignoreme;
import orchi.HHCloud.Start;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.UserProvider;
import orchi.HHCloud.user.userAvailable.AvailableDescriptor;
import orchi.HHCloud.user.userAvailable.Exceptions.UserUnAvailableException;
import orchi.HHCloud.user.userAvailable.UserAvailableProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@org.aspectj.lang.annotation.Aspect
public class Aspect {
    public static final String USER_UNAVAILABLE_MSG = "Este usuario se encuetra inhabilidato.\nRazon: %s";
    private static Logger log = LoggerFactory.getLogger(Aspect.class);
    private static UserAvailableProvider uap = Start.getUserManager().getUserAvailableProvider();
    private static UserProvider up = Start.getUserManager().getUserProvider();

    public Aspect(){
        log.info("Aspecto: disponibilidad de usuario");
    }

    @Around("execution (* orchi.HHCloud.auth.AuthProvider.authenticate(orchi.HHCloud.user.User,..))")
    public Object autenticate(ProceedingJoinPoint p) throws Throwable {
        log.debug("Aspecto: disponibilidad de usuario");
        log.debug("Consejo AUTHENTICATE");
        log.debug("Punto de corte: {}",p.getSignature().getDeclaringTypeName() + "." + p.getSignature().getName());
        DataUser u = (DataUser) p.getArgs()[0];
        User u2 = up.getUserByEmail(u.getEmail());
        if (!uap.userIsEnable(u2)) {
            AvailableDescriptor avd = uap.getDescriptor(u2);
            throw new UserUnAvailableException(String.format(USER_UNAVAILABLE_MSG,avd.getReason()));
        }
        return p.proceed();
    }

    @Around("execution (* orchi.HHCloud.Api.*.do*(..))")
    public Object apiAround(ProceedingJoinPoint p) throws Throwable {
        log.debug("Aspecto: disponibilidad de usuario");
        log.debug("Consejo API");
        log.debug("Punto de corte: {}",p.getSignature().getDeclaringTypeName() + "." + p.getSignature().getName());

        HttpServletRequest req = (HttpServletRequest) p.getArgs()[0];
        HttpServletResponse resp = (HttpServletResponse) p.getArgs()[1];
        HttpSession s = req.getSession(false);
        if (s != null) {

            DataUser u = new DataUser();
            u.setId((String) s.getAttribute("uid"));
            if (!uap.userIsEnable(u)) {
                AvailableDescriptor avd = uap.getDescriptor(u);
                sendError(resp,resp, "user_unavailable", new UserUnAvailableException(String.format(USER_UNAVAILABLE_MSG,avd.getReason())));
                Start.server.getSessionIdManager().invalidateAll(s.getId());
                return null;
            }

        }

        return p.proceed();
    }

    public void sendError(HttpServletResponse resp, HttpServletResponse resps, String error, Exception e) throws Exception {
        String ACCESS_CONTROL_ALLOW_ORIGIN = resps.getHeader("Origin");
        resps.setHeader("Access-Control-Allow-Origin", ACCESS_CONTROL_ALLOW_ORIGIN);
        resps.setHeader("Content-type", "application/json");
        resps.setHeader("Access-Control-Allow-Credentials", "true");
        JSONObject json = new JSONObject();
        json.put("status", "error");
        json.put("error", error);
        json.put("errorMsg", e.getMessage());
        json.put("msg", e.getMessage());
        resps.getWriter().println(json.toString(2));
        resps.getWriter().close();

        return;
    }

    @Ignoreme
    private void showUser(User u) throws UserException {
        DataUser dUser = (DataUser) u;

        String comprober = "id";
        if (u.getId() == null || u.getId() == "") {
            comprober = "email";

            if (u.getEmail() == null || u.getEmail() == "") {
                comprober = "username";
            }
        }

        System.out.println("------------- USER: comprobrer: " + comprober);
        System.out.println("------------- USER: " + dUser.getId());
        System.out.println("------------- USER: " + dUser.getEmail());
        System.out.println("------------- USER: " + dUser.getUsername());
        System.out.println("------------- USER: " + dUser.getFirstName());
        System.out.println("------------- USER: " + dUser.getLastName());


    }
}