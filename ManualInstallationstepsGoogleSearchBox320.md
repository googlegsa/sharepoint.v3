# Pre-requisites #

The search Box can be deployed on machine with one of the following:
  * Microsoft SharePoint Server 2013
  * Microsoft SharePoint Server 2010
  * Microsoft SharePoint Foundation 2010
  * Microsoft Office SharePoint Server 2007 (MOSS 2007)
  * Windows SharePoint Services 3.0 (WSS 3.0)

The Google Search Box for SharePoint can be downloaded from:
http://code.google.com/p/google-enterprise-connector-sharepoint/downloads/list
  * GSBS-3.2.0`_`SharePoint2013.zip
  * GSBS-3.2.0`_`SharePoint2010.zip
  * GSBS-3.2.0`_`SharePoint2007.zip


For a SharePoint farm scenario, you need to install the Search Box for all the SharePoint Web Front Ends connected to the farm.


# SharePoint 2013 Steps: #

Download and extract the GSBS-3.2.0`_`SharePoint2013.zip on you SharePoint machine and use the extracted files as per the instructions given below:

1.    Place GSASearchresults.aspx, GSAForward.aspx, and PostResponse.xml in directory "C:\Program Files\Common Files\microsoft shared\Web Server Extensions\14\TEMPLATE\LAYOUTS"

2.    Place Folder "GSAFeature", containing elements.xml and Feature.xml, in directory "C:\Program Files\Common Files\microsoft shared\Web Server Extensions\14\TEMPLATE\FEATURES"

3.    Place the files GSA2SP.xsl and SP\_Actual.xsl in directory "C:\Program Files\Common Files\microsoft shared\Web Server Extensions\15\TEMPLATE"

4.    Place GSASearchArea.ascx in directory "C:\Program Files\Common Files\microsoft shared\Web Server Extensions\14\TEMPLATE\CONTROLTEMPLATES"

5.    Place file google\_custom\_search\_watermark.gif in directory "C:\Program Files\Common Files\microsoft shared\Web Server Extensions\14\TEMPLATE\IMAGES"

6.    For installation and activation of the "GSAFeature"
  * Open Windows Command prompt.
  * Go to directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\15\BIN"
  * Run following commands:
```

  stsadm.exe -o installfeature -name GSAFeature -force 
  stsadm.exe -o activatefeature -name GSAFeature -force 

```

7.    To set GSA parameters for performing search using Google search control. In case you have multiple SharePoint web applications, you need to set the GSA parameters for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to set GSA parameters for a given SharePoint web application.

  * In IIS, right click on the SharePoint web site -> Explore. Windows Explorer is opened.
  * In the Windows Explorer, right click on web.config -> Edit.
  * Go to "`<appSettings>`" section and add the following keys (sample values are for reference):
```

  <add key="GSALocation" value="gsa.mycompany.mydomain.com" />
  <add key="frontEnd" value="default_frontend" />
  <add key="verbose" value="True" />
  <add key="GSAStyle" value="true" />
  <add key="accesslevel" value="a" />
  <add key="omitSecureCookie" value="false" />
  <add key="defaultSearchType" value="publicAndSecure" />
  <add key="SearchTipsHTMLFileName" value="user_help.html" />
  <add key="EnableEmbeddedMode" value="true" />
  <add key="UseContainerTheme" value="false" />
  <add key="xslGSA2SP"
       value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\15\Template\GSA2SP.xsl" />
  <add key="xslSP2result"
       value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\15\Template\SP_Actual.xsl" />
  <add key="logLocation" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\15\LOGS" />
  <add key="filterParameter" value="p" />

```
    * accesslevel: value "a" means public and secure search, and value "p" means public search
    * defaultSearchType : value "publicAndSecure" means public and secure search is enabled by default, and value "public" means public search is enabled by default
  * Save the web.config file after making these changes.

9.    To enable session state for performing search with Google search box, the httpmodule for session state needs to be added. In case you have multiple SharePoint web applications, you need to enable the session state for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to enable session state for a given SharePoint web application.
  * In IIS,right click on the SharePoint web site -> Explore. Windows Explorer is opened
  * In the Windows Explorer, right click on web.config -> Edit
  * Go to "`<modules runAllManagedModulesForAllRequests="true">`" section and add the following:
```

  <add name="session" type="System.Web.SessionState.SessionStateModule" preCondition="managedHandler" />   

```


# SharePoint 2010 Steps: #

Download and extract the GSBS-3.2.0`_`SharePoint2010.zip on you SharePoint machine and use the extracted files as per the instructions given below:

1.    Place GSASearchresults.aspx in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\LAYOUTS"

2.    Place GSAForward.aspx in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\LAYOUTS"

3.    Place GSASearchArea.ascx in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\CONTROLTEMPLATES"

4.    Place file google\_custom\_search\_watermark.gif in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\IMAGES"

5.    Place Folder "GSAFeature" in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\FEATURES"

6.    Place the files "GSA2SP.xsl" and "SP\_Actual.xsl" in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE"

7.    For installation and activation of the "GSAFeature"
  * Open Windows Command prompt.
  * Go to directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\BIN"
  * Run following commands:

```

  stsadm.exe -o installfeature -name GSAFeature -force 
  stsadm.exe -o activatefeature -name GSAFeature -force 

```

8.    To set GSA parameters for performing search using Google search control. In case you have multiple SharePoint web applications, you need to set the GSA parameters for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to set GSA parameters for a given SharePoint web application.

  * In IIS, right click on the SharePoint web site -> Explore. Windows Explorer is opened.
  * In the Windows Explorer, right click on web.config -> Edit
  * Go to "`<appSettings>`" section. (For SharePoint Foundation 2010, you will need to create a new "`<appSettings>`" section)
  * Add the following keys (sample values are for reference):
```

  <add key="GSALocation" value="http://gsa.mycompany.mydomain.com" /> 
  <add key="siteCollection" value="default_collection" /> 
  <add key="frontEnd" value="SPS_frontend" /> 
  <add key="verbose" value="True" /> 
  <add key="GSAStyle" value="false" /> 
  <add key="accesslevel" value="a" />
  <add key="omitSecureCookie" value="false" />
  <add key="xslGSA2SP" 
       value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\14\TEMPLATE\GSA2SP.xsl" /> 
  <add key="xslSP2result" 
       value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\14\TEMPLATE\SP_Actual.xsl" /> 
  <add key="logLocation" value="C:\program files\Common Files\Microsoft Shared\web server extensions\14\LOGS" /> 
  <add key="defaultSearchType" value="publicAndSecure" />
  <add key="SearchTipsHTMLFileName" value="user_help.html" />
  <add key="EnableEmbeddedMode" value="true" />     
  <add key="UseContainerTheme" value="true" />
  <add key="filterParameter" value="p" />   

```
    * accesslevel: value "a" means public and secure search, and value "p" means public search
    * defaultSearchType : value "publicAndSecure" means public and secure search is enabled by default, and value "public" means public search is enabled by default
  * Save the web.config file after making these changes.


9.    To enable session state for performing search with Google search box, the httpmodule for session state needs to be added. In case you have multiple SharePoint web applications, you need to enable the session state for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to enable session state for a given SharePoint web application.
  * In IIS, right click on the SharePoint web site -> Explore. Windows Explorer is opened)
  * In the Windows Explorer, right click on web.config -> Edit
  * Go to "`<modules runAllManagedModulesForAllRequests="true">`" section and add the following module:
```

  <add name="session" type="System.Web.SessionState.SessionStateModule" preCondition="managedHandler" />   

```


# SharePoint 2007 (WSS 3.0) Steps: #

Download and extract the GSBS-3.2.0`_`SharePoint2007.zip on you SharePoint machine and use the extracted files as per the instructions given below:

1.    Place GSASearchresults.aspx in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS"

2.    Place GSAForward.aspx in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS"

3.    Place GSASearchArea.ascx in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\CONTROLTEMPLATES"

4.    Place file google\_custom\_search\_watermark.gif in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\IMAGES"

5.    Place Folder "GSAFeature" in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\FEATURES"

6.    Place the files "GSA2SP.xsl" and "SP\_Actual.xsl" in directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE"

7.    For installation and activation of the "GSAFeature"
  * Open Windows Command prompt.
  * Go to directory "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\BIN"
  * Run following commands:
```

  stsadm.exe -o installfeature -name GSAFeature -force 
  stsadm.exe -o activatefeature -name GSAFeature -force 

```

8.    To set GSA parameters for performing search using Google search control. In case you have multiple SharePoint web applications, you need to set the GSA parameters for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to set GSA parameters for a given SharePoint web application.

  * In IIS, right click on the SharePoint web site -> Open, a Windows Explorer is opened
  * In the Windows Explorer, right click on web.config -> Edit
  * Go to "`<appSettings>`" section. (For WSS 3.0, you will need to create a new "`<appSettings>`" section)
  * Add following keys under it. (Following are sample values for reference)
```

  <add key="GSALocation"  value="http://gsa.mycompany.mydomain.com" /> 
  <add key="siteCollection" value="default_collection" /> 
  <add key="frontEnd" value="SPS_frontend" /> 
  <add key="verbose" value="True" /> 
  <add key="GSAStyle" value="false" />
  <add key="accesslevel" value="a" />
  <add key="omitSecureCookie" value="false" />
  <add key="xslGSA2SP" 
       value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\GSA2SP.xsl" /> 
  <add key="xslSP2result" 
       value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\SP_Actual.xsl" /> 
  <add key="logLocation" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\LOGS" /> 
  <add key="defaultSearchType" value="publicAndSecure" />
  <add key="SearchTipsHTMLFileName" value="user_help.html" />
  <add key="EnableEmbeddedMode" value="true" />     
  <add key="UseContainerTheme" value="true" />
  <add key="filterParameter" value="p" />   
    
```
    * accesslevel: value "a" means public and secure search, and value "p" means public search
    * defaultSearchType : value "publicAndSecure" means public and secure search is enabled by default, and value "public" means public search is enabled by default
  * Save the web.config file after making these changes.

9.    To enable session state for performing search with Google search box, the httpmodule for session state needs to be added. In case you have multiple SharePoint web applications, you need to enable the session state for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to enable session state for a given SharePoint web application.
  * In IIS, right click on the SharePoint web site -> Open, a Windows Explorer is opened
  * In the Windows Explorer, right click on web.config -> Edit
  * Go to "`<httpModules>`" section and add the following:
```

  <add name="Session" type="System.Web.SessionState.SessionStateModule" />    

```

10. GSAapplication.master is a newly added file for reference purpose. This file is used to reduce the whitespace displayed at the top of the search results. To use this sample master page, copy the file to the Layouts folder (path - "C:\Program Files\Common Files\microsoft shared\Web Server Extensions\12\TEMPLATE\LAYOUTS"). Modify the Page Directive MasterPageFile attribute like -  MasterPageFile="~/`_`layouts/GSAapplication.master" in the GSASearchresults.aspx page found on the same path.