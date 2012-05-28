// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

public class AdConstants {

  // LDAP attributes
  public static final String ATTR_SAMACCOUNTNAME = "sAMAccountName";
  public static final String ATTR_OBJECTGUID = "objectGUID;binary";
  public static final String ATTR_OBJECTSID = "objectSid;binary";
  public static final String ATTR_USNCHANGED = "uSNChanged";
  public static final String ATTR_PRIMARYGROUPID = "primaryGroupId";
  public static final String ATTR_UPN = "userPrincipalName";
  public static final String ATTR_MEMBER = "member";
  public static final String ATTR_NETBIOSNAME = "nETBIOSName";
  public static final String ATTR_DNSROOT = "dnsRoot";
  public static final String ATTR_DEFAULTNAMINGCONTEXT =
      "defaultNamingContext";
  public static final String ATTR_DSSERVICENAME = "dsServiceName";
  public static final String ATTR_HIGHESTCOMMITTEDUSN = "highestCommittedUSN";
  public static final String ATTR_CONFIGURATIONNAMINGCONTEXT =
      "configurationNamingContext";
  public static final String ATTR_DISTINGUISHEDNAME = "distinguishedName";
  public static final String ATTR_INVOCATIONID = "invocationID;binary";

  public static final String LDAP_QUERY =
      "(|(objectClass=group)(objectClass=user))";
  public static final String PARTIAL_QUERY_START =
      "(&(" + AdConstants.ATTR_USNCHANGED + ">=";
  public static final String PARTIAL_QUERY_END =
      ")" + AdConstants.LDAP_QUERY + ")";

  public static final String DB_DN = "dn";
  public static final String DB_GROUPID = "groupid";
  public static final String DB_ENTITYID = "entityid";
  public static final String DB_SAMACCOUNTNAME = "samaccountname";
  public static final String DB_UPN = "userprincipalname";
  public static final String DB_PRIMARYGROUPID = "primarygroupid";
  public static final String DB_DOMAINSID = "domainsid";
  public static final String DB_RID = "rid";
  public static final String DB_SID = "sid";
  public static final String DB_NETBIOSNAME = "netbiosname";
  public static final String DB_DNSROOT = "dnsroot";
  public static final String DB_OBJECTGUID = "objectguid";
  public static final String DB_USNCHANGED = "usnchanged";
  public static final String DB_WELLKNOWN = "wellknown";
  public static final String DB_HIGHESTCOMMITTEDUSN = "highestcommittedusn";
  public static final String DB_INVOCATIONID = "invocationid";
  public static final String DB_DSSERVICENAME = "dsservicename";

  public static final String COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY =
      "com.sun.jndi.ldap.LdapCtxFactory";
  public static final String COMMA = ",";
  public static final String EMPTY = "";
  public static final String AT = "@";
  public static final char COLON_CHAR = ':';
  public static final String COLON = ":";
  public static final String SLASH = "/";
  public static final char SLASH_CHAR = '/';
  public static final String BACKSLASH = "\\";
  public static final char BACKSLASH_CHAR = '\\';
  public static final String EQUALS = "=";
  public static final char EQUALS_CHAR = '=';
  public static final char HYPHEN_CHAR = '-';

  public static final String SID_START = "S-";
  public static final String GUID_START = "0x";

  public static final String AUTHN_TYPE_SIMPLE = "simple";

  public enum Method {
    STANDARD, SSL;

    @Override
    public String toString() {
      return (this == STANDARD) ? "ldap://" : "ldaps://";
    }
  }
}
