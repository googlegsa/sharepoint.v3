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

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl.LdapConnection;
import com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl.LdapConnectionSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Test class for {@link LdapServiceImpl}
 *
 * @author nageswara_sura
 */
public class LdapServiceTest {
    private LdapConnectionSettings ldapConnectionSettings;
    private LdapConnection ldapConnection;
    LdapServiceImpl ldapService;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.ldapConnectionSettings = TestConfiguration.getLdapConnetionSettings();
        ldapConnection = new LdapConnection(ldapConnectionSettings);
        this.ldapService = new LdapServiceImpl(ldapConnectionSettings,
                TestConfiguration.cacheSize, TestConfiguration.refreshInterval,
                true);
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl#getLdapContext()}
     * .
     */
    @Test
    public void testGetLdapContext() {
        this.ldapConnection.getLdapContext();
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl#getAllParentGroups(java.lang.String, java.util.Set)}
     * .
     */
    @Test
    public void testGetAllParentGroups() {
        Set<String> parentGroups = new HashSet<String>();

        ldapService.getAllParentGroups(TestConfiguration.ldapgroupname, parentGroups);
        // including the group it self.
        assertEquals(3, parentGroups.size());
        assertEquals(new Boolean(true), parentGroups.contains(TestConfiguration.google));
        // I&SBU-Web is parent of Google group
        assertEquals(new Boolean(true), parentGroups.contains(TestConfiguration.expectedParentGroup));
        parentGroups = null;
        ldapService.getAllParentGroups(TestConfiguration.fakeoremptyldapgroupname, parentGroups);
        assertEquals(null, parentGroups);
    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl#getAllLdapGroups(java.lang.String)}
     * .
     */
    @Test
    public void testGetAllLdapGroups() {
        Set<String> groups = ldapService.getAllLdapGroups(TestConfiguration.ldapuser1);
        assertNotNull(groups); // cache for user1
        ldapService.getAllLdapGroups(TestConfiguration.ldapuser2);
        assertNotNull(groups); // cache for user2
        ldapService.getAllLdapGroups(TestConfiguration.ldapuser3);
        assertNotNull(groups); // cache for user3
        ldapService.getAllLdapGroups(TestConfiguration.ldapuser4);
        assertNotNull(groups); // cache for user4
        LdapUserGroupsCache<Object, Object> cacheStore = ldapService.getLugCacheStore();
        if (cacheStore.get(TestConfiguration.ldapuser2) == null)
            throw new Error();
        ldapService.getAllLdapGroups(TestConfiguration.ldapuser5);
        ldapService.getAllLdapGroups(TestConfiguration.ldapuser6);
        ldapService.getAllLdapGroups(TestConfiguration.ldapuser5);
        ldapService.getAllLdapGroups(TestConfiguration.nullldapuser);

    }

    /**
     * Test method for
     * {@link com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl#getSamAccountNameFromSearchUser(java.lang.String)}
     * .
     */
    @Test
    public void testGetSamAccountNameFromSearchUser() {
        String expectedUserName = TestConfiguration.username;

        String userName3 = ldapService.getSamAccountNameFromSearchUser(TestConfiguration.userNameFormat3);
        assertNotNull(userName3);
        assertEquals(expectedUserName, userName3);

        String userName1 = ldapService.getSamAccountNameFromSearchUser(TestConfiguration.userNameFormat1);
        assertNotNull(userName1);
        assertEquals(expectedUserName, userName1);

        String userName2 = ldapService.getSamAccountNameFromSearchUser(TestConfiguration.userNameFormat2);
        assertNotNull(userName2);
        assertEquals(expectedUserName, userName2);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        this.ldapConnection = null;
        this.ldapService = null;
    }
}
