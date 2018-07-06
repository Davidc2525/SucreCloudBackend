package orchi.SucreCloud.operations;

import java.awt.List;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.json.JSONException;
import org.json.JSONObject;

import orchi.SucreCloud.hdfs.HdfsManager;
import orchi.SucreCloud.hdfs.ZipFiles;

public class DownloadOperation implements IOperation {

	private static FileSystem fs;

	public DownloadOperation() {
	}

	public DownloadOperation(AsyncContext ctx, JSONObject arg) {
		fs = HdfsManager.getInstance().fs;
		String root = arg.getString("root");
		String path = arg.getString("path");
		Path opath = new Path(HdfsManager.newPath(root, path).toString());
		HttpServletResponse r = ((HttpServletResponse) ctx.getResponse());
		
		try {
			if(!fs.exists(opath)){
				ctx.getResponse().getWriter().println("no exists"+ opath.toString()  );
				ctx.complete();
			}
			if(fs.isFile(opath)){			
				FileStatus fileStatus = fs.getFileLinkStatus(opath);
				System.out.println("		"+fileStatus.getPath().getName());
				r.addHeader("Content-Disposition"," attachment; filename=\""+fileStatus.getPath().getName()+"\"");
				
				HdfsManager.getInstance().readFile(opath, ctx.getResponse().getOutputStream());
				//ctx.getResponse().getWriter().println( opath.toString()  );
				ctx.complete();
			}
			
			if(fs.isDirectory(opath)){
				r.addHeader("Content-Disposition"," attachment; filename=\""+opath.getName()+".zip\"");
				;new ZipFiles(new Tree(opath),ctx.getResponse().getOutputStream());
				//ctx.getResponse().getWriter().println("descargar carpeta");
				ctx.complete();
			}
			
			
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	@Override
	public JSONObject call() {

		return null;
	}
	
	public static class Tree{
		 
		public ArrayList<String> dirs = new ArrayList<String>();;

		public Tree(Path path){
			get(path);
			
		}
		
		public void get(Path path){
			try {
				for(FileStatus item : fs.listStatus(path)){
					if(item.isFile()){
						dirs.add(item.getPath().toString());
					}
					if(item.isDirectory()){
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
