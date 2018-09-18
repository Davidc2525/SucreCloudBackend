package orchi.HHCloud;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class ParseParamsMultiPart2 {
    private static Logger log = LoggerFactory.getLogger(ParseParamsMultiPart2.class);
    private Map<String, Param> paramsMap = new HashMap<String, Param>();

    public ParseParamsMultiPart2(HttpServletRequest req) throws Exception {
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);

        if (!isMultipart) {
            req.getParameterMap().forEach((name, value) -> {
                String valueParam = value[0];

                if (name.equals("args")) {

                    if (Base64.isBase64(value[0])) {
                        valueParam = new String(Base64.decodeBase64(value[0]));
                    } else {
                        valueParam = value[0];
                    }

                }
                String valueParamUrlDecoded = null;
                try {
                    valueParamUrlDecoded = URLDecoder.decode(valueParam, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    valueParamUrlDecoded = valueParam;
                    log.error("Name character encoding is not supported, the value is no modified {}", valueParam);
                }
                log.debug("Param {}\nvalue {}\ndecoded {}", name, valueParam, valueParamUrlDecoded);
                paramsMap.put(name, new Param(name, valueParamUrlDecoded));
                log.debug("add param " + name + " value: " + valueParam);
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

                    if (name.equals("args")) {
                        if (Base64.isBase64(value)) {
                            value = new String(Base64.decodeBase64(value));
                        } else {
                            value = value;
                        }
                    }

                    String valueParamUrlDecoded = null;
                    try {
                        valueParamUrlDecoded = URLDecoder.decode(value, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        valueParamUrlDecoded = value;
                        log.error("Name character encoding is not supported, the value is no modified {}", value);
                    }
                    log.debug("Param {}\nvalue {}\ndecoded {}", name, value, valueParamUrlDecoded);

                    paramsMap.put(name, new Param(name, valueParamUrlDecoded));

                    log.debug("add param " + name + " value: " + valueParamUrlDecoded);
                }

            }
        } catch (FileUploadException | IOException e) {
            e.printStackTrace();
        }
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

    public Param getParam(String name) {
        return paramsMap.get(name);
    }

    public String getString(String name) {
        Param item = paramsMap.get(name);
        if (item == null) {
            return null;
        }

        return item.getContent();
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
