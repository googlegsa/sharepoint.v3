// Copyright 2007 Google, Inc. 
package com.google.enterprise.connector.sharepoint.client;


import org.xml.sax.ContentHandler;
import org.joda.time.DateTime;

/**
 * Interface followed by any object which needs its state persisted in XML as
 * part of the GlobalState.  
 *
 */
public interface GlobalStateXML extends Cloneable {
  
  /**
   * During parsing, an empty object can be cloned for each XML element
   * encountered which is owned by that class.
   * see Cloneable.
   * @return clone of this object
   */
  public Object clone();
  
  /**
   * Returns the name of the top-level element which this object generates
   * in its dump() method
   * @return String
   */
  public String getElementName();
  
  /**
   * Returns a ContentHandler whose methods are invoked on parsing, whenever the
   * element named by getElementName() is encountered, until the corresponding
   * end element is seen.
   * @return object implementing the ContentHandler interface
   */
  public ContentHandler getHandler();
  
  /**
   * Dumps the object's state to an XML string, whose top-level element must
   * be the same as getElementName().
   * @return XML string
   */
  public String dump();
  
  /**
   * get the "name" of this object (which must be unique)
   * @return string 
   */
  public String getPrimaryKey();
  
  /**
   * get the lastModified time of this object
   * @return DateTime
   */
  public DateTime getLastMod();
}
