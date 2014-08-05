// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.AuthType;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants.Method;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnection;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.Principal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test class for {@link UserGroupsService}
 */
public class UserGroupsServiceTest {
  private LdapConnectionSettings ldapConnectionSettings;
  private LdapConnection ldapConnection;
  UserGroupsService userGroupsService;
  SharepointClientContext sharepointClientContext;
  ClientFactory clientFactory = new SPClientFactory();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    this.ldapConnectionSettings = TestConfiguration.getLdapConnetionSettings();
    ldapConnection = new LdapConnection(ldapConnectionSettings);
    sharepointClientContext = new SharepointClientContext(
        clientFactory, TestConfiguration.sharepointUrl, TestConfiguration.domain,
        TestConfiguration.kdcserver, TestConfiguration.username,
        TestConfiguration.password, TestConfiguration.googleConnectorWorkDir,
        TestConfiguration.googleGlobalNamespace,
        TestConfiguration.googleLocalNamespace,
        TestConfiguration.includedURls, TestConfiguration.excludedURls,
        TestConfiguration.mySiteBaseURL, TestConfiguration.aliasMap,
        TestConfiguration.feedType, TestConfiguration.useSPSearchVisibility);
    sharepointClientContext.setGroupnameFormatInAce(TestConfiguration.groupNameFormatInACE);
    sharepointClientContext.setUsernameFormatInAce(TestConfiguration.userNameFormatInACE);
    sharepointClientContext.setLdapConnectionSettings(ldapConnectionSettings);
    this.userGroupsService = new UserGroupsService(sharepointClientContext);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsService#getLdapContext()}
   * .
   */
  @Test
  public void testGetLdapContext() {
    this.ldapConnection.getLdapContext();
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsService#getAllParentGroups(java.lang.String, java.util.Set)}
   * .
   */
  @Test
  public void testGetAllParentGroups() {
    Set<String> parentGroups = new HashSet<String>();

    userGroupsService.getAllParentGroups(TestConfiguration.ldapgroupname,
        parentGroups, ldapConnection.getLdapContext());
    // including the group it self.
    assertEquals(10, parentGroups.size());
    assertTrue(parentGroups.contains(TestConfiguration.google));
    assertTrue(parentGroups.contains(TestConfiguration.expectedParentGroup));
    // I&SBU-Web is parent of Google group
    parentGroups = null;
    userGroupsService.getAllParentGroups(
        TestConfiguration.fakeoremptyldapgroupname,
        parentGroups, ldapConnection.getLdapContext());
    assertNull(parentGroups);
  }

  /**
   * Test method for
   * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsService#getAllLdapGroups(java.lang.String)}
   * .
   */
  @Test
  public void testGetAllLdapGroups() {
    Set<String> groups = userGroupsService.getAllLdapGroups("u1");
    assertNotNull(groups); // cache for user1
    Set<String> ldapgroups = userGroupsService.getAllLdapGroups(TestConfiguration.ldapuser1);
    assertNotNull(ldapgroups); // cache for user1
    Set<String> groups1 = userGroupsService.getAllLdapGroups(TestConfiguration.ldapuser2);
    assertNotNull(groups1); // cache for user2
    Set<String> groups2 = userGroupsService.getAllLdapGroups(TestConfiguration.ldapuser3);
    assertNotNull(groups2); // cache for user3
    Set<String> groups3 = userGroupsService.getAllLdapGroups(TestConfiguration.ldapuser4);
    assertNotNull(groups3); // cache for user4
		assertEquals(8, groups3.size());

    UserGroupsCache<Object, ConcurrentHashMap<String, Set<Principal>>>
        cacheStore = userGroupsService.getLugCacheStore();
  }

  @Test
  public void testAddGroupNameFormatForTheGroups() {
    Set<String> groups = new HashSet<String>();
    groups.add("group1");
    groups.add("group2");
    groups.add("group3");
    groups.add("group4");
    Set<String> egroups = new HashSet<String>();

    egroups = this.userGroupsService.addGroupNameFormatForTheGroups(groups);
    if (sharepointClientContext.getGroupnameFormatInAce()
        .indexOf(SPConstants.BACKSLASH) != -1) {
      for (String groupName : egroups) {
        assertEquals(true, groupName.indexOf(SPConstants.BACKSLASH) != -1);
      }
      for (String groupName : groups) {
        assertEquals(true, egroups.contains(this.sharepointClientContext.getDomain().toUpperCase()
                + "\\" + groupName));
      }
    } else if (sharepointClientContext.getGroupnameFormatInAce()
        .indexOf(SPConstants.AT) != -1) {
      for (String groupName : egroups) {
        assertEquals(true, groupName.indexOf(SPConstants.AT) != -1);
      }
      for (String groupName : groups) {
        assertEquals(true, egroups.contains(this.sharepointClientContext.getDomain().toUpperCase()
                + "@" + groupName));
      }
    } else {
      for (String groupName : egroups) {
        assertEquals(true, groups.contains(groupName));
      }
    }
  }

  @Test
  public void testAddUserNameFormatForTheSearchUser() {
    String userName = TestConfiguration.usernameFormatInAce;
    String searchUserName = TestConfiguration.userNameFormat1;
    String finalUserName = this.userGroupsService.addUserNameFormatForTheSearchUser(searchUserName);
    if (sharepointClientContext.getUsernameFormatInAce()
        .indexOf(SPConstants.BACKSLASH) != -1) {
      assertEquals(true, finalUserName.indexOf(SPConstants.BACKSLASH) != -1);
      assertEquals(sharepointClientContext.getDomain()
          + SPConstants.BACKSLASH + searchUserName, finalUserName);
    } else if (sharepointClientContext.getUsernameFormatInAce()
        .indexOf(SPConstants.AT) != -1) {
      assertEquals(true, finalUserName.indexOf(SPConstants.AT) != -1);
      assertEquals(searchUserName, finalUserName);
    } else {
      assertTrue(userName.equalsIgnoreCase(finalUserName));
    }
  }

  @Test
  public void testCreateSearchFilterForPrimaryGroup() {
    byte[] usersid = new byte[]{0x01, 0x05, 0x15, 0x55, 0x04, 0x00, 0x00};
    assertEquals("(objectSid=\\01\\05\\15\\01\\00\\00\\00)", this.userGroupsService.createSearchFilterForPrimaryGroup(usersid, "1"));
    assertEquals("(objectSid=\\01\\05\\15\\01\\02\\00\\00)", this.userGroupsService.createSearchFilterForPrimaryGroup(usersid, "513"));
    assertEquals("(objectSid=\\01\\05\\15\\ff\\ff\\00\\00)", this.userGroupsService.createSearchFilterForPrimaryGroup(usersid, "65535"));
    assertEquals("(objectSid=\\01\\05\\15\\fc\\fd\\fe\\ff)", this.userGroupsService.createSearchFilterForPrimaryGroup(usersid, "4294901244"));
  }

  @Test
  public void testLdapEscape() {
    assertEquals("\\2a\\28\\29\\5c\\00\\2f", this.userGroupsService.ldapEscape("*()\\\0/"));
    assertEquals("Group, Name \\28Comment\\29", this.userGroupsService.ldapEscape("Group, Name (Comment)"));
  }

  @Test
  public void testInvalidCredentials() {
    LdapConnectionSettings lcs = new LdapConnectionSettings(Method.STANDARD,
        TestConfiguration.ldapServerHostAddress,
        TestConfiguration.portNumber,
        TestConfiguration.searchBase,
        AuthType.SIMPLE,
        TestConfiguration.username,
        TestConfiguration.password + "invalidatepassword",
        TestConfiguration.ldapDomainName);
    // we are testing if NPE is thrown, no asserts needed
    LdapConnection l = new LdapConnection(lcs);
  }

  @Test
  public void testUppercaseUserInCacheStore() throws SharepointException {
    String searchUser1 = TestConfiguration.searchUser1;

    // perform search twice so the second time is from the cache
    Set<Principal> groups = userGroupsService.getAllGroupsForSearchUser(
        sharepointClientContext, searchUser1);
    assertHasGroup(TestConfiguration.domain + "\\group_1", groups);
    assertHasGroup(TestConfiguration.domain + "\\group_2", groups);
    assertHasGroup(TestConfiguration.domain + "\\group_3", groups);
    assertHasGroup(TestConfiguration.domain + "\\group_4", groups);
    assertHasGroup(TestConfiguration.domain + "\\group_5", groups);

    // Try uppercase retrieval
    Set<Principal> groupsUppercaseRetrieval =
        userGroupsService.getAllGroupsForSearchUser(
            sharepointClientContext,
            searchUser1.toUpperCase());
    assertEquals(groups, groupsUppercaseRetrieval);

    // Try lowercase retrieval
    Set<Principal> groupsLowercaseRetrieval =
        userGroupsService.getAllGroupsForSearchUser(
            sharepointClientContext,
            searchUser1.toLowerCase());
    assertEquals(groups, groupsLowercaseRetrieval);
  }


  @Test
  public void testGetGroupDNForTheGroup() {
    Set<String> groupNames = new HashSet<String>();
    groupNames.add("CN=Domain Users,CN=Users,DC=gsa-connectors,DC=com");
    groupNames.add("CN=Group\\, Name (Comment),DC=gsa-connectors,DC=com");
    groupNames.add("CN=Group2_Name (Comment),DC=gsa-connectors,DC=com");
    groupNames.add("no comma");
    
    Set<String> samNames = userGroupsService.getSAMAccountNames(groupNames,
        ldapConnection.getLdapContext());
    assertNotNull(samNames);
    assertEquals(ImmutableSet.of(
        "Domain Users", "GrpNmCmt", "Group2_Name (Comment)"), samNames);
  }
  
  /**
   * Asserts that a Principal set has a specific group by name.
   *
   * @param groupName The group name to look for
   * @param groups The Principal set to check
   */
  private void assertHasGroup(String groupName, Set<Principal> groups) {
    for (Principal p : groups) {
      if (p.getName().equalsIgnoreCase(groupName)) {
        return;
      }
    }
    assertTrue("Principal set [" + groups +
        "] does not contain the group " + groupName + ".", false);
  }
  
  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    this.ldapConnection = null;
    this.userGroupsService = null;
  }
}
