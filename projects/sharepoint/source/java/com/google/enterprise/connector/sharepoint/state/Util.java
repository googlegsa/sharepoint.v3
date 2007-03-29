package com.google.enterprise.connector.sharepoint.state;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.InstantConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * utility methods, mainly for use of Joda time, and converting to and from.
 */
public class Util {
  private static final InstantConverter timeConverterFromCalendar =
    ConverterManager.getInstance().getInstantConverter(
      new GregorianCalendar());
  private static final InstantConverter timeConverterFromJoda =
    ConverterManager.getInstance().getInstantConverter(
      new DateTime());
  private static final Chronology chron = new DateTime().getChronology();
  private static final DateTimeFormatter formatter =
    ISODateTimeFormat.basicDateTime();
  
  public static Calendar jodaToCalendar(DateTime date) {
    long millis = timeConverterFromJoda.getInstantMillis(date, chron);
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeInMillis(millis);
    return cal;
  }
  
  public static DateTime calendarToJoda(Calendar cal) {
    long millis = timeConverterFromCalendar.getInstantMillis(cal, chron);
    return new DateTime(millis, chron);
  }
  
  public static String formatDate(DateTime date) {
    return formatter.print(date);
  }
  
  public static String formatDate(Calendar cal) {
    return formatter.print(calendarToJoda(cal));
  }
  
  public static DateTime parseDate(String str) {
    return formatter.parseDateTime(str);
  }
}
