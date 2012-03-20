// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

public class AdConstants {
  public static final String COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY =
      "com.sun.jndi.ldap.LdapCtxFactory";
  public static final String COMMA = ",";
  public static final String EMPTY = "";
  public static final String AT = "@";
  public static final String SLASH = "/";
  public static final char SLASH_CHAR = '/';
  public static final String BACKSLASH = "\\";
  public static final char BACKSLASH_CHAR = '\\';
  public static final char EQUALS_CHAR = '=';

  public static final String GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME = "groupname";
  public static final String GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN = "groupname@domain";
  public static final String GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME =
      "domain\\groupname";
  public static final String GROUPNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME =
      "NETBIOS\\sAMAccountName";
  public static final String GROUPNAME_FORMAT_IN_ACE_SAMACCOUNTNAME = "sAMAccountName";
  public static final String GROUPNAME_FORMAT_IN_ACE_DN = "dn";
  public static final String GROUPNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME =
      "UPPER(NETBIOS)\\lower(sAMAccountName)";
  public static final String GROUPNAME_FORMAT_IN_ACE_CN = "cn";
  public static final String USERNAME_FORMAT_IN_ACE_ONLY_USERNAME = "username";
  public static final String USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME = "username@domain";
  public static final String USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME = "domain\\username";
  public static final String USERNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME =
      "NETBIOS\\sAMAccountName";
  public static final String USERNAME_FORMAT_IN_ACE_SAMACCOUNTNAME = "sAMAccountName";
  public static final String USERNAME_FORMAT_IN_ACE_DN = "dn";
  public static final String USERNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME =
      "UPPER(NETBIOS)\\lower(sAMAccountName)";
  public static final String USERNAME_FORMAT_IN_ACE_CN = "cn";
  public static final String USERNAME_FORMAT_IN_ACE_USERPRINCIPALNAME = "userPrincipalName";

  public static final String AUTHN_TYPE_SIMPLE = "simple";

  public enum Method {
    STANDARD, SSL;
  }
}
