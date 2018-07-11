package orchi.SucreCloud;

import java.nio.file.Paths;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

public abstract class Util {
	public static String type(FileStatus i) {
		// TODO Auto-generated method stub
		return i.isFile() ? "file" : "folder";
	}
	

	
	public static String getPathWithoutRootPath(String in) {
		java.nio.file.Path p = Paths.get(Path.getPathWithoutSchemeAndAuthority(new Path(in)).toString());
		
		p = p.subpath(2, p.getNameCount());
		return Paths.get(p + "").normalize() + "";
	}
}
