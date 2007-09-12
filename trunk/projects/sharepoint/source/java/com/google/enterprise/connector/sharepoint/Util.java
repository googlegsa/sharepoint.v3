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

package com.google.enterprise.connector.sharepoint;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.InstantConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Class to hold random utility functions. 
 *
 */
public final class Util {

  private static final String TIMEFORMAT1 = "yyyy-MM-dd HH:mm:ss";
  private static final String TIMEFORMAT2 = "yyyy-MM-dd HH:mm:ss'Z'";
  private static final String TIMEFORMAT3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  
  public static final String AUTHOR = "sharepoint:author";
  public static final String LIST_GUID = "sharepoint:listguid";
  
  private static final InstantConverter TIME_CONVERTER_FROM_CALENDAR =
    ConverterManager.getInstance().getInstantConverter(
      new GregorianCalendar());
  private static final InstantConverter TIME_CONVERTER_FROM_JODA =
    ConverterManager.getInstance().getInstantConverter(
      new DateTime());
  private static final Chronology CHRON = new DateTime().getChronology();
  private static final DateTimeFormatter FORMATTER =
    ISODateTimeFormat.basicDateTime();
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER1 = 
      new SimpleDateFormat(TIMEFORMAT1);
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER2 = 
    new SimpleDateFormat(TIMEFORMAT2);
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER3 = 
    new SimpleDateFormat(TIMEFORMAT3);
  private Util() {   
  }
  
  public static Calendar listItemsStringToCalendar(String strDate) 
      throws ParseException  {    
    Date dt = SIMPLE_DATE_FORMATTER1.parse(strDate);
    Calendar c = Calendar.getInstance();
    c.setTime(dt);    
    return c;
  }
  
  public static Calendar listItemChangesStringToCalendar(String strDate) 
      throws ParseException  {    
    Date dt = SIMPLE_DATE_FORMATTER3.parse(strDate);
    Calendar c = Calendar.getInstance();
    c.setTime(dt);    
    return c;
  }
  
  public static Calendar siteDataStringToCalendar(String strDate) 
      throws ParseException  {    
    Date dt = SIMPLE_DATE_FORMATTER2.parse(strDate);
    Calendar c = Calendar.getInstance();
    c.setTime(dt);    
    return c;
  }
  
  public static Calendar jodaToCalendar(DateTime date) {
    long millis = TIME_CONVERTER_FROM_JODA.getInstantMillis(date, CHRON);
    GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeInMillis(millis);
    return cal;
  }
  
  public static DateTime calendarToJoda(Calendar cal) {
    long millis = TIME_CONVERTER_FROM_CALENDAR.getInstantMillis(cal, CHRON);
    return new DateTime(millis, CHRON);
  }
  
  public static String formatDate(DateTime date) {
    return FORMATTER.print(date);
  }
  
  public static String formatDate(Calendar cal) {
    return FORMATTER.print(calendarToJoda(cal));
  }
  
  public static DateTime parseDate(String str) {
    return FORMATTER.parseDateTime(str);
  }
 
  public static CharSequence removeLineTerminators(CharSequence inputStr) {
    String patternStr = "(?m)$^|[\\r\\n]+\\z";
    String replaceStr = " ";
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(inputStr);
    return matcher.replaceAll(replaceStr);
  }  
  
  public static String getEscapedSiteName(String siteName) 
      throws RepositoryException {
    StringBuffer escapedSiteName = new StringBuffer();
    String siteNamearray[] = siteName.split("/");
    for (int iStr=0;iStr<siteNamearray.length;++iStr) {
      try {
    	  String str = siteNamearray[iStr];
        escapedSiteName.append(URLEncoder.encode(str, "UTF-8")).append("/");
      } catch (UnsupportedEncodingException e) {
        throw new RepositoryException(e.toString());
      }
    }
//    return escapedSiteName.toString().replace("+", "%20");  
    String str = escapedSiteName.toString();
    str.replace('+', ' ');
    //String str2="+";
    return str.replaceAll(" ", "%20");
  }

	/*public static Set propertyNamesFromDoc() {
		//construct the property for document
		Set s = new HashSet();
		s.add(SpiConstants.PROPNAME_CONTENTURL);
		s.add(SpiConstants.PROPNAME_DOCID);
		s.add(SpiConstants.PROPNAME_DISPLAYURL);
		s.add(SpiConstants.PROPNAME_LASTMODIFIED);
		s.add(LIST_GUID);
		s.add(AUTHOR);
		return s;
		
	}*/

	/*public static Property propertyFromDoc() {
		return null;
	}*/
}
