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

import junit.framework.TestCase;

public class AdMembershipTest extends TestCase {

  public void testNoForeignSecurityPrincipal() throws Exception {
    AdMembership m = new AdMembership("CN=user,OU=Users,DC=example,DC=com");
    assertNull("Do not parse normal DNs", m.parseForeignSecurityPrincipal());
  }

  public void testValidForeignSecurityPrincipals() throws Exception {
    AdMembership m = new AdMembership("CN=S-1-5-21-1636582319-1257376048,"
        + "CN=ForeignSecurityPrincipals,DC=example,DC=com");

    assertEquals("SID must be correctly extracted", "S-1-5-21-1636582319",
        m.parseForeignSecurityPrincipal().get(AdConstants.DB_DOMAINSID));
    assertEquals("RID must be correctly extracted", Long.valueOf("1257376048"),
        m.parseForeignSecurityPrincipal().get(AdConstants.DB_RID));
  }

  public void testMangledForeignSecurityPrincipals() throws Exception {
    AdMembership m = new AdMembership("CN=S-1-5-21-1636582319-1257376048-39749"
        + "47499-48617\0ACNF:f901344f-87c3-4b9b-4b9b,"
        + "CN=ForeignSecurityPrincipals,DC=example,DC=com");
    assertNull("parsing must fail", m.parseForeignSecurityPrincipal());
  }

  public void testMaxRid() throws Exception {
    AdMembership m = new AdMembership("CN=S-1-5-21-1636582319-4294967296,"
        + "CN=ForeignSecurityPrincipals,DC=example,DC=com");

    assertEquals("RID must be correctly extracted", Long.valueOf(4294967296L),
        m.parseForeignSecurityPrincipal().get(AdConstants.DB_RID));
  }

  public void testTooLargeRid() throws Exception {
    AdMembership m = new AdMembership("CN=S-1-5-21-1636582319-4294967297,"
        + "CN=ForeignSecurityPrincipals,DC=example,DC=com");
    assertNull("parsing must fail", m.parseForeignSecurityPrincipal());
  }

  public void testZeroRid() throws Exception {
    AdMembership m = new AdMembership("CN=S-1-5-21-1636582319-0,"
        + "CN=ForeignSecurityPrincipals,DC=example,DC=com");
    assertEquals("Zero is valid RID", Long.valueOf("0"),
        m.parseForeignSecurityPrincipal().get(AdConstants.DB_RID));
    }
}
