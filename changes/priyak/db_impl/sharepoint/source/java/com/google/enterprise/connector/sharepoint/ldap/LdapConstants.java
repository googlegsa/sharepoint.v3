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

/**
 * @author nageswara_sura
 */
public class LdapConstants {

    public static final String COM_SUN_JNDI_LDAP_LDAP_CTX_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    public static final Object PREFIX_FOR_PARENTS_GROUPS_FILTER = "(&(objectClass=group)(CN=";
    public static final Object PREFIX_FOR_DIRECT_GROUPS_FILTER = "(&(objectClass=user)(sAMAccountName=";
    public static final String RETURN_ATTRIBUTES_DIRECT_GROUPS_LIST = "memberOf";

    public enum ErrorMessages {
        CONNECTOR_INSTANTIATION_FAILED, MISSING_FIELDS, UNKNOWN_CONNECTION_ERROR, NO_RESULTS_FOR_GIVEN_SEARCH_STRING, ;
        public static ErrorMessages safeValueOf(String v) {
            return LdapConstants.safeValueOf(ErrorMessages.class, v);
        }
    }

    public enum AuthType {
        ANONYMOUS, SIMPLE;
        public static ErrorMessages safeValueOf(String v) {
            return LdapConstants.safeValueOf(ErrorMessages.class, v);
        }

        static AuthType getDefault() {
            return ANONYMOUS;
        }
    }

    public enum Method {
        STANDARD, SSL;
        public static ErrorMessages safeValueOf(String v) {
            return LdapConstants.safeValueOf(ErrorMessages.class, v);
        }

        static Method getDefault() {
            return STANDARD;
        }
    }

    public enum ServerType {
        ACTIVE_DIRECTORY, DOMINO, OPENLDAP, GENERIC;
        public static ErrorMessages safeValueOf(String v) {
            return LdapConstants.safeValueOf(ErrorMessages.class, v);
        }

        static ServerType getDefault() {
            return GENERIC;
        }
    }

    public enum LdapConnectionError {
        AuthenticationNotSupported, NamingException, IOException, CommunicationException;
        public static ErrorMessages safeValueOf(String v) {
            return LdapConstants.safeValueOf(ErrorMessages.class, v);
        }
    }

    public static final int DEFAULT_PORT = 389;

    /**
     * Wraps Enum.valueOf so it returns null if the string is not recognized
     */
    public static <T extends Enum<T>> T safeValueOf(Class<T> enumType,
            String name) {
        if (name == null || name.length() < 1) {
            return null;
        }
        T instance = null;
        try {
            instance = Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException e) {
            instance = null;
        }
        return instance;
    }

}
