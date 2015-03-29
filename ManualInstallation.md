# Introduction #
This document walks you through the steps to manually install the Connector for SharePoint. The instructions mentioned here are applicable to connector version 2.x and above.

Google strongly recommends that you use the Google connectors installer for installing the connector. Though, you may want to do it manually if,
  * You have built and installed a customized connector manager or a customized version of the connector
  * You want to deploy the connector on an existing Tomcat installation
  * You are installing a patch release that is not packaged with an installer.

## Pre-requisites ##

  * Apache Tomcat 6. You can download it from it from http://tomcat.apache.org/download-60.cgi
  * JRE1.5 or above. Refer to http://java.sun.com/javase/downloads/index_jdk5.jsp for downloads

## Steps to Install ##
To install the connector manually, follow the instructions given below:

1. On the Tomcat host, shut down Tomcat if it is running.

2. Go to $CATALINA\_HOME\bin directory.
  * For Windows Users
> Edit the following files:
> setclasspath.bat

```
   Add the following lines in the start of the file:
   set JRE_HOME=<JRE_HOME>
   set PATH=%PATH%;
   
   Add the following lines just before "rem Set standard command for invoking Java":
   rem Google Enterpise Connector Logging
   set CONNECTOR_LOGGING=%CATALINA_HOME%\webapps\connector-manager\WEB-INF\lib\connector-logging.jar
   if not exist "%CONNECTOR_LOGGING%" goto noConnectorLogging
   set CLASSPATH=%CLASSPATH%;%CONNECTOR_LOGGING%
   :noConnectorLogging
```
  * For Linux Users,
> Edit setclasspath.sh
```
     Add the following lines in the start of the file:
     export JRE_HOME=<JRE_HOME>
     export PATH="$PATH":"$JRE_HOME"/bin
     
     Add the following lines just before "# OSX hack to CLASSPATH":
     # Google Enterpise Connector Logging
     CONNECTOR_LOGGING="$CATALINA_HOME"/webapps/connector-manager/WEB-INF/lib/connector-logging.jar
     if [ -f "$CONNECTOR_LOGGING ]; then
     CLASSPATH="$CLASSPATH":"$CONNECTOR_LOGGING"
     fi
```

> Please make sure that all the shell scripts (with ".sh" as extension) have execute permissions.

3. If you do not have the connector manager installed, follow these steps to get the same:
  * Start a web browser and navigate to http://code.google.com/p/google-enterprise-connector-manager/downloads/list
  * Download the correct binary distribution compressed file for your platform
  * Unzip or untar the compressed file
  * copy the connector-manager.war to $CATALINA\_HOME/webapps directory
  * Start Tomcat so that the connector manager gets deployed. To confirm that the connector manager has been properly deployed under Tomcat confirm that a directory with the name connector-manager has been created under $CATALINA\_HOME/webapps directory
> Also you can check for http://localhost:8080/connector-manager/testConnectivity which will displays the connectivity status or http://localhost:8080/connector-manager/startUp which will display a message indicating the successful deployment of Connector Manager
  * Shut down the Tomcat to start with the further steps.

4. Start a web browser and navigate to the [Downloads page](http://code.google.com/p/google-enterprise-connector-sharepoint/downloads/list).

5. Download the correct SharePoint connector version (connector-sharepoint-`<version>`.zip).

6. Unzip the compressed file.

7. Copy the connector-sharepoint.jar file from the root directory to the $CATALINA\_HOME/webapps/connector-manager/WEB-INF/lib directory.

8. Copy the files in the /lib directory to the $CATALINA\_HOME/webapps/connector-manager/WEB-INF/lib directory.

9. Do the following changes in $CATALINA\_HOME/bin/catalina.bat (catalina.sh on Linux):
  * Find the following line:
```
     set JAVA_OPTS=%JAVA_OPTS% -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file="%CATALINA_BASE%\conf\logging.properties"
```
  * Change it to the following:
```
     set JAVA_OPTS=%JAVA_OPTS% -Djava.util.logging.manager=java.util.logging.LogManager -Djava.util.logging.config.file="%CATALINA_BASE%\webapps\connector-manager\WEB-INF\classes\logging.properties"
```
> > For Linux use $CATALINA\_BASE

10. In the $CATALINA\_HOME/webapps/connector-manager/WEB-INF folder, create a directory or folder called classes.

11. Copy the logging.properties file from the /Config folder to the /classes folder.

12. Open the logging.properties file in a text editor and set the value of java.util.logging.FileHandler.pattern equal to the absolute path of the log file.


> java.util.logging.FileHandler.pattern=`<value of $CATALINA_HOME>`/logs/google-connectors.%g.log

13. Start the Tomcat server.

14. To confirm whether the Tomcat server has started correctly and the connector is installed, navigate to the $CATALINA\_HOME/webapps/connector-manager/WEB-INF/connectors directory, and verify that the $CATALINA\_HOME/webapps/connector-manager/WEB-INF/connectors/sharepoint-connector directory exists.

15. You can also check if the connector type has been detected by the Connector Manager using the following URL:

> http://localhost:8080/connector-manager/getConnectorList


## Installing Connector Services for SharePoint ##

It is recommended that the version of the Connector Services for SharePoint should be the same as that of the Connector for SharePoint version.

1. Login to the SharePoint server whose sites are to be crawled by the connector.

2. Go to the ISAPI directory of SharePoint. If you are using the standard default installation, path of this directory would be C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\ISAPI\ for SharePoint 2007 and ...\14\ISAPI for SharePoint 2010

3. Download the correct Services for SharePoint archive (connector-services-sharepoint-`<version>`-src.zip or Google\_Services\_for_SharePoint_`<version>`.zip) from the [Downloads page](https://code.google.com/p/google-enterprise-connector-sharepoint/downloads/list?q=services).

4. Unzip the compressed file.

5. Copy the following files found after de-compressing the zip into the ISAPI folder as specified in step 2:

  * source\_net\Bulk Auth\SharePoint 2007\GSBulkAuthorization.asmx
  * source\_net\Bulk Auth\SharePoint 2007\GSBulkAuthorizationdisco.aspx
  * source\_net\Bulk Auth\SharePoint 2007\GSBulkAuthorizationwsdl.aspx

  * source\_net\Site Discovery\SharePoint 2007\GSSiteDiscovery.asmx
  * source\_net\Site Discovery\SharePoint 2007\GSSiteDiscoverydisco.aspx
  * source\_net\Site Discovery\SharePoint 2007\GSSiteDiscoverywsdl.aspx

  * source\_net\Acl\GssAcl.asmx
  * source\_net\Acl\GssAcldisco.aspx
  * source\_net\Acl\GssAclwsdl.aspx

**Note**: Connector Services for SharePoint should be installed on all SharePoint front end servers where sites that connector will crawl are hosted.

## Verifying the installation of Connector Services for SharePoint ##
  * Pick any SharePoint site hosted on the SharePoint server where Connector Services for SharePoint are deployed. Try browsing this this URL from the browser.
  * The connectivity of web services can be verified using following URLs:
```
     http://mycomp.com/_vti_bin/GSBulkAuthorization.asmx
     http://mycomp.com/_vti_bin/GSSiteDiscovery.asmx
     http://mycomp.com/_vti_bin/GssAcl.asmx

Where http://mycomp.com is the SharePoint site URL
```

  * After opening the above URL(s), you should be able to see all the web methods exposed by the web service. Click on the "Service Description" link available on the top to view the wsdl file description.