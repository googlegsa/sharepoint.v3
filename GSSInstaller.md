This document guides you through building your own version of Google Services for SharePoint Installer. This can be helpful especially when the existing installer binary does not support the hardware platform you are using or you want to do other customizations.

**Pre-requisite:**
  * Visual Studio 2007 IDE

**Following are the steps for building the installer:**

1. Download the source from the following SVN URL:
http://google-enterprise-connector-sharepoint.googlecode.com/svn/tags/2.0.0/GSSInstaller

2. Open the downloaded project source in the Visual Studio IDE.
File -> Open -> Project/Solution
And select the path of the solution file GSSInstaller.sln

3. Compile and build the project.
Right click the solution in solution explorer and select "Build Solution"

4. You can find the newly built installer at the path:
\GSSInstaller\Setup\Release\GSS.msi

5. In order to view and change various property values and other settings follow these steps:

> In 'Solution Explorer' view, right click on 'GSSInstallerApplication', Choose 'View'.
> You can find the following options:
```
     * File System
     * Registry
     * File Types
     * User interface
     * Custom actions
     * Launch conditions
```

> Choose any of the above and view/add/modify the value of properties in the 'Properties' view



Note: To create an Installer for a 64-bit Platform refer to http://msdn.microsoft.com/en-us/library/cd7a85k9.aspx