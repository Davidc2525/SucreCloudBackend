HHCloud server
======================

__HHCloud__ te permite crear una nube de almacenamiento sumamente facil y rapido, con poca configuracion, haciendo uso de su [interfaz web](https://github.com/Davidc2525/HHCloud#imagen-de-presentacion), se complementan para poder prestar el servicio, HHCloud consta de 2 partes:

- Servidor: Gestiona las peticiones de el cliente.
- [Cliente](https://github.com/Davidc2525/HHCloud): Tiene La interfaz adactada a las prestaciones del servidor.


### Uso

```sh
$ git clone https://github.com/Davidc2525/HHCloudBackend.git
$ cd HHCloudBackend
$ mvn compile assembly:single
$ java -cp target/HHCloud-1.0-SNAPSHOT-jar-with-dependencies.jar orchi.HHCloud.Prepare
$ mvn exec:java
```
Predeterminadamente el host y el puerto donde correra el servicio, es **localhost**  y el puerto **8080**, si quieres cambiar de forma rapida el host, o el host y el puerto sin tener que editar la [configuración](src/main/resources/application.properties) lo puedes hacer de la siguiente manera

### solo host

```sh
...
mvn exec:java -Dexec.args="localhost"
```
### host y puerto
```sh
...
mvn exec:java -Dexec.args="localhost 8080"
```

### Administracion
El servidor presta un servicio de administracion por el puerto **2626**, puedes deshabilitar el servicio en el archivo de [configuración](src/main/resources/application.properties) o cambiar el puerto de escucha
```properties
...
#configurar si el servidor de servicio para administracion esta activo o no
admin.adminservice.enable=true
#puerto donde estara activo el servicio administracion
admin.adminservice.port=2626
...
```
para administrar el servidor se hace uso de un software de escritorio incluido en los paquetes, para ejecutarlo tiens que tener la direccion en donde tienes __java__ instalado para poder hacer uso de unas librerias, tiene que estar el servidor activo para que la herramienta puede iniciarse

```sh
java -cp target/HHCloud-1.0-SNAPSHOT-jar-with-dependencies.jar:$JAVA_HOME/jre/lib/ext/jfxrt.jar orchi.HHCloud.HHCloudAdmin.Main
```
El programa de administracion hace uso de la misma [configuración](src/main/resources/application.properties) que esta usando el servidor.

__SE TIENE QUE USAR LA MISMA CONFIGURACION TANTO PARA EL SERVIDOR COMO PARA EL ADMINISTRADOR__.

### Almacenamiento

HHCloud para gestionar la parte de almacenamiento hace uso de un sistema distribuido de archivos [HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html), pero permite usar otros como puede ser el sistema de almacenamient del propio sistema operativo u otros. Dentro del funcionamiento el almacenamiento se abstrae en una [interface](/src/main/java/orchi/HHCloud/store/StoreProvider.java) que permite hacer tus propias implementaciones, dentro del sistema existe 3 implementaciones:
- FsStore: Viene establecida por defecto, hace uso del sistema de archivos del sistema operatvo para almacenar los datos.
- GlusterFsStore: Una implementacion adactada para almacenar los datos en [GlusterFs](https://www.gluster.org/).
- HdfsStoreProvider: Esta implementacion esta basada en el sistema de archivos distribuido [HDFS](https://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html), permitiendo hacer replicacion de los datos, en un cluster de almacenamiento, trae consigo una mayor seguridad de almacenamiento de los datos, para poder hacer uso de esta implementacion tienes que tener configurado un servidor donde este corriendo ese sistema de almacenamiento y editar la [configuracion](src/main/resources/application.properties) para que pueda hacer uso de el.

```properties
...
store.storemanager.storeprovider=orchi.HHCloud.stores.HdfsStore.HdfsStoreProvider
....
```

### Gestion de usuarios

HHCloud para la parte de gestion de los usuarios se cuenta con una [interfaz](src/main/java/orchi/HHCloud/user/UserProvider.java) para poder hacer tus propias implementaciones, la [implementacion](src/main/java/orchi/HHCloud/user/EmbeddedUserProvider.java) por defecto esta preparada para cubrir todo el aspecto de usuarios.

### Base de datos

HHCloud para el sistema de gestion de base de datos al igual que los aspectos anteriores cuenta con una [interfaz](src/main/java/orchi/HHCloud/database/ConnectionProvider.java) y una [implementacion](src/main/java/orchi/HHCloud/database/EmbeddedConnectionProvider.java) debido a que HHCloud quiere ser un sistema facil y rapido de usar y que mantenga una integracion limpia la implementacion esta montada sobre un administrador de base de datos empotrada [Apache Derby](https://db.apache.org/derby/) se usa como administrador de base de datos, debido a que ayuda a tener el sistema integrado, puedes hacer tus propias implementaciones y editar la [configuracion](src/main/resources/application.properties) para usar tu propia implementacion. Implementacion por defecto en la configuracion.
```properties
...
db.dbmanager.connection.provider=orchi.HHCloud.database.EmbeddedConnectionProvider
...
```

### Envio de correos

HHCloud en cierta parte de la gestion de usuario, hace uso de el envio de correos, para eso se describe una [interfaz](src/main/java/orchi/HHCloud/mail/MailProvider.java) y una [implementacion](src/main/java/orchi/HHCloud/mail/GoogleGmailProvider.java). Tambien puedes escribir tus propiaas implementaciones y usarlas editando la [configuracion](src/main/resources/application.properties), existen dos implementaciones con dos proveedores distintos de servicio, uno para Gmail y otro para OutLook, el proveedor por defecto es el que se muestra debajo en la configuracion, hace uso de Gmail para enviar los mensajes por correo.


```properties
...
mail.mailmanager.mail.provider=orchi.HHCloud.mail.GoogleGmailProvider
mail.mailmanager.mail.admin=hhcloud25@gmail.com
...
```


### Configuracion
Puedes editar la configuracion para personalizar el comportamiento y adactarlo a sus necesidades, el archivo de [configuración](src/main/resources/application.properties).
