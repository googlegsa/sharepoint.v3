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

import com.google.enterprise.connector.sharepoint.client.SPDocument;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.PropertyMap;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.InstantConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to hold random utility functions. 
 *
 */
public class Util {

  private static final String timeFormat1 = "yyyy-MM-dd HH:mm:ss";
  private static final String timeFormat2 = "yyyy-MM-dd HH:mm:ss'Z'";
  private static final String timeFormat3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  
  public static final String AUTHOR = "sharepoint:author";
  public static final String LIST_GUID = "sharepoint:listguid";
  
  private static final InstantConverter timeConverterFromCalendar =
    ConverterManager.getInstance().getInstantConverter(
      new GregorianCalendar());
  private static final InstantConverter timeConverterFromJoda =
    ConverterManager.getInstance().getInstantConverter(
      new DateTime());
  private static final Chronology chron = new DateTime().getChronology();
  private static final DateTimeFormatter formatter =
    ISODateTimeFormat.basicDateTime();
  
  private Util() {   
  }
  
  public static Calendar listItemsStringToCalendar(String strDate) 
    throws ParseException  {
    SimpleDateFormat formatter = new SimpleDateFormat(timeFormat1);
    Date dt = formatter.parse(strDate);
    Calendar c = Calendar.getInstance();
    c.setTime(dt);    
    return c;
  }
  
  public static Calendar listItemChangesStringToCalendar(String strDate) 
      throws ParseException  {
    SimpleDateFormat formatter = new SimpleDateFormat(timeFormat3);
    Date dt = formatter.parse(strDate);
    Calendar c = Calendar.getInstance();
    c.setTime(dt);    
    return c;
  }
  
  public static Calendar siteDataStringToCalendar(String strDate) 
      throws ParseException  {
    SimpleDateFormat formatter = new SimpleDateFormat(timeFormat2);
    Date dt = formatter.parse(strDate);
    Calendar c = Calendar.getInstance();
    c.setTime(dt);    
    return c;
  }
  
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
  
  /**
   * Build the Property Map for the connector manager from the 
   * sharepoint document.
   * @param doc sharepoint document
   * @return Property Map.
   */
  public static SimplePropertyMap propertyMapFromDoc(String guidList, 
      SPDocument doc) {
    SimplePropertyMap pm = new SimplePropertyMap();
    Property contentUrlProp = new SimpleProperty(
      SpiConstants.PROPNAME_CONTENTURL, new SimpleValue(ValueType.STRING, 
      doc.getUrl()));
    pm.put(SpiConstants.PROPNAME_CONTENTURL, contentUrlProp);
    
    Property docIdProp = new SimpleProperty(
      SpiConstants.PROPNAME_DOCID, new SimpleValue(ValueType.STRING, 
      doc.getDocId()));
    pm.put(SpiConstants.PROPNAME_DOCID, docIdProp);        
    
    Property searchUrlProp = new SimpleProperty(
      SpiConstants.PROPNAME_SEARCHURL, new SimpleValue(ValueType.STRING, 
      doc.getUrl()));
    pm.put(SpiConstants.PROPNAME_SEARCHURL, searchUrlProp);
    
    Property lastModifyProp = new SimpleProperty(
      SpiConstants.PROPNAME_LASTMODIFY, new SimpleValue(
        ValueType.DATE, SimpleValue.calendarToIso8601(doc.getLastMod())));  
    pm.put(SpiConstants.PROPNAME_LASTMODIFY, lastModifyProp);
    
    Property listGuidProp = new SimpleProperty(
        LIST_GUID, new SimpleValue(ValueType.STRING, guidList));
    pm.put(LIST_GUID, listGuidProp);
    
    if (!doc.getAuthor().equals(SPDocument.NO_AUTHOR)) {
      Property authorProp = new SimpleProperty(AUTHOR, 
          new SimpleValue(ValueType.STRING, doc.getAuthor()));    
      pm.put(AUTHOR, authorProp);
    }    
    return pm;
  }  
  
  /**
   * The inverse process of the above method: if the Connector manager (or
   * someone) gives us back a PropertyMap, reconstruct the SPDocument object
   * that it represents.
   * @param map
   * @return SPDocument
   * @throws RepositoryException
   */
  public static SPDocument docFromPropertyMap(PropertyMap map) 
    throws RepositoryException {
    try {
      String url = map.getProperty(
          SpiConstants.PROPNAME_CONTENTURL).getValue().getString();
      String id = map.getProperty(
          SpiConstants.PROPNAME_DOCID).getValue().getString();
      Calendar lastMod = map.getProperty(
          SpiConstants.PROPNAME_LASTMODIFY).getValue().getDate();
      SPDocument doc = new SPDocument(id, url, lastMod);
      Property authorProp = map.getProperty(Util.AUTHOR);
      if (authorProp != null) {
        doc.setAuthor(authorProp.getValue().getString());
      }
      return doc;
    } catch (IllegalArgumentException e) {
      throw new RepositoryException(e.toString());
    } catch (RepositoryException e) {
      throw new RepositoryException(e.toString());
    }
  }  
  
  /**
   * The List's GUID is also stored in the property map for a ResultSet.
   * We don't want to duplicate that in every SPDocument instance, so this is
   * the way to grab that.
   * @param map
   * @return GUID
   * @throws RepositoryException
   */
  public static String listGuidFromPropertyMap(PropertyMap map)
  throws RepositoryException {
    String guid;
    try {
      guid = map.getProperty(LIST_GUID).getValue().getString();
    } catch (IllegalArgumentException e) {
      throw new RepositoryException(e.toString());
    } catch (RepositoryException e) {
      throw new RepositoryException(e.toString());
    }
    return guid;
  }
  
  public static CharSequence removeLineTerminators(CharSequence inputStr) {
    String patternStr = "(?m)$^|[\\r\\n]+\\z";
    String replaceStr = " ";
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(inputStr);
    return matcher.replaceAll(replaceStr);
}
}
