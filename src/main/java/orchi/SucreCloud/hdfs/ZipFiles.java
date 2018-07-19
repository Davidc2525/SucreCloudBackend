package orchi.SucreCloud.hdfs;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;

import orchi.SucreCloud.Util;
import orchi.SucreCloud.operations.DownloadOperation.PathAndDepth;

public class ZipFiles {
	private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

	private FileSystem fs;

	public ZipFiles(orchi.SucreCloud.operations.DownloadOperation.MultiTree tree, OutputStream outputStream) {
		log.debug("creating new zip file");
		fs = HdfsManager.getInstance().fs;
		int total = tree.paths.size();
		int current = 0;
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(outputStream));
		//level 0 para solo empaquetar, es mas rapido
		zos.setLevel(0);
		for (PathAndDepth item : tree.paths) {
			try {
				current++;

				addToZipFile(item.getName(), zos);
				log.debug("{} by {} files, less {}", current,total,total-current);
			} catch (Exception e) {
				log.error("La compresion del archivo se cancelo por parte del usuario");
				break;
			}
		}

		try {
			zos.flush();
			zos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		log.debug("zip file created, terminated");

	}



	public void addToZipFile(String fileName, ZipOutputStream zos) throws Exception {

		log.debug("Escribiendo '{}' a zip",fileName );
		log.debug("thread '{}' a zip",Thread.currentThread());

		ZipEntry zipEntry = new ZipEntry(Util.getPathWithoutRootPath(fileName));
		try {
			zos.putNextEntry(zipEntry);

			HdfsManager.getInstance().readFile(new Path(fileName), zos);

			zos.closeEntry();

		} catch (IOException e) {
			
			e.printStackTrace();
			throw new Exception("Error al agregar ");
			
		}

	}

}