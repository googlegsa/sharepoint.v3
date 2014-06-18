// Copyright 2014 Google Inc. All Rights Reserved.
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

import static com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE;
import static com.google.enterprise.connector.spi.SpiConstants.PrincipalType.UNQUALIFIED;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO;
import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.sharepoint.generated.gssacl.GssPrincipal;
import com.google.enterprise.connector.sharepoint.generated.gssacl.PrincipalType;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.sharepoint.wsclient.mock.MockClientFactory;
import com.google.enterprise.connector.spi.Principal;

import org.junit.Test;

import java.util.Set;

public class AclHelperTest {
  @Test
  public void testProcessPrincipal_spgroup() throws SharepointException {
    String localNamespace = TestConfiguration.googleLocalNamespace;
    String siteUrl = TestConfiguration.sharepointUrl;
    int principalId = 123;
    String principalName = "group1";

    UserDataStoreDAO dao = createMock(UserDataStoreDAO.class);
    WebState webState = createMock(WebState.class);
    webState.addSPGroupToResolve(String.valueOf(principalId));
    replay(dao, webState);

    SharepointClientContext clientContext =
        TestConfiguration.initContext(new MockClientFactory());
    clientContext.setUserDataStoreDAO(dao);
    GssPrincipal principal = new GssPrincipal(principalId, principalName,
        PrincipalType.SPGROUP, new GssPrincipal[0], null);
    Set<Principal> users = ImmutableSet.of(); // unwritable
    Set<Principal> groups = Sets.newHashSet();
    Set<UserGroupMembership> memberships = ImmutableSet.of(); // unwritable

    AclHelper aclHelper = new AclHelper(clientContext, siteUrl);
    aclHelper.processPrincipal(principal, users, groups, principalName, siteUrl,
        memberships, webState);

    String expectedGroup = String.format("[%s]%s", siteUrl, principalName);
    Set<Principal> expectedGroups = ImmutableSet.of(
        new Principal(UNQUALIFIED, localNamespace, expectedGroup,
            EVERYTHING_CASE_INSENSITIVE));
    assertEquals(expectedGroups, groups);
    verify(dao, webState);
  }
}
