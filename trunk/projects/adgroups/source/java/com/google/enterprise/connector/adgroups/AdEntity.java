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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

public class AdEntity {
  private String dn;
  private String sAMAccountName;
  private String userPrincipalName;
  private String primaryGroupId;
  private String sid;
  private String objectGUID;
  private Set<AdMembership> members;
  private long uSNChanged;
  private boolean wellKnown;
  private boolean allMembershipsRetrieved;

  private Object getAttribute(Attributes attributes, String name)
      throws NamingException {
    Attribute attribute = attributes.get(name);
    if (attribute != null) {
      return attribute.get(0);
    } else {
      return null;
    }
  }

  private Attribute getMemberAttr(Attributes attrs) throws NamingException {
    allMembershipsRetrieved = true;
    Attribute member = attrs.get(AdConstants.ATTR_MEMBER);
    if (member != null && member.size() != 0) {
      return member;
    }

    NamingEnumeration<String> ids = attrs.getIDs();
    while (ids.hasMore()) {
      String id = ids.next();
      if (AdConstants.ATTR_MEMBER_PATTERN.matcher(id).matches()) {
        allMembershipsRetrieved = id.endsWith("*");
        return attrs.get(id);
      }
    }

    return null;
  }

  /**
   * Standard constructor for AdEntity. The instance is created from LDAP
   * search result.
   * @param searchResult searchResult to create the object from
   * @throws NamingException
   */
  public AdEntity(SearchResult searchResult) throws NamingException {
    dn = searchResult.getNameInNamespace();
    wellKnown = false;
    Attributes attrs = searchResult.getAttributes();
    sAMAccountName =
        (String) getAttribute(attrs, AdConstants.ATTR_SAMACCOUNTNAME);
    objectGUID =
        getTextGuid((byte[]) getAttribute(attrs, AdConstants.ATTR_OBJECTGUID));
    sid = getTextSid((byte[]) getAttribute(attrs, AdConstants.ATTR_OBJECTSID));
    String s = (String) getAttribute(attrs, AdConstants.ATTR_USNCHANGED);
    if (s != null) {
      uSNChanged = Long.parseLong(s);
    }
    primaryGroupId =
        (String) getAttribute(attrs, AdConstants.ATTR_PRIMARYGROUPID);
    userPrincipalName = (String) getAttribute(attrs, AdConstants.ATTR_UPN);

    members = new HashSet<AdMembership>();
    if (isGroup()) {
      Attribute member = getMemberAttr(attrs);
      if (member != null) {
        for (int i = 0; i < member.size(); ++i) {
          members.add(new AdMembership(member.get(i).toString()));
        }
      }
    }
  }

  /**
   * Constructor to be used only for creating well known identities
   * @param sid identifier of the object, this will be used as objectGUID as
   *        well to ensure uniqueness in the database
   * @param dn distinguished name of the object
   */
  public AdEntity(String sid, String dn) {
    this.sid = sid;
    this.dn = dn;
    objectGUID = sid;
    sAMAccountName = getCommonName();
    wellKnown = true;
  }

  /**
   * Appends additional memberships from search result 
   * @param searchResult which contains additional groups
   * @return number of groups found
   * @throws NamingException
   */
  public int appendGroups(SearchResult searchResult)
      throws NamingException {
    Attribute member = getMemberAttr(searchResult.getAttributes()); 
    if (member != null) {
      for (int i = 0; i < member.size(); ++i) {
        members.add(new AdMembership(member.get(i).toString()));
      }
      return member.size(); 
    } else {
      return 0;
    }
  }

  /**
   * Returns commonName for the given user/group while making LDAP search query
   * to get all parents groups for a given group we need to retrieve the DN
   * name for a group.
   * @return group DN from group name.
   */
  public String getCommonName() {
    // LDAP queries return escaped commas to avoid ambiguity, find first not
    // escaped comma
    int comma = dn.indexOf(AdConstants.COMMA);
    while (comma > 0 && comma < dn.length()
        && (dn.charAt(comma - 1) == AdConstants.BACKSLASH_CHAR)) {
      comma = dn.indexOf(AdConstants.COMMA, comma + 1);
    }
    String tmpGroupName = dn.substring(0, comma > 0 ? comma : dn.length());
    tmpGroupName =
        tmpGroupName.substring(
        tmpGroupName.indexOf(AdConstants.EQUALS_CHAR) + 1);
    tmpGroupName =
        tmpGroupName.replace(AdConstants.BACKSLASH, AdConstants.EMPTY);
    return tmpGroupName;
  }

  /**
   * Parses the binary SID retrieved from LDAP and converts to textual
   * representation. Text version is used to avoid dealing with different BLOB
   * types between databases.
   * @param objectSid binary array with the SID
   * @return textual representation of SID or null
   */
  public static String getTextSid(byte[] objectSid) {
    if (objectSid == null) {
      return null;
    }
    StringBuilder strSID = new StringBuilder(AdConstants.SID_START);
    long version = objectSid[0];
    strSID.append(Long.toString(version));
    long authority = objectSid[4];

    for (int i = 0; i < 4; i++) {
      authority <<= 8;
      authority += objectSid[4 + i] & 0xFF;
    }
    strSID.append(AdConstants.HYPHEN_CHAR).append(Long.toString(authority));
    long count = objectSid[2];
    count <<= 8;
    count += objectSid[1] & 0xFF;

    long rid;
    for (int j = 0; j < count; j++) {
      rid = objectSid[11 + (j * 4)] & 0xFF;
      for (int k = 1; k < 4; k++) {
        rid <<= 8;
        rid += objectSid[11 - k + (j * 4)] & 0xFF;
      }
      strSID.append(AdConstants.HYPHEN_CHAR).append(Long.toString(rid));
    }
    return strSID.toString();
  }

  /**
   * Generate properties to be used for parameter binding in JDBC
   * @return map of names and properties of current object
   */
  public Map<String, Object> getSqlParams() {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put(AdConstants.DB_DN, dn);
    map.put(AdConstants.DB_SAMACCOUNTNAME, sAMAccountName.toLowerCase());
    map.put(AdConstants.DB_UPN, userPrincipalName);
    map.put(AdConstants.DB_PRIMARYGROUPID, primaryGroupId);
    if (sid != null) {
      map.put(AdConstants.DB_DOMAINSID,
          sid.substring(0, sid.lastIndexOf(AdConstants.HYPHEN_CHAR)));
      map.put(AdConstants.DB_RID,
          sid.substring(sid.lastIndexOf(AdConstants.HYPHEN_CHAR) + 1));
    }
    map.put(AdConstants.DB_OBJECTGUID, objectGUID);
    map.put(AdConstants.DB_USNCHANGED, uSNChanged);
    map.put(AdConstants.DB_WELLKNOWN, wellKnown ? 1 : 0);
    return map;
  }

  /**
   * Parses the binary GUID retrieved from LDAP and converts to textual
   * representation. Text version is used to avoid dealing with different
   * BLOB types between databases.
   * @param binaryGuid
   * @return string containing the GUID
   */
  public static String getTextGuid(byte[] binaryGuid) {
    StringBuilder sb = new StringBuilder(AdConstants.GUID_START);
    for (byte b : binaryGuid) {
      sb.append(Integer.toHexString(b & 0xFF));
    }
    return sb.toString();
  }

  /**
   * @return the members
   */
  public Set<AdMembership> getMembers() {
    return members;
  }

  @Override
  public String toString() {
    return dn;
  }

  /**
   * @return the dn
   */
  public String getDn() {
    return dn;
  }

  /**
   * @return sAMAccountName
   */
  public String getSAMAccountName() {
    return sAMAccountName;
  }

  /**
  * @return if current entity is group
  */
  public boolean isGroup() {
    return primaryGroupId == null;
  }

  /**
   * @return if we need to retrieve further memberships for this group
   */
  public boolean areAllMembershipsRetrieved() {
    return allMembershipsRetrieved;
  }
}
