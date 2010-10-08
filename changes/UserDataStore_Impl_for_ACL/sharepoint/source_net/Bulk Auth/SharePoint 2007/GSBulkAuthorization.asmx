<%@ WebService Language="C#" Class="BulkAuthorization" %>
using System;
using System.Net;
using System.Text;
using System.Web.Services;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Utilities;
using System.Collections.Generic;


[WebService(Namespace = "gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class BulkAuthorization : System.Web.Services.WebService
{
    private const String UNKNOWN_SITE_COLLECTION = "UNKNOWN_SITE_COLLECTION";

    /// <summary>
    /// Checks if this web service can be called
    /// </summary>
    /// <returns></returns>
    [WebMethod]
    public string CheckConnectivity()
    {
        // All the pre-requisites for running this web service should be checked here.
        // Currently, we are ensuring that RunWithElevatedPrivileges works.
        try
        {
            SPSecurity.RunWithElevatedPrivileges(delegate()
            {
            });
        }
        catch (Exception e)
        {
            return e.Message;
        }
        return "success";
    }

    /// <summary>
    /// Authorizes a user against an array of AuthDataPackets containing AuthData objects. Each AuthDataPacket corresponds
    /// to a site collection.
    /// </summary>
    /// <param name="authDataPacketArray"></param>
    /// <param name="username"></param>
    [WebMethod]
    public void Authorize(ref AuthDataPacket[] authDataPacketArray, string username)
    {
        // TODO Check if user is app pool user (SharePoint\\System). If yes, return true for everything.
        ///////

        WSContext wsContext = new WSContext(SPContext.Current, username);
        foreach (AuthDataPacket authDataPacket in authDataPacketArray)
        {
            if (null == authDataPacket)
            {
                continue;
            }
            AuthData[] authDataArray = authDataPacket.AuthDataArray;
            if (null == authDataArray)
            {
                authDataPacket.Message = "AuthDataPacket contains no data to authorize. ";
                continue;
            }

            string siteCollUrl = authDataPacket.SiteCollectionUrl;
            if (null != siteCollUrl && siteCollUrl.Length > 0 && !siteCollUrl.Equals(UNKNOWN_SITE_COLLECTION))
            {
                try
                {
                    wsContext.UsingSiteCollectionUrl(siteCollUrl);
                }
                catch (Exception e)
                {
                    authDataPacket.Message = GetFullMessage(e);
                    continue;
                }
            }

            foreach (AuthData authData in authDataArray)
            {
                if (null == authData)
                {
                    continue;
                }

                if (!wsContext.IsUsingSiteCollectionUrl)
                {
                    try
                    {
                        if (authData.isAlert())
                        {
                            wsContext.UsingSiteUrl(authData.Container);
                        }
                        else
                        {
                            wsContext.UsingListUrl(authData.Container);
                        }
                    }
                    catch (Exception e)
                    {
                        authData.Message = GetFullMessage(e);
                        continue;
                    }
                }

                try
                {
                    Authorize(authData, wsContext);
                }
                catch (Exception e)
                {
                    authData.Message = "Authorization failure! " + GetFullMessage(e);
                    continue;
                }
                authData.IsDone = true;
            }
            authDataPacket.IsDone = true;
        }
    }

    /// <summary>
    /// Authorizes a user against a single site collection and that is the current site collection context that WS is using.
    /// The site collection information is obtained from SPContext and is determined by the endpoint used while calling WS.
    /// </summary>
    /// <param name="authDataPacket"></param>
    /// <param name="username"></param>
    [WebMethod]
    public void AuthorizeInCurrentSiteCollectionContext(ref AuthDataPacket authDataPacket, string username)
    {
        // TODO Check if user is app pool user (SharePoint\\System). If yes, return true for everything.
        ///////

        WSContext wsContext = new WSContext(SPContext.Current, username);
        wsContext.UsingCurrentSPContext();

        AuthData[] authDataArray = authDataPacket.AuthDataArray;
        if (null == authDataArray)
        {
            return;
        }
        foreach (AuthData authData in authDataArray)
        {
            if (null == authData)
            {
                continue;
            }
            try
            {
                Authorize(authData, wsContext);
            }
            catch (Exception e)
            {
                authData.Message = "Authorization failure! " + GetFullMessage(e);
                continue;
            }
            authData.IsDone = true;
        }
        authDataPacket.IsDone = true;
    }

    private void Authorize(AuthData authData, WSContext wsContext)
    {
        if (null == wsContext.User)
        {
            throw new Exception("SPUser is null! ");
        }

        SPWeb web = (authData.isAlert()) ? wsContext.GetWebUsingSiteUrl(authData.Container) : wsContext.GetWebUsingListUrl(authData.Container);
        if (authData.ComplexDocId != null && authData.ComplexDocId.StartsWith("[ALERT]"))
        {
            Guid alert_guid = new Guid(authData.ItemId);
            SPAlert alert = web.Alerts[alert_guid];
            if (null == alert)
            {
                throw new Exception("Alert not found. alert_guid [ " + alert_guid + " ] web " + web.Url);
            }
            if (alert.User.LoginName.ToUpper().Equals(wsContext.User.LoginName.ToUpper()))
            {
                authData.IsAllowed = true;
            }
        }
        else
        {
            SPList list = web.GetListFromUrl(authData.Container);
            if (authData.isList())
            {
                bool isAllowed = list.DoesUserHavePermissions(wsContext.User, SPBasePermissions.ViewListItems);
                authData.IsAllowed = isAllowed;
            }
            else
            {
                int itemId = int.Parse(authData.ItemId);
                SPListItem item = list.GetItemById(itemId);
                bool isAllowed = item.DoesUserHavePermissions(wsContext.User, SPBasePermissions.ViewListItems);
                authData.IsAllowed = isAllowed;
            }
        }
    }

    private string GetFullMessage(Exception e)
    {
        StringBuilder message = new StringBuilder();
        while (null != e)
        {
            message.Append(e.Message);
            e = e.InnerException;
        }
        return message.ToString();
    }
}

/// <summary>
/// Contains a list of <see cref="AuthData"/> for authorization. All the AuthData objects belongs to a single site collection
/// </summary>
[WebService(Namespace = "BulkAuthorization.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class AuthDataPacket
{
    private AuthData[] authDataArray;
    public AuthData[] AuthDataArray
    {
        get { return authDataArray; }
        set { authDataArray = value; }
    }

    private string siteCollectionUrl;
    public string SiteCollectionUrl
    {
        get { return siteCollectionUrl; }
        set { siteCollectionUrl = value; }
    }

    private string message;
    public string Message
    {
        get { return message; }
        set { message = value; }
    }

    private bool isDone;
    public bool IsDone
    {
        get { return isDone; }
        set { isDone = value; }
    }
}

/// <summary>
/// The basic authorization unit
/// </summary>
[WebService(Namespace = "BulkAuthorization.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class AuthData
{
    private string container;
    public string Container
    {
        get { return container; }
        set { container = value; }
    }

    private string itemId;
    public string ItemId
    {
        get { return itemId; }
        set { itemId = value; }
    }

    private bool isAllowed;
    public bool IsAllowed
    {
        get { return isAllowed; }
        set { isAllowed = value; }
    }

    private string message;
    public string Message
    {
        get { return message; }
        set { message = value; }
    }

    private string complexDocId;
    public string ComplexDocId
    {
        get { return complexDocId; }
        set { complexDocId = value; }
    }

    private bool isDone;
    public bool IsDone
    {
        get { return isDone; }
        set { isDone = value; }
    }

    public bool isAlert()
    {
        if (ComplexDocId != null && ComplexDocId.StartsWith("[ALERT]"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    // Note that, SP connector treats alerts as list item and parent site as the container lists; hence, for an alert,
    // both isAlert and isList will return true. But to make the difference, the connector uses a distinguished ID for
    // alerts. Hence, during authz, it must always be checked if a URL that looks like a List is not actually an alert.

    public bool isList()
    {
        if (ItemId == null || ItemId == "" || ItemId.StartsWith("{"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

/// <summary>
/// Creates a context for authZ. An object of this class can provide all the necessary information required for authorization.
/// Examples include SPUser for a user, SPWeb for a URL etc. Instantiation of most of the objects are done lazily. Also, an
/// object is cached till the time next request comes and it is found that the cached object cannot serve the request.
/// In view optimization, this class knows about the scope of usage for every type of objects; for example, SPUser objects
/// are same across every sites and one WS call is made for one user; hence, the SPUser object is created only once.
/// </summary>
public class WSContext
{
    private readonly SPContext spContext;

    private readonly string username;
    private SPUser user;

    private SPSite site;
    private bool isUsingSiteCollectionUrl;

    private KeyValuePair<String, SPWeb> cachedWeb = new KeyValuePair<string, SPWeb>(null, null);

    public SPSite Site
    {
        get { return site; }
    }

    public bool IsUsingSiteCollectionUrl
    {
        get { return isUsingSiteCollectionUrl; }
    }

    public SPUser User
    {
        get { return user; }
    }

    public WSContext(SPContext spContext, string username)
    {
        if (null == spContext || null == spContext.Site)
        {
            throw new Exception("Unable to get SharePoint context. The web service endpoint might not be referring to aan active SharePoitn site. ");
        }
        if (null == spContext.Site)
        {
            throw new Exception("Site Colllection not found!");
        }
        this.spContext = spContext;
        this.username = username;
    }

    ~WSContext()
    {
        disposeCurrentSiteCollection();
    }

    public void disposeCachedWeb()
    {
        if (null != cachedWeb.Value && null != site && !site.ID.Equals(spContext.Site.ID))
        {
            cachedWeb.Value.Dispose();
            cachedWeb = new KeyValuePair<string, SPWeb>(null, null);
        }
    }

    public void disposeCurrentSiteCollection()
    {
        if (null != site && !site.ID.Equals(spContext.Site.ID))
        {
            if (null != cachedWeb.Value)
            {
                cachedWeb.Value.Dispose();
                cachedWeb = new KeyValuePair<string, SPWeb>(null, null);
            }
            site.Dispose();
            site = null;
        }
    }

    public void UsingCurrentSPContext()
    {
        site = spContext.Site;
        if (null == site)
        {
            throw new Exception("Site Colllection not found!");
        }

        if (null == user)
        {
            InitSPUser();
        }
    }

    public void UsingSiteCollectionUrl(String siteCollUrl)
    {
        InitSPSite(siteCollUrl);
        isUsingSiteCollectionUrl = true;
        if (null == user)
        {
            InitSPUser();
        }
    }

    public void UsingListUrl(String listUrl)
    {
        if (null == site)
        {
            InitSPSite(listUrl);
        }
        else
        {
            try
            {
                GetWebUsingListUrl(listUrl);
            }
            catch (Exception currentSPSiteCannotServeThisUrl)
            {
                InitSPSite(listUrl);
            }
        }

        isUsingSiteCollectionUrl = false;

        if (null == user)
        {
            InitSPUser();
        }
    }

    public void UsingSiteUrl(String siteUrl)
    {
        if (null == site)
        {
            InitSPSite(siteUrl);
        }
        else
        {
            try
            {
                GetWebUsingSiteUrl(siteUrl);
            }
            catch (Exception currentSPSiteCannotServeThisUrl)
            {
                InitSPSite(siteUrl);
            }
        }

        isUsingSiteCollectionUrl = false;

        if (null == user)
        {
            InitSPUser();
        }
    }

    private void InitSPSite(String url)
    {
        disposeCurrentSiteCollection();
        SPSecurity.RunWithElevatedPrivileges(delegate()
        {
            // try creating the SPSite object for the incoming URL. If fails, try again by changing the URL format FQDN to Non-FQDN or vice-versa.
            try
            {
                site = new SPSite(url);
            }
            catch (Exception e)
            {
                site = new SPSite(SwitchURLFormat(url));
            }
        });

        if (null == site)
        {
            throw new Exception("Site Colllection not found!");
        }
    }

    private void InitSPUser()
    {
        string username = this.username;
        SPPrincipalInfo userInfo = SPUtility.ResolveWindowsPrincipal(site.WebApplication, username, SPPrincipalType.All, false);
        if (null != userInfo)
        {
            username = userInfo.LoginName;
        }

        SPWeb web = site.OpenWeb();
        // First ensure that the current user has rights to view pages or list items on the web. This will ensure that SPUser object can be constructed for this username.
        bool web_auth = web.DoesUserHavePermissions(username, SPBasePermissions.ViewPages | SPBasePermissions.ViewListItems);

        try
        {
            user = web.AllUsers[username];
        }
        catch (Exception e1)
        {
            try
            {
                user = web.SiteUsers[username];
            }
            catch (Exception e2)
            {
                try
                {
                    user = web.Users[username];
                }
                catch (Exception e3)
                {
                    string msg = "User " + username + " information not found in the parent site collection of web " + web.Url;
                    if (null == userInfo)
                    {
                        throw new Exception(msg, new Exception("User " + username + " can not be resolved into a valid SharePoint user."));
                    }
                    else
                    {
                        throw new Exception(msg);
                    }
                }
            }
        }
    }

    private string SwitchURLFormat(string siteURL)
    {
        Uri url = new Uri(siteURL);
        string host = url.Host;
        if (host.Contains("."))
        {
            host = host.Split('.')[0];
        }
        else
        {
            IPHostEntry hostEntry = Dns.GetHostEntry(host);
            host = hostEntry.HostName;
        }
        siteURL = url.Scheme + "://" + host + ":" + url.Port + url.AbsolutePath;
        return siteURL;
    }

    public SPWeb GetWebUsingListUrl(String listUrl)
    {
        if (null != cachedWeb.Key && cachedWeb.Key.Equals(listUrl))
        {
            return cachedWeb.Value;
        }

        Uri uri = new Uri(listUrl);
        string[] segments = uri.Segments;
        StringBuilder urlBuilder = new StringBuilder(segments[0]);
        for (int i = 1; i < segments.Length - 3; ++i)
        {
            urlBuilder.Append(segments[i]);
        }

        SPWeb web = GetWeb(urlBuilder.ToString());
        cachedWeb = new KeyValuePair<string, SPWeb>(listUrl, web);
        return web;
    }

    public SPWeb GetWebUsingSiteUrl(String siteUrl)
    {
        if (null != cachedWeb.Key && cachedWeb.Key.Equals(siteUrl))
        {
            return cachedWeb.Value;
        }
        SPWeb web = GetWeb(new Uri(siteUrl).AbsolutePath);
        cachedWeb = new KeyValuePair<string, SPWeb>(siteUrl, web);
        return web;
    }

    private SPWeb GetWeb(String relativeWebUrl)
    {
        disposeCachedWeb();
        SPWeb web = null;
        try
        {
            return site.OpenWeb(relativeWebUrl);
        }
        catch (Exception e)
        {
            throw new Exception("Could not get SPWeb for server relative URL [ " + relativeWebUrl + " ]", e);
        }
        if (null == web)
        {
            throw new Exception("Could not get SPWeb for server relative URL [ " + relativeWebUrl + " ]");
        }
    }
}