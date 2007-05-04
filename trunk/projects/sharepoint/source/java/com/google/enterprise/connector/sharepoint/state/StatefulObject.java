package com.google.enterprise.connector.sharepoint.state;

import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.client.SharepointException;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * StatefulObject is an interface which is implemented by any object which
 * maintains the traversal state of a SharePoint object.  ListState is the
 * canonical example of a StatefulObject; a List corresponds to a 
 * SPDocument Library and other SharePoint containers.
 * 
 * A StatefulObject must have at least two attributes:
 * 1) a PrimaryKey, which is unique across all instances of that object.
 * 2) a LastMod date, which represents the time the underlying SharePoint
 * was last modified.  LastMod need not be unique.
 * 
 * A StatefulObject must dump its state to a DOM object when asked, and
 * restore its state from a DOM object. A master object (GlobalState) is
 * responsible for assembling those DOM objects into a master DOM, which
 * it dumps to XML and restores from XML.
 *
 */
public interface StatefulObject extends Comparable {

  public Node dumpToDOM(Document doc) throws SharepointException;
  public void loadFromDOM(Element element) throws SharepointException;
  
  /**
   * Getter for the primary key
   * @return primary key
   */
  public String getPrimaryKey();

  /**
   * Setter for the primary key
   * @param newKey
   */
  public void setPrimaryKey(String newKey);

  public DateTime getLastMod();
  
  /**
   * Get lastMod in string form
   * @return string version of lastMod
   */
  public String dumpLastMod();
  
  public void setLastMod(DateTime lastMod);
  

  /**
   * Get the "existing" state.  This is intended for use in traversal, to
   * be able to detect deleted lists from one WebServices call to another.
   * @return existing state
   */
  public boolean isExisting();

  /**
   * Set the "existing" state.  This is intended for use in traversal, to
   * be able to detect deleted lists from one WebServices call to another.
   * @param visited 
   */  
  public void setExisting(boolean existing);
  
  }