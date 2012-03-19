// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.adgroups;

import java.text.Collator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to hold random utility functions
 *
 * @author nitendra_thakur
 */
public final class Util {
  private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

  /**
   * return collator for default locale
   */
  public static Collator getCollator() {
    final Collator collator = Collator.getInstance();
    collator.setStrength(Collator.PRIMARY);
    return collator;
  }

  /**
   * return collator for a given locale
   *
   * @param locale
   */
  public static Collator getCollator(final Locale locale) {
    if (locale == null) {
      return getCollator();
    }
    final Collator collator = Collator.getInstance(locale);
    collator.setStrength(Collator.PRIMARY);
    return collator;
  }

  /**
   * return username in the format domain\\username or groupname in the format
   * domain\\groupname.
   *
   * @param name user or group name
   * @param domain
   */
  public static String getUserNameWithDomain(final String name,
      final String domain) {
    String tmpName = name;
    if (name == null) {
      LOGGER.log(Level.FINEST, "returning [ " + name + " for input [ "
          + tmpName + " ], domain [ " + domain + " ]. ");
      return null;
    }
    String modified_name = null;
    if (name.lastIndexOf(SPConstants.AT) != -1) {
      final String[] user_or_group_and_domain = name.split(SPConstants.AT);
      // user@domain or group@domain
      if ((user_or_group_and_domain != null)
          && (user_or_group_and_domain.length == 2)) {
        modified_name = user_or_group_and_domain[1]
            + SPConstants.DOUBLEBACKSLASH + user_or_group_and_domain[0];
        // convert to domain\\user format
        LOGGER.log(Level.FINEST, "returning [ " + modified_name
            + " for input [ " + tmpName + " ], domain [ " + domain + " ]. ");
        return modified_name;
      }
    } else if (name.lastIndexOf(SPConstants.SLASH) != SPConstants.MINUS_ONE) {
      final String[] user_and_domain = name.split(SPConstants.SLASH);// domain/user
      if ((user_and_domain != null) && (user_and_domain.length == 2)) {
        modified_name = user_and_domain[0] + SPConstants.DOUBLEBACKSLASH
            + user_and_domain[1];
        // convert to domain\\user format
        LOGGER.log(Level.FINEST, "returning [ " + modified_name
            + " for input [ " + tmpName + " ], domain [ " + domain + " ]. ");
        return modified_name;
      }
    } else if (name.lastIndexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
      LOGGER.log(Level.FINEST, "returning [ " + name + " for input [ "
          + tmpName + " ], domain [ " + domain + " ]. ");
      return name;
    } else if (null != domain) {
      modified_name = domain + SPConstants.DOUBLEBACKSLASH + name;
      LOGGER.log(Level.FINEST, "returning [ " + modified_name + " for input [ "
          + tmpName + " ], domain [ " + domain + " ]. ");
      return modified_name;
    }
    LOGGER.log(Level.FINEST, "returning [ " + name + " for input [ " + tmpName
        + " ], domain [ " + domain + " ]. ");
    return name;
  }

  /**
   * return username in the format username@domain or groupname in the format
   * domain\\groupname.
   *
   * @param name user or group name
   * @param domain
   */
  public static String getUserNameAtDomain(String name, final String domain) {
    String tmpname = name;
    if (name == null) {
      LOGGER.log(Level.WARNING, "returning [ " + name + " for input [ "
          + tmpname + " ], domain [ " + domain + " ]. ");
      return null;
    }
    String modified_Name = null;
    if (name.lastIndexOf(SPConstants.DOUBLEBACKSLASH) != -1) {
      name = name.replace(SPConstants.DOUBLEBACKSLASH_CHAR, SPConstants.SLASH_CHAR);
      // else gives pattern exception while parsing
    }
    if (name.lastIndexOf(SPConstants.SLASH) != -1) {
      final String[] user_or_group_and_domain = name.split(SPConstants.SLASH);
      if ((user_or_group_and_domain != null)
          && (user_or_group_and_domain.length == 2)) {
        modified_Name = user_or_group_and_domain[1] + SPConstants.AT
            + user_or_group_and_domain[0];
        // convert to user@domain or group@domain format
        LOGGER.log(Level.FINEST, "returning [ " + modified_Name
            + " for input [ " + tmpname + " ], domain [ " + domain + " ]. ");
        return modified_Name;
      }
    } else if (name.lastIndexOf(SPConstants.AT) != -1) {
      LOGGER.log(Level.FINEST, "returning [ " + name + " for input [ "
          + tmpname + " ], domain [ " + domain + " ]. ");
      // when username/groupname is already in required format
      return name;
    } else if (null != domain) {
      modified_Name = name + SPConstants.AT + domain;
      LOGGER.log(Level.FINEST, "returning [ " + modified_Name + " for input [ "
          + tmpname + " ], domain [ " + domain + " ]. ");
      return modified_Name;
    }
    LOGGER.log(Level.FINEST, "returning [ " + name + " for input [ " + tmpname
        + " ], domain [ " + domain + " ]. ");
    return name;
  }

  /**
   * finds and return domain from a username
   *
   * @param userName
   */
  public static String getDomainFromUsername(String userName) {
    String domain = null;
    if (userName != null) {
      if (userName.indexOf(SPConstants.AT_CHAR) != -1) {
        final String[] cred = userName.split(SPConstants.AT);
        if ((cred != null) && (cred.length == 2)) {
          domain = cred[1];
        }
      } else if (userName.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
        userName = userName.replace(SPConstants.DOUBLEBACKSLASH_CHAR, SPConstants.SLASH_CHAR);
        final String[] cred = userName.split(SPConstants.SLASH);
        if ((cred != null) && (cred.length == 2)) {
          domain = cred[0];
        }
      }
    }
    LOGGER.log(Level.FINEST, "domain found as " + domain + " for username [ "
        + userName + " ]. ");
    return domain;
  }

  /**
   * Parses a username to get the username without domain info
   *
   * @param userName
   */
  public static String getUserFromUsername(String userName) {
    String tmpUsername = userName;
    String user = userName;
    if (userName != null) {
      if (userName.indexOf(SPConstants.AT_CHAR) != -1) {
        final String[] cred = userName.split(SPConstants.AT);
        if ((cred != null) && (cred.length == 2)) {
          user = cred[0];
        }
      } else if (userName.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE) {
        userName = userName.replace(SPConstants.DOUBLEBACKSLASH_CHAR, SPConstants.SLASH_CHAR);
        final String[] cred = userName.split(SPConstants.SLASH);
        if ((cred != null) && (cred.length == 2)) {
          user = cred[1];
        }
      }
    }
    LOGGER.log(Level.FINEST, "input [ " + tmpUsername + " ], output [ " + user
        + " ]. ");
    return user;
  }

  /**
   * COnversion: domain\\username <=> username@domain
   *
   * @param userName
   */
  public static String switchUserNameFormat(final String userName) {
    if (userName.indexOf(SPConstants.AT) != -1) {
      return getUserNameWithDomain(userName, null);
    } else {
      return getUserNameAtDomain(userName, null);
    }
  }

  /**
   * Checks if a string is numeric
   *
   * @param value
   */
  public static boolean isNumeric(final String value) {
    try {
      Integer.parseInt(value);
      return true;
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, value + " ... " + e.getMessage());
      return false;
    }
  }

  /**
   * Return group name with domain name i.e. domain\\groupname
   *
   * @param groupName the group/user name
   * @param domain the domain
   * @return a group/user name with domain
   */
  public static String getGroupNameWithDomain(final String groupName,
      final String domain) {
    return getUserNameWithDomain(groupName, domain);
  }

  /**
   * Return group name at domain name i.e. groupname@domain
   *
   * @param groupName the group/user name
   * @param domain the domain
   * @return a group/user name with domain
   */
  public static String getGroupNameAtDomain(String groupName,
      final String domain) {
    return getUserNameAtDomain(groupName, domain);
  }
}
