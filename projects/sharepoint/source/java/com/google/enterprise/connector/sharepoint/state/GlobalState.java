// Copyright 2007 Google, Inc.
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

import com.google.enterprise.connector.persist.PrefsStore;
import com.google.enterprise.connector.sharepoint.state.Util;
import com.google.enterprise.connector.sharepoint.client.SharepointException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * Represents the state of a site. Can be persisted to XML and
 * loaded again from the XML. This would normally be done if the
 * connector wants to be restartable.
 * GlobalState can have an arbitrary number of StatefulObject-derived
 * classes injected into it at initialization time. Its state is
 * composed almost entirely of the state of those classes. (the "almost"
 * comes from the fact that GlobalState stores the "current" member of
 * each dependent class, if any exists)
 * As of April 2007, there is only one StatefulObject -- ListState.
 *
 * Classes:
 *    GlobalState.
 * related classes:
 *   StatefulObject (base class)
 *   ListState (instance of StatefulObject)
 *
 */
public class GlobalState {
  private static Log logger = LogFactory.getLog(GlobalState.class);
  private static final String CONNECTOR_NAME = "Sharepoint";

  /**
   * Map of short class names (everything after last period) to the
   * actual classes:
   */
  private static HashMap<String, Class<StatefulObject>> dependencies =
    new HashMap<String, Class<StatefulObject>>(3);

  private boolean refreshing = false;

  /**
   * For each dependency, we keep two data structures: a TreeSet relying on
   * the lastMod property of a StatefulObject object, and a HashMap on the
   * primary key of the object (url for Sites, guid for Lists)
   */
  protected HashMap<String, TreeSet<StatefulObject>> dateMaps =
    new HashMap<String, TreeSet<StatefulObject>>(3);
  protected HashMap<String, HashMap<String, StatefulObject>> keyMaps =
    new HashMap<String, HashMap<String, StatefulObject>>(3);

  /**
   * The "current" object for each dependency, e.g. ListState. The current
   * object may be null.
   */
  protected HashMap<String, StatefulObject> currentObjs =
    new HashMap<String, StatefulObject>(3);

  /**
   * The dependency-injection point.  A StatefulObject object is a stateful
   * object which can save its state to XML and load from that XML. GlobalState
   * must also act as a Factory for the class.
   * @param dependency class, which must be assignable from StatefulObject
   */
  public static void injectDependency(Class dependency)
    throws SharepointException {
    if (!StatefulObject.class.isAssignableFrom(dependency)) {
      throw new SharepointException("invalid class: " +
          dependency.getCanonicalName());
    }
    String className = dependency.getSimpleName();
    dependencies.put(className, dependency);
  }

  /**
   * Constructor. injectDependency(), a static method, should have been called
   * before this.
   * @throws SharepointException if there are no dependencies (since that
   * almost certainly indicates a programming error)
   */
  public GlobalState() throws SharepointException{

    // for each dependency, create the two maps (on lastMod and key)
    Set<Entry<String, Class<StatefulObject>>> entries =
      dependencies.entrySet();
    if (entries.size() == 0) {
      throw new SharepointException("bad initialization (no dependencies)");
    }
    for (Iterator<Entry<String, Class<StatefulObject>>> iter =
        entries.iterator(); iter.hasNext(); ) {
      Entry<String, Class<StatefulObject>> entry = iter.next();
      String simpleName = entry.getValue().getSimpleName();
      dateMaps.put(simpleName, new TreeSet<StatefulObject>());
      keyMaps.put(simpleName, new HashMap<String, StatefulObject>());
      currentObjs.put(simpleName, null);
    }
  }

  /**
   * Create a StatefulObject (e.g. a ListState).
   * @param simpleName  simple name of the class, e.g. "ListState", which
   * must be one of this class's dependencies (staticly added via
   * addDependency()).
   * @param key the "primary key" of the object. For ListState this would
   * probably be the GUID.
   * @param lastMod most recent time this object was modified.
   * @return new object
   * @throws SharepointException if simpleName is not one of the
   * dependencies of this object.
   */
  public StatefulObject makeDependentObject(String simpleName,
      String key, DateTime lastMod)  throws SharepointException {
    Class<StatefulObject> cls = dependencies.get(simpleName);
    if (cls == null) {
      throw new SharepointException("Unknown class: " + simpleName);
    }
    try {
      StatefulObject obj = cls.newInstance();
      obj.setLastMod(lastMod);
      obj.setPrimaryKey(key);
      updateStatefulObject(obj, lastMod); // add to our maps
      return obj;
    } catch (InstantiationException e) {
      throw new SharepointException("Internal error: " + e.toString());
    } catch (IllegalAccessException e) {
      throw new SharepointException("Internal error: " + e.toString());
    }
  }

  /**
   * Convenience routine for clients who don't deal in Joda time.
   * @param simpleName
   * @param key
   * @param lastModCal (Calendar, not Joda time)
   * @return new object
   * @throws SharepointException if simpleName is not one of the
   * dependencies of this object.
   */
  public StatefulObject makeDependentObject(String simpleName,
      String key, Calendar lastModCal)  throws SharepointException {
    return makeDependentObject(simpleName, key,
        Util.calendarToJoda(lastModCal));
  }

  /**
   * Look up the key -> obj map for the class simpleName.  Since this is
   * a private routine, it's assumed that the class name is valid; otherwise
   * null is returned (and must be checked for).
   * @param simpleName
   * @return the HashMap for that class
   */
  private Map<String, StatefulObject> getKeyMap(String simpleName) {
    return keyMaps.get(simpleName);
  }

  private SortedSet<StatefulObject> getDateMap(String simpleName) {
    return dateMaps.get(simpleName);
  }
  /**
   * Signal that a complete "refresh" cycle is beginning, where all sites
   * and all lists are being fetched from SharePoint.  This GlobalState will
   * keep track of which objects are still present.  At the endRefresh()
   * call, objects no longer present may be removed.
   *
   */
  public void startRefresh() {
    refreshing = true;
    // for each dependency, set all the instances "not visited"
    Set<Entry<String, Class<StatefulObject>>> dependencySet =
      dependencies.entrySet();
    for (Iterator<Entry<String, Class<StatefulObject>>> iter =
      dependencySet.iterator(); iter.hasNext(); ) {
      Entry<String, Class<StatefulObject>> dependency = iter.next();
      String simpleName = dependency.getValue().getSimpleName();
      setAllVisited(simpleName, false);
    }
  }

  /**
   * Signals that the refresh cycle is over, and GlobalState may now
   * delete any StatefulObject which did not appear in a updateSite() /
   * updateList() call since the startRefresh() call.
   */
  public void endRefresh() {
    if (!refreshing) {
      logger.error("called endRefresh() when not in a refresh state");
      return;
    }
    Set<Entry<String, Class<StatefulObject>>> dependencySet =
      dependencies.entrySet();
    for (Iterator<Entry<String, Class<StatefulObject>>> iterDependencies =
      dependencySet.iterator(); iterDependencies.hasNext(); ) {
      Entry<String, Class<StatefulObject>> dependency = iterDependencies.next();
      String simpleName = dependency.getValue().getSimpleName();
      Map<String, StatefulObject> keyMap = getKeyMap(simpleName);
      SortedSet<StatefulObject> dateMap = getDateMap(simpleName);
      for (Iterator<StatefulObject> iterObj = dateMap.iterator();
      iterObj.hasNext(); ) {
        StatefulObject obj = iterObj.next();
        if (!obj.isVisited()) {
          iterObj.remove(); // we MUST use the iterator's own remove() here
          keyMap.remove(obj.getPrimaryKey());
        }
      }
    }
    refreshing = false;
  }

  /**
   * Get an iterator which returns the objects in increasing order of their
   * lastModified dates
   * @param simpleName the name of the class for the desired objects
   *   (e.g. "ListState")
   * @return Iterator on the objects by lastModified time
   * @throws SharepointException if simpleName is not one of the
   *  dependencies of this class
   */
  public Iterator<StatefulObject> getIterator(String simpleName)
     throws SharepointException {
    SortedSet<StatefulObject> dateMap = getDateMap(simpleName);
    if (dateMap == null) {
      throw new SharepointException("Internal error: no class " + simpleName);
    }
    return dateMap.iterator();
  }

  /**
   * Lookup a StatefulObject by its key
   * @param simpleName class of the object
   * @param key primary key
   * @return object handle, or null if none found
   * @throws SharepointException if simpleName is not one of the
   *  dependencies of this class
   */
  public StatefulObject lookupObject(String simpleName, String key)
      throws SharepointException{
    Map<String, StatefulObject> keyMap = getKeyMap(simpleName);
    if (keyMap == null) {
      throw new SharepointException("Internal error: no class " + simpleName);
    }
    return keyMap.get(key);
  }

  /**
   * Return an XML string representing the current state. Since our state
   * is comprised almost entirely of the state of the dependent
   * classes (e.g. ListState), this is largely done by calling an
   * analogous method on those classes. Here is an example (note that
   * the only state that belongs to GlobalState itself if the "current"
   * object for each dependent class).  Consult ListState for details on
   * its XML representation.
   * <pre>
    <?xml version="1.0" encoding="UTF-8"?>
    <state>
      <current id="foo" type="ListState"/>
      <ListState id="bar">
        <lastMod>20070420T154348.133-0700</lastMod>
        <URL/>
        <lastDocCrawled>
          <document id="id2">
            <lastMod>20070420T154348.133-0700</lastMod>
            <url>url2</url>
          </document>
        </lastDocCrawled>
      </ListState>
      <ListState id="foo">
        <lastMod>20070420T154348.133-0700</lastMod>
        <URL/>
        <lastDocCrawled>
          <document id="id1">
            <lastMod>20070420T154348.133-0700</lastMod>
            <url>url1</url>
          </document>
        </lastDocCrawled>
        <crawlQueue>
          <document id="id3">
            <lastMod>20070420T154348.133-0700</lastMod>
            <url>url3</url>
          </document>
          <document id="id4">
            <lastMod>20070420T154348.133-0700</lastMod>
            <url>url4</url>
          </document>
        </crawlQueue>
      </ListState>
    </state>
   * </pre>
   * @return XML string
   */
  public String getStateXML() throws SharepointException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    org.w3c.dom.Document doc;
    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      doc = builder.newDocument();
    } catch (ParserConfigurationException e) {
      throw new SharepointException(e.toString());
    }
    Element top = doc.createElement("state");
    doc.appendChild(top);
    Set<Entry<String, Class<StatefulObject>>> dependencySet =
      dependencies.entrySet();

    // first dump our "current" objects, for each dependency:
    for (Iterator<Entry<String, Class<StatefulObject>>> iterDependencies =
      dependencySet.iterator(); iterDependencies.hasNext(); ) {
      Entry<String, Class<StatefulObject>> dependency = iterDependencies.next();
      String simpleName = dependency.getValue().getSimpleName();
      StatefulObject obj = currentObjs.get(simpleName);
      if (obj != null) {
        Element element = doc.createElement("current");
        element.setAttribute("type", simpleName);
        element.setAttribute("id", obj.getPrimaryKey());
        top.appendChild(element);
      }
    }

    // now dump the actual dependent objects (ListState, initially):
    for (Iterator<Entry<String, Class<StatefulObject>>> iterDependencies =
      dependencySet.iterator(); iterDependencies.hasNext(); ) {
      Entry<String, Class<StatefulObject>> dependency = iterDependencies.next();
      String simpleName = dependency.getValue().getSimpleName();
      SortedSet<StatefulObject> dateMap = getDateMap(simpleName);
      for (Iterator<StatefulObject> iterObj = dateMap.iterator();
      iterObj.hasNext(); ) {
        StatefulObject obj = iterObj.next();
        top.appendChild(obj.dumpToDOM(doc));
      }
    }
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = null;
    try {
      t = tf.newTransformer();
    } catch (TransformerConfigurationException e) {
      throw new SharepointException(e.toString());
    }
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    t.setOutputProperty(OutputKeys.METHOD, "xml");
    t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    DOMSource doms = new DOMSource(doc);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    StreamResult sr = new StreamResult(os);
    try {
      t.transform(doms, sr);
    } catch (TransformerException e) {
      throw new SharepointException(e.toString());
    }
    return os.toString();
  }

  /**
   * Creates an XML representation of our state, and saves it, using the
   * PrefsStore mechanism.
   * @throws SharepointException
   */
  public void saveState()  throws SharepointException {
    PrefsStore store = new PrefsStore();
    String xml = getStateXML();
    logger.info(xml);
    store.storeConnectorState(CONNECTOR_NAME, xml);
  }

  /**
   * Load from XML.
   * @param persisted - a string representing the output of a previous
   * getStateXML() call.
   */
  public void loadStateXML(String persisted) throws SharepointException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      StringReader stringReader = new StringReader(persisted);
      org.w3c.dom.Document doc = builder.parse(new InputSource(stringReader));
      NodeList nodeList = doc.getElementsByTagName("state");
      if (nodeList.getLength() == 0) {
        throw new SharepointException("Invalid XML: no <state> element");
      }
      // temporary list of the "current" objects (just their keys):
      HashMap<String, String> currentKeys = new HashMap<String, String>();

      NodeList children = nodeList.item(0).getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node node = children.item(i);
        if (node.getNodeType() != Node.ELEMENT_NODE) {
          continue;
        }
        Element element = (Element) children.item(i);
        if (element.getTagName().equals("current")) {
          String type = element.getAttribute("type");
          String id = element.getAttribute("id");
          if (type != null && id != null) {
            currentKeys.put(type, id);
          }
          continue;
        }
        String simpleName = element.getTagName();
        Class cls = dependencies.get(simpleName);
        if (cls == null) {
          continue; //DON'T throw exception; just ignore things we don't know
        }
        try {
          StatefulObject subObject = (StatefulObject) cls.newInstance();
          subObject.loadFromDOM(element);
          updateStatefulObject(subObject, subObject.getLastMod());
        } catch (InstantiationException e) {
          throw new SharepointException(e.toString());
        } catch (IllegalAccessException e) {
          throw new SharepointException(e.toString());
        }
      }
      // now, for each "current", for which so far we've only the key, find the
      // actual object:
      Set<Entry<String, String>> entries = currentKeys.entrySet();
      for (Iterator<Entry<String, String>> iter = entries.iterator();
        iter.hasNext(); ) {
        Entry<String, String> entry = iter.next();
        String simpleName = entry.getKey();
        StatefulObject obj = keyMaps.get(simpleName).get(entry.getValue());
        currentObjs.put(simpleName, obj);
        if (obj != null) {
          obj.setCurrent(true);
        }
      }
    } catch (IOException e) {
      logger.error(e);
      throw new SharepointException(e.toString());
    } catch (ParserConfigurationException e) {
      logger.error(e);
      throw new SharepointException(e.toString());
    } catch (SAXException e) {
      logger.error(e);
      throw new SharepointException(e.toString());
    }
  }

  /**
   * Load persistent state from our XML state
   * @throws SharepointException if the XML file can't be found, or is
   * invalid in any way.
   */
  public void loadState() throws SharepointException {
    PrefsStore store = new PrefsStore();
    String state = store.getConnectorState(CONNECTOR_NAME);
    if (state != null) {
      loadStateXML(state);
    }
  }


  /**
   * For a given dependency (e.g. ListState), set the given object as "current"
   * This will be remembered in the XML state.
   * @param simpleName
   * @param obj
   * @throws SharepointException if simpleName is not a registered
   *   dependency
   */
  public void setCurrentObject(String simpleName, StatefulObject obj)
  throws SharepointException {
    if (dependencies.get(simpleName) == null) {
      throw new SharepointException("Internal error: bad class name " +
          simpleName);
    }
    // remove the old current, if any
    StatefulObject oldCurrent = currentObjs.get(simpleName);
    if (oldCurrent != null) {
      oldCurrent.setCurrent(false);
    }
    // set the new current
    currentObjs.put(simpleName, obj);
    if (obj != null) {
      obj.setCurrent(true);
    }
  }

  /**
   * Get the current object for a given dependency (e.g. ListState)
   * @param simpleName
   * @return the current object, or null if none
   * @throws SharepointException if simpleName doesn't correspond to a
   *   known dependency
   */
  public StatefulObject getCurrentObject(String simpleName)
  throws SharepointException {
    if (dependencies.get(simpleName) == null) {
      throw new SharepointException("Internal error: bad class name " +
          simpleName);
    }
    return currentObjs.get(simpleName);
  }

  /**
   * For a single StatefulObject, update the two
   * data structures (url -> obj and time -> obj) and mark
   * it "visited" (if between startRefresh() and endRefresh())
   * @param StatefulObject
   * @param time
   */
  public void updateStatefulObject(StatefulObject state, DateTime time)
    throws SharepointException {
    String simpleName = state.getClass().getSimpleName();
    Map<String, StatefulObject> keyMap = getKeyMap(simpleName);
    SortedSet<StatefulObject> dateMap = getDateMap(simpleName);
    if (keyMap == null || dateMap == null) {
      throw new SharepointException("Internal error: invalid class");
    }
    StatefulObject stateOld = keyMap.get(state.getPrimaryKey());
    if (stateOld != null) {
      if (stateOld.getLastMod().compareTo(time) != 0) { // if new time differs
        dateMap.remove(state);
        state.setLastMod(time);
      }
    } else {
      state.setLastMod(time);
      keyMap.put(state.getPrimaryKey(), state);
    }
    if (refreshing) {
      state.setVisited(true); // remember we saw this one!
    }
    dateMap.add(state);
  }

  /**
   * Mark all the dependent objects "visited"
   * @param simpleName
   * @param visited
   */
  private void setAllVisited(String simpleName, boolean visited) {
    Map<String, StatefulObject> keyMap = getKeyMap(simpleName);
    Set<Entry<String, StatefulObject>> entries = keyMap.entrySet();
    for (Iterator iter = entries.iterator(); iter.hasNext(); ) {
      Map.Entry<String, StatefulObject> entry =
        (Entry<String, StatefulObject>) iter.next();
      StatefulObject state = entry.getValue();
      state.setVisited(visited);
    }
  }


}
