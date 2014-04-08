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

<%@ WebService Language="C#" Class="GssAclMonitor" %>
using System;
using System.Net;
using System.Text;
using System.Xml;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Web;
using System.Web.Services;
using Microsoft.SharePoint;
using Microsoft.SharePoint.Administration;
using Microsoft.SharePoint.Utilities;

/// <summary>
/// Represents a user/group which is used in the <see cref="GssAce"/>
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssPrincipal
{
    // Site Collection specific ID. This is very useful to track such users/groups which have been
    // deleted. A -1 value (GssAclMonitor.GSSITEADMINGROUPID) is used for the hypothetical site
    // collection group and anything smaller than -1 value is considered to be unknown
    private int id;

    // Name of the prinicpal
    private string name;

    public enum PrincipalType
    {
        USER, DOMAINGROUP, SPGROUP, NA
    }

    private PrincipalType type;

    /// <summary>
    /// Represents the member users If the current principal is a group
    /// </summary>
    private List<GssPrincipal> members;
    private StringBuilder logMessage = new StringBuilder();

    public int ID
    {
        get { return id; }
        set { id = value; }
    }
    public string Name
    {
        get { return name; }
        set { name = value; }
    }
    public PrincipalType Type
    {
        get { return type; }
        set { type = value; }
    }
    public List<GssPrincipal> Members
    {
        get { return members; }
        set { members = value; }
    }
    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    // A web service always require a default constructor. But, we do not want to use it intentionally
    private GssPrincipal() { }

    public GssPrincipal(string name, int id)
    {
        ID = id;
        Name = GssAclUtility.DecodeIdentity(name);
        Members = new List<GssPrincipal>();
    }

    public override bool Equals(object obj)
    {
        if (obj is GssPrincipal)
        {
            GssPrincipal principal = (GssPrincipal)obj;
            if (null == this.name || null == principal || null == principal.name)
            {
                return false;
            }

            if (principal.ID == this.ID && principal.Name.Equals(this.Name) && principal.Type == this.Type)
            {
                return true;
            }
        }
        return false;
    }

    public override int GetHashCode()
    {
        return 13 * (this.Name.GetHashCode() + this.Type.GetHashCode() + ID);
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// An object of this class represents the actual SharePoint permission
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssSharepointPermission
{
    // The type being used for permissions are a list of string. SPBasePermission which is an enumeration provided by the SharePoint can also be used over here and would make better sense to have. But, it has a problem when SOAP serialization occurs.
    // The bitmask representing a set of permissions may not be serialized properly during SOAP serialization. One such case is when "Deny Write" is used in the security policy. The bitmask used in that case is 4611685812065333150.

    // List of allowed permissions
    private List<string> allowedPermissions = new List<string>();

    // List denied permission
    private List<string> deniedPermission = new List<string>();

    public List<string> AllowedPermissions
    {
        get { return allowedPermissions; }
        set { allowedPermissions = value; }
    }
    public List<string> DeniedPermission
    {
        get { return deniedPermission; }
        set { deniedPermission = value; }
    }

    /// <summary>
    /// Converts a SPBasePermission object into a set of string representing the actual permission being used
    /// </summary>
    /// <param name="spPerms"></param>
    /// <param name="applyReadSecurity">flag to indicate if read security is applicable</param>
    /// <param name="checkViewListItems">flag to indicate if user should have ViewListItems permissions. This is to avoid limited acccess users in Web service response.</param>
    /// <returns></returns>
    private List<string> GetPermissions(SPBasePermissions spPerms, Boolean applyReadSecurity, Boolean checkViewListItems)
    {
        List<string> perms = new List<string>();
        foreach (SPBasePermissions value in Enum.GetValues(typeof(SPBasePermissions)))
        {
            if (value == (value & spPerms))
            {
                perms.Add(value.ToString());
            }
        }
        // When Read Security is applicable, user should minimum have manage List permissions to view the items
        // which are not created by them.
        if ((applyReadSecurity && !perms.Contains(SPBasePermissions.ManageLists.ToString()))
            || (checkViewListItems && !perms.Contains(SPBasePermissions.ViewListItems.ToString())))
        {
            perms.Clear();
        }        
        return perms;
    }

    /// <summary>
    /// Adds new grant and deny permission(s) to the current object
    /// </summary>
    /// <param name="allowedPermissions"></param>
    /// <param name="deniedPermission"></param>
    /// <param name="applyReadSecurity"> flag to indicate if read security is applicable</param>
    public void UpdatePermission(SPBasePermissions allowedPermissions, SPBasePermissions deniedPermission, Boolean applyReadSecurity)
    {
        // Read security check will be applied only for allowed permissions
        this.allowedPermissions.AddRange(GetPermissions(allowedPermissions, applyReadSecurity, true));
        this.deniedPermission.AddRange(GetPermissions(deniedPermission, false, false));
    }
}

/// <summary>
/// An object of ths class represents an Access Control Entry in an <see cref="ACL"/>
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAce
{
    private GssPrincipal principal;
    private GssSharepointPermission permission;

    public GssPrincipal Principal
    {
        get { return principal; }
        set { principal = value; }
    }
    public GssSharepointPermission Permission
    {
        get { return permission; }
        set { permission = value; }
    }

    // A web servcei always require a default constructor. But, we do not want to use it intentionally
    private GssAce() { }

    public GssAce(GssPrincipal principal, GssSharepointPermission permission)
    {
        Principal = principal;
        Permission = permission;
    }

    public override bool Equals(object obj)
    {
        if (obj is GssAce)
        {
            GssAce ace = (GssAce)obj;
            if (null != this.Principal && null != ace && null != ace.Principal
                && this.Principal.Equals(ace.Principal) && this.Permission.Equals(ace.Permission))
            {
                return true;
            }
        }
        return false;
    }

    public override int GetHashCode()
    {
        return 17 * this.Principal.GetHashCode();
    }
}

/// <summary>
/// Represents ACL of a SharePoint entity. The represented ACL is a collection of all the effective <see cref="ACE"/>
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAcl
{
    // URL of the entity whose ACLs are being represented
    private string entityUrl;

    // Author/Owner of the document. This is an added info returned along with the ACL. This could be relevant to the clients because the owner's value as returned by the available SharePoint web services are not LDAP format.
    private string owner;

    // List of all the ACEs
    private List<GssAce> allAce;

    private StringBuilder logMessage = new StringBuilder();

    private String parentId;

    public String ParentId
    {
        get { return parentId; }
        set { parentId = value; }
    }    

    private string parentUrl;

    public string ParentUrl
    {
        get { return parentUrl; }
        set { parentUrl = value; }
    }
    // This field is typed as String instead of Boolean to ensure
    // SOAP compatiblity with earlier versions of SharePoint Connectors.
    private string inheritPermissions;

    public string InheritPermissions
    {
        get { return inheritPermissions; }
        set { inheritPermissions = value; }
    }

    // This field is typed as String instead of Boolean to ensure
    // SOAP compatiblity with earlier versions of SharePoint Connectors.
    private string largeAcl;

    public String LargeAcl
    {
        get { return largeAcl; }
        set { largeAcl = value; }
    }

    // This field is typed as String instead of Boolean to ensure
    // SOAP compatiblity with earlier versions of SharePoint Connectors.
    private string anonymousAccess;

    public String AnonymousAccess
    {
        get { return anonymousAccess; }
        set { anonymousAccess = value; }
    }
  
    public string EntityUrl
    {
        get { return entityUrl; }
        set { entityUrl = value; }
    }
    public string Owner
    {
        get { return owner; }
        set { owner = value; }
    }
    public List<GssAce> AllAce
    {
        get { return allAce; }
        set { allAce = value; }
    }
    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    // A web service always require a default constructor. But, we do not want to use it intentionally
    private GssAcl() { }

    public GssAcl(string entityUrl, int count)
    {
        this.entityUrl = entityUrl;
        this.allAce = new List<GssAce>(count);
    }

    public void AddAce(GssAce ace)
    {
        allAce.Add(ace);
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// Type of possible Object/Entity which the web service deals with
/// </summary>
public enum ObjectType
{
    NA,
    SECURITY_POLICY,
    ADMINISTRATORS,
    GROUP,
    USER,
    WEB,
    LIST,
    ITEM,
    SITE_LANDING_PAGE
}

/// <summary>
/// Represents a single ACL specific change in SharePoint
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAclChange
{
    // The object type that has changed
    private ObjectType changedObject;

    // Type of change
    private SPChangeType changeType;

    // An additional hint to identify the exact object/entity that has changed. Most of the time, this would be the ID, GUID or URL.
    private string hint;

    // A way to identify if the current change has its implication under the web site to which the request has been sent. This is useful because the web service processing is done at site collection level and all changes might not be relevant to all the web sites in the site collection.
    private bool isEffectiveIncurrentWeb;

    public ObjectType ChangedObject
    {
        get { return changedObject; }
        set { changedObject = value; }
    }

    public SPChangeType ChangeType
    {
        get { return changeType; }
        set { changeType = value; }
    }

    public string Hint
    {
        get { return hint; }
        set { hint = value; }
    }

    public bool IsEffectiveInCurrentWeb
    {
        get { return isEffectiveIncurrentWeb; }
        set { isEffectiveIncurrentWeb = value; }
    }

    // A web servcei always require a default constructor. But, we do not want to use it intentionally
    private GssAclChange() { }

    public GssAclChange(ObjectType inChangedObject, SPChangeType inChangeType, string inHint)
    {
        ChangedObject = inChangedObject;
        ChangeType = inChangeType;
        Hint = inHint;
    }
}

/// <summary>
/// Represents a list of <see cref="GssAclChnage"/> that have happened on SharePoint and provides a Change Token for synchronization purpose
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAclChangeCollection
{
    // Next change token that should be used for synchronization
    private string changeToken;
    private List<GssAclChange> changes;
    private StringBuilder logMessage = new StringBuilder();

    public string ChangeToken
    {
        get { return changeToken; }
        set { changeToken = value; }
    }

    public List<GssAclChange> Changes
    {
        get { return changes; }
        set { changes = value; }
    }

    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    // A web servcei always require a default constructor. But, we do not want to use it intentionally
    private GssAclChangeCollection() { }

    public GssAclChangeCollection(SPChangeToken changeToken)
    {
        if (null != changeToken)
        {
            ChangeToken = changeToken.ToString();
        }
        else
        {
            AddLogMessage("Invalid Token");
        }
        Changes = new List<GssAclChange>();
    }

    /// <summary>
    /// Construct an appropriate <see cref="GssAclChnage"/> object from a SharePoint's SPChange object and adds it to the list of changes
    /// </summary>
    /// <param name="change"> SharePoint's change object. This may not necessarily be a ACL related change. </param>
    /// <param name="site"> The site collection from which the change has been found. </param>
    /// <param name="web"> The web site from which the change has been found. </param>
    public void AddChange(SPChange change, SPSite site, SPWeb web)
    {
        if (change is SPChangeWeb)
        {
            switch (change.ChangeType)
            {
                case SPChangeType.AssignmentAdd:
                case SPChangeType.AssignmentDelete:
                case SPChangeType.RoleAdd:
                case SPChangeType.RoleDelete:
                case SPChangeType.RoleUpdate:
                    SPChangeWeb changeWeb = (SPChangeWeb)change;
                    // Instead of sending the ID of the web that has changed, one may consider sending the actual List IDs that should be re-crawled. This is because,
                    // connector does the actual document discovery per list level. Sending the changed webId will force the connector to make an extra call to get the Lists that should be re-crawled.
                    // But, such implementation will become confusing when the connector will evolve in future to support site collection and web application level crawling.
                    // It's better to send the change web ID as hint and let the connector decide how to work on this.
                    GssAclChange gssChange = new GssAclChange(ObjectType.WEB, changeWeb.ChangeType, changeWeb.Id.ToString());
                    gssChange.IsEffectiveInCurrentWeb = IsEffectiveForWeb(site, web, changeWeb.Id);
                    changes.Add(gssChange);
                    break;
            }
        }
        else if (change is SPChangeList)
        {
            switch (change.ChangeType)
            {
                case SPChangeType.AssignmentAdd:
                case SPChangeType.AssignmentDelete:
                case SPChangeType.RoleAdd:
                case SPChangeType.RoleDelete:
                case SPChangeType.RoleUpdate:
                    SPChangeList changeList = (SPChangeList)change;
                    GssAclChange gssChange = new GssAclChange(ObjectType.LIST, changeList.ChangeType, changeList.Id.ToString());
                    gssChange.IsEffectiveInCurrentWeb = IsEffectiveForWeb(site, web, changeList.WebId);
                    changes.Add(gssChange);
                    break;
            }
        }
        else if (change is SPChangeUser)
        {
            SPChangeUser changeUser = (SPChangeUser)change;
            GssAclChange gssChange = null;
            if (changeUser.IsSiteAdminChange)
            {
                gssChange = new GssAclChange(ObjectType.ADMINISTRATORS, changeUser.ChangeType,
                    GssAclMonitor.GSSITEADMINGROUPID.ToString());
            }
            else if (changeUser.ChangeType == SPChangeType.Delete)
            {
                gssChange = new GssAclChange(ObjectType.USER, changeUser.ChangeType, changeUser.Id.ToString());
            }
            if (null != gssChange)
            {
                gssChange.IsEffectiveInCurrentWeb = true;
                changes.Add(gssChange);
            }
        }
        else if (change is SPChangeGroup)
        {
            switch (change.ChangeType)
            {
                case SPChangeType.MemberAdd:
                case SPChangeType.MemberDelete:
                case SPChangeType.Delete:
                    SPChangeGroup changeGroup = (SPChangeGroup)change;
                    GssAclChange gssChange = new GssAclChange(ObjectType.GROUP, changeGroup.ChangeType, changeGroup.Id.ToString());
                    gssChange.IsEffectiveInCurrentWeb = true;
                    changes.Add(gssChange);
                    break;
            }
        }
        else if (change is SPChangeSecurityPolicy)
        {
            SPChangeSecurityPolicy changeSecurityPolicy = (SPChangeSecurityPolicy)change;
            GssAclChange gssChange = new GssAclChange(ObjectType.SECURITY_POLICY, changeSecurityPolicy.ChangeType, "");
            gssChange.IsEffectiveInCurrentWeb = true;
            changes.Add(gssChange);
        }
    }

    /// <summary>
    /// Determines if any change in SPWeb identified by changeWebId can affect the ACLs under SPWeb web
    /// </summary>
    /// <param name="site"> The site collection from which the change has been found. </param>
    /// <param name="web"> The web site from which the change has been found. </param>
    /// <param name="changeWebId"> Guid of the web site where the change has occured. </param>
    private bool IsEffectiveForWeb(SPSite site, SPWeb web, Guid changeWebId)
    {
        using (SPWeb thisWeb = site.OpenWeb(changeWebId))
        {
            if (null == thisWeb)
            {
                return false;
            }

            if (web.ID.Equals(thisWeb.ID))
            {
                return true;
            }
            else
            {
                return GssAclUtility.isSame(web.FirstUniqueAncestor, thisWeb.FirstUniqueAncestor);
            }
        }
    }

    public void UpdateChangeToken(SPChangeToken inToken)
    {
        if (null != inToken)
        {
            ChangeToken = inToken.ToString();
        }
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// Represents a basic response object containing minimal information that can be used by all other web methods.
/// For now, site collection information has been identified as one such information. The reason being, Java connector
/// uses SharePoint site's URL to access this web service. However, the operation GetAclForURLs, GetAclChangeSinceToken,
/// ResolveSPGroup etc works at site collection level. Returning this site collection info in the web service response will
/// tell the client (Java connector) about the actual site collection which was used by the web service for serving the request.
/// This info can be used for various purposes like maintaining users/groups membership as they are defined at site collection level.
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public abstract class GssAclBaseResult
{
    private string siteCollectionUrl;
    private Guid siteCollectionGuid;
    private StringBuilder logMessage = new StringBuilder();

    public string SiteCollectionUrl
    {
        get { return siteCollectionUrl; }
        set { siteCollectionUrl = value; }
    }
    public Guid SiteCollectionGuid
    {
        get { return siteCollectionGuid; }
        set { siteCollectionGuid = value; }
    }

    public String LogMessage
    {
        get { return logMessage.ToString(); }
        set { logMessage = new StringBuilder(value); }
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.AppendLine(logMsg);
    }
}

/// <summary>
/// Response Object for GetAclForUrls web method
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssGetAclForUrlsResult : GssAclBaseResult
{
    // Ideally, a map of <url, Acl> should be returned. But, C# Dictionary is not SOAP serializable. Hence, using List.
    private List<GssAcl> allAcls;

    public List<GssAcl> AllAcls
    {
        get { return allAcls; }
        set { allAcls = value; }
    }
}

/// <summary>
/// Response Object for GetAclChangesSinceToken web method
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssGetAclChangesSinceTokenResult : GssAclBaseResult
{
    private GssAclChangeCollection allChanges;

    public GssAclChangeCollection AllChanges
    {
        get { return allChanges; }
        set { allChanges = value; }
    }
}

/// <summary>
/// Response Object for ResolveSPGroup web method
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssResolveSPGroupResult : GssAclBaseResult
{
    private List<GssPrincipal> prinicpals;

    public List<GssPrincipal> Prinicpals
    {
        get { return prinicpals; }
        set { prinicpals = value; }
    }
}

/// <summary>
///
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssGetListItemsWithInheritingRoleAssignments : GssAclBaseResult
{
    // XML representation of all the documents/items to be returned
    private string docXml;

    // Are there more documents to be crawled?
    private bool moreDocs;

    // From where the next set of documents should be requested. This info has to be sent explicitly in the response because, it may happen that WS crawl some documents but does not return any as none of the documents inherits the permission. In such case, this explicit info about the lastDocCraed will save the client from visiting the same set of documents again and again
    private int lastIdVisited;

    public string DocXml
    {
        get { return docXml; }
        set { docXml = value; }
    }
    public bool MoreDocs
    {
        get { return moreDocs; }
        set { moreDocs = value; }
    }

    public int LastIdVisited
    {
        get { return lastIdVisited; }
        set { lastIdVisited = value; }
    }
}

// TODO It's better to use in-out (holders in java) parameters instead of separate objects for returning responses from every web methods

/// <summary>
/// Provides all the necessary web methods exposed by the Web Service
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class GssAclMonitor
{
    // A random guess about how many items should be query at a time. Such threshold is required to save the web service from being unresponsive for a long time
    private const int ROWLIMIT = 500;

    // A hypothetical name given to the site collection administrator group. This is required because the web service treats
    // site collection administrators as one of the SharePoitn groups. This is in benefit of avoiding re-crawling all the documents
    // when there is any change in the administrators list. Java connector sends ACL as document's metadata and any change in the
    // administrator requires re-crawling all the documents in the site collection. Having a common group for the administrator will
    // just require updating the group membership info and no re-crawl will be required.
    public const string GSSITEADMINGROUP = "[GSSiteCollectionAdministrator]";
    public const int GSSITEADMINGROUPID = -1;

    void init(out SPSite site, out SPWeb web)
    {
        SPContext spContext = SPContext.Current;
        if (null == spContext)
        {
            throw new Exception("No SharePoint context found at the endpoint.");
        }
        site = spContext.Site;
        if (null == site)
        {
            throw new Exception("SharePoint site collection not found");
        }
        web = site.OpenWeb();
        if (null == web || !web.Exists)
        {
            throw new Exception("SharePoint site not found");
        }
        // workaroud for claims based authentication to trigger authentication as soon as possible
        web.ToString();
    }

    /// <summary>
    /// A dummy method used mainly to test the availability and connectivity of the web service.
    /// </summary>
    [WebMethod]
    public string CheckConnectivity()
    {
        SPSite site;
        SPWeb web;
        init(out site, out web);

        try
        {
            // Ensure that all the required APIs are accessible
            SPUserCollection admins = web.SiteAdministrators;
            SPPolicyCollection policies = site.WebApplication.Policies;
            policies = site.WebApplication.ZonePolicies(site.Zone);
            return "success";
        }
        finally
        {
            if (web != null)
            {
                web.Dispose(); // Dispose the SPWeb Object
            }
        }
    }
    /// <summary>
    /// Method to get GssAcl corresponding to Webapplication policy and Site Collection Admin; 
    /// </summary>
    /// <returns>GssGetAclForUrlsResult object corresponding to Webapplication Policy</returns>
     [WebMethod]
    public GssGetAclForUrlsResult GetAclForWebApplicationPolicy()
    {
        using (SPSite site = new SPSite(SPContext.Current.Site.ID, GssAclUtility.GetAdminToken()))
        {
            using (SPWeb web = site.OpenWeb(SPContext.Current.Web.ID))
            {
                GssGetAclForUrlsResult result = new GssGetAclForUrlsResult();
                List<GssAcl> allAcls = new List<GssAcl>();
                Dictionary<GssPrincipal, GssSharepointPermission> commonAceMap = new Dictionary<GssPrincipal, GssSharepointPermission>();
                try
                {
                    GssAclUtility.FetchSecurityPolicyForAcl(site, commonAceMap);
                }
                catch (Exception e)
                {
                    result.AddLogMessage("Problem while processing security policies. " +
                        "ACL processing failed for [" + web.Url + "]. " +
                        "Exception [" + e.Message + "]" + Environment.NewLine + 
                        e.StackTrace);
                }

                try
                {
                    GssAclUtility.FetchSiteAdminsForAcl(web, commonAceMap);
                }
                catch (Exception e)
                {
                    result.AddLogMessage("Problem while processing site collection admins. " +
                        "ACL processing failed for [" + web.Url + "]. " + 
                        "Exception [" + e.Message + "]" + Environment.NewLine + 
                        e.StackTrace);
                }

                // Site Collection Url
                string strWebappUrl = SPContext.Current.Site.RootWeb.Url;
                GssAcl acl = new GssAcl(strWebappUrl, commonAceMap.Count);
                foreach (KeyValuePair<GssPrincipal, GssSharepointPermission> keyVal in commonAceMap)
                {
                    acl.AddAce(new GssAce(keyVal.Key, keyVal.Value));
                }
                allAcls.Add(acl);
                result.AllAcls = allAcls;
                result.SiteCollectionUrl = strWebappUrl;
                result.SiteCollectionGuid = SPContext.Current.Site.WebApplication.Id;
                return result;
            }
        }
    }      
    
    /// <summary>
    /// Returns ACLs of a set of entities which belongs to a single SharePoint web site. The SharePoint site is identified by the SPContext in which the request is being served.
    /// </summary>
    /// <param name="urls"> Entity URLs whose ACLs are to be returned </param>
    /// <returns> List Of ACLs corresponding to the entity URLs </returns>
    [WebMethod]
    public GssGetAclForUrlsResult GetAclForUrls(string[] urls)
    {
        return GetAclForUrlsUsingInheritance(urls, false, true, 0,false);
    } 
    
    /// <summary>
    ///  Returns ACLs of a set of entities which belongs to a single SharePoint web site. The SharePoint site is identified by the SPContext in which the request is being served.
    /// </summary>
    /// <param name="urls">Entity URLs whose ACLs are to be returned</param>
    /// <param name="bUseInheritance">flag indicating use of ACL inheritance</param>
    /// <param name="bIncludePolicyAcls">flag indicating to include Web Application Policy ACLs</param>
    /// <returns></returns>
    [WebMethod]
    public GssGetAclForUrlsResult GetAclForUrlsUsingInheritance(string[] urls, Boolean bUseInheritance, Boolean bIncludePolicyAcls, int largeAclThreshold, Boolean bMetaUrlFeed)
    {
        using (SPSite site = new SPSite(SPContext.Current.Site.Url, GssAclUtility.GetAdminToken()))
        {
            using (SPWeb web = site.OpenWeb(SPContext.Current.Web.ID))
            {
                GssGetAclForUrlsResult result = new GssGetAclForUrlsResult();
                List<GssAcl> allAcls = new List<GssAcl>();
                Dictionary<GssPrincipal, GssSharepointPermission> commonAceMap = new Dictionary<GssPrincipal, GssSharepointPermission>();
                Boolean checkForAnonymousAccess = false;

                try
                {
                    SPIisSettings iisSettings = site.WebApplication.IisSettings.ContainsKey(site.Zone) 
                        ? site.WebApplication.IisSettings[site.Zone] 
                        : site.WebApplication.IisSettings[SPUrlZone.Default];
                    checkForAnonymousAccess = 
                        (site.WebApplication.Policies.AnonymousPolicy != SPAnonymousPolicy.DenyAll)
                        && iisSettings.AllowAnonymous
                        && !(GssAclUtility.DenyReadPolicyAvailable(site.WebApplication, site.Zone));
                }
                catch (Exception exAnonymous)
                {
                    result.AddLogMessage(String.Format(
                        "Error reading anonymous access setting for Web [{0}]. Exception [{1}] {2} {3}",
                        web.Url, exAnonymous.Message, Environment.NewLine, exAnonymous.StackTrace));
                }
                
               
                                            
                if (bIncludePolicyAcls)
                {
                    try
                    {
                        GssAclUtility.FetchSecurityPolicyForAcl(site, commonAceMap);
                    }
                    catch (Exception e)
                    {
                        result.AddLogMessage("Problem while processing security policies. " +
                            "ACL processing failed for [" + web.Url + "]. " +
                            "Exception [" + e.Message + "]" + Environment.NewLine + 
                            e.StackTrace);
                    }

                    try
                    {
                        GssAclUtility.FetchSiteAdminsForAcl(web, commonAceMap);
                    }
                    catch (Exception e)
                    {
                        result.AddLogMessage("Problem while processing site collection admins. " +
                            "ACL processing failed for [" + web.Url + "]. " +
                            "Exception [" + e.Message + "]" + Environment.NewLine + 
                            e.StackTrace);
                    }
                }
                
                string strWebappUrl = SPContext.Current.Site.WebApplication.GetResponseUri(SPContext.Current.Site.Zone).AbsoluteUri;
                if (!String.IsNullOrEmpty(strWebappUrl) && strWebappUrl.EndsWith("/"))
                {
                    //Removing Trailing "/" from web application url;
                    strWebappUrl = strWebappUrl.Remove(strWebappUrl.Length - 1);
                }
                Uri uriWebApp = new Uri(strWebappUrl);
                if (uriWebApp.IsDefaultPort && !strWebappUrl.EndsWith(uriWebApp.Port.ToString()))
                {
                    // Adding default port to web app URL
                    strWebappUrl = String.Format("{0}:{1}", strWebappUrl, uriWebApp.Port);
                }
                foreach (string url in urls)
                {
                    GssAcl acl = null;
                    ISecurableObject secobj = null;
                    try
                    {
                        Dictionary<GssPrincipal, GssSharepointPermission> aceMap = new Dictionary<GssPrincipal, GssSharepointPermission>(commonAceMap);
                        secobj = GssAclUtility.IdentifyObject(url, web);

                        if (checkForAnonymousAccess && (web.AnonymousState != SPWeb.WebAnonymousState.Disabled) && GssAclUtility.IsAnonymousAccessAllowed(secobj))
                        {
                            acl = new GssAcl(url, 0);
                            acl.AnonymousAccess = true.ToString();
                            acl.AddLogMessage(String.Format("Anonymous access allowed for {0}", url));
                            allAcls.Add(acl);
                            continue;
                        }
                        Boolean readSecurityApplicable = false;
                        if (secobj is SPListItem)
                        {
                            SPList parentList = ((SPListItem)secobj).ParentList;
                            readSecurityApplicable = (parentList.ReadSecurity == 2);
                        }
                        if (secobj != null)
                        {
                            if (secobj.HasUniqueRoleAssignments || bUseInheritance == false || readSecurityApplicable)
                            {
                                Boolean largeAcl = secobj.RoleAssignments.Count > largeAclThreshold && largeAclThreshold > 0;
                                if (largeAcl && urls.Length > 1)
                                {
                                    acl = new GssAcl(url, 0);
                                    allAcls.Add(acl);
                                    acl.LargeAcl = largeAcl.ToString();
                                    if (!readSecurityApplicable)
                                    {
                                        if (!secobj.HasUniqueRoleAssignments)
                                        {
                                            acl.ParentUrl = GssAclUtility.GetUrl(secobj.FirstUniqueAncestor, strWebappUrl);
                                            acl.InheritPermissions = true.ToString();
                                        }
                                    }
                                    acl.AddLogMessage(String.Format("Large ACL for URL {0} with {1} role assignments with readsecurity as {2}", url, secobj.RoleAssignments.Count, readSecurityApplicable));
                                }
                                else
                                {
                                    SPUser owner = null;
                                    String strGetOwnerException = null;
                                    try
                                    {
                                        owner = GssAclUtility.GetOwner(secobj);
                                    }
                                    catch (Exception e)
                                    {
                                        strGetOwnerException = "Owner information was not found because following exception occured: " + e.Message;
                                    }

                                    // largeAclThreshold > 0 check to ensure connector supports large ACLs, else SP Groups will be reloved as part of ACL processing.
                                    GssAclUtility.FetchRoleAssignmentsForAcl(secobj.RoleAssignments, aceMap, readSecurityApplicable, largeAclThreshold > 0);
                                    if (readSecurityApplicable)
                                    {
                                        GssAclUtility.AddOwnerToAcl(aceMap, owner, secobj);
                                    }

                                    acl = new GssAcl(url, aceMap.Count);
                                    if (!String.IsNullOrEmpty(strGetOwnerException))
                                    {
                                        acl.AddLogMessage(strGetOwnerException);
                                    }
                                    if (owner != null)
                                    {
                                        acl.Owner = GssAclUtility.DecodeIdentity(owner.LoginName);
                                    }
                                    foreach (KeyValuePair<GssPrincipal, GssSharepointPermission> keyVal in aceMap)
                                    {
                                        acl.AddAce(new GssAce(keyVal.Key, keyVal.Value));
                                    }
                                    allAcls.Add(acl);
                                    if (!bIncludePolicyAcls)
                                    {
                                        acl.ParentUrl = SPContext.Current.Site.RootWeb.Url;
                                        acl.ParentId = String.Format("{{{0}}}", SPContext.Current.Site.WebApplication.Id.ToString());
                                    }
                                }
                            }
                            else
                            {
                                acl = new GssAcl(url, 0);
                                acl.InheritPermissions = "true";
                                GssAclUtility.GetParentUrl(secobj, strWebappUrl, acl, bMetaUrlFeed);
                                allAcls.Add(acl);
                            }
                        }
                        else
                        {
                            acl = new GssAcl(url, 0);
                            acl.AddLogMessage("Problem Identifying Object with Url [" + url + " ] ");
                            allAcls.Add(acl);
                        }
                    }
                    catch (Exception e)
                    {
                        acl = new GssAcl(url, 0);
                        acl.AddLogMessage("Problem while processing role assignments. " +
                            "ACL processing failed for [" + url + "]. " +
                            "Exception [" + e.Message + "]" + Environment.NewLine + 
                            e.StackTrace);
                        allAcls.Add(acl);
                    }
                }                
                result.AllAcls = allAcls;
                result.SiteCollectionUrl = site.Url;
                result.SiteCollectionGuid = site.ID;
                return result;
            }
        }        
    }
    
    /// <summary>
    /// Returns a list of ACL specific changes that have happened over a period of time, determined by the change token.
    /// These changes purely reflects the actions performed on the SharePoint but does not talk about their implications. The caller should not assume that the ACL of any entity has changed just because it receives a set of changes from this API.
    /// A typical example could be when the permission hierarchy of a list/web is reset and immediately brought to its original state. In that case this API will return two changes but there is no ACL change on the SharePoint.
    /// Deletion of an empty group is another such case.
    /// </summary>
    /// <param name="fromChangeToken"> The change token from where the changes are to be scanned in the SharePoint's change log. It defines the starting point in the change log </param>
    /// <param name="toChangeToken"> Only those changes which have been registered in the change log with a token that appears before this token will be retrieved. It defines the ending pooint in the change log </param>
    /// <returns> a list of ACL specific changes </returns>
    [WebMethod]
    public GssGetAclChangesSinceTokenResult GetAclChangesSinceToken(string fromChangeToken, string toChangeToken)
    {
        GssGetAclChangesSinceTokenResult result = new GssGetAclChangesSinceTokenResult();
        using (SPSite site = new SPSite(SPContext.Current.Site.Url, GssAclUtility.GetAdminToken()))
        {
            using (SPWeb web = site.OpenWeb(SPContext.Current.Web.ID))
            {

                GssAclChangeCollection allChanges = null;
                SPChangeToken changeTokenEnd = null;
                if (String.IsNullOrEmpty(fromChangeToken))
                {
                    // It's the first request. Return the current chage token of the site
                    // collection as the next token for synchronization
                    allChanges = new GssAclChangeCollection(site.CurrentChangeToken);
                    result.AllChanges = allChanges;
                    result.SiteCollectionUrl = site.Url;
                    result.SiteCollectionGuid = site.ID;
                    return result;
                }

                if (null != toChangeToken && toChangeToken.Length != 0)
                {
                    changeTokenEnd = new SPChangeToken(toChangeToken);
                }

                SPChangeToken changeTokenStart = new SPChangeToken(fromChangeToken);
                allChanges = new GssAclChangeCollection(changeTokenStart);
                try
                {
                    SPChangeCollection spChanges = GssAclUtility.FetchAclChanges(site, changeTokenStart, changeTokenEnd);
                    foreach (SPChange change in spChanges)
                    {
                        allChanges.AddChange(change, site, web);
                    }

                    // There are two ways to get the next Change Token value that should be used
                    // for synchronization. 1) Get the last change token available for the site
                    // 2) Get the last change token corresponding to which changes have been tracked.
                    // The problem with the second approach is that if no ACL specific changes will
                    // occur, the change token will never gets updated and will become invalid after
                    // some time. Another performance issue is is that, the scan will always start
                    // form the same token unless there is a ACL specific change.
                    // Since, the change tracking logic ensures that all changes will be tracked
                    // (i.e there is no rowlimit kind of thing associated), it is safe to use the
                    // first approach.
                    if (null == changeTokenEnd)
                    {
                        // Since all the changes have been detected till the time, use the current
                        // chage token of the site collection as the next token for synchronization
                        allChanges.UpdateChangeToken(site.CurrentChangeToken);
                    }
                    else
                    {
                        // Since change detection was done only till changeTokenToEnd, we have to
                        // use the same token as next token for synchronization
                        allChanges.UpdateChangeToken(changeTokenEnd);
                    }
                }
                catch (Exception e)
                {
                    // If current change token is invalid there is no way to recover from it.
                    // So just return current change token. Also mark each group from site
                    // collection as changed group to force reindexing of SharePoint groups
                    // to keep them in sync.
                    allChanges = new GssAclChangeCollection(site.CurrentChangeToken);
                    result.AddLogMessage("Exception occurred while change detection. "
                        + "Exception [" + e.Message + "]" + Environment.NewLine +
                        e.StackTrace);
                    if (web.IsRootWeb)
                    {
                        result.AddLogMessage(
                            "Forcing reindexing of SharePoint Groups under Site [" + site.Url + "]");
                        foreach (SPGroup group in web.SiteGroups)
                        {
                            GssAclChange groupChange = new GssAclChange(
                                ObjectType.GROUP, SPChangeType.MemberAdd, group.ID.ToString());
                            groupChange.IsEffectiveInCurrentWeb = true;
                            allChanges.Changes.Add(groupChange);
                        }
                    }
                }
                result.AllChanges = allChanges;
                result.SiteCollectionUrl = site.Url;
                result.SiteCollectionGuid = site.ID;
            }
        }
        return result;
    }

    /// <summary>
    /// Expands a SharePoint group to find all the member users and domain groups. Creates a <see cref="GssPrincipal"/> object for each of them and returns the same.
    /// The group must exist in the site collection for which the request has been sent.
    /// </summary>
    /// <param name="groupId"> the SharePoint group ID/Name that is to be resolved </param>   
    /// <returns> list of GssPrincipal object with the Members attribute set as the list of member users/domain-groups</returns>
    [WebMethod]
    public GssResolveSPGroupResult ResolveSPGroup(string[] groupId)
    {
        return ResolveSPGroupInBatch(groupId, -1);
    }
    
    
    /// <summary>
    /// Expands a SharePoint group to find all the member users and domain groups. Creates a <see cref="GssPrincipal"/> object for each of them and returns the same.
    /// The group must exist in the site collection for which the request has been sent.
    /// </summary>
    /// <param name="groupId"> the SharePoint group ID/Name that is to be resolved </param>
    /// <param name="batchSize">batch size to limit number of users being returned. -1 to return everything</param>
    /// <returns> list of GssPrincipal object with the Members attribute set as the list of member users/domain-groups</returns>
    [WebMethod]
    public GssResolveSPGroupResult ResolveSPGroupInBatch(string[] groupId, int batchSize)
    {
         using (SPSite site = new SPSite(SPContext.Current.Site.ID, GssAclUtility.GetAdminToken()))
         {
             using (SPWeb web = site.OpenWeb(SPContext.Current.Web.ID))
             {
                 GssResolveSPGroupResult result = new GssResolveSPGroupResult();
                 List<GssPrincipal> prinicpals = new List<GssPrincipal>();
                             
                     if (groupId != null)
                     {
                         int principalCounter = 0;                                     
                         foreach (string id in groupId)
                         {                            
                             GssPrincipal principal = null;
                             try
                             {
                                 if (GSSITEADMINGROUPID.ToString().Equals(id))
                                 {
                                     principal = new GssPrincipal(GSSITEADMINGROUP, GSSITEADMINGROUPID);
                                     // Get all the administrator users as member of the GSSITEADMINGROUP.
                                     List<GssPrincipal> admins = new List<GssPrincipal>();
                                     foreach (SPPrincipal spPrincipal in web.SiteAdministrators)
                                     {
                                         GssPrincipal admin = GssAclUtility.GetGssPrincipalFromSPPrincipal(spPrincipal);
                                         if (admin == null)
                                         {
                                             continue;
                                         }
                                         admins.Add(admin);
                                     }
                                     principal.Members = admins;
                                     principalCounter += principal.Members.Count;
                                 }
                                 else
                                 {
                                     SPGroup spGroup = web.SiteGroups.GetByID(int.Parse(id));
                                     if (spGroup != null)
                                     {
                                         principal = new GssPrincipal(spGroup.Name, spGroup.ID);
                                         principal.Members = GssAclUtility.ResolveSPGroup(spGroup);
                                         principalCounter += principal.Members.Count;
                                     }
                                     else
                                     {
                                         principal = new GssPrincipal(id, -2);
                                         principal.AddLogMessage("Could not resolve Group Id [ " + id + " ] ");
                                     }
                                 }
                                 principal.Type = GssPrincipal.PrincipalType.SPGROUP;
                             }
                             catch (Exception e)
                             {
                                 principal = new GssPrincipal(id, -2);
                                 principal.AddLogMessage("Could not resolve Group Id [ " + id + " ]. " +
                                     "Exception [" + e.Message + "]" + Environment.NewLine + 
                                     e.StackTrace);
                                 principal.Type = GssPrincipal.PrincipalType.NA;
                             }
                             prinicpals.Add(principal);

                             if (batchSize > 0 && principalCounter >= batchSize)
                             {                               
                                 break;
                             }
                         }
                     }
                 result.Prinicpals = prinicpals;
                 result.SiteCollectionUrl = site.Url;
                 result.SiteCollectionGuid = site.ID;
                 return result;
                 
             }
         }      
    }

    /// <summary>
    /// Returns the GUIDs of all those Lists which are inheriting their permissions from the SharePoint web site to which the request has been sent
    /// </summary>
    /// <returns> list of GUIDs of the lists </returns>
    [WebMethod]
    public List<string> GetListsWithInheritingRoleAssignments()
    {
        SPSite site;
        SPWeb web;
        init(out site, out web);

        try
        {
            List<string> listIDs = new List<string>();
            SPListCollection lists = web.Lists;
            foreach (SPList list in lists)
            {
                if (!list.HasUniqueRoleAssignments && GssAclUtility.isSame(web.FirstUniqueAncestor, list.FirstUniqueAncestor))
                {
                    listIDs.Add(list.ID.ToString());
                }
            }
            return listIDs;
        }
        finally
        {
            if (web != null)
            {
                web.Dispose(); // Dispose the SPWeb Object
            }
        }
    }

    /// <summary>
    /// Returns the List Items (sorted in ascending order of their IDs) which are inheriting the role assignments from the passed in SharePoint List.
    /// </summary>
    /// <param name="listGuId"> GUID of the SharePoint List from which the Items are to be returned. The list must belong to the the site in which the request has been sent </param>
    /// <param name="rowLimit"> Threshold value for the document count to be returned </param>
    /// <param name="lastItemId"> Only document ahead of this ID should be returned </param>
    /// <returns> list of IDs of the items </returns>
    [WebMethod]
    public GssGetListItemsWithInheritingRoleAssignments GetListItemsWithInheritingRoleAssignments(string listGuId, int rowLimit, int lastItemId)
    {
        SPSite site;
        SPWeb web;
        init(out site, out web);

        SPList changeList = null;
        try
        {
            changeList = web.Lists[new Guid(listGuId)];
            if (null == changeList)
            {
                throw new Exception("Passed in listId [ " + listGuId + " ] does not exist in the current web site context");
            }
        }
        catch (Exception e)
        {
            throw new Exception("Passed in listId [ " + listGuId + " ] does not exist in the current web site context");
        }
        finally
        {
            if (web != null)
            {
                web.Dispose(); // Dispose the SPWeb Object
            }
        }

        try
        {
            List<string> itemIDs = new List<string>();
            SPQuery query = new SPQuery();
            query.RowLimit = GssAclMonitor.ROWLIMIT;
            // CAML query to do a progressive crawl of items in ascending order of their IDs. The prgression is controlled by lastItemId
            query.Query = "<Where>"
                           + "<Gt>"
                           + "<FieldRef Name=\"ID\"/>"
                           + "<Value Type=\"Counter\">" + lastItemId + "</Value>"
                           + "</Gt>"
                           + "</Where>"
                           + "<OrderBy>"
                           + "<FieldRef Name=\"ID\" Ascending=\"TRUE\" />"
                           + "</OrderBy>";
            if (changeList.BaseType == SPBaseType.DocumentLibrary
                || changeList.BaseType == SPBaseType.GenericList
                || changeList.BaseType == SPBaseType.Issue)
            {
                query.ViewAttributes = "Scope = 'RecursiveAll'";
            }

            SPListItemCollection items = changeList.GetItems(query);

            GssGetListItemsWithInheritingRoleAssignments result = new GssGetListItemsWithInheritingRoleAssignments();
            XmlDocument xmlDoc = new XmlDocument();
            XmlNode rootNode = xmlDoc.CreateNode(XmlNodeType.Element, "GssListItems", "");

            int i = 0;
            foreach (SPListItem item in items)
            {
                if (i >= rowLimit)
                {
                    result.MoreDocs = true;
                    break;
                }
                if (!item.HasUniqueRoleAssignments && GssAclUtility.isSame(changeList.FirstUniqueAncestor, item.FirstUniqueAncestor))
                {
                    XmlNode node = handleOwsMetaInfo(item);
                    node = xmlDoc.ImportNode(node, true);
                    rootNode.AppendChild(node);
                    ++i;
                }
                result.LastIdVisited = item.ID;
            }
            if (null != items.ListItemCollectionPosition)
            {
                result.MoreDocs = true;
            }
            XmlAttributeCollection allAttrs = rootNode.Attributes;
            XmlAttribute attr = xmlDoc.CreateAttribute("Count");
            attr.Value = i.ToString();
            allAttrs.Append(attr);

            result.DocXml = rootNode.OuterXml;
            result.SiteCollectionUrl = site.Url;
            result.SiteCollectionGuid = site.ID;
            return result;
        }
        finally 
        {
            if (web != null)
            {
                web.Dispose(); // Dispose the SPWeb Object
            }
        }
        
    }

    /// <summary>
    /// Return the XML representation of a ListItem. Handles the ows_MetaInfo attribute by taking these value explicitly from the item's property bag. This ensures that the (key, value) pairs stored inside the property bag will be returned as separate attributes.
    /// </summary>
    /// <param name="listItem"></param>
    /// <returns></returns>
    static XmlNode handleOwsMetaInfo(SPListItem listItem)
    {
        Hashtable props = listItem.Properties;
        XmlDocument xmlDoc = new XmlDocument();
        xmlDoc.LoadXml(listItem.Xml);
        XmlNodeList nodeList = xmlDoc.GetElementsByTagName("z:row");
        XmlNode node = nodeList[0];
        if (null == node)
        {
            return null;
        }
        XmlAttributeCollection allAttrs = node.Attributes;
        XmlAttribute ows_MetaInfo = node.Attributes["ows_MetaInfo"];
        if (null == allAttrs || null == ows_MetaInfo)
        {
            return null;
        }
        allAttrs.Remove(ows_MetaInfo);
        foreach (DictionaryEntry propEntry in props)
        {
            // xmlDoc.CreateAttribute throws exception if aatribute contains space
            string propName = propEntry.Key.ToString().Replace(" ", "_x0020_");
            XmlAttribute attr = xmlDoc.CreateAttribute("ows_MetaInfo_" + propName);
            attr.Value = propEntry.Value.ToString();
            allAttrs.Append(attr);
        }
        return node;
    }
}

/// <summary>
/// Provides general purpose utility methods.
/// This class must be stateless becasue it is a member instance of the web service
/// </summary>
public sealed class GssAclUtility
{

    private GssAclUtility()
    {
        throw new Exception("Operation not allowed! ");
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from the web application security policies
    /// </summary>
    /// <param name="site"> Site Collection for which the security policies are to be tracked </param>
    /// <param name="userAceMap"> ACE map to be updated </param>
    public static void FetchSecurityPolicyForAcl(SPSite site, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        // policies apllied at web application level. This is applicable to all the zones
        SPPolicyCollection policies = site.WebApplication.Policies;
        foreach (SPPolicy policy in policies)
        {
            GssPrincipal principal = GetGssPrincipalForSecPolicyUser(site, policy.UserName);
            if (null == principal)
            {
                continue;
            }

            GssSharepointPermission permission = null;

            if (aceMap.ContainsKey(principal))
            {
                permission = aceMap[principal];
            }
            else
            {
                permission = new GssSharepointPermission();
                aceMap.Add(principal, permission);
            }

            foreach (SPPolicyRole policyRole in policy.PolicyRoleBindings)
            {
                permission.UpdatePermission(policyRole.GrantRightsMask, policyRole.DenyRightsMask, false);
            }
        }

        // policies applied on the current URL zone
        policies = site.WebApplication.ZonePolicies(site.Zone);
        foreach (SPPolicy policy in policies)
        {
            GssPrincipal principal = GetGssPrincipalForSecPolicyUser(site, policy.UserName);
            if (null == principal)
            {
                continue;
            }

            GssSharepointPermission permission = null;

            if (aceMap.ContainsKey(principal))
            {
                permission = aceMap[principal];
            }
            else
            {
                permission = new GssSharepointPermission();
                aceMap.Add(principal, permission);
            }

            foreach (SPPolicyRole policyRole in policy.PolicyRoleBindings)
            {
                permission.UpdatePermission(policyRole.GrantRightsMask, policyRole.DenyRightsMask, false);
            }
        }
    }

    /// <summary>
    /// Method to check if deny read policy is specified.
    /// </summary>
    /// <param name="webApp"></param>
    /// <param name="currentZone"></param>
    /// <returns></returns>
    public static Boolean DenyReadPolicyAvailable(SPWebApplication webApp, SPUrlZone currentZone)
    {
        foreach (SPPolicy policy in webApp.Policies)
        {
            foreach (SPPolicyRole policyRole in policy.PolicyRoleBindings)
            {
                if (SPBasePermissions.ViewListItems == (SPBasePermissions.ViewListItems & policyRole.DenyRightsMask))
                {
                    return true;
                }
            }
        }

        foreach (SPPolicy policy in webApp.ZonePolicies(currentZone))
        {
            foreach (SPPolicyRole policyRole in policy.PolicyRoleBindings)
            {
                if (SPBasePermissions.ViewListItems == (SPBasePermissions.ViewListItems & policyRole.DenyRightsMask))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from the site collection administrators list.
    /// Site Collection Administrator is treated as another site collection group. All the users/groups are sent as members of this group
    /// </summary>
    /// <param name="web"> SharePoint web site whose administrators are to be tracked </param>
    /// <param name="userAceMap"> ACE Map to be updated </param>
    public static void FetchSiteAdminsForAcl(SPWeb web, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        GssPrincipal principal = new GssPrincipal(GssAclMonitor.GSSITEADMINGROUP, GssAclMonitor.GSSITEADMINGROUPID);
        principal.Type = GssPrincipal.PrincipalType.SPGROUP;
        GssSharepointPermission permission = new GssSharepointPermission();
        // Administrators have Full Rights in the site collection.
        permission.UpdatePermission(SPBasePermissions.FullMask, SPBasePermissions.EmptyMask, false);
        aceMap.Add(principal, permission);

        // Get all the administrator user as member of the GSSITEADMINGROUP.
        List<GssPrincipal> admins = new List<GssPrincipal>();
        foreach (SPPrincipal spPrincipal in web.SiteAdministrators)
        {
            GssPrincipal admin = GetGssPrincipalFromSPPrincipal(spPrincipal);
            if (null == admin)
            {
                continue;
            }
            admins.Add(admin);
        }

        principal.Members = admins;
    }
    public static void AddOwnerToAcl(Dictionary<GssPrincipal, GssSharepointPermission> aceMap, SPUser owner, ISecurableObject secobj)
    {
        SPListItem item = (SPListItem) secobj;
        if (owner != null && item.DoesUserHavePermissions(owner, SPBasePermissions.ViewListItems))
        {
            GssPrincipal gssPrincipal = new GssPrincipal(owner.LoginName, owner.ID);
            gssPrincipal.Type = GssPrincipal.PrincipalType.USER;
            GssSharepointPermission permission;
            if (aceMap.ContainsKey(gssPrincipal))
            {
                permission = aceMap[gssPrincipal];
            }
            else
            {
                permission = new GssSharepointPermission();
                aceMap.Add(gssPrincipal, permission);
           }
           permission.UpdatePermission(SPBasePermissions.FullMask, SPBasePermissions.EmptyMask, false);
        }
    }
    
    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from a list of role assignments
    /// </summary>
    /// <param name="roles"> list of role assignments </param>
    /// <param name="userAceMap"> ACE Map to be updated </param>
    public static void FetchRoleAssignmentsForAcl(SPRoleAssignmentCollection roles, Dictionary<GssPrincipal, GssSharepointPermission> aceMap, Boolean readSecurityApplicable, Boolean resolveGroup )
    {
        foreach (SPRoleAssignment roleAssg in roles)
        {
            GssPrincipal principal = GetGssPrincipalFromSPPrincipal(roleAssg.Member, resolveGroup);
            GssSharepointPermission permission = null;
            Boolean bNewPrincipal = false;

            if (null == principal)
            {
                continue;
            }

            if (aceMap.ContainsKey(principal))
            {
                permission = aceMap[principal];
            }
            else
            {
                permission = new GssSharepointPermission();
                bNewPrincipal = true;

            }

            foreach (SPRoleDefinition roledef in roleAssg.RoleDefinitionBindings)
            {
                permission.UpdatePermission(roledef.BasePermissions, SPBasePermissions.EmptyMask, readSecurityApplicable);
            }

            if (bNewPrincipal)
            {
                if (permission.AllowedPermissions.Count > 0 || permission.DeniedPermission.Count > 0)
                {
                    // Adding permissions only if non empty.
                    // This check will avoid empty ACLs
                    aceMap.Add(principal, permission);
                }
            }
        }
    }
    
    /// <summary>
    /// Identifies if anonymous access is allowed for SharePoint securable object
    /// </summary>
    /// <param name="secObj"> Secured Object to check for anonymous access</param>
    /// <returns> returns true if anonymous access is allowed else returns false</returns>
    public static Boolean IsAnonymousAccessAllowed(ISecurableObject secObj)
    {
        if (secObj is SPWeb)
        {
            return SPBasePermissions.ViewListItems == (SPBasePermissions.ViewListItems & ((SPWeb)secObj).AnonymousPermMask64);
            
        }

        if (secObj is SPList)
        {
            return ((SPBasePermissions.ViewListItems == (SPBasePermissions.ViewListItems & ((SPList)secObj).AnonymousPermMask64)) 
                && ((SPList)secObj).ReadSecurity != 2);
                
        }

        if (secObj is SPListItem)
        {
            SPListItem oChildItem = (SPListItem)secObj;
            if (oChildItem.HasUniqueRoleAssignments) 
            {
                return false;
            }
            SPList oList = oChildItem.ParentList;
            Boolean bAnonymousAccessEnabledOnList =
               (SPBasePermissions.ViewListItems == (SPBasePermissions.ViewListItems & oList.AnonymousPermMask64)) && (oList.ReadSecurity != 2);
              
            ISecurableObject firstUniqueAncestor = oChildItem.FirstUniqueAncestor;
            // if firstUniqueAncestor is of type SPListItem then oChildItem is part of folder structure
            // with broken inheritance chain from parent list. 
            return bAnonymousAccessEnabledOnList && !(firstUniqueAncestor is SPListItem);           
        }
        return false;
    }
       
    /// <summary>
    /// Identifies the SharePoint object represented by the incoming URL and returns a corresponding ISecurable object for same   
    /// </summary>
    /// <param name="url"> Entity URL</param>
    /// <param name="web"> Parent Web to which the entity URL belongs </param>
    /// <returns></returns>
    public static ISecurableObject IdentifyObject(string url, SPWeb web)
    {
        SPListItem listItem = null;
        //check if the url ending with default.aspx, then return IsecureObejct
        // for the site not any of it's List/listItems to fetch Acl's.
        
        if (!url.EndsWith("default.aspx")) 
        {
            listItem = web.GetListItem(url);
            if (null != listItem)
            {
                return listItem;
            }
        }
        else
        {
            return web;
        }

        SPList list = web.GetList(url);
        if (null != list)
        {
            try
            {
                Uri uri = new Uri(url);
                string query = uri.Query;
                string id = HttpUtility.ParseQueryString(query).Get("ID");
                int idToCheck = 0;
                if (int.TryParse(id, out idToCheck))
                {
                    listItem = list.GetItemById(idToCheck);
                    if (listItem != null)
                    {
                        return listItem;
                    }
                }

                //Check for Folder / File                  
                SPFile oFile = web.GetFile(url);
                if (oFile != null)
                {
                    if (oFile.Item != null)
                    {
                        return oFile.Item;
                    }                    
                    if (oFile.ParentFolder != null && list.EnableAttachments)
                    {
                        //This is check for Attachments. 
                        //In case of Attachments, security is same as of SPListItem it is associated with
                        int parentId = 0;
                        if (int.TryParse(oFile.ParentFolder.Name, out parentId))
                        {
                            SPListItem oParent = list.GetItemById(parentId);
                            if (oParent != null && oParent.Attachments != null)
                            {
                                String attachmentUrlPrefix = oParent.Attachments.UrlPrefix;
                                String attachmentUrlTocheck = attachmentUrlPrefix + oFile.Name;
                                if (String.Compare(attachmentUrlTocheck, url, true) == 0)
                                {
                                    // This is Url for Attachment Item. So we will return Parent Item.
                                    // For attachment url, inherit from will be same as inherit from for
                                    // list item it is associated with.
                                    return oParent;
                                } 
                            }
                        }
                        
                    }
                }
            }
            catch (Exception e)
            {
                return list;
            }
            //Returning List as a default Identified Object
            return list;
        }
        // TODO Need to check if we should return null here
        // instead of sending current SPWeb since we are unable to identify object. 
        return web;
    }

    public static String GetUrl(ISecurableObject spObject, String strSiteUrl)
    {
        String strUrl = String.Empty;
        if (spObject is SPWeb)
        {
            strUrl = ((SPWeb)spObject).Url + "/default.aspx";
        }
        else if (spObject is SPList)
        {
            SPList oList = (SPList)spObject;
            strUrl = strSiteUrl + oList.RootFolder.ServerRelativeUrl;
        }
        else if (spObject is SPListItem)
        {
            SPListItem oItem = (SPListItem)spObject;
            strUrl = strSiteUrl + oItem.Url;
        }
        return strUrl;
    }

    /// <summary>
    /// Utility method to get Parent Object URL
    /// </summary>
    /// <param name="child">ISecurable Object. This can be SPListItem, SPlist or SPWeb</param>
    /// <returns></returns>
    public static void GetParentUrl(ISecurableObject child, String strSiteUrl, GssAcl aclToUpdate, Boolean bMetaUrlFeed)
    {      
        if (child == null)
        {
            aclToUpdate.ParentId = String.Empty;
            aclToUpdate.ParentUrl = String.Empty;
        }
        if (child is SPListItem)
        {
            SPListItem oChildItem = (SPListItem)child;
            SPFile oFile = oChildItem.File;
            // In case of Folders and generic List items oChildItem.File will be null. 
            // So need to create SPFile object explicitly.
            if (oFile == null)
            {
                oFile = oChildItem.Web.GetFile(oChildItem.Url);
            }
            if (oFile != null)
            {
                String parentListUrl = oChildItem.ParentList.DefaultViewUrl;
                if (String.IsNullOrEmpty(parentListUrl) || parentListUrl == "/")
                {
                    parentListUrl = oChildItem.ParentList.RootFolder.ServerRelativeUrl;
                }
                aclToUpdate.ParentUrl = strSiteUrl + parentListUrl;
                //To check if Item is available at root level or inside folder
                if (String.Compare(oFile.ParentFolder.ServerRelativeUrl,
                    oChildItem.ParentList.RootFolder.ServerRelativeUrl, true) == 0)
                {
                    // If item is available at root level (outside folder) return default view
                    // URL for SPList (same URL is being used in ListState by connector)                       
                    aclToUpdate.ParentId =
                        String.Format("{{{0}}}", oChildItem.ParentList.ID.ToString());
                }
                else
                {
                    // If item is available inside folder 
                    // then return folder Url in case of meta url feeds.
                    if (bMetaUrlFeed)
                    {
                        aclToUpdate.ParentUrl = strSiteUrl + oFile.ParentFolder.ServerRelativeUrl;
                    }
                    aclToUpdate.ParentId = oFile.ParentFolder.Item.ID.ToString();
                }
            }
        }
        else if (child is SPList)
        {
            SPList oChildList = (SPList)child;
            // As per https://msdn.microsoft.com/en-us/library/aa973248(v=office.12).aspx
            // No need to dispose SPList.ParentWeb
            SPWeb oParentWeb = oChildList.ParentWeb; 
            // Homepage for SPWeb. SPConnector is always using this as default.aspx
            // TODO Ideally This should be used as String strWelcomePage = oParentWeb.RootFolder.WelcomePage;
            String strWelcomePage = "default.aspx";
            aclToUpdate.ParentUrl = oParentWeb.Url + "/" + strWelcomePage;
            aclToUpdate.ParentId = String.Format("{{{0}}}", oParentWeb.ID.ToString());

        }
        else if (child is SPWeb)
        {
            // No need to dispose SPWeb Object created by type cast.
            SPWeb oChildWeb = (SPWeb)child;
            // As per https://msdn.microsoft.com/en-us/library/aa973248(v=office.12).aspx
            // No need to dispose SPWeb.ParentWeb
            SPWeb oParentWeb = oChildWeb.ParentWeb;

            if (oParentWeb != null && oParentWeb.Exists)
            {
                //Homepage for SPWeb
                String strWelcomePage = "default.aspx";
                aclToUpdate.ParentUrl = oParentWeb.Url + "/" + strWelcomePage;
                aclToUpdate.ParentId = String.Format("{{{0}}}", oParentWeb.ID.ToString());
            }
            else // Child is a root web (since there is no parent)
            {
                //Homepage for Root SPWeb
                String strWelcomePage = "default.aspx";
                aclToUpdate.ParentUrl = oChildWeb.Url + "/" + strWelcomePage;
                aclToUpdate.ParentId = String.Format("{{{0}}}", oChildWeb.ID.ToString());
            }
        }
    }

    /// <summary>
    /// Uses reflection to detect if the Principal is claim and if it is, decodes the real identity
    /// Reflection must be used, so that we can run in SharePoint 2007
    /// </summary>
    /// <param name="user">identity information to have checked for claim and decoded if needed</param>
    /// <returns>decoded identity</returns>
    public static String DecodeIdentity(String identity)
    {
        Type managerType = Assembly.GetAssembly(typeof(SPUser)).GetType("Microsoft.SharePoint.Administration.Claims.SPClaimProviderManager");
        if (managerType == null)
        {
            return identity;
        }
        Object manager = managerType.GetProperty("Local").GetValue(null, null);

        if (manager != null &&
            (bool)managerType.GetMethod("IsEncodedClaim").Invoke(manager, new object[]{identity}))
        {
            Object claim = managerType.GetMethod("DecodeClaim").Invoke(manager, new object[]{identity});
            string username = (string)claim.GetType().GetProperty("Value").GetValue(claim, null);
            if (username == "true")
            {
                return "Everyone";
            }
            else if (username == "windows")
            {
                return "NT AUTHORITY\\Authenticated Users";
            }
            else
            {
                return username;
            }
        }
        else
        {
            return identity;
        }
    }

    /// <summary>
    /// Retrieves the Owner's information about a given ISecurable entity
    /// </summary>
    /// <param name="secobj"></param>
    /// <returns></returns>
    public static SPUser GetOwner(ISecurableObject secobj)
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

    /// <summary>
    /// Tracks the list of ACL related changes that have happened under the current site collection (to which the request has been sent) from a given point of time, determined by the change token value.
    /// These list of changes are not guaranteed to affect the ACL of every or a even a single entity in the SharePoint. Rather, it purely reflects what action has been performed.
    /// Caller should analyse the implications of these changes and work accordingly.
    ///
    /// This method does not currently supports Item level tracking. This is not required becasue getListItemChangesSinceToken which connector already usees already does the same.
    /// This makes the web service implementation tightly coupled with the connector though, we can live with this limitation for now as the web service, for now, is to be used by the connector only.
    /// </summary>
    /// <param name="site"> The Site collection in which the changes are to be tracked </param>
    /// <param name="changeTokenStart"> The starting change token value from where the changes are to be scanned in the SharePoint's change log </param>
    /// <param name="changeTokenEnd"> The ending change token value until where the changes are to be scanned in the SharePoint's change log </param>
    /// <returns> list of changes that most likely expected to change the ACLs of the entities </returns>
    public static SPChangeCollection FetchAclChanges(SPSite site, SPChangeToken changeTokenStart, SPChangeToken changeTokenEnd)
    {
        SPChangeQuery query = new SPChangeQuery(false, false);
        query.ChangeTokenStart = changeTokenStart;
        query.ChangeTokenEnd = changeTokenEnd;

        // Define objects on which changes are to be tracked
        query.SecurityPolicy = true;
        query.User = true;
        query.Group = true;
        query.Web = true;
        query.List = true;

        // Define the type of changes that are to be tracked
        query.RoleAssignmentAdd = true;
        query.RoleAssignmentDelete = true;
        query.RoleDefinitionAdd = true;
        query.RoleDefinitionDelete = true;
        query.RoleDefinitionUpdate = true;
        query.Update = true;
        query.Delete = true;
        query.GroupMembershipAdd = true;
        query.GroupMembershipDelete = true;

        return site.GetChanges(query);
    }

    /// <summary>
    /// Resolves a SharePoint group and create GssPrincipal object containg the expanded list memners users
    /// </summary>
    /// <param name="group"> SharePoint group ID/Name to be resolved </param>
    /// <returns> list of GssPricnicpal object corresponding to the group that was resolved </returns>
    public static List<GssPrincipal> ResolveSPGroup(SPGroup group)
    {
        if (null == group)
        {
            return null;
        }
        List<GssPrincipal> members = new List<GssPrincipal>();
        foreach (SPUser user in group.Users)
        {
            GssPrincipal principal = GetGssPrincipalFromSPPrincipal(user);
            if (null != principal)
            {
                members.Add(principal);
            }
        }
        return members;
    }

    /// <summary>
    /// create a GssPrincipal object from the SharePoint's SPPrincipal object and returns the same
    /// </summary>
    /// <param name="spPrincipal"></param>
    /// <returns></returns>
    public static GssPrincipal GetGssPrincipalFromSPPrincipal(SPPrincipal spPrincipal)
    {
        return GetGssPrincipalFromSPPrincipal(spPrincipal, true);
    }

    /// <summary>
    /// create a GssPrincipal object from the SharePoint's SPPrincipal object and returns the same
    /// </summary>
    /// <param name="spPrincipal">SPPrincipal</param>
    /// <param name="resolveGroup">flag to indicate if SPGroup needs to be resolved</param>
    /// <returns></returns> 
    public static GssPrincipal GetGssPrincipalFromSPPrincipal(SPPrincipal spPrincipal, Boolean resolveGroup)
    {
        if (null == spPrincipal)
        {
            return null;
        }
        GssPrincipal gssPrincipal = null;
        if (spPrincipal is SPUser)
        {
            SPUser user = (SPUser) spPrincipal;
            gssPrincipal = new GssPrincipal(user.LoginName, user.ID);
            if (user.IsDomainGroup)
            {
                gssPrincipal.Type = GssPrincipal.PrincipalType.DOMAINGROUP;
                // in claims mode the resolved groupname will be only sid, get the Name instead
                if (gssPrincipal.Name.ToLower().StartsWith("s-1-5"))
                {
                    gssPrincipal.Name = user.Name;
                }
            }
            else
            {
                gssPrincipal.Type = GssPrincipal.PrincipalType.USER;
            }
        }
        else if (spPrincipal is SPGroup)
        {
            SPGroup group = (SPGroup) spPrincipal;
            gssPrincipal = new GssPrincipal(group.Name, group.ID);
            gssPrincipal.Type = GssPrincipal.PrincipalType.SPGROUP;
            if (resolveGroup)
            {
                gssPrincipal.Members = ResolveSPGroup(group);
            }           
        }
        else
        {
            gssPrincipal = new GssPrincipal(spPrincipal.Name, -2);
            gssPrincipal.AddLogMessage("could not create GssPrincipal for SPSprincipal [ " + spPrincipal.Name + " ] since it's neither a SPGroup nor a SPUser.");
        }

        return gssPrincipal;
    }

    /// <summary>
    /// Creates a GssPrincipal object from the user's login name. The login name must e identifiable in the web application and UrlZone to which the specified site belongs
    /// </summary>
    /// <param name="site">SharePoint Site Collection whose context is to be used for constructing the prinicpal</param>
    /// <param name="login">user login name for which the prinicipal is to be created</param>
    /// <returns></returns>
    public static GssPrincipal GetGssPrincipalForSecPolicyUser(SPSite site, string login)
    {
        if (null == site || null == login)
        {
            return null;
        }
        GssPrincipal gssPrincipal = null;
        SPPrincipalInfo userInfo = null;
        StringBuilder logMessage = new StringBuilder();
        string identity = GssAclUtility.DecodeIdentity(login);
        // ResolvePrincipal is very expensive for deleted users in multidomain environments - check if login is valid first
        // For SP 2007 identity will be same as login name
        // For SP 2010 need to check decoded identity value when using claims authentication
        // For SP 2013 need to check encoded login value when using claims authentication
        if (SPUtility.IsLoginValid(site, identity) 
            || (login != identity && SPUtility.IsLoginValid(site, login)))
        {
            try
            {
                userInfo = SPUtility.ResolvePrincipal(site.WebApplication, SPUrlZone.Default,
                    identity, SPPrincipalType.All, SPPrincipalSource.All, false);
            }
            catch (Exception exResolveDefault)
            {
                logMessage.AppendFormat(
                    "Error resolving principal for User {0} under default site zone: {1}",
                    identity, exResolveDefault.Message);
                if (site.Zone != SPUrlZone.Default)
                {
                    try
                    {
                        // In case of exception, try to resolve under current site zone.
                        userInfo = SPUtility.ResolvePrincipal(site.WebApplication, site.Zone, identity,
                            SPPrincipalType.All, SPPrincipalSource.All, false);
                    }
                    catch (Exception exResolveSite)
                    {
                        // ignore exception and continue processing of other security 
                        // policy principals
                        logMessage.AppendFormat(
                            "Error resolving principal for User {0} for current site zone {1}: {2}",
                            identity, site.Zone, exResolveSite.Message);
                    }
                }
            }
        }
        if (userInfo == null)
        {
            gssPrincipal = new GssPrincipal(identity, -2);
            gssPrincipal.AddLogMessage("[ " + identity + " ( " + login + ") ] could not be resolved to a valid windows principal. ");
            gssPrincipal.AddLogMessage(logMessage.ToString());
            gssPrincipal.Type = GssPrincipal.PrincipalType.NA;
            return gssPrincipal;
        }

        // There is no concept of ID for security policy users. IDs are an offset defined in context of a site collection and policies are defined at web application level
        gssPrincipal = new GssPrincipal(userInfo.LoginName, -2);
        gssPrincipal.AddLogMessage(logMessage.ToString());
        if (userInfo.PrincipalType.Equals(SPPrincipalType.DistributionList) || userInfo.PrincipalType.Equals(SPPrincipalType.SecurityGroup))
        {
            gssPrincipal.Type = GssPrincipal.PrincipalType.DOMAINGROUP;
        }
        // In claims mode the resolved groupname will be only sid, get the DisplayName instead
        if (gssPrincipal.Name.ToLower().StartsWith("s-1-5"))
        {
            gssPrincipal.Name = userInfo.DisplayName;
        }
        return gssPrincipal;
    }

    /// <summary>
    /// Check if the two ISecurable objects are same
    /// </summary>
    /// <param name="secObj1"> First Object </param>
    /// <param name="secObj2"> Second Object </param>
    /// <returns></returns>
    public static bool isSame(ISecurableObject secObj1, ISecurableObject secObj2)
    {
        if (secObj1 is SPWeb && secObj2 is SPWeb)
        {
            SPWeb web1 = (SPWeb)secObj1;
            SPWeb web2 = (SPWeb)secObj2;
            if (null != web1 && null != web2 && web1.ID.Equals(web2.ID))
            {
                return true;
            }
        }
        else if (secObj1 is SPList && secObj2 is SPList)
        {
            SPList list1 = (SPList)secObj1;
            SPList list2 = (SPList)secObj2;
            if (null != list1 && null != list2 && list1.ID.Equals(list2.ID))
            {
                return true;
            }
        }
        else if (secObj1 is SPListItem && secObj2 is SPListItem)
        {
            SPListItem listItem1 = (SPListItem)secObj1;
            SPListItem listItem2 = (SPListItem)secObj2;
            if (null != listItem1 && null != listItem2 && listItem1.ID.Equals(listItem2.ID))
            {
                return true;
            }
        }
        return false;
    }

    /// <summary>
    /// Get admin token, in case of failure return null, which will use current user's token
    /// </summary>
    public static SPUserToken GetAdminToken()
    {
        SPUserToken user = null;
        try {
            user = SPContext.Current.Site.SystemAccount.UserToken;
        } catch (Exception) {
            // this can happen if site collection is readonly, we can't obtain
            // SystemAccount's token, so we return current user's (null)
        }
        return user;
    }
}
