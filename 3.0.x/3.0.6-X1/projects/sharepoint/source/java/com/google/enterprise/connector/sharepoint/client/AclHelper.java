// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.enterprise.connector.sharepoint.client.ListsHelper;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChangeCollection;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission;
import com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType;
import com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType;
import com.google.enterprise.connector.sharepoint.generated.gssacl.SPChangeType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocumentList;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;
import com.google.enterprise.connector.spi.SpiConstants.RoleType;

import org.apache.axis.AxisFault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling GssAcl.asmx web service. Provides a layer to talk to
 * the ACL Web Service on the SharePoint server. Any call to this Web Service
 * must go through this layer.
 *
 * @author nitendra_thakur
 */
public class AclHelper {
  private final Logger LOGGER = Logger.getLogger(AclHelper.class.getName());
  private SharepointClientContext sharepointClientContext = null;
  private boolean supportsInheritedAcls = false;
  private boolean supportsDenyAcls = false;
  private final AclWS aclWS;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @param siteurl siteurl to be used for constructing endpoints. If null, site
   *          url is taken from SharepointClientContext
   * @throws SharepointException
   */
  public AclHelper(final SharepointClientContext inSharepointClientContext,
      String siteurl) throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null ");
    }
    sharepointClientContext = inSharepointClientContext;
    if (null == siteurl) {
      siteurl = sharepointClientContext.getSiteURL();
    }
    aclWS = sharepointClientContext.getClientFactory().getAclWS(
        sharepointClientContext, siteurl);

    if (!sharepointClientContext.isPushAcls()) {
      return;
    }
    if (null == siteurl) {
      siteurl = sharepointClientContext.getSiteURL();
    }
    if (null != sharepointClientContext.getTraversalContext()) {
      supportsInheritedAcls = 
          sharepointClientContext.getTraversalContext().supportsInheritedAcls();
      supportsDenyAcls = 
          sharepointClientContext.getTraversalContext().supportsDenyAcls();
    }
    LOGGER.log(Level.CONFIG, "Supports ACL " + supportsInheritedAcls);

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();
    final int timeout = sharepointClientContext.getWebServiceTimeOut();
    LOGGER.fine("Setting time-out to " + timeout + " milliseconds.");

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    aclWS.setUsername(strUser);
    aclWS.setPassword(strPassword);
    aclWS.setTimeout(timeout);
  }

  /**
   * Executes GetAclForUrls() web method of GssAcl web service. Used to get the
   * ACL of a set of entities.
   *
   * @param urls Set of entity URLs whose ACLs are to be fetched
   * @param useInheritance flag indicating use of ACL inheritance
   * @param includePolicyAcls flag indicating if policy ACLs needs
   *  to be part of document ACLs. With Inheritance support this value should
   *  be false.
   * @return web service response {@link GssGetAclForUrlsResult} as it is
   */
  private GssGetAclForUrlsResult getAclForUrls(final String[] urls,
      final boolean useInheritance, final boolean includePolicyAcls) {
    if (null == urls || urls.length == 0) {
      return null;
    }

    return Util.makeWSRequest(sharepointClientContext, aclWS,
        new Util.RequestExecutor<GssGetAclForUrlsResult>() {
      public GssGetAclForUrlsResult onRequest(final BaseWS ws)
          throws Throwable {
        return ((AclWS) ws).getAclForUrlsUsingInheritance(urls, 
            useInheritance, includePolicyAcls, 
            sharepointClientContext.getLargeACLThreshold(),
            sharepointClientContext.getFeedType().equals(
                FeedType.METADATA_URL_FEED));
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Call to getAclForUrls failed.", e);
      }
    });
  }

  /**
   * This method will attach inherited ACLs to the document since parent
   *  can not be crawled as per includedUrls
   * @param urls Urls to crawl
   * @param urlToDocMap Document map to update
   */
  private void fetchAclForDocumentsWithExcludedParents(String[] urls,
      Map<String, SPDocument> urlToDocMap, WebState webState) {
    GssGetAclForUrlsResult wsResult = getAclForUrls(urls, false, false);
    if (wsResult != null) {
      processWsResponse(wsResult, urlToDocMap, webState);
    }
  }
  
  /**
   * To process documents with large ACLs
   */
  private void fetchAclForSPDocument(String documentUrl,
      SPDocument document, WebState webState) {    
    GssGetAclForUrlsResult wsResult =
        getAclForUrls(new String[] { documentUrl },
        supportsInheritedAcls, !supportsInheritedAcls);
    if (wsResult != null) {
      Map<String, SPDocument> docMapToPass = Maps.newHashMap();
      docMapToPass.put(documentUrl, document);
      processWsResponse(wsResult, docMapToPass, webState);
    }
  }

  /**
   * Used to parse the response of {@link #getAclForUrls getAclForUrls} and
   * update the ACLs into the {@link SPDocument} The set of document objects
   * must be passed in form of a map with their URLs as keys. If a user has
   * more than one permission assigned on SHarePoint, connector will include
   * each of them in the ACE. Hence, the {@link RoleType} that is sent to CM may
   * include a list of role types.
   *
   * @param wsResult Web Service response to be parsed
   * @param urlToDocMap Documents whose ACLs are to be set. The keys in the map
   *          represents the document URL
   */
  private void processWsResponse(GssGetAclForUrlsResult wsResult,
      Map<String, SPDocument> urlToDocMap, WebState webState) {
    if (wsResult == null || urlToDocMap == null) {
      return;
    }
    LOGGER.log(Level.CONFIG, "Building ACLs from the WS response. WSLog [ "
        + wsResult.getLogMessage() + " ]");
    GssAcl[] allAcls = wsResult.getAllAcls();
    if (allAcls == null || allAcls.length == 0) {
      return;
    }

    Set<UserGroupMembership> memberships = new TreeSet<UserGroupMembership>();
    Map<String, SPDocument> excludedParentUrlToDocMap = Maps.newHashMap();
    List<String> docUrlsToReprocess = Lists.newArrayList();
    Map<String, SPDocument> largeACLUrlToDocMap = Maps.newHashMap();
    List<String> largeAclUrlsToReprocess = Lists.newArrayList();
    Map<String,List<SPDocument>> reprocessDocs =
        Maps.newHashMap();

 ACL: for (GssAcl acl : allAcls) {
      String entityUrl = acl.getEntityUrl();
      GssAce[] allAces = acl.getAllAce();
      if (null == entityUrl || null == allAces) {
        LOGGER.log(Level.WARNING, "Either entityUrl [ " + entityUrl
            + " ] is unavailable or No ACE found in the ACL. WSLog [ "
            + acl.getLogMessage() + " ] ");
        continue;
      }
      SPDocument document = urlToDocMap.get(entityUrl);
      if (document == null) {
        LOGGER.warning(
            "No document found in urlToDocMap map for the entityUrl [ "
            + entityUrl + " ], WSLog [ " + acl.getLogMessage() + " ] ");
        continue;
      }
      LOGGER.log(Level.CONFIG, "WsLog [ " + acl.getLogMessage() + " ] ");
      boolean allowAnonymousAccess = Boolean.parseBoolean(
          acl.getAnonymousAccess());
      if (allowAnonymousAccess) {
        LOGGER.log(Level.INFO, "Document [ " + document.getUrl()
            + " ] is identified as Public Document");
       document.setPublicDocument(allowAnonymousAccess);
       continue;
      }
      boolean largeAcl = Boolean.parseBoolean(acl.getLargeAcl());
      if (largeAcl) {
        boolean inheritPermissions =
            Boolean.parseBoolean(acl.getInheritPermissions());
        if (inheritPermissions) {
          String parentUrl = acl.getParentUrl();
          // if parentUrl is null or empty then document will be processed
          // as largeAcl document.
          if (!Strings.isNullOrEmpty(parentUrl)) {
            LOGGER.log(Level.INFO, "Document [ " + document.getUrl()
                + " ] is idenified as Large ACL but with inherit permissions, "
                + "with Parent URL as " + parentUrl);
            List<SPDocument> childList =
                reprocessDocs.get(parentUrl);
            if (childList == null) {
              childList = Lists.newArrayList();              
            }
            childList.add(document);
            reprocessDocs.put(parentUrl, childList);
            continue ACL;
          }          
        }
        LOGGER.log(Level.INFO, "Document [ " + document.getUrl()
            + " ] needs to be reprocessed as Large ACL Document");
        largeAclUrlsToReprocess.add(document.getUrl());
        largeACLUrlToDocMap.put(document.getUrl(), document);
        continue ACL;
      }
      Map<Principal, Set<RoleType>> userPermissionMap = Maps.newHashMap();
      Map<Principal, Set<RoleType>> groupPermissionMap = Maps.newHashMap();
      Map<Principal, Set<RoleType>> deniedUserPermissionMap = Maps.newHashMap();
      Map<Principal, Set<RoleType>> deniedGroupPermissionMap =
          Maps.newHashMap();
      document.setUniquePermissions(
          !Boolean.parseBoolean(acl.getInheritPermissions()));
      if (!Strings.isNullOrEmpty(acl.getParentUrl())) {
        if (sharepointClientContext.isIncludedUrl(acl.getParentUrl())) {
          document.setParentUrl(acl.getParentUrl());
          document.setParentId(acl.getParentId());
        } else {
          if (document.isUniquePermissions()) {
            document.setParentUrl(sharepointClientContext.getSiteURL());
            document.setParentId(acl.getParentId());
          } else {
            LOGGER.log(Level.INFO, "Document [ " +document.getUrl()
                + " ] needs to be reprocessed as Parent Url ["
                + acl.getParentUrl() + "] is not inluded for Traversal");
            docUrlsToReprocess.add(document.getUrl());
            excludedParentUrlToDocMap.put(document.getUrl(), document);
            continue ACL;
          }
        }
      }
      for (GssAce ace : allAces) {
        // Handle Principal
        GssPrincipal principal = ace.getPrincipal();
        if (null == principal) {
          LOGGER.log(Level.WARNING, "No Principal found in ace.");
          continue;
        }
        if (null == principal.getType() || null == principal.getName()) {
          LOGGER.log(Level.WARNING, "Either Principal Name [ "
              + principal.getName() + " ] or Principal Type [ "
              + principal.getType() + " ]  is unavailable");
          continue;
        }

        // Handle Permissions
        GssSharepointPermission permissions = ace.getPermission();
        if (null == permissions) {
          LOGGER.log(Level.WARNING, "No permissions found for Principal [ "
              + principal.getName() + " ] ");
          continue;
        }
        // Check to determine whether the object-type of the document is list
        // list-item or site.

        ObjectType objectType = ObjectType.ITEM;

        if (document.getObjType().equals(SPConstants.SITE)) {
          objectType = ObjectType.SITE_LANDING_PAGE;
        } else if (null != document.getParentList()) {
          if (document.getParentList().getPrimaryKey().equals(
              Util.getOriginalDocId(document.getDocId(),
                  document.getFeedType()))) {
            objectType = ObjectType.LIST;
          }
        }
        final String principalName = getPrincipalName(principal);
        String siteCollUrl = wsResult.getSiteCollectionUrl();
        String[] deniedPermissions = permissions.getDeniedPermission();
        if (null != deniedPermissions) {
          Set<RoleType> deniedRoleTypes =
              Util.getRoleTypesFor(deniedPermissions, objectType);
          if (null != deniedRoleTypes && deniedRoleTypes.size() > 0) {
            LOGGER.fine("Denied Permission list "
                + Arrays.asList(permissions.getDeniedPermission())
                + " for the User " + principalName);
            LOGGER.fine("Principal [" + principalName
                + "] Denied Role Types [ " + deniedRoleTypes + " ]");
            //Pass denied permissions only if Reader role is denied.
            if (deniedRoleTypes.contains(RoleType.READER)) {
              if (supportsDenyAcls) {
                LOGGER.fine("Processing Deny permissions"
                    + " for Principal ["+ principalName + "]");
                processPermissions(principal, deniedRoleTypes,
                    deniedUserPermissionMap, deniedGroupPermissionMap,
                    principalName, siteCollUrl, memberships,
                    webState);
              } else {
                // Skipping ACL as denied ACLs are not supported as per
                // Traversal Context.
                LOGGER.warning("Skipping ACL as Deny permissions are detected"
                    + "for Document [" + entityUrl + "] for Principal ["
                    + principalName + " ] when Supports Deny ACL ["
                    + supportsDenyAcls + "].");
                continue ACL;
              }
            }
          }
        }
        LOGGER.fine("Permission list "
            + Arrays.asList(permissions.getAllowedPermissions())
            + " for the User " + principalName);
        Set<RoleType> allowedRoleTypes = Util.getRoleTypesFor(
            permissions.getAllowedPermissions(), objectType);
        if (allowedRoleTypes != null && !allowedRoleTypes.isEmpty()) {
          LOGGER.fine("Principal [ "+ principalName
              + " ] Allowed Role Types [ "+ allowedRoleTypes + " ]");
          processPermissions(principal, allowedRoleTypes, userPermissionMap,
              groupPermissionMap, principalName, siteCollUrl, memberships,
              webState);
        }
      }
      document.setUsersAclMap(userPermissionMap);
      document.setGroupsAclMap(groupPermissionMap);
      document.setDenyUsersAclMap(deniedUserPermissionMap);
      document.setDenyGroupsAclMap(deniedGroupPermissionMap);
    }

    if (!reprocessDocs.isEmpty()) {
      for (String parentUrl : reprocessDocs.keySet()) {
        LOGGER.fine("Processing Parent URL [ "+ parentUrl + " ] ");
        SPDocument parent = new SPDocument(parentUrl, parentUrl,
            Calendar.getInstance(), ActionType.ADD);
        fetchAclForSPDocument(parentUrl, parent, webState);
        List<SPDocument> childList = reprocessDocs.get(parentUrl);
        if (childList != null) { 
          for(SPDocument child : childList) {
            copyAcls(parent, child);
          }
        }
      }
    }

    if (!largeAclUrlsToReprocess.isEmpty()) {
      for (String largeACLUrl : largeAclUrlsToReprocess) {
        SPDocument documentToPass = largeACLUrlToDocMap.get(largeACLUrl);
        if (documentToPass != null) {
          fetchAclForSPDocument(largeACLUrl, documentToPass, webState);
        }
      }
    }

    if (!docUrlsToReprocess.isEmpty()) {
      String[] arrToPass = new String[docUrlsToReprocess.size()];
      docUrlsToReprocess.toArray(arrToPass);
      fetchAclForDocumentsWithExcludedParents(arrToPass,
          excludedParentUrlToDocMap, webState);
    }

    if (null != sharepointClientContext.getUserDataStoreDAO()) {
      try {
        sharepointClientContext.getUserDataStoreDAO().addMemberships(
            memberships);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to add #" + memberships.size()
            + " memberships in user data store. ", e);
      }
    }
  }
  
  /**
   * Utility method to copy ACLs.
   * @param source source SPDocument for copy
   * @param target target SPDocument for copy
   */
  private void copyAcls(SPDocument source, SPDocument target) {
    target.setUsersAclMap(source.getUsersAclMap());
    target.setGroupsAclMap(source.getGroupsAclMap());
    target.setDenyUsersAclMap(source.getDenyUsersAclMap());
    target.setDenyGroupsAclMap(source.getDenyGroupsAclMap());
  } 

  /**
   * Method to process GssAcl permissions.
   *
   * @param principal GsssPrincipal Object to process.
   * @param roleTypes  Allowed / denied RoleTypes.
   * @param userPermissionMap Permissions Map to add user permissions.
   * @param groupPermissionMap Permissions Map to add group permissions.
   * @param principalName Principal Name
   * @param webStateUrl Site Collection Url from WebState
   * @param memberships UserGroup Membership object
   */
  private void processPermissions(GssPrincipal principal,
      Set<RoleType> roleTypes, Map<Principal, Set<RoleType>> userPermissionMap,
      Map<Principal, Set<RoleType>> groupPermissionMap, String principalName,
      String webStateUrl, Set<UserGroupMembership> memberships,
      WebState webState) {
    String globalNamespace = sharepointClientContext.getGoogleGlobalNamespace();
    String localNamespace = sharepointClientContext.getGoogleLocalNamespace();
    if (PrincipalType.USER.equals(principal.getType())) {
      userPermissionMap.put(new Principal(SpiConstants.PrincipalType.UNKNOWN,
              globalNamespace, principalName,
              CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE), roleTypes);
    } else if (PrincipalType.DOMAINGROUP.equals(principal.getType())) {
      groupPermissionMap.put(new Principal(SpiConstants.PrincipalType.UNKNOWN,
              globalNamespace, principalName,
              CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE), roleTypes);
    } else if (PrincipalType.SPGROUP.equals(principal.getType())) {
      groupPermissionMap.put(
          new Principal(SpiConstants.PrincipalType.UNQUALIFIED, localNamespace,
              "[" + webStateUrl + "]" + principalName,
              CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE), roleTypes);

      // If it's a SharePoint group, add the membership info
      // into the User Data Store
      if (PrincipalType.SPGROUP.equals(principal.getType())
          && null != sharepointClientContext.getUserDataStoreDAO()) {
        GssPrincipal[] members = principal.getMembers();
        if (members == null || members.length == 0) {
          // If SharePoint group does not have any members
          // then such groups will be resolved later.
          webState.addSPGroupToResolve (Integer.toString(principal.getID()));         
        } else {
          for (GssPrincipal member : members) {
            memberships.add(new UserGroupMembership(member.getID(),
                getPrincipalName(member), principal.getID(), principalName,
                webStateUrl));
          }
        }
      }
    } else {
      LOGGER.log(Level.WARNING, "Skipping ACE for principal [ "
          + principal.getName() + " ] because its type [ "
          + principal.getType() + " ] is unknown");
    }
  }

  /**
   * Returns user/group name in the format as specified in as specified in
   * the connector configuration page.
   *
   * @param principal the principal used to get the user/group name from
   * @return a string that represents the user/group name in the appropriate format
   */
  /*
   * Marked public of JUnit test case GssAclTest.testGetPrincipalName.
   */
  @VisibleForTesting
  public String getPrincipalName(GssPrincipal principal) {
    String principalname = Util.getUserFromUsername(principal.getName());
    final String domain = Util.getDomainFromUsername(principal.getName());
    String domainStringConst = SPConstants.DOMAIN_CONSTANT_IN_ACL;

    if (null != domain) {
      if (PrincipalType.USER.equals(principal.getType())) {
        String usernameFormatInAcl = sharepointClientContext.getUsernameFormatInAce();
        if (null != usernameFormatInAcl
            && usernameFormatInAcl.trim().length() > 0) {
          if (principalname.contains(domainStringConst)) {
            usernameFormatInAcl = usernameFormatInAcl.replace(domainStringConst, domainStringConst = "_"
                + domainStringConst);
          }
          principalname = usernameFormatInAcl.replace(SPConstants.USERNAME_CONSTANT_IN_ACL, principalname);
          principalname = principalname.replace(domainStringConst, domain);
        }
      } else if (PrincipalType.DOMAINGROUP.equals(principal.getType())
          || PrincipalType.SPGROUP.equals(principal.getType())) {
        String groupnameFormatInAcl = sharepointClientContext.getGroupnameFormatInAce();
        if (null != groupnameFormatInAcl
            && groupnameFormatInAcl.trim().length() > 0) {
          if (principalname.contains(domainStringConst)) {
            groupnameFormatInAcl = groupnameFormatInAcl.replace(domainStringConst, domainStringConst = "_"
                + domainStringConst);
          }
          principalname = groupnameFormatInAcl.replace(SPConstants.GROUPNAME_CONSTANT_IN_ACL, principalname);
          principalname = principalname.replace(domainStringConst, domain);
        }
      }
    }
    return principalname;
  }

  /**
   * Gets a set of documents in the form of {@link SPDocumentList} crawled from
   * a single SharePoint site {@link WebState} and fetches ACL for all the
   * documents and set it the document's properties.
   *
   * @param resultSet {@link SPDocumentList} to be processed
   * @param webState parent {@link WebState} from which documents have been
   *          crawled
   */
  public void fetchAclForDocuments(SPDocumentList resultSet, WebState webState) {
    if (!sharepointClientContext.isPushAcls() || null == resultSet) {
      return;
    }
    List<SPDocument> documents = resultSet.getDocuments();
    if (null != documents) {
      Map<String, SPDocument> urlToDocMap = Maps.newHashMap();
      String[] allUrlsForAcl = new String[resultSet.size()];
      try {
        int i = 0;
        for (SPDocument document : documents) {
          if (document.isWebAppPolicyDoc()) {
            LOGGER.log(Level.FINEST, 
                "Skipping Web application policy DOC for ACL Fetch "
                + document.getUrl());
            continue;
          }
          urlToDocMap.put(document.getUrl(), document);
          allUrlsForAcl[i++] = document.getUrl();
        }
        LOGGER.log(Level.CONFIG, "Getting ACL for #" + urlToDocMap.size()
            + " entities crawled from site [ " + webState.getWebUrl()
            + " ]. Document list : " + resultSet.toString());
        GssGetAclForUrlsResult wsResult = getAclForUrls(allUrlsForAcl,
            supportsInheritedAcls, !supportsInheritedAcls);
        processWsResponse(wsResult, urlToDocMap, webState);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Problem while getting ACL from site [ "
            + webState.getWebUrl() + " ]", e);
      }
    }
  }
   /**
   * Works similar to
   * {@link ListsHelper#getListItems(ListState, java.util.Calendar, String, Set)}
   * but is designed to be used only to get those list items whose ACLs have
   * changed because of any security change at parent level.
   *
   * @param listState The list from which the items are to be retrieved
   * @param listsHelper The lists helper for parsing the web service response
   * @return a list of {@link SPDocument}
   */
  public List<SPDocument> getListItemsForAclChangeAndUpdateState(
      ListState listState, ListsHelper listsHelper) {
    List<SPDocument> aclChangedDocs = null;
    if (sharepointClientContext.isPushAcls() && listState.isAclChanged()) {
      GssGetListItemsWithInheritingRoleAssignments wsResult = GetListItemsWithInheritingRoleAssignments(listState.getPrimaryKey(), String.valueOf(listState.getLastDocIdCrawledForAcl()));
      if (null != wsResult) {
        aclChangedDocs = listsHelper.parseCustomWSResponseForListItemNodes(wsResult.getDocXml(), listState);
        if (null != aclChangedDocs) {
          LOGGER.log(Level.INFO, "Found " + aclChangedDocs.size()
              + " documents from list [ " + listState
              + " ] under ACL based crawling. Crawling status: FromID [ "
              + listState.getLastDocIdCrawledForAcl() + " ], ToID [ "
              + wsResult.getLastIdVisited() + " ], moreDocs [ "
              + wsResult.isMoreDocs() + " ] ");
          for (SPDocument document : aclChangedDocs) {
            document.setForAclChange(true);
          }
        }
        if (wsResult.isMoreDocs()) {
          listState.updateAclCrawlStatus(true, wsResult.getLastIdVisited());
        } else {
          SPDocument listDoc = listState.getDocumentInstance(sharepointClientContext.getFeedType());
          listDoc.setForAclChange(true);
          aclChangedDocs.add(listDoc);
          if (null != aclChangedDocs && aclChangedDocs.size() > 0) {
            // We have crawled the last set of documents and there
            // are
            // no more documents to be crawled. However, we can not
            // say
            // listState.endAclCrawl() at this point because the
            // crawled
            // documents are not yet fed to GSA. Once these
            // documents
            // get fed, we'll call listState.commitAclCrawlStatus()
            // and
            // the state will be updated with the same effect as if
            // we
            // have
            // called listState.endAclCrawl().
            listState.updateAclCrawlStatus(false, 0);
          } else {
            // Since, the current crawled not return any document
            // and
            // also, there are no more documents to be crawled, we
            // can
            // safely end the ACL crawl for this list.
            listState.endAclCrawl();
          }
        }
      }
    }
    return aclChangedDocs;
  }

  /**
   * Executes GetAclChangesSinceToken() web method of GssAcl web service Used
   * for ACL change detection; change token is used for synchronization purpose.
   *
   * @param webstate The {@link WebState} for which change detection is to be
   *          done
   * @return web service response {@link GssGetAclChangesSinceTokenResult} as it
   *         is
   */
  private GssGetAclChangesSinceTokenResult getAclChangesSinceToken(
      final WebState webstate) {
    return Util.makeWSRequest(sharepointClientContext, aclWS,
        new Util.RequestExecutor<GssGetAclChangesSinceTokenResult>() {
      public GssGetAclChangesSinceTokenResult onRequest(final BaseWS ws)
          throws Throwable {
        return ((AclWS) ws).getAclChangesSinceToken(
            webstate.getAclChangeTokenForWsCall(),
            webstate.getNextAclChangeToken());
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "ACL change detection has failed.", e);
      }
    });
  }

  public void fetchAclChangesSinceTokenAndUpdateState(WebState webState) {
    if (!sharepointClientContext.isPushAcls()) {
      return;
    }

    // Do not initiate ACL change detection if all the list states have not
    // yet been processed for the previously detected ACl changes
    for (ListState listState : webState.getAllListStateSet()) {
      if (listState.isAclChanged()) {
        return;
      }
    }

    // Commit the cached change token to be used for subsequent change
    // detections before initiating the change detection
    if (null != webState.getNextAclChangeToken()
        && webState.getNextAclChangeToken().trim().length() != 0) {
      webState.commitAclChangeToken();
    }

    LOGGER.log(Level.CONFIG, "Initiating ACL Change detection for web [ "
        + webState.getWebUrl() + " ] from change token [ "
        + webState.getAclChangeTokenForWsCall());
    GssGetAclChangesSinceTokenResult wsResult = getAclChangesSinceToken(webState);
    processWsResponse(wsResult, webState);
  }

  /**
   * Analyze the set of changes returned by the Custom ACL web service and
   * update the status of child ListStates reflecting the way crawl should
   * proceed for them. Typically, any permission change at Web level will
   * trigger a re-crawl of all the list and items which are inheriting role
   * assignments.
   *
   * @param wsResult @link{GssGetAclChangesSinceTokenResult}
   * @param webstate The {@link WebState} for which the change detection is
   *          being done
   */
  private void processWsResponse(GssGetAclChangesSinceTokenResult wsResult,
      WebState webstate) {
    if (null == wsResult || null == webstate) {
      return;
    }
    LOGGER.log(Level.CONFIG, "Processing the received ACL changes. WsLog [ "
        + wsResult.getLogMessage() + " ]");
    GssAclChangeCollection allChanges = wsResult.getAllChanges();
    GssAclChange[] changes = (null == allChanges) ? null
        : allChanges.getChanges();

    if (null == changes) {
      return;
    }
    LOGGER.log(Level.CONFIG, "Total changes to be oprocessed # "
        + changes.length + " . WsLog [ " + allChanges.getLogMessage() + " ]");
    // If permissions of the Web has changed due to role assignment changes
    // at its first unique ancestor
    boolean isWebChanged = false;

    // If the Web has been reset to initiate a re-crawl due to a high level
    // permission change like security policy change
    boolean isWebReset = false;

    // To keep track of all the lists which have been processed. This is to
    // avoid re-processing of the same list due to multiple changes
    Set<ListState> processedLists = new HashSet<ListState>();

    // All groups where there are some membership changes
    // TODO: why not this is integer?
    Set<String> changedGroups = new TreeSet<String>();
    Set<Integer> deletedGroups = new TreeSet<Integer>();
    Set<Integer> deletedUsers = new TreeSet<Integer>();
    for (GssAclChange change : changes) {
      if (null == change) {
        continue;
      }
      ObjectType objType = change.getChangedObject();
      SPChangeType changeType = change.getChangeType();
      String changeObjectHint = change.getHint();
      if (!change.isIsEffectiveInCurrentWeb()) {
        LOGGER.log(Level.CONFIG, "Change changeType [ "
            + changeType
            + " ], objectType [ "
            + objType
            + " ] is not applicable to the current web. skipping tio the next change...");
        continue;
      }
      LOGGER.log(Level.CONFIG, "Change detected changeType [ " + changeType
          + " ], objectType [ " + objType + " ]. ");

      if (objType == ObjectType.SECURITY_POLICY) {
        LOGGER.log(Level.INFO, "Policy Change under web [ "
            + webstate.getWebUrl() + " ]");
         // With inherited ACL support no need to re-crawl entire Web.
         // Web Application policy is represented by a separate document
         // which will be processed by
         // SharePointClient.java --> processSiteData.
        if (supportsInheritedAcls) {
          webstate.setWebApplicationPolicyChange(true);
        } else {
          LOGGER.log(Level.INFO, "Resetting all list states under web [ "
              + webstate.getWebUrl()
              + " ] because web application policy change detected.");
          webstate.resetState();
          isWebReset = true;
        }
       } else if (objType == ObjectType.WEB && !isWebChanged) {
         if (changeType == SPChangeType.AssignmentDelete) {
          // Typically, deletion of a role affects the ACL of only
          // those entities down the hierarchy which are inheriting
          // the permission. But, limited access is a special case
          // where the ACL of all entities gets affected. Since, we do
          // not know what permission has been deleted, we have to
          // consider the worst case scenario and assume that the
          // deleted role was of limited access.
          LOGGER.log(Level.INFO, "Resetting all list states under web [ "
              + webstate.getWebUrl()
              + " ] because some role has been deleted and the deleted role could be Limited Access.");
          webstate.resetState();
          webstate.setWebApplicationPolicyChange(true);   
          isWebReset = true;
        } else {
          // With inherited ACL support no need to re-crawl
          // all inheriting Lists.
          // Web Permissions are associated with Web home Page.
          // just marking web home page for re-crawl.
          // TODO : Need to change setWebApplicationPolicyChange
          // to something like setRevisitWebHome.
          if (supportsInheritedAcls) {
            LOGGER.log(Level.INFO, "Change in Web Permissions - Reseting Site home Page");
            webstate.setWebApplicationPolicyChange(true);
            for (ListState list : webstate.getAllListStateSet()) {
              if (list.isApplyReadSecurity() && list.isInheritedSecurity()) {
                LOGGER.log(Level.INFO, "Change in Web Permissions -"
                    + " Resetting list state URL [ "
                    + list.getListURL()
                    + " ] because effective permisssions modified"
                    + " for List with Read Security");
                list.resetState();
              }
            }
          } else {
            isWebChanged = true;
            // Since, role assignment at web have changed, we need to
            // re-crawl all the list/items which are inheriting the
            // changed role assignments.
            for (ListState listState : webstate.getAllListStateSet()) {
              if (!listState.isInheritedSecurity()) {
                continue;
              }
              if (!processedLists.contains(listState)) {
                LOGGER.log(Level.INFO, "Marking List [ "
                    + listState
                    + " ] as a candidate for ACL based crawl because the effective ACL at this list have been updated. All the items with inheriting permissions wil be crawled from this list.");
                listState.startAclCrawl();
                processedLists.add(listState);
              }
            }
          }
        }
      } else if (objType == ObjectType.LIST && null != changeObjectHint) {
        ListState listState = webstate.getListStateForGuid(changeObjectHint);
        if (null == listState) {
          LOGGER.log(Level.WARNING, "Changed List ID [ "
              + changeObjectHint
              + " ] is not found in the WebState. Skipping to the next change.");
          continue;
        }

        if (changeType == SPChangeType.AssignmentDelete) {
          // Assuming the worst case scenario of Limited Access
          // deletion
          LOGGER.log(Level.INFO, "Resetting list state URL [ "
              + listState
              + " ] because some role has been deleted and the deleted role"
              + " could be Limited Access.");
          listState.resetState();
        } else {
          if (supportsInheritedAcls) {
            // Revisit List home for ACL changes.
            listState.markListToRevisitListHome(sharepointClientContext.getFeedType());
            if (listState.isApplyReadSecurity()) {
              LOGGER.log(Level.INFO, "Resetting list state URL [ "
                  + listState.getListURL()
                  + " ] because effective permisssions modified"
                  + " for List with Read Security");
              listState.resetState();
            }
          } else {
            if (!processedLists.contains(listState)) {
              LOGGER.log(Level.INFO, "Marking List [ "
                  + listState
                  + " ] as a candidate for ACL based crawl because the effective"
                  + " ACL at this list have been updated. All the items with"
                  + " inheriting permissions wil be crawled from this list.");
              listState.startAclCrawl();
              processedLists.add(listState);
            }
          }
        }
      } else if (objType == ObjectType.USER
      // For user-related changes, we only consider deletion changes.
      // Rest all are covered as part of web/list/item/group
      // specific changes. Refer to the WS impl. for more details
          && changeType == SPChangeType.Delete) {
        try {
          deletedUsers.add(new Integer(changeObjectHint));
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "UserId [ " + changeObjectHint
              + " ] is invalid. skipping... ", e);
          continue;
        }

        // TODO: Even if the user is not known to the user data store,
        // we would proceed here. This is because, the user data store,
        // currently, stores only those users who are member of some
        // groups. A re-crawl due to user deletion can be avoided
        // by storing all the SharePoint users (sent into ACLs in past)
        // in the local data store.

        LOGGER.log(Level.INFO, "Resetting all list states under web [ "
            + webstate.getWebUrl()
            + " ] because a user has been deleted from the SharePoint.");
        webstate.resetState();
        isWebReset = true;
        // TODO: A re-crawl due to User deletion can be avoided
        // by storing more ACL information in the local data
        // store.
      }
      // Administrators are treated as another SPGroup
      else if (objType == ObjectType.GROUP
          || objType == ObjectType.ADMINISTRATORS) {
        if (changeType == SPChangeType.Delete) {
          try {
            deletedGroups.add(Integer.parseInt(changeObjectHint));
          } catch (Exception e) {
            LOGGER.log(Level.WARNING, "GroupId [ " + changeObjectHint
                + " ] is invalid. skipping... ", e);
            continue;
          }
          // TODO: A re-crawl due to Group deletion can be avoided
          // by storing more ACL information in the local data
          // store.
          webstate.resetState();
          isWebReset = true;
        } else {
          changedGroups.add(changeObjectHint);
          // Mark Web Application Policy Change to track Site Admin
          // Change also.
          if (objType == ObjectType.ADMINISTRATORS) {
            webstate.setWebApplicationPolicyChange(true);
          }
        }
      }

      if (isWebReset) {
        break;
      }
    }

    // Sync the membership of all changed groups
    syncGroupMembership(deletedUsers, deletedGroups, changedGroups, wsResult.getSiteCollectionUrl());

    if (null == webstate.getNextAclChangeToken()
        || webstate.getNextAclChangeToken().trim().length() == 0) {
      webstate.setNextAclChangeToken(allChanges.getChangeToken());
    }
  }

  /**
   * Updates all the deleted/changed user group membership information into the
   * user data store.
   *
   * @param deletedUsers
   * @param deletedGroups
   * @param changedGroups
   * @param siteCollectionUrl
   */
  private void syncGroupMembership(Set<Integer> deletedUsers,
      Set<Integer> deletedGroups, Set<String> changedGroups,
      String siteCollectionUrl) {
    if (null == sharepointClientContext.getUserDataStoreDAO()) {
      return;
    }

    if (null != deletedUsers && deletedUsers.size() > 0) {
      try {
        sharepointClientContext.getUserDataStoreDAO().removeUserMembershipsFromNamespace(deletedUsers, siteCollectionUrl);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to remove user memberships from namespace [ "
            + siteCollectionUrl + " ] ");
      }
    }

    if (null != deletedGroups && deletedGroups.size() > 0) {
      try {
        sharepointClientContext.getUserDataStoreDAO().removeGroupMembershipsFromNamespace(deletedGroups, siteCollectionUrl);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to remove group memberships from namespace [ "
            + siteCollectionUrl + " ] ");
      }
    }

    if (null != changedGroups && changedGroups.size() > 0) {
      try {
        Map<Integer, Set<UserGroupMembership>> groupToMemberships = processChangedGroupsToSync(changedGroups);
        if (null != groupToMemberships && groupToMemberships.size() > 0) {
          try {
            sharepointClientContext.getUserDataStoreDAO().syncGroupMemberships(groupToMemberships, siteCollectionUrl);
          } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failure while syncing memberships from namespace [ "
                + siteCollectionUrl + " ]");
          }
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Failed to update/sync group memberships from namespace [ "
            + siteCollectionUrl + " ] ");
      }
    }
  }

  /**
   * Resolves a set of groups identified by their IDs and returns a map
   * <groupID, latest_memberships>. This is useful when a group has been changed
   * and its membership is to be re-synced with the user data store.
   *
   * @param changedGroups IDs of the groups that is to be resolved
   * @return a map of group ID to latest memberships, the map could be 
   *         empty but not null.
   */
  private Map<Integer, Set<UserGroupMembership>> processChangedGroupsToSync(
      Set<String> changedGroups) {
    Map<Integer, Set<UserGroupMembership>> groupsToMemberships =
        Maps.newHashMap();
    if (null != changedGroups && changedGroups.size() > 0) {
      String[] groupIds = new String[changedGroups.size()];
      changedGroups.toArray(groupIds);
      GssResolveSPGroupResult wsResult = resolveSPGroup(groupIds);
      if (null != wsResult) {
        GssPrincipal[] groups = wsResult.getPrinicpals();
        if (null != groups && groups.length > 0) {
          for (GssPrincipal group : groups) {
            Set<UserGroupMembership> memberships = new TreeSet<UserGroupMembership>();
            for (GssPrincipal member : group.getMembers()) {
              memberships.add(new UserGroupMembership(member.getID(),
                  getPrincipalName(member), group.getID(), group.getName(),
                  wsResult.getSiteCollectionUrl()));
            }
            groupsToMemberships.put(group.getID(), memberships);
          }
        }
      }
    }
    return groupsToMemberships;
  }

  /**
   * Executes GetAffectedItemIDsForChangeList() web method of GssAcl web
   * service. Used for getting all the Item IDs which are inheriting their role
   * assignments from the parent List.
   *
   * @param listGuid GUID of the List to be processed
   * @return Item IDs which are inheriting their role assignments from their
   *         parent list whose GUID was passed in the argument
   */
  private GssGetListItemsWithInheritingRoleAssignments GetListItemsWithInheritingRoleAssignments(
      final String listGuid, String lastItemId) {
    final int intLastItemId = Util.parseNumeric(lastItemId, 0);
    if (0 == intLastItemId) {
      LOGGER.finest("The incoming lastItemId [ " + lastItemId
          + " ] is not of a list item.");
    }

    return Util.makeWSRequest(sharepointClientContext, aclWS,
        new Util.RequestExecutor<
            GssGetListItemsWithInheritingRoleAssignments>() {
      public GssGetListItemsWithInheritingRoleAssignments onRequest(
          final BaseWS ws) throws Throwable {
        return ((AclWS) ws).getListItemsWithInheritingRoleAssignments(
            listGuid, sharepointClientContext.getBatchHint(), intLastItemId);
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING,
            "Failed to get ListItems With Inheriting RoleAssignments.", e);
      }
    });
  }

  /**
   * Executes GetAffectedListIDsForChangeWeb() web method of GssAcl web service.
   * Used for getting all the List IDs which are inheriting their role
   * assignments from the parent web site.
   *
   * @return List IDs which are inheriting their role assignments from their
   *         parent web site whose ID was passed in the argument
   */
  private String[] getListsWithInheritingRoleAssignments() {
    return Util.makeWSRequest(sharepointClientContext, aclWS,
        new Util.RequestExecutor<String[]>() {
      public String[] onRequest(final BaseWS ws) throws Throwable {
        return ((AclWS) ws).getListsWithInheritingRoleAssignments();
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING,
            "Failed to get List With Inheriting RoleAssignments.", e);
      }
    });
  }

  /**
   * Executes ResolveSPGroup() web method of GssAcl web service. Used for
   * expanding SharePoint groups to get the members.
   *
   * @param groupIds IDs of the SP Groups to be resolved
   * @return web service response {@link GssResolveSPGroupResult} as it is
   */
  public GssResolveSPGroupResult resolveSPGroup(final String[] groupIds) {
    return Util.makeWSRequest(sharepointClientContext, aclWS,
        new Util.RequestExecutor<GssResolveSPGroupResult>() {
      public GssResolveSPGroupResult onRequest(final BaseWS ws)
          throws Throwable {
        return ((AclWS) ws).resolveSPGroupInBatch(groupIds,
            sharepointClientContext.getGroupResolutionBatchSize());
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING,
            "Call to resolveSPGroupInBatch call failed.", e);
      }
    });
  }
  
  /**
   * Resolves SharePoint Groups associated with WebState and stores
   * User Group mapping in local database. Returns true if all groups
   * are resolved without error. Returns false in case of error.
   * 
   * @param webState WebState for which groups needs to be resolved
   * @return True if group resolution is successful,
   *         False if group resolution failed
   */
  public boolean resolveSharePointGroups(WebState webState) {
    // Return true if there are no groups to resolve
    Set<String> spGroupsToResolve = webState.getSPGroupsToResolve();
    if (spGroupsToResolve == null ||
        spGroupsToResolve.isEmpty()) {
      return true;
    }    
    try {
      while (!spGroupsToResolve.isEmpty()) {
        spGroupsToResolve = webState.getSPGroupsToResolve();
        String[] groupIds = new String[spGroupsToResolve.size()];
        spGroupsToResolve.toArray(groupIds);
        GssResolveSPGroupResult result = resolveSPGroup(groupIds);
        // Null check for result. Return false if result is null.
        if (result == null) {
          LOGGER.warning("Group Resolution null for WebState[ "
              + webState.getWebUrl() + " ]. Returning false");
          return false;
        }
        GssPrincipal[] groups = result.getPrinicpals();
        if (groups !=null && groups.length > 0) {
          Set<UserGroupMembership> memberships =
              new TreeSet<UserGroupMembership>();
          for (GssPrincipal group : groups) {         
            for (GssPrincipal member : group.getMembers()) {
              memberships.add(new UserGroupMembership(member.getID(),
                  getPrincipalName(member), group.getID(), group.getName(),
                  result.getSiteCollectionUrl()));
            }
            webState.removeSPGroupToResolve(Integer.toString(group.getID()));
          }
          if (!memberships.isEmpty() &&
              sharepointClientContext.getUserDataStoreDAO() != null) {
            try {
              sharepointClientContext.getUserDataStoreDAO().addMemberships(
                  memberships);
            } catch (Exception e) {
              LOGGER.log(Level.WARNING, "Failed to add #" + memberships.size()
                  + " memberships in user data store.", e);
              return false;
            }
          }
          if (webState.getSPGroupsToResolve().isEmpty()) {
            LOGGER.info("Group resolution complete for WebState [ " +
                webState.getWebUrl()
                +" ]");
          } else {
            LOGGER.info("Need to revisit group resolution for WebState [ "
                + webState.getWebUrl() 
                +" ] as all the groups are not resolved in current batch");
          }
        }
      }
      return true;
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING,
          "Group Resolution failed for WebState[ "
              + webState.getWebUrl() + " ]. Returning false",ex);
      return false;
    }
  }

  /**
   * Construct SPDocument object for representing Web application policy
   * ACL information
   */
  public SPDocument getWebApplicationPolicy(WebState webState,
      String strFeedType) {
    GssGetAclForUrlsResult result = Util.makeWSRequest(
        sharepointClientContext, aclWS,
        new Util.RequestExecutor<GssGetAclForUrlsResult>() {
      public GssGetAclForUrlsResult onRequest(final BaseWS ws) 
          throws Throwable {
        return ((AclWS) ws).getAclForWebApplicationPolicy();
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING,
            "Call to getAclForWebApplicationPolicy failed.", e);
      }
    });

    FeedType feedType = FeedType.getFeedType(strFeedType);
    SPDocument webAppPolicy = null;
    if (result == null) {
      return webAppPolicy;
    }
    String siteCollectionUrlToUse;
    if (sharepointClientContext.isIncludedUrl(result.getSiteCollectionUrl())) {
      siteCollectionUrlToUse = result.getSiteCollectionUrl();
    } else {
      LOGGER.log(Level.INFO,
          "Changing web app policy URL to connector URL ["
          + sharepointClientContext.getSiteURL() + "] as policy URL [ "
          + result.getSiteCollectionUrl() + " ] is not included.");
      siteCollectionUrlToUse = sharepointClientContext.getSiteURL();
    }
    String docID = siteCollectionUrlToUse;
    if (feedType == FeedType.CONTENT_FEED) {
      docID = docID + "|{" + result.getSiteCollectionGuid().toUpperCase() +"}";
    }
    // TODO Set SPType and Last Modified correctly.
    webAppPolicy = new SPDocument(docID, siteCollectionUrlToUse,
        Calendar.getInstance(), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE,
        siteCollectionUrlToUse, feedType, SPType.SP2007);
    webAppPolicy.setDocumentType(DocumentType.ACL);
    Map<String, SPDocument> urlToDocMap = Maps.newHashMap();
    urlToDocMap.put(result.getSiteCollectionUrl(), webAppPolicy);
    processWsResponse(result, urlToDocMap, webState);
    webAppPolicy.setWebAppPolicyDoc(true);
    return webAppPolicy;
  }

  /**
   * Executes CheckConnectivity() web method of GssAcl web service. Used for
   * checking the Web Service connectivity
   */
  public void checkConnectivity() throws SharepointException {
    Util.makeWSRequestVoid(sharepointClientContext, aclWS,
        new Util.RequestExecutorVoid() {
      public void onRequest(final BaseWS ws) throws Throwable {
        ((AclWS) ws).checkConnectivity();
      }
      
      public void onError(final Throwable e) {
        LOGGER.log(Level.WARNING, "Call to checkConnectivity failed.", e);
      }
    });
  }
}
