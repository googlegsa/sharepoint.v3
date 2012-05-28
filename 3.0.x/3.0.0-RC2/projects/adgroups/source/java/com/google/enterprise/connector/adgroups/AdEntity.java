package com.google.enterprise.connector.adgroups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private List<String> members;
  private long uSNChanged;
  private boolean wellKnown;

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
        (String) attrs.get(AdConstants.ATTR_SAMACCOUNTNAME).get(0);
    objectGUID =
        getTextGuid((byte[]) attrs.get(AdConstants.ATTR_OBJECTGUID).get(0));
    sid = getTextSid((byte[]) attrs.get(AdConstants.ATTR_OBJECTSID).get(0));
    uSNChanged =
        Long.parseLong((String) attrs.get(AdConstants.ATTR_USNCHANGED).get(0));
    if (attrs.get(AdConstants.ATTR_PRIMARYGROUPID) != null) {
      primaryGroupId =
          (String)attrs.get(AdConstants.ATTR_PRIMARYGROUPID).get(0);
    }
    if (attrs.get(AdConstants.ATTR_UPN) != null) {
      userPrincipalName = (String) attrs.get(AdConstants.ATTR_UPN).get(0);
    }
    members = new ArrayList<String>();

    Attribute member = attrs.get(AdConstants.ATTR_MEMBER);
    if (member != null) {
      for (int i = 0; i < member.size(); ++i) {
        members.add(member.get(i).toString());
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
   * @return textual representation of SID
   */
  public static String getTextSid(byte[] objectSid) {
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
    map.put(AdConstants.DB_SAMACCOUNTNAME, sAMAccountName);
    map.put(AdConstants.DB_UPN, userPrincipalName);
    map.put(AdConstants.DB_PRIMARYGROUPID, primaryGroupId);
    map.put(AdConstants.DB_DOMAINSID,
        sid.substring(0, sid.lastIndexOf(AdConstants.HYPHEN_CHAR)));
    map.put(AdConstants.DB_RID,
        sid.substring(sid.lastIndexOf(AdConstants.HYPHEN_CHAR) + 1));
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
  public List<String> getMembers() {
    return members;
  }
}
