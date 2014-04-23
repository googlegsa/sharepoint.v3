// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.wsclient.client;

import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;

public interface AclWS extends BaseWS {
  /**
   * Executes CheckConnectivity() web method of GssAcl web service. Used for
   * checking the Web Service connectivity
   */
  public void checkConnectivity() throws Exception;

  /**
   * Executes GetAffectedListIDsForChangeWeb() web method of GssAcl web service.
   * Used for getting all the List IDs which are inheriting their role
   * assignments from the parent web site.
   *
   * @return List IDs which are inheriting their role assignments from their
   *         parent web site whose ID was passed in the argument
   */
  public String[] getListsWithInheritingRoleAssignments() throws Exception;

  /**
   * Executes GetGroupResolutionBatchSize() web method of GssAcl web service. 
   * Used for expanding SharePoint groups to get the members.
   *
   * @param groupIds IDs of the SP Groups to be resolved
   * @param batchSize The group resolution batch size
   * @return web service response {@link GssResolveSPGroupResult} as it is
   */
  public GssResolveSPGroupResult resolveSPGroupInBatch(
      String[] groupIds, int batchSize) throws Exception;

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
      int batchHint, int lastItemId) throws Exception;

  /**
   * Executes GetAclChangesSinceToken() web method of GssAcl web service used
   * for ACL change detection; change token is used for synchronization 
   * purpose.
   *
   * @param token The current change token to get ACLs for
   * @param nextToken The change token that follows the current change token
   * @return web service response {@link GssGetAclChangesSinceTokenResult}
   */
  public GssGetAclChangesSinceTokenResult getAclChangesSinceToken(
      String token, String nextToken) throws Exception;

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
      int largeAclThreshold, boolean metaUrlFeed) throws Exception;

  /**
   * Constructs a SPDocument object representing the web application policy
   * ACL information.
   */
  public GssGetAclForUrlsResult getAclForWebApplicationPolicy()
      throws Exception;
}
