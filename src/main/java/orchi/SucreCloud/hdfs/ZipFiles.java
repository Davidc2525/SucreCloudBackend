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
import org.slf4j.*;

import orchi.SucreCloud.Util;

public class ZipFiles {
	private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

	private FileSystem fs;

	public ZipFiles(orchi.SucreCloud.operations.DownloadOperation.Tree tree, OutputStream outputStream) {
		log.debug("creating new zip file");
		fs = HdfsManager.getInstance().fs;
		int total = tree.dirs.size();
		int current = 0;
		try {
			
			ZipOutputStream zos = new ZipOutputStream(outputStream);
			//level 0 para solo empaquetar, es mas rapido
			zos.setLevel(0);
			for (String item : tree.dirs) {
				try {
					current++;
					
					addToZipFile(item, zos);
					log.debug("{} by {} files, less {}", current,total,total-current);
				} catch (Exception e) {
					log.error("La compresion del archivo se cancelo por parte del usuario");
					break;
				}
			}

			zos.close();
			log.debug("zip file created, terminated");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void addToZipFile(String fileName, ZipOutputStream zos) throws Exception {

		log.debug("Escribiendo '{}' a zip",fileName );
		log.debug("thread '{}' a zip",Thread.currentThread());

		ZipEntry zipEntry = new ZipEntry(Util.nc(fileName));
		try {
			zos.putNextEntry(zipEntry);
			HdfsManager.getInstance().readFile(new Path(fileName), zos);
			zos.closeEntry();
		} catch (IOException e) {
			
			throw new Exception("Error al agregar ");
			// e.printStackTrace();
		} 

	}

}