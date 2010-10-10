//Copyright 2010 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

<%@ WebService Language="C#" Class="BulkAuthorization" %>
using System;
using System.Net;
using System.Text;
using System.Web.Services;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Utilities;
using System.Collections.Generic;


/// <summary>
/// Google Search Appliance Connector for Microsoft SharePoint uses this web service to authorize documents at serve time.
/// </summary>
[WebService(Namespace = "gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class BulkAuthorization : System.Web.Services.WebService
{
    /// <summary>
    /// Checks if this web service can be called
    /// </summary>
    /// <returns></returns>
    [WebMethod]
    public string CheckConnectivity()
    {
        // All the pre-requisites for running this web service should be checked here.
        SPContext spContext = SPContext.Current;
        if (null == spContext || null == spContext.Site)
        {
            throw new Exception("Unable to get SharePoint context. The web service endpoint might not be referring to an active SharePoitn site. ");
        }
        if (null == spContext.Site)
        {
            throw new Exception("Site Collection not found!");
        }

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
    /// Authorizes a user against a batch of documents. This method gives a high level view of the way authorization progresses.
    /// It iterates over each AuthData object of every AuthDataPacket and attempts authorization. For the internal housekeeping,
    /// WSContext is used which keeps track of the authorization context. Typically, at the start of every iteration, iterator
    /// informs WSContext about the site collection/site/list being used. Afterward, when actual authorization done, it ask for
    /// specific details from WSContext. WSContext is optimized to serve these requests in a better and performant way
    /// since it knows the context in which authorization is being carried out.
    /// </summary>
    /// <param name="authDataPacketArray"></param>
    /// <param name="username"></param>
    [WebMethod]
    public void Authorize(ref AuthDataPacket[] authDataPacketArray, string username)
    {
        // TODO Check if user is app pool user (SharePoint\\System). If yes, return true for everything.
        ///////
        WSContext wsContext = new WSContext(SPContext.Current, username, false);
        foreach (AuthDataPacket authDataPacket in authDataPacketArray)
        {
            if (null == authDataPacket)
            {
                continue;
            }

            AuthData[] authDataArray = authDataPacket.AuthDataArray;
            if (null == authDataArray)
            {
                continue;
            }

            try
            {
                wsContext.Using(authDataPacket);
            }
            catch (Exception e)
            {
                authDataPacket.Message = GetFullMessage(e);
                continue;
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
    }

    /// <summary>
    /// Authorizes a user against a batch of URLs that belongs to a single site collection and that is the current site collection
    /// context in WS is running. The site collection information is obtained from SPContext and is determined by the endpoint used
    /// while calling WS. If a document does not actually belongs to this site collection, its authorization will fail.
    /// </summary>
    /// <param name="authDataPacket"></param>
    /// <param name="username"></param>
    [WebMethod]
    public void AuthorizeInCurrentSiteCollectionContext(ref AuthDataPacket authDataPacket, string username)
    {
        // TODO Check if user is app pool user (SharePoint\\System). If yes, return true for everything.
        ///////
        WSContext wsContext = new WSContext(SPContext.Current, username, true);
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

    /// <summary>
    /// Actual Authorization is done here
    /// </summary>
    /// <param name="authData">the data for which the authorization is done</param>
    /// <param name="wsContext">serves specific details like SPUser and SPWeb required for authZ</param>
    private void Authorize(AuthData authData, WSContext wsContext)
    {
        SPUser user = wsContext.GetUser();
        SPWeb web = wsContext.GetWeb(authData);
        if (authData.Type == AuthData.EntityType.ALERT)
        {
            Guid alert_guid = new Guid(authData.ItemId);
            SPAlert alert = web.Alerts[alert_guid];
            if (null == alert)
            {
                throw new Exception("Alert not found. alert_guid [ " + alert_guid + " ] web " + web.Url);
            }
            if (alert.User.LoginName.ToUpper().Equals(user.LoginName.ToUpper()))
            {
                authData.IsAllowed = true;
            }
        }
        else
        {
            SPList list = web.GetListFromUrl(authData.Container.Url);
            if (authData.Type == AuthData.EntityType.LIST)
            {
                bool isAllowed = list.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                authData.IsAllowed = isAllowed;
            }
            else if (authData.Type == AuthData.EntityType.LISTITEM)
            {
                int itemId = int.Parse(authData.ItemId);
                SPListItem item = list.GetItemById(itemId);
                bool isAllowed = item.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                authData.IsAllowed = isAllowed;
            }
        }
    }

    /// <summary>
    /// Exception is not serializable and cannot be sent on wire. This method provides a convenient way to get the entire
    /// exception message by looking recursively into the cause of the exception until the root cause is found.
    /// </summary>
    /// <param name="e"></param>
    /// <returns></returns>
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
/// Authorization of an item requires knowledge of its container. This class represents the container of an item that is to be authorized
/// </summary>
[WebService(Namespace = "BulkAuthorization.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class Container
{
    public enum ContainerType
    {
        NA, SITE_COLLECTION, SITE, LIST
    }

    ContainerType type = ContainerType.NA;
    string url;

    public string Url
    {
        get { return url; }
        set { url = value; }
    }

    public ContainerType Type
    {
        get { return type; }
        set { type = value; }
    }

    public override bool Equals(Object obj)
    {
        Container inContainer = null; ;
        if (obj is Container)
        {
            inContainer = (Container)obj;
        }

        if (null == this.Url || null == inContainer.Url || this.Type == ContainerType.NA || inContainer.Type == ContainerType.NA)
        {
            return false;
        }
        return this.Type == inContainer.Type && this.Url.Equals(inContainer.Url);
    }

    public override int GetHashCode()
    {
        int hashCode = base.GetHashCode();
        if (this.Type == ContainerType.NA)
        {
            hashCode *= new Random().Next(13, 23);
        }
        return hashCode;
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

    private Container container;
    public Container Container
    {
        get { return container; }
        set { container = value; }
    }

    // any message for diagnosis purpose
    private string message;
    public string Message
    {
        get { return message; }
        set { message = value; }
    }

    // Whether the authorization of this packet was completed. If false, it will mean no AuthData in this packet was authorized
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
    private Container container;
    public Container Container
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

    // authz status
    private bool isAllowed;
    public bool IsAllowed
    {
        get { return isAllowed; }
        set { isAllowed = value; }
    }

    // any message for diagnosis purpose
    private string message;
    public string Message
    {
        get { return message; }
        set { message = value; }
    }

    // The actual DocId as received from GSA
    private string complexDocId;
    public string ComplexDocId
    {
        get { return complexDocId; }
        set { complexDocId = value; }
    }

    // Whether the authorization of this data was completed successfully.
    private bool isDone;
    public bool IsDone
    {
        get { return isDone; }
        set { isDone = value; }
    }

    public enum EntityType
    {
        LISTITEM, LIST, ALERT
    }
    private EntityType type;
    public EntityType Type
    {
        get { return type; }
        set { type = value; }
    }
}

/// <summary>
/// Creates a context for authZ. An object of this class can provide all the necessary information required for authorization.
/// Examples include SPUser for a user, SPWeb for a URL etc. To serve in a better and performant way, it remembers the context
/// in which authorization is being done. Also, this class knows about the scope of usage for every type of objects; for example,
/// SPUser objects are same across every sites hence it make sense to have a single SPUser for a given username. Since, one WS call
/// is made for only one user's authorization, the SPUser object is created only once.
/// </summary>
public class WSContext
{
    private readonly UserInfoHolder userInfoHolder;
    private SiteInfoHolder siteInfoHolder;

    internal WSContext(SPContext spContext, string username, bool mustIdentifyUser)
    {
        if (null == spContext)
        {
            throw new Exception("Unable to get SharePoint context. The web service endpoint might not be referring to an active SharePoitn site. ");
        }
        SPSite site = spContext.Site;
        if (null == site)
        {
            throw new Exception("Site Colllection not found!");
        }
        siteInfoHolder = new SiteInfoHolder(site, false);
        userInfoHolder = new UserInfoHolder(username);
        try
        {
            userInfoHolder.TryInit(site);
        }
        catch (Exception e)
        {
            if (mustIdentifyUser)
            {
                throw e;
            }
        }
    }

    ~WSContext()
    {
        siteInfoHolder.Dispose();
    }

    internal void Using(AuthDataPacket authDataPacket)
    {
        if (authDataPacket.Container.Type == Container.ContainerType.NA)
        {
            siteInfoHolder.Container.Type = Container.ContainerType.NA;
        }
        else
        {
            Using(authDataPacket.Container);
        }
    }

    /// <summary>
    /// Inform WSContext about the context in which the authorization is being done. This, typically is the site collection
    /// </summary>
    /// <param name="container"></param>
    private void Using(Container container)
    {
        if (container.Equals(siteInfoHolder.Container))
        {
            return;
        }
        if (null != container.Url && !container.Url.Equals(siteInfoHolder.Container.Url))
        {
            siteInfoHolder.Dispose();
            siteInfoHolder = new SiteInfoHolder(container);
        }
        userInfoHolder.TryInit(siteInfoHolder.Site);
    }

    /// <summary>
    /// If WSContext is aware of a site collection or site context, it tries to serve SPWeb from there only. Otherwise it makes
    /// the passed in AuthData's URL's context as current authZ context and retries to find SPWeb. The latter however, will happen
    /// only if no explicit context information was given to WSContext prior to authorization
    /// </summary>
    /// <param name="authData"></param>
    /// <returns></returns>
    internal SPWeb GetWeb(AuthData authData)
    {
        if (authData.Container.Type == Container.ContainerType.NA)
        {
            throw new Exception("Container information is not available. A list or site is expected. ");
        }
        try
        {
            return siteInfoHolder.GetWeb(authData.Container);
        }
        catch (Exception e)
        {
            if (siteInfoHolder.Container.Type == Container.ContainerType.NA)
            {
                Using(authData.Container);
                return siteInfoHolder.GetWeb(authData.Container);
            }
            throw e;
        }
    }

    /// <summary>
    /// Gets SPUser if one available. Otherwise, throws an exception.
    /// </summary>
    /// <returns></returns>
    internal SPUser GetUser()
    {
        if (null == userInfoHolder.User)
        {
            userInfoHolder.throwException();
        }
        return userInfoHolder.User;
    }
}

/// <summary>
/// Stores site related information
/// </summary>
internal class SiteInfoHolder
{
    private readonly SPSite site;
    private readonly Container container;
    private readonly bool isDisposable = true;

    internal SPSite Site
    {
        get { return site; }
    }

    internal Container Container
    {
        get { return container; }
    }

    internal SiteInfoHolder(SPSite site, bool isDisposable)
    {
        this.site = site;
        this.isDisposable = isDisposable;
        this.container = new Container();
        this.container.Url = site.Url;
        this.container.Type = Container.ContainerType.SITE_COLLECTION;
    }

    internal SiteInfoHolder(Container container)
    {
        SPSite site = null;
        SPSecurity.RunWithElevatedPrivileges(delegate()
        {
            // try creating the SPSite object for the incoming URL. If fails, try again by changing the URL format FQDN to Non-FQDN or vice-versa.
            try
            {
                site = new SPSite(container.Url);
            }
            catch (Exception e)
            {
                site = new SPSite(SwitchURLFormat(container.Url));
            }
        });

        if (null == site)
        {
            throw new Exception("Site Colllection not found!");
        }
        this.site = site;
        this.container = container;
    }

    ~SiteInfoHolder()
    {
        Dispose();
    }

    internal void Dispose()
    {
        if (isDisposable)
        {
            this.site.Dispose();
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

    /// <summary>
    /// Returns a SPWeb to work with. This SPWeb is the one in which the passed in container has been created.
    /// </summary>
    /// <param name="container"></param>
    /// <returns></returns>
    internal SPWeb GetWeb(Container container)
    {
        Uri uri = new Uri(container.Url);
        string relativeWebUrl = null;
        if (container.Type == Container.ContainerType.LIST)
        {
            string[] segments = uri.Segments;
            StringBuilder urlBuilder = new StringBuilder(segments[0]);
            for (int i = 1; i < segments.Length - 3; ++i)
            {
                urlBuilder.Append(segments[i]);
            }
            relativeWebUrl = urlBuilder.ToString();
        }
        else
        {
            relativeWebUrl = uri.AbsolutePath;
        }

        try
        {
            return site.OpenWeb(relativeWebUrl);
        }
        catch (Exception e)
        {
            throw new Exception("Could not get SPWeb for url [ " + container.Url + " ], server relative URL [ " + relativeWebUrl + " ]", e);
        }
    }
}

/// <summary>
/// Stores user related information
/// </summary>
internal class UserInfoHolder
{
    string msg = "";
    private string username;
    private SPUser user;
    private bool isResolved;

    internal SPUser User
    {
        get { return user; }
    }

    internal UserInfoHolder(string username)
    {
        this.username = username;
    }

    /// <summary>
    /// If the SPuser object is not yet constructed, try to get it using the passed in SPSite
    /// </summary>
    /// <param name="site"></param>
    internal void TryInit(SPSite site)
    {
        if (null != user)
        {
            return;
        }
        if (!isResolved)
        {
            SPPrincipalInfo userInfo = SPUtility.ResolveWindowsPrincipal(site.WebApplication, username, SPPrincipalType.All, false);
            if (null != userInfo)
            {
                username = userInfo.LoginName;
                isResolved = true;
            }
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
                    msg = "User " + username + " information not found in the parent site collection of web " + web.Url;
                    throwException();
                }
            }
        }
    }

    internal void throwException()
    {
        if (!isResolved)
        {
            throw new Exception(msg, new Exception("User " + username + " can not be resolved into a valid SharePoint user."));
        }
        else
        {
            throw new Exception(msg);
        }
    }
}
