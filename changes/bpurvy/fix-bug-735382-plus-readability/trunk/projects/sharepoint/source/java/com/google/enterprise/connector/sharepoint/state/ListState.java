//Copyright 2006 Google Inc.

package com.google.enterprise.connector.sharepoint.state;

//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

import com.google.enterprise.connector.sharepoint.client.SharepointException;

import com.google.enterprise.connector.sharepoint.client.SPDocument;
import com.google.enterprise.connector.sharepoint.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

/**
 * Maintains the SharePoint connector's state with respect to a List (a
 * SharePoint container, e.g. Document Library).  In addition to the 
 * functionality required by the StatefulObject interface, ListState also keeps
 * 1) The "crawl queue", an ordered list of SPDocuments due to be fed
 * to the Connector Manager
 * 2) The "last crawled" SPDocument, i.e. the last one that was successfully
 * fed to Connector Manager
 * 3) the List "url", the trailing part of the URL for this List
 * It also determines, from at least (1) and (2), the date-time that the 
 * connector should pass to SharePoint in its GetListItemChanges web services
 * call.
 */
public class ListState implements StatefulObject {
  protected String key = null;
  protected DateTime lastMod = null;
  
  /**
   * Whether the underlying SharePoint object that this object was created
   * to represent actually exists. This variable is periodically set to false
   * by GlobalState, and then set true if the underlying object is found to
   * be still there.
   */
  private boolean exists = true;
  
  /**
   * The URL of the List is more human-readable, which is good for debugging:
   */
  private String url = "";
  
  private static Log logger = LogFactory.getLog(ListState.class);
  
  /**
   * No-args constructor
   */
  public ListState() {}
  
  /**
   * Constructor
   * @param key
   * @param lastMod
   */
  public ListState(String key, DateTime lastMod) {
    this.key = key;
    this.lastMod = lastMod;
  }
  
  public ListState get() {
    return new ListState();
  }
  /**
   * this should be set by the main Sharepoint client every time it 
   * successfully crawls a SPDocument. For Lists that are NOT current,
   * this is maintained in the persistent state.
   */
  private SPDocument lastDocCrawled;
  private List<SPDocument> crawlQueue = null;
  
  /**
   * Get the lastMod time
   * @return time the List was last modified
   */
  public DateTime getLastMod() {
    return lastMod;
  }

  /**
   * Set the lastMod time
   * @param lastMod time
   */
  public void setLastMod(DateTime lastMod) {
    this.lastMod = lastMod;
  }

  /**
   * Return lastMod in String form
   * @return lastMod string-ified
   */
  public String getLastModString() {
    return Util.formatDate(lastMod);
  }
  
  /**
   * Get the primary key
   * @return primary key
   */
  public String getPrimaryKey() {
    return key;
  }

  /**
   * Sets the primary key
   * @param key
   */
  public void setPrimaryKey(String newKey) {
    key = newKey;
  }
  
  public boolean isExisting() {
    return exists;
  }

  public void setExisting(boolean existing) {
    this.exists = existing;
  }

  public int compareTo(Object o) {
    ListState other = (ListState) o;
    int lastModComparison = this.lastMod.compareTo(other.lastMod);
    if (lastModComparison != 0) {
      return lastModComparison;
    } else {
      return this.key.compareTo(other.key);
    }
  }

  /**
   * Return the date which should be passed to Web Services (the
   * GetListItemChanges call of ListWS) in order to find new items for
   * this List.  This should be called in preference to getLastDocCrawled()
   * or any other lower-level method,
   * since this method can apply some intelligence, e.g. looking at the 
   * crawl queue, and that intelligence may change over time.
   * 
   * Current algorithm: take the later of the date in getLastDocCrawled()
   * and the crawl queue, IF ANY.  Otherwise, take the lastMod of the
   * List itself.
   * 
   * @return Calendar suitable for passing to WebServices
   */
  public Calendar getDateForWSRefresh() {
    Calendar date = Util.jodaToCalendar(getLastMod()); // our default
    if (lastDocCrawled != null) {
      Calendar lastDocDate = lastDocCrawled.getLastMod();
      if (lastDocDate.compareTo(date) < 0) {
        date = lastDocDate;
      }
    }
    // now, if there's a crawl queue, we might take its last entry:
    if (crawlQueue != null && crawlQueue.size() > 0) {
      Calendar lastCrawlQueueDate = 
        crawlQueue.get(crawlQueue.size() - 1).getLastMod();
      if (lastCrawlQueueDate.compareTo(date) < 0) {
        date = lastCrawlQueueDate;
      }
    }
    return date;
  }
  
  public SPDocument getLastDocCrawled() {
    return lastDocCrawled;
  }

  /**
   * setting lastDocCrawled has two effects:
   * 1) doc is remembered.
   * 2) if doc is present in the current crawlQueue, it is removed. It is not
   * an error if doc is NOT present; thus, the client can do either this style:
   *   a) process the doc
   *   b) remove it from its local crawl queue
   *   c) setLastDocCrawled()
   *   d) setCrawlQueue() with its local crawl queue
   *  -- OR --
   *   a) process the doc
   *   b) setLastDocCrawled()
   *   c) do getCrawlQueue().first() to get the next doc
   * It is possible, or even likely,that 'doc' is not the first item in the
   * queue. If we get a checkpoint from the Connector Manager, it could be
   * the 100th of a 100-item queue, or the 50th, or in error cases it might
   * even not be IN the queue. So the operation of this method is:
   * 1) make sure the doc is in the queue, and if so:
   * 2) remove everything up to and including the doc.
   * @param doc
   */
  public void setLastDocCrawled(SPDocument doc) {
    lastDocCrawled = doc;
    if (crawlQueue == null) {
      return;
    }
    if (!crawlQueue.contains(doc)) {
      // don't log. The caller may be removing through an iterator, which 
      // we wouldn't see
      return;
    }
    // "foreach" syntax not used because we need the iterator to remove()
    for (Iterator<SPDocument> iter = crawlQueue.iterator(); iter.hasNext(); ) {
      SPDocument docQ = iter.next();
      iter.remove();
      if (docQ.equals(doc)) {
        break;
      }
    }
  }

  
  public List<SPDocument> getCrawlQueue() {
    return crawlQueue;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getUrl() {
    return url;
  }
  
  /**
   * Debug routine: dump the crawl queue to stdout. (this is deliberately
   * in preference to log messages, since it's much easier to follow in 
   * Eclipse.)
   */
  public void dumpCrawlQueue() {
    if (crawlQueue != null && crawlQueue.size() > 0) {
      System.out.println("Crawl queue for " + getUrl());
      for (SPDocument doc : crawlQueue) {
        System.out.println(doc.getLastMod().getTime() + ", " + doc.getUrl());
      }
    } else {
      System.out.println("Empty crawl queue for " + getUrl());
    }
  }
  
  public void setCrawlQueue(List<SPDocument> crawlQueue) {
    this.crawlQueue = crawlQueue;
  }
  
  /**
   * Create a DOM tree for an SPDocument.
   * @param domDoc the containing XML document (which is needed to create
   * new DOM nodes)
   * @param doc SPDocument
   * @return Node head of the DOM tree
   * @throws SharepointException
   */
  private Node dumpDocToDOM(org.w3c.dom.Document domDoc, SPDocument doc)
      throws SharepointException {
    Element element = domDoc.createElement("document");
    element.setAttribute("id", doc.getDocId());
    Element lastModTmp = domDoc.createElement("lastMod");
    lastModTmp.appendChild(domDoc.createTextNode(
        Util.formatDate(Util.calendarToJoda(doc.getLastMod()))));
    element.appendChild(lastModTmp);
    Element urlTmp = domDoc.createElement("url");
    try {
      urlTmp.appendChild(domDoc.createTextNode(
          URLEncoder.encode(doc.getUrl(), "UTF-8")));
    } catch (DOMException e) {
      throw new SharepointException(e.toString());
    } catch (UnsupportedEncodingException e) {
      throw new SharepointException(e.toString());
    }
    element.appendChild(urlTmp);
    return element;
  }
  
  /**
   * Create a DOM tree for the entire ListState object.
   * @param domDoc DOM node for the containing XML document, which is necessary
   *  for creating new DOM nodes
   * @return Node head of DOM tree
   * @throws SharepointException
   */
  public Node dumpToDOM(Document domDoc) throws SharepointException{
    Element element = domDoc.createElement(this.getClass().getSimpleName());
    element.setAttribute("id", getGuid());
    
    // the lastMod
    Element lastModTmp = domDoc.createElement("lastMod");
    Text text = domDoc.createTextNode(getLastModString());
    lastModTmp.appendChild(text);
    element.appendChild(lastModTmp);
    
    // the URL
    Element urlTmp = domDoc.createElement("URL");
    Text urlText = domDoc.createTextNode(getUrl());
    urlTmp.appendChild(urlText);
    element.appendChild(urlTmp);
    
    // dump the "last doc crawled"
    if (lastDocCrawled != null) {
      Element elementLastDocCrawled = domDoc.createElement("lastDocCrawled");
      element.appendChild(elementLastDocCrawled);
      elementLastDocCrawled.appendChild(dumpDocToDOM(domDoc, lastDocCrawled));
    }
    if (crawlQueue != null) {
      Element queue = domDoc.createElement("crawlQueue");
      element.appendChild(queue);
      for (SPDocument docTmp : crawlQueue) {
        queue.appendChild(dumpDocToDOM(domDoc, docTmp));
      }
    }
    return element;
  }
  
  /**
   * Create an SPDocument from a DOM tree
   * @param element DOM node representing the SPDocument
   * @return SPDocument
   * @throws SharepointException
   */
  private SPDocument loadDocFromDOM(Element element) 
    throws SharepointException {
    if (!element.getTagName().equals("document")) {
      throw new SharepointException("should be 'document', was " + 
          element.getTagName());
    }
    String id = element.getAttribute("id");
    NodeList lastModNodeList = element.getElementsByTagName("lastMod");
    NodeList urlNodeList = element.getElementsByTagName("url");
    if (id == null || lastModNodeList.getLength() == 0 ||
        urlNodeList.getLength() == 0) {
      throw new SharepointException("Invalid XML: " + element.toString());
    }
    String lastModString = lastModNodeList.item(0).getTextContent();
    DateTime lastModTmp = Util.parseDate(lastModString);
    GregorianCalendar calDate = new GregorianCalendar();
    calDate.setTimeInMillis(lastModTmp.getMillis());
    String urlTmp = URLDecoder.decode(urlNodeList.item(0).getTextContent());
    return new SPDocument(id, urlTmp, calDate);
  }
  
  /**
   * Reload this ListState from a DOM tree.  The opposite of dumpToDOM().
   * @param Element the DOM element
   * @throws SharepointException if the DOM tree is not a valid representation
   *   of a ListState 
   */
  public void loadFromDOM(Element element) throws SharepointException {
    key = element.getAttribute("id");
    if (key == null || key.length() == 0) {
      throw new SharepointException("Invalid XML: no id attribute");
    }
    
    // lastMod
    NodeList lastModNodeList = element.getElementsByTagName("lastMod");
    if (lastModNodeList.getLength() == 0) {
      throw new SharepointException("Invalid XML: no lastMod");
    }
    String lastModString = lastModNodeList.item(0).getTextContent();
    lastMod = Util.parseDate(lastModString);
    if (lastMod == null) {
      throw new SharepointException("Invalid XML: bad date " + lastModString);
    }
    
    // URL
    NodeList urlNodeList = element.getElementsByTagName("URL");
    if (urlNodeList.getLength() > 0) {
      url = urlNodeList.item(0).getTextContent();
    }
    
    // get the lastDocCrawled
    NodeList lastDocCrawledNodeList = 
      element.getElementsByTagName("lastDocCrawled");
    if (lastDocCrawledNodeList != null && 
        lastDocCrawledNodeList.getLength() > 0) {
      Element lastDocCrawledNode = (Element) lastDocCrawledNodeList.item(0);
      NodeList documentNodeList = 
        lastDocCrawledNode.getElementsByTagName("document");
      Node documentNode = documentNodeList.item(0);
      if (documentNode.getNodeType() == Node.ELEMENT_NODE) {
        lastDocCrawled = loadDocFromDOM((Element) documentNode);
      }
    }
    
    // get the crawlQueue
    NodeList crawlQueueNodeList = element.getElementsByTagName("crawlQueue");
    if (crawlQueueNodeList.getLength() > 0) {
      Node crawlQueueNode = crawlQueueNodeList.item(0);
      NodeList docNodeList = crawlQueueNode.getChildNodes();
      if (docNodeList != null) {
        crawlQueue = new ArrayList<SPDocument>();
        for (int i = 0; i < docNodeList.getLength(); i++) {
          Node node = docNodeList.item(i);
          if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
          }
          SPDocument doc = loadDocFromDOM((Element) node);
          if (doc != null) {
            crawlQueue.add(doc);
          }
        }
      }
    }
  }
   
  public String getGuid() {
    return key;
  }
}
