// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthenticationResponse;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.SimpleAuthenticationIdentity;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import junit.framework.TestCase;

public class AdGroupsAuthenticationManagerTest extends TestCase {
  public void testBuiltinExcluded() throws Exception {
    final String dbType = "h2";
    AdGroupsConnector con = new AdGroupsConnector();
    con.setMethod("STANDARD");
    con.setHostname(TestConfiguration.d1hostname);
    con.setPort(Integer.toString(TestConfiguration.d1plaintextport));
    con.setPrincipal(TestConfiguration.d1principal);
    con.setPassword(TestConfiguration.d1password);
    con.setDataSource(dbType, TestConfiguration.dbs.get(dbType));
    con.setReturnBuiltin("YES");

    Session s = con.login();
    s.getTraversalManager().startTraversal();
    AuthenticationManager am = s.getAuthenticationManager();
    String username = TestConfiguration.d1principal.split("\\\\")[1];
    AuthenticationResponse response = am.authenticate(
        new SimpleAuthenticationIdentity(username));
    assertNotNull(response);

    @SuppressWarnings("unchecked") Collection<Principal> principals =
        (Collection<Principal>) response.getGroups();
    assertNotNull(principals);
    assertTrue(principals.size() > 0);

    // Count the builtin groups to make sure at least one is found.
    ArrayList<Principal> groupsFound = new ArrayList<Principal>();
    int foundCount = 0;
    for (Principal principal : principals) {
      if (isBuiltin(principal.getName())) {
        foundCount++;
      } else {
        groupsFound.add(principal);
      }
    }
    assertTrue(foundCount > 0);

    // Run the traversal again, this time excluding builtin groups.
    con.setReturnBuiltin("NO");
    am = s.getAuthenticationManager();
    response = am.authenticate(new SimpleAuthenticationIdentity(username));

    principals =
        (Collection<Principal>) response.getGroups();
    assertNotNull(principals);
    assertTrue(principals.size() > 0);

    for (Principal principal : principals) {
      assertFalse("A builtin group was found: " + principal.getName(),
          isBuiltin(principal.getName()));
    }

    assertEquals("Builtin group resolution mismatch.",
        groupsFound, principals);
  }

  private static boolean isBuiltin(String str) {
    final String prefix = "BUILTIN\\";
    return str.regionMatches(true, 0, prefix, 0, prefix.length());
  }
}
