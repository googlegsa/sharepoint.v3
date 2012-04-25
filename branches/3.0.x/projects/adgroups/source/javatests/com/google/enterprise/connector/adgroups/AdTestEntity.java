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

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AdTestEntity {
  private String dn;
  final Set<AdTestEntity> children;
  final String sAMAccountName;
  final String commonName;
  final String upn;
  final int level;
  final Set<AdTestEntity> memberOf;

  public AdTestEntity(Set<String> names,
      List<AdTestEntity> namespace, Random random, int level) {
    namespace.add(this);
    children = new HashSet<AdTestEntity>();
    sAMAccountName = getName(names, random,
        TestConfiguration.sAMAccountGroupnameAlphabet, 1, 256);
    commonName = getName(names, random, TestConfiguration.alphabet, 1, 61);
    this.level = level;
    memberOf = new HashSet<AdTestEntity>();
    upn = null;
  }

  public AdTestEntity(Set<String> names, List<AdTestEntity> namespace,
      Random random) {
    namespace.add(this);
    children = null;
    sAMAccountName = getName(names, random,
        TestConfiguration.sAMAccountUsernameAlphabet, 1, 20);
    commonName = getName(names, random,
        TestConfiguration.sAMAccountUsernameAlphabet, 1, 61);
    upn = getName(
        names, random, TestConfiguration.sAMAccountUsernameAlphabet, 1, 50)
        + "@" + getName(
            names, random, TestConfiguration.sAMAccountUsernameAlphabet, 1, 20);
    level = -1;
    memberOf = new HashSet<AdTestEntity>();
    
  }

  public void addChildren(Set<String> names, List<AdTestEntity> namespace,
      Random random, int max) {
    if (namespace.size() < max) {
      for (int i = 0; i < Math.sqrt(max); ++i) {
        AdTestEntity child = new AdTestEntity(names, namespace, random,
            level + 1);
        children.add(child);
      }
    }
    for (AdTestEntity e : children) {
      e.addChildren(names, namespace, random, max);
    }
  }

  public String getName(Set<String> names, Random random,
      String alphabet, int min, int max) {
    StringBuilder sb;
    do {
      sb = new StringBuilder(max);
      int length = min + random.nextInt(max - min);
      for (int i = 0; i < length; ++i) {
        sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
      }
    } while (names.contains(sb.toString().toLowerCase()));
    return sb.toString();
  }

  public void print(int level) {
    for (int i = 0; i < level; ++i) {
      System.out.print("\t");
    }
    System.out.println(sAMAccountName);
    
    if (children != null) {
      for (AdTestEntity e : children) {
        e.print(level + 1);
      }
    }
  }

  public void getAllGroups(Set<AdTestEntity> resolved) {
    for (AdTestEntity e : memberOf) {
      if (!resolved.contains(e)) {
        resolved.add(e);
        e.getAllGroups(resolved);
      }
    }
  }
  
  public void printGroups(Set<AdTestEntity> printed) {
    if (!printed.contains(this)) {
      for (AdTestEntity e : memberOf) { 
        System.out.println(
            "\"" + sAMAccountName + "\" -> \"" + e.sAMAccountName + "\";");
      }
      printed.add(this);
      for (AdTestEntity e : memberOf) {
        e.printGroups(printed);
      }
    }
  }

  public String getDn() {
    return dn;
  }

  public void setDn(String dn) {
    this.dn = dn;
  }
}
