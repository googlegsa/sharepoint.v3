// Copyright 2007 Google Inc.
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

package com.google.enterprise.connector.sharepoint.client;

import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPBasePermissions;
import com.google.enterprise.connector.sharepoint.generated.gssacl.ObjectType;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.SpiConstants.RoleType;

import org.apache.axis.AxisFault;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.InstantConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to hold random utility functions
 *
 * @author nitendra_thakur
 */
public final class Util {

  public static final String TIMEFORMAT1 = "yyyy-MM-dd HH:mm:ss";
  private static final String TIMEFORMAT2 = "yyyy-MM-dd HH:mm:ss'Z'";
  private static final String TIMEFORMAT3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public static final String TIMEFORMAT_WITH_ZONE = "yyyy-MM-dd HH:mm:ss z";

  public static final String AUTHOR = "sharepoint:author";
  public static final String LIST_GUID = "sharepoint:listguid";

  private static final InstantConverter TIME_CONVERTER_FROM_CALENDAR = ConverterManager.getInstance().getInstantConverter(new GregorianCalendar());
  private static final InstantConverter TIME_CONVERTER_FROM_JODA = ConverterManager.getInstance().getInstantConverter(new DateTime());
  private static final Chronology CHRON = new DateTime().getChronology();
  private static final DateTimeFormatter FORMATTER = ISODateTimeFormat.basicDateTime();
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER1 = new SimpleDateFormat(
      TIMEFORMAT1);
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER2 = new SimpleDateFormat(
      TIMEFORMAT2);
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER3 = new SimpleDateFormat(
      TIMEFORMAT3);

  private static final Logger LOGGER = Logger.getLogger(Util.class.getName());

  /**
   * Formats last modified date (yyyy-MM-dd HH:mm:ss) to Calendar format
   *
   * @param strDate
   * @throws ParseException
   */
  public static Calendar listItemsStringToCalendar(final String strDate)
      throws ParseException {
    final Date dt = SIMPLE_DATE_FORMATTER1.parse(strDate);
    final Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return c;
  }

  /**
   * Formats last modified date (yyyy-MM-dd'T'HH:mm:ss'Z') to Calendar format
   *
   * @param strDate
   * @throws ParseException
   */
  public static Calendar listItemChangesStringToCalendar(final String strDate)
      throws ParseException {
    final Date dt = SIMPLE_DATE_FORMATTER3.parse(strDate);
    final Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return c;
  }

  /**
   * Formats last modified date (yyyy-MM-dd HH:mm:ss'Z') to Calendar format
   *
   * @param strDate
   * @throws ParseException
   */
  public static Calendar siteDataStringToCalendar(final String strDate)
      throws ParseException {
    final Date dt = SIMPLE_DATE_FORMATTER2.parse(strDate);
    final Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return c;
  }

  /**
   * Converts a DateTime format to Calendar
   *
   * @param date
   */
  public static Calendar jodaToCalendar(final DateTime date) {
    final long millis = TIME_CONVERTER_FROM_JODA.getInstantMillis(date, CHRON);
    final GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeInMillis(millis);
    return cal;
  }

  /**
   * Converts a Calendar format to DateTime
   *
   * @param cal
   */
  public static DateTime calendarToJoda(final Calendar cal) {
    final long millis = TIME_CONVERTER_FROM_CALENDAR.getInstantMillis(cal, CHRON);
    DateTime dt = new DateTime(millis, CHRON);
    return dt;
  }

  /**
   * Return a String formated DateTime value
   *
   * @param date
   */
  public static String formatDate(final DateTime date) {
    String dtString = FORMATTER.print(date);
    return dtString;
  }

  /**
   * Return a string formated Calendar value
   *
   * @param cal
   */
  public static String formatDate(final Calendar cal) {
    String dtString = FORMATTER.print(calendarToJoda(cal));
    return dtString;
  }

  /**
   * Formats a date with the given format
   *
   * @param cal The calendar instance representing the date, time, timezone
   * @param format The supported formats. Currently following are supported:
   *          TIMEFORMAT1, TIMEFORMAT_WITH_ZONE
   * @return The formatted date time string as per the given format
   * @throws IllegalArgumentException In case any invalid format is given as
   *           input
   * @throws NullPointerException In case any argument is null
   */
  public static String formatDate(final Calendar cal, String format) {
    if (cal == null || format == null) {
      throw new NullPointerException();
    }

    if (format.equals(TIMEFORMAT1)) {
      return SIMPLE_DATE_FORMATTER1.format(new Date(cal.getTimeInMillis()));
    } else if (format.equals(TIMEFORMAT_WITH_ZONE)) {
      SimpleDateFormat dateFormatter = new SimpleDateFormat(
          TIMEFORMAT_WITH_ZONE);
      return dateFormatter.format(new Date(cal.getTimeInMillis()));
    } else {
      throw new IllegalArgumentException(
          "Invalid value specified for the format : " + format);
    }
  }

  /**
   * returns a DateTime value for a well-formed string
   *
   * @param str
   */
  public static DateTime parseDate(final String str) {
    DateTime dt = FORMATTER.parseDateTime(str);
    return dt;
  }

  /**
   * For removing line terminators
   *
   * @param inputStr
   */
  public static CharSequence removeLineTerminators(final CharSequence inputStr) {
    final String patternStr = "(?m)$^|[\\r\\n]+\\z";
    final String replaceStr = " ";
    final Pattern pattern = Pattern.compile(patternStr);
    final Matcher matcher = pattern.matcher(inputStr);
    CharSequence charSeq = matcher.replaceAll(replaceStr);
    return charSeq;
  }

  /**
   * Returns a formatted site URL. appends / at the end and encodes various
   * non-UTF character.
   *
   * @param siteName
   * @throws RepositoryException
   */
  public static String getEscapedSiteName(final String siteName)
      throws RepositoryException {
    final StringBuffer escapedSiteName = new StringBuffer();
    final String siteNamearray[] = siteName.split(SPConstants.SLASH);
    for (final String str : siteNamearray) {
      try {
        escapedSiteName.append(URLEncoder.encode(str, "UTF-8")).append(SPConstants.SLASH);
      } catch (final UnsupportedEncodingException e) {
        throw new RepositoryException(e.toString());
      }
    }
    final String str = escapedSiteName.toString();
    str.replace('+', ' ');
    String strModified = str.replaceAll(" ", "%20");
    return strModified;
  }

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
   * Desc : match the String Value with the string array.
   *
   * @param strList
   * @param strValue
   */
  public static boolean match(final String[] strList, final String strValue,
      final StringBuffer matchedPattern) {
    if ((strList == null) || (strValue == null)) {
      return false;
    }
    for (final String strURLPat : strList) {
      if ((strURLPat != null) && (strURLPat.length() > 0)) {
        String strDecodedValue = strValue;
        String strDecodedURLPat = strURLPat;
        try {
          strDecodedValue = URLDecoder.decode(strValue, "UTF-8");
          strDecodedURLPat = URLDecoder.decode(strURLPat, "UTF-8");
        } catch (final Exception e) {
          LOGGER.log(Level.FINE, e.getMessage());
          strDecodedValue = strValue;
          strDecodedURLPat = strURLPat;
        }
        if (Util.matcher(strDecodedURLPat, strDecodedValue)) {
          if (matchedPattern != null) {
            matchedPattern.append(strURLPat);
          }
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Matches a url with a pattern. Mimics GSA's pattern matching
   *
   * @param pattern
   * @param strValue
   */
  public static boolean matcher(final String pattern, final String strValue) {
    // null check for the arguments
    if ((pattern == null) | (strValue == null)) {
      return false;
    }

    // If the pattern starts with "#" then its a comment so ignore
    if (pattern.startsWith(SPConstants.HASH)) {
      return false;
    }

    // if the pattern starts with "-", remove the "-" from begin and proceed
    if (pattern.startsWith(SPConstants.MINUS)) {
      return false;
    }

    // handle "contains:"
    // if pattern starts with "contains:" then check if the URL contains the
    // string in pattern
    if (pattern.startsWith(SPConstants.CONTAINS)) {
      final StringBuffer tempBuffer = new StringBuffer(pattern);
      if (tempBuffer == null) {
        return false;
      }
      final String strContainKey = new String(
          tempBuffer.delete(0, SPConstants.CONTAINS.length()));
      RE re;
      try {
        re = new RE(strContainKey); // with case
        final REMatch reMatch = re.getMatch(strValue);
        if (reMatch != null) {
          return true;
        }
        return false;
      } catch (final REException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      }
    }

    // handle regexp
    // if pattern starts with "regexp:", then check for regex match with
    // case
    if (pattern.startsWith(SPConstants.REGEXP)) {
      final StringBuffer tempBuffer = new StringBuffer(pattern);
      if (tempBuffer == null) {
        return false;
      }
      final String strRegexPattrn = new String(
          tempBuffer.delete(0, SPConstants.REGEXP.length()));
      RE re;
      try {
        re = new RE(strRegexPattrn);
        final REMatch reMatch = re.getMatch(strValue);
        if (reMatch != null) {
          return true;
        }
        return false;
      } catch (final REException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      }
    }

    // handle regexpCase
    // if pattern starts with "regexpCase:", then check for regex match with
    // case
    if (pattern.startsWith(SPConstants.REGEXP_CASE)) {
      final StringBuffer tempBuffer = new StringBuffer(pattern);
      if (tempBuffer == null) {
        return false;
      }
      final String strRegexCasePattrn = new String(
          tempBuffer.delete(0, SPConstants.REGEXP_CASE.length()));
      RE re;
      try {
        re = new RE(strRegexCasePattrn);
        final REMatch reMatch = re.getMatch(strValue);
        if (reMatch != null) {
          return true;
        }
        return false;
      } catch (final REException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      }
    }

    // handle regexpIgnoreCase
    // if pattern starts with "regexpIgnoreCase:", then check for regex
    // match without case
    if (pattern.startsWith(SPConstants.REGEXP_IGNORE_CASE)) {
      final StringBuffer tempBuffer = new StringBuffer(pattern);
      if (tempBuffer == null) {
        return false;
      }
      final String strRegexIgnoreCasePattrn = new String(
          tempBuffer.delete(0, SPConstants.REGEXP_IGNORE_CASE.length()));
      RE re;
      try {
        re = new RE(strRegexIgnoreCasePattrn, RE.REG_ICASE); // ignore
        // case
        final REMatch reMatch = re.getMatch(strValue);
        if (reMatch != null) {
          return true;
        }
        return false;
      } catch (final REException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      }
    }

    // handle "^" and "$"
    if (pattern.startsWith(SPConstants.CARET)
        || pattern.endsWith(SPConstants.DOLLAR)) {
      StringBuffer tempBuffer = new StringBuffer(pattern);
      boolean bDollar = false;
      String strValueModified = strValue;
      if (pattern.startsWith(SPConstants.CARET)) {
        URL urlValue;
        try {
          urlValue = new URL(strValue);
          int port = urlValue.getPort();
          if (port == -1) {
            port = urlValue.getDefaultPort();
            strValueModified = urlValue.getProtocol() + SPConstants.URL_SEP
                + urlValue.getHost() + SPConstants.COLON + port
                + urlValue.getFile();
          }
        } catch (final MalformedURLException e1) {
          LOGGER.log(Level.FINE, e1.getMessage());
          return false;
        }
        tempBuffer = new StringBuffer(pattern);
        final int indexOfStar = tempBuffer.indexOf("*");
        if (indexOfStar != -1) {
          tempBuffer.replace(indexOfStar, indexOfStar + "*".length(), "[0-9].*");
        } else {
          tempBuffer.delete(0, "^".length());
          if (pattern.endsWith(SPConstants.DOLLAR)) {
            bDollar = true;
            tempBuffer.delete(tempBuffer.length() - SPConstants.DOLLAR.length(), tempBuffer.length());
          }
          try {
            final URL urlPatt = new URL(tempBuffer.toString());
            final int port = urlPatt.getPort();

            final String strHost = urlPatt.getHost().toString();

            if ((port == -1) && (strHost != null) && (strHost.length() != 0)) {
              tempBuffer = new StringBuffer("^" + urlPatt.getProtocol()
                  + SPConstants.URL_SEP + urlPatt.getHost() + ":[0-9].*"
                  + urlPatt.getPath());
            }
            if (bDollar) {
              tempBuffer.append(SPConstants.DOLLAR);
            }
          } catch (final MalformedURLException e) {
            LOGGER.log(Level.FINE, e.getMessage());
            tempBuffer = new StringBuffer(pattern);
          }
        }
      }

      RE re;
      try {
        re = new RE(tempBuffer);
        final REMatch reMatch = re.getMatch(strValueModified);
        if (reMatch != null) {
          return true;
        }
        return false;
      } catch (final REException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      }
    }

    // url decode the pattern
    String patternDecoded = pattern;
    try {
      patternDecoded = URLDecoder.decode(pattern, "UTF-8");
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, e.getMessage());
      patternDecoded = pattern;
    }

    if (patternDecoded == null) {
      return false;
    }

    boolean containProtocol = false;
    try {
      final RE re = new RE(SPConstants.URL_SEP);
      final REMatch reMatch = re.getMatch(patternDecoded);
      if (reMatch != null) {
        containProtocol = true; // protocol is present
      }
    } catch (final REException e) {
      containProtocol = false;
    }

    if (containProtocol) {
      // split the test URL into two parts
      String urlValue1stPart = null;
      String urlValue2ndPart = null;

      URL urlValue;
      try {
        urlValue = new URL(strValue);
        int port = urlValue.getPort();
        if (port == -1) {
          port = urlValue.getDefaultPort();
        }
        urlValue1stPart = urlValue.getProtocol() + SPConstants.URL_SEP
            + urlValue.getHost() + SPConstants.COLON + port;
        urlValue2ndPart = urlValue.getFile();

        if (urlValue2ndPart != null) {
          if (!urlValue2ndPart.startsWith(SPConstants.SLASH)) {
            urlValue2ndPart = SPConstants.SLASH + urlValue2ndPart;
          }
        }
      } catch (final MalformedURLException e1) {
        LOGGER.log(Level.FINE, e1.getMessage());
        return false;
      }

      // split the pattern into two parts
      String urlPatt1stPart = null;
      String urlPatt2ndPart = null;
      boolean bPortStar = false;
      try {
        final URL urlPatt = new URL(patternDecoded);
        final int port = urlPatt.getPort();
        String strPort = "";
        if (port == -1) {
          strPort = "[0-9].*";
        } else {
          strPort = port + "";
        }
        urlPatt1stPart = "^" + urlPatt.getProtocol() + SPConstants.URL_SEP
            + urlPatt.getHost() + SPConstants.COLON + strPort;
        if (!(urlPatt.getFile()).startsWith(SPConstants.SLASH)) { // The
          // pattern
          // must
          // have
          // "/"
          // at
          // after
          // the
          // port
          return false;
        }
        urlPatt2ndPart = "^" + urlPatt.getFile();
      } catch (final MalformedURLException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        bPortStar = true;
      }

      if (bPortStar) {
        final int indexOfStar = patternDecoded.indexOf("*");
        if (indexOfStar != -1) {
          urlPatt1stPart = "^" + patternDecoded.substring(0, indexOfStar)
              + "[0-9].*";
          if (!(patternDecoded.substring(indexOfStar + 1)).startsWith(SPConstants.SLASH)) {
            return false;
          }
          urlPatt2ndPart = "^" + patternDecoded.substring(indexOfStar + 1);
        }
      }

      // check 1st part of both with ignorecase
      RE re;
      try {
        re = new RE(urlPatt1stPart, RE.REG_ICASE); // ignore case for
        // 1st part
        REMatch reMatch = re.getMatch(urlValue1stPart);
        if (reMatch != null) {
          // check 2nd part of both with case
          re = new RE(urlPatt2ndPart);
          reMatch = re.getMatch(urlValue2ndPart);
          if (reMatch != null) {
            return true;
          }
        }
      } catch (final REException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      } catch (final Exception e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      }
    } else {
      String pat1 = null;
      String pat2 = null;
      // split the pattern into two parts
      if (patternDecoded.indexOf(SPConstants.SLASH) != -1) {
        if (patternDecoded.indexOf(SPConstants.COLON) == -1) {
          pat1 = patternDecoded.substring(0, patternDecoded.indexOf(SPConstants.SLASH))
              + ":[0-9].*";
        } else {
          pat1 = patternDecoded.substring(0, patternDecoded.indexOf(SPConstants.SLASH));
        }
        pat2 = patternDecoded.substring(patternDecoded.indexOf(SPConstants.SLASH));
      } else {
        // The pattern must have "/" at after the port
        return false;
      }

      pat1 = "^.*://.*" + pat1;
      pat2 = "^" + pat2;
      URL urlValue;
      try {
        urlValue = new URL(strValue);
        int port = urlValue.getPort();
        if (port == -1) {
          port = urlValue.getDefaultPort();
        }
        final String urlValue1stPart = urlValue.getProtocol()
            + SPConstants.URL_SEP + urlValue.getHost() + SPConstants.COLON
            + port;
        String urlValue2ndPart = urlValue.getFile();

        if (urlValue2ndPart != null) {
          if (!urlValue2ndPart.startsWith(SPConstants.SLASH)) {
            urlValue2ndPart = SPConstants.SLASH + urlValue2ndPart;
          }
        }

        RE re;
        try {
          re = new RE(pat1, RE.REG_ICASE); // ignore case for 1st part
          REMatch reMatch = re.getMatch(urlValue1stPart);
          if (reMatch != null) {
            re = new RE(pat2); // with case for 2nd part
            reMatch = re.getMatch(urlValue2ndPart);
            if (reMatch != null) {
              return true;
            }
          }
        } catch (final REException e) {
          LOGGER.log(Level.FINE, e.getMessage());
          return false;
        }
      } catch (final MalformedURLException e) {
        LOGGER.log(Level.FINE, e.getMessage());
        return false;
      }
    }

    return false;
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
   * Get the web application url from a SharePoint URL
   *
   * @param web
   */
  public static String getWebApp(final String web) {
    URL url = null;
    try {
      url = new URL(web);
    } catch (final MalformedURLException e) {
      LOGGER.log(Level.FINE, web + " ... " + e.getMessage());
      return null;
    }

    final int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    final String res = url.getProtocol() + SPConstants.URL_SEP + url.getHost()
        + SPConstants.COLON + port;
    LOGGER.log(Level.FINEST, "input [ " + web + " ], output [ " + res + " ]. ");
    return res;
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
   * Checks to see if the incoming value is a valid URL
   *
   * @param value
   */
  public static boolean isURL(final String value) {
    try {
      final URL url = new URL(value);
      if (url == null) {
        return false;
      }
    } catch (final Exception e) {
      LOGGER.log(Level.FINE, value + " ... " + e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Gets the original DOcID if the document ID has been modified in respect to
   * the Content feed mode.
   *
   * @param docId
   * @param feedType
   */
  public static String getOriginalDocId(final String docId, FeedType feedType) {
    String originalDocId = docId;
    if (docId == null) {
      return docId;
    }
    if (FeedType.CONTENT_FEED == feedType) {
      final String[] parts = docId.split(SPConstants.DOUBLEBACKSLASH
          + SPConstants.DOC_TOKEN); // because | is a regexp character
      // and has to be delimited.
      if (parts.length == 2) {
        originalDocId = parts[1];
      }
    }
    LOGGER.log(Level.FINEST, "docId [ " + docId + " ], originalDocID [ "
        + originalDocId + " ]. ");
    return originalDocId;
  }

  /**
   * return URL without default port if contained the default port.
   *
   * @param strUrl
   */
  public static String getWebURLForWSCall(final String strUrl) {
    String tmpSPURL = strUrl;
    if (strUrl != null) {
      try {
        final URL url = new URL(strUrl);
        final String hostTmp = url.getHost();
        final String protocolTmp = url.getProtocol(); // to remove the
        // hard-coded
        // protocol
        int portTmp = -1;
        if (-1 != url.getPort()) {
          if (url.getPort() != url.getDefaultPort()) {
            portTmp = url.getPort();
          }
        }
        String siteNameTmp = url.getPath();
        String strSPURL = protocolTmp + SPConstants.URL_SEP + hostTmp;
        if (portTmp != -1) {
          strSPURL += SPConstants.COLON + portTmp;
        }
        if (siteNameTmp.endsWith(SPConstants.SLASH)) {
          siteNameTmp = siteNameTmp.substring(0, siteNameTmp.length() - 1);
        }
        strSPURL += siteNameTmp;
        return strSPURL;

      } catch (final MalformedURLException e) {
        LOGGER.log(Level.WARNING, e.toString());
      }
    }
    LOGGER.log(Level.FINEST, "input [ " + tmpSPURL + " ], output [ " + strUrl
        + " ]. ");
    return strUrl;
  }

  /**
   * While specifying the folder level scope we have to remove the preceding
   * parent site name. e.g, site/list/folder should be converted to list/folder
   *
   * @param webURL The parent Web URL
   * @param docPath docPath as returned by the Web Service
   */
  public static String getFolderPathForWSCall(final String webURL,
      String docPath) {
    String tmpDocPath = docPath;
    if ((webURL == null) || (docPath == null)) {
      return null;
    }
    if (docPath.startsWith(SPConstants.SLASH)) {
      docPath = docPath.replaceFirst(SPConstants.SLASH, "");
    }
    String webPath = "";
    try {
      final URL web_url = new URL(webURL);
      webPath = web_url.getPath();
      if (webPath.startsWith(SPConstants.SLASH)) {
        webPath = webPath.replaceFirst(SPConstants.SLASH, "");
      }
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Failed to create URL from given webURL.", e);
      return null;
    }
    if (docPath.startsWith(webPath)) {
      docPath = docPath.replaceFirst(webPath, "");
    } else {
      LOGGER.log(Level.WARNING, "Received docPath [" + docPath
          + "] is not valid as per the given webURL [" + webURL + "] ");
      return null;
    }
    if (docPath.startsWith(SPConstants.SLASH)) {
      docPath = docPath.replaceFirst(SPConstants.SLASH, "");
    }
    LOGGER.log(Level.FINEST, "input [ " + tmpDocPath + " ], output [ "
        + docPath + " ]. ");
    return docPath;
  }

  /**
   * Normalizes the attribute's name to make better sense to the end user.
   *
   * @param metaName the attribute name as returned by the web service the
   *          normalized attribute name
   */
  public static String normalizeMetadataName(String metaName) {
    String metaNameNormalized = metaName;
    if (null != metaNameNormalized) {
      if (metaNameNormalized.startsWith(SPConstants.OWS)) {
        metaNameNormalized = metaNameNormalized.replaceFirst(SPConstants.OWS, "");
      }
      if (metaNameNormalized.startsWith(SPConstants.METAINFO)) {
        metaNameNormalized = metaNameNormalized.replaceFirst(SPConstants.METAINFO, "");
      }
      if (metaNameNormalized.startsWith(SPConstants.VTI)) {
        metaNameNormalized = metaNameNormalized.replaceFirst(SPConstants.VTI, "");
      }
      metaNameNormalized = metaNameNormalized.replaceAll(SPConstants.ENCODED_SPACE, " ");
    }
    LOGGER.log(Level.FINEST, "metaName [ " + metaName
        + " ], metaNameNormalized [ " + metaNameNormalized + " ]. ");
    return metaNameNormalized;
  }

  /**
   * Normalizes the attribute's value to make better sense to the end user.
   *
   * @param metaValue the attribute value as returned by the web service
   * @return the normalized attribute value
   */
  public static String normalizeMetadataValue(String metaValue) {
    String metaValNormalized = metaValue;
    if (null != metaValNormalized) {
      final Matcher match = 
          SPConstants.ATTRIBUTE_VALUE_PATTERN.matcher(metaValue);
      if (match.find()) {
        String arr[] = 
            metaValNormalized.split(SPConstants.SP_MULTI_VALUE_DELIMITER);
        // SharePoint makes us of ;# characters for Lookup Fields as will as
        // for some of the internal meta fields.
        // SharePoint Uses <integer ID1>;#value1;#<integer ID2>;#value2 for
        // multi choice lookup fields values.
        if (arr.length <= 2) {
          // Replace first integer with empty string only in case of internal
          // meta fields or lookup fields with single value. lookup fields with
          // more than 1 values will be processed in SPDocument.
          metaValNormalized = match.replaceFirst("");
        }
      }
    }
    LOGGER.log(Level.FINEST, "metaValue [ " + metaValue
        + " ], metaValNormalized [ " + metaValNormalized + " ]. ");
    return metaValNormalized;
  }
  
  public static List<String> processMultiValueMetadata(String metaValue) {
    if (null == metaValue) {
      return null;
    }
    List<String> valueToPass = new ArrayList<String>();
    
    final Matcher match = 
        SPConstants.ATTRIBUTE_VALUE_PATTERN.matcher(metaValue);
    if (match.find()) {
      // This is a lookup field. We need to take alternate values only. Ignore
      // integer part.
      // <integer ID1>;#value1;#<integer ID2>;#value2
      String[] arr = metaValue.split(SPConstants.SP_MULTI_VALUE_DELIMITER);
      for (int i = 1; i < arr.length; i =i + 2) {
        if (!Strings.isNullOrEmpty(arr[i])) {
          valueToPass.add(arr[i]);
        }
      }
    } else if (metaValue.startsWith(SPConstants.SP_MULTI_VALUE_DELIMITER) 
        && metaValue.endsWith(SPConstants.SP_MULTI_VALUE_DELIMITER)) {
      // This is a multi choice field. Values will be in
      // ;#value1;#value2;# format.
      String[] arr = metaValue.split(SPConstants.SP_MULTI_VALUE_DELIMITER);
      for (String value : arr) {
        if (value.length() > 0) {
          valueToPass.add(value);
        }
      }
    } else {
      valueToPass.add(metaValue);
    }
    return valueToPass;    
  }

  /**
   * A file writter
   *
   * @param filePath absolute path of the file
   * @param info message to be written
   */
  public static void logInfo(final String filePath, final String info) {
    File file;
    file = new File(filePath);
    if (file == null) {
      return;
    }
    try {
      final FileWriter fw = new FileWriter(file, true);
      final PrintWriter pw = new PrintWriter(fw);

      final Calendar cal = Calendar.getInstance();
      final SimpleDateFormat formater = new SimpleDateFormat(
          "yyyy-MM-dd HH:mm:ss");

      pw.println(formater.format(cal.getTime()) + " : " + info);
      pw.flush();
      pw.close();
    } catch (final IOException e) {
      LOGGER.log(Level.WARNING, "failed to log info [ " + info
          + " ] at filePath [ " + filePath + " ]. ", e);
    } catch (final Throwable e) {
      LOGGER.log(Level.WARNING, "failed to log info [ " + info
          + " ] at filePath [ " + filePath + " ]. ", e);
    }
  }

  /**
   * URL encoder.
   *
   * @param strURL
   */
  public static String encodeURL(final String strURL) {
    URL url = null;
    try {
      url = new URL(strURL);
      if (null == url) {
        LOGGER.log(Level.SEVERE, "Malformed URL!");
        return strURL;
      }
      // URI does the encoding of the URL automatically and also checks
      // for the valid URL
      final URI uri = new URI(url.getProtocol(), null, url.getHost(),
          url.getPort(), url.getPath(), url.getQuery(), url.getRef());
      return uri.toASCIIString();
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Malformed URL [ " + strURL + " ]. ");
      return strURL;
    }
  }

  /**
   * @param strURL
   */
  public static String getHost(final String strURL) {
    URL url = null;
    try {
      url = new URL(strURL);
      if (null == url) {
        LOGGER.log(Level.SEVERE, "Malformed URL [ " + strURL + " ]. ");
        return null;
      }
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Malformed URL [ " + strURL + " ]. ");
      return null;
    }
    return url.getHost();
  }

  public static boolean isFQDN(String serverName) {
    if (serverName.indexOf(SPConstants.PERIOD) == -1
        || serverName.lastIndexOf(SPConstants.PERIOD) == serverName.length() - 1) {
      return false;
    } else {
      try {
        InetAddress.getByName(serverName);
      } catch (UnknownHostException e) {
        return false;
      }
    }
    return true;
  }

  /**
   * Maps a set of SharePoint defined permissions to CM defined permissions.
   * TODO: The logic used for mapping could be improved depending on the current
   * discussions going on at this front with John Felton.
   *
   * @param permissions SharePoint Permissions
   * @param objectType Kind of entity (List/List-Item/Web) for which the mapping
   *          is to be done. This is required because SharePoint defines
   *          multiple granular permissions for various entity types and all
   *          these permissions may not be applicable to all the entities. For
   *          Example, "ManageWeb" has nothing do with ListItems.
   * @return a list of {@link RoleType}
   */
  public static Set<RoleType> getRoleTypesFor(String[] permissions,
      ObjectType objectType) {
    Set<RoleType> roleTypes = new HashSet<RoleType>();
    if (null == permissions || permissions.length == 0 || null == objectType) {
      return roleTypes;
    }
    if (permissions.length == 0
        || (permissions.length == 1 && permissions[0].equals(SPBasePermissions.EMPTYMASK))) {
      return roleTypes;
    }

    // The following two flags are to check if all the required permissions
    // for WRITER access on a list are fulfilled or not. We may need to add
    // more flags in future corresponding to any extra permissions that we
    // agree to check to give a user WRITER access on a list
    boolean managelist = false;
    boolean additems = false;

    // For checking Limited Access permission which will be mapped to the
    // PEEKER in CM
    boolean viewFormPages = false;
    boolean open = false;
    // flags to check all the required permissions for READER access on
    // SharePoint List or Document Library.
    boolean viewPages = false;
    boolean viewListItems = false;

    for (String permission : permissions) {
      if (SPBasePermissions.FULLMASK.equals(permission)) {
        roleTypes.add(RoleType.OWNER);
      }

      if (SPBasePermissions.VIEWFORMPAGES.equals(permission)) {
        viewFormPages = true;
      }
      if (SPBasePermissions.OPEN.equals(permission)) {
        open = true;
      }

      if (ObjectType.ITEM.equals(objectType)) {
        if (SPBasePermissions.EDITLISTITEMS.equals(permission)) {
          roleTypes.add(RoleType.WRITER);
        }
        if (SPBasePermissions.VIEWLISTITEMS.equals(permission)) {
          roleTypes.add(RoleType.READER);
        }
      } else if (ObjectType.LIST.equals(objectType)) {
        if (!managelist && SPBasePermissions.MANAGELISTS.equals(permission)) {
          managelist = true;
        }
        if (!additems && SPBasePermissions.ADDLISTITEMS.equals(permission)) {
          additems = true;
        }
        if (SPBasePermissions.VIEWPAGES.equals(permission)) {
          viewPages = true;
        }
        if (SPBasePermissions.VIEWLISTITEMS.equals(permission)) {
          viewListItems = true;
        }
      } else if (ObjectType.SITE_LANDING_PAGE.equals(objectType)) {
        if (SPBasePermissions.EDITLISTITEMS.equals(permission)) {
          roleTypes.add(RoleType.WRITER);
        }
        if (SPBasePermissions.VIEWLISTITEMS.equals(permission)) {
          roleTypes.add(RoleType.READER);
        }
      }
      // Currently, only list and list-items are fed as documents. In
      // future, if sites and pages are also sent, more checks will have
      // to be added here
    }
    if (ObjectType.LIST.equals(objectType) && viewPages && viewListItems) {
      roleTypes.add(RoleType.READER);
    }     
    if (ObjectType.LIST.equals(objectType) && managelist && additems) {
      roleTypes.add(RoleType.WRITER);
    }
    if (viewFormPages && open) {
      roleTypes.add(RoleType.PEEKER);
    }
    return roleTypes;
  }

  /**
   * Re-writes a given URL using the alias mapping rule specified.
   *
   * @param url URL to be re-written/mapped
   * @param aliasMap the alias mapping rules
   * @param fqdn If true, resulting URLs are converted into fqdn format. If
   *          false, URLs are returned just by applying the alias mapping rules.
   *          No further attempt will be made to re-write them.
   */
  public static String doAliasMapping(final String url,
      Map<String, String> aliasMap, boolean fqdn) throws MalformedURLException {
    URL objURL = new URL(url);
    String strUrl = "";

    boolean matched = false;
    // processing of alias values
    if ((null != aliasMap) && (null != aliasMap.keySet())) {
      for (final Iterator<String> aliasItr = aliasMap.keySet().iterator(); aliasItr.hasNext();) {

        String aliasPattern = aliasItr.next();
        String aliasValue = aliasMap.get(aliasPattern);

        if ((aliasPattern == null) || (aliasValue == null)) {
          continue;
        }
        aliasPattern = aliasPattern.trim();
        aliasValue = aliasValue.trim();
        if (aliasPattern.equalsIgnoreCase("")
            || aliasValue.equalsIgnoreCase("")) {
          continue;
        }

        URL patternURL = null;
        String aliasPatternURL = aliasPattern;
        if (aliasPattern.startsWith(SPConstants.GLOBAL_ALIAS_IDENTIFIER)) {
          aliasPatternURL = aliasPattern.substring(1);
        }

        try {
          patternURL = new URL(aliasPatternURL);
        } catch (final MalformedURLException e) {
          LOGGER.log(Level.WARNING, "Malformed alias pattern: "
              + aliasPatternURL, e);
        }
        if (patternURL == null) {
          continue;
        }

        if (!objURL.getProtocol().equalsIgnoreCase(patternURL.getProtocol())) {
          continue;
        }

        if (!objURL.getHost().equalsIgnoreCase(patternURL.getHost())) {
          continue;
        }

        if (aliasPattern.startsWith(SPConstants.GLOBAL_ALIAS_IDENTIFIER)) {
          aliasPattern = aliasPattern.substring(1);
          if (patternURL.getPort() == SPConstants.MINUS_ONE) {
            aliasPattern = patternURL.getProtocol() + SPConstants.URL_SEP
                + patternURL.getHost();
            if (objURL.getPort() != SPConstants.MINUS_ONE) {
              aliasPattern += SPConstants.COLON + objURL.getPort();
            }
            aliasPattern += patternURL.getFile();
          }
        } else if ((objURL.getPort() == SPConstants.MINUS_ONE)
            && (patternURL.getPort() == patternURL.getDefaultPort())) {
          aliasPattern = patternURL.getProtocol() + SPConstants.URL_SEP
              + patternURL.getHost() + patternURL.getFile();
        } else if ((objURL.getPort() == objURL.getDefaultPort())
            && (patternURL.getPort() == SPConstants.MINUS_ONE)) {
          aliasPattern = patternURL.getProtocol() + SPConstants.URL_SEP
              + patternURL.getHost() + SPConstants.COLON
              + patternURL.getDefaultPort() + patternURL.getFile();
        } else if (objURL.getPort() != patternURL.getPort()) {
          continue;
        }

        if (url.startsWith(aliasPattern)) {
          LOGGER.config("document url[" + url
              + "] has matched against alias source URL [ " + aliasPattern
              + " ]");
          strUrl = aliasValue;
          final String restURL = url.substring(aliasPattern.length());
          if (!strUrl.endsWith(SPConstants.SLASH)
              && !restURL.startsWith(SPConstants.SLASH)) {
            strUrl += SPConstants.SLASH;
          }
          strUrl += restURL;
          matched = true;
          LOGGER.config("document url[" + url + "] has been re-written to [ "
              + strUrl + " ] in respect to the aliasing.");
          break;
        }
      }
    }

    if (!matched) {
      strUrl = objURL.getProtocol() + SPConstants.URL_SEP;
      strUrl += getFQDNHostName(objURL.getHost(), fqdn) + SPConstants.COLON;
      final int portNo = objURL.getPort();
      if (portNo != SPConstants.MINUS_ONE) {
        strUrl += portNo;
      } else {
        strUrl += objURL.getDefaultPort();
      }
      strUrl += objURL.getFile();
    }

    return strUrl;
  }

  /**
   * Converts a host name to FQDN using Java's
   * {@link InetAddress#getCanonicalHostName()}
   *
   * @param hostName
   * @return the host name in FQDN format
   */
  private static String getFQDNHostName(final String hostName, boolean fqdn) {
    if (fqdn) {
      InetAddress ia = null;
      try {
        ia = InetAddress.getByName(hostName);
      } catch (final UnknownHostException e) {
        LOGGER.log(Level.WARNING, "Exception occurred while converting to FQDN, hostname [ "
            + hostName + " ].", e);
      }
      if (ia != null) {
        return ia.getCanonicalHostName();
      }
    }
    return hostName;
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

  /**
   * Finds the connector name from the provided connector instance
   * directory path by tokenizing the path and getting the leaf
   * directory name.
   *
   * @param googleConnectorWorkDir the connector instance directory
   * @return the connector instance name
   */
  public static String getConnectorNameFromDirectoryUrl(
      String googleConnectorWorkDir) {
    String directory = null;
    StringTokenizer tokenizer = new StringTokenizer(googleConnectorWorkDir,
        File.separator);
    while (tokenizer.hasMoreTokens()) {
      directory = tokenizer.nextToken();
    }
    return directory;
  }

  /**
   * An interferace used for making SOAP requests.
   */
  public interface RequestExecutor<T> {
    /**
     * Called to make a SOAP request.
     *
     * @param ws the web service interface to use to make the request
     * @return the request result
     */
    T onRequest(final BaseWS ws) throws Throwable;

    /**
     * Called when an exception occurs when make the web service request.
     *
     * @param e the exception that was thrown
     */
    void onError(final Throwable e);
  }

  /**
   * Makes a web service request.
   *
   * @param ctx the context
   * @param ws the web service interface to use to make the request
   * @param executor the interface that makes the request and handles errors
   * @return the request result
   */
  public static <T> T makeWSRequest(SharepointClientContext ctx, BaseWS ws,
      RequestExecutor<T> executor) {
    try {
      return executor.onRequest(ws);
    } catch (AxisFault af) {
      // Handling of username formats for different authentication models.
      // Switch the username format from domain\\username to username@domain
      // or vice versa.
      if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
          && (ctx.getDomain() != null)) {
        final String username = Util.switchUserNameFormat(ws.getUsername());
        LOGGER.info("Web service call failed for username [ " 
            + ws.getUsername() + " ], re-trying with username [ " 
            + username + " ].");
        ws.setUsername(username);
        try {
          return executor.onRequest(ws);
        } catch (final Throwable e) {
          executor.onError(e);
        }
      } else {
        executor.onError(af);
      }
    } catch (final Throwable e) {
      executor.onError(e);
    }
    return null;
  }
}
