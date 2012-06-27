<%@ WebService Language="C#" Class="SiteDiscovery" %>
using System;
using System.Collections;
using System.Collections.Generic;
using System.Web;
using System.Web.Services;
using System.Web.Services.Protocols;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;

[WebService(Namespace = "gssitediscovery.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class SiteDiscovery : System.Web.Services.WebService
{
    public SiteDiscovery  () {

        //Uncomment the following line if using designed components
        //InitializeComponent();
    }

    /// <summary>
    /// Check connectivity of the GSP Site discovery service.
    /// </summary>
    /// <returns></returns>
    [WebMethod]
    public string CheckConnectivity() {
    SPWebApplicationCollection wc1 = SPWebService.AdministrationService.WebApplications;
        SPWebApplicationCollection wc2 = SPWebService.ContentService.WebApplications;
        return "success";
    }

    /// <summary>
    /// Get the top level URL of all site collections form all web applications for a given sharepoint installation.
    /// </summary>
    /// <returns></returns>
    [WebMethod]
    public ArrayList GetAllSiteCollectionFromAllWebApps()
    {
        ArrayList webSiteList = new ArrayList();

        //get the site collection for the central administration
        foreach (SPWebApplication wa in SPWebService.AdministrationService.WebApplications)
        {
            foreach (SPSite sc in wa.Sites)
            {
                try
                {
                    //add the site collection top level url in\to our arraylist
                    webSiteList.Add(sc.Url);
                }
                finally
                {
                    sc.Dispose();
                }
            }
        }

        foreach (SPWebApplication wa in SPWebService.ContentService.WebApplications)
        {
            //Console.WriteLine("web Application: " + wa.Name);
            foreach (SPSite sc in wa.Sites)
            {
                try
                {
                    webSiteList.Add(sc.Url);
                }
                finally
                {
                    sc.Dispose();
                }
            }
        }
        return webSiteList;//return the list
    }

    /// <summary>
    /// Stores the information about the crawl behavior of a web
    /// </summary>
    [WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [Serializable]
    public class WebCrawlInfo
    {
        // An identification of the web whose information is contained in the current WebCrawlInfo
        private string webKey;

        private bool crawlAspxPages;
        private bool noCrawl;

        // status indicates whether the CrawlInfo is valid or not. If invalid, error contains the possible reason.
        private bool status;
        private string error;

        public string WebKey
        {
            get { return webKey; }
            set { webKey = value; }
        }

        public bool CrawlAspxPages
        {
            get { return crawlAspxPages; }
            set { crawlAspxPages = value; }
        }

        public bool NoCrawl
        {
            get { return noCrawl; }
            set { noCrawl = value; }
        }

        public bool Status
        {
            get { return status; }
            set { status = value; }
        }

        public string Error
        {
            get { return error; }
            set { error = value; }
        }
    }

    /// <summary>
    /// To get the <see cref="WebCrawlInfo"/> of the current web
    /// </summary>
    /// <returns></returns>
    [WebMethod]
    public WebCrawlInfo GetWebCrawlInfo()
    {
        SPContext spContext = SPContext.Current;
        if (null == spContext)
        {
            throw new Exception("Unable to get SharePoint context. The web service endpoint might not be referring to a valid SharePoitn site. ");
        }
        SPWeb web = spContext.Web;
        if (null == web)
        {
            throw new Exception("SharePoint site not found");
        }

        try
        {
            WebCrawlInfo webCrawlInfo = new WebCrawlInfo();
            webCrawlInfo.WebKey = web.Url;
            webCrawlInfo.CrawlAspxPages = web.AllowAutomaticASPXPageIndexing;
            webCrawlInfo.NoCrawl = web.NoCrawl;
            webCrawlInfo.Status = true;
            return webCrawlInfo;
        }
        catch (Exception e)
        {
            throw new Exception("Could not get the required information for the web ", e);
        }
        finally
        {
            web.Dispose();
        }
    }

    /// <summary>
    /// To get the <see cref="WebCrawlInfo"/> of a list of webs
    /// </summary>
    /// <returns></returns>
    [WebMethod]
    public List<WebCrawlInfo> GetWebCrawlInfoInBatch(List<string> webUrls)
    {
        List<WebCrawlInfo> wsResult = new List<WebCrawlInfo>();
        if (null == webUrls || webUrls.Count == 0)
        {
            return wsResult;
        }

        foreach (string webUrl in webUrls)
        {
            WebCrawlInfo webCrawlInfo = new WebCrawlInfo();
            webCrawlInfo.WebKey = webUrl;
            SPSite site = null;
            SPWeb web = null;
            try
            {
                site = new SPSite(webUrl);
                if (null == site)
                {
                    webCrawlInfo.Status = false;
                    webCrawlInfo.Error = "SharePoint site collection not found for url " + webUrl;
                }
                else
                {
                    web = site.OpenWeb();
                    if (null == web)
                    {
                        webCrawlInfo.Status = false;
                        webCrawlInfo.Error = "SharePoint site not found for url " + webUrl;
                    }
                    else
                    {
                        webCrawlInfo.CrawlAspxPages = web.AllowAutomaticASPXPageIndexing;
                        webCrawlInfo.NoCrawl = web.NoCrawl;
                        webCrawlInfo.Status = true;
                    }
                }
            }
            catch (Exception e)
            {
                webCrawlInfo.Status = false;
                webCrawlInfo.Error = "Could not get the required information for for url " + webUrl + " . Exception: " + e.Message;
            }
            finally
            {
                if (null != web)
                    web.Dispose();

                if (null != site)
                    site.Dispose();
            }
            wsResult.Add(webCrawlInfo);
        }
        return wsResult;
    }

    /// <summary>
    /// Stores the information about the crawl behavior of a list
    /// </summary>
    [WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
    [WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
    [Serializable]
    public class ListCrawlInfo
    {
        private string listGuid;
        private bool noCrawl;

        // status indicates whether the CrawlInfo is valid or not. If invalid, error contains the possible reason.
        private bool status;
        private string error;

        public string ListGuid
        {
            get { return listGuid; }
            set { listGuid = value; }
        }

        public bool NoCrawl
        {
            get { return noCrawl; }
            set { noCrawl = value; }
        }

        public bool Status
        {
            get { return status; }
            set { status = value; }
        }

        public string Error
        {
            get { return error; }
            set { error = value; }
        }
    }

    /// <summary>
    /// To get the <see cref="ListCrawlInfo"/> of the current web
    /// </summary>
    /// <param name="listGuids"></param>
    /// <returns></returns>
    [WebMethod]
    public List<ListCrawlInfo> GetListCrawlInfo(List<string> listGuids)
    {
        SPContext spContext = SPContext.Current;
        if (null == spContext)
        {
            throw new Exception("Unable to get SharePoint context. The web service endpoint might not be referring to a valid SharePoitn site. ");
        }
        SPWeb web = spContext.Web;
        if (null == web)
        {
            throw new Exception("SharePoint site not found");
        }

        List<ListCrawlInfo> listCrawlInfo = new List<ListCrawlInfo>();
        foreach (string guid in listGuids)
        {
            ListCrawlInfo info = new ListCrawlInfo();
            info.ListGuid = guid;
            try
            {
                Guid key = new Guid(guid);
                try
                {
                    SPList list = web.Lists[key];
                    info.NoCrawl = list.NoCrawl;
                    info.Status = true;
                }
                catch (Exception e)
                {
                    info.Error = "List not found! Exception [ " + e.Message + " ] ";
                }
            }
            catch (Exception e)
            {
                info.Error = "Invalid List GUID! Exception [ " + e.Message + " ] ";
            }
            listCrawlInfo.Add(info);
        }

        web.Dispose();
        return listCrawlInfo;
    }

    /// <summary>
    /// Checks whether a list is marked for crawling or not
    /// </summary>
    /// <param name="listGUID"></param>
    /// <returns></returns>
    [WebMethod]
    public bool IsCrawlableList(String listGUID)
    {
        SPContext spContext = SPContext.Current;
        if (null == spContext)
        {
            throw new Exception("Unable to get SharePoint context. The web service endpoint might not be referring to a valid SharePoitn site. ");
        }
        SPWeb web = spContext.Web;
        if (null == web)
        {
            throw new Exception("SharePoint site not found");
        }
        try
        {
            Guid key = new Guid(listGUID);
            try
            {
                SPList list = web.Lists[key];
                return list.NoCrawl;
            }
            catch (Exception e)
            {
                throw new Exception("List no found!", e);
            }
        }
        catch (Exception e)
        {
            throw new Exception("Invalid List GUID!", e);
        }
        finally
        {
            web.Dispose();
        }
        return false;
    }
}

