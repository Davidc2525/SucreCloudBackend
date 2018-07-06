package orchi.SucreCloud.hdfs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HdfsManager {

	private static HdfsManager instance;
	private String hdfsuri = "hdfs://orchi:9000";
	public FileSystem fs;

	public HdfsManager() {
		Configuration conf = new Configuration();
		// Set FileSystem URI
		conf.set("fs.defaultFS", hdfsuri);
		// Because of Maven
		conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
		conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
		// Set HADOOP user
		// System.setProperty("HADOOP_USER_NAME", "david");
		// System.setProperty("hadoop.home.dir", "/");
		// Get the filesystem - HDFS
		try {
			 fs = FileSystem.get(URI.create(hdfsuri), conf);
			for (FileStatus f : fs.listStatus(new Path("/"))) {

				System.err.println(f.getPath().toString());

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static HdfsManager getInstance() {
		if (instance == null) {
			instance = new HdfsManager();
		}
		return instance;
	}

	public static Path newPath(String pRoot, String path) {
		Path p = new Path(Paths.get("/", path).normalize().toString());

		p = Path.mergePaths(new Path(pRoot), p);
		return p;
	}
}
