package com.google.enterprise.connector.sharepoint.state;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.client.SharepointException;
/**
 * 
 * @author amit_kagrawal
 *
 */

public class WebState implements StatefulObject{
	private static final String ID = "id";
	private static final String INSERT_TIME = "insertionTime";
	private String webUrl = null;
	private String webId = null;
	//private String webTitle=null;
	private String title = "No Title"; 
	private static final String WEB_TITLE="WebTitle";
	private DateTime lastMod = null;
	private DateTime insertionTime = null;
	private boolean exists = true;
	private TreeSet allListStateSet = new TreeSet();
	private Map keyMap = new HashMap();
	private Collator collator = SharepointConnectorType.getCollator();
	private static final Logger LOGGER = Logger.getLogger(WebState.class.getName());
	private String className = WebState.class.getName();
//	private boolean bFullReCrawl = false;

	/**
	 * The "current" object for ListState. The current object may be null.
	 */
	private ListState currentList = null;
	private String lastCrawledListID = null;

	public WebState(){

	}

	/**
	 * @param inWebID 
	 * @param inWebURL 
	 * 
	 *
	 */
//	public WebState(String inWebID, String inWebURL){
	public WebState(String inWebID, String inWebURL, String inTitle){
		webId =inWebID;
		webUrl=inWebURL;
		title = inTitle;
		exists =true; //While Creating webs state mark as existing
	}
	/**
	 * Constructor.
	 * @param inKey The webId for this List.
	 * @param inLastMod last-modified date for Web (from SharePoint)
	 */
	/*public WebState(String inKey, DateTime inLastMod){
		if(inKey!=null){
			this.webId = inKey;
			this.webUrl = inKey;
			this.lastMod = inLastMod;
		}
	}*/

	/**
	 * Get the lastMod time.
	 * @return time the Web was last modified
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
		return webId;
	}

	/**
	 * Sets the primary key.
	 * @param key
	 */
	public void setPrimaryKey(String newKey) {
		//primary key cannot be null
		if(newKey!=null){
			this.webId = newKey;
		}
	}

	/**
	 * 
	 */
	public boolean isExisting() {
		return exists;
	}

	/**
	 * 
	 */
	public void setExisting(boolean existing) {
		this.exists = existing;
		if(this.exists == false){
			//	for each ListState, set "not existing" as the Webstate is not existing
			Iterator it = allListStateSet.iterator();
			while(it.hasNext()){
				StatefulObject obj = (StatefulObject) it.next();
				obj.setExisting(false);
			}
		}
	}

	/**
	 * Reload this WebState from a DOM tree.  The opposite of dumpToDOM().
	 * @param Element the DOM element
	 * @throws SharepointException if the DOM tree is not a valid representation
	 *     of a ListState 
	 */
	public void loadFromDOM(Element element) throws SharepointException {
		String sFunctionName = "loadFromDOM(Element element)";
		LOGGER.entering(className, sFunctionName);
		if(element==null){
			throw new SharepointException("element not found");
		}

		NodeList children = element.getChildNodes();
		if(children!=null){
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				//modified by amit
				if ((node==null)||(node.getNodeType() != Node.ELEMENT_NODE)) {
					continue;
				}
				Element childElement = (Element) children.item(i);

				//added by amit
				if(childElement==null){
					continue;
				}

				if (! collator.equals(childElement.getTagName(),ListState.class.getName())) {
					continue; //no exception; ignore xml for things we don't understand
				}
				ListState subObject = new ListState(); 
				subObject.loadFromDOM(childElement);
				updateList(subObject, subObject.getLastMod());

				// now, for "lastCrawledListID", for which so far we've only the key, find the
				// actual object:

				if (lastCrawledListID != null) {
					currentList = (ListState) keyMap.get(lastCrawledListID);
				}
			}
		}

		// get the Web ID
		webId = element.getAttribute(ID);
		if (webId == null || webId.length() == 0) {
			throw new SharepointException("Invalid XML: no id attribute for the WebState");
		}

		/*
		 * last modified not required for WebState
		 * // lastMod
		NodeList lastModNodeList = element.getElementsByTagName(LAST_MOD);
		if ((lastModNodeList==null)||lastModNodeList.getLength() == 0) {
			throw new SharepointException("Invalid XML: no lastMod attribute for the WebState");
		}
		String lastModString = null;
		if(lastModNodeList.item(0) != null && lastModNodeList.item(0).getFirstChild() != null) {
			lastModString = lastModNodeList.item(0).getFirstChild().getNodeValue();
		}
		lastMod = Util.parseDate(lastModString);
		if (lastMod == null) {
			throw new SharepointException("Invalid XML: bad date for the WebState" + lastModString);
		}*/

		// URL
		NodeList urlNodeList = element.getElementsByTagName("URL");
		if((urlNodeList!=null)&& (urlNodeList.getLength() > 0)){
			if(urlNodeList.item(0) != null && urlNodeList.item(0).getFirstChild() != null) {
				webUrl = urlNodeList.item(0).getFirstChild().getNodeValue();
			}
		}

		
		NodeList titleNodeList = element.getElementsByTagName(WEB_TITLE);
		if((titleNodeList!=null)&& (titleNodeList.getLength() > 0)){
			if(titleNodeList.item(0) != null && titleNodeList.item(0).getFirstChild() != null) {
				title = titleNodeList.item(0).getFirstChild().getNodeValue();
			}
		}
		
		//	get insertion time for web state
		NodeList insertTimeNodeList = element.getElementsByTagName(INSERT_TIME);
		String insertTime = null;
		if(insertTimeNodeList.item(0) != null && insertTimeNodeList.item(0).getFirstChild() != null) {
			insertTime = insertTimeNodeList.item(0).getFirstChild().getNodeValue();
		}
		DateTime insertTimeTmp = Util.parseDate(insertTime);
		if(insertTimeTmp==null){
			throw new SharepointException("Unable to get insertion time for web state");
		}
		setInsertionTime(insertTimeTmp);


		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * For a single StatefulObject, update the two
	 * data structures (url -> obj and time -> obj) and mark
	 * it "Existing" (if bFullReCrawl is true).
	 * @param ListState
	 * @param time lastMod time for the List. If time is later than the existing
	 *     lastMod, the List is reindexed in the allListStateSet.
	 */

	public void updateList(ListState state, DateTime time) {
		String sFunctionName="updateList(ListState state, DateTime time)";
		LOGGER.entering(className, sFunctionName);
		//null check added by Amit
		if(state!=null){
			ListState stateOld = (ListState) keyMap.get(state.getPrimaryKey());
			if (stateOld != null) {
				if (stateOld.getLastMod().compareTo(time) != 0) { // if new time differs
					allListStateSet.remove(state);
					state.setLastMod(time);
				}
			} else {
				state.setLastMod(time);
				keyMap.put(state.getPrimaryKey(), state);
			}
		//	if (bFullReCrawl) {
				state.setExisting(true); // remember we saw this one!
		//	}
			allListStateSet.add(state);
		}//end..null check
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * 
	 */
	public Node dumpToDOM(Document domDoc) throws SharepointException {
		String sFunctionName = "dumpToDOM(Document domDoc)";
		LOGGER.entering(className, sFunctionName);
//		Element element = domDoc.createElement(this.getClass().getSimpleName());
		Element element = domDoc.createElement(this.getClass().getName());
		element.setAttribute(ID, getPrimaryKey());

		/*
		 * last modified not required for WebState
		 * // the lastMod
		Element lastModTmp = domDoc.createElement(LAST_MOD);
		Text text = domDoc.createTextNode(getLastModString());
		lastModTmp.appendChild(text);
		element.appendChild(lastModTmp);*/

		// the URL
		Element urlTmp = domDoc.createElement("URL");
		Text urlText = domDoc.createTextNode(getWebUrl());
		urlTmp.appendChild(urlText);
		element.appendChild(urlTmp);
		
//		 the Title
		Element titleTmp = domDoc.createElement(WEB_TITLE);
		Text titleText = domDoc.createTextNode(getTitle());
		urlTmp.appendChild(titleText);
		element.appendChild(titleTmp);

		//	the insertion time
		Element insertTimeTmp = domDoc.createElement(INSERT_TIME);
		Text text = domDoc.createTextNode(getInsertionTimeString());
		insertTimeTmp.appendChild(text);
		element.appendChild(insertTimeTmp);

		if(allListStateSet != null){
//			now dump the actual ListStates:
			Iterator it = allListStateSet.iterator(); 
//			for (StatefulObject obj : dateMap) {
			while(it.hasNext()){
				StatefulObject obj = (StatefulObject) it.next();
				element.appendChild(obj.dumpToDOM(domDoc));
			}
		}
		LOGGER.exiting(className, sFunctionName);
		return element;
	}	


	/**
	 * Compares this WebState to another (for the Comparable interface).
	 * Comparison is first on the insertion date. If that produces a tie, the
	 * primary key (the WebID) is used as tie-breaker.
	 * @param o other WebState.  If null, returns 1.
	 * @return the usual integer result: -1 if this object is less, 1 if it's
	 *     greater, 0 if equal (which should only happen for the identity
	 *     comparison).
	 */
	public int compareTo(Object o) {
		WebState other = (WebState) o;
		if (other == null) {  
			return 1; // anything is greater than null
		}
		if(this.insertionTime != null && other.insertionTime != null){
			int insertComparison = this.insertionTime.compareTo(other.insertionTime);
			if (insertComparison != 0) {
				return insertComparison;
			}
		}
		return this.webId.compareTo(other.webId);
	}

	/**
	 * 
	 * @return
	 */
	public String getWebUrl() {
		return webUrl;
	}

	/**
	 * 
	 * @param inWebUrl
	 */
	public void setWebUrl(String inWebUrl) {
		this.webUrl = inWebUrl;
	}

	/**
	 * 
	 * @return
	 */
	public TreeSet getAllListStateSet() {
		return allListStateSet;
	}

	/**
	 * 
	 * @param inAllListStateSet
	 */
	public void setAllListStateSet(TreeSet inAllListStateSet) {
		this.allListStateSet = inAllListStateSet;
	}

	/**
	 * Factory method for ListState.
	 * @param key the "primary key" of the object. This would
	 * probably be the GUID.
	 * @param lastMod most recent time this object was modified.
	 * @return new ListState which is already indexed in WebState's
	 *     allListStateSet and keyMap
	 */
	public ListState makeListState(String key, DateTime inLastMod) throws SharepointException {
		String sFunName="makeListState(String key, DateTime lastMod)";
		LOGGER.entering(className, sFunName);
		LOGGER.config(sFunName+": List key["+key+"], lastmodified["+inLastMod+"]");
		if(key!=null){
			ListState obj = new ListState(); 
			obj.setLastMod(inLastMod);
			obj.setPrimaryKey(key);
			updateList(obj, inLastMod); // add to our maps
			LOGGER.exiting(className, sFunName);
			return obj;
		}else{
			LOGGER.warning(className+":"+sFunName+": Unable to make ListState due to list key not found");
			throw new SharepointException(sFunName+": Unable to make ListState due to list key not found");
		}
	}

	/**
	 * Convenience factory for clients who don't deal in Joda time.
	 * @param key the "primary key" of the object. This would
	 * probably be the GUID.
	 * @param lastModCal (Calendar, not Joda time)
	 * @return new ListState which is already indexed in WebState's
	 *     allListStateSet and keyMap
	 */
	public ListState makeListState(String key, Calendar lastModCal) throws SharepointException {
		String sFunName="makeListState(String key, Calendar lastModCal)";
		LOGGER.entering(className, sFunName);
		if(key!=null){
			LOGGER.exiting(className, sFunName);
			return makeListState(key, Util.calendarToJoda(lastModCal));
		}else{
			LOGGER.warning(className+":"+sFunName+": Unable to make ListState due to list key not found");
			throw new SharepointException(sFunName+": Unable to make ListState due to list key not found");
		}

	}

	/**
	 * 
	 * @param fullReCrawl
	 *//*
	public void setBFullReCrawl(boolean fullReCrawl) {
		bFullReCrawl = fullReCrawl;
	}
*/
	/**
	 * 
	 *
	 */
	public void cleanLists() {

		for (Iterator iter = getIterator(); iter.hasNext();){
			ListState obj = (ListState) iter.next(); 
			if (!obj.isExisting()) {
				iter.remove(); // we MUST use the iterator's own remove() here
				keyMap.remove(obj.getPrimaryKey());
			}
		}


	}

	/**
	 * 
	 * @return
	 */
	public Iterator getIterator() {
		return allListStateSet.iterator();
	}

	/**
	 * Lookup a ListState by its key.
	 * @param key primary key
	 * @return object handle, or null if none found
	 */
	public ListState lookupList(String key) {
		ListState ls = (ListState) keyMap.get(key);
		if(null!=ls){
			ls.setExisting(true);
		}
		return ls;
	}

	/**
	 * 
	 * @return
	 */
	public Iterator getCircularIterator() {
		ListState start = getCurrentList();
		if (start == null) {
			return getIterator();
		}
		// one might think you could just do tail.addAll(head) here. But you can't.
		ArrayList full = new ArrayList(allListStateSet.tailSet(start));
		full.addAll(allListStateSet.headSet(start));
		return full.iterator();
	}

	/**
	 * 
	 * @return
	 */
	private ListState getCurrentList() {
		return this.currentList;
	}

	/**
	 * 
	 * @param currentObj
	 */
	public void setCurrentList(ListState currentObj) {
		this.currentList = currentObj;
	}

	/**
	 * 
	 * @return
	 */
	public String getLastCrawledListID() {
		if((lastCrawledListID==null) || (lastCrawledListID.trim().equals(""))){
			return null;
		}
		return lastCrawledListID;
	}

	/**
	 * 
	 * @param inLastCrawledListID
	 */
	public void setLastCrawledListID(String inLastCrawledListID) {
		this.lastCrawledListID = inLastCrawledListID;
	}

	/**
	 * 
	 * @return
	 */
	public DateTime getInsertionTime() {
		return insertionTime;
	}

	/**
	 * 
	 * @param inInsertionTime
	 */
	public void setInsertionTime(DateTime inInsertionTime) {
		this.insertionTime = inInsertionTime;
	}

	public String getInsertionTimeString() {
		return Util.formatDate(insertionTime);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
