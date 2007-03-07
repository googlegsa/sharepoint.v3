// Copyright 2007 Google, Inc. 
package com.google.enterprise.connector.sharepoint.client;

import com.google.common.collect.TreeMultimap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;


/**
 * Represents the state of a site. Can be persisted.
 */
public class GlobalState {
  private static Log logger;
  private boolean refreshing = false;

  protected TreeMultimap<DateTime, ListState> listDateMap = 
    new TreeMultimap<DateTime, ListState>();
  protected HashMap<String, ListState> listGuidMap = 
    new HashMap<String, ListState>();
  
  protected TreeMultimap<DateTime, SiteState> siteDateMap =
    new TreeMultimap<DateTime, SiteState>();
  protected HashMap<String, SiteState> siteUrlMap = 
    new HashMap<String, SiteState>();
  protected final DateTimeFormatter formatter = 
    ISODateTimeFormat.basicDateTime();
  private HashMap<String, GlobalStateXML> elementMap = 
    new HashMap<String, GlobalStateXML>();
  

  /**
   * Internal XML parsing class.  For loading our state from XML. Mainly 
   * passes on the XML to sub-handlers, who are gotten from the elementMap
   * object.   
   *
   */
  class Handler extends DefaultHandler {
    private ContentHandler subHandler;
    private String endingElement = null;
    private GlobalStateXML subObject = null;
    public Handler() {
    }
    @Override
    public void startElement(String uri, String localName, String qname, 
        Attributes attrs)
      throws SAXException {
      if (subHandler != null ) {
        subHandler.startElement(uri, localName, qname, attrs);
      } else {
        GlobalStateXML obj = elementMap.get(qname);
        if (obj != null) {
          subObject = (GlobalStateXML) elementMap.get(qname).clone();   
          subHandler = subObject.getHandler();
          endingElement = qname;
          subHandler.startElement(uri, localName, qname, attrs);
        }
      }
    }
    public void characters(char[] ch, int start, int length)
    throws SAXException {
      if (subHandler != null) {
        subHandler.characters(ch, start, length);
      }
    }

    public void endElement(String uri, String localName, String qName)
    throws SAXException {
      if (subHandler != null) {
        subHandler.endElement(uri, localName, qName);
        if (endingElement.equals(qName)) {
          subHandler = null;
          endingElement = null;
          indexStatefulObject(subObject);
        }
      }
    }
  } 
  
  public GlobalState() {
    logger = LogFactory.getLog(GlobalState.class);
    
    /* seed the elementMap with empty objects of each class storing
     * its state in XML, which can then be cloned during parsing.
     */
    ListState list = new ListState();
    ListState.class.getName();
    elementMap.put(list.getElementName(), list);
    SiteState state = new SiteState();
    elementMap.put(state.getElementName(), state);
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
    setAllSitesVisited(false);
    setAllListsVisited(false);
  }
  
  /**
   * Signals that the refresh cycle is over, and GlobalState may now
   * delete any Site or List which did not appear in a updateSite() /
   * updateList() call since the startRefresh() call.
   */
  public void endRefresh() {
    if (!refreshing) {
      logger.error("called endRefresh() when not in a refresh state");
      return;
    }
    Set<Entry<String,SiteState>> entries = siteUrlMap.entrySet();
    for (Iterator iterSite = entries.iterator(); iterSite.hasNext(); ) {
      Map.Entry<String, SiteState> entry = 
        (Entry<String, SiteState>) iterSite.next();
      SiteState site = entry.getValue();
      if (!site.isVisited()) {
        siteDateMap.remove(site.getLastMod(), site);
        siteUrlMap.remove(site.getUrl());
      }
    }
    Set<Entry<String,ListState>> lists = listGuidMap.entrySet();

    for (Iterator iterList = lists.iterator(); iterList.hasNext(); ) {
      Map.Entry<String, ListState> listEntry = 
        (Entry<String, ListState>) iterList.next();
      ListState list = listEntry.getValue();
      if (!list.isVisited()) {
        listDateMap.remove(list.getLastModForList(), list);
        listGuidMap.remove(list.getGuid());
      }
    }
    refreshing = false;
  }
  
  /**
   * Get an iterator which returns the sites in increasing order of their
   * lastModified dates
   * @return Iterator on sites by lastModified time
   */
  public Iterator<Entry<DateTime, SiteState>> getSiteIterator() {
    Set<Entry<DateTime, SiteState>> entries = siteDateMap.entries();
    return entries.iterator();
  }
  
  /**
   * Get an iterator over the ListStates, in increasing order of their
   * lastModified dates.
   * @return Iterator on sites by lastModified time
   */
  public Iterator<Entry<DateTime, ListState>> getListIterator() {
    Set<Entry<DateTime, ListState>> entries = listDateMap.entries();
    return entries.iterator();
  }
  
  /**
   * Save our state to XML. Crude, for now: each site is an element with
   * its url and lastMod time as attributes.  Similarly for lists, except
   * for that, it's GUID and lastMod time.
   * @return XML
   */
  public String dump() {
    StringBuilder res = new StringBuilder(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    res.append("<state>\n");
    Set<Entry<String, SiteState>> entries = siteUrlMap.entrySet();
    for (Iterator iter = entries.iterator(); iter.hasNext(); ) {
      Map.Entry<String, SiteState> entry = 
        (Entry<String, SiteState>) iter.next();
      res.append(entry.getValue().dump());
    }
    Set<Entry<String, ListState>> lists = listGuidMap.entrySet();
    for (Iterator iter = lists.iterator(); iter.hasNext(); ) {
      Map.Entry<String, ListState> entry = 
        (Entry<String, ListState>) iter.next();
      res.append(entry.getValue().dump());
    }
    res.append("</state>\n");
    return res.toString();
  }
 
  /**
   * Load from persistent state. (this may eventually be changed to be more
   * specific to ConnectorManager.)
   * @param persisted - a string representing the output of a previous
   * dump() call.
   */
  public void load(String persisted) {
    try {
      SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
      XMLReader reader = sp.getXMLReader();
      reader.setContentHandler(new Handler());
      reader.parse(new InputSource(new StringReader(persisted)));
    } catch (IOException e) {
      logger.error(e);
    } catch (ParserConfigurationException e) {
      logger.error(e);
    } catch (SAXException e) {
      logger.error(e);
    }

  }
 
  /**
   * Currently this only applies to ListState and SiteState, but it could
   * be extended to any object implementing the GlobalStateXML interface.
   * Create the necessary data structures
   * @param obj
   */
  public void indexStatefulObject(GlobalStateXML obj) {
    String key = obj.getPrimaryKey();
    DateTime lastMod = obj.getLastMod();
    String className = obj.getClass().getName();
    if (className.endsWith(".ListState")) {
      updateList(key, lastMod);
    } else if (className.endsWith(".SiteState")) {
      updateSite(key, lastMod);
    }
  }
  
  /**
   * For a single site with url & lastModified time, update the two
   * data structures (url -> SiteState and time -> SiteState)
   * @param url
   * @param time
   */
  protected void updateSite(String url, DateTime time) {
    SiteState site = siteUrlMap.get(url);
    if (site != null) {
      DateTime lastMod = site.getLastMod();
      if (lastMod.compareTo(time) < 0) { // if new time is greater
        site.setLastMod(time);
        siteDateMap.remove(lastMod, site);
      }
    } else {
      site = new SiteState(url);
      site.setLastMod(time);
      siteUrlMap.put(url, site);
    }
    site.setVisited(true); // remember we saw this one!
    Collection<SiteState> values = siteDateMap.values();
    System.out.println("there are " + values.size());
    for (Iterator iter = values.iterator(); iter.hasNext(); ) {
      System.out.println(iter.next());
    }
    siteDateMap.put(time, site);
  }
  
  
  /**
   * For a single list with guid & lastModified time, update the two
   * data structures (guid -> time and guid -> url)
   * @param guid
   * @param time
   */
  protected void updateList(String guid, DateTime time) {
    ListState list = listGuidMap.get(guid);
    if (list != null) {
      DateTime lastMod = list.getLastModForList();
      if (lastMod.compareTo(time) < 0) { // if new time is greater
        list.setLastModForList(time);
        listDateMap.remove(lastMod, list);
      }
    } else {
      list = new ListState(guid);
      list.setLastModForList(time);
      listGuidMap.put(guid, list);
    }
    list.setVisited(true); // remember we saw this one!
    listDateMap.put(time, list);
  }
  
  protected void setAllSitesVisited(boolean visited) {
    Set<Entry<String,SiteState>> entries = siteUrlMap.entrySet();
    for (Iterator iter = entries.iterator(); iter.hasNext(); ) {
      Map.Entry<String, SiteState> entry = 
        (Entry<String, SiteState>) iter.next();
      SiteState site = entry.getValue();
      site.setVisited(visited);
    }
  }
  
  private void setAllListsVisited(boolean visited) {
    Set<Entry<String,ListState>> entries = listGuidMap.entrySet();
    for (Iterator iter = entries.iterator(); iter.hasNext(); ) {
      Map.Entry<String, ListState> entry = 
        (Entry<String, ListState>) iter.next();
      ListState list = entry.getValue();
      list.setVisited(visited);
    }
  }
    
}
