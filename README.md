HHCloud server api
======================

Miembros @Davidc2525 y @mrgab0

Uso:

 - mvn compile assembly:single
 - java -cp target/HHCloud-1.0-SNAPSHOT-jar-with-dependencies.jar orchi.HHCloud.Prepare
 - java -cp target/HHCloud-1.0-SNAPSHOT-jar-with-dependencies.jar orchi.HHCloud.Start hostName port
 
Puedes editar la configuracion para personalizar el comportamiento y adactarlo a sus necesidades, el archivo de [configuración](src/main/resources/application.properties).

