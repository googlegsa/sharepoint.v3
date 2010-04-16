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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAce;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAcl;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitor;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorLocator;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssSharepointPermission;
import com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType;
import com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
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
    public GssGetAclForUrlsResult getAclForUrls(String[] urls) {
        GssGetAclForUrlsResult result = null;
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
                    LOGGER.log(Level.WARNING, "Call to getAclForUrls call failed. endpoint [ "
                            + endpoint + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Call to getAclForUrls call failed. endpoint [ "
                        + endpoint + " ].", af);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Call to getAclForUrls call failed. endpoint [ "
                    + endpoint + " ].", e);
        }
        return result;
    }

    /**
     * Used to parse the response of {@link GssAclWS#getAclForUrls(String[])}
     * and update the ACLs into the {@link SPDocument} The set of document
     * objects must be passed in form of a map with theie URLs as keys.
     *
     * @param wsResult Web Service response to be parsed
     * @param urlToDocMap Documents whose ACLs are to be set
     */
    public void processWsResponse(GssGetAclForUrlsResult wsResult,
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
                    if (document.isList()) {
                        objectType = ObjectType.LIST;
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
                    String prinicpalName = Util.getUserFromUsername(principal.getName());
                    List<RoleType> allowedRoleTypes = Util.getRoleTypesFor(permissions.getGrantRightMask(), objectType);
                    if (PrincipalType.USER.equals(principal.getType())) {
                        userPermissionMap.put(prinicpalName, allowedRoleTypes);
                    } else if (PrincipalType.DOMAINGROUP.equals(principal.getType())
                            || PrincipalType.SPGROUP.equals(principal.getType())) {
                        groupPermissionMap.put(prinicpalName, allowedRoleTypes);
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
     * Executes GetAclChangesSinceToken() web method of GssAcl web service Used
     * for ACL change detection; change token is used for synchronization
     * purpose.
     *
     * @param strChangeToken ChangeToken from where the change tracking should
     *            initiate
     * @return web service response {@link GssGetAclChangesSinceTokenResult} as
     *         it is
     */
    public GssGetAclChangesSinceTokenResult getAclChangesSinceToken(
            String strChangeToken) {
        GssGetAclChangesSinceTokenResult result = null;
        try {
            result = stub.getAclChangesSinceToken(strChangeToken);
        } catch (final AxisFault af) {
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
                        + stub.getUsername() + " ]. Trying with " + username);
                stub.setUsername(username);
                try {
                    result = stub.getAclChangesSinceToken(strChangeToken);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Call to getAclChangesSinceToken call failed. endpoint [ "
                            + endpoint + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Call to getAclChangesSinceToken call failed. endpoint [ "
                        + endpoint + " ].", af);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Call to getAclChangesSinceToken call failed. endpoint [ "
                    + endpoint + " ].", e);
        }
        return result;
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
    public String[] getAffectedItemIDsForChangeList(String listGuid) {
        String[] result = null;
        try {
            result = stub.getAffectedItemIDsForChangeList(listGuid);
        } catch (final AxisFault af) {
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
                        + stub.getUsername() + " ]. Trying with " + username);
                stub.setUsername(username);
                try {
                    result = stub.getAffectedItemIDsForChangeList(listGuid);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Call to getAffectedItemIDsForChangeList call failed. endpoint [ "
                            + endpoint + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Call to getAffectedItemIDsForChangeList call failed. endpoint [ "
                        + endpoint + " ].", af);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Call to getAffectedItemIDsForChangeList call failed. endpoint [ "
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
    public String[] getAffectedListIDsForChangeWeb(String webGuid) {
        String[] result = null;
        try {
            result = stub.getAffectedListIDsForChangeWeb(webGuid);
        } catch (final AxisFault af) {
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
                        + stub.getUsername() + " ]. Trying with " + username);
                stub.setUsername(username);
                try {
                    result = stub.getAffectedListIDsForChangeWeb(webGuid);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Call to getAffectedListIDsForChangeWeb call failed. endpoint [ "
                            + endpoint + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Call to getAffectedListIDsForChangeWeb call failed. endpoint [ "
                        + endpoint + " ].", af);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Call to getAffectedListIDsForChangeWeb call failed. endpoint [ "
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
