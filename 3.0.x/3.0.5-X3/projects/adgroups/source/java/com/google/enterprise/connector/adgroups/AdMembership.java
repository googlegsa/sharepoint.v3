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

public class AdMembership {
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
}
