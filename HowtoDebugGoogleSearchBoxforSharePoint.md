# Introduction #

It is possible to debug Google Search Box for SharePoint using Microsoft Visual Studio (2005\2008). In order to debug Google Search Box for SharePoint, you need to be on the same machine where the Search Box is installed. Debugging is quite helpful especially when you want to check the workflow of the Search Box or want to troubleshoot Google Search Box for SharePoint. It is possible to debug flow of Search Box on one or multiple web applications simultaneously.


## To debug the Search Box, please follow the steps below: ##

  1. Open the SharePoint site in a browser where the Serach Box is installed.
  1. Go to the IIS Manager.
  1. Browse through IIS->[name](machine.md)->Web sItes->[sharepoint web site](your.md)
  1. Right click on the web site and click on open to see the physical location of the web.config file of the given web site.
  1. Edit the web.config file
  1. Check the following:
```
      <system.web>
          <compilation defaultLanguage="c#" debug="true" />
      </system.web>
```
  1. If debug is not set to **"true"**, please set it to **"true"**.
  1. Save web.config file.
  1. Open file "C:\Program Files\Common Files\Microsoft Shared\web server extensions\12\TEMPLATE\LAYOUTS\searchresults.aspx" in Visual Studio editor
  1. Click on Debug->attach to process...
  1. It will open a dialog similar to task manager.
  1. Select the process w3wp.exe equivalent to the SharePoint web application and click on "attach".
  1. Set appropriate breakpoints on file "searchresults.aspx"
  1. Thats all! you can now fire test search and the debug it using the Visual Studio editor.

_**Note:**_ If you want to debug multiple SharePoint web applications, follow steps 3-8 for all the web applications, and attach all the "w3wp.exe" corresponding to the web applications while debugging (step 12)