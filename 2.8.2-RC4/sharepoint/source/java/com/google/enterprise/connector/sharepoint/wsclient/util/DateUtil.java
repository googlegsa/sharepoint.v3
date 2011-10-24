package com.google.enterprise.connector.sharepoint.wsclient.util;

// Copyright 2011 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This is utility class to handle conversion between date Calendar objects used
 * by the connector and ISO 8601 String format used by SharePoint web-services.
 * All conversions are restricted to GMT as per SharePoint web-service
 * specifications. The date management here governs important state management
 * and change detection for WSS 2.0 and SPS 2003.
 *
 * @author darshanj@google.com (Darshan Jawalebhoi)
 */
public class DateUtil {
  // The date management here governs important state management and change
  // detection for WSS 2.0 and SPS 2003. If you need to make any changes please
  // consider the impact on the same.

  public enum Iso8601DateAccuracy {
    MILLIS, SECS, MINS, DATE
  }

  /**
   * Timezone is always GMT for SP web-services
   */
  private static final TimeZone TIME_ZONE_GMT = TimeZone.getTimeZone("GMT+0");
  private static final Calendar GMT_CALENDAR = Calendar.getInstance(TIME_ZONE_GMT);
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_MILLIS = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_SECS = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss'Z'");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_MINS = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm'Z'");
  private static final SimpleDateFormat ISO8601_DATE_FORMAT_DATE = new SimpleDateFormat(
      "yyyy-MM-dd");

  static {
    ISO8601_DATE_FORMAT_MILLIS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_MILLIS.setLenient(true);
    ISO8601_DATE_FORMAT_SECS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_SECS.setLenient(true);
    ISO8601_DATE_FORMAT_MINS.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_MINS.setLenient(true);
    ISO8601_DATE_FORMAT_DATE.setCalendar(GMT_CALENDAR);
    ISO8601_DATE_FORMAT_DATE.setLenient(true);
  }

  /**
   * Converts the given calendar date to its equivalent ISo 8601 string in GMT
   *
   * @param c The calendar value to be converted to ISO 8601 format
   * @param accuracy The accuracy at which the date is to be trimmed
   * @return a String in ISO-8601 format - always in GMT zone
   */
  public static synchronized String calendarToIso8601(Calendar c,
      Iso8601DateAccuracy accuracy) {
    Date d = c.getTime();
    String isoString;
    if (accuracy.equals(Iso8601DateAccuracy.MILLIS)) {
      isoString = ISO8601_DATE_FORMAT_MILLIS.format(d);
    } else if (accuracy.equals(Iso8601DateAccuracy.SECS)) {
      isoString = ISO8601_DATE_FORMAT_SECS.format(d);
    } else if (accuracy.equals(Iso8601DateAccuracy.MINS)) {
      isoString = ISO8601_DATE_FORMAT_MINS.format(d);
    } else {
      isoString = ISO8601_DATE_FORMAT_DATE.format(d);
    }
    return isoString;
  }

  private static synchronized Date iso8601ToDate(String s)
      throws ParseException {
    Date d = null;
    try {
      d = ISO8601_DATE_FORMAT_MILLIS.parse(s);
    } catch (ParseException e1) {
      try {
        d = ISO8601_DATE_FORMAT_SECS.parse(s);
      } catch (ParseException e2) {
        try {
          d = ISO8601_DATE_FORMAT_MINS.parse(s);
        } catch (ParseException e3) {
          d = ISO8601_DATE_FORMAT_DATE.parse(s);
        }
      }
    }
    return d;
  }

  /**
   * Parses a String in ISO-8601 format (GMT zone) and returns an equivalent
   * java.util.Calendar object.
   *
   * @param s
   * @return a Calendar object
   * @throws ParseException if the the String can not be parsed
   */
  public static synchronized Calendar iso8601ToCalendar(String s)
      throws ParseException {
    Date d = iso8601ToDate(s);
    Calendar c = Calendar.getInstance(TIME_ZONE_GMT);
    c.setTime(d);
    return c;
  }
}
