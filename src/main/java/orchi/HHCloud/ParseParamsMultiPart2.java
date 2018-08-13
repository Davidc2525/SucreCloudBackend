package orchi.HHCloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

public class ParseParamsMultiPart2 {
	private Map<String, Param> paramsMap = new HashMap<String, Param>();

	public ParseParamsMultiPart2(HttpServletRequest req) throws Exception {
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);

		if (!isMultipart) {
			req.getParameterMap().forEach((name, value) -> {
                String valueParam = value[0];

				if(name.equals("args")){

				    if(Base64.isBase64(value[0])){
					    valueParam = new String(Base64.decodeBase64(value[0]));
				    }else{
					    valueParam = value[0];
				    }

				}

				paramsMap.put(name,new Param(name, valueParam));
				System.out.println("add param " + name +" value: "+valueParam);
			});

			return;
		}
		ServletFileUpload upload = new ServletFileUpload();
		try {
			FileItemIterator params = upload.getItemIterator(req);
			while (params.hasNext()) {
				FileItemStream param = params.next();

				if (param.isFormField()) {
					String name = param.getFieldName();
					String value = Streams.asString(param.openStream());

					if(name.equals("args")){
					    if(Base64.isBase64(value)){
					        value = new String(Base64.decodeBase64(value));
				        }else{
					        value = value;
				        }
					}

					paramsMap.put(name,new Param(name, value));

					System.out.println("add param " + name +" value: "+value);
				}

			}
		} catch (FileUploadException | IOException e) {
			e.printStackTrace();
		}
	}


	public Param getParam(String name) {
		return paramsMap.get(name);
	}

	public String getString(String name)  {
		Param item = paramsMap.get(name);
		if (item == null) {
			return null;
		}

		return item.getContent();
	}

	private static InputStream copyInputStream(InputStream input) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int len;
		while ((len = input.read(buffer)) > -1) {
			baos.write(buffer, 0, len);
		}
		baos.flush();
		InputStream inputCopy = new ByteArrayInputStream(baos.toByteArray());

		return inputCopy;
	}

	private static class Param {

		private String fieldName;


		private String content;



		public Param(String fieldName, String content) {
			this.fieldName = fieldName;
			this.content = content;

		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

	}

}
