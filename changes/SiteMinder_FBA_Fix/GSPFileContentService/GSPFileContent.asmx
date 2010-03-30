<%@ WebService Language="C#" Class="GSPFileContent" %>
using System;
using System.Web;
using System.Web.Services;
using System.Web.Services.Protocols;

using Microsoft.SharePoint;

[WebService(Namespace = "http://tempuri.org/")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class GSPFileContent : System.Web.Services.WebService
{
    public GSPFileContent()
    {

        //Uncomment the following line if using designed components 
        //InitializeComponent(); 
    }

    [WebMethod]
    public string HelloWorld() {
        return "Hello World";
    }

    [WebMethod]
    public byte[] GetFileContents(String fileURL)
    {
        SPSite site = new SPSite(fileURL);
        SPWeb web = site.OpenWeb();


        SPFile file = web.GetFile(fileURL);//Get the file contents from the sharepoint 
        byte[] contents = file.OpenBinary();
        return contents;
    }

}
