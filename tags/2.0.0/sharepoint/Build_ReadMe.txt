Following are the steps to build Google Enterprise Connector for SharePoint 2007,2003 :

1. Ensure that you have Apache Ant installed on your system. If not, you can get it from http://ant.apache.org/bindownload.cgi

2. Ensure that an environment variable with the name ANT_HOME is created on your system and is pointing to the installed ANT home directory.
If not, create one.

3. Ensure that "ant-contrib-1.0b3.jar" is present in the $ANT_HOME\lib. You can download this jar from "http://sourceforge.net/project/showfiles.php?group_id=36177".

4. SharePoint Connector runs on top of Connector Manager. Hence, you must have Connector Manager binaries on your system.
If not, you can get it from http://code.google.com/p/google-enterprise-connector-manager/downloads/list

5. Create an environment variable on your system with the name CONNECTOR_MANAGER_DIR. 
Set its value to the Connector Manager home directory. Ensure that all the Connector Manager binaries are present at the following location:
{CONNECTOR_MANAGER_DIR}/dist/jarfile/

Following are the least required Connector Manager libraries which are expected to be present:
* connector-spi.jar
* connector-util.jar
* connector.jar

6. Create a directory with name "lib" at the current path (where the build.xml is existing after you have extracted connector-sharepoint-2.0.0-src).

7. Copy the HttpClient-Modified.jar that comes with SharePoint Connector binary under "lib/" 

8. Copy the following libraries under lib/
* activation-1.1.jar
* axis.jar
* commons-codec-1.3.jar
* commons-discovery-0.2.jar
* gnu-regexp-1.1.4.jar
* HttpClient-Modified.jar
* jaxrpc.jar
* jcifs-1.2.15.jar
* joda-time-1.1.jar
* mail-1.4.jar
* saaj.jar
* wsdl4j-1.5.1.jar
* xercesImpl-2.8.1.jar
* xml-apis-1.3.03.jar
* axis-ant.jar
* commons-logging.jar
* junit.jar

9. From the command prompt, execute the build.xml using "ant" command.



