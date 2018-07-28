package orchi.SucreCloud.RestApi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.SucreCloud.hdfs.HdfsManager;

public class Opener extends HttpServlet {
	private Logger log = LoggerFactory.getLogger(Opener.class);
	private Long readedParts = 0L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// resp.addHeader("Access-Control-Allow-Origin",
		// "http://localhost:9090");
		resp.addHeader("Access-Control-Allow-Credentials", "true");
		log.debug("Abrir contenido de archivo.");
		Path p;
		try {
			ParseOpenerHeaders headers = new ParseOpenerHeaders(req);
			if (headers.headers.containsKey("Range")) {
				String hRange = new ParseOpenerHeaders(req).headers.get("Range");
				String endodePath = new ParseOpenerParams(req).params.get("path");
				String decodePath = new decodeParam(endodePath).decodedParam;
				Path path  = new Path("/mi_dfs/david/" + decodePath);
				Long fileSize = HdfsManager.getInstance().fs.getFileStatus(path).getLen();
				String mime = Files.probeContentType(Paths.get(path.toString()));
				
				Range range = new Range(hRange,fileSize);
				Long[] ranges = range.range;
				long contentLength = range.getContentLength();
				
				log.debug("contenido parcial");
				log.debug("cabesera {}",hRange);
				log.debug("ruta decodificada {}",decodePath);
				log.debug("rango de consulta {} {}",ranges[0],ranges[1]);
				log.debug("tamaño de contenido de salida {}",contentLength);
				log.debug("tamaño del contenido total {}",fileSize);
				log.debug("tipo de mime {}",mime);
				log.debug("------------- {}",++readedParts);

				
				
				resp.setStatus(206);
				resp.setHeader("Accept-Ranges", "bytes");
				resp.setHeader("Content-Length", contentLength + "");
				resp.setHeader("Content-Range", "bytes " + ranges[0] + "-" + ranges[1] + "/" + fileSize);
				resp.setHeader("Content-Type", mime);
				HdfsManager.getInstance().readFile(path, (resp.getOutputStream()), range);

			} else {

				String endodePath = new ParseOpenerParams(req).params.get("path");
				String decodePath = new decodeParam(endodePath).decodedParam;
				Path path  = new Path("/mi_dfs/david/" + decodePath);
				String mime = Files.probeContentType(Paths.get(path.toString()));
				Long fileSize = HdfsManager.getInstance().fs.getFileStatus(path).getLen();
				
				log.debug("contenido total");
				log.debug("ruta decodificada {}",decodePath);
				log.debug("tamaño del contenido total {}",fileSize);
				log.debug("tipo de mime {}",mime);
				log.debug("------------- {}",++readedParts);
				
				resp.setHeader("Content-Length", fileSize + "");
				resp.setHeader("Content-Type", mime);
				HdfsManager.getInstance().readFile(path, resp.getOutputStream());
			}
		} catch (Exception e1) {
			
			//e1.printStackTrace();
		}

	}

	public static class ParseOpenerParams {
		public Map<String, String> params = new HashMap<>();
		private HttpServletRequest r;

		public ParseOpenerParams(HttpServletRequest r) {
			this.r = r;

			Enumeration<String> nps = r.getParameterNames();
			while (nps.hasMoreElements()) {
				String name = nps.nextElement();
				params.put(name, this.r.getParameter(name));
			}
		}
	}

	public static class ParseOpenerHeaders {
		public Map<String, String> headers = new HashMap<>();
		private HttpServletRequest r;

		public ParseOpenerHeaders(HttpServletRequest r) {
			this.r = r;

			Enumeration<String> headerNames = r.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String hName = headerNames.nextElement();
				headers.put(hName, this.r.getHeader(hName));
			}
		}
	}

	public static class Range {
		public Long[] range = { 0L, 0L };
		private String reg = "(?:bytes)=(\\d+)-(\\d+)?";
		private Pattern pattern = Pattern.compile(reg);
		private long contentLength;

		public Range(String headerRange,Long fileSize) {
			//System.out.println(headerRange);
			Matcher m = pattern.matcher(headerRange);
			// System.out.println(m.matches());
			if (m.matches()) {
				range[0] = Long.valueOf(m.group(1));
				if (m.group(2) == null) {
					range[1] = (1024L * 1024L);
				}
				if (m.group(2) != null) {
					range[1] = Long.valueOf(m.group(2));
				}
				if (range[0] > range[1]) {
					range[1] = range[0] + range[1];
				}
				
				if(range[1]>fileSize){
					range[1] = fileSize-1;
				}
				
				setContentLength(range[1] - range[0] + 1);

			}

		}

		/**
		 * @return the contentLength
		 */
		public long getContentLength() {
			return contentLength;
		}

		/**
		 * @param contentLength the contentLength to set
		 */
		public void setContentLength(long contentLength) {
			this.contentLength = contentLength;
		}

	}

	public static class decodeParam {
		private String p;
		public String decodedParam = "";

		public decodeParam(String param) throws Exception {
			this.p = param;

			if (p != null && !p.equalsIgnoreCase("")) {
				String[] parts = p.split(":");

				for (String part : parts) {
					decodedParam += Character.toString((Character.toChars(Integer.valueOf(part, 16)))[0]);
				}
			} else {
				throw new Exception("La cadena no puede ser null o estar vacia.");
			}

		}
	}

}
