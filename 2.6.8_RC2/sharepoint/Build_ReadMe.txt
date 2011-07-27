Following are the steps to build Google Enterprise Connector for SharePoint 2007,2003 :
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
