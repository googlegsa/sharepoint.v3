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
        SPSecurity.RunWithElevatedPrivileges(delegate()
        {
        });
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
        WSContext wsContext = new WSContext(username);
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

            DateTime authDataPacketStartTime = System.DateTime.Now;
            if (authDataPacket.Container.Type != global::Container.ContainerType.NA)
            {
                try
                {
                    wsContext.Using(authDataPacket.Container.Url);
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

                DateTime authDataStartTime = System.DateTime.Now;
                try
                {
                    SPWeb web = wsContext.OpenWeb(authData.Container, authDataPacket.Container.Type == global::Container.ContainerType.NA);
                    SPUser user = wsContext.User;
                    Authorize(authData, web, user);
                }
                catch (Exception e)
                {
                    authData.Message = "Authorization failure! " + GetFullMessage(e);
                    authData.Message += " \nAuthorization of this document took " + System.DateTime.Now.Subtract(authDataStartTime).TotalSeconds + " seconds";
                    continue;
                }
                authData.IsDone = true;
                authData.Message += "\nAuthorization of this document completed successfully in " + System.DateTime.Now.Subtract(authDataStartTime).TotalSeconds + " seconds";
            }
            authDataPacket.IsDone = true;
            if (authDataPacket.Container.Type == global::Container.ContainerType.NA)
            {
                authDataPacket.Message += " Authorization of " + authDataArray.Length + " documents completed in " + System.DateTime.Now.Subtract(authDataPacketStartTime).TotalSeconds + " seconds";
            }
            else
            {
                authDataPacket.Message += " Authorization of " + authDataArray.Length + " documents from site collection " + authDataPacket.Container.Url + " completed in " + System.DateTime.Now.Subtract(authDataPacketStartTime).TotalSeconds + " seconds";
            }
        }
    }

    /// <summary>
    /// Actual Authorization is done here
    /// </summary>
    /// <param name="authData">the data for which the authorization is done</param>
    /// <param name="wsContext">serves specific details like SPUser and SPWeb required for authZ</param>
    private void Authorize(AuthData authData, SPWeb web, SPUser user)
    {
        String url = authData.Container.Url;
        if (authData.Type == AuthData.EntityType.ALERT)
        {
            Guid alert_guid = new Guid(authData.ItemId);
            SPAlert alert = web.Alerts[alert_guid];
            if (null == alert)
            {
                throw new Exception("Alert not found. alert_guid [ " + alert_guid + " ], web [ " + web.Url + " ]");
            }
            if (alert.User.LoginName.ToUpper().Equals(user.LoginName.ToUpper()))
            {
                authData.IsAllowed = true;
            }
        }
        else if (authData.Type == AuthData.EntityType.SITE) {
            bool isAllowd = web.DoesUserHavePermissions(SPBasePermissions.ViewPages);
            authData.IsAllowed = isAllowd;
        } else {
            SPList list = web.GetListFromUrl(url);
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
        LISTITEM, LIST, ALERT, SITE
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
/// in which authorization is being done. Knowing the context, SPWeb kind of objects can be served in less time. Also, this class
/// knows about the scope of usage for every type of objects; for example, SPUser objects are same across every sites hence it make
/// sense to have a single SPUser for a given username. Since, one WS call is made for only one user's authorization, the SPUser object
/// is created only once.
/// </summary>
public class WSContext
{
    private readonly UserInfoHolder userInfoHolder;
    private SPSite site;

    internal SPUser User
    {
        get { return userInfoHolder.User; }
    }

    /// <summary>
    /// WSContext objects needs to know about the user who is being authorized. And, there can be only one such user
    /// becasue one web service call authorizes only one user
    /// </summary>
    /// <param name="username"></param>
    internal WSContext(string username)
    {
        userInfoHolder = new UserInfoHolder(username);
    }

    ~WSContext()
    {
        site.Dispose();
    }

    /// <summary>
    /// Reinitialize SPSite using the passed in url. All subsequest requests will be served using this SPSite
    /// </summary>
    /// <param name="url"></param>
    internal void Using(string url)
    {
        SPSite site = null;
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
        if (null != this.site)
        {
            this.site.Dispose();
        }
        this.site = site;
        userInfoHolder.TryInit(this.site);
    }

    /// <summary>
    /// FQDN - non-FQDN conversion
    /// </summary>
    /// <param name="siteURL"></param>
    /// <returns></returns>
    string SwitchURLFormat(string siteURL)
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
    /// Using the SPSite member, returns SPWeb that hosts the passed in container. If no SPWeb is found and retryUsingCurrentContainerUrl
    /// is true, reinitializes SPSite with the container's URL and then retries.
    /// </summary>
    /// <param name="authData"></param>
    /// <returns></returns>
    internal SPWeb OpenWeb(Container container, bool usingCurrentContainerUrl)
    {
        if (null == site || usingCurrentContainerUrl)
        {
            Using(container.Url);
        }

        SPWeb web = null;
        string relativeWebUrl = GetServerRelativeWebUrl(container);
        try
        {
            web = site.OpenWeb(relativeWebUrl);
        }
        catch (Exception e)
        {
            throw new Exception("Could not get SPWeb for url [ " + container.Url + " ], server relative URL [ " + relativeWebUrl + " ]", e);
        }

        if (null == web || !web.Exists)
        {
            throw new Exception("Could not get SPWeb for url [ " + container.Url + " ], server relative URL [ " + relativeWebUrl + " ]");
        }
        else
        {
            return web;
        }
    }

    /// <summary>
    /// Returns server relative URL of a list or site
    /// </summary>
    /// <param name="container"></param>
    /// <returns></returns>
    // FIXME For some lists of site directory, this logic does not work because the URL formats of those lists are different from what the logic assumes.
    string GetServerRelativeWebUrl(Container container)
    {
        String listUrl = container.Url;
        //If the container type is SITE return only site name.
        if (container.Type == Container.ContainerType.SITE)
        {
            String siteUrl = container.Url;
            /*
             * Retrieves proper server relative URL for a site. This logic works for both
             * - i.e. site collection created using "/" or "/sites/" path. Also works for 
             * sites created using the HTTP port 80. This works correctly for following url patterns -
             
                1 - URL of the default.aspx page for a site, whose site collection is 
                created using the '/sites/' path. 
                http://SharePointURL:portno/sites/site1/default.aspx
                http://SharePointURL/sites/site1/default.aspx

                2 - URL of site using "Social Meeting Workspace" template and the 
                sitecollection for the site is created using the "/sites/" path
                http://SharePointURL:portno/sites/site1/socialsite/default.aspx
                http://SharePointURL/sites/site1/socialsite/default.aspx

                3 - URL of site using "Blank" template and the sitecollection for the
                site is created using the "/" path
                http://SharePointURL:portno/default.aspx
                http://SharePointURL/default.aspx
               
             */
            siteUrl = siteUrl.Substring(siteUrl.IndexOf(':') + 1);
            /* 
             * Checking whether the url contains port number, by testing whether the url contains another instance of
             * colon i.e. ":".
             */
            int colonPos = siteUrl.IndexOf(':');
            if (colonPos >= 0)// Means URL contains Port No.
            {
                siteUrl = siteUrl.Substring(siteUrl.IndexOf(':') + 1);
            }
            else              // Means URL does not contain Port No.
            {
                siteUrl = siteUrl.Substring(siteUrl.IndexOf('/') + 1);
                siteUrl = siteUrl.Substring(siteUrl.IndexOf('/') + 1);
            }
            siteUrl = siteUrl.Substring(siteUrl.IndexOf('/'));
            siteUrl = siteUrl.Substring(0, siteUrl.LastIndexOf('/'));
            return siteUrl;
        }

        listUrl = listUrl.Substring(listUrl.IndexOf(':') + 1);
        listUrl = listUrl.Substring(listUrl.IndexOf(':') + 1);
        listUrl = listUrl.Substring(listUrl.IndexOf('/'));
        if (container.Type == Container.ContainerType.LIST)
        {
            bool isTrimmed = false;
            int formsPos = listUrl.LastIndexOf("/forms/", listUrl.Length, StringComparison.InvariantCultureIgnoreCase);
            if (formsPos >= 0)
            {
                listUrl = listUrl.Substring(0, listUrl.LastIndexOf('/', formsPos));
                listUrl = listUrl.Substring(0, listUrl.LastIndexOf('/'));
                isTrimmed = true;

                /*
                 * For blog site template, picture library list URL (i.e. URL for "Photos" List)
                 * contains both "lists" and "forms" string in the url. Hence removing the "lists"
                 * from the url, so as to get the correct server relative URL for the picture library. 
                 */ 
                int listPos1 = listUrl.IndexOf("/lists", 0, StringComparison.InvariantCultureIgnoreCase);
                if (listPos1 >= 0)
                {
                    listUrl = listUrl.Substring(0, listUrl.LastIndexOf('/'));
                    isTrimmed = true;
                }
            }

            if (!isTrimmed)
            {
                int listPos = listUrl.LastIndexOf("/lists/", listUrl.Length, StringComparison.InvariantCultureIgnoreCase);
                if (listPos >= 0)
                {
                    listUrl = listUrl.Substring(0, listUrl.LastIndexOf('/', listPos));
                    isTrimmed = true;
                }
            }

            if (!isTrimmed)
            {
                listUrl = listUrl.Substring(0, listUrl.LastIndexOf('/'));
                listUrl = listUrl.Substring(0, listUrl.LastIndexOf('/'));
            }
        }
        return listUrl;
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
        get
        {
            if (null == user)
            {
                throwException();
            }
            return user;
        }
    }

    internal UserInfoHolder(string username)
    {
        this.username = username;
    }

    /// <summary>
    /// If the SPuser object is not yet constructed, try to get it using the passed-in SPSite
    /// </summary>
    /// <param name="site"></param>
    internal void TryInit(SPSite site)
    {
        if (null != user)
        {
            return;
        }

        SPWeb web = null;
        try
        {
            web = site.OpenWeb();
        }
        catch (Exception e)
        {
            throw new Exception("Could not create SPUser for user [ " + username + " ] using site collection [ " + site.Url + " ]", e);
        }
        if (null == web || !web.Exists)
        {
            throw new Exception("Could not create SPUser for user [ " + username + " ] using site collection [ " + site.Url + " ] because root web is not existing.");
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