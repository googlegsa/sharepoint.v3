//Copyright 2010 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and

//limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChange;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclChangeCollection;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitor;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorLocator;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_BindingStub;
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
import com.google.enterprise.connector.spi.SpiConstants.RoleType;

import org.apache.axis.AxisFault;

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
public class GssAclWS {
  private String endpoint;
  private GssAclMonitorSoap_BindingStub stub = null;
  private final Logger LOGGER = Logger.getLogger(GssAclWS.class.getName());
  private SharepointClientContext sharepointClientContext = null;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @param siteurl siteurl to be used for constructing endpoints. If null, site
   *          url is taken from SharepointClientContext
   * @throws SharepointException
   */
  public GssAclWS(final SharepointClientContext inSharepointClientContext,
      String siteurl) throws SharepointException {
    if (null == inSharepointClientContext) {
      throw new SharepointException("SharePointClient context cannot be null ");
    }
    sharepointClientContext = inSharepointClientContext;
    if (!sharepointClientContext.isPushAcls()) {
      return;
    }
    if (null == siteurl) {
      siteurl = sharepointClientContext.getSiteURL();
    }

    endpoint = Util.encodeURL(siteurl) + SPConstants.GSACLENDPOINT;
    LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

    final GssAclMonitorLocator loc = new GssAclMonitorLocator();
    loc.setGssAclMonitorSoapEndpointAddress(endpoint);
    final GssAclMonitor service = loc;

    try {
      stub = (GssAclMonitorSoap_BindingStub) service.getGssAclMonitorSoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      throw new SharepointException("Unable to create GssAcl stub");
    }

    final String strDomain = sharepointClientContext.getDomain();
    String strUser = sharepointClientContext.getUsername();
    final String strPassword = sharepointClientContext.getPassword();

    strUser = Util.getUserNameWithDomain(strUser, strDomain);
    stub.setUsername(strUser);
    stub.setPassword(strPassword);
    // The web service time-out value
    stub.setTimeout(sharepointClientContext.getWebServiceTimeOut());
    LOGGER.fine("Set time-out of : "
        + sharepointClientContext.getWebServiceTimeOut() + " milliseconds");
  }

  /**
   * Executes GetAclForUrls() web method of GssAcl web service. Used to get the
   * ACL of a set of entities.
   *
   * @param urls Set of entity URLs whose ACLs are to be fetched
   * @return web service response {@link GssGetAclForUrlsResult} as it is
   */
  private GssGetAclForUrlsResult getAclForUrls(String[] urls) {
    GssGetAclForUrlsResult result = null;
    if (null == urls || urls.length == 0) {
      return result;
    }
    try {
      result = stub.getAclForUrls(urls);
    } catch (final AxisFault af) {
      if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
          && (sharepointClientContext.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(stub.getUsername());
        LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
            + stub.getUsername() + " ]. Trying with " + username);
        stub.setUsername(username);
        try {
          result = stub.getAclForUrls(urls);
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, "Call to getAclForUrls failed. endpoint [ "
              + endpoint + " ].", e);
        }
      } else {
        LOGGER.log(Level.WARNING, "Call to getAclForUrls failed. endpoint [ "
            + endpoint + " ].", af);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Call to getAclForUrls failed. endpoint [ "
          + endpoint + " ].", e);
    }
    return result;
  }

  /**
   * Used to parse the response of {@link GssAclWS#getAclForUrls(String[])} and
   * update the ACLs into the {@link SPDocument} The set of document objects
   * must be passed in form of a map with their URLs as keys. If a user have
   * more than one permission assigned on SHarePoint, connector will include
   * each of them in the ACE. Hence, the {@link RoleType} that is sent to CM may
   * include a list of role types.
   * <p/>
   * Also, due to the current GSA limitation, deny permissions are not handled.
   * We may include some logics to decide when to ignore a DENY when to skip.
   * But, that would be an overkill as this limitation is going to be fixed in
   * future GSA releases
   * <p/>
   *
   * @param wsResult Web Service response to be parsed
   * @param urlToDocMap Documents whose ACLs are to be set. The keys in the map
   *          represents the document URL
   */
  private void processWsResponse(GssGetAclForUrlsResult wsResult,
      Map<String, SPDocument> urlToDocMap) {
    if (null == wsResult || null == urlToDocMap) {
      return;
    }
    LOGGER.log(Level.CONFIG, "Building ACLs from the WS response. WSLog [ "
        + wsResult.getLogMessage() + " ]");
    GssAcl[] allAcls = wsResult.getAllAcls();
    if (null != allAcls && allAcls.length != 0) {
      Set<UserGroupMembership> memberships = new TreeSet<UserGroupMembership>();
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
        if (null == document) {
          LOGGER.log(Level.WARNING, "No document found in urlToDocMap map for the entityUrl [ "
              + entityUrl + " ], WSLog [ " + acl.getLogMessage() + " ] ");
          continue;
        }
        LOGGER.log(Level.CONFIG, "WsLog [ " + acl.getLogMessage() + " ] ");
        Map<String, Set<RoleType>> userPermissionMap = new HashMap<String, Set<RoleType>>();
        Map<String, Set<RoleType>> groupPermissionMap = new HashMap<String, Set<RoleType>>();
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

          ObjectType objectType = null;

          if (document.getObjType().equals(SPConstants.SITE)) {
            objectType = ObjectType.SITE_LANDING_PAGE;
          } else if (null != document.getParentList()) {
            if (document.getParentList().getPrimaryKey().equals(Util.getOriginalDocId(document.getDocId(), document.getFeedType()))) {
              objectType = ObjectType.LIST;
            }
          } else {
            objectType = ObjectType.ITEM;
          }

          String[] deniedPermissions = permissions.getDeniedPermission();
          if (null != deniedPermissions) {
            Set<RoleType> deniedRoleTypes = Util.getRoleTypesFor(deniedPermissions, objectType);
            if (null != deniedRoleTypes && deniedRoleTypes.size() > 0) {
              // GSA does not support DENY permissions in the ACL.
              // And, sending a partial ACL (by dropping just the
              // DENY ACEs) could be wrong because two ACEs,
              // directly or indirectly, might be refering to the
              // same single user. In such cases, dropping the
              // DENY will be wrong because DENY has a
              // preference over GRANT
              LOGGER.log(Level.WARNING, "Skipping the ACL for entity URL [ "
                  + entityUrl
                  + " ] it contains some deny permissions for Principal [ "
                  + principal.getName() + " ] ");
              continue ACL;
            }
          }

          final String principalName = getPrincipalName(principal);
          Set<RoleType> allowedRoleTypes = Util.getRoleTypesFor(permissions.getAllowedPermissions(), objectType);
          if (PrincipalType.USER.equals(principal.getType())) {
            userPermissionMap.put(principalName, allowedRoleTypes);
          } else if (PrincipalType.DOMAINGROUP.equals(principal.getType())
              || PrincipalType.SPGROUP.equals(principal.getType())) {
            // If it's a SharePoint group, add the membership info
            // into the User Data Store
            if (PrincipalType.SPGROUP.equals(principal.getType())
                && null != sharepointClientContext.getUserDataStoreDAO()) {
              GssPrincipal[] members = principal.getMembers();
              for (GssPrincipal member : members) {
                memberships.add(new UserGroupMembership(member.getID(),
                    getPrincipalName(member), principal.getID(), principalName,
                    wsResult.getSiteCollectionUrl()));
              }
            }

            if (PrincipalType.SPGROUP.equals(principal.getType())) {
              // FIXME This is temporary
              groupPermissionMap.put(new StringBuffer().append("[").append(wsResult.getSiteCollectionUrl()).append("]").append(principalName).toString(), allowedRoleTypes);
            } else {
              groupPermissionMap.put(principalName, allowedRoleTypes);
            }
          } else {
            LOGGER.log(Level.WARNING, "Skipping ACE for principal [ "
                + principal.getName() + " ] becasue its type [ "
                + principal.getType() + " ]  is unknown");
            continue;
          }
        }
        document.setUsersAclMap(userPermissionMap);
        document.setGroupsAclMap(groupPermissionMap);
      }

      if (null != sharepointClientContext.getUserDataStoreDAO()) {
        try {
          sharepointClientContext.getUserDataStoreDAO().addMemberships(memberships);
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Failed to add #" + memberships.size()
              + " memberships in user data store. ", e);
        }
      }
    }
  }

  /**
   * Returns user/group name in the format as specified in connectorInstance.xml
   *
   * @param principal
   * @return
   */
  /*
   * marked package-private because of JUnit test case
   * GssAclTest.testGetPrincipalName
   */
  String getPrincipalName(GssPrincipal principal) {
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
      Map<String, SPDocument> urlToDocMap = new HashMap<String, SPDocument>();
      String[] allUrlsForAcl = new String[resultSet.size()];
      try {
        int i = 0;
        for (SPDocument document : documents) {
          urlToDocMap.put(document.getUrl(), document);
          allUrlsForAcl[i++] = document.getUrl();
        }
        LOGGER.log(Level.CONFIG, "Getting ACL for #" + urlToDocMap.size()
            + " entities crawled from site [ " + webState.getWebUrl()
            + " ]. Document list : " + resultSet.toString());
        GssGetAclForUrlsResult wsResult = getAclForUrls(allUrlsForAcl);
        processWsResponse(wsResult, urlToDocMap);
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Problem while getting ACL from site [ "
            + webState.getWebUrl() + " ]", e);
      }
    }
  }

  /**
   * Works similar to
   * {@link ListsWS#getListItems(ListState, java.util.Calendar, String, Set)}
   * but is designed to be used only to get those list items whose ACLs have
   * changed because of any security change at parent level.
   *
   * @param listState The list from which the items are to be retrieved
   * @param listsWS delegate for parsing the web service response
   * @return a list of {@link SPDocument}
   */
  public List<SPDocument> getListItemsForAclChangeAndUpdateState(
      ListState listState, ListsWS listsWS) {
    List<SPDocument> aclChangedDocs = null;
    if (sharepointClientContext.isPushAcls() && listState.isAclChanged()) {
      GssGetListItemsWithInheritingRoleAssignments wsResult = GetListItemsWithInheritingRoleAssignments(listState.getPrimaryKey(), String.valueOf(listState.getLastDocIdCrawledForAcl()));
      if (null != wsResult) {
        aclChangedDocs = listsWS.parseCustomWSResponseForListItemNodes(wsResult.getDocXml(), listState);
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
      WebState webstate) {
    GssGetAclChangesSinceTokenResult result = null;
    try {
      result = stub.getAclChangesSinceToken(webstate.getAclChangeTokenForWsCall(), webstate.getNextAclChangeToken());
    } catch (final AxisFault af) {
      if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
          && (sharepointClientContext.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(stub.getUsername());
        LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
            + stub.getUsername() + " ]. Trying with " + username);
        stub.setUsername(username);
        try {
          result = stub.getAclChangesSinceToken(webstate.getAclChangeTokenForWsCall(), webstate.getNextAclChangeToken());
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, "ACl change detection has failed. endpoint [ "
              + endpoint + " ].", e);
        }
      } else {
        LOGGER.log(Level.WARNING, "ACl change detection has failed. endpoint [ "
            + endpoint + " ].", af);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "ACl change detection has failed. endpoint [ "
          + endpoint + " ].", e);
    }
    return result;
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
        LOGGER.log(Level.INFO, "Resetting all list states under web [ "
            + webstate.getWebUrl() + " ] becasue of security policy change.");
        webstate.resetState();
        isWebReset = true;
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
              + " ] becasue some role has been deleted and the deleted role could be Limited Access.");
          webstate.resetState();
          isWebReset = true;
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
                  + " ] as a candidate for ACL based crawl becasue the effective ACL at this list have been updated. All the items with inheriting permissions wil be crawled from this list.");
              listState.startAclCrawl();
              processedLists.add(listState);
            }
          }
        }
      } else if (objType == ObjectType.LIST && null != changeObjectHint) {
        ListState listState = webstate.getListStateForGuid(changeObjectHint);
        if (null == listState) {
          LOGGER.log(Level.WARNING, "Changed List ID [ "
              + changeObjectHint
              + " ] is not found in the WebState. Skipping to the next change..");
          continue;
        }

        if (changeType == SPChangeType.AssignmentDelete) {
          // Assuming the worst case scenario of Limited Access
          // deletion
          LOGGER.log(Level.INFO, "Resetting list state URL [ "
              + webstate.getWebUrl()
              + " ] becasue some role has been deleted and the deleted role could be Limited Access.");
          listState.resetState();
        } else {
          if (!processedLists.contains(listState)) {
            LOGGER.log(Level.INFO, "Marking List [ "
                + listState
                + " ] as a candidate for ACL based crawl becasue the effective ACL at this list have been updated. All the items with inheriting permissions wil be crawled from this list.");
            listState.startAclCrawl();
            processedLists.add(listState);
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
            + " ] becasue a user has been deleted from the SharePoint.");
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
   * @return
   */
  private Map<Integer, Set<UserGroupMembership>> processChangedGroupsToSync(
      Set<String> changedGroups) {
    Map<Integer, Set<UserGroupMembership>> groupsToMemberships = new HashMap<Integer, Set<UserGroupMembership>>();
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
      String listGuid, String lastItemId) {
    int intLastItemId = 0;
    try {
      intLastItemId = Integer.parseInt(lastItemId);
    } catch (Exception e) {
      LOGGER.log(Level.FINEST, "The incoming lastItemId [ " + lastItemId
          + " ] is not of a list item. Returning...", e);
    }
    GssGetListItemsWithInheritingRoleAssignments result = null;
    try {
      result = stub.getListItemsWithInheritingRoleAssignments(listGuid, sharepointClientContext.getBatchHint(), intLastItemId);
    } catch (final AxisFault af) {
      if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
          && (sharepointClientContext.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(stub.getUsername());
        LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
            + stub.getUsername() + " ]. Trying with " + username);
        stub.setUsername(username);
        try {
          result = stub.getListItemsWithInheritingRoleAssignments(listGuid, sharepointClientContext.getBatchHint(), intLastItemId);
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, "Failed to get ListItems With Inheriting RoleAssignments. endpoint [ "
              + endpoint + " ].", e);
        }
      } else {
        LOGGER.log(Level.WARNING, "Failed to get ListItems With Inheriting RoleAssignments. endpoint [ "
            + endpoint + " ].", af);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to get ListItems With Inheriting RoleAssignments. endpoint [ "
          + endpoint + " ].", e);
    }
    return result;
  }

  /**
   * Executes GetAffectedListIDsForChangeWeb() web method of GssAcl web service.
   * Used for getting all the List IDs which are inheriting their role
   * assignments from the parent web site.
   *
   * @param webGuid GUID or URL of the SharePoint WebSite to be processed
   * @return List IDs which are inheriting their role assignments from their
   *         parent web site whose ID was passed in the argument
   */
  private String[] getListsWithInheritingRoleAssignments() {
    String[] result = null;
    try {
      result = stub.getListsWithInheritingRoleAssignments();
    } catch (final AxisFault af) {
      if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
          && (sharepointClientContext.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(stub.getUsername());
        LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
            + stub.getUsername() + " ]. Trying with " + username);
        stub.setUsername(username);
        try {
          result = stub.getListsWithInheritingRoleAssignments();
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, "Failed to get List With Inheriting RoleAssignments. endpoint [ "
              + endpoint + " ].", e);
        }
      } else {
        LOGGER.log(Level.WARNING, "Failed to get List With Inheriting RoleAssignments. endpoint [ "
            + endpoint + " ].", af);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to get List With Inheriting RoleAssignments. endpoint [ "
          + endpoint + " ].", e);
    }
    return result;
  }

  /**
   * Executes ResolveSPGroup() web method of GssAcl web service. Used for
   * expanding SharePoint groups to get the members.
   *
   * @param groupIds IDs of the SP Groups to be resolved
   * @return web service response {@link GssResolveSPGroupResult} as it is
   */
  public GssResolveSPGroupResult resolveSPGroup(String[] groupIds) {
    GssResolveSPGroupResult result = null;
    try {
      result = stub.resolveSPGroup(groupIds);
    } catch (final AxisFault af) {
      if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
          && (sharepointClientContext.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(stub.getUsername());
        LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
            + stub.getUsername() + " ]. Trying with " + username);
        stub.setUsername(username);
        try {
          result = stub.resolveSPGroup(groupIds);
        } catch (final Exception e) {
          LOGGER.log(Level.WARNING, "UCall to resolveSPGroup call failed. endpoint [ "
              + endpoint + " ].", e);
        }
      } else {
        LOGGER.log(Level.WARNING, "Call to resolveSPGroup call failed. endpoint [ "
            + endpoint + " ].", af);
      }
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Call to resolveSPGroup call failed. endpoint [ "
          + endpoint + " ].", e);
    }
    return result;
  }

  /**
   * Executes CheckConnectivity() web method of GssAcl web service Used for
   * checking the Web Service connectivity
   *
   * @return the Web Service connectivity status
   */
  public void checkConnectivity() throws SharepointException {
    try {
      stub.checkConnectivity();
    } catch (final AxisFault af) {
      if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
          && (sharepointClientContext.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(stub.getUsername());
        LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
            + stub.getUsername() + " ]. Trying with " + username);
        stub.setUsername(username);
        try {
          stub.checkConnectivity();
        } catch (final Exception e) {
          throw new SharepointException(
              "Call to checkConnectivity failed. endpoint [ " + endpoint
                  + " ].", e);
        }
      } else {
        throw new SharepointException(
            "Call to checkConnectivity failed. endpoint [ " + endpoint + " ].",
            af);
      }
    } catch (final Exception e) {
      throw new SharepointException(
          "Call to checkConnectivity failed. endpoint [ " + endpoint + " ].", e);

    }
  }
}
