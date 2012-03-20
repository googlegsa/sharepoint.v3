//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.adgroups;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Used to store constants
 *
 * @author nitendra_thakur
 */
public class SPConstants {
  // Used while forming user and group names.
  public static final String SLASH = "/";
  public static final String DOUBLEBACKSLASH = "\\";
  public static final String AT = "@";
  public static final char SLASH_CHAR = '/';
  public static final char DOUBLEBACKSLASH_CHAR = '\\';
  public static final char AT_CHAR = '@';

  // Other Misc. characters used mostly by SharePointClientUtils and during
  // processing the data returned by WS.
  public static final String BLANK_STRING = "";
  public static final String COMMA = ",";

  // Used in SPDocumentList.
  public static final int MINUS_ONE = -1;

  // Used by SharePointClientContext
  public static final String SEPARATOR = " ";
  public static final int SSL_DEFAULT_PORT = 443;

  // Used while creating/validating configuration form
  public static final String VALUE = "value";
  public static final String CONFIG_NAME = "name";
  public static final String TEXT = "text";
  public static final String SUBMIT = "submit";
  public static final String HIDDEN = "hidden";
  public static final String TEXTAREA = "textarea";
  public static final String TYPE = "type";
  public static final String INPUT = "input";
  public static final String CLOSE_ELEMENT = ">";
  public static final String OPEN_ELEMENT = "<";
  public static final String BREAK_LINE = "<br/>\r\n";
  public static final String PASSWORD = "Password";
  public static final String TR_END = "</tr>\r\n";
  public static final String TD_END = "</td>\r\n";
  public static final String TD_START = "<td style=\"white-space: nowrap\">";
  public static final String TD_START_FORMAT = "<td style=\"background-color: #DDDDDD\">";
  public static final String TD_START_ADDMRE = "<td colspan=\"2\" align=\"right\">\r\n";
  public static final String TR_START = "<tr valign=\"top\">\r\n";
  public static final String TH = "th";
  public static final String TH_END = "</th>\r\n";
  public static final String RADIO = "radio";
  public static final String TITLE = "title";
  public static final String CONFIG_ID = "id";
  public static final String CHECKED = "checked";
  public static final String CHECKBOX = "checkbox";

  public static final String TABLE = "table";
  public static final String END_TABLE = "</table>\r\n";
  public static final String ALIASCONTAINER = "aliasContainer";
  public static final String CELLSPACING = "cellspacing";
  public static final String CELLPADDING = "cellpadding";

  public static final String ROWS = "rows";
  public static final String ROWS_VALUE = "5";
  public static final String COLS = "cols";
  public static final String COLS_VALUE = "50";
  public static final String END_TEXTAREA = "/textarea";
  public static final String START_BOLD = "<b>";
  public static final String END_BOLD = "</b>";
  public static final String MANDATORY_FIELDS = "Mandatory_Fields";
  public static final String TEXTBOX_SIZE = "size";
  public static final String TEXTBOX_SIZE_VALUE = "50";
  public static final String ALIAS_TEXTBOX_SIZE_VALUE = "28";
  public static final String ONCHANGE = "onchange";
  public static final String ONCLICK = "onclick";
  public static final char SPACE = ' ';
  public static final char NEW_LINE = '\n';

  public static final String USERNAME = "username";
  public static final String DOMAIN = "domain";
  public static final String ADD_MORE = "Add_More";
  public static final String EMPTY_FIELD = "Empty_Field";
  public static final String REQ_FIELDS_MISSING = "Field_Is_Required";
  public static final String DUPLICATE_DOMAIN = "Duplicate_Domain";
  public static final String REASON = "Reason";
  public static final Object DOUBLE_CLOSE_PARENTHESIS = "))";
  public static final String USERNAME_FORMAT_IN_ACE = "Username Format in ACE";
  public static final String GROUPNAME_FORMAT_IN_ACE = "Groupname Format in ACE";
  public static final String CONNECT_METHOD_STANDARD_HELP = "connectMethodStandard_help";
  public static final String CONNECT_METHOD_SSL_HELP = "connectMethodSSL_help";
  public static final String LDAP_SERVER_HOST_ADDRESS = "ldapServerHostAddress";
  public static final String PORT_NUMBER = "portNumber";
  public static final String SEARCH_BASE = "searchBase";
  public static final String USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP = "useCacheToStoreLdapUserGroupsMembership";
  public static final String USE_CACHE_TO_STORE_LDAP_USER_GROUPS_MEMBERSHIP_LABEL = "use_Cache_To_Store_Ldap_UserGroups_Membership";
  public static final String INITAL_CACHE_SIZE = "initialCacheSize";
  public static final String CACHE_REFRESH_INTERVAL = "cacheRefreshInterval";
  public static final String AUTHENTICATION_TYPE = "authenticationType";
  public static final String CONNECT_METHOD = "connectMethod";
  public static final String CONNECT_METHOD_STANDARD = "Standard";
  public static final String CONNECT_METHOD_SSL = "SSL";
  public static final String AUTHENTICATION_TYPE_SIMPLE = "Simple";
  public static final String AUTHENTICATION_TYPE_ANONYMOUS = "Anonymous";

  public static final String USERNAME_CONSTANT_IN_ACL = "username";
  public static final String GROUPNAME_CONSTANT_IN_ACL = "groupname";
  public static final String DOMAIN_CONSTANT_IN_ACL = "domain";
  public static final String DEFAULT_SITE_LANDING_PAGE = "/default.aspx";
  public static final String LEFT_SQUARE_BRACKET = "[";
  public static final String RIGHT_SQUARE_BRACKET = "]";
  public static final String UDS_TABLE = "'TABLE'";
  public static final String SELECTED_DATABASE = "oracle";
  public static final String TABLE_NAME = "TABLE_NAME";
  public static final String USER_NAME = "user_name";
  public static final String GROUP_NAME = "group_name";
  public static final String NAMESPACE = "namespace";
  public static final int MINUS_THREE = -3;
  public static final int MINUS_TWO = -2;
  public static final String ON = "on";
  public static final String UNCHECKED = "unchecked";
  public static final String LDAP_DEFAULT_PORT_NUMBER = "389";
  public static final String LDAP_INITIAL_CACHE_SIZE = "1000";
  public static final String LDAP_CACHE_REFRESH_INTERVAL_TIME = "7200";
  public static final String TRUE = "true";
  public static final String WRONG_GROUPNAME_FORMAT = "Wrong_Groupname_Format";
  public static final String WRONG_USERNAME_FORMAT = "Wrong_Username_Format";
  public static final String BLANK_GROUPNAME_FORMAT = "Blank_Groupname_Format";
  public static final String BLANK_USERNAME_FORMAT = "Blank_Username_Format";
  public static final String BLANK_DOMAIN_NAME_LDAP = "Blank_Domain_Name_LDAP";
  public static final String LDAP_CONNECTVITY_ERROR = "LDAP_Connectivity_Error";
  public static final String LDAP_SERVER_HOST_ADDRESS_BLANK = "LDAP_Server_Host_Address_Blank";
  public static final String PORT_NUMBER_BLANK = "Port_Number_Blank";
  public static final String SEARCH_BASE_BLANK = "Search_Base_Blank";
  public static final String SEARCH_BASE_INVALID = "Search_Base_Invalid";
  public static final String SEARCH_BASE_INVALID_SYNTAX = "Search_Base_Invalid_Syntax";
  public static final String SEARCH_BASE_INVALID_NAME = "Search_Base_Invalid_Name";
  public static final String INVALID_PORT_NUMBER = "Invalid_Port_Number";
  public static final String ONKEY_PRESS = "onkeypress=";
  public static final String DEFAULT_CACHE_REFRESH_INTERVAL = "7200";
  public static final String DEFAULT_INITAL_CACHE_SIZE = "10000";
  public static final String GROUPS = "groups";
  public static final String EQUAL_TO = "=";
  public static final String INVALID_LDAP_HOST_ADDRESS = "Invalid_LDAP_HOST_Address_Blank";
  public static final String INVALID_INITIAL_CACHE_SIZE = "Invalid_Initial_Cache_Size";
  public static final String INVALID_CACHE_REFRESH_INTERVAL = "Invalid_Cache_Refresh_Interval";
  public static final String BLANK_INITIAL_CACHE_SIZE = "Blank_Initial_Cache_Size";
  public static final String BLANK_CACHE_REFRESH_INTERVAL = "Blank_Cache_Refresh_Interval";
  public static final String SPECIAL_CHARACTERS_IN_USERNAME_FORMAT = "Special_Characters_In_Username_Format";
  public static final String SPECIAL_CHARACTERS_IN_GROUPNAME_FORMAT = "Special_Characters_In_Groupname_Format";
  public static final String SIMPLE = "simple";
  public static final String ANONYMOUS = "ANONYMOUS";
  public static final String ADGROUPS = "adgroups";
  public static final String SPGROUPS = "spgroups";
  public static final String START_BREAK = "<br>";
  public static final String END_BREAK = "</br>";
  public static final String GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME = "groupname";
  public static final String GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN = "groupname@domain";
  public static final String GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME = "domain\\groupname";
  public static final String GROUPNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME = "NETBIOS\\sAMAccountName";
  public static final String GROUPNAME_FORMAT_IN_ACE_SAMACCOUNTNAME = "sAMAccountName";
  public static final String GROUPNAME_FORMAT_IN_ACE_DN = "dn";
  public static final String GROUPNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME = "UPPER(NETBIOS)\\lower(sAMAccountName)";
  public static final String GROUPNAME_FORMAT_IN_ACE_CN = "cn";
  public static final String USERNAME_FORMAT_IN_ACE_ONLY_USERNAME = "username";
  public static final String USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME = "username@domain";
  public static final String USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME = "domain\\username";
  public static final String USERNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME = "NETBIOS\\sAMAccountName";
  public static final String USERNAME_FORMAT_IN_ACE_SAMACCOUNTNAME = "sAMAccountName";
  public static final String USERNAME_FORMAT_IN_ACE_DN = "dn";
  public static final String USERNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME = "UPPER(NETBIOS)\\lower(sAMAccountName)";
  public static final String USERNAME_FORMAT_IN_ACE_CN = "cn";
  public static final String USERNAME_FORMAT_IN_ACE_USERPRINCIPALNAME = "userPrincipalName";
  public static final String DISABLED = "disabled";
  public static final String ON_CLICK = "onclick=";
  public static final String OFF = "off";
  public static final String LABEL_FOR = "label for";
  public static final String FORWARD_SLASH = "/";
  public static final String LABEL = "label";
  public static final String SELECT = "select";
  public static final String STYLE = "style=\"width:11em\"";
  public static final String OPTION = "option";
  public static final String SELECTED = "selected";
  public static final String CONNECTOR_NAME_COLUMN = "connectorname";
  public static final String CONNECTOR_NAME_COLUMN_CAPITAL = "CONNECTORNAME";
  public static final String SYSTEM_UPDATE = "SystemUpdate";

  // User store query constants.
  public static final int UDS_MAX_GROUP_NAME_LENGTH = 256;
  public static final String UDS_COLUMN_GROUP_NAME = "SPGroupName";
  public static final String UDS_COLUMN_USER_NAME = "SPUserName";
  }
