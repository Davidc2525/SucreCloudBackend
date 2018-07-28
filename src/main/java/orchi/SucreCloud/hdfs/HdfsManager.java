package orchi.SucreCloud.hdfs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;

import orchi.SucreCloud.RestApi.Opener.Range;

public class HdfsManager {

	private static HdfsManager instance;
	private String hdfsuri = "hdfs://orchi2:9000";
	public FileSystem fs;
	public FileSystem dfs;

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
			//dfs = DistributedFileSystem.get(URI.create(hdfsuri),conf);
			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(()->{
				try {
					fs.printStatistics();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} , 0, 5, TimeUnit.SECONDS);

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
	
	public void readFile(Path path, OutputStream out, Range range) throws IOException {

		if (!fs.exists(path)) {
			System.out.println("File " + path.getName() + " does not exists");
			return;
		}

		FSDataInputStream in = fs.open(path);
		in.seek(range.range[0]);
		long contentLength = range.getContentLength();
		byte[] b = new byte[1024];
		Long totalReads=0L;
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0 && totalReads < contentLength ) {
			out.write(b, 0, numBytes);
			totalReads+=numBytes;	
		}
		in.close();
		in=null;
		//out.close();

	}

		
	public void readFile(Path path, OutputStream out) throws IOException {

		if (!fs.exists(path)) {
			System.out.println("File " + path.getName() + " does not exists");
			return;
		}

		FSDataInputStream in = fs.open(path);

		byte[] b = new byte[1024 * 1024];
		int totalReads=0;
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0) {
			out.write(b, 0, numBytes);
			totalReads+=numBytes;
			//String parte = "Leido "+totalReads;
			//System.out.println(String.format("read %s", totalReads));
		}

		in.close();
		in=null;
		// out.close();

	}

	public void writeFile(Path path, InputStream inStream) throws IOException {

		if (!fs.exists(path)||true) {

			System.out.println("creando archivo " + path.toString());
			FSDataOutputStream f = fs.create(path,true);/*,1024*4, new Progressable() {

				@Override
				public void progress() {

					System.out.println(String.format("copiando %s ...",path.toString()));

				}
			});*/
			byte[] b = new byte[1024];
			int numBytes = 0;
			while ((numBytes = inStream.read(b)) > 0) {
				f.write(b, 0, numBytes);

			}

			inStream.close();
			//f.flush();
			//f.hflush();
			f.close();

			return;
		}

		// out.close();
		// fs.close();
	}
	public void deletePath(Path path) throws IOException {

		if (fs.exists(path)) {

			if(fs.isDirectory(path)){
				fs.delete(path, true);
			}
			if(fs.isFile(path)){
				fs.delete(path,true);
			}



		}

		// out.close();
		// fs.close();
	}

	public static Path newPath(String pRoot, String path) {
		Path p = new Path(Paths.get("/", path).normalize().toString());

		p = Path.mergePaths(new Path(pRoot), p);
		return p = new Path(Paths.get(p.toString()).normalize().toString());
	}
}
