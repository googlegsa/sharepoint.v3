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

import com.google.enterprise.connector.sharepoint.client.SPConstants;

/**
 * @author nageswara_sura
 */
public class LdapConstants {

  public static final String COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
  public static final Object PREFIX_FOR_PARENTS_GROUPS_FILTER = "(&(objectClass=group)(CN=";
  public static final Object PREFIX_FOR_DIRECT_GROUPS_FILTER = "(&(objectClass=user)(sAMAccountName=";
  public static final String RETURN_ATTRIBUTES_DIRECT_GROUPS_LIST = "memberOf";
  public static final String ESCAPE_CHARACTERS = "\\*()\0/";

  public static final int DEFAULT_PORT = 389;

  public enum Method {
    STANDARD, SSL;
    static Method getDefault() {
      return STANDARD;
    }

    public String toString() {
      if (this.equals(STANDARD)) {
        return SPConstants.CONNECT_METHOD_STANDARD;
      } else {
        return SPConstants.CONNECT_METHOD_SSL;
      }
    }
  }

  // Specifies the authentication mechanism to use while connecting to LDAp
  // directory server. When the initial context is created, the underlying
  // LDAP service provider extracts the authentication information from these
  // environment properties and uses the LDAP "bind" operation to pass them to
  // the server
  public enum AuthType {
    ANONYMOUS, SIMPLE;
    static AuthType getDefault() {
      return ANONYMOUS;
    }

    public String toString() {
      if (this.equals(ANONYMOUS)) {
        return SPConstants.AUTHENTICATION_TYPE_ANONYMOUS;
      } else {
        return SPConstants.AUTHENTICATION_TYPE_SIMPLE;
      }
    }
  }

  public enum ServerType {
    ACTIVE_DIRECTORY, DOMINO, OPENLDAP, GENERIC;
    static ServerType getDefault() {
      return ACTIVE_DIRECTORY;
    }
  }

  public enum LdapConnectionError {
    AuthenticationNotSupportedException, NamingException, AuthenticationFailedException, CommunicationException;
  }

}
