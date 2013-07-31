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
using System.Reflection;
using System.Text;
using System.Web.Services;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Utilities;
using Microsoft.SharePoint.Administration;
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
        // To force connector to use authentication in case anonymous acess is enabled
        SPContext.Current.Web.ToString();
        // All the pre-requisites for running this web service should be checked here.
        SPSecurity.RunWithElevatedPrivileges(delegate()
        {
        });
        return "success";
    }
    /// <summary>
    /// returns the version of the GSS installed on SharePoint Server.
    /// </summary>
    [WebMethod]
    public string GetGSSVersion()
    {
        // To force connector to use authentication in case anonymous acess is enabled
        SPContext.Current.Web.ToString();
        return "3.1.4";
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
        // To force connector to use authentication in case anonymous acess is enabled
        SPContext.Current.Web.ToString();
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
                String webUrl = String.Empty;
                try
                {                  
                    using(SPWeb web = wsContext.OpenWeb(authData.Container, authDataPacket.Container.Type == global::Container.ContainerType.NA))
                    {
                        if (web != null && web.Exists)
                        {
                            SPUser user = wsContext.User;
                            webUrl = web.Url;
                            Authorize(authData, web, user);
                        }
                        else
                        {
                            throw new Exception("Error creating Web for URL "+ authData.Container.Url);
                        }
                    };
                }
                catch (Exception e)
                {
                    authData.Message = "Authorization failure! " + GetFullMessage(e);
                    authData.Message += String.Format("\nauthData.Type {0}, authData.Container {1}, webUrl {2}", authData.Type, authData.Container.Url, webUrl);
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
        else if (authData.Type == AuthData.EntityType.SITE)
        {
            bool isAllowd = web.DoesUserHavePermissions(user.LoginName,SPBasePermissions.ViewPages);
            authData.IsAllowed = isAllowd;
        }
        else
        {
            try
            {
                SPList list = null;
                try
                {
                    list = web.GetListFromUrl(url);
                }
                catch (Exception ex)
                {
                    //ignoring exception for possible folder URL
                    ex = null;
                    try
                    {
                        SPFolder oFolder = web.GetFolder(url);
                        if (oFolder != null && oFolder.Item != null)
                        {
                            list = oFolder.Item.ParentList;
                        }
                    }
                    catch (Exception exFolder)
                    {
                        // ignoring exception.
                        exFolder = null;
                    }
                }
                if (authData.Type == AuthData.EntityType.LIST)
                {
                    bool isAllowed = list.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                    authData.IsAllowed = isAllowed;
                }
                else if (authData.Type == AuthData.EntityType.LISTITEM)
                {
                    int itemId = 0;
                    bool bItemFound = false;
                    SPListItem item = null;
                    if (int.TryParse(authData.ItemId, out itemId))
                    {
                        if (list != null)
                        {
                            item = list.GetItemById(itemId);
                            if (item != null)
                            {                             
                                if (list.ReadSecurity == 2)
                                {
                                    authData.IsAllowed = VerifyReadSecurity(item, user);
                                }
                                else
                                {
                                    authData.IsAllowed = item.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                                }
                              
                                bItemFound = true;
                            }
                        }
                    }

                    /*
                     * Authorization for documents existing under a document library and folder
                     * For eg: document url is of the form "http://SharePointWebApp:portNo/site/doclib/myDoc.txt". Hence
                     * the code below will perform authorization for documents under a document library or folder.
                     */
                    if (!bItemFound)
                    {                        
                        item = web.GetListItem(url);
                        if (item != null)
                        {                           
                            if (item.ParentList.ReadSecurity == 2)
                            {
                                authData.IsAllowed = VerifyReadSecurity(item, user);
                            }
                            else
                            {
                                authData.IsAllowed = item.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                            }
                        }                       
                    }
                }
            }
            catch (Exception e)
            {
                throw new Exception("Error Authorizing Url: " + url, e);
            }
        }
    }

    private Boolean VerifyReadSecurity(SPListItem item, SPUser user)
    {
        Boolean bUserHasManageListPermissions = item.DoesUserHavePermissions(user, SPBasePermissions.ManageLists);
        if (bUserHasManageListPermissions)
        {
            return true;
        }
        else
        {           
            try
            {
                SPUser owner = GetOwner(item);
                if (owner != null && user.ID == owner.ID)
                {
                    return item.DoesUserHavePermissions(user, SPBasePermissions.ViewListItems);
                }
                return false;

            }
            catch (Exception exOwner)
            {
                // Since Exception in fetching Owner, returing false;
               return false;
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
            message.AppendLine(e.StackTrace);
            e = e.InnerException;
        }
        return message.ToString();
    }

    /// <summary>
    /// Retrieves the Owner's information about a given ISecurable entity
    /// </summary>
    /// <param name="secobj"></param>
    /// <returns></returns>
    private SPUser GetOwner(ISecurableObject secobj)
    {
        SPUser owner = null;
        if (secobj is SPList)
        {
            owner = ((SPList)secobj).Author;
        }
        else if (secobj is SPListItem)
        {
            SPListItem item = (SPListItem)secobj;
            SPFile file = item.File;
            if (null != file)
            {
                // Case of Document Library
                owner = file.Author;
            }
            else
            {
                // Case of other generic lists
                String key = "Created By";
                SPFieldUser field = item.Fields[key] as SPFieldUser;
                if (field != null)
                {
                    SPFieldUserValue fieldValue = field.GetFieldValue(item[key].ToString()) as SPFieldUserValue;
                    if (fieldValue != null)
                    {
                        owner = fieldValue.User;
                    }
                }
            }
        }
        else if (secobj is SPWeb)
        {
            owner = ((SPWeb)secobj).Author;
        }
        else
        {
            throw new Exception("Uncompatible entity type. A listitem, list or a web is expected. ");
        }
        return owner;
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
    private SPWeb web;

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
        if (this.web != null)
        {
            try
            {
                web.Dispose();
            }
            catch (Exception ex)
            {
                // TODO Avoid this exception
                ex = null;
            }
        }
        
        if (this.site != null)
        {
            try
            {
                site.Dispose();
            }
            catch (Exception ex)
            {
                // TODO Avoid this exception
                ex = null;
            }
        }
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
        if (this.web != null)
        {
            this.web.Dispose();
        }
        if (this.site != null)
        {
            this.site.Dispose();
        }
        this.site = site;
        // Reinitialize SPWeb Object since
        // parent SPSite Object is reinitialized.
        this.web = site.OpenWeb();
        userInfoHolder.TryInit(this.site, this.web);
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
        /* 
        * Checking whether the url contains port number. This can be recognized by getting the index of 
        * the occurrence of colon (i.e. ":" )within the url.
        */
        int colonPosListUrl = listUrl.IndexOf(':');
        if (colonPosListUrl >= 0)// Means URL contains Port No.
        {
            listUrl = listUrl.Substring(listUrl.IndexOf(':') + 1);
        }
        else              // Means URL does not contain Port No.
        {
            listUrl = listUrl.Substring(listUrl.IndexOf('/') + 1);
            listUrl = listUrl.Substring(listUrl.IndexOf('/') + 1);
        }
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

                // Get the folder URL
                string folderURL = container.Url.Substring(0, container.Url.LastIndexOf("/"));
                try
                {
                    // Get the folder object corresponding to the URL
                    SPFolder folder = site.OpenWeb().GetFolder(folderURL);
                    
                    // Get the server relative URL for the folder
                    string serverRelativeURLFolder = folder.ServerRelativeUrl;

                    /*
                     * If there is a folder hierarchy, format the string to get the correct server relative 
                     * url for document existing under subfolders. For eg: if the document url is of the form 
                     * "http://SharePointWebApp:portNo/site/doclib/folder1/folder2/folder3/myDoc.txt", then for 
                     * extracting correct server relative url, the corresponding folders existing along the path
                     * (i.e. folder1,folder2 and folder3 in our case) need to be removed from the document url.
                     */
                    while (folder.ParentFolder.Url != "")
                    {
                        /* 
                         * Using a while loop facilitates the job of removing the folders, along the path, existing 
                         * in the document url.
                         */
                        listUrl = listUrl.Substring(0, listUrl.LastIndexOf('/'));
                        /*
                         * Set the parent folder object as the current folder object to be processed. 
                         * This is due to the fact that the folder hierarchy needs to be traversed upwards, starting
                         * from the document name, in the document url, and is done to get correct server relative URL.
                         */
                        folder = folder.ParentFolder; 
                    }
                }
                catch (Exception e)
                {
                }
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
    ///  Reconstruct SPUser object everytime SPSite for WSContext is reinitialized.  
    /// </summary>
    /// <param name="site"></param>
    internal void TryInit(SPSite site, SPWeb web)
    {
        try
        {
            // Try with available username
            username = GssAuthzUtility.GetUserNameWithDomain(username);
            user = GssAuthzUtility.GetUserFromWeb(username, web, site.WebApplication);
            if (user != null)
            {
                isResolved = true;
            }

            if (!isResolved)
            {
                SPPrincipalInfo userInfo = GssAuthzUtility.ResolveWindowsPrincipal(username);
                if (null != userInfo)
                {
                    // Mark isResolved = true since userInfo object is not null.                
                    isResolved = true;
                
                    // SPUtility.ResolveWindowsPrincipal will
                    // resolve input username with login name for user. In most of the cases
                    // both values will be same.
                    // Try with resolved username only if input username is not same as resolved username.
                    if (String.Compare(username, userInfo.LoginName, true) != 0)
                    {
                        username = userInfo.LoginName;
                        user = GssAuthzUtility.GetUserFromWeb(username, web, site.WebApplication);                        
                    }
                }
            }
        }
        catch (Exception exUser)
        {
            throw new Exception(String.Format("Error initializing User [{0}] for web [{1}] due to Exception {2}", username, web.Url, exUser.Message), exUser);
        }

        if (user == null)
        {
            // Throwing user not found exception since SPUser with resolved username is not found
            msg = "User " + username + " information not found in the parent site collection of web " + web.Url;
            throwException();
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

/// <summary>
/// Provides general purpose utility methods.
/// This class must be stateless because it is a member instance of the web service
/// </summary>
public sealed class GssAuthzUtility
{

    private static Dictionary<string, string> domainMapping = new Dictionary<string, string>();
    private static Dictionary<Guid, Boolean> webAppClaimsSetting = new Dictionary<Guid, bool>();
    private static Dictionary<String, Type> reflectionTypes = new Dictionary<String, Type>();
    private static Dictionary<String, MethodInfo> reflectionMethods = new Dictionary<String, MethodInfo>();

    private GssAuthzUtility()
    {
        throw new Exception("Operation not allowed! ");
    }

    public static SPUser GetUserFromWeb(String userName, SPWeb web, SPWebApplication webApp)
    {
        Boolean isClaimsApplicable = IsClaimsAuthenticationApplicable(webApp);
        if (isClaimsApplicable)
        {
            userName = GetClaimsUserName(userName);
          
        }
        SPUser userToReturn = FindUserInWeb(userName, web);        
        if (userToReturn == null)
        {
           
            try
            {
                // If user with username is not available in AllUsers or SiteUsers then call web.DoesUserHavePermissions
                // This will create SPUser object for username and ensure user is available in AllUsers for SPWeb object.
                // Users will become part of AllUsers as well as SiteUsers collection when first time user access / login to
                // SharePoint site.
                // Since web.DoesUserHavePermissions is expensive call compare to checking username in usercollections,
                // web.DoesUserHavePermissions is used as a fallback mechanism.
                web.DoesUserHavePermissions(userName, SPBasePermissions.ViewPages | SPBasePermissions.ViewListItems);
                userToReturn = FindUserInWeb(userName, web);              
            }
            catch (Exception exUserPerm)
            {
                // ignore this exception
                exUserPerm = null;
            }                     
        }
        return userToReturn;
        
    }

    private static SPUser FindUserInWeb(String userName, SPWeb web)
    {
        SPUser userToReturn = null;       

        // With available username search user in web.AllUsers and web.SiteUsers
        // Not checking web.Users since web.Users is a subset to web.AllUsers as well as web.SiteUsers 
        try
        {
            userToReturn = web.AllUsers[userName];
        }
        catch (Exception e1)
        {
            try
            {
                userToReturn = web.SiteUsers[userName];
            }
            catch (Exception e2)
            {
                
            }
        }
        return userToReturn;
    }

    public static SPPrincipalInfo ResolveWindowsPrincipal(String userName)
    {
        
        SPPrincipalInfo userInfo = SPUtility.ResolveWindowsPrincipal(SPContext.Current.Site.WebApplication, userName, SPPrincipalType.User, false);
        if (userInfo != null)
        {
            String[] userNameResolved = userInfo.LoginName.Split(new String[] {"\\"}, StringSplitOptions.RemoveEmptyEntries);
            String[] userNameSplit = userName.Split(new String[] { "\\" }, StringSplitOptions.RemoveEmptyEntries);
            if (userNameSplit.Length == 2 && userNameResolved.Length == 2)
            {
                // Add to cache only if resolved domain is diffrent than input domain.
                if (String.Compare(userNameSplit[0], userNameResolved[0], true) != 0)
                {
                    domainMapping[userNameSplit[0].ToLower()] = userNameResolved[0];
                }
            }
        }
        return userInfo;
    }

    public static String GetUserNameWithDomain(String userName)
    {
        // Considering only "\\" since connector is always using username format as domain\\username 
        String[] userNameSplit = userName.Split(new String[] { "\\" }, StringSplitOptions.RemoveEmptyEntries);
        if (userNameSplit.Length == 2 && domainMapping.ContainsKey(userNameSplit[0].ToLower()))
        {
            String userDomain = domainMapping[userNameSplit[0].ToLower()];
            return userDomain + "\\" + userNameSplit[1];
        }
        else
        {
            return userName;
        }
    }

    public static String GetClaimsUserName(String userName)
    {
        Type managerType = GetTypeForObject(Assembly.GetAssembly(typeof(SPUser)), "Microsoft.SharePoint.Administration.Claims.SPClaimProviderManager");
        if (managerType == null)
        {
            return userName;
        }

        Object manager = managerType.GetProperty("Local").GetValue(null, null);
        if (manager == null)
        {
            return userName;
        }

        Type spIdentifierTypesEnumType = GetTypeForObject(Assembly.GetAssembly(typeof(SPUser)), "Microsoft.SharePoint.Administration.Claims.SPIdentifierTypes");
        if (spIdentifierTypesEnumType == null)
        {
            return userName;
        }
        MethodInfo methodConvertIdentifierToClaim = GetMethodFromType(managerType, "ConvertIdentifierToClaim");
        if (methodConvertIdentifierToClaim == null)
        {
            return null;
        }
        Object claim = methodConvertIdentifierToClaim.Invoke(manager, new object[] { userName, Enum.Parse(spIdentifierTypesEnumType, "WindowsSamAccountName") });
        if (claim == null)
        {
            return userName;
        }
        MethodInfo methodEncodeClaim = GetMethodFromType(managerType, "EncodeClaim");
        if (methodEncodeClaim == null)
        {
            return null;
        }
        Object encodedClaim = methodEncodeClaim.Invoke(manager, new object[] { claim });
        return encodedClaim.ToString();
    }
       
    

    private static Boolean IsClaimsAuthenticationApplicable(SPWebApplication webApp)
    {
        if (webAppClaimsSetting.ContainsKey(webApp.Id))
        {
            return webAppClaimsSetting[webApp.Id];
        }
        
        Type spWebAppType = webApp.GetType();
        PropertyInfo useClaimsAuthentication = spWebAppType.GetProperty("UseClaimsAuthentication");
        Boolean bClaimsApplicable = useClaimsAuthentication != null ? (bool)useClaimsAuthentication.GetValue(webApp, null) : false;
        webAppClaimsSetting[webApp.Id] = bClaimsApplicable;
        return bClaimsApplicable;
    }

    private static Type GetTypeForObject(Assembly assemblyToLookUp, String classPath)
    {
        if (reflectionTypes.ContainsKey(classPath))
        {
            return reflectionTypes[classPath];
        }

        Type typeToReturn = assemblyToLookUp.GetType(classPath);
        reflectionTypes[classPath] = typeToReturn;
        return typeToReturn;           
        
    }

    private static MethodInfo GetMethodFromType(Type fromType, String methodName)
    {
        String key = String.Format("{0}.{1}", fromType.FullName, methodName);
        if (reflectionMethods.ContainsKey(key))
        {
            return reflectionMethods[key];
        }

        MethodInfo methodToReturn = fromType.GetMethod(methodName);
        reflectionMethods[key] = methodToReturn;
        return methodToReturn;
    }
}
