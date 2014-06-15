// Copyright 2010 Google Inc.
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

package com.google.enterprise.connector.sharepoint.dao;

import static com.google.enterprise.connector.spi.SpiConstants.CaseSensitivityType.EVERYTHING_CASE_INSENSITIVE;
import static com.google.enterprise.connector.spi.SpiConstants.PrincipalType.UNQUALIFIED;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import com.google.common.collect.ImmutableSet;
import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.Principal;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DeadlockLoserDataAccessException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

import junit.framework.TestCase;

/**
 * @author nitendra_thakur
 */
public class UserDataStoreDAOTest extends TestCase {
  String namespace;
  UserDataStoreDAO userDataStoreDAO;
  Set<UserGroupMembership> memberships;

  protected void setUp() throws Exception {
    super.setUp();
    userDataStoreDAO = new UserDataStoreDAO(
        TestConfiguration.getUserDataSource(),
        TestConfiguration.getUserDataStoreQueryProvider(),
        TestConfiguration.getUserGroupMembershipRowMapper());
    namespace = TestConfiguration.sharepointUrl;
    memberships = TestConfiguration.getMembershipsForNameSpace(namespace);
    userDataStoreDAO.addMemberships(memberships);
  }

  /**
   * Retrieves all the membership information pertaining to a user.
   *
   * @param username the user's login name, NOT the ID
   * @return list of {@link UserGroupMembership} representing memberships
   *     of the user
   */
  private List<UserGroupMembership> getAllMembershipsForUser(String username)
      throws SharepointException {
    return userDataStoreDAO.getAllMembershipsForSearchUserAndLdapGroups(
        ImmutableSet.<String>of(), username);
  }

  public void testAddMemberships() {
    try {
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertTrue(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testRemoveUserMemberships() {
    try {
      Set<Integer> userIds = new TreeSet<Integer>();
      for (UserGroupMembership membership : memberships) {
        userIds.add(membership.getUserId());
        userIds.add(membership.getUserId());
        userIds.add(membership.getUserId());
      }
      userDataStoreDAO.removeUserMembershipsFromNamespace(userIds, namespace);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertFalse(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testRemoveGroupMemberships() {
    try {
      Set<Integer> groupIds = new TreeSet<Integer>();
      for (UserGroupMembership membership : memberships) {
        groupIds.add(membership.getGroupId());
        groupIds.add(membership.getGroupId());
        groupIds.add(membership.getGroupId());
      }
      userDataStoreDAO.removeGroupMembershipsFromNamespace(groupIds, namespace);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertFalse(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testNameSpaceMemberships() {
    try {
      Set<String> namespaces = new TreeSet<String>();
      namespaces.add(namespace);
      userDataStoreDAO.removeAllMembershipsFromNamespace(namespaces);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertFalse(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testSyncGroupMembership() {
    try {
      Map<Integer, Set<UserGroupMembership>> membershipMap = new HashMap<Integer, Set<UserGroupMembership>>();
      for (UserGroupMembership membership : memberships) {
        if (membershipMap.containsKey(membership.getGroupId())) {
          membershipMap.get(membership.getGroupId()).add(membership);
        } else {
          Set<UserGroupMembership> memberships = new HashSet<UserGroupMembership>();
          memberships.add(membership);
          membershipMap.put(membership.getGroupId(), memberships);
        }
      }
      userDataStoreDAO.syncGroupMemberships(membershipMap, namespace);
      for (UserGroupMembership membership : memberships) {
        List<UserGroupMembership> userMemberships =
            getAllMembershipsForUser(membership.getUserName());
        assertNotNull(userMemberships);
        assertTrue(userMemberships.contains(membership));
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetAllMembershipsForUser() {
    try {
      String userName = TestConfiguration.searchUserID;
      List<UserGroupMembership> members = getAllMembershipsForUser(userName);
      assertNotNull(members);
      for (UserGroupMembership membership : members) {
        assertEquals(userName, membership.getUserName());
        assertNotNull(membership.getGroupName());
      }
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetAllMembershipsForUserWithNull() {
    try {
      List<UserGroupMembership> members = getAllMembershipsForUser("testuser1");
      assertTrue(members.isEmpty());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public void testGetSharePointGroupsForSearchUserAndLdapGroups()
      throws SharepointException {
    Set<Principal> ldapGroups = ImmutableSet.of();
    Set<Principal> spGroups =
        userDataStoreDAO.getSharePointGroupsForSearchUserAndLdapGroups(
            "ns", ldapGroups, "user1");
    String expectedGroup = String.format("[%s]%s", namespace, "group1");
    Set<Principal> expectedGroups = ImmutableSet.of(
        new Principal(UNQUALIFIED, "ns", expectedGroup,
            EVERYTHING_CASE_INSENSITIVE));
    assertEquals(expectedGroups, spGroups);
  }  
  
  public void testSharePointGroupResolutionWith601ErrorRetry()
      throws SharepointException, SQLException { 
    QueryProvider queryProvider = new QueryProvider(
        "com.google.enterprise.connector.sharepoint.sql.sqlQueries");
    queryProvider.setUdsTableName("USER_GROUP_MEMBERSHIPS");
    queryProvider.setCnTableName("CONNECTOR_NAMES");
    queryProvider.setDatabase("sqlserver");
    queryProvider.init("sqlserver");
    
    UserGroupMembershipRowMapper rowMapper = new UserGroupMembershipRowMapper();
    rowMapper.setUserID("SPUserID");
    rowMapper.setUserName("SPUserName");
    rowMapper.setGroupID("SPGroupID");
    rowMapper.setGroupName("SPGroupName");
    rowMapper.setNamespace("SPSite");

    DataSource ds = createMock(DataSource.class);
    Connection c = createMock(Connection.class);
    DatabaseMetaData dbm = createNiceMock(DatabaseMetaData.class);
    Statement statement = createMock(Statement.class);
    ResultSet rs = createNiceMock(ResultSet.class);
    SQLException e601 = createNiceMock(SQLException.class);
    DataAccessException dataException = new DeadlockLoserDataAccessException(
        "Fake DataAccess exception", e601);   

    expect(ds.getConnection()).andReturn(c).anyTimes();
    expect(dbm.getTables(isNull(String.class), isNull(String.class),
        isA(String.class), isNull(String[].class))).andReturn(rs).anyTimes();
    expect(c.getMetaData()).andReturn(dbm).anyTimes();
    expect(c.createStatement()).andReturn(statement).anyTimes();
    expect(statement.executeUpdate(isA(String.class))).andReturn(1).anyTimes();
    expect(statement.executeQuery(isA(String.class))).andThrow(dataException)
        .andThrow(dataException).andReturn(rs);
    expect(e601.getMessage()).andReturn("Fake Data Move Exception").anyTimes();
    expect(e601.getErrorCode()).andReturn(601).anyTimes();
    replay(ds, c, statement, e601, dbm, rs);

    UserDataStoreDAO userDataStore = new UserDataStoreDAO(
        ds, queryProvider, rowMapper);
    userDataStore.getAllMembershipsForSearchUserAndLdapGroups(
        new HashSet<String>(), "SearchUser");
    verify(ds, c, statement, e601, dbm, rs);
  }
}
