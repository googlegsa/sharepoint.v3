//Copyright 2007 Google Inc.

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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.client.Attribute;
import com.google.enterprise.connector.sharepoint.client.SPDocument;
import com.google.enterprise.connector.sharepoint.client.SharepointException;

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
   * The URL of the List is more human-readable, which is good for debugging.
   */
  private String url = "";
  
  private static Log logger = LogFactory.getLog(ListState.class);
  
  /**
   * this should be set by the main Sharepoint client every time it 
   * successfully crawls a SPDocument. For Lists that are NOT current,
   * this is maintained in the persistent state.
   */
  private SPDocument lastDocCrawled;
  private List crawlQueue = null;
  
  private static final String ID = "id";
  private static final String LAST_MOD = "lastMod";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_VALUE = "value";
  private static final String DOC_ATTR = "docAttr";
  private Collator collator = SharepointConnectorType.getCollator();
  
  /**
   * No-args constructor.
   */
  public ListState() {}
  
  /**
   * Constructor.
   * @param inKey The GUID for this List.
   * @param inLastMod last-modified date for List (from SharePoint)
   */
  public ListState(String inKey, DateTime inLastMod){
	  if(inKey!=null){
		  this.key = inKey;
		  this.lastMod = inLastMod;
	  }
  }
  
  public ListState get() {
    return new ListState();
  }

  /**
   * Get the lastMod time.
   * @return time the List was last modified
   */
  public DateTime getLastMod() {
    return lastMod;
  }

  /**
   * Set the lastMod time.
   * @param inLastMod time
   */
  public void setLastMod(DateTime inLastMod) {
    this.lastMod = inLastMod;
  }

  /**
   * Return lastMod in String form.
   * @return lastMod string-ified
   */
  public String getLastModString() {
    return Util.formatDate(lastMod);
  }
  
  /**
   * Get the primary key.
   * @return primary key
   */
  public String getPrimaryKey() {
    return key;
  }

  /**
   * Sets the primary key.
   * @param key
   */
  public void setPrimaryKey(String newKey) {
	  //primary key cannot be null
	  if( newKey!=null){
		  key = newKey;
	  }
  }
  
  public boolean isExisting() {
    return exists;
  }

  public void setExisting(boolean existing) {
    this.exists = existing;
  }

  /**
   * Compares this ListState to another (for the Comparable interface).
   * Comparison is first on the lastMod date. If that produces a tie, the
   * primary key (the GUID) is used as tie-breaker.
   * @param o other ListState.  If null, returns 1.
   * @return the usual integer result: -1 if this object is less, 1 if it's
   *     greater, 0 if equal (which should only happen for the identity
   *     comparison).
   */
  public int compareTo(Object o) {
    ListState other = (ListState) o;
    if (other == null) {  
      return 1; // anything is greater than null
    }
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
    
    //modified by amit
    if ((date!=null)&&(lastDocCrawled != null)) {
      Calendar lastDocDate = lastDocCrawled.getLastMod();
	  if (lastDocDate.toString().compareTo(date.toString()) < 0) {
        date = lastDocDate;
      }
    }
    // now, if there's a crawl queue, we might take its last entry:
    if (crawlQueue != null && crawlQueue.size() > 0) {
      Calendar lastCrawlQueueDate = 
          ((SPDocument) crawlQueue.get(crawlQueue.size() - 1)).getLastMod();
      if (lastCrawlQueueDate.before(date)) {
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
   *     a) process the doc
   *     b) remove it from its local crawl queue
   *     c) setLastDocCrawled()
   *     d) setCrawlQueue() with its local crawl queue
   *     -- OR --
   *     a) process the doc
   *     b) setLastDocCrawled()
   *     c) do getCrawlQueue().first() to get the next doc
   *     
   * It is possible, or even likely,that 'doc' is not the first item in the
   * queue. If we get a checkpoint from the Connector Manager, it could be
   * the 100th of a 100-item queue, or the 50th, or in error cases it might
   * even not be IN the queue. So the operation of this method is:
   * 1) make sure the doc is in the queue, and if so:
   * 2) remove everything up to and including the doc.
   * @param doc
   */
  public void setLastDocCrawled(SPDocument doc) {
	  //null check added by Amit
	if(doc!=null){
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
	    Iterator iter = crawlQueue.iterator();
	    if(iter!=null){
		    while ( iter.hasNext()){
		      SPDocument docQ = (SPDocument) iter.next();
		      iter.remove();
		      if (docQ.equals(doc)) {
		        break;
		      }
		    }
	    }
	}//end null check
  }

  public List getCrawlQueue() {
    return crawlQueue;
  }
  
  public void setUrl(String inUrl) {
	  if(inUrl!=null){
		  this.url = inUrl;
	  }
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
	  String sFunctionName = "dumpCrawlQueue()";
    if (crawlQueue != null && crawlQueue.size() > 0) {
     logger.debug(sFunctionName+" : Crawl queue for " + getUrl());
//      for (SPDocument doc : crawlQueue) {
      for (int iDoc=0; iDoc<crawlQueue.size();++iDoc) {
    	SPDocument doc =(SPDocument) crawlQueue.get(iDoc);
        logger.debug(sFunctionName+": "+ doc.getLastMod().getTime() + ", " + doc.getUrl());
        doc.dumpAllAttrs();
      }
    } else {
      logger.debug(sFunctionName+" : Empty crawl queue for " + getUrl());
    }
  }
  
  public void setCrawlQueue(List inCrawlQueue) {
    this.crawlQueue = inCrawlQueue;
  }
  
  /**
   * Create a DOM tree for an SPDocument. "Attributes" of the SPDocument
   * are dumped as compactly as possible: a single element with two
   * XML attributes: name and value. 
   * @param domDoc the containing XML document (which is needed to create
   * new DOM nodes)
   * @param doc SPDocument
   * @return Node head of the DOM tree
   * @throws SharepointException
   */
  private Node dumpDocToDOM(org.w3c.dom.Document domDoc, SPDocument doc)
      throws SharepointException {
	  //added by amit
	 if(domDoc==null){
		 throw new SharepointException("Unable to get the DOM document");
	 }
	  
    Element element = domDoc.createElement("document");
    //added by amit
    if(element==null){
    	throw new SharepointException("Unable to get the documentt element");
    }
    
    element.setAttribute(ID, doc.getDocId());
    Element lastModTmp = domDoc.createElement(LAST_MOD);
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
    for (Iterator iter = doc.getAllAttrs().iterator(); iter.hasNext();){
      Attribute attr = (Attribute) iter.next();
      Element docAttr = domDoc.createElement(DOC_ATTR);
      docAttr.setAttribute(ATTR_NAME, attr.getName());
      docAttr.setAttribute(ATTR_VALUE, attr.getValue().toString());
      element.appendChild(docAttr);
    }
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
//    Element element = domDoc.createElement(this.getClass().getSimpleName());
    Element element = domDoc.createElement(this.getClass().getName());
    element.setAttribute(ID, getGuid());
    
    // the lastMod
    Element lastModTmp = domDoc.createElement(LAST_MOD);
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
//      for (SPDocument docTmp : crawlQueue) {
      for (int iDoc=0;iDoc<crawlQueue.size();++iDoc) {
    	  SPDocument docTmp = (SPDocument) crawlQueue.get(iDoc);
        queue.appendChild(dumpDocToDOM(domDoc, docTmp));
      }
    }
    return element;
  }
  
  /**
   * Create an SPDocument from a DOM tree.
   * @param element DOM node representing the SPDocument
   * @return SPDocument
   * @throws SharepointException
   */
  private SPDocument loadDocFromDOM(Element element) 
    throws SharepointException {
	 
	  //added by amit
	 if(element==null){
		 throw new SharepointException("element is not found");
	 }
	  
    if (! collator.equals(element.getTagName(),"document")) {
      throw new SharepointException("should be 'document', was "+element.getTagName());
    }
    String id = element.getAttribute(ID);
    NodeList lastModNodeList = element.getElementsByTagName(LAST_MOD);
    NodeList urlNodeList = element.getElementsByTagName("url");
    
    //modified by amit
    if ((urlNodeList==null)||(lastModNodeList == null) || (id == null) || lastModNodeList.getLength() == 0 ||urlNodeList.getLength() == 0) {
      throw new SharepointException("Invalid XML: " + element.toString());
    }
    String lastModString = null;
    if(lastModNodeList.item(0) != null && lastModNodeList.item(0).getFirstChild() != null) {
    	lastModString = lastModNodeList.item(0).getFirstChild().getNodeValue();
    }
    DateTime lastModTmp = Util.parseDate(lastModString);
    //added by amit
    if(lastModTmp==null){
    	throw new SharepointException("Unable to get lastModTmp");
    }
    	
    GregorianCalendar calDate = new GregorianCalendar();
    calDate.setTimeInMillis(lastModTmp.getMillis());
    String urlTmp = null;
    if(urlNodeList.item(0) != null && urlNodeList.item(0).getFirstChild() != null) {
    	urlTmp = URLDecoder.decode(urlNodeList.item(0).getFirstChild().getNodeValue());
    }
    SPDocument doc = new SPDocument(id, urlTmp, calDate);
    
    // pick up the other document attributes:
    NodeList attrNodeList = element.getElementsByTagName(DOC_ATTR);
    //null check added by amit
    if(attrNodeList!=null){
	    for (int i = 0; i < attrNodeList.getLength(); i++) {
	      Element docAttr = (Element) attrNodeList.item(i);
	      String attrName = docAttr.getAttribute(ATTR_NAME);
	      String attrValue = docAttr.getAttribute(ATTR_VALUE);
	      if (attrName != null && attrValue != null) {
	        doc.setAttribute(attrName, attrValue);
	      }
	    }
    }//end..null check
    return doc;
  }
  
  /**
   * Reload this ListState from a DOM tree.  The opposite of dumpToDOM().
   * @param Element the DOM element
   * @throws SharepointException if the DOM tree is not a valid representation
   *     of a ListState 
   */
  public void loadFromDOM(Element element) throws SharepointException {
	if(element==null){
		throw new SharepointException("element not found");
	}
	  
    key = element.getAttribute(ID);
    if (key == null || key.length() == 0) {
      throw new SharepointException("Invalid XML: no id attribute");
    }
    
    // lastMod
    NodeList lastModNodeList = element.getElementsByTagName(LAST_MOD);
    if ((lastModNodeList==null)||lastModNodeList.getLength() == 0) {
      throw new SharepointException("Invalid XML: no lastMod");
    }
    String lastModString = null;
    if(lastModNodeList.item(0) != null && lastModNodeList.item(0).getFirstChild() != null) {
    	lastModString = lastModNodeList.item(0).getFirstChild().getNodeValue();
    }
    lastMod = Util.parseDate(lastModString);
    if (lastMod == null) {
      throw new SharepointException("Invalid XML: bad date " + lastModString);
    }
    
    // URL
    NodeList urlNodeList = element.getElementsByTagName("URL");
    //modified by amit
    if((urlNodeList!=null)&& (urlNodeList.getLength() > 0) ){
    	if(urlNodeList.item(0) != null && urlNodeList.item(0).getFirstChild() != null) {
    		url = urlNodeList.item(0).getFirstChild().getNodeValue();
    	}
    }
    
    // get the lastDocCrawled
    NodeList lastDocCrawledNodeList = 
      element.getElementsByTagName("lastDocCrawled");
    if (lastDocCrawledNodeList != null && lastDocCrawledNodeList.getLength() > 0) {
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
    //modified by amit
    if((crawlQueueNodeList!=null)&& (crawlQueueNodeList.getLength() > 0)) {
      Node crawlQueueNode = crawlQueueNodeList.item(0);
      
      if(crawlQueueNode!=null){//added by amit
      NodeList docNodeList = crawlQueueNode.getChildNodes();
	      if (docNodeList != null) {
	        crawlQueue = new ArrayList();
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
  }
   
  public String getGuid() {
    return key;
  }
}
