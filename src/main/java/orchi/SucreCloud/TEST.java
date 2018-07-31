package orchi.SucreCloud;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.json.JSONObject;

public class TEST extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		resp.addHeader("Access-Control-Allow-Origin", "http://localhost:9090");
		resp.addHeader("Access-Control-Allow-Credentials", "true");
		
		JSONObject json = new JSONObject();
		resp.getWriter().println(json.put("name", "david").put("token",req.getParameter("t")));
	}
}
