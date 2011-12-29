//Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnection;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test class for {@link UserGroupsService}
 * 
 * @author nageswara_sura
 */
public class UserGroupsServiceTest {
	private LdapConnectionSettings ldapConnectionSettings;
	private LdapConnection ldapConnection;
	UserGroupsService userGroupsService;
	SharepointClientContext sharepointClientContext;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.ldapConnectionSettings = TestConfiguration.getLdapConnetionSettings();
		ldapConnection = new LdapConnection(ldapConnectionSettings);
		sharepointClientContext = new SharepointClientContext(
				TestConfiguration.sharepointUrl, TestConfiguration.domain,
				TestConfiguration.kdcserver, TestConfiguration.username,
				TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir,
				TestConfiguration.includedURls, TestConfiguration.excludedURls,
				TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
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

		userGroupsService.getAllParentGroups(TestConfiguration.ldapgroupname, parentGroups);
		// including the group it self.
		assertEquals(10, parentGroups.size());
		assertEquals(new Boolean(true), parentGroups.contains(TestConfiguration.google));
		// I&SBU-Web is parent of Google group
		assertEquals(new Boolean(true), parentGroups.contains(TestConfiguration.expectedParentGroup));
		parentGroups = null;
		userGroupsService.getAllParentGroups(TestConfiguration.fakeoremptyldapgroupname, parentGroups);
		assertEquals(null, parentGroups);
	}

	/**
	 * Test method for
	 * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsService#getAllLdapGroups(java.lang.String)}
	 * .
	 */
	@Test
	public void getAllLdapGroups() {
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

		UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>> cacheStore = userGroupsService.getLugCacheStore();
	}

	public void testGetADGroupsForTheSearchUser() {
		Set<String> groups = userGroupsService.getADGroupsForTheSearchUser("d1");
		assertNotNull(groups); // cache for user1
		Set<String> ldapgroups = userGroupsService.getADGroupsForTheSearchUser(TestConfiguration.ldapuser1);
		assertNotNull(ldapgroups); // cache for user1
		Set<String> groups1 = userGroupsService.getADGroupsForTheSearchUser(TestConfiguration.ldapuser2);
		assertNotNull(groups1); // cache for user2
		Set<String> groups2 = userGroupsService.getADGroupsForTheSearchUser(TestConfiguration.ldapuser3);
		assertNotNull(groups2); // cache for user3
		Set<String> groups3 = userGroupsService.getADGroupsForTheSearchUser(TestConfiguration.ldapuser4);
		assertNotNull(groups3); // cache for user4

		UserGroupsCache<Object, ConcurrentHashMap<String, Set<String>>> cacheStore = userGroupsService.getLugCacheStore();
	}

	/**
	 * Test method for
	 * {@link com.google.enterprise.connector.sharepoint.ldap.UserGroupsService#getSamAccountNameFromSearchUser(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetSamAccountNameFromSearchUser() {
		String expectedUserName = TestConfiguration.username;

		String userName3 = userGroupsService.getSamAccountNameForSearchUser(TestConfiguration.userNameFormat3);
		assertNotNull(userName3);
		assertEquals("kerbadmin", userName3);

		String userName1 = userGroupsService.getSamAccountNameForSearchUser(TestConfiguration.userNameFormat1);
		assertNotNull(userName1);
		assertEquals("kerbadmin", userName1);

		String userName2 = userGroupsService.getSamAccountNameForSearchUser(TestConfiguration.userNameFormat2);
		assertNotNull(userName2);
		assertEquals("kerbadmin", userName2);
	}

	@Test
	public void testAddGroupNameFormatForTheGroups() {
		Set<String> groups = new HashSet<String>();
		groups.add("group1");
		groups.add("group2");
		groups.add("group3");
		groups.add("group4");
		Set<String> egroups = new HashSet<String>();

		egroups = this.userGroupsService.addGroupNameFormatForTheGroups("user1", groups);
		if (sharepointClientContext.getGroupnameFormatInAce().indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
			for (String groupName : egroups) {
				assertEquals(true, groupName.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE);
			}
			for (String groupName : groups) {
				assertEquals(true, egroups.contains(this.sharepointClientContext.getDomain().toUpperCase()
						+ "\\" + groupName));
			}
		} else if (sharepointClientContext.getGroupnameFormatInAce().indexOf(SPConstants.AT) != SPConstants.MINUS_ONE) {
			for (String groupName : egroups) {
				assertEquals(true, groupName.indexOf(SPConstants.AT) != SPConstants.MINUS_ONE);
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
		if (sharepointClientContext.getUsernameFormatInAce().indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
			assertEquals(true, finalUserName.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE);
			assertEquals(sharepointClientContext.getDomain()
					+ SPConstants.DOUBLEBACKSLASH + searchUserName, finalUserName);
		} else if (sharepointClientContext.getUsernameFormatInAce().indexOf(SPConstants.AT) != SPConstants.MINUS_ONE) {
			assertEquals(true, finalUserName.indexOf(SPConstants.AT) != SPConstants.MINUS_ONE);
			assertEquals(searchUserName, finalUserName);
		} else {
			assertTrue(userName.equalsIgnoreCase(finalUserName));
		}
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
