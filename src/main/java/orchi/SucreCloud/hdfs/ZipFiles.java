package orchi.SucreCloud.hdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import orchi.SucreCloud.Util;

public class ZipFiles {

	private FileSystem fs;

	public ZipFiles(orchi.SucreCloud.operations.DownloadOperation.Tree tree, OutputStream servletOutputStream) {
		fs = HdfsManager.getInstance().fs;
		try {
			
			ZipOutputStream zos = new ZipOutputStream(servletOutputStream);
			//level 0 para solo empaquetar, es mas rapido
			zos.setLevel(0);
			for (String item : tree.dirs) {
				addToZipFile(item, zos);
			}

			zos.close();
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void addToZipFile(String fileName, ZipOutputStream zos) {

		System.out.println("Writing '" + fileName + "' to zip file");

		ZipEntry zipEntry = new ZipEntry(Util.nc(fileName));
		try {
			zos.putNextEntry(zipEntry);
			HdfsManager.getInstance().readFile(new Path(fileName), zos);

		} catch (IOException e) {

			// e.printStackTrace();
		} finally {
			try {
				zos.closeEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

	}

}