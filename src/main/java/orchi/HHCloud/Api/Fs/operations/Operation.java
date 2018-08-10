package orchi.HHCloud.Api.Fs.operations;

public enum Operation {
	LIST("list"), 
	GETSTATUS("getstatus"), 
	DOWNLOAD("download"), 
	PUT("put"), 
	MKDIR("mkdir"), 
	COPY("copy"), 
	MOVE("move"), 
	RENAME("rename"),
	DELETE("delete");

	public final String name;

	Operation(String s) {
		name = s;
	}

	public boolean equalsName(String otherName) {
		// (otherName == null) check is not needed because name.equals(null)
		// returns false
		return name.equals(otherName);
	}

	public String toString() {
		return this.name;
	}
}
