# Pre-requisites #

  * The search Box can be deployed on machine with WSS 3.0 or MOSS 2007 installed (32-bit)
  * The search Box can also be deployed on machine with SharePoint Server 2010 or SharePoint Foundation 2010 installed (64-bit).
  * For a SharePoint farm scenario, you need to install the Search Box for all the SharePoint Web Front Ends connected to the farm.
  * The Google Search Box for SharePoint can be downloaded from
http://code.google.com/p/google-enterprise-connector-sharepoint/downloads/list


# Steps: #

Extract the GSBS-3.0.0.zip on you SharePoint machine and use the extracted files as per the instructions given below:

1.    Place GSASearchresults.aspx in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS
> (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\LAYOUTS directory)

2.    Place GSAForward.aspx in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS
> (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\LAYOUTS directory)

3.    Place GSASearchArea.ascx in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\CONTROLTEMPLATES


> (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\CONTROLTEMPLATES directory)


4.    Place file google\_custom\_search\_watermark.gif in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\IMAGES


> (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\IMAGES directory)

5.    Place Folder "GSAFeature" in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\FEATURES


> (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\FEATURES directory)


6.    Place the files "GSA2SP.xsl" and "SP\_Actual.xsl" in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE


> (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE directory)

7.    For installation and activation of the "GSAFeature"


```
         * Open Windows Command prompt. 
         * Go to directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\BIN 
     (For Sharepoint 2010, goto C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\BIN )
         * Run following commands  
                 stsadm.exe -o installfeature -name GSAFeature -force 
                 stsadm.exe -o activatefeature -name GSAFeature -force 
```

8.    To set GSA parameters for performing search using Google search control. In case you have multiple SharePoint web applications, you need to set the GSA parameters for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to set GSA parameters for a given SharePoint web application.


```
   * In IIS, right click on the SharePoint web site -> Open, a Windows Explorer is opened 
     (For Sharepoint 2010, in IIS, right click on the SharePoint web site -> Explore. Windows Explorer is opened)               
   * In the Windows Explorer, right click on web.config -> Edit               
   *  Go to "<appSettings>" section (For MOSS 2007 and SharePoint 2010). In case of WSS 3.0 and SharePoint Foundation 2010 you need to create a new "<appSettings>" section               
   *  For Sharepoint 2007, add following keys under it. (Following are sample values for reference)
        <!--Beginning of GSA search control section -->
      <add key="GSALocation"  value="http://gsa.mycompany.mydomain.com" /> 
      <add key="siteCollection" value="default_collection" /> 
      <add key="frontEnd" value="SPS_frontend" /> 
      <add key="verbose" value="True" /> 
      <add key="GSAStyle" value="false" /> 
<!--Search Type : value "a" means public and secure search and value "p" means public search -->
      <add key="accesslevel" value="a" />
      <add key="omitSecureCookie" value="false" />
      <add key="xslGSA2SP" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\GSA2SP.xsl" /> 
      <add key="xslSP2result" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\SP_Actual.xsl" /> 
      <add key="logLocation" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\LOGS\ " /> 
<!--Default Search Type : value "publicAndSecure" means public and secure search is enabled by default, and value "public" means public search is enabled by default-->
      <add key="defaultSearchType" value="publicAndSecure" />
      <add key="SearchTipsHTMLFileName" value="user_help.html" />
      <add key="EnableEmbeddedMode" value="true" />     
      <add key="UseContainerTheme" value="true" />
      <add key="filterParameter" value="p" />   
<!--End of GSA search control section --> 

   * For Sharepoint 2010, add following keys under it. (Following are sample values for reference)  
    <!--Beginning of GSA search control section --> 

       <add key="GSALocation" value="http://gsa.mycompany.mydomain.com" /> 
       <add key="siteCollection" value="default_collection" /> 
       <add key="frontEnd" value="SPS_frontend" /> 
       <add key="verbose" value="True" /> 
       <add key="GSAStyle" value="false" /> 
<!--Search Type : value "a" means public and secure search and value "p" means public search -->
       <add key="accesslevel" value="a" />
       <add key="omitSecureCookie" value="false" />
       <add key="xslGSA2SP" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\14\TEMPLATE\GSA2SP.xsl" /> 
       <add key="xslSP2result" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\14\TEMPLATE\SP_Actual.xsl" /> 
       <add key="logLocation" value="C:\program files\Common Files\Microsoft Shared\web server extensions\14\LOGS\" /> 
<!--Default Search Type : value "publicAndSecure" means public and secure search is enabled by default, and value "public" means public search is enabled by default-->
       <add key="defaultSearchType" value="publicAndSecure" />
       <add key="SearchTipsHTMLFileName" value="user_help.html" />
       <add key="EnableEmbeddedMode" value="true" />     
       <add key="UseContainerTheme" value="true" />
       <add key="filterParameter" value="p" />   
       <!--End of GSA search control section -->   

    
```
Save the web.config file after making these changes.


9.    To enable session state for performing search with Google search box, the httpmodule for session state needs to be added. In case you have multiple SharePoint web applications, you need to enable the session state for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to enable session state for a given SharePoint web application.

```

      * In IIS, right click on the SharePoint web site -> Open, a Windows Explorer is opened  
       (For Sharepoint 2010, in IIS, right click on the SharePoint web site -> Explore. Windows Explorer is opened)  
     * In the Windows Explorer, right click on web.config -> Edit   
     * Go to "<httpModules>" section (For MOSS 2007only), and add the following module under it -  
       <add name="Session" type="System.Web.SessionState.SessionStateModule" />    
      * (Perform this step only for SharePoint 2010) Go to "<modules runAllManagedModulesForAllRequests="true">" section and add the       following module under it -   
      <add name="session" type="System.Web.SessionState.SessionStateModule" preCondition="managedHandler" />   

```



10. GSAapplication.master is a newly added file for reference purpose. This file is used to reduce the whitespace displayed at the top of the search results. (steps below are applicable only for SharePoint 2007)
> To use this sample master page, copy the file to the Layouts folder (path - C:\Program Files\Common Files\microsoft shared\Web Server Extensions\12\TEMPLATE\LAYOUTS).
> Modify the Page Directive MasterPageFile attribute like -  MasterPageFile="~/