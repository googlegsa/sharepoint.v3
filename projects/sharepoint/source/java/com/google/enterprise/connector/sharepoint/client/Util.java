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
  private static final String timeFormat2 = "yyyy-MM-dd HH:mm:ss'Z'";
  private static final String timeFormat3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  
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
}
