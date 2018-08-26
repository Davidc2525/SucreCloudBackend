package orchi.HHCloud.stores.HdfsStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.Start;

import orchi.HHCloud.store.ContextStore;
import orchi.HHCloud.store.Range;

public class HdfsManager {
	private static Logger log = LoggerFactory.getLogger(HdfsManager.class);
	public static Boolean isLocalFileSystem;

	private static HdfsManager instance;
	public static HdfsAdmin dfsAdmin;
	private String hdfsuri;// getIsLocalFileSystem() ? "file:///":
							// "hdfs://orchi2:9000";
	public static String root;// = getIsLocalFileSystem() ?
								// "/home/david/HHCloudFsStore/mi_dfs/":"/mi_dfs/";
	
	public FileSystem fs;
	public FileSystem dfs;
	private Random rfs = new Random();
	public HdfsManager() {
		this(false);
	}
	
	public FileSystem getFs(){
		
		return fs;		
	}

	public HdfsManager(Boolean isLocal) {
		setIsLocalFileSystem(isLocal);
		log.info("Iniciando administrador hdfs");
		log.info("isLocalFileSystem: {}", getIsLocalFileSystem());
		Configuration conf = new Configuration();

		hdfsuri = getIsLocalFileSystem()
				? Start.conf.getString("store.hdfs.hdfsmanager.fs.defaultFS.local")
				: Start.conf.getString("store.hdfs.hdfsmanager.fs.defaultFS.dfs");

		root = getIsLocalFileSystem()
				? Start.conf.getString("store.hdfs.hdfsmanager.path.store.local")
				: Start.conf.getString("store.hdfs.hdfsmanager.path.store.dfs");

		log.debug("hdfs uri {}", hdfsuri);
		log.debug("root system {}", root);

		conf.set("fs.defaultFS", hdfsuri);
		conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
		conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

		try {
			fs = FileSystem.get(URI.create(hdfsuri), conf);
			if (!isLocalFileSystem) {
				dfsAdmin = new HdfsAdmin(fs.getUri(), conf);
			}


			Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(() -> {
				try {
					FileSystem.printStatistics();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}, 0, 5, TimeUnit.SECONDS);

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static HdfsManager getInstance() {
		if (instance == null) {
			instance = new HdfsManager(false);
		}
		return instance;
	}

	public static HdfsManager getInstance(boolean isLocal) {
		if (instance == null) {
			instance = new HdfsManager(isLocal);
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
		Long totalReads = 0L;
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0 && totalReads < contentLength) {
			out.write(b, 0, numBytes);
			totalReads += numBytes;
		}
		in.close();
		
		in = null;
		// out.close();

	}

	public void readFile(Path path, OutputStream out) throws IOException {

		if (!fs.exists(path)) {
			System.out.println("File " + path.getName() + " does not exists");
			return;
		}

		FSDataInputStream in = fs.open(path);

		byte[] b = new byte[1024 * 1024];
		int numBytes = 0;
		while ((numBytes = in.read(b)) > 0) {
			out.write(b, 0, numBytes);
		}
		in.close();
		in = null;
		// out.close();

	}

	public void writeFile(Path path, InputStream inStream) throws IOException {

		if (!fs.exists(path) || true) {

			System.out.println("creando archivo " + path.toString());
			FSDataOutputStream f = fs.create(path,
					true);/*
							 * ,1024*4, new Progressable() {
							 * 
							 * @Override public void progress() {
							 * 
							 * System.out.println(String.
							 * format("copiando %s ...",path.toString()));
							 * 
							 * } });
							 */
			byte[] b = new byte[1024];
			int numBytes = 0;
			while ((numBytes = inStream.read(b)) > 0) {
				f.write(b, 0, numBytes);
			}

			inStream.close();
			// f.flush();
			// f.hflush();
			f.close();

			return;
		}

		// out.close();
		// fs.close();
	}

	public void deletePath(Path path) throws IOException {

		if (fs.exists(path)) {

			if (fs.isDirectory(path)) {
				fs.delete(path, true);
			}
			if (fs.isFile(path)) {
				fs.delete(path, true);
			}

		}

		// out.close();
		// fs.close();
	}

	public static String getRoot(String uid) {
		return root + uid + "/files/";
	}

	/**
	 * Crea una nueva rruta añadiendo el valor root como '/' para no permitir
	 * salir de su contexto de almacenamiento de el usuario y entrar a otro <br>
	 * si el usuario quiere entrar a una rruta de su unidad '/mi
	 * imagenes/img1.jpg' se añadira la rruta root del sistema HHCloud donde se
	 * almacenan las carpetas root de los usuarios registrados <br>
	 * para q funcione las rrutas absolutas ingresadas por los usuarios con las
	 * rrutas absolutas del sistema HHCloud, temporalmente, y se eliminara
	 * cuando se muestre la informacion al cliente <br>
	 * 
	 * <pre>
	 *  si tenemos '/mi imagenes/img1.jpg'
	 *  se añade la rruta root configurada dependiendo de si es en sistema local o distribuido
	 *	
	 *	asumiendo q la rruta root es del sistema local '/home/david/HHCloudFsStore/mi_dfs/'
	 *	la rruta completa queda: '/home/david/HHCloudFsStore/mi_dfs/${iduser}/mi imagenes/img1.jpg'
	 *
	 *	Ó
	 *
	 *	asumiendo q la rruta root es del sistema distribuido 'hdfs://${host}:${port}/mi_dfs/'
	 *	la rruta completa queda: 'hdfs://${host}:${port}/mi_dfs/${iduser}/mi imagenes/img1.jpg'
	 * </pre>
	 * 
	 * 
	 * @param userId
	 *            contiene la ruta root del sistema mas la id del usuario
	 * @param contiene
	 *            la rruta a la q quiere acceder el usuario
	 */
	public static Path newPath(String userId, String path) {
		//Path p = new Path(Paths.get("/", path).normalize().toString());

		//p = Path.mergePaths(new Path(getRoot(pRoot)), p);
		java.nio.file.Path p = Paths.get(root,ContextStore.toUserContext(userId, path).toString());
		System.err.println("newPath " +p);
		return new Path(p.normalize().toString());
	}

	/**
	 * @return the isLocalFileSystem
	 */
	public static Boolean getIsLocalFileSystem() {
		return isLocalFileSystem;
	}

	/**
	 * @param isLocalFileSystem
	 *            the isLocalFileSystem to set
	 */
	public static void setIsLocalFileSystem(Boolean isLocalFileSystem) {
		HdfsManager.isLocalFileSystem = isLocalFileSystem;
	}
}
