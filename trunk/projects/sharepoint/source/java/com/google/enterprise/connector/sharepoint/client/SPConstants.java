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

package com.google.enterprise.connector.sharepoint.client;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Used to store constants
 * 
 * @author nitendra_thakur
 */
public class SPConstants {
	// used while parsing the alias string
	public static final String SOURCE_ALIAS_SEPARATOR = "/\\$\\$EQUAL\\$\\$/";
	public static final String ALIAS_ENTRIES_SEPARATOR = "/\\$\\$CRLF\\$\\$/";

	public static final String GLOBAL_ALIAS_IDENTIFIER = "^";

	// TODO: Create similar enums for ListTypes and templates etc. This will
	// help in easy reference to the relevant values as the list of constants
	// grows. In future, we may also create another class specifically for these
	// enums.
	public static enum FeedType {
		METADATA_URL_FEED("metadata-and-URL"), CONTENT_FEED("content");
		// For backward compatibility. Earlier connectors would be using
		// "metadata-and-URL" & "content" literals
		private String feedType;

		FeedType(String feedType) {
			this.feedType = feedType;
		}

		public static FeedType getFeedType(String feedType) {
			if (METADATA_URL_FEED.toString().equalsIgnoreCase(feedType)) {
				return METADATA_URL_FEED;
			} else if (CONTENT_FEED.toString().equalsIgnoreCase(feedType)) {
				return CONTENT_FEED;
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return feedType;
		}
	}

	public static enum SPType {
		SP2007, SP2003;
		public static SPType getSPType(String spType) {
			if (SP2007.toString().equalsIgnoreCase(spType)) {
				return SP2007;
			} else if (SP2003.toString().equalsIgnoreCase(spType)) {
				return SP2003;
			} else {
				return null;
			}
		}
	}

	public static enum DocVersion {
		APPROVED("0"), REJECTED("1"), DRAFT("3"), PENDING("2");
		private String docVersion;

		DocVersion(String docVersion) {
			this.docVersion = docVersion;
		}

		@Override
		public String toString() {
			return docVersion;
		}
	}

	// used while forming URLs
	public static final String URL_SEP = "://";
	public static final String COLON = ":";
	public static final String SLASH = "/";
	public static final String DOUBLEBACKSLASH = "\\";
	public static final String AT = "@";
	public static final char SLASH_CHAR = '/';
	public static final char DOUBLEBACKSLASH_CHAR = '\\';
	public static final char AT_CHAR = '@';

	// The string to be stripped off from the atrribute's name as web service
	// return so.
	public static final String OWS = "ows_";
	public static final String METAINFO = "MetaInfo_";
	public static final String VTI = "vti_";
	public static final String ENCODED_SPACE = "_x0020_";
	public static final Pattern ATTRIBUTE_VALUE_PATTERN = Pattern.compile("^\\d+;\\#");

	public static final String UNAUTHORIZED = "(401)Unauthorized";
	public static final String SAXPARSEEXCEPTION = "org.xml.sax.SAXParseException";

	public static final String EXCLUDED_URL_DIR = "excluded-URLs";
	public static final String EXCLUDED_URL_LOG = "excluded_url";
	public static final int EXCLUDED_URL_MAX_SIZE = 52428800;
	public static final int EXCLUDED_URL_MAX_COUNT = 5;

	public static final String CONNECTIVITY_SUCCESS = "success";
	public static final String CONNECTIVITY_FAIL = "fail";

	public static final String LIST_URL_SUFFIX = "/Forms/AllItems.aspx";
	public static final String FORMS_LIST_URL_SUFFIX = "Forms";

	public static final String INVALID_TOKEN = "ERROR";

	public static final String ATTACHMENT_SUFFIX_IN_DOCID = "[ATTACHMENT]";
	public static final Pattern ATTACHMENT_SUFFIX_PATTERN = Pattern.compile("^\\[ATTACHMENT\\]\\[.+\\]");
	public static final String ALERT_SUFFIX_IN_DOCID = "[ALERT]";

	// End Point constants
	public static final String LISTS_END_POINT = "/_vti_bin/Lists.asmx";
	public static final String WEBSENDPOINT = "/_vti_bin/Webs.asmx";
	public static final String ALERTSENDPOINT = "/_vti_bin/alerts.asmx";
	public static final String GSPBULKAUTHORIZATION_ENDPOINT = "/_vti_bin/GSBulkAuthorization.asmx";
	public static final String GSPSITEDISCOVERYWS_END_POINT = "/_vti_bin/GSSiteDiscovery.asmx";
	public static final String SITEDATAENDPOINT = "/_vti_bin/SiteData.asmx";
	public static final String USERPROFILEENDPOINT = "/_vti_bin/UserProfileService.asmx";
	public static final String GSACLENDPOINT = "/_vti_bin/GssAcl.asmx";

	public static final String DEFAULT_ROWLIMIT = "1000";

	// Constant used by ListsWS for precessing the list item data returned by
	// the Web Service.
	public static final String NAME = "Name";
	public static final String DISPLAYNAME = "DisplayName";
	public static final String MODIFIED = "ows_Modified";
	public static final String FILEREF = "ows_FileRef";
	public static final String CONTENTTYPE = "ows_ContentType";
	public static final String CONTENTTYPE_INMETA = "ows_MetaInfo_ContentType";
	public static final String EDITOR = "ows_Editor";
	public static final String AUTHOR = "ows_Author";
	public static final String FILE_SIZE_DISPLAY = "ows_FileSizeDisplay";
	public static final String FILE_SIZE = "ows_File_x0020_Size";
	public static final String QUERYOPTIONS = "QueryOptions";
	public static final String VIEWATTRIBUTES = "ViewAttributes";
	public static final String SCOPE = "Scope";
	public static final String RECURSIVE = "Recursive";
	public static final String QUERY = "Query";
	public static final String ORDERBY = "OrderBy";
	public static final String DISPFORM = "DispForm.aspx?ID=";
	public static final String ID = "ows_ID";
	public static final String DISCUSSIONLASTUPDATED = "ows_DiscussionLastUpdated";
	public static final String DOCUMENT = "Document";
	public static final String URL = "ows_URL";
	public static final String BT_DISCUSSIONBOARD = "DiscussionBoard";
	public static final String CONTENT_TYPE_HEADER = "Content-Type";
	public static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	public static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	public static final String CHANGES = "Changes";
	public static final String LASTCHANGETOKEN = "LastChangeToken";
	public static final String LIST = "List";
	public static final String FIELDS = "Fields";
	public static final String FIELD = "Field";
	public static final String CHANGETYPE = "ChangeType";
	public static final String DELETE = "Delete";
	public static final String RESTORE = "Restore";
	public static final String RENAME = "Rename";
	public static final String DATA = "data";
	public static final String LIST_ITEM_COLLECTION_POSITION_NEXT = "ListItemCollectionPositionNext";
	public static final String CONTENT_TYPE_FOLDER = "Folder";

	// The minimum time out value for WS calls
	public static final int MINIMUM_TIMEOUT_FOR_WS = 1000;

	public static final String GSSLISTITEMS = "GssListItems";

	// For Alerts
	public static final String ALERTS_TYPE = "Alerts";

	// Constants used by SiteDataWS while processing the list data as returned
	// by the Web Service.
	public static final String NOREDIRECT = "NoRedirect=true";
	public static final String DOC_LIB = "DocumentLibrary";// BaseType=1 (In
	// SPS2003)
	public static final String DISCUSSION_BOARD = "DiscussionBoard";// BaseType=3
	// (In
	// SPS2003)
	public static final String SURVEYS = "Survey"; // BaseType=4 (In SPS2003)
	public static final String GENERIC_LIST = "GenericList";// BaseType=0 (In
	// SPS2003)
	public static final String ISSUE = "Issue";// BaseType=5 (In SPS2003)
	public static final String BT_SLIDELIBRARY = "SlideLibrary";
	public static final String BT_FORMLIBRARY = "FormLibrary";
	public static final String BT_TRANSLATIONMANAGEMENTLIBRARY = "TranslationManagementLibrary";
	public static final String BT_REPORTLIBRARY = "ReportLibrary";
	public static final String BT_TRANSLATOR = "Translator";
	public static final String BT_PROJECTTASK = "ProjectTask";
	public static final String BT_SITESLIST = "SitesList";
	public static final String ORIGINAL_BT_SLIDELIBRARY = "2100";
	public static final String ORIGINAL_BT_FORMLIBRARY = "XMLForm";
	public static final String ORIGINAL_BT_TRANSLATIONMANAGEMENTLIBRARY = "1300";
	public static final String ORIGINAL_BT_REPORTLIBRARY = "433";
	public static final String ORIGINAL_BT_TRANSLATOR = "1301";
	public static final String ORIGINAL_BT_PROJECTTASK = "GanttTasks";
	public static final String ORIGINAL_BT_SITESLIST = "300";
	public static final String ORIGINAL_BT_LINKS = "Links";
	public static final String NO_TEMPLATE = "No Template";
	public static final String ATTR_DEFAULTVIEWURL = "DefaultViewUrl";
	public static final String ATTR_DESCRIPTION = "Description";
	public static final String ATTR_TITLE = "Title";

	public static final String WEB_TITLE = "Title";
	public static final String SITE = "Site";

	// Constants used by SPDocument for various info related to documents
	public static final String SPAUTHOR = "sharepoint:author";
	public static final String LIST_GUID = "sharepoint:listguid";
	public static final String PARENT_WEB_TITLE = "sharepoint:parentwebtitle";
	public static final String OBJECT_TYPE = "google:objecttype";
	public static final String NO_OBJTYPE = "No Object Type";
	public static final String OBJTYPE_WEB = "Web";
	public static final String OBJTYPE_ATTACHMENT = "Attachment";
	public static final String OBJTYPE_LIST_ITEM = "ListItem";// when no type is
	// recv through
	// ws call
	public static final String NO_AUTHOR = "No author";
	public static final String DOC_TOKEN = "|";
	public static final String DOC_TOKEN_META_AND_URL_FEED = "?ID=";

	// Other Misc. characters used mostly by SharePointClientUtils and during
	// processing the data returned by WS.
	public static final String BLANK_STRING = "";
	public static final String COMMA = ",";
	public static final String HASH = "#";
	public static final String MINUS = "-";
	public static final String DOLLAR = "$";
	public static final String CARET = "^";
	public static final String CONTAINS = "contains:";
	public static final String REGEXP = "regexp:";
	public static final String REGEXP_CASE = "regexpCase:";
	public static final String REGEXP_IGNORE_CASE = "regexpIgnoreCase:";

	// Used in SPDocumentList.
	public static final int MINUS_ONE = -1;

	// Used by SharePointClientContext
	public static final String SEPARATOR = " ";
	public static final int SSL_DEFAULT_PORT = 443;

	// Used in the the state file info
	public static final String STATE_ID = "ID";
	public static final String STATE_TITLE = "Title";
	public static final String STATE_TYPE = "Type";
	public static final String STATE_URL = "URL";
	// TODO: rename it to CurrentChangeToken in future to make better sense.
	// Keeping it as it is for now so that already running connectors does not
	// break
	public static final String STATE_CHANGETOKEN = "ChangeToken";
	// TODO: rename it to NextChangeToken in future to make better sense.
	// Keeping it as it is for now so that already running connectors does not
	// break
	public static final String STATE_CACHED_CHANGETOKEN = "CachedChangeToken";

	public static final String STATE_ACLCHANGETOKEN = "AclChangeToken";
	public static final String STATE_ACLNEXTCHANGETOKEN = "AclNextChangeToken";
	public static final String STATE_ISACLCHANGED = "IsAclChanged";
	public static final String STATE_LASTDOCIDCRAWLEDFORACL = "LastDocIdCrawledForAcl";

	public static final String STATE_EXTRAIDS_FOLDERS = "FolderItemIDs";
	public static final String STATE_EXTRAIDS_ATTACHMENTS = "Attachments";
	public static final String STATE_EXTRAIDS_ALERTS = "Alerts";
	public static final String STATE_BIGGESTID = "BiggestID";
	public static final String STATE_DELETED_LIST_ITEMIDS = "DeletedListItemIDs";
	public static final String STATE_LASTDOCCRAWLED = "LastDocCrawled";
	public static final String STATE_LASTMODIFIED = "LastModified";
	public static final String STATE_FEEDTYPE = "FeedType";

	@Deprecated
	public static final String STATE_FOLDER_LEVEL = "FolderLevel";

	public static final String STATE_PARENT_FOLDER_PATH = "ParentFolderPath";
	public static final String STATE_PARENT_FOLDER_ID = "ParentFolderID";
	public static final String STATE_RENAMED_FOLDER_PATH = "RenamedFolderPath";
	public static final String STATE_RENAMED_FOLDER_ID = "RenamedFolderID";
	public static final String STATE_CONTENT_TYPE = "ContentType";
	public static final String CONTENT_TYPE_DOCUMENT = "Document";
	public static final String STATE_INSERT_TIME = "InsertionTime";
	public static final String STATE_SPTYPE = "SPType";
	public static final String STATE_WEB_TITLE = "WebTitle";
	public static final String STATE_ACTION = "Action";
	public static final String LIST_STATE = "ListState";
	public static final String WEB_STATE = "WebState";
	public static final String STATE = "State";
	public static final String LAST_FULL_CRAWL_DATETIME = "LastFullCrawlDateTime";
	public static final String LAST_CRAWLED_DATETIME = "LastCrawledDateTime";
	public static final String STATE_ATTR_CDATA = "CDATA";
	public static final String STATE_ATTR_ID = "ID";
	public static final String STATE_ATTR_IDREF = "IDREF";
	public static final String STATE_NOCRAWL = "NoCrawl";
	public static final String STATE_CRAWLASPXPAGES = "CrawlASPXPages";
	// To persist the list of renamed folders that need to be processed on
	// connector restart
	public static final String STATE_RENAMED_FOLDER_LIST = "RenamedFolderList";
	public static final String STATE_RENAMED_FOLDER_NODE = "RenamedFolder";
	public static final String STATE_RENAMED_FOLDERPATH = "Path";

	public static final String CONNECTOR_NAME = "Sharepoint";
	public static final String CONNECTOR_PREFIX = "_state.xml";
	public static final String LAST_CRAWLED_WEB_ID = "LastCrawledWebStateID";
	public static final String LAST_CRAWLED_LIST_ID = "LastCrawledListStateID";
	public static final String FULL_RECRAWL_FLAG = "FullRecrawlFlag";
	public static final String CONNECTOR_INSTANCE_XML = "connectorInstance.xml";
	public static final String CONNECTOR_INSTANCE_ROOT = "beans";
	public static final String CONNECTOR_INSTANCE_BEAN = "bean";
	public static final String CONNECTOR_INSTANCE_PROPERTY = "property";
	public static final String CONNECTOR_INSTANCE_NAME = "name";
	public static final String CONNECTOR_INSTANCE_WHITELIST = "whiteList";
	public static final String CONNECTOR_INSTANCE_BLACKLIST = "blackList";
	public static final String CONNECTOR_INSTANCE_LIST = "list";
	public static final String CONNECTOR_INSTANCE_VALUE = "value";

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
	public static final String KDC_SERVER = "kdcserver";
	public static final String DOMAIN = "domain";
	public static final String SHAREPOINT_URL = "sharepointUrl";
	public static final String EXCLUDED_URLS = "excludedURls";
	public static final String INCLUDED_URLS = "includedURls";
	public static final String MYSITE_BASE_URL = "mySiteBaseURL";
	public static final String ALIAS_MAP = "aliasMap";
	public static final String AUTHORIZATION = "authorization";
	public static final String SHAREPOINT_CRAWLING_OPTION = "sharepointCrawlingOptions";
	public static final String AUTHZ_BY_CONNECTOR = "AuthZ_By_Connector";
	public static final String HELP_AUTHZ_BY_CONNECTOR = "Help_AuthZ_By_Connector";
	public static final String AUTHZ_BY_GSA = "AuthZ_By_GSA";
	public static final String HELP_AUTHZ_BY_GSA = "Help_AuthZ_By_GSA";
	public static final String ALIAS_SOURCE_PATTERN = "Alias_Source_Pattern";
	public static final String MALFORMED_ALIAS_PATTERN = "Malformed_Alias_Pattern";
	public static final String ALIAS = "Alias";
	public static final String ALIAS_SOURCE_PATTERN_HELP = "Alias_Source_Pattern_Help";
	public static final String ALIAS_HELP = "Alias_Help";
	public static final String ADD_MORE = "Add_More";
	public static final String EMPTY_FIELD = "Empty_Field";
	public static final String DUPLICATE_ALIAS = "Duplicate_Alias";
	public static final String MALFORMED_URL = "MalFormedURL";
	public static final String MALFORMED_MYSITE_URL = "MalFormedMySiteURL";
	public static final String REQ_FIELDS_MISSING = "Field_Is_Required";
	public static final String REQ_FQDN_URL = "Url_Entered_Should_Be_Fully_Qualified";
	public static final String REQ_FQDN_MYSITE_URL = "MySite_Url_Entered_Should_Be_Fully_Qualified";
	public static final String CANNOT_CONNECT = "Cannot_Connect";
	public static final String CANNOT_CONNECT_MYSITE = "Cannot_Connect_MySite";
	public static final String DUPLICATE_DOMAIN = "Duplicate_Domain";
	public static final String ENDPOINT_NOT_FOUND = "EndPoint_not_Found";
	public static final String BULKAUTH_ERROR_CRAWL_URL = "BulkAuth_Error_CrawlURL";
	public static final String BULKAUTH_ERROR_MYSITE_URL = "BulkAuth_Error_MySiteURL";
	public static final String INCLUDED_PATTERN_MISMATCH = "Included_Pattern_Mismatch";
	public static final String EXCLUDED_PATTERN_MATCH = "Excluded_Pattern_Match";
	public static final String INVALID_INCLUDE_PATTERN = "Invalid_Include_Pattern";
	public static final String INVALID_EXCLUDE_PATTERN = "Invalid_Exclude_Pattern";
	public static final String REASON = "Reason";
	public static final String KERBEROS_KDC_HOST_BLANK = "Kerberos_Kdc_Hostname_Blank";
	public static final String USE_SP_SEARCH_VISIBILITY = "useSPSearchVisibility";
	public static final String USE_SP_SEARCH_VISIBILITY_LABEL = "Use_SP_SearchVisibility";
	public static final String USE_SP_SEARCH_VISIBILITY_HELP = "Use_SP_SearchVisibility_Help";
	public static final Object DOUBLE_CLOSE_PARENTHESIS = "))";
	public static final String PUSH_ACLS = "pushAcls";
	public static final String PUSH_ACLS_LABEL = "push_Acls";
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

	public static final String CHECKPOINT_VALUE = "SharePoint";
	public static final int MAX_PORT_VALUE = 65535;

	// HTTP response header constants
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	// Kerberos related constants
	public static final String CONFIG_KRB5 = "config/krb5.conf";
	public static final String CONFIG_LOGIN = "config/login.conf";
	public static final String NEGOTIATE = "Negotiate";
	public static final String FILE_KRB5 = "krb5.conf";
	public static final String FILE_LOGIN = "login.conf";
	public static final String VAR_KRB5_REALM_UPPERCASE = "{REALM}";
	public static final String VAR_KRB5_REALM_LOWERCASE = "{realm}";
	public static final String VAR_KRB5_KDC_SERVER = "{kdcserver}";
	public static final String UTF_8 = "UTF-8";
	public static final String SYS_PROP_AUTH_LOGIN_CONFIG = "java.security.auth.login.config";
	public static final String SYS_PROP_AUTH_KRB5_CONFIG = "java.security.krb5.conf";
	public static final String SYS_PROP_AUTH_USESUBJETCREDSONLY = "javax.security.auth.useSubjectCredsOnly";
	public static final String FALSE = "false";
	public static final String PERIOD = ".";

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
	public static final String USERNAME_FORMAT_IN_ACE_ONLY_USERNAME = "username";
	public static final String USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME = "username@domain";
	public static final String USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME = "domain\\username";
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
	public static final boolean EDIT_MODE = true;
	public static final int DEFAULT_TIMEOUT_FOR_WS = 300000;
	public static final String MODERATION_STATUS = "ows__ModerationStatus";
	public static final String FEED_UNPUBLISHED_CONTENT = "feedUnPublishedDocuments";
	public static final String FEED_UNPUBLISHED_CONTENT_LABEL = "feed_Un_Published_Documents";
	public static final String META_URL_FEED_DOC_TOKEN = "?";
	public static final String ATTACHMENTS = "Attachments";
	public static final CharSequence ALERTS_EQUALTO = "Alert=";

	public static class SPBasePermissions {
		public static final String EMPTYMASK = "EmptyMask";
		public static final String VIEWLISTITEMS = "ViewListItems";
		public static final String ADDLISTITEMS = "AddListItems";
		public static final String EDITLISTITEMS = "EditListItems";
		public static final String DELETELISTITEMS = "DeleteListItems";
		public static final String APPROVEITEMS = "ApproveItems";
		public static final String OPENITEMS = "OpenItems";
		public static final String VIEWVERSIONS = "ViewVersions";
		public static final String DELETEVERSIONS = "DeleteVersions";
		public static final String CANCELCHECKOUT = "CancelCheckout";
		public static final String MANAGEPERSONALVIEWS = "ManagePersonalViews";
		public static final String MANAGELISTS = "ManageLists";
		public static final String VIEWFORMPAGES = "ViewFormPages";
		public static final String OPEN = "Open";
		public static final String VIEWPAGES = "ViewPages";
		public static final String ADDANDCUSTOMIZEPAGES = "AddAndCustomizePages";
		public static final String APPLYTHEMEANDBORDER = "ApplyThemeAndBorder";
		public static final String APPLYSTYLESHEETS = "ApplyStyleSheets";
		public static final String VIEWUSAGEDATA = "ViewUsageData";
		public static final String CREATESSCSITE = "CreateSSCSite";
		public static final String MANAGESUBWEBS = "ManageSubwebs";
		public static final String CREATEGROUPS = "CreateGroups";
		public static final String MANAGEPERMISSIONS = "ManagePermissions";
		public static final String BROWSEDIRECTORIES = "BrowseDirectories";
		public static final String BROWSEUSERINFO = "BrowseUserInfo";
		public static final String ADDDELPRIVATEWEBPARTS = "AddDelPrivateWebParts";
		public static final String UPDATEPERSONALWEBPARTS = "UpdatePersonalWebParts";
		public static final String MANAGEWEB = "ManageWeb";
		public static final String USECLIENTINTEGRATION = "UseClientIntegration";
		public static final String USEREMOTEAPIS = "UseRemoteAPIs";
		public static final String MANAGEALERTS = "ManageAlerts";
		public static final String CREATEALERTS = "CreateAlerts";
		public static final String EDITMYUSERINFO = "EditMyUserInfo";
		public static final String ENUMERATEPERMISSIONS = "EnumeratePermissions";
		public static final String FULLMASK = "FullMask";
	}
}
