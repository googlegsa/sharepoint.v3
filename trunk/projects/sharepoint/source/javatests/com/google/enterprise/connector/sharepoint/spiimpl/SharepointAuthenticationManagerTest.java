//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.dao.UserDataStoreDAO;
import com.google.enterprise.connector.sharepoint.wsclient.soap.SPClientFactory;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.RepositoryLoginException;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import java.util.Collection;

import junit.framework.TestCase;

public class SharepointAuthenticationManagerTest extends TestCase {

	SharepointClientContext sharepointClientContext;
	SharepointAuthenticationManager authMan;
	AuthenticationIdentity authID;
	AuthenticationResponse authenticationResponse;
	UserDataStoreDAO userDataStoreDAO;
  private final SPClientFactory clientFactory = new SPClientFactory();

	protected void setUp() throws Exception {
		System.out.println("\n...Setting Up...");
		System.out.println("Initializing SharepointClientContext ...");
		this.sharepointClientContext = new SharepointClientContext(
				clientFactory, TestConfiguration.sharepointUrl, TestConfiguration.domain,
				TestConfiguration.kdcserver, TestConfiguration.username,
				TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir,
				TestConfiguration.includedURls, TestConfiguration.excludedURls,
				TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
				TestConfiguration.feedType, TestConfiguration.useSPSearchVisibility);
		assertNotNull(this.sharepointClientContext);
		sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
		sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);
		sharepointClientContext.setPushAcls(true);
		sharepointClientContext.setUseCacheToStoreLdapUserGroupsMembership(true);
		sharepointClientContext.setCacheRefreshInterval(300);
		sharepointClientContext.setInitialCacheSize(5);
		sharepointClientContext.setGroupnameFormatInAce(TestConfiguration.groupNameFormatInACE);
		sharepointClientContext.setUsernameFormatInAce(TestConfiguration.userNameFormatInACE);
		System.out.println("Initializing SharepointAuthenticationManager ...");
		userDataStoreDAO = new UserDataStoreDAO(
				TestConfiguration.getUserDataSource(),
				TestConfiguration.getUserDataStoreQueryProvider(),
				TestConfiguration.getUserGroupMembershipRowMapper());
		this.sharepointClientContext.setUserDataStoreDAO(userDataStoreDAO);
		sharepointClientContext.setLdapConnectionSettings(TestConfiguration.getLdapConnetionSettings());
    this.authMan = new SharepointAuthenticationManager(clientFactory,
        this.sharepointClientContext, null);
		System.out.println("Initializing SharepointAuthenticationIdentity ...");
		this.authID = new SimpleAuthenticationIdentity(
				TestConfiguration.searchUserID, TestConfiguration.searchUserPwd);
	}

	public void testAuthenticate() throws Throwable {
		System.out.println("Testing authenticate()...");
		this.authenticationResponse = this.authMan.authenticate(this.authID);
		assertNotNull(authenticationResponse);
		Collection<String> groups = 
		    (Collection<String>) this.authenticationResponse.getGroups();
		assertNotNull(groups);
		System.out.println("[ authenticate() ] Test Completed.");
	}

	/**
	 * Need to run this test case by changing user name (not existed user) in
	 * TestConfig.properties.
	 * 
	 * @throws Throwable
	 */
	public void testAuthenticateWithNullGroups() throws Throwable {
		System.out.println("Testing Authenticate() with null groups");
		this.authID = null;
		this.authID = new SimpleAuthenticationIdentity("testuser",
				TestConfiguration.searchUserPwd);
		this.authenticationResponse = null;
		this.authenticationResponse = this.authMan.authenticate(this.authID);
		Collection<String> groups = 
		    (Collection<String>) this.authenticationResponse.getGroups();
		assertNull(groups);
	}

	public void testAuthenticateWithGroups() throws Throwable {
		System.out.println("Testing Authenticate() with groups");
		testAuthenticate();
		Collection<String> groups = 
		    (Collection<String>) this.authenticationResponse.getGroups();
		assertNotNull(groups);
	}

	/**
	 * Run this with empty or null password by specifying in TestConfig.properties
	 * 
	 * @throws Throwable
	 */
	public void testAuthenticateWithEmptyOrNullPassword() throws Throwable {
		System.out.println("Testing Authenticate() with empty or null password");
		testAuthenticate();
		Collection<String> groups = 
		    (Collection<String>) this.authenticationResponse.getGroups();
		assertNotNull(groups);
	}

	public void testAuthenticateWithDifferentUserNameFormats() throws Throwable {
		System.out.println("Testing Authenticate() with domain\\user");
		this.authID = new SimpleAuthenticationIdentity(
				TestConfiguration.userNameFormat1, TestConfiguration.searchUserPwd);
		this.authenticationResponse = this.authMan.authenticate(this.authID);
		assertTrue(this.authenticationResponse.isValid());
		// assertNotNull(this.authenticationResponse.getGroups());
		System.out.println("Authentication sucessful for : "
				+ TestConfiguration.userNameFormat1);
		System.out.println("Testing Authenticate() with user@domain");
		this.authID = new SimpleAuthenticationIdentity(
				TestConfiguration.userNameFormat2, TestConfiguration.searchUserPwd);
		this.authenticationResponse = this.authMan.authenticate(this.authID);
		assertTrue(this.authenticationResponse.isValid());
		// assertNotNull(this.authenticationResponse.getGroups());
		System.out.println("Authentication sucessful for : "
				+ TestConfiguration.userNameFormat2);
		System.out.println("Testing Authenticate() with user");
		this.authID = new SimpleAuthenticationIdentity(
				TestConfiguration.userNameFormat3, TestConfiguration.searchUserPwd);
		this.authenticationResponse = this.authMan.authenticate(this.authID);
		assertTrue(this.authenticationResponse.isValid());
		// assertNotNull(this.authenticationResponse.getGroups());
		System.out.println("Authentication sucessful for : "
				+ TestConfiguration.userNameFormat3);
	}

	public void testGetSamAccountNameFromSearchUser() {
		String expectedUserName = TestConfiguration.username;

		String userName3 = this.authMan.ldapService.getSamAccountNameForSearchUser(TestConfiguration.userNameFormat3);
		assertNotNull(userName3);
		assertEquals(TestConfiguration.searchUserID, userName3);

		String userName1 = this.authMan.ldapService.getSamAccountNameForSearchUser(TestConfiguration.userNameFormat1);
		assertNotNull(userName1);
		assertEquals(TestConfiguration.searchUserID, userName1);

		String userName2 = this.authMan.ldapService.getSamAccountNameForSearchUser(TestConfiguration.userNameFormat2);
		assertNotNull(userName2);
		assertEquals(TestConfiguration.searchUserID, userName2);
	}

	public void testGetAllGroupsForTheUser() throws SharepointException {
		this.authenticationResponse = this.authMan.getAllGroupsForTheUser(TestConfiguration.username);

		assertNotNull(this.authenticationResponse);
		assertNotNull(this.authenticationResponse.getGroups());

		// this time should get results from cache for the same user.
		this.authenticationResponse = this.authMan.getAllGroupsForTheUser(TestConfiguration.username);
		assertNotNull(this.authenticationResponse);
		assertNotNull(this.authenticationResponse.getGroups());

		// should fetch results from service.
		this.authenticationResponse = this.authMan.getAllGroupsForTheUser(TestConfiguration.fakeusername);
		assertNotNull(this.authenticationResponse);
		assertNull((this.authenticationResponse.getGroups()));
	}

	/**
	 * Test Authenticate method of AuthN manager by setting feed ACLs option as
	 * not selected.
	 * 
	 * @throws RepositoryLoginException
	 * @throws RepositoryException
	 */
	public void testAuthenticateIfFeedAclsTurnedOff()
			throws RepositoryLoginException, RepositoryException {
		System.out.println("Testing authenticate() by setting feed ACLs off onthe configuration page.");
		this.authenticationResponse = this.authMan.authenticate(this.authID);
		assertNotNull(authenticationResponse);
		assertTrue(authenticationResponse.isValid());
		assertNotNull(this.authenticationResponse.getGroups());
		System.out.println("[ authenticate() ] test completed if feed acls set to off.");
	}

	/**
	 * Test Authenticate method of AuthN manager by setting feed ACLs option as
	 * selected.
	 * 
	 * @throws RepositoryLoginException
	 * @throws RepositoryException
	 */
	public void testAuthencateIfFeedAclsTurnedOn()
			throws RepositoryLoginException, RepositoryException {
		System.out.println("Testing authenticate() by setting feed ACLs on on the configuration page.");
		this.authenticationResponse = this.authMan.authenticate(this.authID);
		assertNotNull(authenticationResponse);
		assertTrue(authenticationResponse.isValid());
		assertNotNull(this.authenticationResponse.getGroups());
		System.out.println("[ authenticate() ] test completed if feed acls set to on at the connector configuration page.");
	}
}
