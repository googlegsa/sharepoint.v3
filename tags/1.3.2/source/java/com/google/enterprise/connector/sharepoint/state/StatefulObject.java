//Copyright 2007 Google Inc.

package com.google.enterprise.connector.sharepoint.state;

//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.enterprise.connector.sharepoint.client.SharepointException;

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

  Node dumpToDOM(Document doc) throws SharepointException;
  void loadFromDOM(Element element) throws SharepointException;
  
  /**
   * Getter for the primary key.
   * @return primary key
   */
  String getPrimaryKey();

  /**
   * Setter for the primary key.
   * @param newKey
   */
  void setPrimaryKey(String newKey);

  DateTime getLastMod();
  
  /**
   * Get lastMod in string form.
   * @return string version of lastMod
   */
  String getLastModString();
  
  void setLastMod(DateTime lastMod);

  /**
   * Get the "existing" state.  This is intended for use in traversal, to
   * be able to detect deleted lists from one WebServices call to another.
   * @return existing state
   */
  boolean isExisting();

  /**
   * Set the "existing" state.  This is intended for use in traversal, to
   * be able to detect deleted lists from one WebServices call to another.
   * @param existing 
   */  
  void setExisting(boolean existing);
  
}