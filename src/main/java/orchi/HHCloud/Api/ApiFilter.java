package orchi.HHCloud.Api;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.ParseParamsMultiPart2;
import orchi.HHCloud.Start;
import orchi.HHCloud.Api.ApiManager.ApiDescriptor;

public class ApiFilter implements Filter {
	private static Logger log = LoggerFactory.getLogger(ApiFilter.class);
	private static String ACCESS_CONTROL_ALLOW_ORIGIN = Start.conf.getString("api.headers.aclo");

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest r = (HttpServletRequest) request;
		HttpServletResponse rs = (HttpServletResponse) response;
		rs.setHeader("Access-Control-Allow-Origin",	ACCESS_CONTROL_ALLOW_ORIGIN);
		rs.setHeader("Content-type", "application/json");
		rs.setHeader("Access-Control-Allow-Credentials", "true");


		HttpSession session = r.getSession(false);

		String path = r.getRequestURI().substring(request.getServletContext().getContextPath().length());

		log.debug("PATH {}", path);

		ApiDescriptor apid = ApiManager.getApid(path);

		if(apid==null){
			request.setAttribute("status", "error");
			request.setAttribute("error", "api_no_found");
			request.setAttribute("errorMsg", "Esta api no esta registrada.");
			request.getRequestDispatcher("/test").forward(request, response);
			;
			return;
		}


		if (log.isDebugEnabled()) {
			log.debug(new JSONObject(apid).toString(2));
		}


		if (apid.isIgnored()) {
			log.debug("{}, ignoring", path);
			apid.calls++;
			if (session == null) {
				if (apid.isGsr()) {
					apid.wrongCalls++;
					request.setAttribute("status", "error");
					request.setAttribute("error", "session");
					request.setAttribute("errorMsg", "Debe iniciar session.");
					request.getRequestDispatcher("/test").forward(request, response);
					;
					return;
				}
			}
			chain.doFilter(request, response);
		} else {

			ParseParamsMultiPart2 params = null;
			try {
				params = new ParseParamsMultiPart2(r);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String op = params.getString("op");
			String args = params.getString("args");
			if (op == null || op.equalsIgnoreCase("")) {
				apid.wrongCalls++;
				request.setAttribute("status", "error");
				request.setAttribute("params", params);
				request.setAttribute("error", "op_missing");
				request.setAttribute("errorMsg", "Debe proporsionar una operacion.");
				request.getRequestDispatcher("/test").forward(request, response);
				;
				return;
			}
			if (args == null || args.equalsIgnoreCase("")) {
				apid.wrongCalls++;
				request.setAttribute("status", "error");
				request.setAttribute("params", params);
				request.setAttribute("error", "args_missing");
				request.setAttribute("errorMsg", "Debe proporsionar argumentos.");
				request.getRequestDispatcher("/test").forward(request, response);
				;
				return;
			}

			apid.calls++;
			if (!apid.hasOperation(op)) {
				apid.wrongCalls++;
				request.setAttribute("status", "error");
				request.setAttribute("params", params);
				request.setAttribute("error", "op_wrong");
				request.setAttribute("errorMsg", "Esa operacion no esta registrada.");
				request.getRequestDispatcher("/test").forward(request, response);
				;

			} else {

				if (session == null) {
					if (apid.isGsr() || apid.getOperation(op).isSr()) {
						apid.wrongCalls++;
						request.setAttribute("status", "error");
						request.setAttribute("params", params);
						request.setAttribute("error", "session");
						request.setAttribute("errorMsg", "Debe iniciar session.");
						request.getRequestDispatcher("/test").forward(request, response);
						;
						return;
					}
				}

				apid.getOperation(op).calls++;

				log.debug("{}, op: {}, args {}.", path, op, args);

				request.setAttribute("params", params);
				chain.doFilter(request, response);
			}

		}
	}

	@Override
	public void destroy() {

	}

}
