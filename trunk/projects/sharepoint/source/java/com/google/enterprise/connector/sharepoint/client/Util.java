// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Class to hold random utility functions. 
 *
 */
public class Util {

  private static final String timeFormat1 = "yyyy-MM-dd HH:mm:ss";
  
  public static Calendar StringToCalendar(String strDate) 
    throws ParseException  {
    SimpleDateFormat formatter = new SimpleDateFormat(timeFormat1);
    Date dt = formatter.parse(strDate);
    Calendar c = Calendar.getInstance();
    c.setTime(dt);    
    return c;
  }
}
