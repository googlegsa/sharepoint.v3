//Copyright 2009 Google Inc.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
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
     *            information can be used to create the instance of current
     *            class Web Service endpoint is set to the default SharePoint
     *            URL stored in SharePointClientContext.
     * @param siteurl siteurl to be used for constructing endpoints. If null,
     *            site url is taken from SharepointClientContext
     * @throws SharepointException
     */
    public GssAclWS(final SharepointClientContext inSharepointClientContext,
            String siteurl) throws SharepointException {
        if (null == inSharepointClientContext) {
            throw new SharepointException(
                    "SharePointClient context cannot be null ");
        }
        sharepointClientContext = inSharepointClientContext;
        if (!sharepointClientContext.isPushAcls()) {
            return;
        }
        if (null == siteurl) {
            siteurl = sharepointClientContext.getSiteURL();
        }

    endpoint = Util.encodeURL(siteurl) + SPConstants.GSACLENDPOINT;
        LOGGER.log(Level.INFO, "Endpoint set to: " + endpoint);


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
    }

    // TODO: It's better use command pattern for executing web services methods.
    // This is applicable to all the WS Java clients in the current package.

    /**
     * Executes GetAclForUrls() web method of GssAcl web service. Used to get
     * the ACL of a set of entities.
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
     * Used to parse the response of {@link GssAclWS#getAclForUrls(String[])}
     * and update the ACLs into the {@link SPDocument} The set of document
     * objects must be passed in form of a map with their URLs as keys. If a
     * user have more than one permission assigned on SHarePoint, connector will
     * include each of them in the ACE. Hence, the {@link RoleType} that is sent
     * to CM may include a list of role types. Also, due to the current GSA
     * limitation, deny permissions are not handled. We may include some logics
     * to decide when to ignore a DENY when to skip. But, that would be an
     * overkill as this limitation is going to be fixed in future GSA releases
     *
     * @param wsResult Web Service response to be parsed
     * @param urlToDocMap Documents whose ACLs are to be set. The keys in the
     *            map represents the document URL
     */
    private void processWsResponse(GssGetAclForUrlsResult wsResult,
            Map<String, SPDocument> urlToDocMap) {
        if (null == wsResult || null == urlToDocMap) {
            return;
        }
        GssAcl[] allAcls = wsResult.getAllAcls();
        if (null != allAcls && allAcls.length != 0) {
            for (GssAcl acl : allAcls) {
                String entityUrl = acl.getEntityUrl();
                GssAce[] allAces = acl.getAllAce();
                if (null == entityUrl || null == allAces) {
                    LOGGER.log(Level.WARNING, "Either entityUrl [ " + entityUrl
                            + " ] is unavailable or No ACE found in the ACL");
                    continue;
                }
                SPDocument document = urlToDocMap.get(entityUrl);
                if (null == document) {
                    LOGGER.log(Level.WARNING, "No document found in urlToDocMap map for the entityUrl [ "
                            + entityUrl + " ] ");
                    continue;
                }
                Map<String, List<RoleType>> userPermissionMap = new HashMap<String, List<RoleType>>();
                Map<String, List<RoleType>> groupPermissionMap = new HashMap<String, List<RoleType>>();
                for (GssAce ace : allAces) {
                    // Handle Principal
                    GssPrincipal principal = ace.getPrincipal();
                    if (null == principal) {
                        LOGGER.log(Level.WARNING, "No Principal found in ace");
                        continue;
                    }
                    if (null == principal.getType()
                            || null == principal.getName()) {
                        LOGGER.log(Level.WARNING, "Either Principal Name [ "
                                + principal.getName()
                                + " ] or Principal Type [ "
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

                    // Currently, only list and list-items are fed as
                    // documents. In future, if sites and pages are also
                    // sent, more checks will have to be added here
                    ObjectType objectType = ObjectType.ITEM;
                    if (null != document.getParentList()) {
                        if (document.getParentList().getPrimaryKey().equals(Util.getOriginalDocId(document.getDocId(), document.getFeedType()))) {
                            objectType = ObjectType.LIST;
                        }
                    }

                    String[] deniedPermissions = permissions.getDenyRightMask();
                    if (null != deniedPermissions) {
                        List<RoleType> deniedRoleTypes = Util.getRoleTypesFor(deniedPermissions, objectType);
                        if (null != deniedRoleTypes
                                && deniedRoleTypes.size() > 0) {
                            LOGGER.log(Level.WARNING, "Skipping the current ACE becasue it contains some deny permissions [ "
                                    + deniedPermissions
                                    + " ] for Principal [ "
                                    + principal.getName() + " ] ");
                            continue;
                        }
                    }

                    // TODO:Stripping off the domain from UID is temporary as
                    // GSA
                    // does not support it currently. In future, domains will be
                    // sent as namespace. This will also be useful for sending
                    // SP Groups as they must be defined in the context of site
                    // collection. Here is Max's comment about this:
                    // A change is coming in the June train: user and group
                    // names will be associated with a namespace. By default,
                    // this will be the empty namespace, but other namespaces
                    // will be possible. This is important for sharepoint-local
                    // groups, but less so for user names - probably. In the
                    // meantime, please use the simple name (with domain
                    // stripped off) but later we will put the domain in the
                    // namespace field of the principal (user or group) name.
                    String principalName = principal.getName();
                    if (sharepointClientContext.isStripDomainFromAces()) {
                        principalName = Util.getUserFromUsername(principalName);
                    }
                    List<RoleType> allowedRoleTypes = Util.getRoleTypesFor(permissions.getGrantRightMask(), objectType);
                    if (PrincipalType.USER.equals(principal.getType())) {
                        userPermissionMap.put(principalName, allowedRoleTypes);
                    } else if (PrincipalType.DOMAINGROUP.equals(principal.getType())
                            || PrincipalType.SPGROUP.equals(principal.getType())) {
                        groupPermissionMap.put(principalName, allowedRoleTypes);
                    } else {
                        LOGGER.log(Level.WARNING, "Skipping ACE for principal [ "
                                + principal.getName()
                                + " ] becasue its type [ "
                                + principal.getType() + " ]  is unknown");
                        continue;
                    }
                }
                document.setUsersAclMap(userPermissionMap);
                document.setGroupsAclMap(groupPermissionMap);
            }
        }

    }

    /**
     * Gets a set of documents in the form of {@link SPDocumentList} crawled
     * from a single SharePoint site {@link WebState} and fetches ACL for all
     * the documents and set it the document's properties.
     *
     * @param resultSet {@link SPDocumentList} to be processed
     * @param webState parent {@link WebState} from which documents have been
     *            crawled
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
                LOGGER.log(Level.CONFIG, "Getting ACL for #"
                        + urlToDocMap.size() + " entities crawled from site [ "
                        + webState.getWebUrl() + " ]");
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
                    LOGGER.log(Level.INFO, "Found #"
                            + aclChangedDocs.size()
                            + " documents from list [ "
                            + listState
                            + " ] under ACL based crawling. Crawling status: FromID [ "
                            + listState.getLastDocIdCrawledForAcl()
                            + " ], ToID [ " + wsResult.getLastIdVisited()
                            + " ], moreDocs [ " + wsResult.isMoreDocs() + " ] ");
                    for (SPDocument document : aclChangedDocs) {
                        document.setForAclChange(true);
                    }
                }
                if (wsResult.isMoreDocs()) {
                    listState.updateAclCrawlStatus(true, wsResult.getLastIdVisited());
                } else if (null != aclChangedDocs && aclChangedDocs.size() > 0) {
                    // We have crawled the last set of documents and there are
                    // no more documents to be crawled. However, we can not say
                    // listState.endAclCrawl() at this point because the crawled
                    // documents are not yet fed to GSA. Once these documents
                    // get fed, we'll call listState.commitAclCrawlStatus() and
                    // the state will be updated with the same effect as if we
                    // have
                    // called listState.endAclCrawl().
                    listState.updateAclCrawlStatus(false, 0);
                } else {
                    // Since, the current crawled not return any document and
                    // also, there are no more documents to be crawled, we can
                    // safely end the ACL crawl for this list.
                    listState.endAclCrawl();
                }
            }
        }
        return aclChangedDocs;
    }

    /**
     * Executes GetAclChangesSinceToken() web method of GssAcl web service Used
     * for ACL change detection; change token is used for synchronization
     * purpose.
     *
     * @param webstate The {@link WebState} for which change detection is to be
     *            done
     * @return web service response {@link GssGetAclChangesSinceTokenResult} as
     *         it is
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

        LOGGER.log(Level.INFO, "Initiating ACL Change detection for web [ "
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
     *            being done
     */
    private void processWsResponse(GssGetAclChangesSinceTokenResult wsResult,
            WebState webstate) {
        if (null == wsResult || null == webstate) {
            return;
        }

        GssAclChangeCollection allChanges = wsResult.getAllChanges();
        GssAclChange[] changes = (null == allChanges) ? null
                : allChanges.getChanges();

        if (null == changes) {
            return;
        }

        // If permissions of the Web has changed due to role assignment changes
        // at its first unique ancestor
        boolean isWebChanged = false;

        // If the Web has been reset to initiate a re-crawl due to a high level
        // permission change like security policy change
        boolean isWebReset = false;

        Set<String> changedListsGuids = new HashSet<String>();
        for (GssAclChange change : changes) {
            if (null == change) {
                continue;
            }
            ObjectType objType = change.getChangedObject();
            SPChangeType changeType = change.getChangeType();
            String changeObjectHint = change.getHint();
            if (!change.isIsEffectiveInCurrentWeb()) {
                continue;
            }
            LOGGER.log(Level.CONFIG, "Change detected changeType [ " + changeType + " ], objectType [ " + objType + " ]. ");

            if (objType == ObjectType.SECURITY_POLICY) {
                LOGGER.log(Level.INFO, "Resetting all list states under web [ "
                        + webstate.getWebUrl()
                        + " ] becasue of security policy change.");
                webstate.resetState();
                isWebReset = true;
            } else if (objType == ObjectType.WEB && !isWebChanged) {
                isWebChanged = true;
                // Since, role assignment at web have changed, we need to
                // re-crawl all the list/items which are inheriting the changed
                // role assignments.
                String[] allChangedListIds = getListsWithInheritingRoleAssignments();
                if (null != allChangedListIds) {
                    changedListsGuids.addAll(Arrays.asList(allChangedListIds));
                }
            } else if (objType == ObjectType.LIST && null != changeObjectHint) {
                changedListsGuids.add(changeObjectHint);
            } else if (objType == ObjectType.USER
                    && changeType == SPChangeType.Delete) {
                // For user-related changes, we only consider deletion changes.
                // Rest all are covered as part of web/list/item/group specific
                // changes. Refer to the WS impl. for more details
                LOGGER.log(Level.INFO, "Resetting all list states under web [ "
                        + webstate.getWebUrl()
                        + " ] becasue a user has been deleted from the SharePoint.");
                webstate.resetState();
                isWebReset = true;
                // TODO: Initiating a complete re-crawl due to USER deletion is
                // not a good approach. Imagine a case when the deleted user
                // did not have any permission on anything. The re-crawl would
                // be completely unnecessary in that case.
                // A better approach could be, GSA sending the authentication
                // request to connector
                // where the user existence in SharePoint will be verified. If
                // the authentication succeeds, than only proceed with the ACL
                // based authorization. This can be done along with the session
                // creation channel approach.
            } else if (objType == ObjectType.GROUP) {
                // TODO: Group related changes are not being handled currently.
                // The vision is to maintain a local DB of user group
                // memberships. Once that is done, this change will be reflected
                // in the local DB.
            } else if (objType == ObjectType.ADMINISTRATORS) {
                // Administrators are to be treated as another SharePoint groups
                // so that any change on the administrators list does not
                // enforce a re-crawl as we are doing for user deletion case.
                // Hence, this case will also be handled with the implementation
                // of local DB for user group memberships
            }

            if (isWebReset) {
                break;
            }
        }

        for (String listGuid : changedListsGuids) {
            ListState listState = webstate.getListStateForGuid(listGuid);
            if (null == listState) {
                continue;
            }
            listState.startAclCrawl();
            LOGGER.log(Level.INFO, "Marking List [ "
                    + listState
                    + " ] as a candidate for ACL based crawl becasue the effective ACL at this list have been updated. All the items with inheriting permissions wil be crawled from this list.");
        }

        if (null == webstate.getNextAclChangeToken()
                || webstate.getNextAclChangeToken().trim().length() == 0) {
            webstate.setNextAclChangeToken(allChanges.getChangeToken());
        }
    }

    /**
     * Executes GetAffectedItemIDsForChangeList() web method of GssAcl web
     * service. Used for getting all the Item IDs which are inheriting their
     * role assignments from the parent List.
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
     * Executes GetAffectedListIDsForChangeWeb() web method of GssAcl web
     * service. Used for getting all the List IDs which are inheriting their
     * role assignments from the parent web site.
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
    public String checkConnectivity() {
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
                    LOGGER.log(Level.WARNING, "Call to checkConnectivity call failed. endpoint [ "
                            + endpoint + " ].", e);
                    return e.getLocalizedMessage();
                }
            } else {
                LOGGER.log(Level.WARNING, "Call to checkConnectivity call failed. endpoint [ "
                        + endpoint + " ].", af);
                return af.getLocalizedMessage();
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Call to checkConnectivity call failed. endpoint [ "
                    + endpoint + " ].", e);
            return e.getLocalizedMessage();
        }

        return SPConstants.CONNECTIVITY_SUCCESS;
    }
}
