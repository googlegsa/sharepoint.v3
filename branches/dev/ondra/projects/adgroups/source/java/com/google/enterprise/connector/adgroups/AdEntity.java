package com.google.enterprise.connector.adgroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class AdEntity {
  private static final Logger LOGGER = Logger.getLogger(AdEntity.class.getName());

  public String dn;
  public String sAMAccountName;
  public String userPrincipalName;
  public String primaryGroupId;
  public String sid;
  public String objectGUID;
  public AdServer server;
  public ArrayList<String> members;
  public long uSNChanged;

  public AdEntity(String dn) {
    this.dn = dn;
    members = new ArrayList<String>();
  }

  /**
   * Returns commonName for the given user/group while making LDAP search query to get all parents
   * groups for a given group we need to retrieve the DN name for a group.
   *
   * @return group DN from group name.
   */

  public String getCommonName() {
    // LDAP queries return escaped commas to avoid ambiguity, find first not escaped comma
    int comma = dn.indexOf(AdConstants.COMMA);
    while (comma > 0 && comma < dn.length()
        && (dn.charAt(comma - 1) == AdConstants.BACKSLASH_CHAR)) {
      comma = dn.indexOf(AdConstants.COMMA, comma + 1);
    }
    String tmpGroupName = dn.substring(0, comma > 0 ? comma : dn.length());
    tmpGroupName = tmpGroupName.substring(tmpGroupName.indexOf(AdConstants.EQUALS_CHAR) + 1);
    tmpGroupName = tmpGroupName.replace(AdConstants.BACKSLASH, AdConstants.EMPTY);
    return tmpGroupName;
  }

  public String getDC() {
    return dn.substring(dn.indexOf("DC="));
  }

  public String getPrimaryGroupSid(String primaryGroupId) {
    return sid.substring(0, sid.lastIndexOf('-') + 1) + primaryGroupId;
  }

  public void setSid(byte[] objectSid) {
    StringBuilder strSID = new StringBuilder("S-");
    long version = objectSid[0];
    strSID.append(Long.toString(version));
    long authority = objectSid[4];

    for (int i = 0; i < 4; i++) {
      authority <<= 8;
      authority += objectSid[4 + i] & 0xFF;
    }
    strSID.append("-").append(Long.toString(authority));
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
      strSID.append("-").append(Long.toString(rid));
    }
    sid = strSID.toString();
  }

  public String formatUser(String userNameFormatInAce) {

    if (userNameFormatInAce.equalsIgnoreCase(
        AdConstants.USERNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME)) {
      return server.nETBIOSName + AdConstants.BACKSLASH + sAMAccountName;
    } else if (
        userNameFormatInAce.equalsIgnoreCase(AdConstants.USERNAME_FORMAT_IN_ACE_ONLY_USERNAME)
        || userNameFormatInAce.equalsIgnoreCase(
            AdConstants.USERNAME_FORMAT_IN_ACE_SAMACCOUNTNAME)) {
      return sAMAccountName;
    } else if (userNameFormatInAce.equalsIgnoreCase(AdConstants.USERNAME_FORMAT_IN_ACE_DN)) {
      return dn;
    } else if (userNameFormatInAce.equalsIgnoreCase(AdConstants.USERNAME_FORMAT_IN_ACE_CN)) {
      return getCommonName();
    } else if (userNameFormatInAce.equalsIgnoreCase(
        AdConstants.USERNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME)
        || userNameFormatInAce.equalsIgnoreCase(
            AdConstants.USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME)) {
      return server.nETBIOSName.toUpperCase() + AdConstants.BACKSLASH
          + sAMAccountName.toLowerCase();
    } else if (userNameFormatInAce.equalsIgnoreCase(
        AdConstants.USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME) || userNameFormatInAce
        .equalsIgnoreCase(AdConstants.USERNAME_FORMAT_IN_ACE_USERPRINCIPALNAME)) {
      return userPrincipalName;
    } else {
      return sAMAccountName.toLowerCase();
    }
  }

  public String formatGroup(String groupFormatInAce) {
    if (groupFormatInAce.equalsIgnoreCase(
        AdConstants.GROUPNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME)) {
      return server.nETBIOSName + AdConstants.BACKSLASH + sAMAccountName;
    } else if (
        groupFormatInAce.equalsIgnoreCase(AdConstants.GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME)
        || groupFormatInAce.equalsIgnoreCase(AdConstants.GROUPNAME_FORMAT_IN_ACE_SAMACCOUNTNAME)) {
      return sAMAccountName;
    } else if (groupFormatInAce.equalsIgnoreCase(AdConstants.GROUPNAME_FORMAT_IN_ACE_DN)) {
      return dn;
    } else if (groupFormatInAce.equalsIgnoreCase(AdConstants.GROUPNAME_FORMAT_IN_ACE_CN)) {
      return getCommonName();
    } else if (groupFormatInAce.equalsIgnoreCase(
        AdConstants.GROUPNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME)
        || groupFormatInAce.equalsIgnoreCase(
            AdConstants.GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME)) {
      return server.nETBIOSName.toUpperCase() + AdConstants.BACKSLASH
          + sAMAccountName.toLowerCase();
    } else if (groupFormatInAce.equalsIgnoreCase(
        AdConstants.GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN)) {
      return sAMAccountName + AdConstants.AT + server.hostName;
    } else {
      return sAMAccountName.toLowerCase();
    }
  }

  public String format(String userNameFormatInAce, String groupFormatInAce) {
    if (userPrincipalName != null) {
      return formatUser(userNameFormatInAce);
    } else {
      return formatGroup(groupFormatInAce);
    }
  }
  
  public Map<String, Object> getSqlParams() {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("dn", dn);
    map.put("samaccountname", sAMAccountName);
    map.put("userprincipalname", userPrincipalName);
    map.put("primarygroupid", primaryGroupId);
    map.put("sid", sid);
    map.put("objectguid", objectGUID);
    map.put("usnchanged", uSNChanged);
    return map;
    
  }
  
  public void setObjectGUID(byte[] binaryGUID) {
    StringBuilder sb = new StringBuilder("0x");
    for (byte b: binaryGUID) {
        sb.append(Integer.toHexString(b & 0xFF));
    }
    objectGUID = sb.toString();
  }
}
