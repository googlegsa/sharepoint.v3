// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.adgroups;

import com.google.enterprise.connector.sharepoint.dao.UserGroupMembership;
import com.google.enterprise.connector.spi.AuthenticationIdentity;
import com.google.enterprise.connector.spi.RepositoryException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.ldap.LdapContext;

// TODO: investigate if reconfiguration recreates this class, if not delete state on reconf 

public class MultiLdapService implements LdapService {
	private static final Logger LOGGER = Logger
			.getLogger(MultiLdapService.class.getName());
	

	ConnectorContext scc;

	public MultiLdapService(ConnectorContext scc) {
	  this.scc = scc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.enterprise.connector.sharepoint.ldap.LdapService#getLdapContext
	 * ()
	 */
	@Override
	public LdapContext getLdapContext() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.enterprise.connector.sharepoint.ldap.LdapService#
	 * getAllParentGroups(java.lang.String, java.util.Set)
	 */
	@Override
	public void getAllParentGroups(String groupName, Set<String> parentGroups) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.enterprise.connector.sharepoint.ldap.LdapService#getAllLdapGroups
	 * (java.lang.String)
	 */
	@Override
	public Set<String> getAllLdapGroups(String userName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.enterprise.connector.sharepoint.ldap.LdapService#
	 * getSamAccountNameForSearchUser(java.lang.String)
	 */
	@Override
	public String getSamAccountNameForSearchUser(String userName) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.enterprise.connector.sharepoint.ldap.LdapService#
	 * getAllGroupsForSearchUser
	 * (com.google.enterprise.connector.sharepoint.client
	 * .Context, java.lang.String)
	 */
	@Override
	public Set<String> getAllGroupsForSearchUser(
			ConnectorContext sharepointClientContext,
			AuthenticationIdentity identity) throws RepositoryException {
		Set<LdapEntity> groups = new HashSet<LdapEntity>();
		Set<String> formattedGroups = new HashSet<String>();
		if (scc.multiCrawl == null)
		{ 
		  LOGGER.info("crawl not happened");
		  return formattedGroups;
		}

		LdapEntity user = scc.multiCrawl.findUser(identity);
		
		if (user == null) {
		  LOGGER.info("user not found");
		  return formattedGroups;
        }
		user.getAllGroups(groups);

		LOGGER.info("user is member of: " + user.memberOf.size() + " groups");
        LOGGER.info("user is member of: " + user);
		
		for (LdapEntity e : groups) {
		  formattedGroups.add(e.format(scc.getUsernameFormatInAce(), scc.getGroupnameFormatInAce()));
		}
		
		for (LdapEntity e: scc.multiCrawl.wellKnownEntities) {
		  formattedGroups.add(e.format(scc.getUsernameFormatInAce(), scc.getGroupnameFormatInAce()));
		}
		
		List<UserGroupMembership> groupMembershipList = null;
		if (null != scc.multiCrawl.sharepointClientContext.getUserDataStoreDAO()) {
			groupMembershipList = scc.getUserDataStoreDAO().getAllMembershipsForSearchUserAndLdapGroups(formattedGroups, user.format(scc.getUsernameFormatInAce(), scc.getGroupnameFormatInAce()));
			for (UserGroupMembership userGroupMembership : groupMembershipList) {
		        // append name space to SP groups.
				String groupName = new StringBuffer().append(SPConstants.LEFT_SQUARE_BRACKET).append(userGroupMembership.getNamespace()).append(SPConstants.RIGHT_SQUARE_BRACKET).append(userGroupMembership.getGroupName()).toString();					
				formattedGroups.add(groupName);
			}
		}
		
		return formattedGroups;
	}
}
