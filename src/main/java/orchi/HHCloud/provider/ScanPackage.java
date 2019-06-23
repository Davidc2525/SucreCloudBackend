package orchi.HHCloud.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

public class ScanPackage {

    private static Logger log = LoggerFactory.getLogger(ScanPackage.class);
    private String packageName;
    private List<String> ignorePackageNames = new ArrayList<>();

    private List<Class<?>> classes = new ArrayList<>();

    public ScanPackage(String packageName) {

        log.debug("ScanPackage: package {}", packageName);
        this.packageName = packageName;
    }

    public ScanPackage(String packageName, List<String> ignorePackageNames) {

        log.debug("ScanPackage package {} ignore: {}", packageName, ignorePackageNames);

        this.packageName = packageName;
        this.ignorePackageNames = ignorePackageNames;
        try {
            getClasses();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Class<?>> getList() {
        return this.classes;
    }

    /**
     * Escanea todas las clases accesibles desde el contesto de el cargador de clases "ClassLoader" desde un paquete y sus sub paquetes
     *
     * @return Las clases
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void getClasses()
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');

        Enumeration resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList();
        while (resources.hasMoreElements()) {
            URL resource = (URL) resources.nextElement();
            log.info(path+" "+resource.getProtocol());
            if (resource.getProtocol().equals("jar")) {
                try {
                    dirs.add(new File(getClass().getResource("/"+path).toURI()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                dirs.add(new File(resource.getFile()));
            }

        }

        for (File directory : dirs) {

            log.debug("resource: {}", directory.getPath());
            findClasses(directory, packageName).forEach(new Consumer<Class<?>>() {
                @Override
                public void accept(Class<?> aClass) {
                    if (!classes.contains(aClass)) {
                        classes.add(aClass);
                    }
                }
            });

        }
        //return (Class[]) classes.toArray(new Class[classes.size()]);
    }

    /**
     * Metodo recursivo usado para encontrar todas las clases en un directorio dado y sub directorios
     *
     * @param directory   Directorio base
     * @param packageName Nombre del paquete para encotrar las clases dentro de el directorio base
     * @return Clases
     * @throws ClassNotFoundException
     */
    private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List classes = new ArrayList();
        if (!directory.exists()) {
            log.warn("{} no exists", directory.getPath());
            return classes;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {

                assert !file.getName().contains(".");
                log.debug("directory: " + file.toPath().toString());
                String newPackageName = packageName + "." + file.getName();
                if (!ignorePackageNames.contains(newPackageName)) {
                    classes.addAll(findClasses(file, newPackageName));
                }

            } else {

                //boolean contains = file.getPath().replace("/", ".").contains("orchi.HHCloud.HHCloudAdmin");
                if (file.getName().endsWith(".class")) {
                    log.debug("class: " + file.toPath().toString());

                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));

                }
            }
        }
        return classes;
    }

}
