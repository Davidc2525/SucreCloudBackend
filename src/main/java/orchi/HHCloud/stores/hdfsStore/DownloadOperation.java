package orchi.HHCloud.stores.hdfsStore;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.RemoteIterator;
import org.json.JSONObject;
import org.slf4j.*;

import orchi.HHCloud.Util;
import orchi.HHCloud.Api.Fs.operations.IOperation;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;
import orchi.HHCloud.stores.hdfsStore.ZipFiles;

public class DownloadOperation implements IOperation {
	private static Logger log = LoggerFactory.getLogger(DownloadOperation.class);
	private static FileSystem fs = HdfsManager.getInstance().fs;
	private java.util.List<Object> paths;

	public DownloadOperation() {
	}

	public DownloadOperation(AsyncContext ctx, JSONObject arg) {
		log.info("Nueva operacion de descarga.");
		//fs = HdfsManager.getInstance().fs;
		String root = arg.getString("root");
		String path = arg.getString("path");
		paths = (arg.has("paths") && !arg.isNull("paths")) ?arg.getJSONArray("paths").toList():null;
		Path opath = new Path(HdfsManager.newPath(root, path).toString());
		HttpServletResponse r = ((HttpServletResponse) ctx.getResponse());
		if(paths!=null){
			log.info("Descarga de multiples archivos {}",paths);

			java.util.List<Path> yetPaths = paths.stream()
			.map(x->new Path(HdfsManager.newPath(root, (String)x).toString()))
			.filter(x->{
				try {
					return fs.exists(x);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return false;
			}).collect(Collectors.toList());;
			
			
			try{

				r.addHeader("Content-Disposition", " attachment; filename=\"" + opath.getName() + ".zip\"");

				MultiTree tree = new MultiTree(fs,yetPaths);
				ZipFiles zip = new ZipFiles(tree, ctx.getResponse().getOutputStream());
				zip = null;
				tree = null;

				log.info("Operacion de descarga terminada {}",opath.toString());
				
				ctx.getResponse().flushBuffer();
				ctx.complete();
				
			}catch(IOException e){
				
			}
			return ;
		}
		try {
			if (!fs.exists(opath)) {
				log.info("{} no existe",opath.toString());
				ctx.getResponse().getWriter().println("no exists" + opath.toString());
				log.info("Operacion de descarga terminada {}",opath.toString());
				ctx.complete();
			}
			if (fs.isFile(opath)) {
				log.info("Descargar archivo {}",opath.toString());
				FileStatus fileStatus = fs.getFileLinkStatus(opath);
				System.out.println("		" + fileStatus.getPath().getName());
				r.addHeader("Content-Disposition", " attachment; filename=\"" + fileStatus.getPath().getName() + "\"");

				HdfsManager.getInstance().readFile(opath, ctx.getResponse().getOutputStream());
				// ctx.getResponse().getWriter().println( opath.toString() );
				log.info("Operacion de descarga terminada {}",opath.toString());
				ctx.complete();
			}

			if (fs.isDirectory(opath)) {
				log.info("Descargar directorio {}",opath.toString());

				//r.addHeader("Transfer-Encoding","gzip");
				r.addHeader("Content-Disposition", " attachment; filename=\"" + opath.getName() + ".zip\"");

				MultiTree tree = new MultiTree(fs,Arrays.asList(opath));
				ZipFiles zip = new ZipFiles(tree, ctx.getResponse().getOutputStream());
				zip = null;
				tree = null;

				log.info("Operacion de descarga terminada {}",opath.toString());
				ctx.getResponse().flushBuffer();
				ctx.complete();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public JSONObject call() {

		return null;
	}
	
	public static class MultiTree {
		//public ArrayList<String> pathsS = new ArrayList<String>();
		public ArrayList<PathAndDepth> paths = new ArrayList<PathAndDepth>();
		//public Map<String, Integer> pathMap = new HashMap<String, Integer>();
		private FileSystem fs;
		private int depth = -1;

		public MultiTree (FileSystem fs, Path path) throws FileNotFoundException, IOException {
			log.info("Nuevo arbol de directorio {}",path);
			this.fs = fs;
			get(path,null);
			log.info("Arbol de directorio multiple {} completado. ",path);
			log.info("\t {} elementos.",paths.size());
		}

		public MultiTree (FileSystem fs,List<Path> ps) throws FileNotFoundException, IOException {
			log.info("Nuevo arbol de directorio multiple {}",ps);
			this.fs = fs;

			for (Path path : ps) {
				get(path,null);
			}
			log.info("Arbol de directorio multiple {} completado. ",ps);
			log.info("\t {} elementos.",paths.size());
		}

		/**debug*/
		private static String repeat(String value, int x) {
			String r = "";

			for (int i = 0; i < x; i++) {
				r += value;
			}

			return r;
		}

		public void get(Path path,Boolean isFile) throws FileNotFoundException, IOException {
			if(isFile==null){
				if(fs.isFile(path)){
					log.debug("\tfile {}",Util.getPathWithoutRootPath(path+""));
					paths.add(new PathAndDepth(path.toString().toString(), 0));
					return ;
				}
			}else{
				if(isFile&&fs.isFile(path)){
					log.debug("\tfile {}",Util.getPathWithoutRootPath(path+""));
					paths.add(new PathAndDepth(path.toString().toString(), 0));
					return ;
				}
			}
			
			log.debug("path {}",Util.getPathWithoutRootPath(path+""));

			++depth;
			RemoteIterator<FileStatus> list = fs.listStatusIterator(path);
			while (list.hasNext()) {
				FileStatus item = list.next();
				if (item.isFile() ) {
					
					paths.add(new PathAndDepth((item.getPath().toString()).toString(), depth));
					
					log.debug("\tfile {}",Util.getPathWithoutRootPath(item.getPath().toString()));
					
				}else{
					get(item.getPath(),false);
					depth--;
				}

			}
		}
		/**debug*/
		public void get2(Path path) throws FileNotFoundException, IOException {
			++depth;
			FileStatus[] list = fs.listStatus(path);
			for (FileStatus item : list) {
				if (item.isDirectory() && depth <= 3) {

					// System.out.println(String.format("%s %s %s",
					// repeat("\t",depth),item.getPath().getName(),
					// item.getLen()));
					paths.add(
							new PathAndDepth(Path.getPathWithoutSchemeAndAuthority(item.getPath()).toString(), depth));
					// pathMap.put(item.getPath().getName(), depth);
					get2(item.getPath());
					depth--;
				}

			}
		}
		
		/** debug
		 * el peor */
		public void get3(Path path) throws FileNotFoundException, IOException {
			++depth;
			FileStatus[] list = fs.listStatus(path, new PathFilter() {

				@Override
				public boolean accept(Path path) {
					try {
						return fs.isDirectory(path);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}
			});
			for (FileStatus item : list) {
				if (item.isDirectory() && depth <= 3) {

					// System.out.println(String.format("%s %s %s",
					// repeat("\t",depth),item.getPath().getName(),
					// item.getLen()));
					paths.add(
							new PathAndDepth(Path.getPathWithoutSchemeAndAuthority(item.getPath()).toString(), depth));
					// pathMap.put(item.getPath().getName(), depth);
					get3(item.getPath());
					depth--;
				}

			}
		}
	}

	public static class PathAndDepth {
		private String name;
		private int depth;

		public PathAndDepth(String name, int depth) {
			this.setName(name);
			this.setDepth(depth);

		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the depth
		 */
		public int getDepth() {
			return depth;
		}

		/**
		 * @param depth the depth to set
		 */
		public void setDepth(int depth) {
			this.depth = depth;
		}
	}

	public static class Tree {

		public ArrayList<String> dirs = new ArrayList<String>();;
		public Long totalSize = 0L;
		public Tree(Path path) {
			log.info("new  tree");
			get(path);

		}

		public void get(Path path) {
			log.info("get path {}",path.toString());
			try {
				for (FileStatus item : fs.listStatus(path)) {
					if (item.isFile()) {
						dirs.add(item.getPath().toString());
						totalSize+=item.getLen();
						log.info("\tadd to tree {}",item.getPath().toString());
					}
					if (item.isDirectory()) {
						get(item.getPath());
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
