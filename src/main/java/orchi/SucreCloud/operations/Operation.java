package orchi.SucreCloud.operations;

public enum Operation {
	LIST("list"), 
	GETSTAT("getstat"), 
	DOWNLOAD("download"), 
	PUT("put"), 
	MKDIR("mkdir"), 
	COPY("copy"), 
	MOVE("move");

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
