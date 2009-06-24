//Copyright 2007 Google Inc.

package com.google.enterprise.connector.sharepoint.state;


//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.client.SharepointException;

/**
 * Represents the state of a site. Can be persisted to XML and
 * loaded again from the XML. This would normally be done if the
 * connector wants to be restartable.
 * The state of SharePoint traversal is composed almost entirely of the 
 * state of other classes, which must implement the StatefulObject interface. 
 * As of May 2007, there is only one StatefulObject -- ListState.
 *
 * Classes:
 *     GlobalState.
 * related classes:
 *     StatefulObject (interface)
 *     ListState (implements StatefulObject)
 *
 */
public class GlobalState {
	//private static Log logger = LogFactory.getLog(GlobalState.class);
	private static final Logger LOGGER = Logger.getLogger(GlobalState.class.getName());
	private String className = GlobalState.class.getName();
	private static final String CONNECTOR_NAME = "Sharepoint";
	private static final String CONNECTOR_PREFIX = "_state.xml";
	private static final String LAST_CRAWLED_WEB_ID = "lastCrawledWebStateID";
	private static final String LAST_CRAWLED_LIST_ID = "lastCrawledListStateID";
	private static final String FULL_RECRAWL_FLAG = "FullRecrawlFlag";
	private boolean recrawling = false;
	private String workDir = null;

	/**
	 * To keep track of WebStates, we keep two data structures: a TreeSet relying
	 * on the insertion time property of a StatefulObject, and a HashMap on the
	 * primary key of the object (id for Webs).  The StatefulObject interface
	 * is often used to reduce our dependency on peculiarities of WebState. (At
	 * one time, it was thought that there would be other instances of 
	 * StatefulObject, and in the future there could be again)
	 */
	protected SortedSet dateMap = new TreeSet();
	protected Map keyMap = 
		new HashMap();

	/**
	 * The "currentWeb" object for WebState. The current object may be null.
	 */
	protected WebState currentWeb = null;

	private String lastCrawledWebID = null;
	private String lastCrawledListID = null;
	private boolean bFullReCrawl = false;

	/**
	 * Delete our state file. This is for debugging purposes, so that unit
	 * tests can start from a clean state.
	 * @param workDir the googleConnectorWorkDir argument to the constructor
	 */
	public static void forgetState(String workDir) {
		String sFunctionName = "forgetState(String workDir)";
		LOGGER.entering(GlobalState.class.getName(), sFunctionName);
		File f;
		if (workDir == null) {
			LOGGER.info("No working directory was given; using cwd");
			f = new File(CONNECTOR_NAME + CONNECTOR_PREFIX);
		} else {
			LOGGER.info("Work Dir: "+workDir);
			
			f = new File(workDir, CONNECTOR_NAME + CONNECTOR_PREFIX);
		}
		LOGGER.info("deleting state file from location...."+f.getAbsolutePath());
		if (f.exists()) {
			
			boolean isDeleted = f.delete();
			LOGGER.info("deleted status :"+isDeleted);
			
			/*if(!isDeleted){
				//Create an empty file
				try {
					f.createNewFile();
				} catch (IOException e) {
					LOGGER.log(Level.WARNING,"Unable to erase file..",e);
				}
			}*/
			
			
		}
		LOGGER.exiting(GlobalState.class.getName(), sFunctionName);
	}

	/**
	 * Constructor. 
	 * @param inWorkDir the googleConnectorWorkDir (which we ask for
	 *     in connectorInstance.xml).  The state file is saved in this
	 *     directory.  If workDir is null, the current working directory is
	 *     used instead.  (In either case, the location of the file is
	 *     saved in the system preferences, so that environmental changes don't
	 *     make us lose the file.)
	 */
	public GlobalState(String inWorkDir){
		String sFunctionName = "GlobalState(String inWorkDir)";
		LOGGER.entering(className, sFunctionName);
		if(inWorkDir!=null){
			this.workDir = inWorkDir;
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * Factory method for WebState.
	 * @param key the "primary key" of the object. This would
	 * probably be the WebID.
	 * @param lastMod most recent time this object was modified(not required).
	 * @return new WebState which is already indexed in GlobalState's
	 *     dateMap and keyMap
	 */
	public WebState makeWebState(String key, DateTime lastMod, String title) throws SharepointException { // updated to take one more arguement: title. By Nitendra
		String sFunName="makeWebState(String key, DateTime lastMod)";
		LOGGER.entering(className, sFunName);
		LOGGER.config(sFunName+": List key["+key+"], lastmodified["+lastMod+"], title["+title+"]");
		if(key!=null){
			WebState obj = new WebState(); 
			DateTime dt = new DateTime();
			obj.setInsertionTime(dt);
			obj.setLastMod(lastMod);
			obj.setPrimaryKey(key);
			obj.setWebUrl(key);
			obj.setTitle(title); // added by Nitendra
			updateList(obj, lastMod); // add to our maps
			LOGGER.exiting(className, sFunName);
			return obj;
		}else{
			LOGGER.warning(className+":"+sFunName+": Unable to make WebState due to list key not found");
			throw new SharepointException(sFunName+": Unable to make WebState due to list key not found");
		}
	}

	/**
	 * Convenience factory for clients who don't deal in Joda time.
	 * @param key the "primary key" of the object. This would
	 * probably be the WebID.
	 * @param lastModCal (Calendar, not Joda time)
	 * @return new WebState which is already indexed in GlobalState's
	 *     dateMap and keyMap
	 */
	public WebState makeWebState(String key, Calendar lastModCal, String title) throws SharepointException { // updated to take one more arguement: title. By Nitendra
		String sFunName="makeListState(String key, Calendar lastModCal)";
		LOGGER.entering(className, sFunName);
		if(key!=null){
			LOGGER.exiting(className, sFunName);
			return makeWebState(key, (DateTime)null/*Util.calendarToJoda(lastModCal)*/,title); //title arguement added by Nitendra
		}else{
			LOGGER.warning(className+":"+sFunName+": Unable to make WebState due to list key not found");
			throw new SharepointException(sFunName+": Unable to make WebState due to list key not found");
		}

	}
	
	/**
	 * Signal that a complete "recrawl" cycle is beginning, where all lists
	 * are being fetched from SharePoint.  This GlobalState will
	 * keep track of which objects are still present.  At the endRecrawl()
	 * call, objects no longer existing may be removed.
	 */
	public void startRecrawl() {
		String sFunName="startRecrawl()";
		LOGGER.entering(className, sFunName);
		recrawling = true;

		if(bFullReCrawl == true){
			LOGGER.config(className +":"+sFunName+":"+"start of full recrawl ... setting all web states is isExist flag to false for clean up purpose");
			// mark all webs for deletion	
			// for each ListState, set "not existing"
			Iterator it = dateMap.iterator();
//			for (StatefulObject obj : dateMap) {
			while(it.hasNext()){
				StatefulObject obj = (StatefulObject) it.next();
				obj.setExisting(false);
			}
		}
		LOGGER.exiting(className, sFunName);
	}

	/**
	 * Signals that the recrawl cycle is over, and GlobalState may now
	 * delete any ListState which did not appear in a 
	 * updateList() call since the startRecrawl() call.
	 */
	public void endRecrawl() {
		String sFunName="endRecrawl()";
		LOGGER.entering(className, sFunName);
		if (!recrawling) {
			LOGGER.severe("called endRecrawl() when not in a recrawl state");
			return;
		}

		if(bFullReCrawl == true){
			LOGGER.config(className +":"+sFunName+":"+"end of full recrawl ...bFullReCrawl true ... cleaning up WebStates");
			// 'foreach' not used here, since we need the iterator for remove()
			for (Iterator iter = getIterator(); iter.hasNext();){
				WebState obj = (WebState) iter.next(); 
				if (!obj.isExisting()) {
					iter.remove(); // we MUST use the iterator's own remove() here
					keyMap.remove(obj.getPrimaryKey());
				}else{
					obj.cleanLists();
				}
			}
	//		bFullReCrawl = false;
		}
		recrawling = false;
		LOGGER.exiting(className, sFunName);
	}

	/**
	 * Get an iterator which returns the objects in increasing order of their
	 * lastModified dates.
	 * @return Iterator on the objects by lastModified time
	 */
	public Iterator getIterator() {
		return dateMap.iterator();
	}

	/**
	 * Get dateMap iterator beginning at the current ListState and wrapping 
	 * around to finish just before the current.  If there is no current,
	 * you just get an ordinary iterator.
	 * @return Iterator which begins at getCurrentList() and wraps around the end
	 */

	public Iterator getCircularIterator() {
		WebState start = getCurrentWeb();
		if (start == null) {
			return getIterator();
		}
		// one might think you could just do tail.addAll(head) here. But you can't.
		ArrayList full = new ArrayList(dateMap.tailSet(start));
		full.addAll(dateMap.headSet(start));
		return full.iterator();
	}

	/**
	 * Lookup a ListState by its key.
	 * @param key primary key
	 * @return object handle, or null if none found
	 */
	public WebState lookupList(String key) {
		return lookupList(key,true);
	}
	
	public WebState lookupList(String key,boolean bvalue) {
		WebState ws =(WebState) keyMap.get(key); 
		if(null!=ws){
			ws.setExisting(bvalue);
		}
		return ws;
	}

	/**
	 * Return an XML string representing the current state. Since our state
	 * is comprised almost entirely of the state of the StatefulObjects
	 * (i.e. ListState), this is largely done by calling an
	 * analogous method on those classes. Here is an example (note that
	 * the only state that belongs to GlobalState itself if the "current"
	 * object for each dependent class).  Consult ListState for details on
	 * its XML representation.
	 * (we have to use &lt; and &gt; here so the javadocs will come out right.)
<pre>
        &lt;?xml version="1.0" encoding="UTF-8"?&gt;
        &lt;state&gt;
          &lt;current id="foo" type="ListState"/&gt;
          &lt;ListState id="bar"&gt;
            &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
            &lt;URL/&gt;
           &lt;lastDocCrawled&gt;
              &lt;document id="id2"&gt;
                &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
                &lt;url&gt;url2&lt;/url&gt;
              &lt;/document&gt;
            &lt;/lastDocCrawled&gt;
          &lt;/ListState&gt;
          &lt;ListState id="foo"&gt;
            &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
            &lt;URL/&gt;
            &lt;lastDocCrawled&gt;
              &lt;document id="id1"&gt;
                &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
                &lt;url&gt;url1&lt;/url&gt;
             &lt;/document&gt;
            &lt;/lastDocCrawled&gt;
            &lt;crawlQueue&gt;
              &lt;document id="id3"&gt;
                &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
                &lt;url&gt;url3&lt;/url&gt;
              &lt;/document&gt;
              &lt;document id="id4"&gt;
               &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
                &lt;url&gt;url4&lt;/url&gt;
              &lt;/document&gt;
            &lt;/crawlQueue&gt;
          &lt;/ListState&gt;
        &lt;/state&gt;
</pre>
	 * @return XML string
	 */
	public String getStateXML() throws SharepointException {
		String sFunName="getStateXML()";
		LOGGER.entering(className, sFunName);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
		} catch (ParserConfigurationException e) {
			LOGGER.warning(className+":"+sFunName+":"+e.toString());
			throw new SharepointException("Unable to get state XML");
		}
		Element top = doc.createElement("state");
		doc.appendChild(top);

		// FULL_RECRAWL_FLAG
		Element elementFlag = doc.createElement(FULL_RECRAWL_FLAG);
		elementFlag.setAttribute("id", bFullReCrawl+"");
		top.appendChild(elementFlag);

		// LAST_CRAWLED_WEB_ID
		Element element1 = doc.createElement(LAST_CRAWLED_WEB_ID);
		element1.setAttribute("id", lastCrawledWebID);
		top.appendChild(element1);
		

		// LAST_CRAWLED_LIST_ID
		Element element2 = doc.createElement(LAST_CRAWLED_LIST_ID);
		element2.setAttribute("id", lastCrawledListID);
		top.appendChild(element2);

		// now dump the actual WebStates:
		Iterator it = dateMap.iterator(); 
//		for (StatefulObject obj : dateMap) {
		while(it.hasNext()){
			StatefulObject obj = (StatefulObject) it.next();
			top.appendChild(obj.dumpToDOM(doc));
		}
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = null;
		try {
			t = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			LOGGER.warning(className+":"+sFunName+":"+e.toString());
			throw new SharepointException("Unable to get state XML");
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
			LOGGER.warning(className+":"+sFunName+":"+e.toString());
			throw new SharepointException("Unable to get state XML");
		}
		LOGGER.exiting(className, sFunName);
		return os.toString();
	}

	/**
	 * Creates an XML representation of our state, and saves it, using the
	 * PrefsStore mechanism.
	 * @throws SharepointException
	 */
	public void saveState()  throws SharepointException {
		String sFunName="saveState()";
		LOGGER.entering(className, sFunName);
		try{
			String xml = getStateXML();
			File f = getStateFileLocation();

			FileOutputStream out = new FileOutputStream(f);
			out.write(xml.getBytes());
			out.close();
			LOGGER.info("saving state to " + f.getCanonicalPath());
		} catch (IOException e) {
			LOGGER.warning(className+":"+sFunName+":"+e.toString());
			throw new SharepointException("Save state failed");

		}catch(Throwable e){
			LOGGER.warning(className+":"+sFunName+":"+e.toString());
			throw new SharepointException("Save state failed");
		}
		LOGGER.exiting(className, sFunName);
	}

	/**
	 * Load from XML.
	 * @param persisted - file name for the state file, which has already been
	 *     checked as to its existence.
	 */
	private void loadStateXML(File fileState) throws SharepointException {
		String sFunctionName="loadStateXML(File fileState)";
		LOGGER.entering(className, sFunctionName);
		if(fileState==null){
			throw new SharepointException(sFunctionName+": Unable to get the file containing the state info");
		}
		Collator collator = SharepointConnectorType.getCollator();
		BufferedReader in = null;
		ByteArrayInputStream b = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			//----------------start: UTF-16 problem ----------------
			//problem: UTF-16 problem: Invalid byte 2 of 3-byte UTF-8 sequence
			//help link: http://forum.java.sun.com/thread.jspa?threadID=508676&messageID=2414565
			in = new BufferedReader(new FileReader(fileState));
			StringBuffer str = new StringBuffer();
			String str1;
			while ((str1 = in.readLine()) != null) {
				str.append(str1);
				str.append("\n");//easy to see when printed
			}

			str1 = str.toString();
			b =  new java.io.ByteArrayInputStream(str1.getBytes("UTF8"));
			//----------------end: UTF-16 problem ----------------

//			org.w3c.dom.Document doc = builder.parse(fileState);//this need to be changed
			org.w3c.dom.Document doc = builder.parse(b);//this need to be changed
			if(doc==null){
				throw new SharepointException("Unable to det doc");
			}
			NodeList nodeList = doc.getElementsByTagName("state");
			if ((nodeList==null) || (nodeList.getLength() == 0)) {
				throw new SharepointException("Invalid XML: no <state> element");
			}
			// temporary list of the "current" objects (just their keys):
			//		String currentKeyTmp = null;

			//added by Amit
			if(nodeList.item(0)==null){
				throw new SharepointException("Unable to get the item of the nodelist");
			}

			// get bFullRecrawl flag
			NodeList flagListFullRecrawl = ((Element)nodeList.item(0)).getElementsByTagName(FULL_RECRAWL_FLAG);

			if(flagListFullRecrawl == null || flagListFullRecrawl.getLength() == 0){
				throw new SharepointException("Unable to get lastCrawledWebStateList from nodelist");
			}

		//	if(flagListFullRecrawl.item(0)==null || flagListFullRecrawl.item(0).getFirstChild() == null){
			if(flagListFullRecrawl.item(0)==null){
				throw new SharepointException("Unable to get the lastCrawledWebStateList item of the nodelist");
			}

			Element el  = (Element)flagListFullRecrawl.item(0);
			
			if(el == null){
				throw new SharepointException("Unable to get the lastCrawledWebStateList element");
			}
			String strFlagFullRecrawl = el.getAttribute("id");
			
			
			if(strFlagFullRecrawl!= null){
				if(strFlagFullRecrawl.equalsIgnoreCase("true")){
					bFullReCrawl = true;
				}else{
					bFullReCrawl = false;
				}
			}
			el = null;
			
			// get 	LAST_CRAWLED_WEB_ID		
			NodeList lastCrawledWebStateList = ((Element)nodeList.item(0)).getElementsByTagName(LAST_CRAWLED_WEB_ID);

			if(lastCrawledWebStateList == null || lastCrawledWebStateList.getLength() == 0){
				throw new SharepointException("Unable to get lastCrawledWebStateList from nodelist");
			}

		//	if(lastCrawledWebStateList.item(0)==null || lastCrawledWebStateList.item(0).getFirstChild() != null){
			if(lastCrawledWebStateList.item(0)==null){
				throw new SharepointException("Unable to get the lastCrawledWebStateList item of the nodelist");
			}

			el  = (Element)lastCrawledWebStateList.item(0);
			
			if(el == null){
				throw new SharepointException("Unable to get the lastCrawledWebStateList item of element");
			}
			lastCrawledWebID = el.getAttribute("id");
			el = null;
			
			//get 	LAST_CRAWLED_LIST_ID		
			NodeList lastCrawledListStateList = ((Element)nodeList.item(0)).getElementsByTagName(LAST_CRAWLED_LIST_ID);

			if(lastCrawledListStateList == null || lastCrawledListStateList.getLength() == 0){
				throw new SharepointException("Unable to get lastCrawledListStateList from nodelist");
			}

//			if(lastCrawledListStateList.item(0)==null || lastCrawledListStateList.item(0).getFirstChild() != null){
			if(lastCrawledListStateList.item(0)==null){
				throw new SharepointException("Unable to get the lastCrawledListStateList item of the nodelist");
			}

			el  = (Element)lastCrawledListStateList.item(0);
			
			if(el == null){
				throw new SharepointException("Unable to get the lastCrawledListStateList item of element");
			}
			lastCrawledListID = el.getAttribute("id");
			
		//	lastCrawledListID = lastCrawledListStateList.item(0).getNodeValue();

			NodeList children = nodeList.item(0).getChildNodes();
			if(children!=null){
				for (int i = 0; i < children.getLength(); i++) {
					Node node = children.item(i);
					//modified by amit
					if ((node==null)||(node.getNodeType() != Node.ELEMENT_NODE)) {
						continue;
					}
					Element element = (Element) children.item(i);

					//added by amit
					if(element==null){
						continue;
					}

					if (! collator.equals(element.getTagName(),WebState.class.getName())) {
						continue; //no exception; ignore xml for things we don't understand
					}
					WebState subObject = new WebState(); 
					subObject.setLastCrawledListID(lastCrawledListID);
					subObject.loadFromDOM(element);
					updateList(subObject, subObject.getLastMod());

					// now, for "currentWeb", for which so far we've only the key, find the
					// actual object:
					if (lastCrawledWebID != null) {
						currentWeb = (WebState) keyMap.get(lastCrawledWebID);
					}
				}
			}//null check for children
		}/* catch (IOException e) {
			LOGGER.log(className+":"+sFunctionName+":Actual Exception",e);
			throw new SharepointException("Unable to load state XML file");
		} catch (ParserConfigurationException e) {
			LOGGER.severe(className+":"+sFunctionName+":"+e.toString());
			throw new SharepointException("Unable to load state XML file");
		} catch (SAXException e) {
			LOGGER.severe(className+":"+sFunctionName+":"+e.toString());
			throw new SharepointException("Unable to load state XML file");
		}*/catch(Throwable e){
			LOGGER.log(Level.SEVERE, className+":"+sFunctionName+":Actual Exception\n",e);
			throw new SharepointException("Unable to load state XML file");
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, className+":"+sFunctionName+":Actual Exception\n",e);
					throw new SharepointException("Unable to load state XML file");
				}
			}
			if (null != b) {
				try {
					b.close();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, className+":"+sFunctionName+":Actual Exception\n",e);
					throw new SharepointException("Unable to load state XML file");
				}
			}
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * Load persistent state from our XML state.
	 * @throws SharepointException if the XML file can't be found, or is
	 *     invalid in any way.
	 */
	public void loadState() throws SharepointException {
		String sFunctionName="loadState()";
		LOGGER.entering(className, sFunctionName);
		File f = getStateFileLocation();
		try {
			if (!f.exists()) {
				LOGGER.warning("state file '" + f.getCanonicalPath()
						+ "' does not exist");//amit changed this!!
				return;
			}
			LOGGER.info(className+":"+sFunctionName+": loading state from " + f.getCanonicalPath());
			loadStateXML(f);
		} catch (IOException e) {
			LOGGER.severe(className+":"+sFunctionName+":"+e.getMessage());
			throw new SharepointException(e.getMessage());
		}/*finally{
			f = null;
		}*/
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * Set the given List as "current"
	 * This will be remembered in the XML state.
	 * @param inCurrentWeb ListState
	 */
	public void setCurrentWeb(WebState inCurrentWeb) {
		currentWeb = inCurrentWeb;
	}

	/**
	 * Get the current ListState.
	 * @return the current object, or null if none
	 */
	public WebState getCurrentWeb() {
		return currentWeb;
	}

	/**
	 * For a single StatefulObject, update the two
	 * data structures (url -> obj and time -> obj) and mark
	 * it "Existing" (if between startRecrawl() and endRecrawl()).
	 * @param WebState
	 * @param time lastMod time for the Web (not required now). 
	 */
	public void updateList(WebState state, DateTime time) {
		String sFunctionName="updateList(WebState state, DateTime time)";
		LOGGER.entering(className, sFunctionName);
		//null check added by Amit
		if(state!=null){
			// update the key map
			state.setLastMod(time);
			keyMap.put(state.getPrimaryKey(), state);

			//if (recrawling) {
//			if(bFullReCrawl){
//					state.setBFullReCrawl(bFullReCrawl);
				state.setExisting(true); // remember we saw this one!
//			}
			dateMap.add(state);
		}//end..null check
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * Return the location for our state file. If we were given a 
	 * googleConnectorWorkDir (the expected case), use that; else use the
	 * current working directory and log an error.
	 * @return File
	 */
	private File getStateFileLocation() {
		File f;
		if (workDir == null) {
			LOGGER.warning("No working directory was given; using cwd");
			f = new File(CONNECTOR_NAME + CONNECTOR_PREFIX);
		} else {
			f = new File(workDir, CONNECTOR_NAME + CONNECTOR_PREFIX);
		}
		return f;
	}

	/**
	 * 
	 * @return
	 */
	public SortedSet getAllWebStateSet() {
		return dateMap;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLastCrawledListID() {
		return lastCrawledListID;
	}

	/**
	 * 
	 * @param inLastCrawledListState
	 */
	public void setLastCrawledListID(String inLastCrawledListState) {
		this.lastCrawledListID = inLastCrawledListState;
	}

	/**
	 * 
	 * @return
	 */
	public String getLastCrawledWebID() {
		if((lastCrawledWebID==null) || (lastCrawledWebID.trim().equals(""))){
			return null;
		}
		
		return lastCrawledWebID;
	}

	/**
	 * 
	 * @param inLastCrawledWebID
	 */
	public void setLastCrawledWebID(String inLastCrawledWebID) {
		this.lastCrawledWebID = inLastCrawledWebID;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isBFullReCrawl() {
		return bFullReCrawl;
	}

	/**
	 * 
	 * @param fullReCrawl
	 */
	public void setBFullReCrawl(boolean fullReCrawl) {
		bFullReCrawl = fullReCrawl;
	}


}
