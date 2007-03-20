// Copyright 2006 Google Inc.

/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.enterprise.connector.sharepoint.state;

import com.google.enterprise.connector.sharepoint.client.SharepointException;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.InstantConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Base class of all Sharepoint-related objects which can be persisted
 * by GlobalState.  Currently there is just one, ListState, but there may
 * be others in the future.
 * A StatefulObject has a unique key (for Lists, this is the GUID), and
 * a lastModified time.
 * This is an abstract class. Some functions are implemented here, and some
 * are left to the concrete subclass.
 */
public abstract class StatefulObject
  implements Comparable<StatefulObject> {
  protected String key = null;
  protected DateTime lastMod = null;
  private boolean visited = false;
  private boolean isCurrent = false;
  protected final InstantConverter timeConverter =
    ConverterManager.getInstance().getInstantConverter(
      new GregorianCalendar());
  protected final Chronology chron = new DateTime().getChronology();
  protected final DateTimeFormatter formatter =
    ISODateTimeFormat.basicDateTime();

  /**
   * All StatefulObjects must provide a static factory method which
   * takes a string and a lastMod as arguments
   * @param key the primary key (for Lists, this would be the GUID)
   * @param lastMod the time the object was last modified
   * @return a new object.
   * @throws UnsupportedOperationException
   */
  public static StatefulObject make(String key, DateTime lastMod)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }
  /**
   * All StatefulObjects must provide a static factory method which
   * takes no arguments.
   * @return a new object.
   * @throws UnsupportedOperationException
   */
  public static StatefulObject make()
  throws SharepointException {
    throw new SharepointException("Unimplemented make()");
  }

  /**
   * A StatefulObject must be able to create a DOM subtree from itself,
   * and load itself from one.
   * @param element
   * @throws UnsupportedOperationException
   */
  public void loadFromDOM(Element element)
    throws SharepointException {
    throw new SharepointException("Unimplemented loadFromDOM()");
  }
  /** 
   * method implementing the Comparable interface. Primarily compares on
   * lastMod time, with key as a tie-breaker.  This permits a TreeMap to
   * sort on lastMod even if there are duplicates.
   * The method is final because GlobalState.java depends on this
   * comparison method.
   * @param state : other object being compared
   */
  public final int compareTo(StatefulObject state) {
    int lastModComparison = this.lastMod.compareTo(state.lastMod);
    if (lastModComparison != 0) {
      return lastModComparison;
    } else {
      return this.key.compareTo(state.key) ;
    }
  }
 
  /**
   * A StatefulObject may want to maintain different information when it's
   * the "current" object.  For example, ListState keeps a crawl queue for
   * the current list, but only a lastModForDocuments for non-current lists.
   * Concrete classes should, but aren't required to, provide their own
   * implementation of this.
   * @param isCurrent
   */
  protected void setCurrent(boolean current) {
    this.isCurrent = current;
  }
  
  public boolean isCurrent() {
    return isCurrent;
  }
 
  protected String dumpLastMod() {
    return formatter.print(lastMod);

  }
  
  /**
   * parse a joda-time formatted date-time string
   * @param str
   * @return DateTime, or null if the parse failed
   */
  protected DateTime parseLastMod(String str) {
    return formatter.parseDateTime(str);
  }

  /**
   * A StatefulObject must be able to create a DOM subtree which represents
   * itself.
   * @param doc
   * @return
   * @throws UnsupportedOperationException
   */
  public Node dumpToDOM(org.w3c.dom.Document doc) 
    throws SharepointException {
    throw new SharepointException("Unimplemented method: dumpToDOM");
  }

  /**
   * get the "name" of this object (which must be unique)
   * @return string 
   */
  public String getPrimaryKey() {
    return key;
  }

  public void setPrimaryKey(String newKey) {
    this.key = newKey;
  }

  public DateTime getLastMod() {
    return lastMod;
  }

  /**
   * Convenience routine to get the lastMod NOT in JodaTime, but in 
   * java.util.Calendar
   * @return Calendar format for lastMod
   */
  public Calendar getLastModAsCalendar() {
    long millis = timeConverter.getInstantMillis(lastMod, chron);
    GregorianCalendar cal =  new GregorianCalendar();
    cal.setTimeInMillis(millis);
    return cal;
  }
  
  public void setLastMod(DateTime lastMod) {
    this.lastMod = lastMod;
  }
  /**
   * Get the "visited" state.  This is intended for use in traversal, to
   * be able to detect deleted lists from one WebServices call to another.
   * @return visited state
   */
  public boolean isVisited() {
    return visited;
  }

  /**
   * Set the "visited" state.  This is intended for use in traversal, to
   * be able to detect deleted lists from one WebServices call to another.
   * @param visited 
   */  
  protected void setVisited(boolean visited) {
    this.visited = visited;
  }  
}
