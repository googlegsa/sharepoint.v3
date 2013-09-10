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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableMap;

public class AdMembership {
  private static final Logger LOGGER =
      Logger.getLogger(AdGroupsTraversalManager.class.getName());
  String memberDn;
  Long memberId;

  AdMembership(String memberDn) {
    this.memberDn = memberDn;
  }

  public Map<String, Object> getSqlParams() {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put(AdConstants.DB_MEMBERDN, memberDn);
    map.put(AdConstants.DB_ENTITYID, memberId);
    return map;
  }

  public Map<String, Object> parseForeignSecurityPrincipal() {
    if (!memberDn.toLowerCase().contains("cn=foreignsecurityprincipals,dc=")) {
      return null;
    }
    int start = memberDn.indexOf('=');
    int end = memberDn.indexOf(',');
    String sid = memberDn.substring(start + 1, end);
    // check for mangled or malformed security principal format
    if (!sid.matches("^S-1-5-21(-[0-9]+)+$")) {
      LOGGER.fine("Invalid foreign security principal [" + memberDn + "].");
      return null;
    }
    int ridStart = sid.lastIndexOf('-');
    long rid = -1L;
    try {
      rid = Long.parseLong(sid.substring(ridStart + 1, sid.length()));
    } catch (NumberFormatException e) {
      LOGGER.fine("Invalid rid in foreign security principal ["
          + memberDn + "].");
    }
    if (rid < 0 || rid > 1L << 32) {
      LOGGER.fine("Invalid rid in foreign security principal ["
          + memberDn + "].");
      return null;
    }
    return ImmutableMap.<String, Object>of(
      AdConstants.DB_DOMAINSID, sid.substring(0, ridStart),
      AdConstants.DB_RID, rid);
  }
}
