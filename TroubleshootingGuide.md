# Troubleshooting Guide for the SharePoint connector #

## Contents ##



## Introduction ##
This document has a list of troubleshooting tips to identify if the SharePoint connector has been configured correctly and is discovering and traversing SharePoint content.

This document covers both types of authorization that can be used:

  * Authorization by the GSA or a SAML provider (metadata-and-URL feed mode)
  * Authorization by the connector (conent feed mode)

Each Section may further be divided into sub-sections depending on the feed type to which they are applicable:

  1. Common to Content and Metadata-and-URL Feed Mode
  1. Content Feed Mode
  1. Metadata-and-URL Feed Mode


## Error Messages ##
This section describes some commonly encountered error messages and their likely solutions.


### Common to Both Content and Metadata-and-URL Feed Mode ###
  * **Crawl URL does not match against any 'Include URLs' patterns**
> You see this message when a user-provided Crawl URL does not match patterns specified under "Include URLs Matching the Following Patterns"


  * **Crawl URL matches against one of the patterns specified under 'Do Not Include URLs'**
> You see this message when a user-provided Crawl URL matches patterns specified under "Do Not Include URLs Matching the Following Patterns".


  * **Following URL Pattern provided under 'Include URLs' is invalid:**
> Some of the patterns are not valid patterns


  * **Following URL Pattern provided under 'Do Not Include URLs' is invalid:**
> Some of the patterns are not valid patterns


  * **Required field not specified.**
> Fields marked with an asterisk (`*`) on the Configuring Connector Instances form are required. You must provide appropriate values for these fields.


  * **The Crawl URL must contain a fully qualified domain name. Please check the Crawl URL value.**
> You must provide the appropriate SharePoint Site URL with a fully qualified domain name for SharePoint Site URL field on Configuring Connector Instances.


  * **Cannot connect to the given SharePoint Site URL with the supplied Domain/Username/Password. Please re-enter. OR Cannot connect to the Google Services for SharePoint on the given Crawl URL with the supplied Domain/Username/Password.Reason:(401)Unauthorized**

> This occurs when the web service call fails for the given SharePoint URL. The exact reason for this is displayed along with the error message.
> Possible reasons:
    * 401 Unauthorized: Check the credential (domain/username/password) for the correctness and the privilege that is assigned to this on the SharePoint URL. Connector requires at least 'Contribute' permission on the site collection to be crawled. Site collection administrator is the recommended privilege in order to crawl maximum content.

  * 404 Not Found: The requested SharePoint URL could not be found but may be available again in the future.
  * 403 Forbidden: Often caused by the SharePoint site being configured to use Forms Based Authentication.


> For a complete listing of the response codes, refer to http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html


  * **Domain has been specified twice under Domain/Username. Please provide the credentials in right format.**
> This message appears when domain has been specified twice under domain field as well as in the username. You should always enter domain as domain and username as username; i.e, for a user dom\user or user@dom username is user and domain is dom.

  * **Cannot connect to the Google Services for SharePoint on the given Crawl URL with the supplied Domain/Username/Password.Reason:; nested exception is: java.net.UnknownHostException: <Your SharePoint Host>**

This message appears when the SharePoint Host is not reachable from the connector host. Work-around for this is to add host entry for the SharePoint machine in your connector host. In case of on-board connector, you need to add the host entry under Administration -> DNS Override section of the Admin console.


  * **Web service gets a SAXParseException due to an invalid XML character in the web service response**
> This is a known issue, see: .http://code.google.com/p/google-enterprise-connector-sharepoint/issues/detail?id=50 The SharePoint web-service call fails with error `org.xml.sax.SAXParseException: Character reference "&#11" is an invalid XML character`. This is because the web-service response received from SharePoint contains an invalid XML character.

> Please remove the unsupported character; it is probably present in one of the fields returned by the web-service, could be in the title/meta-data of the document.


  * **java.lang.OutOfMemoryError: Java heap space**
> This message appears when the connector does not have sufficient memory to perform the requested task. One of the cases when this can occur is when the connector's state file has grown too large and the connector is restarted. The connector will fail while parsing the state file due to memory shortage.

> To overcome this, increase the memory allocated to tomcat. By default the connector is configured to use only up to 1GB memory. In order to overcome this problem, we must allocate around 2GB memory to the connector.

> This example below will allocate 2GB (i.e. 2048MB) of memory. You can tweak the memory allocation to fit your needs.

> LINUX
    1. Open the file: <Connector Installation Path>/<Connector Name>/Tomcat/bin/catalina.sh
    1. Search for: JAVA\_OPTS="$JAVA\_OPTS -Xms256m -Xmx1024m
    1. Change it to: JAVA\_OPTS="$JAVA\_OPTS -Xms256m -Xmx2048m


> WINDOWS
    1. Open the file: <Connector Installation Path>\<Connector Name>\Tomcat\bin\service.bat
    1. Search for (must be towards the end of the file) :  --JvmMs 256 --JvmMx 1024
    1. Change it to:  --JvmMs 256 --JvmMx 2048

> For Tomcat Windows services, if you only edited the .bat files to change heap size, that has no effect on the Tomcat service.  You have to edit the service configuration, or edit service.bat and uninstall and reinstall the Tomcat service. You can edit the service configuration with tomcat6w.exe, e.g., "tomcat6w //ES//<service name>" where "<service name>" is the connector installation name (the name of the parent of the Tomcat directory, not a connector instance name).

> _Note: To be able to allocate memory successfully, the system must have at least twice as much physical memory.  To allocate 2GB of java heap size, the system architecture must be 64-bit_


  * **The SharePoint connector throws exception when parsing extraIDs**
> The SharePoint connector is showing the following exception in the connector log:

> WARNING: Problem while updating relativeURL `[` ... `]`, listURL `[` ... `]`.
> com.google.enterprise.connector.sharepoint.spiimpl.SharepointException: extraIDs needs to be updated.

> This can be safely ignored. The issue here is that a path (either for a folder or document) that is in the state file has been changed (either a document or folder was renamed or a list or website was moved). The effect of this exception is that the document will be skipped in this crawl but will be picked up when the crawl starts from the beginning.

  * **SEVERE: No Change Token Found in the Web Service Response !!!! The current change token might have become invalid; please check the Event Cache table of SharePoint content database.**

> If you get the above error regularly in the connector logs, then it hints at the problem in the event cache table.This can happen if the "entries in event cache which the getListItemChangesSinceToken service relies on to track changes were getting deleted.  To solve this problem the lifetime and size of the eventcache should be increased. See point "C" at http://blogs.technet.com/stefan_gossner/archive/2008/03/13/moss-common-issue-incremental-deployment-fails-with-the-changetoken-refers-to-a-time-before-the-start-of-the-current-change-log.aspx".To fix the current outdated state file, a full clean re-crawl will be necessary.

  * **Call to checkConnectivity failed. endpoint [http://sharepointhost:port/_vti_bin/GssAcl.asmx ].Server was unable to process request. ---> Object reference not set to an instance of an object.**

> You will get this exception in the connector logs while configuring the connector to feed ACLs, if the root site collection of the SharePoint WebApp you want to crawl doesn't require authentication. In this case, it can be viewed by anyone without authentication. In this context, call to GssACL.asmx using this webApp fails because it is not designed to run as anonymous. You need to configure the connector for a SharePoint URL that uses some authentication such as Basic or NTLM or Kerberos.

  * **Call to checkConnectivity failed. endpoint [http://sharepointhost:port/_vti_bin/GssAcl.asmx ].: (401)Unauthorized**

> You will get this exception in the connector logs while configuring the connector to feed ACLs for one of the following reasons:

  1. Crawler user doesn't have minimum Full read permission on the web application
  1. Crawler user doesn't have Site collection administrator permission
  1. Crawler user has DENY permission on the web application

### Metadata-and-URL Feed Mode ###
  * **Crawl Diagnostics Error Message
> Retrying URL: Host unreachable while trying to fetch robots.txt.**
> To correct the error:
> Check the network settings and ensure that the SharePoint Server host can be reached by GSA. The GSA crawler checks for the presence of robots.txt at the root level of the site web application. In case the SharePoint server host is unreachable you will see the above error.
> For more details refer to: http://code.google.com/apis/searchappliance/documentation/52/admin_crawl/Troubleshooting.html#statusmessh2


  * **ProcessNode Error**
> You might see the following error message on the Crawl Diagnostics page in the Admin Console, where URL is the URL to a graphic file:
> ProcessNode: Not match URL patterns, skipping record with URL: URL
> Ensure that you have modified the crawl patterns to include graphic formats. For information on including graphic formats, see Configuring the Crawl Patterns.



## Diagnosing the Connector Logs ##
This section details some of the important log messages that are written into the connector’s log:


### Common to Both Content and Metadata-and-URL Feed Mode ###
| **Log Message** | **Description** | **Logging Level** |
|:----------------|:----------------|:------------------|
| startTraversal / resumeTraversal |  A new / incremental crawl has begun. | INFO |
| Getting the initial list of MySites |  Connector is discovering the personal site / My Sites | INFO |
| Web [Web\_URL](Web_URL.md) is getting traversed for documents | Web with the URL Web\_URL is getting traversed for documents. | INFO |
| discovered new listState: List\_URL  |  Connector has just discovered the new SharePoint list and traversed the documents in it. | INFO |
| revisiting old listState: List\_URL  |  Connector is checking for the list for any changes / updates. | INFO |
| found #  items to crawl in List\_URL | Connector has discovered # no. of documents from to be sent to GSA. | INFO |
| found: # Items in List/Library [List\_URL](List_URL.md) for feed action=ADD | Connector has discovered # no. of documents for ADD feeds. | INFO |
| Processing the renamed/restored folder ID[docId](docId.md), relativeURL[relativeURL](relativeURL.md) | Connector is fetching the child documents under a folder which have been restored or renamed. | INFO |
| document url[url](url.md) has been re-written to [Aliased\_Url ] in respect to the aliasing. | Document URL has been re-written as per the Site Alias Mapping specified during the connector configuration. | INFO |
| Document URL sending to CM : DocURL  | Connector has sent the document DocURL to the connector Manager from where it will be sent to GSA. | INFO |
| Traversal returned [#count](#count.md) documents  |  Number of documents returned after the current traversal cycle. | INFO |
| checkpoint received  |  Connector has just received a check pointing request from the Connector Manager / GSA. | INFO |
| checkpoint processed; saving GlobalState to disk  | Connector has saved the traversal status to the state file. | INFO |
| 401: Unauthorized: Indicates invalid credentials | Connector can not call the SharePoint web service with the specified user credentials. Check the correctness of the credential and the user rights. | WARNING |
| Call to the GSSiteDiscovery web service failed with the following exception: | Connector can not discover the sites outside the current site collection because GSSitediscovery has failed. | WARNING |
| Unable to match the metadata\_name1 [,metadata\_name2] against one of the [Included/excluded] metadata. | Some problem occurred while matching the metadata against the included / excluded list of metadata. | WARNING |
| One of the metadata under [included/excluded] is invalid as GNU Regexp. metadata\_name1 [,metadata\_name2] | All the metadata entries under included / excluded metadata should be valid as per the GNU Regexp rules. | WARNING |
| Unable to match the metadata [metadata\_name](metadata_name.md) against one of the [Included/excluded] metadata. | Some problem occurred while matching the metadata against the included / excluded list of metadata. | WARNING |
| Getting child sites for web `[` siteCollectionURL `]` | Sub sites are being discovered for a given site collection | INFO |
| global state has been updated with newly intermediate webs | Connector has discovered new sites to be crawled and have been added to the connector's state information | INFO |
| Total MyLinks returned: `<# of MySite URLs>`| The total # of MySites discovered by the connector from the MySite base URL provided with the connector configuration | INFO |
| Connector completed a full crawl cycle traversing all the known site collections at time 

&lt;TIMESTAMP&gt;

 | Connector completed a full crawl cycle traversing all the known site collections in the farm at the indicated time. This is indicates end of the full crawl cycle. | INFO |
| Skipping Web URL [http://yourSite.domain.com ](.md) while crawling because it has been marked for No Crawling on SharePoint. | This SharePoint site is configured not to be allowed to appear in the search search through Site Search visibility options and you have selected the option 'Use SharePoint search visibility options' on the connector configuration | WARNING |


### Metadata-and-URL Feed Mode ###
| **Log Message** | **Description** | **Logging Level** |
|:----------------|:----------------|:------------------|
| Deleting the state information for list/library `[` listURL `]` | The list is being deleted from connector state as it does not exist anymore in SharePoint | INFO |


### Content Feed Mode ###
| **Log Message** | **Description** | **Logging Level** |
|:----------------|:----------------|:------------------|
| Authenticating User: `<username>` | Authentication request received for user `<username>`. This happens in case of batch(bulk) authorization when the user search for some docs that was content-fed by the connector. | INFO |
| Authorizing User: `<username>` | Connector is authorizing the user against a set of docs. This is required when the connector is set to handle authorization of search results. | INFO |
| received #| Connector got # of documents to authorize | FINER |
| `[`status: `<true/false>``]`, Complex Document ID: `[` `the doc id` `]` | The docID against which the user is getting authorized along with the authorization status. This is required when the connector is set to handle authorization of search results. | WARNING if the authz fails |
| Web Service has thrown the following error while authorizing. \n Error: `<error message>` | Any error encountered by the GSBulkAuthorization web service during authorization. This is required when the connector is set to handle authorization of search results. | SEVERE |
| Web Service has thrown the following error while authorizing.  Error: Value does not fall within the expected range. | The document to be authorized is not found on the SharePoint. It might have been deleted. | SEVERE |
| Web Service has thrown the following error while authorizing.  Error: User not found | The search user is not found on the SharePoint site. | SEVERE |
| Sending # `<`document\_count`>` documents to delete from the deleted List/Library `[` `<`listURL`>` `]`. | The # docs for which the connector is sending delete feeds for the given list as it has been deleted from SharePoint | INFO |
|You get this error for Site discovery or Bulk authorization web-services in the connector logs "  'Server did not recognize the value of HTTP Header SOAPAction:' |   There is a version mis-match between the SharePoint connector and Google Services for SharePoint.It is recommended that the version of the Google services for SharePoint? should be the same as that of the SharePoint? connector version. | WARNING |
|You get this error for GSBulkAuthorization web-service in the connector logs at serve time: 'cannot complete the action' when contacting http://sharepointsite.domain.com:80/_vti_bin/GSBulkAuthorization.asmx OR  authData is null at the completion of the call. |   There is a version mis-match between the SharePoint connector and Google Services for SharePoint.It is recommended that the version of the Google services for SharePoint? should be the same as that of the SharePoint? connector version. | SEVERE |
| You get this error for GSBulkAuthorization web-service in the connector logs at serve time: User domain\user not found in parent site collection of web http://sharepointsite.domain.com/sites/somesite | Make sure that the domain name and username logged here are correct. In some cases, connector doesn't get any domain from GSA and falls back to the domain name specified under connector configuration and hence, fails to authorize the documents.  | WARNING |
| At serve time, connector authentication fails. You see following message in the logs: "WARNING: Can not connect to GSBulkAuthorization web service." and "There is not enough space on the disk." | This is due to un-availability of disk space on the SharePoint server. Make sure that disk is not full on the SharePoint server.| WARNING |
| At serve time, you do not see results for authorized users. In the connector logs, you get error "The Web application at http://SharePointSiteURL/List/Forms/AllItems.aspx could not be found. Verify that you have typed the URL correctly. If the URL should be serving existing content, the system administrator may need to add a new request URL mapping to the intended application." | This occurs when there is no alternate access mapping in the default zone with fully qualified domain name for the SharePoint web application. SharePoint connector expects the URLs with fully qualified domain names in the SharePoint while authorizing them. | WARNING|


## Verification of GSS ##

  * Three web services are deployed on the SharePoint (frontend) server on installing Google Search Appliance Resource Kit for SharePoint, namely GSSiteDiscovery, GSBulkAuthorization and GssAcl.
  * Pick any SharePoint site hosted on the SharePoint server where Google services for SharePoint are deployed. Try browsing this this URL from the browser.
  * The connectivity of web services can be verified using following URLs:
```
     http://mycomp.com/_vti_bin/GSBulkAuthorization.asmx
     http://mycomp.com/_vti_bin/GSSiteDiscovery.asmx
     http://mycomp.com/_vti_bin/GssAcl.asmx

Where http://mycomp.com is the SharePoint site URL
```

  * After opening the above URL(s), you should be able to see all the web methods exposed by the web service. Click on the "Service Description" link available on the top to view the wsdl file description.


## Frequently Asked Questions - FAQ ##
This section lists some of the most commonly asked questions:


### Common to Both Content and Metadata-and-URL Feed Mode ###
**Q. I cannot register the Connector Manager on GSA. What should I do?**

> You can test that the connector manager URL is valid and is running by typing the URL in a browser: `http://<localhost>:<tomcat_port>/connector-manager` on the machine that has the Connector Manager and connector installed on it
> You will get an informative text displaying the connector manager version. You should see something like:
```
<CmResponse>
	<Info>Google Search Appliance Connector Manager 3.2.2-RC1 (build 3276 3.2.2-RC1 October 29 2013); Oracle Corporation Java HotSpot(TM) 64-Bit Server VM 1.7.0_51; Windows Server 2012 6.2 (amd64)</Info> 
	<StatusId>0</StatusId> 
</CmResponse>
```
> If you see the above response and GSA is still unable to register the Connector Manager, you need to check the network settings between your GSA and the Connector Manager host.
> If you do not see the above response, then please check that the Connector Manager host is reachable and it is running.

**Q. I get "403 Access Forbidden" errors if try the test connectivity page of Connector Manager using the machine hostname. It works with 'localhost' as the hostname. Why is it so?**

It is because only GSA IP address (which you entered with the installer) and
'localhost' are set to be allowed to connect to Tomcat (hosting connector
manager and the connector) on connector host.

Check this for a detailed explanation:
http://code.google.com/p/google-enterprise-connector-manager/wiki/ChangeGSA

In order to allow any other host to connect to the Connector Manager, you need to set the IP address of that particular host in the server.xml file of Tomcat as explained in the above link.

**Q. How can I track the feeds that connector sends to the GSA?**

> Set the `feedLoggingLevel` property to `ALL` in the `applicationContext.properties` found under `$CATALINA_HOME/webapps/connector-manager/WEB-INF/`. Restart the connector and let it run for some time. Check out the google-connectors.feed log files generated under `$CATALINA_HOME/logs/` folder.

In GSA 6.10 and above, you can see the feeds coming to the GSA from connectors by downloading the feeds at Crawl and Index -> Feeds page of the admin console.

**Q. How can I change the log level so that only the relevant log messages are generated?**

> Go to `$CATALINA_HOME/webapps/connector-manager/WEB-INF/classes`. Open `logging.properties` and change the log level(s) to the required level(s).


**Q. Does connector maintain the list of excluded URLs?**

> Yes. It stores the list of excluded URLs in `excluded-URLs/excluded_url%g.txt` under the connector instance directory. Please note that these URLs are excluded by the connector during the traversal and not by the GSA when the feed is sent.
This feature is available in connector version 2.0 and above.


**Q. Is it possible for a single connector to send feed to more then one GSA?**

> No. You have to register a connector manager and create connector instances under this, on each individual GSA.


**Q. Can I create multiple connector instances with the same name?**

> No.


**Q. Does connector run in an incremental mode, so that the changes done on the SharePoint site are reflected during search?**

> Yes. Connector sends new feeds for the documents which are modified.


**Q. Why should I not use consecutive ports while installing multiple connectors on the same machine?**

> Because, the port next to the connector instance port is used as a shutdown port by the connector. For example , a connector running on port 8080, uses port 8081 as the shutdown port.


**Q. What value should I enter for the My Site Base URL field in case of SharePoint 2003?**

> No value is required. You can ignore this field. Even if you specify any value, connector will ignore this if the Crawl URL is of SharePoint 2003.


**Q. Does connector discover and crawl Personal Sites in SharePoint 2003?**

> Yes. You need not specify it as part of connector configuration explicitly. In SharePoint 2003, personal sites are always created under the same Web Application (also called Virtual Server).


**Q. What value should I provide for MySite URL field on connector configuration to crawl MySite in case of SharePoint 2007?**

> Make sure that you do not specify the complete MySite URL. Remember, what connector expects is only MySite Base URL. For example, if the MySite URL is: http://server.domain:port/personal/someuser/default.aspx, enter http://server.domain:port/.

This information is required in case of SharePoint 2007 because, MySites can be hosted on different web application or even different SharePoint server. Connector needs the base URL where all the MySites are hosted using which it constructs the complete MySite URL dynamically using user credentials you specify.


**Q. How can I search metadata for a document?**

> Use inmeta search for this. For details, refer to
http://code.google.com/apis/searchappliance/documentation/610/xml_reference.html#inmeta_filter

**Q: Do I need to restart the connector service each time I modify the connector configuration?**

> No


**Q: Do I need to restart the connector service each time I modify the connectorInstance.xml?**

> Yes


**Q. I can’t get the connector to re-crawl using the 'Reset' feature. Is there any other way of forcing a re-crawl?**

> The manual steps to force a re-crawl of the connector.
  1. On the connector host, navigate to the location of the connector state file.
    * On Windows, this is `<Installation Location>\Tomcat\webapps\connector-manager\WEB-INF\connectors\sharepoint-connector\Sharepoint Connector Instance Name\`.
    * On Linux, this is `<Installation Location>/Tomcat/webapps/connector-manager/WEB-INF/connectors/sharepoint-connector/Sharepoint Connector Instance Name/`
  1. Delete the file Sharepoint\_state.xml file.
  1. Restart the SharePoint connector.
> The connector traverses the content again and generates new feeds.


**Q. Does the connector detect folder renames and folder restoration?**

> Yes. In both the cases, connector recursively discovers all the documents which are under the concerned folder and sends new feeds for them.


**Q. Can I install and run the Connector Manager and Connector on a different Tomcat or any another servlet container?**

> Installer bundles Tomcat with the connector and it cannot use local Tomcat server
> For installing the Connector Manager and Connector on a different Tomcat instance, you will have to follow the manual installation steps
> The Connector Manager and Connector has only been tested and certified to work with Tomcat.


**Q. How do I change the port on which the Connector Manager is running?**

> Go to `<Installation Directory>/Tomcat/conf` and edit the server.xml file as follows:
> Find: `<Connector port="<portNo>"`. Here replace the `<portNo>` with the port configured during initial installation.
> Specify a new port value for 'port' attribute and restart Connector service.


**Q. Can I restore a connector instances in case it has been deleted by mistake?**

> No. Though, you can always create a new connector instance with the same name and same configuration details as of the deleted connector instance. This will serve the same purpose except the state information is lost. In that case, connector will re-crawl the whole SharePoint site again.

**Q. I have many sites under multiple site collections each organized into multiple web applications. Will single connector instance be able to crawl all these without installing the Google Services for SharePoint?**

No. If you do not install Google Services for SharePoint, one connector instance can crawl only one site collection and links provided under that.

**Q. How can the connector be configured to crawl multiple site collections distributed across multiple SharePoint servers in a farm using a single connector instance?**

For discovering and crawling multiple site collections and web applications (possibly distributed across multiple SharePoint servers in a farm), deploy the Google Services for SharePoint on each of the SharePoint servers. You can then configure a single connector instance for discovering and crawling  all content in the farm.


**Q. What if I have a Site Directory listing all the site collections? Do I need to still install the Google Services for SharePoint?**

If the deployment has a Site Directory where all the site collections are listed, only one connector instance will work. No need to deploy the Google Services for SharePoint.


**Q. I don't want to install Google Services for SharePoint on my SharePoint server. Is there any way I can create a single connector instance to crawl all site collections?**

Yes, you can. Here are the options:

Create a list of type Links, which can be discovered from the given crawl URL. The connector will then follow all the links and traverse all the site collections

**OR**

Create a Site Directory with links to all site collections. Give the site directory URL as crawl URL and the connector will crawl all site collections  listed under it.


**Q. Connector fails to detect changes if there is a huge gap in the activity on the SharePoint Server, goes into an infinite loop and re-discovers the same documents again and again.**

The connector relies on the SharePoint's web-service to detect changes. The web-service in turn relies on the EventCache table. By default the entries in EventCache have a life time of 15days. If there is a long time gap in between modifications to the SharePoint list, the change tokens with the connector are invalidated and it is unable to detect any changes since the last crawl.

To circumvent this problem, please increase the EventCache timeout to the maximum possible value for your environment. See http://code.google.com/p/google-enterprise-connector-sharepoint/issues/detail?id=113 for details.



### Metadata-and-URL Feed Mode ###
**Q. Do I need to configure robots.txt on the SharePoint server and if so how do I do it?**

> The robots.txt file tells crawlers which files and directories can or cannot be crawled, including various file types. If the search engine gets an error when getting this file, no content will be crawled on that server. The robots.txt file will be checked on a regular basis, but changes may not have immediate results. The configuration of robots.txt file is optional.
> Here are the steps for the same.
  1. Open Internet Information Services (IIS) Manager
  1. On the left side Tree View, click the machine name, expand the node Web Sites, find the name of the SharePoint application (Web site).
  1. Right click on the name of the SharePoint application, select Open. A Windows Explorer will open with the location of the root directory for this SharePoint application.
  1. Create a text file robots.txt, open it, and paste the follow content, then save the file.
```
User-agent: *
Disallow:
```
> > _The pattern mentioned above specifies to allow all content to be crawled. You can customize it to include/exclude specific content._


> Once you create a `robots.txt` file, you must define a managed path for the file in SharePoint:

> _**For SharePoint 2007:**_
  1. Log in to the SharePoint Central Administration site: from **Start menu > Administrative Tools  > SharePoint Central Administration**
  1. On the top link bar of the Central Administration Web site, click **Application Management.**
  1. On the Application Management page, in the **SharePoint Web Application Management** section, click **Define managed paths**.
  1. On the **Define Managed Paths** page, select the correct web application.
  1. On the **Select Web Application** page, click the web application for which you want to define managed paths.
  1. Under **Add a New Path**, enter the following: `/robots.txt`
  1. In the **Type** list, select **Explicit inclusion** and click **OK**.
  1. Browse the robots.txt file for the given SharePoint site from the browser. You should get either HTTP 404 or HTTP 200. If you get HTTP 401, you will have to check the security settings on the given SharePoint application (Web site)

> _**For SharePoint 2003:**_
  1. Log in to the SharePoint Central Administration site: from **Start menu > Administrative Tools  > SharePoint Central Administration**
  1. Under **Virtual Server Configuration**, click **Configure virtual server settings**.
  1. Select the correct virtual server, for example, **Default Web Site**.
  1. Under **Virtual Server Management**, click **Define managed paths**.
  1. Under **Add a New Path**, enter the following: `/robots.txt`
  1. Click **Check URL**. A browser window appears and shows a 404 error. This is the correct behavior.
  1. **Select Excluded path** and click **OK**. The robots.txt file is added to the excluded paths.
  1. Under **Add a New Path**, enter the following again: `/robots.txt`
  1. Click Check URL again. The contents of the robots.txt file are now displayed.
  1. Exit from the Central Administration Site.


### Content Feed Mode ###
**Q. What benefit do I get by making the connector content feed?**

> Hits on the SharePoint server are reduced. Authorization is done in batches.


**Q. Can I, at any time, may need functionality like restoring a deleted connector instance?**

> Yes, you can. In case of content feed. Since, the authorization is done by the connector, if you delete the connector, the documents it has sent will not be searchable. To get rid of this, you should re-create a connector instance with the same name. Please note that GSBulkAuthorization has to be deployed on the SharePoint server for the authorization to be successful.


**Q. I have configured the connector for content feed. Though, I’m not able to see the documents URLs that fed to GSA under Crawl Diagnostics. Rather, it shows only the List level URLs with some encoded characters appended to it.**

> This is an expected behavior. In case of content feed, document ID that is shown under the crawl diagnostics Has the following format: `googleconnector://<connector_name>.localhost/doc/?docid=<ListURL>|<ItemID>`. These document IDs are shown in the encoded form. In case of metadata-and-URL feed, document URL is shown.


**Q. Does connector keep track of the deleted documents so that they are removed from the GSA’s index?**

> Yes. Connector sends delete feeds for such documents. GSA, than removes all such documents and their contents from its index.

### Connector-Kerberos Configuration ###

**Q. How can I verify that which type of Authentication Scheme is being used by SharePoint Connector at crawl time?**

Selected authentication scheme is listed in the logs. To enable it, you need to follow the steps below:
  1. Open CONNECTOR\_HOME\Tomcat\webapps\connector-manager\WEB-INF\classes\logging.properties and under the section #####  HTTPCLIENT LOGS #####, reset property "org.apache.commons.httpclient.level" to INFO
  1. Run the connector and check logs.
  1. In logs you will find following messages as per the authentication scheme selected:
    1. "negotiate authentication scheme selected" indicates Kerberos scheme
    1. "ntlm authentication scheme selected" indicates NTLM scheme
    1. "basic authentication scheme selected" indicates Basic scheme

**Q. What if, I left KDC Hostname field blank?**

Appropriate other authentication schemes i.e. ntlm or basic will be automatically selected by connector while crawling.

**Q. What if, I enter invalid IP Address in KDC Hostname field?**

You will get an error message "Please specify a fully qualified hostname for the Kerberos Key Distribution Center (KDC)" and configuration will not save. Please enter KDC Hostname field in FQDN format (Recommended) or enter valid IP address.

**Q. I have entered valid IP Address in KDC Hostname field. I can ping this IP Address, but still why connector is not able to crawl?**

Please verify that the IP Address you have entered should be of Key Distribution Centre Server.

**Q. How can I limit the maximum size of document to be fed to Search Appliance?**

You can customize the maximum size of document by modifying CONNECTOR\_HOME\Tomcat\webapps\connector-manager\WEB-INF\applicationContext.xml. Edit value of property "maxDocumentSize" as desired.

**Q. My SharePoint repository have the documents of size >30 MB (Maximum file size accepted by the GSA). Will those documents be fed to Search Appliance?**

In case of documents exceeding maximum file size, only metadata will be fed to Search Appliance. Content will not be sent.

**Q. For some of the documents, only metadata is fed to Search Appliance. Why the content is not fed to Search Appliance?**

Following are the probable reasons for content not being fed to Search Appliance:
  1. Target document size > maximum file size (default 30MB)
  1. Unknown MIME type
Please check supported MIME types at: CONNECTOR\_HOME\Tomcat\webapps\connector-manager\WEB-INF\applicationContext.xml

**Q. What if I have dual authentication enabled on SharePoint?**

http://msdn.microsoft.com/en-us/library/cc441429.aspx
http://grounding.co.za/blogs/brett/archive/2008/01/09/setting-up-dual-authentication-on-windows-sharepoint-services-3-0-forms-and-ntlm.aspx
http://www.andrewconnell.com/blog/articles/HowToConfigPublishingSiteWithDualAuthProvidersAndAnonAccess.aspx
http://sharenotes.wordpress.com/2007/10/19/multiple-or-dual-authentication-for-a-single-site-in-sharepoint-office-system-2007/
http://www.andrewconnell.com/blog/articles/HowToConfigPublishingSiteWithDualAuthProvidersAndAnonAccess.aspx

Setting up dual authentication basically means setting up FBA for internet users and IWA for intranet users. In such cases the connector should be configured with crawl URL that is protected by IWA and not FBA

**Q. What if I have host-header based sites?**
The host-header based sites, is a different way of creating sites:

http://technet.microsoft.com/en-us/library/cc424952.aspx
http://sharepointguys.com/matt/sharepoint-administration/sharepoint-for-web-hosters-host-header-based-site-collections/
http://bloggingabout.net/blogs/mglaser/archive/2007/01/31/hosting-sharepoint-on-a-fully-qualified-domain-name-fqdn.aspx
http://sharepoint.microsoft.com/blogs/zach/Lists/Posts/Post.aspx?ID=38

Host-header based sites are popular where you require to setup a separate domain for each site. These are not path based sites which are created from SharePoint's central admin, but using the command line tool 'stsadm.exe'. The Alternate Access Mappings are not applicable to these.

The connector is able to crawl only one host header based site. This is because SharePoint treats path based site collection and host header based site collections differently.

However, you can still crawl all the sites using the Google Services for SharePoint that need to be deployed on the SharePoint server. You can download the installer from the connectors download page: http://code.google.com/p/googlesearchapplianceconnectors/

Two important points to note:

  * The Include URL patterns should be broad enough to cover all the host header based sites, or else the connector will exclude them from crawling even though they are being discovered by the custom web service
  * All the site URLs should be able to be resolved using Windows authentication.

**Q. I have configured connector authN with Security Manager for content fed in Meta-data and URL feed mode, I am unable to get search results**

Metadata and URL feed is an invalid feed mode for connector authN with Security Manager so you don't get search results. Content feed is an appropriate feed mode for using Security Manager Connector authN.

**Q. What is early and late binding with respect to the SharePoint connector?**

Early binding means authorization by ACLs that are present per URL on the GSA. Connector 2.6.0 onward can feed ACLs to GSA.Connector v2.8 has full support for ACLs as LDAP groups /SP local groups are also supported in this version.

Late binding is authorization by either head request (metadata and URL feed) or connector authorization (content feed mode) where connector is not configured to feed ACLs.


**Q. How to enable ACL feature in connector (v 2.6.x)?**

  1. Go to the connector instance directory of the connector for which you want to
> > turn on ACL.The connector instance directory can be found under
> > > $CATALINA\_HOME/webapps/connector-manager/WEB-INF/connectors/sharepoint-connector
> > > /CONNECTOR
> > > Replace CONNECTOR with the actual name of your connector

> 2. Open connectorInstance.xml
> 3. Scroll down to the end until you find the ACL Related flags
> 4. Uncomment the following
> > 

&lt;property name="pushAcls"&gt;



&lt;value&gt;

true

&lt;/value&gt;



&lt;/property&gt;


> > 

&lt;property name="stripDomainFromAces"&gt;



&lt;value&gt;

true

&lt;/value&gt;



&lt;/property&gt;



> 5. If pushAcls value is false make it true.
> 6. Save the updated connectorInstance.xml
> 7. Restart the connector

**Q. How to enable ACL feature in connector 2.8?**

Connector 2.8 has a flag 'Feed ACLs' as a part of configuration UI. You need to specify LDAP credentials to use this feature. LDAP credentials are required by the connector to resolve LDAP groups and SharePoint local groups in the ACL during serve time authentication.

**Q. How to deploy the ACL web service on SharePoint?**
  1. Go to the SharePoint front server which is serving the sites
> 2. Open the directory C:\Program Files\Common Files\Microsoft Shared\web server
> > extensions\12\ISAPI

> 3. Copy the following three files under the ISAPI directory opened above
> > GssAcl.asmx
> > GssAcldisco.aspx
> > GssAclwsdl.aspx

**Q. I have Share Point farm Server. How to deploy the ACL web service on SharePoint?**

> The web service has to be deployed on all front-end servers which are hosting sites
> that you want the connector to crawl.

**Q. Where can I see ACLs on GSA?**
> You can see the ACLs (permitted users and groups) for each URL under Crawl diagnostics of the GSA.

**Q. Does the crawler user require any special permission for feeding ACL to GSA ?**
> For feeding ACL, the minimum required permission is either a "Full Read" at web application
> level or, the user should be the site collection administrator. The user credential
> that you use to configure the connector must have either of these two permissions if
> you are willing to use ACL based authorization.

**Q. Does the connector support SharePoint 2010 ?**
> Yes, Connector 2.6.0 onward supports Share Point 2010 and SharePoint Foundation 2010. You need to use corresponding version of the Google services for SharePoint.

**Q. How to include/exclude a site/page in the search results?**
> Connector, during the crawl checks if a site/list search visibility is turned on.
> If not, it pauses the crawling of that particular site/list. If it is a new
> site/list, no crawling is done for this.
> You can turn on this from Connector Configuration Page under Authorization Handling
> section.

> Note: For this feature, You need to install the custom GSSiteDiscovery web service
> on every SharePoint front end server where the sites to be crawled are hosted. This
> web service comes as part of Google Services for SharePoint. The steps for manual
> installation is same as of the ACL web service with the files being changed. In this
> case the files to be copied in the ISAPI directory are:
> GSSiteDiscovery.asmx
> GSSiteDiscoverydisco.aspx
> GSSiteDiscoverywsdl.aspx

**Q. Do we support Share Point Web Parts ?**
> > The Web Part page that contains the web parts gets crawled and indexed.

> > Web Parts are searchable based on the Content/Text visible on the page.

> > The Web part page gets crawled when we add a page viewer web part linked to a file.
> > However it does not get re-crawled when we update the contents of the Source file,
> > even if the changes are visible in target web part.

**Q. Do we support Crawling of Infopath Forms ?**

> Yes, now (2.6.2 onwards) the connector supports Infopath forms.

**Q. My Crawl URL on GSA should follow any  specific pattern?**
> Your Crawl URL should match the pattern given in Alternate Access Mapping of Central
> Administration.
> You can do so by going to
> Central Admin -> Operations Tab -> Global Configuration Section -> Alternate Access
> Mapping
> Click on the Webapp and change the URL.

**Q. I'm trying to setup SharePoint connector to crawl a Kerberos based site using Kerberos crawling feature. When I enter a valid KDC hostname, I get this error "Cannot connect to the given SharePoint Site URL with the supplied Domain/Username/Password.Reason:(401)Unauthorized". When I remove KDC hostname, connector gets saved.**
> > Make sure that you are using correct credentials for the site. Connector requires at least 'Contribute' permission on the site collection to be crawled. Site collection administrator is the recommended privilege in order to crawl maximum content.

> > Make sure that there are no duplicate service principal names (SPN) set for the SharePoint server

**Q. How does the connector behave when the SharePoint list is configured with "only their own" access control?**

The connector simply checks if the user has "READ" access to all the items in that list to decide if the SharePoint list is accessible to the given user. So if the user does not own or does not have access to any of the item in the list, the connector will return DENY for the SharePoint list landing page.

**Q. I get the following error in the connector logs:
SharepointException: Unknown SharePoint version**


You need to set the HTTP Header for your IIS web sites:


1. Open IIS (You will probably required to repeat this for ALL your web servers in the farm.)

2. Click on the website that hosts your target web app.  Click on 'HTTP Response Headers' (In IIS 7) or properties / HTTP Headers (in IIS 6).

You should see two headers - "X-Powered-By : ASP.NET" and "MicrosoftSharePointTeamServices : 12.0.0.6318"

12.x.x.xxxx indicates SharePoint 2007/MOSS, 6.x.x.xxxx indicates SharePoint 2003 and 14.x.x.xxxx indicates SharePoint 2010.

The connector detects the SharePoint type by comparing against this header value:

startsWith("6") --> SharePoint 2003 or WSS 2.0

startsWith("12") --> SharePoint 2007 or WSS 3.0

startsWith("14") --> SharePoint 2010 or SharePoint Foundation 2010 or WSS 4.0

Refer for more details: (Note:These are not Google authored pages)

http://social.technet.microsoft.com/Forums/en/sharepointsearch/thread/0e5205a5-37eb-4cf0-8397-51906e74c2fd

http://sharepointlogics.com/2010/02/how-to-check-sharepoint-2007-versions.html

http://social.msdn.microsoft.com/Forums/en/sharepointinfopath/thread/d2acfa81-14d6-4830-8518-34f61eaf77b7

**Q. Are the Include patterns in the connector configuration case sensitive? Some of my URLs are not getting indexed. They have upper-case characters but my Include patterns are all in lower-case.**

Yes. The Include patterns in the connector configuration case sensitive. You should include the patterns in the exact case as that of the SharePoint URLs or have the pattern using regular expression that matches both the cases i.e. regexpIgnoreCase:yourpattern

**Q. What is the recommended value of maximum number of TCP Ports for Windows Machine which hosts external SharePoint connector ?**

If you are installing the connector on external Windows machine (Windows 2003, Windows XP), please ensure to change the default maximum number of ephemeral TCP ports from 5000 to 65534. The connector uses these ports to make outbound socket connections from the Windows machine. The workaround is documented in the following link,

http://support.microsoft.com/default.aspx?scid=kb;en-us;196271

**Q. Does the connector support crawling secure SharePoint sites which use self-signed certificate?**

Yes. Connector supports crawling SSL based sites which use IIS self-signed certificate. The connector uses Axis and HTTPClient to make web service calls to SharePoint.

If you have a self signed cetificate which is not from a trusted authority, the HTTPClient library (internally uses Java SSL suport) fails to find a valid certificate to be presented to the SharePoint server along with the request. You may see the following error message:
javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

To resolve this issue, import the certificate into the JVM default keystore and check if
that works. Here are couple of helpful links:

http://www.java-samples.com/showtutorial.php?tutorialid=210
http://www.nabble.com/ssl-failure-td15753599.html

The connector supports certificate, but in case it is not from a trusted authority it will have to be imported to the JDK keystore as identified above.

## Viewing responses from SharePoint web services ##
You can diagnose whether SharePoint is returning the expected results to a web services call using a utility. Search google.com to [find examples of utilities](https://www.google.com/search?q=sharepoint+web+services+utility) that can do this.

The connector logs show the web service requests sent to SharePoint. For example:
```
Nov 15, 2010 8:01:49 PM [Traverse issue174spcon] com.google.enterprise.connector.sharepoint.wsclient.ListsWS getListItemChangesSinceToken
CONFIG: Making Web Service call with the following parameters:
 query [ <Query><Where><Or><Gt><FieldRef Name="ID"/><Value Type="Counter">0</Value></Gt><Eq><FieldRef Name="ContentType"/><Value Type="Text">Folder</Value></Eq></Or></Where><OrderBy><FieldRef Ascending="TRUE" Name="ID"/></OrderBy></Query> ], queryoptions [ <QueryOptions><IncludeMandatoryColumns>true</IncludeMandatoryColumns><DateInUtc>TRUE</DateInUtc><ViewAttributes Scope="Recursive"/><OptimizeFor>ItemIds</OptimizeFor></QueryOptions> ], viewFields [ <ViewFields Properties="TRUE"/>], token [ 1;3;ca894ebb-41ed-44ee-9f09-0e8cb578bab6;634249936372170000;1499 ] 
```
An example of how this may be useful is to make a call to the GetSite() method on the "sitedata" web service.

The expected result is a list of all the webs and sub webs including the metadata and site users. For example:
```
<SharePoint>
<SharePointServices.SiteData._sSiteMetadata LastModified="2/24/2008 9:15:09 AM" LastModifiedForceRecrawl="2/12/2008 12:25:27 PM" SmallSite="True" PortalUrl="" UserProfileGUID="" ValidSecurityInfo="True" />
<Webs>
<SharePointServices.SiteData._sWebWithTime Url=" http://sp.example.com:2905" LastModified="2/24/2008 9:15:09 AM" />
<SharePointServices.SiteData._sWebWithTime Url=" http://sp.example.com:2905/ECSCDemo" LastModified="2/24/2008 9:15:09 AM" />
.....
</Webs>
<GroupsPerSite>
<Groups>
<Group ID="14" Name="Group1" Description="" OwnerID="10" OwnerIsUser="True" />
</Groups>
</GroupsPerSite>
<UsersOfGroups>
<Users>
<User ID="12" Sid="S-1-5-21-2444170630-2081454939-2702159083-1143" Name="PS4312\abc" LoginName="PS4312\abc" Email="" Notes="" IsSiteAdmin="False" IsDomainGroup="False" />
<User ID="1073741823" Sid="S-1-0-0" Name="System Account" LoginName="SHAREPOINT\system" Email="" Notes="" IsSiteAdmin="False" IsDomainGroup="False" />
</Users>
.....
</UsersOfGroups>
</SharePoint>
```