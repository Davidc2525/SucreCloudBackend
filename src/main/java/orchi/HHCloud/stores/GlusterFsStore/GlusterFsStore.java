package orchi.HHCloud.stores.GlusterFsStore;

import orchi.HHCloud.Start;
import orchi.HHCloud.quota.Exceptions.QuotaException;
import orchi.HHCloud.quota.Quota;
import orchi.HHCloud.store.ContentSummary;
import orchi.HHCloud.store.StoreProvider;
import orchi.HHCloud.stores.HdfsStore.HdfsManager;
import orchi.HHCloud.stores.HdfsStore.HdfsStoreProvider;
import orchi.HHCloud.user.DataUser;
import orchi.HHCloud.user.User;
import org.apache.hadoop.fs.FileSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;


public class GlusterFsStore extends HdfsStoreProvider implements StoreProvider {

	@Override
	public void init(){

	}
	
	@Override
	public void start(){
		HdfsManager.getInstance(true);
	}

	@Override
	public Quota setQuota(User user, Path path, long size)throws QuotaException {
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path+"" );
		Path path1 = Paths.get(p.toString());
		Path path2 = Paths.get("/",path1.subpath(1,path1.getNameCount()).toString()).normalize();
		runProccess("sudo gluster volume quota hhcloud limit-usage "+path2.toString()+" "+size);
		return new Quota(size);
	}

	@Override
	public void removeQuota(User user, Path path) {
		org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path+"" );
		Path path1 = Paths.get(p.toString());
		Path path2 = Paths.get("/",path1.subpath(1,path1.getNameCount()).toString()).normalize();
		runProccess("sudo gluster volume quota hhcloud remove "+path2.toString());
	}

	@Override
	public ContentSummary getContentSummary(User user, Path path) {
		ContentSummary cs = new ContentSummary();
		DataUser dUser = (DataUser) user;
		boolean verified = dUser.isEmailVerified();
		try {
			org.apache.hadoop.fs.Path p = HdfsManager.newPath(user.getId(), path.toString());
			FileSystem fs = HdfsManager.getInstance().getFs();
			org.apache.hadoop.fs.ContentSummary fsCs = fs.getContentSummary(p);
			Quota spaceQuota = Start.getQuotaManager().getProvider().getQuota(user);
			cs.setDirectoryCount(fsCs.getDirectoryCount());
			cs.setFileCount(fsCs.getFileCount());
			cs.setSpaceQuota(spaceQuota.getQuota()/*verified ? Start.getStoreManager().SPACE_QUOTA_SIZE : Start.getStoreManager().SPACE_QUOTA_SIZE_NO_VERIFIED_USER*/);
			cs.setSpaceConsumed(fsCs.getSpaceConsumed());
			cs.setLength(fsCs.getLength());
			cs.setQuota(fsCs.getQuota());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cs;
	}


	public String runProccess(String command){
		String ss = null;

		try {

			// run the Unix "ps -ef" command
			// using the Runtime exec method:
			Process p = Runtime.getRuntime().exec(command);

			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(p.getErrorStream()));

			// read the output from the command
			System.out.println("Valor devuelto por la ejecucion:\n");
			while ((ss = stdInput.readLine()) != null) {
				System.out.println(ss);
			}

			// read any errors from the attempted command
			System.out.println("Valor devuelto por la ejecucion (error):\n");
			while ((ss = stdError.readLine()) != null) {
				System.out.println(ss);
			}
		}
		catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
		}

		return ss;
	}
}
