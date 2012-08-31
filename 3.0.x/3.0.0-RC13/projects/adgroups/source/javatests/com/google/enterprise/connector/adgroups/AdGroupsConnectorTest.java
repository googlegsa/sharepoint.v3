// Copyright 2012 Google Inc.
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

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.adgroups.AdConstants.Method;
import com.google.enterprise.connector.adgroups.TestConfiguration;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

public class AdGroupsConnectorTest extends TestCase {
  private static final Logger LOGGER =
      Logger.getLogger(AdDbUtil.class.getName());

  private void runUsernameTest(String comment, AuthenticationManager am,
      String username, String domain, String password) 
      throws Exception {
    AuthenticationResponse response;

    response = am.authenticate(
        new SimpleAuthenticationIdentity(username));
    assertTrue(comment + ": Username, no domain, no password",
        response.isValid());

    response = am.authenticate(new SimpleAuthenticationIdentity(
        username, null));
    assertTrue(comment + ": Username, no domain, null password",
        response.isValid());

    response = am.authenticate(new SimpleAuthenticationIdentity(
        username, ""));
    assertFalse(comment + ": Username, no domain, empty password",
        response.isValid());

    response = am.authenticate(
        new SimpleAuthenticationIdentity(username, null, domain));
    assertTrue(comment + ": Username, domain, null password",
        response.isValid());

    response = am.authenticate(new SimpleAuthenticationIdentity(
            username, "", domain));
    assertFalse(comment + ": Username, domain, empty password",
        response.isValid());

    response = am.authenticate(new SimpleAuthenticationIdentity(
        username, password, domain));
    assertTrue(comment + ": Username, domain, password",
        response.isValid());

    response = am.authenticate(new SimpleAuthenticationIdentity(
        username, password + "makeinvalid"));
    assertFalse(comment + ": Username, no domain, incorrect password",
        response.isValid());

    response = am.authenticate(new SimpleAuthenticationIdentity(
        username, password + "makeinvalid", domain));
    assertFalse(comment + ": Username, domain, incorrect password",
        response.isValid());
  }

  public void testSimpleCrawl() throws Exception {
    for (String dbType : TestConfiguration.dbs.keySet()) {
      AdGroupsConnector con = new AdGroupsConnector();
      LOGGER.info("Testing database: " + dbType);

      con.setMethod("SSL");
      con.setHostname(TestConfiguration.d1hostname);
      con.setPort(Integer.toString(TestConfiguration.d1port));
      con.setPrincipal(TestConfiguration.d1principal);
      con.setPassword(TestConfiguration.d1password);

      con.setDataSource(dbType, TestConfiguration.dbs.get(dbType));
      Session s = con.login();
      s.getTraversalManager().startTraversal();
      AuthenticationManager am = s.getAuthenticationManager();

      AuthenticationResponse response = am.authenticate(
          new SimpleAuthenticationIdentity(
              "non-existing user", "wrong password", "wrong domain"));
      assertFalse("Non existing user fails authn", response.isValid());
      assertNull("No groups resolved for non-existing user", 
          response.getGroups());

      String[] principal =
          TestConfiguration.d1principal.split("\\\\"); 
      String domain = principal[0];
      String username = principal[1];

      runUsernameTest("Normal case",
          am, username, domain, TestConfiguration.d1password);

      runUsernameTest("Uppercase username",
          am, username.toUpperCase(), domain, TestConfiguration.d1password);

      runUsernameTest("Lowercase username",
          am, username.toLowerCase(), domain, TestConfiguration.d1password);

      runUsernameTest("Uppercase domain",
          am, username, domain.toUpperCase(), TestConfiguration.d1password);

      runUsernameTest("Lowercase domain",
          am, username, domain.toLowerCase(), TestConfiguration.d1password);
    }
  }

  public void testGroupDeletion() throws Exception {
    for (String dbType : TestConfiguration.dbs.keySet()) {
      // setup active directory and create user
      AdTestServer ad = new AdTestServer(
          Method.SSL, 
          TestConfiguration.d1hostname, 
          TestConfiguration.d1port,
          TestConfiguration.d1principal,
          TestConfiguration.d1password);
      ad.initialize();
      String ou = TestConfiguration.testOu + "_groupdeletion";
      ad.deleteOu(ou);
      ad.createOu(ou);
      Set<String> names = new HashSet<String>();
      List<AdTestEntity> namespace = new ArrayList<AdTestEntity>();
      Random random = new Random(TestConfiguration.seed);
      AdTestEntity group = new AdTestEntity(names, namespace, random, 1);
      AdTestEntity user = new AdTestEntity(names, namespace, random);
      ad.createGroup(false, group, ou);
      ad.createUser(false, user, ou);
      user.memberOf.add(group);
      group.children.add(user);
      ad.setMembers(false, group);

      // crawl the active directory
      AdGroupsConnector con = new AdGroupsConnector();
      con.setMethod("SSL");
      con.setHostname(TestConfiguration.d1hostname);
      con.setPort(Integer.toString(TestConfiguration.d1port));
      con.setPrincipal(TestConfiguration.d1principal);
      con.setPassword(TestConfiguration.d1password);
      con.setDataSource(dbType, TestConfiguration.dbs.get(dbType));
      Session s = con.login();
      s.getTraversalManager().startTraversal();

      // delete the group
      ad.deleteEntity(group);

      // recrawl the active directory
      s.getTraversalManager().resumeTraversal("");

      // get groups for the created user
      AuthenticationResponse response = s.getAuthenticationManager()
          .authenticate(new SimpleAuthenticationIdentity(user.sAMAccountName));

      @SuppressWarnings("unchecked") Collection<Principal> principals =
          (Collection<Principal>) response.getGroups();
      assertNotNull(principals);
      assertTrue(principals.size() > 0);

      String groupname = ad.getnETBIOSName() + "\\" + group.sAMAccountName;

      for (Principal principal : principals) {
        assertFalse("Not deleted group", principal.getName().equals(groupname));
      }
    }
  }
  
  public void testUserRenames() throws Exception {
    for (String dbType : TestConfiguration.dbs.keySet()) {
      // Initialize AD
      AdTestServer ad = new AdTestServer(
          Method.SSL, 
          TestConfiguration.d1hostname, 
          TestConfiguration.d1port,
          TestConfiguration.d1principal,
          TestConfiguration.d1password);

      ad.initialize();
      String ou = TestConfiguration.testOu + "_userrenames";
      ad.deleteOu(ou);
      ad.createOu(ou);
      Set<String> names = new HashSet<String>();
      List<AdTestEntity> namespace = new ArrayList<AdTestEntity>();
      Random random = new Random(TestConfiguration.seed);
      AdTestEntity user = new AdTestEntity(names, namespace, random);
      // create the user
      ad.createUser(false, user, ou);

      // crawl AD
      AdGroupsConnector con = new AdGroupsConnector();
      con.setMethod("SSL");
      con.setHostname(TestConfiguration.d1hostname);
      con.setPort(Integer.toString(TestConfiguration.d1port));
      con.setPrincipal(TestConfiguration.d1principal);
      con.setPassword(TestConfiguration.d1password);
      con.setDataSource(dbType, TestConfiguration.dbs.get(dbType));
      Session s = con.login();
      s.getTraversalManager().startTraversal();

      // rename user
      ad.renameEntity(user, "new commonName");

      // recrawl AD
      s.getTraversalManager().resumeTraversal("");

      assertTrue("Authentication successful for renamed user", 
          s.getAuthenticationManager().authenticate(
              new SimpleAuthenticationIdentity(
                  user.sAMAccountName, TestConfiguration.password)).isValid());
    }
  }

  public void testScalability() throws Exception {
    AdTestServer ad = new AdTestServer(
        Method.SSL, 
        TestConfiguration.d1hostname, 
        TestConfiguration.d1port,
        TestConfiguration.d1principal,
        TestConfiguration.d1password);
    ad.initialize();
    if (!TestConfiguration.prepared) {
      ad.deleteOu(TestConfiguration.testOu);
      ad.createOu(TestConfiguration.testOu);
    }
    ad.generateUsersAndGroups(
        TestConfiguration.prepared,
        TestConfiguration.testOu,
        new Random(TestConfiguration.seed),
        TestConfiguration.groupsPerDomain,
        TestConfiguration.usersPerDomain);

    for (String dbType : TestConfiguration.dbs.keySet()) {
      AdGroupsConnector con = new AdGroupsConnector();
      LOGGER.info("Testing database: " + dbType);
      
      con.setMethod("SSL");
      con.setHostname(TestConfiguration.d1hostname);
      con.setPort(Integer.toString(TestConfiguration.d1port));
      con.setPrincipal(TestConfiguration.d1principal);
      con.setPassword(TestConfiguration.d1password);

      con.setDataSource(dbType, TestConfiguration.dbs.get(dbType));
      Session s = con.login();
      s.getTraversalManager().startTraversal();
      AuthenticationManager am = s.getAuthenticationManager();

      for (AdTestEntity user : ad.users) {
        AuthenticationResponse response = am.authenticate(
            new SimpleAuthenticationIdentity(user.sAMAccountName));

        Set<AdTestEntity> groupsCorrect = new HashSet<AdTestEntity>();
        user.getAllGroups(groupsCorrect);

        Set<String> groups = new HashSet<String>();
        @SuppressWarnings("unchecked") Collection<Principal> principals =
            (Collection<Principal>) response.getGroups();
        for (Principal p : principals) {
          groups.add(p.getName());
        }

        for (AdTestEntity e : groupsCorrect) {
          assertTrue(groups.contains(ad.getnETBIOSName()
              + AdConstants.BACKSLASH + e.sAMAccountName));
        }
      }
    }
  }
}
