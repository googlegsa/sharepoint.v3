// Copyright 2011 Google Inc.
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

package com.google.enterprise.connector.sharepoint.ldap;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.ldap.UserGroupsService.LdapConnectionSettings;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.spi.Principal;

import java.util.Set;

import javax.naming.ldap.LdapContext;

/**
 * Provides access to the Directory service.
 *
 * @author nageswara_sura
 */
public interface LdapService {

  /**
   * Returns {@link LdapContext} for a given {@link LdapConnectionSettings}.
   *
   * @return context object.
   */
  LdapContext getLdapContext();

  /**
   * Returns all groups (AD groups + SP groups) for the search user by querying
   * LDAP directory server and User Data Store respectively. The class that
   * implements this interface should deal with user group’s cache. I.e. If user
   * groups cache is enabled on the connector configuration page, the
   * implementation should check for the groups in cache. If there is no entry
   * found for the search user, the implementation should query LDAP server to
   * get ADGroups and then store in cache.
   *
   * @param sharepointClientContext the sharepointClientContext
   * @param searchUser the searchUser
   * @throws SharepointException
   */
  Set<Principal> getAllGroupsForSearchUser(
      SharepointClientContext sharepointClientContext, String searchUser)
      throws SharepointException;
}
