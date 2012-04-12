// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.enterprise.connector.sharepoint.wsclient.util;

import java.util.Calendar;

import junit.framework.TestCase;

/**
 * Test case checks if value of date is preserved when the ISO8601 string is
 * converted to calendar and back to a String. This is important as the date is
 * used for WSS 2.0 change detection. Any failure in this test indicates that
 * the WSS 2.0 change detection logic will break.
 *
 * @author darshanj@google.com (Darshan Jawalebhoi)
 */
public class DateUtilTest extends TestCase {
  static final String SAMPLE_ISO_8601_DATE_STRING = "2010-01-01T12:00:00Z";

  public final void testDateConversion() throws Throwable {
    System.out.println("Testing if date conversion from ISO 8601 to Calendar and back preserves the original value");
    Calendar calendarDate = DateUtil.iso8601ToCalendar(SAMPLE_ISO_8601_DATE_STRING);
    String retrievedDateString = DateUtil.calendarToIso8601(calendarDate, DateUtil.Iso8601DateAccuracy.SECS);
    assertEquals("SHOW STOPPER Error:  The date value is not being preserved while converting from and to ISO8601. This will break your change detection logic for WSS 2.0 and SPS 2003", SAMPLE_ISO_8601_DATE_STRING, retrievedDateString);
    System.out.println("[ DateUtil - converison from and to ISO 8601 ] Test Passed");
  }
}
