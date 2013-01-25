Following are the steps to build Google Enterprise Connector for SharePoint 2010, 2007 and 2003 :
========================================================================================
1. Ensure that you have Apache Ant installed on your system. If not, you can get it from http://ant.apache.org/bindownload.cgi

2. Ensure that an environment variable with the name ANT_HOME is created on your system and is pointing to the installed ANT home directory.
If not, create one.

3. Ensure that "ant-contrib-1.0b3.jar" is present in the $ANT_HOME\lib. You can download this jar from "http://sourceforge.net/project/showfiles.php?group_id=36177".

4. SharePoint Connector runs on top of Connector Manager. Hence, you must have Connector Manager binaries on your system.
If not, you can get it from http://code.google.com/p/google-enterprise-connector-manager/downloads/list

5. Set the value for CONNECTOR_MANAGER_DIR in build.properties. The value should be Connector Manager home directory. The required libraries should be in '/dist/jarfile/' folder which is set as CONNECTOR_MANAGER_DIR.
If you have the Connector Manager source code, run the Connector Manager build so that the the Connector Manager binaries are present at the following location:
{CONNECTOR_MANAGER_DIR}/dist/jarfile/

Following are the required Connector Manager libraries which are expected to be present:
* connector-spi.jar
* connector-util.jar
* connector.jar
* connector-logging.jar

6. From the command prompt, execute the build.xml using "ant" command. The 'connector-sharepoint.jar' will be created in the dist/jarfile directory

To create user data store where all SharePoint groups/user memberships are stored, follow steps 7, 8, and 9:
7. After registering the connector-manager on GSA, uncomment the below lines from applicationContext.property file and then specify values for all these properties.
for example in case of MySQL data base:
jdbc.datasource.type=mysql
jdbc.datasource.mysql.url=jdbc:mysql://myserver/google_connectors
jdbc.datasource.mysql.user=<username>
jdbc.datasource.mysql.password=<encrypted password>
Refer connector-manager wiki page http://code.google.com/p/google-enterprise-connector-manager/wiki/EncryptPassword to encrypt password.

7. After registering the connector-manager on GSA, copy UserDataStoreConfig.properties file (comes along with the connector 2.8 jar) at the following location:
$CATALINA_HOME\webapps\connector-manager\WEB-INF\connectors\sharepoint-connector

8. Copy all database specific JDBC implementation libraries under $CATALINA_HOME\webapps\connector-manager\WEB-INF\lib\.
NOTE:We have tested the 2.8 build with all below mentioned drivers.
for MYSQL : mysql-connector-java-5.1.6-bin.jar
for MSSQL : sqljdbc.jar
for Oracle : ojdbc14.jar

9. Proceed with the creation of connector instance on GSA.