# Troubleshooting guide and FAQ for Google Search Box for SharePoint #



## Introduction ##
This document has a list of troubleshooting tips and FAQ to quickly identify if Google Search Box for SharePoint has been configured correctly and is serving public/secure on GSA content as intended.

## Error Messages ##
This section describes some commonly encountered error messages during search using the search box and their likely solutions.

**401 - Unauthorized**

You see this message when

1. Your SharePoint site is using HTTP Basic and not Kerberos/NTLM authentication.

2. If SAML bridge and Search box are on the same box, you may have to disable loopback check functionality.
See http://support.microsoft.com/kb/926642/ for details.

3. You haven't configured your browser to use NTLM/Kerberos. Refer http://code.google.com/apis/searchappliance/documentation/connectors/200/connector_admin/searchbox_sharepoint.html#Kerberos for setting up your browser for NTLM/Kerberos.

**The remote server returned an error: (502) Bad Gateway.Search Box logs show message: "The cookie contains only key 'secure'without any value"**

To avoid getting this error for a secure search, configure the search box to omit the secure cookie. Open web.config file for the SharePoint site/s. Set the omitSecureCookie flag (available in version 2.6.8) to true and perform a fresh search. You should get the search results.

Procedure to change the omitSecureCookie flag:

1. Open the web.config file for the SharePoint web application
(web.config file is on path - C:\inetpub\wwwroot\wss\VirtualDirectories\<PORT NO>
Where <PORT NO>. Is the port number for the SharePoint web application)

2. Modify the value for the "omitSecureCookie"  key in line


&lt;add key="omitSecureCookie" value="false" /&gt;

 from "false" to "true".

3. Restart IIS


**500-Internal Server Error**

You see this message when

1. GSA is not setup properly. Make sure that you are getting secure search results from GSA directly.

2. Check the configuration parameters for the search box on the SharePoint site usign configuration wizard.

3. If you are using GSA's frontend, make sure that the frontend name mentioned under configuration is correct.

Check http://code.google.com/apis/searchappliance/documentation/connectors/260/connector_admin/searchbox_sharepoint.html#MultipleColl for more error messages.

**I'm getting error 'No local SharePoint web applications found in this machine' while installing the Search Box using GSARKS installer**

In SharePoint farm deployment, it is possible that the WFE (Web Front-end) is configured NOT to host any web-application. In that case, no web application is installed on that host. GSARKS installer looks whether the web-application is deployed locally. If not, it will throw this error. You can check this by browsing the SP URLs and looking at the host name shown in the URL. Installer should definately work if the web applications are hosted locally. Note that search box must be deployed on all the WFEs in the farm that host the web-applications.

## FAQ ##

**1. Does Google Search Box for SharePoint support Public search?**

Yes. From version 2.6.8, the search box supports public search. In the configuration wizard, you can choose either public or public and secure option to use this feature. If you choose public and secure, by default, Public option is selected on the SharePoint Site showing the search box. To use public and secure search feature, un-check the  check-box showing Public.

**2.What all SharePoint versions does the search box support?**

Search box supports MOSS 2007, SharePoint 2010 and WSS 3.0 versions. It does not support SharePoint Foundation 2010.

**3.Can I upgrade my previous search box version to newer version?**

No. You need to uninstall the previous version and install the new version using the GSARKS (GSA Resource Kit for SharePoint) installer. Check the downloads page on this site for the latest version.