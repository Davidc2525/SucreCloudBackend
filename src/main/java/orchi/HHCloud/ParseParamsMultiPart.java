package orchi.HHCloud;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
//import org.mortbay.log.Log;

public class ParseParamsMultiPart {
	private Map<String, FileItemStream> paramsMap = new HashMap<String, FileItemStream>();

	public ParseParamsMultiPart(HttpServletRequest req) throws Exception {
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);

		if(!isMultipart){
			throw new Exception("los parametros no son multipar/data-form");
		}
		ServletFileUpload upload = new ServletFileUpload();
		try {
			FileItemIterator params = upload.getItemIterator(req);
			while (params.hasNext()) {
				FileItemStream param = params.next();
				System.out.println("add param " + param.getFieldName());
				paramsMap.put(param.getFieldName(),
						new Param(
							param.getFieldName(),
							param.getHeaders(),
							param.getName(),
							param.isFormField(),
							param.getContentType(),
							param.openStream())
				);

			}
		} catch (FileUploadException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public InputStream getAsStream(String name) throws IOException {
		FileItemStream item = paramsMap.get(name);
		if (item == null) {
			return null;
		}
		return item.openStream();
	}

	public FileItemStream getParam(String name){
		return paramsMap.get(name);
	}

	public String getAsString(String name) throws IOException {
		FileItemStream item = paramsMap.get(name);
		if (item == null) {
			return null;
		}
		return Streams.asString(item.openStream());
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

	private static class Param implements FileItemStream {

		private String fieldName;
		private FileItemHeaders headers;
		private String name;
		private boolean formField;
		private String contentType;
		private InputStream inputStream;

		public Param(String fieldName, FileItemHeaders headers, String name, boolean formField, String contentType, InputStream inputStream) {
			this.fieldName = fieldName;
			this.headers = headers;
			this.name = name;
			this.formField = formField;
			this.contentType = contentType;
			try {
				this.inputStream = copyInputStream(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public FileItemHeaders getHeaders() {
			return headers;
		}

		@Override
		public void setHeaders(FileItemHeaders arg0) {

		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		public String getFieldName() {
			return fieldName;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isFormField() {
			return formField;
		}

		@Override
		public InputStream openStream() throws IOException {
			// TODO Auto-generated method stub
			return inputStream;
		}



	}

}
