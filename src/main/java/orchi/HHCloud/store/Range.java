package orchi.HHCloud.store;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range {

	public Long[] range = { 0L, 0L };
	private String reg = "(?:bytes)=(\\d+)-(\\d+)?";
	private Pattern pattern = Pattern.compile(reg);
	private long contentLength;
	private Long sizeRange; //TODO

	public Range(String headerRange,Long fileSize) {
		//System.out.println(headerRange);
		Matcher m = pattern.matcher(headerRange);
		// System.out.println(m.matches());
		if (m.matches()) {
			range[0] = Long.valueOf(m.group(1));
			if (m.group(2) == null) {
				range[1] = (sizeRange);
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