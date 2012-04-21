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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.Control;

public class AdTestServer extends AdServer {

  List<AdTestEntity> groups;
  List<AdTestEntity> users;
  Map<String, AdTestEntity> map;
  Set<String> names;

  public AdTestServer(
      Method connectMethod,
      String hostName,
      int port,
      String principal,
      String password) {
    super(connectMethod, hostName, port, principal, password);
  }

  public void deleteOu(String ou) throws Exception {
    if (get(AdConstants.ATTR_DISTINGUISHEDNAME + 
        AdConstants.EQUALS + ou + "," + getDn(), 
        AdConstants.ATTR_DISTINGUISHEDNAME, getDn()) != null) {
      Control[] tdCtls = new Control[]{new TreeDeleteControl()};
      Control[] orig = ldapContext.getRequestControls();
      ldapContext.setRequestControls(tdCtls);
      ldapContext.destroySubcontext(ou + "," + getDn());
      ldapContext.setRequestControls(orig);
    }
  }

  public void createOu(String ou) throws Exception {
    BasicAttributes attrs = new BasicAttributes(
        "objectClass", "organizationalUnit");
    ldapContext.createSubcontext(ou + "," + getDn(), attrs);
  }

  public void createUser(boolean prepared, AdTestEntity user, String ou)
      throws Exception {
    BasicAttributes attrs = new BasicAttributes();
    Attribute objectClass = new BasicAttribute("objectClass");
    objectClass.add("top");
    objectClass.add("person");
    objectClass.add("organizationalPerson");
    objectClass.add("user");
    attrs.put(objectClass);
    attrs.put(new BasicAttribute("sAMAccountName", user.sAMAccountName));
    attrs.put(new BasicAttribute("userPrincipalName", user.upn));
    attrs.put(new BasicAttribute("cn", user.commonName));
    attrs.put(new BasicAttribute("userAccountControl", "544"));
    byte[] utf16password = ("\"" + TestConfiguration.password + "\"")
        .getBytes("UTF-16LE");
    attrs.put(new BasicAttribute("unicodePwd", utf16password));
    user.setDn("cn=" + user.commonName + "," + ou + "," + getDn());
    if (!prepared) {
      ldapContext.createSubcontext(user.getDn(), attrs);
    }
  }

  public void setMembers(boolean prepared, AdTestEntity group)
      throws Exception {
    BasicAttributes attrs = new BasicAttributes();
    Attribute member = new BasicAttribute("member");
    if (group.children != null && group.children.size() > 0) {
      for (AdTestEntity e : group.children) {
        member.add(e.getDn());
      }
      attrs.put(member);
    }

    if (!prepared) {
      ldapContext.modifyAttributes(group.getDn(),
          new ModificationItem[] { new ModificationItem(
              DirContext.REPLACE_ATTRIBUTE, member) });
    }
  }

  public void createGroup(boolean prepared, AdTestEntity group, String ou)
      throws Exception {
    BasicAttributes attrs = new BasicAttributes();
    Attribute objectClass = new BasicAttribute("objectClass");
    objectClass.add("top");
    objectClass.add("group");
    attrs.put(objectClass);
    //TODO: investigate if always cn == name for groups
    attrs.put(new BasicAttribute("name", group.commonName));
    attrs.put(new BasicAttribute("sAMAccountName", group.sAMAccountName));
    group.setDn("cn=" + group.commonName + "," + ou + "," + getDn());
    
    if (!prepared) {
      ldapContext.createSubcontext(group.getDn(), attrs);
    }
  }

  public void generateUsersAndGroups(boolean prepared, String rootOu,
      Random random, int groupCount, int userCount) throws Exception {
    names = new HashSet<String>(groupCount + userCount);
    groups = new ArrayList<AdTestEntity>(groupCount);
    AdTestEntity root = new AdTestEntity(names, groups, random, 0);
    root.addChildren(names, groups, random, groupCount);

    for (int i = 0; i < Math.sqrt(groupCount); ++i) {
      AdTestEntity e1 = groups.get(random.nextInt(groups.size()));
      AdTestEntity e2 = groups.get(random.nextInt(groups.size()));

      if (e1 != e2) {
        if (e1.level < e2.level) {
          e1.children.add(e2);
        } else {
          e2.children.add(e1);
        }
      }
    }
    
    users = new ArrayList<AdTestEntity>(userCount);
    for (int i = 0; i < userCount; ++i) {
      AdTestEntity user = new AdTestEntity(names, users, random);
      int maxGroups = random.nextInt(100);
      for (int g = 0; g < maxGroups; ++g) {
        AdTestEntity group = groups.get(random.nextInt(groups.size()));
        group.children.add(user);
      }
    }
    
    int oui = 0;
    String ou = "ou=users_" + oui + "," + rootOu;
    
    for (int i = 0; i < users.size(); ++i) {
      if (i % 2000 == 0) {
        oui++;
        ou = "ou=users_" + oui + "," + rootOu;;
        if (!prepared) {
          createOu(ou);
        }
      }
      createUser(prepared, users.get(i), ou);
    }
    
    oui = 0;
    ou = "ou=groups_" + oui + "," + rootOu;
    createOu(ou);
    
    for (int i = 0; i < groups.size(); ++i) {
      if (i % 2000 == 0) {
        oui++;
        ou = "ou=groups_" + oui + "," + rootOu;
        createOu(ou);
      }
      createGroup(prepared, groups.get(i), ou);
    }
    
    for (int i = 0; i < groups.size(); ++i) {
      setMembers(prepared, groups.get(i));
    }
    
    map = new HashMap<String, AdTestEntity>(groups.size() + users.size());
    
    for (AdTestEntity e : groups) {
      map.put(e.getDn(), e);
    }
    for (AdTestEntity e : users) {
      map.put(e.getDn(), e);
    }
    
    for (AdTestEntity parent : map.values()) {
      if (parent.children != null) {
        for (AdTestEntity child : parent.children) {
          child.memberOf.add(parent);
        }
      }
    }
  }

  class TreeDeleteControl implements Control 
  {
    private static final long serialVersionUID = 1L;

    public byte[] getEncodedValue() {
      return new byte[] {};
    }

    public String getID() {
      return "1.2.840.113556.1.4.805";
    }

    public boolean isCritical() {
      return true;
    }
  }
}
