## Instructions for manual deployment of Google Search Box for SharePoint 2.6.10 ##

  * The search Box can be deployed on machine with WSS 3.0 or MOSS 2007 installed (32-bit)
  * The search Box can also be deployed on machine with MOSS 2010 installed (64-bit)
  * The documents should be fed and indexed on appliance before doing search
  * For a SharePoint farm scenario, you need to install the Search Box for all the SharePoint server machines connected to the farm.
  * The Google Search Box for SharePoint 2007 can be downloaded at http://code.google.com/p/google-enterprise-connector-sharepoint/downloads/detail?name=GSBS-2.6.0_SharePoint2007.zip&can=2&q=#makechanges
  * The Google Search Box for SharePoint 2010 can be downloaded at http://code.google.com/p/google-enterprise-connector-sharepoint/downloads/detail?name=GSBS-2.6.0_SharePoint2010.zip&can=2&q=#makechanges

## Steps: ##

  1. Place GSASearchresults.aspx in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS
> > (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\LAYOUTS directory)
  1. Place GSASearchArea.ascx in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\CONTROLTEMPLATES
> > (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\CONTROLTEMPLATES directory)
  1. Place file google\_custom\_search\_watermark.gif in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\IMAGES
> > (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\IMAGES directory)
  1. Place Folder "GSAFeature" in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\FEATURES
> > (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE\FEATURES directory)
  1. Place the files "GSA2SP.xsl" and "SP\_Actual.xsl" in directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE

> (For Sharepoint 2010, refer C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\TEMPLATE directory)
> > 6. For installation and activatation of the "GSAFeature"
```
         * Open Windows Command prompt. 
         * Go to directory C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\BIN 
     (For Sharepoint 2010, goto C:\Program Files\Common Files\Microsoft Shared\web server extensions\14\BIN )
         * Run following commands  
                 stsadm.exe -o installfeature -name GSAFeature -force 
                 stsadm.exe -o activatefeature -name GSAFeature -force 
```
> > 7. To set GSA parameters for performing search using Google search control. In case you have multiple SharePoint web applications, you need to set the GSA parameters for all the web applications to perform search from sites of respective SharePoint web application. Following are the steps to set GSA parameters for a given SharePoint web application.
```
         * In IIS, right click on the SharePoint web site -> Open, a Windows Explorer is opened 
(For Sharepoint 2010, in IIS, right click on the SharePoint web site -> Explore. Windows Explorer is opened)
         * In the Windows Explorer, right click on web.config -> Edit 
         * Go to "<appSettings>" section (For MOSS 2007 and MOSS 2010). In case of WSS 3.0, you need to create a new "<appSettings>" section 
         * For Sharepoint 2007, add following keys under it. (Following are sample values for reference) 
          
             <!--Beginning of GSA search control section --> 
             <add key="GSALocation" value="http://gsa.mycompany.mydomain.com" /> 
             <add key="siteCollection" value="default_collection" /> 
             <add key="frontEnd" value="SPS_frontend" /> 
             <add key="verbose" value="True" /> 
             <add key="GSAStyle" value="false" /> 
             <add key="xslGSA2SP" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\GSA2SP.xsl" /> 
             <add key="xslSP2result" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\TEMPLATE\SP_Actual.xsl" /> 
             <add key="logLocation" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\12\LOGS\ " /> 
             <!--End of GSA search control section --> 
         * For Sharepoint 2010, add following keys under it. (Following are sample values for reference) 
          
             <!--Beginning of GSA search control section --> 
             <add key="GSALocation" value="http://gsa.mycompany.mydomain.com" /> 
             <add key="siteCollection" value="default_collection" /> 
             <add key="frontEnd" value="SPS_frontend" /> 
             <add key="verbose" value="True" /> 
             <add key="GSAStyle" value="false" /> 
             <add key="xslGSA2SP" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\14\TEMPLATE\GSA2SP.xsl" /> 
             <add key="xslSP2result" value="C:\Program Files\Common Files\Microsoft Shared\Web Server Extensions\14\TEMPLATE\SP_Actual.xsl" /> 
             <add key="logLocation" value="C:\program files\Common Files\Microsoft Shared\web server extensions\14\LOGS\" /> 
             <!--End of GSA search control section --> 
```


> 8. To setup SharePoint like frontend:
```
         * open GSA Admin page->Serving->FrontEnd 
         * create a new frontend "SPS_frontend" 
         * copy the contents of file "SPS_frontend.xslt" packaged in the downloadable zip file 
         * save it 
```