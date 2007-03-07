// Copyright 2007 Google, Inc. 
package com.google.enterprise.connector.sharepoint.client;

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
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.StringBuilder;
import java.util.GregorianCalendar;


/**
 * This is the state we maintain for a single Site in SharePoint, be it the
 * "home site" or a subsite within that.
 *
 */
public class SiteState implements GlobalStateXML, Comparable<SiteState> {
  private static Log logger;
  private DateTime lastMod = null; // lastModified time for the site
  private final InstantConverter timeConverter = 
    ConverterManager.getInstance().getInstantConverter(
        new GregorianCalendar());
  private final Chronology _chron = new DateTime().getChronology();
  protected final DateTimeFormatter formatter = ISODateTimeFormat.basicDateTime();
  private String url = null;
  private boolean visited = false;

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

      if (qname.equals("site")) {
        return;
      } else if (qname.equals("url") || qname.equals("lastMod")) {
        characterData = new StringBuilder("");
      } 
    }

    public void characters(char[] ch, int start, int length)
    throws SAXException {
      // this has the effect of ignoring any elements we don't know about
      if (characterData != null) {
        String str = new String(ch, start, length);
        characterData.append(str);
      }
    }

    public void endElement(String uri, String localName, String qName)
    throws SAXException {
      if (characterData != null) {
        String characters = characterData.toString();
        if (qName.equals("url")) {
          setUrl(characters);
        }
        else if (qName.equals("lastMod")) {
          setLastMod(parseLastMod(characters));
        }
        characterData = null;
      }
    }
  } 

  /**
   * Constructor with URL
   * @param url
   */
  public SiteState(String url) {
    this.url = url;
    logger = LogFactory.getLog(SiteState.class);
  }

  /**
   * No-args constructor, for parsers and whatnot
   */
  public SiteState() {
    logger = LogFactory.getLog(SiteState.class);
  }

  /**
   * for Cloneable interface. Used in XML parsing to clone the empty object.
   */
  public Object clone() {
    SiteState site = new SiteState();
    site.setUrl(url);
    site.setLastMod(lastMod);
    site.setVisited(visited);
    return site;
  }
  
  public String getElementName() {
    return "site";
  }
  
  public ContentHandler getHandler() {
    return new Handler();
  }

  public String dump() {
    StringBuilder res = new StringBuilder("");
    res = (StringBuilder) Join.join(res, "\n", "<site>",
        "\t" + dumpLastMod(),
        "\t" + dumpUrl(),
        "</site>\n");
    return res.toString();    
  }
  
  public String getPrimaryKey() {
    return url;
  }
  
  /**
   * get the lastModified time of this object
   * @return DateTime
   */
  public DateTime getLastMod() {
    return lastMod;
  }
  
  // method implementing the Comparable interface
  public int compareTo(SiteState state) {
    return this.url.compareTo(state.url);
  }
  
  private String dumpLastMod() {
    StringBuilder res = new StringBuilder("");
    res.append("<lastMod>");
    res.append(formatter.print(getLastMod()));
    res.append("</lastMod>");
    return res.toString();
  }
  
  private DateTime parseLastMod(String str) {
    DateTime time = formatter.parseDateTime(str);
    if (time == null) {
      logger.error("bad date string: " + str);
    }
    return time;
  }
  
  private String dumpUrl() {
    StringBuilder res = new StringBuilder("");
    res.append("<url>");
    res.append(url);
    res.append("</url>");
    return res.toString();
  }

  public void setLastMod(DateTime mod) {
    lastMod = mod;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
  /**
   * Get the "visited" state.  This is intended for use in traversal, to
   * be able to detect deleted sites from one WebServices call to another.
   * @return visited state
   */
  public boolean isVisited() {
    return visited;
  }

  /**
   * Set the "visited" state.  This is intended for use in traversal, to
   * be able to detect deleted sites from one WebServices call to another.
   * @param visited 
   */  
  public void setVisited(boolean visited) {
    this.visited = visited;
  }

}
