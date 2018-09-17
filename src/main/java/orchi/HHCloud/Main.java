package orchi.HHCloud;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import orchi.HHCloud.AdminService.AdminServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.client.HdfsAdmin;
import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;

import orchi.HHCloud.auth.GenerateToken;
import orchi.HHCloud.cipher.CipherManager;
import orchi.HHCloud.mail.MailProvider;
import orchi.HHCloud.mail.Exceptions.SendEmailException;
import orchi.HHCloud.share.DefaultShareProvider;
import orchi.HHCloud.share.ShareProvider;
import orchi.HHCloud.store.ContextStore;
import orchi.HHCloud.store.arguments.DeleteArguments;
import orchi.HHCloud.store.arguments.GetStatusArguments;
import orchi.HHCloud.store.arguments.ListArguments;
import orchi.HHCloud.store.arguments.MkDirArguments;
import orchi.HHCloud.store.arguments.MoveOrCopyArguments;
import orchi.HHCloud.store.arguments.RenameArguments;
import orchi.HHCloud.stores.HdfsStore.HdfsManager;
import orchi.HHCloud.stores.HdfsStore.HdfsStoreProvider;
import orchi.HHCloud.user.BasicUser;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.Exceptions.UserException;

/**
 * Aqui se hacen pruebas.
 * */
public class Main {
	private static Logger log = LoggerFactory.getLogger(Main.class);
	public static void main(String[] args) throws FileNotFoundException, IllegalArgumentException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, URISyntaxException, GeneralSecurityException, XmlRpcException {

		System.out.println("HHCloud pruebas!");
		//orchi.HHCloud.HHCloudAdmin.Main.main(args);
		System.out.println(FileUtils.byteCountToDisplaySize(7231242343L));
		new AdminServer();
		if (true) {
			return;
		}


		System.out.printf(WordUtils.capitalize("david colmenares"));

		java.nio.file.Path ppp = Paths.get("/montart/HDFS/mi_dfs");
		System.out.println(ppp.subpath(1,ppp.getNameCount())+"");



		String ss = null;

		try {

			// run the Unix "ps -ef" command
			// using the Runtime exec method:
			Process p = Runtime.getRuntime().exec("gluster volume quota hhcloud list");

			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(p.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((ss = stdInput.readLine()) != null) {
				System.out.println(ss);
			}

			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((ss = stdError.readLine()) != null) {
				System.out.println(ss);
			}

			System.exit(0);
		}
		catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}



		String urlEncoded = "%2FUP%202%2FGTA%2BVice%2BCity%2Bby%2BNJ-Tutoriales.rar";//URLEncoder.encode("/a b","UTF-8");
		String urlDecoded = URLDecoder.decode(urlEncoded,"UTF-8");
		log.info("decoded {} {}",urlEncoded,urlDecoded);
		log.info("{}",new JSONObject(new StringBuilder(urlDecoded)));
		log.info("{}",Paths.get(urlDecoded));
		log.info("{}",new Path(Paths.get(urlDecoded)+""));
		FileSystem fs = HdfsManager.getInstance().getFs();
		HdfsAdmin admin = HdfsManager.getInstance().dfsAdmin;
		DistributedFileSystem dfs = (DistributedFileSystem) fs;
		//	dfs.setQuotaByStorageType(new Path("/mi_dfs/1234/files"),StorageType.DISK, 78_555_432_078L + 10_000);
		//admin.setSpaceQuota(new Path("/mi_dfs/1234/files"),78_555_432_078L + 10_000);
		ContentSummary cs = dfs.getContentSummary(new Path("/mi_dfs/1535289546807/files/"));
		System.out.println("getDirectoryCount: "+cs.getDirectoryCount());
		System.out.println("getFileCount: "+cs.getFileCount());
		System.out.println("getLength: "+cs.getLength());
		System.out.println("getSpaceQuota: "+cs.getSpaceQuota());
		System.out.println("getSpaceConsumed(): "+cs.getSpaceConsumed());
		System.out.println("getQuota: "+cs.getQuota());
		System.out.println("isTypeConsumedAvailable: "+cs.isTypeConsumedAvailable());
		System.out.println("isTypeQuotaSet: "+cs.isTypeQuotaSet());
		System.out.println("getHeader(true): "+cs.getHeader(true));

		Start.conf.getList("app.folders.wellcome").forEach(x->{
			log.info("csm {}",x);
		});
		log.info("{}",Start.conf.getList("app.folders.wellcome"));

	ShareProvider s = Start.getShareManager().getShareProvider();
	java.nio.file.Path pa = Paths.get("/respaldo/9/1.jpg");
	BasicUser use2 = new BasicUser();
	use2.setId("123");
	s.createShare(use2, Paths.get("Nueva carpeta 1"));
	
	
	BasicUser use = new BasicUser();
	use.setId("1234");
	//DefaultShareProvider s = new DefaultShareProvider();
	
	
	log.info("is shared B {}",s.isShared(use, pa));;
	
	s.createShare(use, pa);
	s.createShare(use, Paths.get("/respaldo"));;
	
	s.createShare(use, Paths.get("/respaldo/9/19888493_362559294162537_3723173154512699392_n.mp4"));;
	
	//log.info("shareds {}",s.sharesInDirectory(use, Paths.get("/")) );
	
	log.info("shareds {}",s.sharedInDirectory(use, Paths.get("/respaldo/9")) );
	
	//s.deleteShare(use, Paths.get("/"));
	
	log.info("shareds {}",s.sharedInDirectory(use, Paths.get("/respaldo/9")) );
	log.info("shareds {} user 123 ",s.sharedInDirectory(use2, Paths.get("/")) );
	int j=0;
	while(j<1){
		log.info("{} is shared A {}",++j,s.isShared(use, pa));;
	}
	
	HdfsManager.getInstance(true);
		ObjectMapper om = new ObjectMapper();
		om.enable(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT);
		om.getJsonFactory();
		BasicUser us = new BasicUser();
		us.setId("1234");
		
		RenameArguments ar = new RenameArguments(Paths.get("me lleva/3"), Paths.get("me lleva/3 nueva"));
		ar.setUser(us);;
		
		System.err.println(om.writeValueAsString(new HdfsStoreProvider().rename(ar)));
			
		
		
		MoveOrCopyArguments amoc = new MoveOrCopyArguments( Paths.get("me lleva/2"), Paths.get("me lleva/3"));
		amoc.setUser(us);
		System.err.println(om.writeValueAsString(new HdfsStoreProvider().move(amoc)));
		
		
		DeleteArguments adelete = new DeleteArguments(Paths.get("me lleva/"));
		adelete.setPaths(Arrays.asList(Paths.get("me lleva/1"),Paths.get("me lleva/3")));
		adelete.setUser(us);
		System.err.println(om.writeValueAsString(new HdfsStoreProvider().delete(adelete)));
		
		
		
		MkDirArguments amkd = new MkDirArguments(Paths.get("me lleva"));
		amkd.setUser(us);
		//System.err.println(om.writeValueAsString(new HdfsStoreProvider().mkdir(amkd)));
		
		
		ListArguments a = new ListArguments();
		a.setUser(us);
		a.setPath(Paths.get("/musica/Of Mice & Men - Unbreakable (Official Music Video).mp4"));
		System.err.println(om.writeValueAsString(new HdfsStoreProvider().list(a)));
		 
		GetStatusArguments gsa = new GetStatusArguments();
		gsa.setPath(Paths.get("/"));
		gsa.setPaths(Arrays.asList(Paths.get("/asc"),Paths.get("/"),Paths.get("/musica/Heart Of A Coward - Severance (full album) HD 320kbps.mp4")));
		gsa.setUser(us);
		System.err.println(om.writeValueAsString(new HdfsStoreProvider().status(gsa)));
	if(true){
		return;
	}	
	
	String en = CipherManager.getInstance().getCipherProvider().encrypt("Test de sífrado");
	String des = CipherManager.getInstance().getCipherProvider().decrypt(en);
	log.info("{}\n{}",en,des);
	int x = 0;
	while(x<1){
		en = CipherManager.getInstance().getCipherProvider().encrypt("TEST-"+x);
		des = CipherManager.getInstance().getCipherProvider().decrypt(en);
		log.info("{}\n{}",en,des);
		x++;
	}
	
	
	AeadConfig.register();
    String keysetFilename = Thread.class.getResource("/").getPath()+"keyset.json";
    System.err.println(Thread.class.getResource("/").getPath());
	KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withFile(new File(keysetFilename)));
	
	// 1. Generate the key material.
    //KeysetHandle keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM);

    //CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(new File(keysetFilename)));
    String plaintext = "David23030487";
    // 2. Get the primitive.
    Aead aead = AeadFactory.getPrimitive(keysetHandle);

    // 3. Use the primitive to encrypt a plaintext,
    byte[] ciphertext = aead.encrypt(plaintext.getBytes(), "pass".getBytes());
    
    byte[] ciphertext2 = aead.encrypt(plaintext.getBytes(), "pass".getBytes());

    // ... or to decrypt a ciphertext.
    byte[] decrypted = aead.decrypt(ciphertext, "pass".getBytes());
    byte[] decrypted2 = aead.decrypt(ciphertext, "pass".getBytes());
    new FileOutputStream("/home/david/claveSifrada").write(org.apache.commons.net.util.Base64.encodeBase64(ciphertext));;
    
    log.info("\n1: {} \n2:{} \n{}\n{}",org.apache.commons.net.util.Base64.encodeBase64String(ciphertext),new String(ciphertext2),new String(decrypted),new String(decrypted2));
		
    

		DataUser u = new DataUser();
		u.setId("TEST");

		ContextStore.createUserContext(u);


		System.out.println(Paths.get("/mi_dfs/","/user/","files",Paths.get(
				"/hola","//files/","1","2","../../../../../"
				).normalize().toString()));

		System.err.println(GenerateToken.newToken());


		MailProvider mp = Start.getMailManager().getProvider();
		try {
			mp.sendEmail("david25pcxtreme@gmail.com", "david25pcxtreme@gmail.com", "email provider 1", "hola con proveedor de correo 1");
		} catch (SendEmailException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


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
			//Start.getStoreManager().getStoreProvider().createStoreContextToUser(user);
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
