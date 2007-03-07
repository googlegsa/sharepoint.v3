package com.google.enterprise.connector.sharepoint.client;

import com.google.common.base.GoogleException;
import com.google.common.base.Join;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.convert.InstantConverter;
import org.joda.time.convert.ConverterManager;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


import java.io.StringWriter;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * Stores the Connector's state information about a List.
 *
 */
public class ListState implements GlobalStateXML, Comparable<ListState> {
  private static Log logger;
  private boolean visited;
  private String guid;
  private DateTime lastModForList; // lastModified time for the list itself
  private DateTime lastModForDocuments;
  private final InstantConverter _timeConverter = 
    ConverterManager.getInstance().getInstantConverter(
      new GregorianCalendar());
  private final Chronology chron = new DateTime().getChronology();
  protected final DateTimeFormatter formatter = ISODateTimeFormat.basicDateTime();

  class Handler extends DefaultHandler {
    StringBuilder characterData = null;
    
    public Handler() {
    }
    @Override
    /**
     * Handles startElement for the &lt;list&gt; element and any of its
     * children.  Depends on the GlobalState to
     */
    public void startElement(String uri, String localName, String qname, Attributes attrs) {

      if (qname.equals("list")) {
        return;
      } else if (qname.equals("guid") || qname.equals("lastMod")) {
        characterData = new StringBuilder("");
      } 
    }
  
    public void characters(char[] ch, int start, int length)
    throws SAXException {
      // ignore any elements we don't know about:
      if (characterData != null) {
        String str = new String(ch, start, length);
        characterData.append(str);
      }
    }
    
    public void endElement(String uri, String localName, String qName)
    throws SAXException {
      if (characterData != null) {
        String characters = characterData.toString();
        if (qName.equals("guid")) {
          setGuid(characters);
        }
        else if (qName.equals("lastMod")) {
          setLastModForList(parseLastModForList(characters));
        }
        characterData = null;
      }
    }
  } 
  
  /**
   * No-argument constructor, for parsers & whatnot
   *
   */
  public ListState() {
    logger = LogFactory.getLog(ListState.class); 
  }
  
  public Object clone() {
    ListState state = new ListState();
    state.setGuid(guid);
    state.setLastModForList(lastModForDocuments);
    state.setVisited(visited);
    return state;
  }
  
  // methods implementing the GlobalStateXML interface
  public String getElementName() {
    return "list";
  }
  
  public ContentHandler getHandler() {
    return new Handler();
  }

  public String dump() {
    StringBuilder res = new StringBuilder("");
    res = (StringBuilder) Join.join(res, "\n", "<list>",
        "\t" + dumpLastModForList(),
        "\t" + dumpGuid(),
        "</list>\n");
    return res.toString();    
  }
  
  public String getPrimaryKey() {
    return guid;
  }
  
  /**
   * get the lastModified time of this object
   * @return DateTime
   */
  public DateTime getLastMod() {
    return lastModForList;
  }
  
  // method implementing the Comparable interface
  public int compareTo(ListState list) {
    return this.guid.compareTo(list.guid);
  }
  
  private String dumpLastModForList() {
    StringBuilder res = new StringBuilder("");
    res.append("<lastMod>");
    res.append(formatter.print(getLastModForList()));
    res.append("</lastMod>");
    return res.toString();
  }
  
  private DateTime parseLastModForList(String str) {
    DateTime time = formatter.parseDateTime(str);
    if (time == null) {
      logger.error("bad date string: " + str);
    }
    return time;
  }
  
  private String dumpGuid() {
    StringBuilder res = new StringBuilder("");
    res.append("<guid>");
    res.append(guid);
    res.append("</guid>");
    return res.toString();
  }
  
  /**
   * Constructor with List GUID
   * @param guid
   */
  public ListState(String guid) {
    this.guid = guid;
  }
  
  
  public void setGuid(String guid) {
    this.guid = guid;
  }
  
  public String getGuid() {
    return guid;
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
  public void setVisited(boolean visited) {
    this.visited = visited;
  }  
  /**
   * Sets the lastModified time for the list itself (<em>not</em> the lastMod
   * time of the item in the list we've last processed). This time must
   * be in the form returned by SharePoint, e.g. 2007-03-02T01:11:10.000Z
   * @param time
   * @throws GoogleException if parsing does not succeed
   */
  public void setLastModForList(DateTime time) {
    this.lastModForList = time;
  }
  
  public DateTime getLastModForList() {
    return this.lastModForList;
  }
}







