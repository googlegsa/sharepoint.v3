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

  // Restrict to security groups, where
  // LDAP_MATCHING_RULE_BIT_AND = 1.2.840.113556.1.4.803
  // and ADS_GROUP_TYPE_SECURITY_ENABLED = 2147483648.
  public static final String LDAP_QUERY =
      "(|(&(objectClass=group)(groupType:1.2.840.113556.1.4.803:=2147483648))"
          + "(&(objectClass=user)(objectCategory=person)))";
  public static final String PARTIAL_LDAP_QUERY =
      "(&(uSNChanged>=%d)" + LDAP_QUERY + ")";

  public static final String TOMBSTONE_QUERY =
      "(&(|(objectClass=group)(objectclass=user))(isDeleted=TRUE)("
      + ATTR_USNCHANGED + ">=%d))";

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
  public static final String DB_MEMBERDN = "memberdn";
  public static final String DB_LASTFULLSYNC = "lastfullsync";

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
  public static final String AUTHN_TYPE_ANONYMOUS = "none";

  public enum Method {
    STANDARD, SSL;

    @Override
    public String toString() {
      return (this == STANDARD) ? "ldap://" : "ldaps://";
    }
  }
}
