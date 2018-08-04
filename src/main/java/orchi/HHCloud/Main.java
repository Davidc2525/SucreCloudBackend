package orchi.HHCloud;

import java.awt.List;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import orchi.HHCloud.stores.FsStore.FsStore;
import orchi.HHCloud.stores.hdfsStore.HdfsManager;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import orchi.HHCloud.user.Exceptions.UserException;



public class Main {
	private static Logger log = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) throws FileNotFoundException, IllegalArgumentException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException {

		System.out.println("HHCloud pruebas!");
		
		
		Connection con = Start.getDbConnectionManager().getConnection();
		
		DataUser user = new DataUser();
		user.setId("904538689");
		user.setEmail("karelis@gmail.com");
		user.setUsername("karelis.c");
		user.setFirstName("karelis");
		user.setLastName("cerrado");
		user.setCreateAt(System.currentTimeMillis());
		user.setPassword("2525");
		
		try {
			Start.getUserManager().getUserProvider().createUser(user);
			Start.getStoreManager().getStoreProvider().createStoreContextToUser(user);
		} catch (UserException e) {
			
			e.printStackTrace();
		}
		
		/*con.createStatement().execute("create table users (name varchar(32) not null)");
		PreparedStatement psInsert = con.prepareStatement("insert into users(name) values (?)");
		psInsert.setString(1,"david");
		psInsert.executeUpdate();
		*/
		ResultSet myWishes = con.createStatement().executeQuery("SELECT * FROM USERS");
		
		
		while (myWishes.next()) {
			log.info("id {} name {}",myWishes.getString("id"),myWishes.getString("email"));
			//System.out.println("On " + myWishes.getTimestamp(1) + " I wished for " + myWishes.getString(2));
		}
		if(true){
			return;
		}
		
		java.nio.file.Path p = Paths.get("/home/david/HHCloudFsStore/mi_dfs/david/document/asc/");
		
		String path = "/";
		Boolean add = false;
		Iterator<java.nio.file.Path> iter = p.iterator();
		while(iter.hasNext()){
			java.nio.file.Path part = iter.next();
			
			if(add){
				path+="/"+part.toString();
			}
			
			if(part.toString().equals("mi_dfs")){
				add = true;	
				iter.next();
			}		
			
		}
		System.err.println(Paths.get("/",path).normalize());
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		Class.forName(driver).newInstance();
		String protocol = "jdbc:derby:";
		
		Connection conn = DriverManager.getConnection(protocol + "HHCloud;create=false");
		log.info("{}",conn);
		
		/*conn.createStatement().execute("create table users (name varchar(32) not null)");
		PreparedStatement psInsert = conn.prepareStatement("insert into users(name) values (?)");
		psInsert.setString(1,"david");
		psInsert.executeUpdate();
		*/
		 myWishes = conn.createStatement().executeQuery("select * from users");
		while (myWishes.next()) {
			log.info(" name {}",myWishes.getString(1));
			//System.out.println("On " + myWishes.getTimestamp(1) + " I wished for " + myWishes.getString(2));
		}
		
		//new FsStore();
		//log.info("{}",nc.toString());;
		//FileSystem fs = HdfsManager.getInstance().fs;

		/*
		 * RemoteIterator<FileStatus> list = fs.listStatusIterator(new
		 * Path("/mi_dfs/david"));
		 * 
		 * while (list.hasNext()) { FileStatus item = list.next();
		 * System.out.println(String.format("%s %s", item.getPath(),
		 * item.getLen())); }
		 */
		/*int X = 1;
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
*/
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
