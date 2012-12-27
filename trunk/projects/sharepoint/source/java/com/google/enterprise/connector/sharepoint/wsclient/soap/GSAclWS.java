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

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitor;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorLocator;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssAclMonitorSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;

import java.rmi.RemoteException;
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
public class GSAclWS implements AclWS{
  private GssAclMonitorSoap_BindingStub stub = null;
  private final Logger LOGGER = Logger.getLogger(GSAclWS.class.getName());
  private boolean supportsDenyAcls = false;

  /**
   * @param siteurl siteurl to be used for constructing endpoints.
   * @throws SharepointException
   */
  public GSAclWS(String siteurl) throws SharepointException {
    String endpoint = Util.encodeURL(siteurl) + SPConstants.GSACLENDPOINT;
    LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

    final GssAclMonitorLocator loc = new GssAclMonitorLocator();
    loc.setGssAclMonitorSoapEndpointAddress(endpoint);
    final GssAclMonitor service = loc;

    try {
      stub = (GssAclMonitorSoap_BindingStub) service.getGssAclMonitorSoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      throw new SharepointException("Unable to create GssAcl stub.");
    }
  }

  /**
   * (@inheritDoc)
   */
  public String getUsername() {
    return stub.getUsername();
  }

  /**
   * (@inheritDoc)
   */
  public void setUsername(final String username) {
    stub.setUsername(username);
  }

  /**
   * (@inheritDoc)
   */
  public void setPassword(final String password) {
    stub.setPassword(password);
  }

  /**
   * (@inheritDoc)
   */
  public void setTimeout(final int timeout) {
    stub.setTimeout(timeout);
  }

  /**
   * (@inheritDoc)
   */
  public GssGetAclChangesSinceTokenResult getAclChangesSinceToken(
      String token, String nextToken) throws RemoteException {
    return stub.getAclChangesSinceToken(token, nextToken);
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
  public GssGetListItemsWithInheritingRoleAssignments 
      getListItemsWithInheritingRoleAssignments(String listGuid,
      int batchHint, int lastItemId) throws RemoteException {
    return stub.getListItemsWithInheritingRoleAssignments(listGuid, 
        batchHint, lastItemId);
  }

  /**
   * Executes GetAffectedListIDsForChangeWeb() web method of GssAcl web service.
   * Used for getting all the List IDs which are inheriting their role
   * assignments from the parent web site.
   *
   * @return List IDs which are inheriting their role assignments from their
   *         parent web site whose ID was passed in the argument
   */
  public String[] getListsWithInheritingRoleAssignments()
      throws RemoteException {
    return stub.getListsWithInheritingRoleAssignments();
  }

  /**
   * Executes ResolveSPGroupInBatch() web method of GssAcl web service. 
   * Used for expanding SharePoint groups to get the members.
   *
   * @param groupIds IDs of the SP Groups to be resolved
   * @param batchSize The batch size
   * @return web service response {@link GssResolveSPGroupResult} as it is
   */
  public GssResolveSPGroupResult resolveSPGroupInBatch(
      String[] groupIds, int batchSize) throws RemoteException {
    return stub.resolveSPGroupInBatch(groupIds, batchSize);
  }
  
  /**
   * Executes CheckConnectivity() web method of GssAcl web service. Used for
   * checking the Web Service connectivity
   */
  public void checkConnectivity() throws RemoteException {
    stub.checkConnectivity();
  }

  /**
   * Executes GetAclForUrlsUsingInheritance() web method of GssAcl 
   * web service to retrieve the ACLs which belongs to a single SharePoint
   * web site.
   *
   * @param urls Entity URLs whose ACLs are to be returned
   * @param useInheritance A flag indicating whether to use of ACL inheritance
   * @param includePolicyAcls If true the result includes policy ACLs, 
   *        otherwise policy ACLs are not included
   * @param largeAclThreshold The threashold for large ACLs
   * @return web service response {@link GssGetAclChangesSinceTokenResult}
   */
  public GssGetAclForUrlsResult getAclForUrlsUsingInheritance(
      String[] urls, boolean useInheritance, boolean includePolicyAcls,
      int largeAclThreshold, boolean metaUrlFeed) throws RemoteException {
    return stub.getAclForUrlsUsingInheritance(urls, useInheritance,
        includePolicyAcls, largeAclThreshold, metaUrlFeed);
  }

  /**
   * Constructs a SPDocument object representing the web application policy
   * ACL information.
   */
  public GssGetAclForUrlsResult getAclForWebApplicationPolicy()
      throws Exception {
    return stub.getAclForWebApplicationPolicy();
  }
}
