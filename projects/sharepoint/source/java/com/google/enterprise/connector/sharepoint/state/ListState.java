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


import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.client.Document;
import com.google.enterprise.connector.sharepoint.client.SharepointException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.w3c.dom.DOMException;
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
 * Stores the Connector's state information about a List. Besides the 
 * standard StatefulObject fields of primary key (the GUID) and lastMod time,
 * ListState can also store
 *
 */
public class ListState extends StatefulObject {
  private static Log logger;
  
  /**
   * The URL of the List is more human-readable, which is good for debugging:
   */
  private String url = "";
  /**
   * this should be set by the main Sharepoint client every time it 
   * successfully crawls a Document. For Lists that are NOT current,
   * this is maintained in the persistent state.
   */
  private Document lastDocCrawled;
  private List<Document> crawlQueue = null;
  
  /**
   * Factory method, required by the superclass
   * @param key
   * @param lastMod
   * @return new object
   * @throws UnsupportedOperationException
   */
  public static ListState make(String key, DateTime lastMod) {
    ListState obj = new ListState(key, lastMod);
    return obj;
  }
  /**
   * Factory method, required by the superclass
   * @return new object
   * @throws UnsupportedOperationException
   */
  public static ListState make() {
    return new ListState();
  }  
  
  /**
   * No-argument constructor, for parsers & whatnot. 
   *
   */
  protected ListState() {
    logger = LogFactory.getLog(ListState.class); 
  }
  
  /**
   * Constructor with List GUID and lastMod. Private because only the
   * Factory method, make() can invoke (and the Factory method can only
   * be invoked by GlobalState.java)
   * @param guid
   * @param lastMod
   */
  protected ListState(String guid, DateTime lastMod) {
    this.key = guid;
    this.lastMod = lastMod;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getUrl() {
    return url;
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
  
  public Document getLastDocCrawled() {
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
  public void setLastDocCrawled(Document doc) {
    lastDocCrawled = doc;
    if (crawlQueue == null) {
      return;
    }
    if (!crawlQueue.contains(doc)) {
      // don't log. The caller may be removing through an iterator, which 
      // we wouldn't see
      return;
    }
    for (Iterator<Document> iter = crawlQueue.iterator(); iter.hasNext(); ) {
      Document docQ = iter.next();
      iter.remove();
      if (docQ.equals(doc)) {
        break;
      }
    }
  }

  
  public List<Document> getCrawlQueue() {
    return crawlQueue;
  }
  
  public void dumpCrawlQueue() {
    if (crawlQueue != null && crawlQueue.size() > 0) {
      System.out.println("Crawl queue for " + getUrl());
      for (Iterator<Document> iter = crawlQueue.iterator(); iter.hasNext(); ) {
        Document doc = iter.next();
        System.out.println(doc.getLastMod().getTime() + ", " + doc.getUrl());
      }
    }
    else {
      System.out.println("Empty crawl queue for "+ getUrl());
    }
  }
  
  public void setCrawlQueue(List<Document> crawlQueue) {
    this.crawlQueue = crawlQueue;

  }
  
  private Node dumpDocToDOM(org.w3c.dom.Document domDoc, Document doc)
      throws SharepointException {
    Element element = domDoc.createElement("document");
    element.setAttribute("id", doc.getDocId());
    Element lastMod = domDoc.createElement("lastMod");
    lastMod.appendChild(domDoc.createTextNode(
        Util.formatDate(Util.calendarToJoda(doc.getLastMod()))));
    element.appendChild(lastMod);
    Element url = domDoc.createElement("url");
    try {
      url.appendChild(domDoc.createTextNode(
          URLEncoder.encode(doc.getUrl(), "UTF-8")));
    } catch (DOMException e) {
      throw new SharepointException(e.toString());
    } catch (UnsupportedEncodingException e) {
      throw new SharepointException(e.toString());
    }
    element.appendChild(url);
    return element;
  }
  
  public Node dumpToDOM(org.w3c.dom.Document domDoc) 
  throws SharepointException{
    Element element = domDoc.createElement(this.getClass().getSimpleName());
    element.setAttribute("id", getGuid());
    
    // the lastMod
    Element lastMod = domDoc.createElement("lastMod");
    Text text = domDoc.createTextNode(dumpLastMod());
    lastMod.appendChild(text);
    element.appendChild(lastMod);
    
    // the URL
    Element url = domDoc.createElement("URL");
    Text urlText = domDoc.createTextNode(getUrl());
    url.appendChild(urlText);
    element.appendChild(url);
    
    // dump the "last doc crawled"
    if (lastDocCrawled != null) {
      Element elementLastDocCrawled = domDoc.createElement("lastDocCrawled");
      element.appendChild(elementLastDocCrawled);
      elementLastDocCrawled.appendChild(dumpDocToDOM(domDoc, lastDocCrawled));
    }
    if (crawlQueue != null) {
      Element queue = domDoc.createElement("crawlQueue");
      element.appendChild(queue);
      for (Iterator<Document> iter = crawlQueue.iterator();  iter.hasNext(); ) {
        queue.appendChild(dumpDocToDOM(domDoc, iter.next()));
      }
    }
    return element;
  }
  
  private Document loadDocFromDOM(Element element) throws SharepointException {
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
    DateTime lastMod = Util.parseDate(lastModString);
    GregorianCalendar calDate = new GregorianCalendar();
    calDate.setTimeInMillis(lastMod.getMillis());
    String url = URLDecoder.decode(urlNodeList.item(0).getTextContent());
    return new Document(id, url, calDate);
  }
  
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
        crawlQueue = new ArrayList<Document>();
        for (int i = 0; i < docNodeList.getLength(); i++) {
          Node node = docNodeList.item(i);
          if (node.getNodeType() != Node.ELEMENT_NODE) {
            continue;
          }
          Document doc = loadDocFromDOM((Element) node);
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
