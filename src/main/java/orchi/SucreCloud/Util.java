package orchi.SucreCloud;

import java.nio.file.Paths;

import org.apache.hadoop.fs.FileStatus;

public abstract class Util {
	public static String type(FileStatus i) {
		// TODO Auto-generated method stub
		return i.isFile() ? "file" : "folder";
	}

	public static String nc(String in) {
		java.nio.file.Path p = Paths.get(in);

		p = p.subpath(4, p.getNameCount());
		return Paths.get(p + "").normalize() + "";
	}
}
