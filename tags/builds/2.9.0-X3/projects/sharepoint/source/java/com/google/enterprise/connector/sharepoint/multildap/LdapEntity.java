package com.google.enterprise.connector.sharepoint.multildap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.ldap.LdapConstants;

public class LdapEntity {
	private static final Logger LOGGER = Logger.getLogger(LdapEntity.class.getName());
	
	public String dn;
	public String sAMAccountName;
	public String userPrincipalName;
	public String primaryGroupId;
	public String sid;
	public LdapServer server;
	public ArrayList<LdapEntity> memberOf;
	
	public LdapEntity(String dn) {
	  this.dn = dn;
	  memberOf = new ArrayList<LdapEntity>();
	}
	
	public void getAllGroups(Set<LdapEntity> groups) {
		for (LdapEntity e : memberOf) {
			if (!groups.contains(e)) {
				groups.add(e);
				e.getAllGroups(groups);
			}
		}
	}
	
	@Override
	public String toString() {
		Set<LdapEntity> groups = new HashSet<LdapEntity>();
		getAllGroups(groups);
		
		StringBuilder sb = new StringBuilder();
		for (LdapEntity e: groups) {
			sb.append(e.dn);
			sb.append(", ");
		}
		return sb.toString();
	}
	
	/**
	 * Returns commonName for the given user/group while making LDAP search query to get
	 * all parents groups for a given group we need to retrieve the DN name for a
	 * group.
	 * 
	 * @return group DN from group name.
	 */
	
	public String getCommonName() {
		// LDAP queries return escaped commas to avoid ambiguity, find first not escaped comma
		int comma = dn.indexOf(SPConstants.COMMA);
		while (comma > 0 && comma < dn.length() && (dn.charAt(comma - 1) == SPConstants.DOUBLEBACKSLASH_CHAR)) {
			comma = dn.indexOf(SPConstants.COMMA, comma + 1);
		}
		String tmpGroupName = dn.substring(0, comma > 0 ? comma : dn.length());
		tmpGroupName = tmpGroupName.substring(tmpGroupName.indexOf(SPConstants.EQUAL_TO) + 1);
		tmpGroupName = tmpGroupName.replace(SPConstants.DOUBLEBACKSLASH, SPConstants.BLANK_STRING);
		return tmpGroupName;
	}
	
	public String getDC() {
		return dn.substring(dn.indexOf("DC="));
	}
	
	/**
	 * Escapes special characters used in string literals for LDAP search filters
	 * 
	 * @return escaped dinstinguishedName for use in LDAP string 
	 */
	public String dnEscaped() {
		StringBuilder buffer = new StringBuilder(dn.length() * 2);
		for (int i = 0; i < dn.length(); ++i) {
			char c = dn.charAt(i);
			  if (LdapConstants.ESCAPE_CHARACTERS.indexOf(c) != -1) {
			  String escape = (c < 16) ? "\\0" : "\\";
              buffer.append(escape).append(Integer.toHexString(c));
			} else {
			   buffer.append(c);
			}
		}
		return buffer.toString();
	}
	
	public String getPrimaryGroupSid(String primaryGroupId) {
		return sid.substring(0, sid.lastIndexOf('-')+1) + primaryGroupId;
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
	  
	  if (userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME)) {
        return server.nETBIOSName + "\\" + sAMAccountName;
      } else if (userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_ONLY_USERNAME) || 
          userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_SAMACCOUNTNAME)) {
        return sAMAccountName;
      } else if (userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_DN)) {
        return dn;
      } else if (userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_CN)) {
        return getCommonName();
      } else if (userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME) ||
          userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_USERNAME)) {
        return server.nETBIOSName.toUpperCase() + "\\" + sAMAccountName.toLowerCase();
      } else if (userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_USERNAME_AT_DOMAINNAME) ||
          userNameFormatInAce.equalsIgnoreCase(SPConstants.USERNAME_FORMAT_IN_ACE_USERPRINCIPALNAME)) {
        return userPrincipalName;
      } else {
        return sAMAccountName.toLowerCase();
      }
	}
	
	public String formatGroup(String groupFormatInAce) {
	  if (groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_NETBIOS_NAME_SLASH_SAMACCOUNTNAME)) {
        return server.nETBIOSName + "\\" + sAMAccountName;
	  } else if (groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_ONLY_GROUP_NAME) || 
          groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_SAMACCOUNTNAME)) {
        return sAMAccountName;
	  } else if (groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_DN)) {
	    return dn;
	  } else if (groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_CN)) {
	    return getCommonName();
	  } else if (groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_UPPER_NETBIOS_SLASH_LOWER_SAMACCOUNTNAME) ||
	      groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_DOMAINNAME_SLASH_GROUPNAME)) {
	    return server.nETBIOSName.toUpperCase() + "\\" + sAMAccountName.toLowerCase();
	  } else if (groupFormatInAce.equalsIgnoreCase(SPConstants.GROUPNAME_FORMAT_IN_ACE_GROUPNAME_AT_DOMAIN)) {
	    return sAMAccountName + SPConstants.AT + server.hostName;
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
}
