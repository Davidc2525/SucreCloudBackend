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

		java.nio.file.Path p = null;
		Path p2 = new Path(in);

		p = Paths.get(Path.getPathWithoutSchemeAndAuthority(p2).toString());

		if (p2.depth() > 2) {
			p = p.subpath(2, p.getNameCount());
		}{
			p = Paths.get("/",p+"");
		}
		// p = p.subpath(2, p.getNameCount());
		return Paths.get("/",p + "").normalize() + "";
	}
}
