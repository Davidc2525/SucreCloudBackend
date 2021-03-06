package orchi.HHCloud;

import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TEST extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //resp.addHeader("Access-Control-Allow-Origin", "http://orchi:9090");
        //resp.addHeader("Access-Control-Allow-Credentials", "true");

        Object error = req.getAttribute("error");
        Object errorMsg = req.getAttribute("errorMsg");

        JSONObject json = new JSONObject();
        json.put("status", "error");
        json.put("error", error + "");
        json.put("errorMsg", errorMsg + "");
        json.put("msg", errorMsg + "");
        resp.getWriter().println(json);
    }
}
