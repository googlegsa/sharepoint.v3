//Copyright 2011 Google Inc.

//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.ldap;

import com.google.enterprise.connector.sharepoint.ldap.LdapServiceImpl.LdapConnectionSettings;

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
     * @param domain
     * @return context object.
     */
    LdapContext getLdapContext();

    /**
     * Fetches all parent groups for a given group that the user belongs to and
     * it will recursively search parent groups for all direct groups.
     *
     * @param groupName to which the search user is direct member of.
     * @param parentGroups list of all parent groups for a given group name.
     */
    void getAllParentGroups(String groupName, final Set<String> parentGroups);

    /**
     * Retrieves all direct and indirect groups that the user belongs to in
     * Directory service implementation.
     *
     * @param userName Search user name
     * @return set of LDAP/AD groups that the search user belongs to.
     */
    Set<String> getAllLdapGroups(String userName);

    /**
     * Retrieves SAM account name for a given search user to start querying
     * LDAP/AD server to get all direct groups that the user belongs to.
     *
     * @param userName given search user name
     * @return a search user name
     */
    String getSamAccountNameFromSearchUser(String userName);

}
