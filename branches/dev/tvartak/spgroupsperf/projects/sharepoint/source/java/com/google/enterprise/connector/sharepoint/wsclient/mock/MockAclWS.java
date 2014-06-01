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

package com.google.enterprise.connector.sharepoint.wsclient.mock;

import com.google.enterprise.connector.sharepoint.client.ListsHelper;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetListItemsWithInheritingRoleAssignments;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;

import java.util.logging.Logger;
import java.util.List;

public class MockAclWS implements AclWS {
  private static final Logger LOGGER = Logger.getLogger(MockAclWS.class.getName());
  private final SharepointClientContext sharepointClientContext;
  private String username;
  private String password;

  /**
   * @param ctx The Sharepoint context is passed so that necessary
   *    information can be used to create the instance of current class
   *    web service endpoint is set to the default SharePoint URL stored
   *    in SharePointClientContext.
   * @throws SharepointException
   */
  public MockAclWS(final SharepointClientContext ctx, String webUrl) {
    sharepointClientContext = ctx;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public void setUsername(final String username) {
    this.username = username;
  }

  @Override
  public void setPassword(final String password) {
    this.password = password;
  }

  @Override
  public void setTimeout(final int timeout) {
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public void checkConnectivity() throws Exception {
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public String[] getListsWithInheritingRoleAssignments() {
    return null;
  }

  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public GssResolveSPGroupResult resolveSPGroupInBatch(
      String[] groupIds, int batchSize) {
    return null;
  }
  
  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public GssGetListItemsWithInheritingRoleAssignments 
      getListItemsWithInheritingRoleAssignments(String listGuid,
      int batchHint, int lastItemId) {
    return null;
  }
  
  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public GssGetAclChangesSinceTokenResult getAclChangesSinceToken(
      String token, String nextToken) {
    return null;
  }
  
  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public GssGetAclForUrlsResult getAclForUrlsUsingInheritance(
      String[] urls, boolean useInheritance, boolean includePolicyAcls,
      int largeAclThreshold, boolean metaUrlFeed) {
    return null;
  }
  
  /**
   * (@inheritDoc)
   *
   * This is a stub implementation.
   */
  public GssGetAclForUrlsResult getAclForWebApplicationPolicy()
      throws Exception {
    return null;
  }
}
