package orchi.HHCloud.store.response;

public class ListResponse extends Response {
	private long size;
	private long directoryCount;
	private long fileCount;
	///private List<Status> payload;

	public ListResponse() {
		super();
	}

	/*public List<Status> getPayload() {
		return payload;
	}

	public void setPayload(List<Status> payload) {
		this.payload = payload;
	}*/

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getDirectoryCount() {
		return directoryCount;
	}

	public void setDirectoryCount(long directoryCount) {
		this.directoryCount = directoryCount;
	}

	public long getFileCount() {
		return fileCount;
	}

	public void setFileCount(long fileCount) {
		this.fileCount = fileCount;
	}
}
