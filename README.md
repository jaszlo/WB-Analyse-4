# WB-Analyse-4
This project contains a simple HTTP-Server developed with Java.
The HTTP-Server works as a REST-API to analyze social network graphs.

## Dependencies
The project is developed with Java 15 and heavily uses features introduced in Java 15 such as text blocks.
As our build automation tool we use Maven thus the following dependencies are required.
In addition to store the data about interactions a MariaSQL database is used. Other SQL-based databases are not supported. For generating graphs as images GraphViz is required as well.
  * Maven3.2 or later - Build the project
  * JDK 15 or later - Run the project
  * A running MariaDB - Store the interaction data
  * Graphviz - Generate graph images for webapp

### Graphviz
For the visual representation of graphs we use Graphviz to generate SVGs. Therefore, Graphviz should
be installed if you want to use the [webapp](doc/admin.md). If you use Windows it is easiest to install Graphviz in the WSL. When generating Graphs following installation locations are searched in the given order 
  * PATH
  * WSL

### MariaDB
To connect to the Database a valid URL to the database is required. How to set the url to the database for the server is explained later.
For further details see the official [documentation](https://mariadb.com/kb/en/about-mariadb-connector-j/).

## Build
To build the project into an executable JAR run
  ```sh
  $ mvn compile assembly:single
  ```
You can find the JAR in the generated folder `target` as `analyse4-server-1.0-jar-with-dependencies.jar"`.

## Running and deploying the JAR
To run the executable JAR run
  ```sh
  $ java -jar target/analyse4-server-1.0-jar-with-dependencies.jar
  ```
## Deployment
The deployment of the server is done by copying the jar to the destination server and running it.
### Manual deployment with ssh
The easy way to deploy the JAR just once is to use `scp` to copy the executable JAR to a SSH-Server.
This can be done by running
```sh
 $ scp target/analyse4-server-1.0-jar-with-dependencies.jar USER@URL:path/to/your/project 
```
For additional information see the man-page for scp [documentation](https://man7.org/linux/man-pages/man1/scp.1.html)
After that you will just need to start the jar on the ssh server.

### Automating the deployment process with Maven
To setup an automated deployment process there are a few extra step but will make it easier to deploy
the project multiple times.  
First thing you will have to do is to create a file called `settings.xml` It should look like this.
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>production</id>
            <username>SSH-Username</username>
            <password>SSH-Password</password>
        </server>
    </servers>
</settings>
```
Obviously you should replace SSH-Username and SSH-Password with your SSH-Username and SSH-Password. 
You are also required to modify the `pom-xml` file as follows
```xml
               ...

    <distributionManagement>
        <repository>
            <id>production</id>
            <url>SSH-Server-URL</url>
        </repository>
    </distributionManagement>
```
Again replace SSH-Server-URL with your own URL. After all that is done simply run
```sh
$ mvn deploy -s settings.xml
```

### Configuration of the deployed server
To configure the deployed server create a file called `config.xml` in the folder with the JAR. With the
following content.
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="port">8080</entry>                                  <!-- port for the http server  -->
    <entry key="logger_level">BASIC</entry>                         <!-- details logged by logger  -->
    <entry key="admin_user">admin</entry>                           <!-- username for the webapp   -->
    <entry key="admin_pass"></entry>                                <!-- password for the webapp   -->
    <entry key="db_url">jdbc:mariadb://URL-to-DB</entry>            <!-- url for the database      --> 
    <entry key="db_user"></entry>                                   <!-- username for the database -->
    <entry key="db_pass"></entry>                                   <!-- password for the database -->
</properties>
```
You might as well just specify a subset of the given properties. Properties not specified will be
added with default values after you launch the server. If you do not create a config file
it is created for you on the first launch of the JAR.

#### Updating the configuration
If you want to update the configuration you need to restart the server after you edited the `config.xml`.

## Webpage for administration
The server has a webpage to display some of its functionality. When running locally the server can be reached via localhost at the port set in the `config.xml` and the endpoint `/admin` e.g. `http://localhost:8080/admin`.  

It is lightly protected with a username and password that can also be set in the `config.xml`. It is noteworthy that this is not a secure protection and merely the bare minimum to protect the webpage from being accessed by non-administrators. Since the server is not using https the password can be sniffed by an attacker.

**Note** that by default no password is set and the default username is admin. Hence, the default is not secure.

## Api documentation
This lightweight HTTP-Server provides several routes each with an individual documentation which can be found here
  *  Hello world [/](doc/root.md) 
  *  Webpage [/admin](doc/admin.md) 
  *  Add interactions betweens users [/interaction](doc/interaction.md) 
  *  Generate graph from interactions [/graph](doc/graph.md) 
  *  Receive analysed data from interactions [/network-analysis](doc/network-analysis.md) 
  *  Receive the graph from interacitons as a svg [/graph-svg](doc/graph-svg.md)
  *  Shutdown the server [/shutdown](doc/shutdown.md) 
  *  Receive all stored ids (persons) [/ids](doc/ids.md)  