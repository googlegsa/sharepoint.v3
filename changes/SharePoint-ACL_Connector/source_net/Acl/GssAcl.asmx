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
using System.Collections.Generic;
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
    // Name of the prinicpal
    private string name;

    public enum PrincipalType
    {
        USER, DOMAINGROUP, SPGROUP
    }

    private PrincipalType type;

    /// <summary>
    /// Represetns the member users If the current principal is a group
    /// </summary>
    List<GssPrincipal> members;
    StringBuilder logMessage;

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
    public StringBuilder LogMessage
    {
        get { return logMessage; }
        set { logMessage = value; }
    }

    public GssPrincipal()
    {
        Members = new List<GssPrincipal>();
        LogMessage = new StringBuilder();
    }

    public GssPrincipal(string name)
    {
        Name = name;
        Members = new List<GssPrincipal>();
        LogMessage = new StringBuilder();
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

            if (principal.Name.Equals(this.Name) && principal.Type == this.Type)
            {
                return true;
            }
        }
        return false;
    }

    public override int GetHashCode()
    {
        return 13 * (this.Name.GetHashCode() + this.Type.GetHashCode());
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.Append("\n" + logMsg);
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
    // List of allowed permissions
    private SPBasePermissions grantRightMask;

    // List denied permission
    private SPBasePermissions denyRightMask;

    public SPBasePermissions GrantRightMask
    {
        get { return grantRightMask; }
        set { grantRightMask = value; }
    }
    public SPBasePermissions DenyRightMask
    {
        get { return denyRightMask; }
        set { denyRightMask = value; }
    }

    public void UpdatePermission(SPBasePermissions allowedPermissions, SPBasePermissions deniedPermission)
    {
        grantRightMask = grantRightMask | allowedPermissions;
        denyRightMask = denyRightMask | deniedPermission;
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

    public GssAce()
    {
        principal = new GssPrincipal();
    }

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
            if(null != this.Principal && null != ace && null != ace.Principal
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

    // Author/Owner of the document. This is an added info returned along with the ACL. This could be relevant to the clients becasue the owner's value as returned by the available SharePoint web servcies are not LDAP fromat.
    private string owner;

    // List of all the ACEs
    private List<GssAce> allAce;

    private StringBuilder logMessage;

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
    public StringBuilder LogMessage
    {
        get { return logMessage; }
        set { logMessage = value; }
    }

    public GssAcl()
    {
        this.allAce = new List<GssAce>();
        logMessage = new StringBuilder();
    }

    public GssAcl(string entityUrl, int count)
    {
        this.entityUrl = entityUrl;
        this.allAce = new List<GssAce>(count);
        logMessage = new StringBuilder();
    }

    public void AddAce(GssAce ace)
    {
        allAce.Add(ace);
    }

    public void AddLogMessage(string logMsg)
    {
        logMessage.Append("\n" + logMsg);
    }
}

/// <summary>
/// Represents a single ACL specific change in SharePoint
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public class GssAclChange
{
    // Type of the Object/Entity which has changed
    public enum ObjectType
    {
        NA,
        SECURITY_POLICY,
        ADMINISTRATORS,
        GROUP,
        USER,
        WEB,
        LIST,
        ITEM
    }

    private ObjectType changedObject;

    // Type of change
    private SPChangeType changeType;

    // An additional hint to identify the exact object/entity that has changed. Most of the time, this would be the ID, GUID or URL.
    private string hint;

    public ObjectType ChangedObject
    {
        get { return changedObject; }
        set { changedObject = value; }
    }

    public SPChangeType ChangeType
    {
        get {return changeType; }
        set { changeType = value; }
    }

    public string Hint
    {
        get { return hint; }
        set { hint = value; }
    }

    public GssAclChange() { }

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
    private StringBuilder logMessage;

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

    public StringBuilder LogMessage
    {
        get { return logMessage; }
        set { logMessage = value; }
    }

    public GssAclChangeCollection()
    {
        Changes = new List<GssAclChange>();
        LogMessage = new StringBuilder();
    }

    public GssAclChangeCollection(SPChangeToken changeToken)
    {
        if (null != changeToken)
        {
            ChangeToken = changeToken.ToString();
        }
        Changes = new List<GssAclChange>();
        LogMessage = new StringBuilder();
    }

    public void AddChange(GssAclChange change)
    {
        changes.Add(change);
    }

    /// <summary>
    /// Construct an appropriare <see cref="GssAclChnage"/> object from a SharePoint's SPChange object and adds it to the list of changes
    /// </summary>
    /// <param name="change"> SharePoint's change object. This may not necessarily be a ACL related change. </param>
    public void AddChange(SPChange change)
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
                    // Instead of sending the ID of the web that has changed, one may consider sending the acrual LIst IDs that should be re-crawled. This is becasue,
                    // connector does the actual document discovery per list level. Sending the changed webId will force the connector to make an extra call to get the Lists that should be re-crawled.
                    // But, such implementationn will become confusing when the connector will evolve in future to support site colection and web application level crawling.
                    // It's better to send the change web ID as hint and let the connector decide how to work on this.
                    AddChange(new GssAclChange(GssAclChange.ObjectType.WEB, changeWeb.ChangeType, changeWeb.Id.ToString()));
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
                    AddChange(new GssAclChange(GssAclChange.ObjectType.LIST, changeList.ChangeType, changeList.Id.ToString()));
                    break;
            }
        }
        else if (change is SPChangeUser)
        {
            SPChangeUser changeUser = (SPChangeUser)change;
            if (changeUser.IsSiteAdminChange)
            {
                AddChange(new GssAclChange(GssAclChange.ObjectType.ADMINISTRATORS, changeUser.ChangeType, "" + changeUser.Id));
            }
            else if (changeUser.ChangeType == SPChangeType.Delete)
            {
                AddChange(new GssAclChange(GssAclChange.ObjectType.USER, changeUser.ChangeType, "" + changeUser.Id));
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
                    AddChange(new GssAclChange(GssAclChange.ObjectType.GROUP, changeGroup.ChangeType, "" + changeGroup.Id));
                    break;
            }
        }
        else if (change is SPChangeSecurityPolicy)
        {
            SPChangeSecurityPolicy changeSecurityPolicy = (SPChangeSecurityPolicy)change;
            AddChange(new GssAclChange(GssAclChange.ObjectType.SECURITY_POLICY, changeSecurityPolicy.ChangeType, ""));
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
        logMessage.Append("\n" + logMsg);
    }
}

/// <summary>
/// Representts a basic response object containing minimal information and that can be used by all other web methods
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
[Serializable]
public abstract class GssAclBaseResult
{
    private string siteCollectionUrl;
    private Guid siteCollectionGuid;

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
/// Provides alll the necessary web methods exposed by the Web Service
/// </summary>
[WebService(Namespace = "gssAcl.generated.sharepoint.connector.enterprise.google.com")]
[WebServiceBinding(ConformsTo = WsiProfiles.BasicProfile1_1)]
public class GssAclMonitor
{
    GssAclUtility gssUtil = new GssAclUtility();
    // Current site collection in which request os to be served
    SPSite site;

    // SharePoint site used for consructing the web servcie endpoint
    SPWeb web;

    public GssAclMonitor()
    {
        SPContext spContext = SPContext.Current;
        site = spContext.Site;
        web = site.OpenWeb();
    }

    ~GssAclMonitor()
    {
        web.Dispose();
        site.Dispose();
    }

    /// <summary>
    /// Returns ACLs of a set of entities which belongs to a single SharePoint web site. The SharePoint site is identified by the SPContext in which thwe request is being served.
    /// </summary>
    /// <param name="urls"> Entity URLs whose ACLs are to be returned </param>
    /// <returns> List Of ACLs corresponding to the entity URLs </returns>
    [WebMethod]
    public GssGetAclForUrlsResult GetAclForUrls(string[] urls)
    {
        if (null == site || null == web)
        {
            throw new Exception("SharePoint site not identified");
        }

        List<GssAcl> allAcls = new List<GssAcl>();

        Dictionary<GssPrincipal, GssSharepointPermission> commonAceMap = new Dictionary<GssPrincipal, GssSharepointPermission>();
        try
        {
            gssUtil.TrackSecurityPolicyForAcl(site.WebApplication, commonAceMap);
        }
        catch (Exception e)
        {
            gssUtil.AddLogMessage("Problem while processing security policies. Exception [" + e.Message + " ] ");
        }

        try
        {
            gssUtil.TrackSiteAdminsForAcl(web, commonAceMap);
        }
        catch (Exception e)
        {
            gssUtil.AddLogMessage("Problem while processing site collection admins. Exception [" + e.Message + " ] ");
        }

        foreach (string url in urls)
        {
            GssAcl acl = null;
            try
            {
                Dictionary<GssPrincipal, GssSharepointPermission> aceMap = new Dictionary<GssPrincipal, GssSharepointPermission>(commonAceMap);
                ISecurableObject secobj = gssUtil.IdentifyObject(url, web);
                gssUtil.TrackRoleAssignmentsForAcl(secobj.RoleAssignments, aceMap);
                acl = new GssAcl(url, aceMap.Count);
                foreach (KeyValuePair<GssPrincipal, GssSharepointPermission> keyVal in aceMap)
                {
                    acl.AddAce(new GssAce(keyVal.Key, keyVal.Value));
                }
                allAcls.Add(acl);

                SPUser owner = gssUtil.GetOwner(secobj);
                if (null != owner)
                {
                    acl.Owner = owner.LoginName;
                }
            }
            catch (Exception e)
            {
                acl = new GssAcl();
                acl.AddLogMessage("Problem while processing role assignments. Exception [" + e.Message + " ] ");
            }

            acl.AddLogMessage(gssUtil.CommonLogMessage.ToString());
        }

        GssGetAclForUrlsResult result = new GssGetAclForUrlsResult();
        result.AllAcls = allAcls;
        result.SiteCollectionUrl = site.Url;
        result.SiteCollectionGuid = site.ID;
        return result;
    }

    /// <summary>
    /// Returns a list of ACL specific changes that have happened over a period of time, determined by the change token.
    /// These changes purely reflects the actions performed on the SharePoint but does not talk about their implications. The caller should not assume that the ACL of any entity has changed just becasue it recieves a set of changes from this API.
    /// A typical exampke could be when the permission hierarchy of a list/web is reset and immediately brought to its orignial state. In that case this API will return two changes but there is no ACL change on the SharePoint.
    /// Deletion of an empty group is another such case.
    /// </summary>
    /// <param name="strChangeToken"> The change tokenfrom where the changes are to be scanned in the SharePoint's change log </param>
    /// <returns> a list of ACL spefic changes </returns>
    [WebMethod]
    public GssGetAclChangesSinceTokenResult GetAclChangesSinceToken(string strChangeToken)
    {
        if (null == site || null == web)
        {
            throw new Exception("SharePoint site not identified");
        }
        SPChangeToken changeToken = new SPChangeToken(strChangeToken);
        GssAclChangeCollection allChanges = new GssAclChangeCollection(changeToken);
        try
        {
            SPChangeCollection spChanges = gssUtil.TrackAclChanges(site, changeToken);
            foreach (SPChange change in spChanges)
            {
                allChanges.AddChange(change);
            }
            // There are two ways to get the next Change Token value that should be used for synchronization. 1) Get the last change token available for the site
            // 2) Get the last change token corresponding to which changes have been tracked.
            // The problem with the second approach is that if no ACL specific changes will occur, the change token will never gets updated and will becaome invalid after some time.
            // Another performance issue is is that, the scan wil always start form the same token unless there is a ACL specific change.
            // Since, the change tracking logic ensures that all changes will be tracked (i.e there is no rowlimit kind of thing associated), it is safe to use the first approach.
            allChanges.UpdateChangeToken(site.CurrentChangeToken);
        }
        catch (Exception e)
        {
            // All the changes should be processed as one atomic operation. If any one fails, all should be ignored. This is in lieu of maintaing a single change token which will be used for executing change queries.
            allChanges = new GssAclChangeCollection(changeToken);
            gssUtil.AddLogMessage("Exception occured while change detection, Exception [ " + e.Message + " ] ");
        }

        GssGetAclChangesSinceTokenResult result = new GssGetAclChangesSinceTokenResult();
        result.AllChanges = allChanges;
        result.SiteCollectionUrl = site.Url;
        result.SiteCollectionGuid = site.ID;
        return result;
    }

    /// <summary>
    /// Exapnds a SharePoint group to find all the member users and domain groups. Creates a <see cref="GssPrincipal"/> object for each of them and returns the same.
    /// The group must exist in the site collection for which the request has been sent.
    /// </summary>
    /// <param name="groupId"> the SharePoint group ID/Name that is to be resolved </param>
    /// <returns> list of GssPrincipal object with the Members attribute set as the list of member users/domain-groups</returns>
    [WebMethod]
    public GssResolveSPGroupResult ResolveSPGroup(string[] groupId)
    {
        if (null == site || null == web)
        {
            throw new Exception("SharePoint site not found! ");
        }

        List<GssPrincipal> prinicpals = new List<GssPrincipal>();
        if (null != groupId)
        {
            foreach (string id in groupId)
            {
                GssPrincipal principal = new GssPrincipal();
                SPGroup spGroup = web.Groups[id];
                if (null == spGroup)
                {
                    principal.AddLogMessage("Could not resolve Group Id [ " + id + " ] ");
                }
                else
                {
                    principal.Members = gssUtil.ResolveSPGroup(spGroup);
                }
                prinicpals.Add(principal);
            }
        }

        GssResolveSPGroupResult result = new GssResolveSPGroupResult();
        result.Prinicpals = prinicpals;
        result.SiteCollectionUrl = site.Url;
        result.SiteCollectionGuid = site.ID;
        return result;
    }

    /// <summary>
    /// Returns the GUIDs of all those Lists which are inheriting their permissions from the passed in SharePoint web site.
    /// </summary>
    /// <param name="webGuId"> GUID of the SharePoint web site from which the Lists are to be returned </param>
    /// <returns> list of GUIDs of the lists </returns>
    [WebMethod]
    public List<string> GetAffectedListIDsForChangeWeb(string webGuId)
    {
        if (null == site || null == web)
        {
            throw new Exception("SharePoint site not found! ");
        }

        SPWeb changeWeb = null;
        try
        {
            changeWeb = site.AllWebs[new Guid(webGuId)];
            if (null == changeWeb)
            {
                throw new Exception("Passed in webId [ " + webGuId + " ] does not exist in the current SharePoint site context");
            }
        }
        catch (Exception e)
        {
            throw new Exception("Passed in webId [ " + webGuId + " ] does not exist in the current SharePoint site context");
        }

        List<string> listIDs = new List<string>();
        SPListCollection lists = changeWeb.Lists;
        foreach (SPList list in lists)
        {
            if (!list.HasUniqueRoleAssignments)
            {
                listIDs.Add(list.ID.ToString());
            }
        }
        return listIDs;
    }

    /// <summary>
    /// Returns the numeric IDs of all those ListItems which are inheriting their permissions from the passed in SharePoint List.
    /// </summary>
    /// <param name="webGuId"> GUID of the SharePoint List from which the Items are to be returned. The list must belong to the the site in which the request has been sent </param>
    /// <returns> list of IDs of the items </returns>
    [WebMethod]
    public List<string> GetAffectedItemIDsForChangeList(string listGuId)
    {
        if (null == site || null == web)
        {
            throw new Exception("SharePoint site not found! ");
        }

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

        List<string> itemIDs = new List<string>();
        SPListItemCollection items = changeList.Items;
        foreach (SPListItem item in items)
        {
            if (!item.HasUniqueRoleAssignments)
            {
                itemIDs.Add("" + item.ID);
            }
        }
        return itemIDs;
    }
}

/// <summary>
/// Provides general purpose utility methods
/// </summary>
public class GssAclUtility
{
    StringBuilder commonLogMessage = new StringBuilder();
    public StringBuilder CommonLogMessage
    {
        get { return commonLogMessage; }
        set { commonLogMessage = value; }
    }

    public void AddLogMessage(string logMsg)
    {
        CommonLogMessage.Append("\n" + logMsg);
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from the web application security policies
    /// </summary>
    /// <param name="webApp"> web application from which the security policies are to be rtracked </param>
    /// <param name="userAceMap"> ACE map to be updated </param>
    public void TrackSecurityPolicyForAcl(SPWebApplication webApp, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        SPPolicyCollection policies = webApp.Policies;
        foreach (SPPolicy policy in policies)
        {
            GssPrincipal principal = GetGssPrincipalFromLogin(webApp, policy.UserName);
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
                permission.UpdatePermission(policyRole.GrantRightsMask, policyRole.DenyRightsMask);
            }
        }
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from the site collection administrators list
    /// </summary>
    /// <param name="web"> SharePoint web site whose administrators are to be tracked </param>
    /// <param name="userAceMap"> ACE Map to be updated </param>
    public void TrackSiteAdminsForAcl(SPWeb web, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        foreach (SPPrincipal spPrincipal in web.SiteAdministrators)
        {
            GssPrincipal principal = GetGssPrincipalFromSPPrincipal(spPrincipal);
            GssSharepointPermission permission = null;

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
                aceMap.Add(principal, permission);
            }
            // Administrators have Full Rights in the site collection.
            permission.UpdatePermission(SPBasePermissions.FullMask, SPBasePermissions.EmptyMask);
        }
    }

    /// <summary>
    /// Update the incoming ACE Map with the users,permissions identified from a list of role assignments
    /// </summary>
    /// <param name="roles"> list of role assignments </param>
    /// <param name="userAceMap"> ACE Map to be updated </param>
    public void TrackRoleAssignmentsForAcl(SPRoleAssignmentCollection roles, Dictionary<GssPrincipal, GssSharepointPermission> aceMap)
    {
        foreach (SPRoleAssignment roleAssg in roles)
        {
            GssPrincipal principal = GetGssPrincipalFromSPPrincipal(roleAssg.Member);
            GssSharepointPermission permission = null;

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
                aceMap.Add(principal, permission);
            }

            foreach (SPRoleDefinition roledef in roleAssg.RoleDefinitionBindings)
            {
                permission.UpdatePermission(roledef.BasePermissions, SPBasePermissions.EmptyMask);
            }
        }
    }

    /// <summary>
    /// Identifies the SharePoint object represented by the incoming URL and returns a corresponding ISecurable object for same
    /// </summary>
    /// <param name="url"> Entity URL</param>
    /// <param name="web"> Parent Web to which the entity URL belongs </param>
    /// <returns></returns>
    public ISecurableObject IdentifyObject(string url, SPWeb web)
    {
        SPListItem listItem = web.GetListItem(url);
        if (null != listItem)
        {
            return listItem;
        }

        SPList list = web.GetList(url);
        if (null != list)
        {
            try
            {
                Uri uri = new Uri(url);
                string query = uri.Query;
                string id = HttpUtility.ParseQueryString(query).Get("ID");
                listItem = list.GetItemById(int.Parse(id));
                return listItem;
            }
            catch (Exception e)
            {
                return list;
            }
        }

        return web.Site.OpenWeb(url);
    }

    /// <summary>
    /// Retrieves the Owner's information about a given ISecurable entity
    /// </summary>
    /// <param name="secobj"></param>
    /// <returns></returns>
    public SPUser GetOwner(ISecurableObject secobj)
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
                try
                {
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
                catch (Exception e)
                {
                    AddLogMessage("Failed to detect Owner for the list item [ " + item.Url + " ] ");
                }
            }
        }
        else if (secobj is SPWeb)
        {
            owner = ((SPWeb)secobj).Author;
        }
        else
        {
            AddLogMessage("Failed to detect Owner becasue the entity is neither a listitem, list or a web. ");
        }
        return owner;
    }

    /// <summary>
    /// Tracks the list of ACL related changes that have happened on the SharePoint site (to which the request has been sent) from a given point of time, determined by the change token value.
    /// These list of changes are not guaranteed to affect the ACL of every or a even a single entity in the SharePoint. Rather, it purely reflects what action has been performed.
    /// Caller should analyse the implications of these changes and work accordingly.
    ///
    /// This method does not currently supports Item level tracking. This is not required becasue getListItemChangesSinceToken which connector already usees already does the same.
    /// This makes the web service implementation tightly coupled with the connector though, we can live with this limitation for now as the web service, for now, is to be used by the connector only.
    /// </summary>
    /// <param name="site"> The Site collection in which the changes are to be tracked </param>
    /// <param name="fromChangeToken"> The starting change token value from where the changes are to be scanned in the SharePoint's change log </param>
    /// <returns> list of changes that most likely expected to change the ACLs of the entities </returns>
    public SPChangeCollection TrackAclChanges(SPSite site, SPChangeToken fromChangeToken)
    {
        SPChangeQuery query = new SPChangeQuery(false, false);
        query.ChangeTokenStart = fromChangeToken;

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
    public List<GssPrincipal> ResolveSPGroup(SPGroup group)
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
    public GssPrincipal GetGssPrincipalFromSPPrincipal(SPPrincipal spPrincipal)
    {
        if (null == spPrincipal)
        {
            return null;
        }
        GssPrincipal gssPrincipal = null;
        if (spPrincipal is SPUser)
        {
            SPUser user = (SPUser)spPrincipal;
            gssPrincipal = new GssPrincipal(user.LoginName);
            if (user.IsDomainGroup)
            {
                gssPrincipal.Type = GssPrincipal.PrincipalType.DOMAINGROUP;
            }
        }
        else if (spPrincipal is SPGroup)
        {
            SPGroup group = (SPGroup)spPrincipal;
            gssPrincipal = new GssPrincipal(group.Name);
            gssPrincipal.Type = GssPrincipal.PrincipalType.SPGROUP;
            gssPrincipal.Members = ResolveSPGroup(group);
        }
        else
        {
            gssPrincipal = new GssPrincipal();
            gssPrincipal.AddLogMessage("could not create GssPrincipal for SPSprincipal [ " + spPrincipal.Name + " ] since it's neither a SPGroup nor a SPUser. ");
        }

        return gssPrincipal;
    }

    /// <summary>
    /// Creates a GssPrincipal object from the user's login name. The login name must e identifiable in the passed in web application
    /// </summary>
    /// <param name="webApp"></param>
    /// <param name="login"></param>
    /// <returns></returns>
    public GssPrincipal GetGssPrincipalFromLogin(SPWebApplication webApp, string login)
    {
        if (null == webApp || null == login)
        {
            return null;
        }
        GssPrincipal gssPrincipal = null;
        SPPrincipalInfo userInfo = SPUtility.ResolveWindowsPrincipal(webApp, login, SPPrincipalType.All, false);
        if (null == userInfo)
        {
            gssPrincipal = new GssPrincipal();
            gssPrincipal.AddLogMessage("could not create GssPrincipal for login [ " + login + " ] since this login  can not be resolved to a valid windows principal. ");
            return gssPrincipal;
        }

        gssPrincipal = new GssPrincipal(userInfo.LoginName);

        if (userInfo.PrincipalType.Equals(SPPrincipalType.DistributionList) || userInfo.PrincipalType.Equals(SPPrincipalType.SecurityGroup))
        {
            gssPrincipal.Type = GssPrincipal.PrincipalType.DOMAINGROUP;
        }
        return gssPrincipal;
    }
}