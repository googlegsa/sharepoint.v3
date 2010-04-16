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

                    List<RoleType> allowedRoleTypes = Util.getRoleTypesFor(permissions.getGrantRightMask(), objectType);
                    if (PrincipalType.USER.equals(principal.getType())) {
                        userPermissionMap.put(principal.getName(), allowedRoleTypes);
                    } else if (PrincipalType.DOMAINGROUP.equals(principal.getType())
                            || PrincipalType.SPGROUP.equals(principal.getType())) {
                        groupPermissionMap.put(principal.getName(), allowedRoleTypes);
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
     * For checking the Web Service connectivity
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
