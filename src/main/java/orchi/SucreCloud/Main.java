package orchi.SucreCloud;

import java.awt.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.RemoteIterator;

import orchi.SucreCloud.hdfs.HdfsManager;

public class Main {

	public static void main(String[] args) throws FileNotFoundException, IllegalArgumentException, IOException {

		System.out.println("SucreCLoud!");
		
		
		
		FileSystem fs = HdfsManager.getInstance().fs;

		/*
		 * RemoteIterator<FileStatus> list = fs.listStatusIterator(new
		 * Path("/mi_dfs/david"));
		 * 
		 * while (list.hasNext()) { FileStatus item = list.next();
		 * System.out.println(String.format("%s %s", item.getPath(),
		 * item.getLen())); }
		 */
		int X = 0;
		while (X < 1) {
			X++;
			System.err.println("comienso " + X);
			Tree nt = new Tree(fs, 
					Arrays.asList(
							new Path("/mi_dfs/david/src")
							)
					);


			// System.out.println(nt.paths);
			nt.paths.forEach((x) -> {
				// System.out.println(x);
				System.out.println(String.format("%s %s %s", x.depth, Tree.repeat("\t", x.depth), x.name));
			});
			System.err.println("listo" + X);
		}

	}

	public static class Tree {
		public ArrayList<String> pathsS = new ArrayList<String>();
		public ArrayList<PathAndDepth> paths = new ArrayList<PathAndDepth>();
		public Map<String, Integer> pathMap = new HashMap<String, Integer>();
		private FileSystem fs;
		private int depth = -1;

		public Tree(FileSystem fs, Path path) throws FileNotFoundException, IOException {

			this.fs = fs;
			get(path);
		}

		public Tree(FileSystem fs, java.util.List<Path> ps) throws FileNotFoundException, IOException {
			this.fs = fs;

			for (Path path : ps) {
				get(path);
			}
		}

		public static String repeat(String value, int x) {
			String r = "";

			for (int i = 0; i < x; i++) {
				r += value;
			}

			return r;
		}

		public void get(Path path) throws FileNotFoundException, IOException {
			if(fs.isFile(path)){
				paths.add(new PathAndDepth(Util.getPathWithoutRootPath(path.toString()).toString(), 0));
				return ;
			}

			++depth;
			RemoteIterator<FileStatus> list = fs.listStatusIterator(path);
			while (list.hasNext()) {
				FileStatus item = list.next();
				if (item.isFile() ) {

					// System.out.println(String.format("%s %s %s",
					// repeat("\t",depth),item.getPath().getName(),
					// item.getLen()));
					paths.add(new PathAndDepth(Util.getPathWithoutRootPath(item.getPath().toString()).toString(), depth));
					// pathMap.put(item.getPath().getName(), depth);
					// pathsS.add(item.getPath().toString());
					
				}else{
					get(item.getPath());
					depth--;
				}

			}
		}

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

		/** el peor */
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
			this.name = name;
			this.depth = depth;

		}
	}

}
