//Copyright 2007 Google Inc.
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.state;

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

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.WebsWS;

/**
 * Represents the state of a site. Can be persisted to XML and loaded again from
 * the XML. This would normally be done if the connector wants to be
 * restartable. The state of SharePoint traversal is composed almost entirely of
 * the state of other classes, which must implement the StatefulObject
 * interface. As of May 2007, there is only one StatefulObject -- ListState.
 * Classes: GlobalState. related classes: StatefulObject (interface) ListState
 * (implements StatefulObject)
 */
public class GlobalState {
	private static final Logger LOGGER = Logger.getLogger(GlobalState.class.getName());
	private boolean recrawling = false;
	private String workDir = null;
	private String feedType;
	/**
	 * To keep track of WebStates, we keep two data structures: a TreeSet
	 * relying on the insertion time property of a StatefulObject, and a HashMap
	 * on the primary key of the object (id for Webs). The StatefulObject
	 * interface is often used to reduce our dependency on peculiarities of
	 * WebState. (At one time, it was thought that there would be other
	 * instances of StatefulObject, and in the future there could be again)
	 */
	protected SortedSet<WebState> dateMap = new TreeSet<WebState>();
	protected Map<String, WebState> keyMap = new HashMap<String, WebState>();

	/**
	 * The "currentWeb" object for WebState. The current object may be null.
	 */
	protected WebState currentWeb = null;

	private String lastCrawledWebID = null;
	private String lastCrawledListID = null;
	private boolean bFullReCrawl = false;
	private String lastFullCrawlDateTime = null;

	/**
	 * Delete our state file. This is for debugging purposes, so that unit tests
	 * can start from a clean state.
	 * 
	 * @param workDir the googleConnectorWorkDir argument to the constructor
	 */
	public static void forgetState(final String workDir) {
		File f1;
		if (workDir == null) {
			LOGGER.info("No working directory was given; using cwd");
			f1 = new File(SPConstants.CONNECTOR_NAME
					+ SPConstants.CONNECTOR_PREFIX);
		} else {
			LOGGER.info("Work Dir: " + workDir);
			f1 = new File(workDir, SPConstants.CONNECTOR_NAME
					+ SPConstants.CONNECTOR_PREFIX);
		}
		LOGGER.info("deleting state file from location...."
				+ f1.getAbsolutePath());
		if (f1.exists()) {
			final boolean isDeleted = f1.delete();
			LOGGER.info("deleted status :" + isDeleted);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param inWorkDir the googleConnectorWorkDir (which we ask for in
	 *            connectorInstance.xml). The state file is saved in this
	 *            directory. If workDir is null, the current working directory
	 *            is used instead. (In either case, the location of the file is
	 *            saved in the system preferences, so that environmental changes
	 *            don't make us lose the file.)
	 */
	public GlobalState(final String inWorkDir, final String inFeedType) {
		if (inWorkDir != null) {
			workDir = inWorkDir;
		}
		feedType = inFeedType;
	}

	/**
	 * Factory method for WebState.
	 * 
	 * @param spContext The connector context being used
	 * @param key the "primary key" of the object. This would probably be the
	 *            WebID
	 * @return new {@link WebState} which is already indexed in GlobalState's
	 *         dateMap and keyMap
	 */
	public WebState makeWebState(final SharepointClientContext spContext,
			final String key) throws SharepointException {
		if (key != null) {
			final WebState obj = new WebState(spContext, key);
			final DateTime dt = new DateTime();
			obj.setInsertionTime(dt);
			updateList(obj);
			return obj;
		} else {
			LOGGER.warning("Unable to make WebState because list key is not found");
			return null;
		}
	}

	/**
	 * Signal that a complete "recrawl" cycle is beginning, where all lists are
	 * being fetched from SharePoint. This GlobalState will keep track of which
	 * objects are still present. At the endRecrawl() call, objects no longer
	 * existing may be removed.
	 */
	public void startRecrawl() {
		recrawling = true;
		if (bFullReCrawl == true) {
			LOGGER.config("Recrawling... setting all web states is isExist flag to false for clean up purpose");
			// mark all as non-existent
			final Iterator it = dateMap.iterator();
			while (it.hasNext()) {
				final WebState webs = (WebState) it.next();
				webs.setExisting(false);
			}
		}
	}

	/**
	 * Signals that the recrawl cycle is over, and GlobalState may now delete
	 * any ListState which did not appear in a updateList() call since the
	 * startRecrawl() call.
	 */
	public void endRecrawl(final SharepointClientContext spContext) {
		if (!recrawling) {
			LOGGER.severe("called endRecrawl() when not in a recrawl state");
			return;
		}

		if (bFullReCrawl == true) {
			LOGGER.config("ending recrawl ...bFullReCrawl true ... cleaning up WebStates");
			final Iterator iter = getIterator();
			if (null != iter) {
				while (iter.hasNext()) {
					final WebState webs = (WebState) iter.next();
					webs.endRecrawl(spContext);
					if (!webs.isExisting()) {
						// Case of web deletion
						// Delete this web State only if does not contain any
						// list State info.
						if (webs.getAllListStateSet().size() == 0) {
							if (SPConstants.CONTENT_FEED.equalsIgnoreCase(spContext.getFeedType())) {
								int responseCode = 0;
								try {
									responseCode = spContext.checkConnectivity(Util.encodeURL(webs.getWebUrl()), null);
								} catch (final Exception e) {
									LOGGER.log(Level.WARNING, "Connectivity failed! ", e);
								}
								if (responseCode == 200) {
									webs.setExisting(true);
									continue;
								} else if (responseCode != 404) {
									continue;
								}
							}
							LOGGER.log(Level.INFO, "Deleting the state information for web ["
									+ webs.getWebUrl() + "]. ");
							iter.remove();
							keyMap.remove(webs.getPrimaryKey());
						}
					}
				}
			}
		}
		recrawling = false;
	}

	/**
	 * @param inWebs
	 */
	public void removeWebStateFromKeyMap(final WebState inWebs) {
		keyMap.remove(inWebs.getPrimaryKey());
	}

	/**
	 * Get an iterator which returns the objects in increasing order of their
	 * lastModified dates.
	 * 
	 * @return Iterator on the objects by lastModified time
	 */
	public Iterator getIterator() {
		return dateMap.iterator();
	}

	/**
	 * Get dateMap iterator beginning at the current ListState and wrapping
	 * around to finish just before the current. If there is no current, you
	 * just get an ordinary iterator.
	 * 
	 * @return Iterator which begins at getCurrentList() and wraps around the
	 *         end
	 */

	public Iterator getCircularIterator() {
		final WebState start = getCurrentWeb();
		if (start == null) {
			return getIterator();
		}
		// one might think you could just do tail.addAll(head) here. But you
		// can't.
		final ArrayList<WebState> full = new ArrayList<WebState>(
				dateMap.tailSet(start));
		full.addAll(dateMap.headSet(start));
		return full.iterator();
	}

	/**
	 * Lookup a ListState by its key.
	 * 
	 * @param webid web state in which the list is to be searched
	 * @param listid the list GUID to be searched
	 * @return object handle, or null if none found
	 */
	public ListState lookupList(final String webid, final String listid) {
		final WebState ws = keyMap.get(webid);
		if (null != ws) {
			final ListState ls = ws.lookupList(listid);
			if (null != ls) {
				return ls;
			}
		}
		return null;
	}

	/**
	 * Lookup a WebState by its key.
	 * 
	 * @param key primary key
	 * @return object handle, or null if none found
	 */
	public WebState lookupWeb(final String key,
			final SharepointClientContext sharepointClientContext) {
		WebState ws = keyMap.get(key);
		if (null == sharepointClientContext) {
			return ws;
		}

		/*
		 * The incoming url might not always be exactly the web URL that is used
		 * while creation of web state and is required by Web Services as such.
		 * Hence, a second check is required.
		 */
		final SharepointClientContext spContext = (SharepointClientContext) sharepointClientContext.clone();
		if (null == ws) {
			final String webAppURL = Util.getWebApp(key);
			WebsWS websWS = null;
			try {
				spContext.setSiteURL(webAppURL);
				websWS = new WebsWS(spContext);
			} catch (final Exception e) {
				LOGGER.log(Level.WARNING, "webWS creation failed for URL [ "
						+ key + " ]. ", e);
			}
			if (null != websWS) {
				final String tmpKey = websWS.getWebURLFromPageURL(key);
				if (!key.equals(tmpKey)) {
					ws = keyMap.get(tmpKey);
					;
				}
			}
		}
		return ws;
	}

	/**
	 * Return an XML string representing the current state. Since our state is
	 * comprised almost entirely of the state of the StatefulObjects (i.e.
	 * ListState), this is largely done by calling an analogous method on those
	 * classes. Here is an example (note that the only state that belongs to
	 * GlobalState itself if the "current" object for each dependent class).
	 * Consult ListState for details on its XML representation. (we have to use
	 * &lt; and &gt; here so the javadocs will come out right.)
	 * 
	 * <pre>
	 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
	 *         &lt;state&gt;
	 *           &lt;current id=&quot;foo&quot; type=&quot;ListState&quot;/&gt;
	 *           &lt;ListState id=&quot;bar&quot;&gt;
	 *             &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
	 *             &lt;URL/&gt;
	 *            &lt;lastDocCrawled&gt;
	 *               &lt;document id=&quot;id2&quot;&gt;
	 *                 &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
	 *                 &lt;url&gt;url2&lt;/url&gt;
	 *               &lt;/document&gt;
	 *             &lt;/lastDocCrawled&gt;
	 *           &lt;/ListState&gt;
	 *           &lt;ListState id=&quot;foo&quot;&gt;
	 *             &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
	 *             &lt;URL/&gt;
	 *             &lt;lastDocCrawled&gt;
	 *               &lt;document id=&quot;id1&quot;&gt;
	 *                 &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
	 *                 &lt;url&gt;url1&lt;/url&gt;
	 *              &lt;/document&gt;
	 *             &lt;/lastDocCrawled&gt;
	 *             &lt;crawlQueue&gt;
	 *               &lt;document id=&quot;id3&quot;&gt;
	 *                 &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
	 *                 &lt;url&gt;url3&lt;/url&gt;
	 *               &lt;/document&gt;
	 *               &lt;document id=&quot;id4&quot;&gt;
	 *                &lt;lastMod&gt;20070420T154348.133-0700&lt;/lastMod&gt;
	 *                 &lt;url&gt;url4&lt;/url&gt;
	 *               &lt;/document&gt;
	 *             &lt;/crawlQueue&gt;
	 *           &lt;/ListState&gt;
	 *         &lt;/state&gt;
	 * </pre>
	 * 
	 * @return XML string
	 */
	public String getStateXML() throws SharepointException {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc;
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
		} catch (final ParserConfigurationException e) {
			LOGGER.log(Level.WARNING, "Unable to get state XML", e);
			throw new SharepointException("Unable to get state XML");
		}
		final Element top = doc.createElement(SPConstants.STATE);
		doc.appendChild(top);

		// Feed Type used
		final Element elementFeedType = doc.createElement(SPConstants.STATE_FEEDTYPE);
		elementFeedType.setAttribute(SPConstants.STATE_TYPE, feedType);
		top.appendChild(elementFeedType);

		// FULL_RECRAWL_FLAG
		final Element elementFlag = doc.createElement(SPConstants.FULL_RECRAWL_FLAG);
		elementFlag.setAttribute(SPConstants.STATE_ID, bFullReCrawl + "");

		// This check is for the first crawl cycle when there is no value set in
		// the state file. Don't set the value in that case. Blank value does
		// not make any sense.
		if (lastFullCrawlDateTime != null) {
			// Log the date and time
			elementFlag.setAttribute(SPConstants.LAST_FULL_CRAWL_DATETIME, lastFullCrawlDateTime);
		}

		top.appendChild(elementFlag);

		// LAST_CRAWLED_WEB_ID
		final Element element1 = doc.createElement(SPConstants.LAST_CRAWLED_WEB_ID);
		element1.setAttribute(SPConstants.STATE_ID, lastCrawledWebID);
		top.appendChild(element1);

		// LAST_CRAWLED_LIST_ID
		final Element element2 = doc.createElement(SPConstants.LAST_CRAWLED_LIST_ID);
		element2.setAttribute(SPConstants.STATE_ID, lastCrawledListID);
		top.appendChild(element2);

		// now dump the actual WebStates:
		final Iterator it = dateMap.iterator();
		// for (StatefulObject obj : dateMap) {
		while (it.hasNext()) {
			final StatefulObject obj = (StatefulObject) it.next();
			top.appendChild(obj.dumpToDOM(doc));
		}
		final TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = null;
		try {
			t = tf.newTransformer();
		} catch (final TransformerConfigurationException e) {
			LOGGER.log(Level.WARNING, "Unable to load state XML", e);
			throw new SharepointException("Unable to get state XML");
		}
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		final DOMSource doms = new DOMSource(doc);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final StreamResult sr = new StreamResult(os);
		try {
			t.transform(doms, sr);
		} catch (final TransformerException e) {
			LOGGER.log(Level.WARNING, "Unable to load state XML", e);
			throw new SharepointException("Unable to get state XML");
		}
		return os.toString();
	}

	/**
	 * Creates an XML representation of our state, and saves it, using the
	 * PrefsStore mechanism.
	 * 
	 * @throws SharepointException
	 */
	public void saveState() throws SharepointException {
		try {
			final String xml = getStateXML();
			final File f = getStateFileLocation();

			final FileOutputStream out = new FileOutputStream(f);
			out.write(xml.getBytes());
			out.close();
			LOGGER.log(Level.INFO, "saving state to " + f.getCanonicalPath());
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "Save State Failed", e);
			throw new SharepointException("Save state failed");

		} catch (final Throwable e) {
			LOGGER.log(Level.WARNING, "Save State Failed", e);
			throw new SharepointException("Save state failed");
		}
	}

	/**
	 * Load from XML.
	 * 
	 * @param fileState - file name for the state file, which has already been
	 *            checked as to its existence.
	 */
	private void loadStateXML(final File fileState) throws SharepointException {
		if (fileState == null) {
			throw new SharepointException(
					"Unable to get the file containing the state info");
		}
		final Collator collator = Util.getCollator();
		BufferedReader in = null;
		ByteArrayInputStream b = null;
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();

			// ----------------start: UTF-16 problem ----------------
			// problem: UTF-16 problem: Invalid byte 2 of 3-byte UTF-8 sequence
			// help link:
			// http://forum.java.sun.com/thread.jspa?threadID=508676&messageID=2414565
			in = new BufferedReader(new FileReader(fileState));
			final StringBuffer str = new StringBuffer();
			String str1;
			while ((str1 = in.readLine()) != null) {
				str.append(str1);
				str.append("\n");// easy to see when printed
			}

			str1 = str.toString();
			b = new java.io.ByteArrayInputStream(str1.getBytes("UTF8"));
			// ----------------end: UTF-16 problem ----------------

			final org.w3c.dom.Document doc = builder.parse(b);// this need to be
			// changed
			if (doc == null) {
				throw new SharepointException("Unable to get doc");
			}
			final NodeList nodeList = doc.getElementsByTagName(SPConstants.STATE);
			if ((nodeList == null) || (nodeList.getLength() == 0)) {
				throw new SharepointException("Invalid XML: no <state> element");
			}

			// Feed Type
			if (nodeList.item(0) == null) {
				throw new SharepointException(
						"Unable to get the item of the nodelist");
			}

			final NodeList nodeListFeedType = ((Element) nodeList.item(0)).getElementsByTagName(SPConstants.STATE_FEEDTYPE);

			if ((nodeListFeedType == null)
					|| (nodeListFeedType.getLength() == 0)) {
				throw new SharepointException(
						"Unable to get feedType from nodelist");
			}

			if (nodeListFeedType.item(0) == null) {
				throw new SharepointException("Unable to get the feedType. ");
			}

			Element el = (Element) nodeListFeedType.item(0);

			if (el == null) {
				throw new SharepointException("Unable to get the feedType. ");
			}
			feedType = el.getAttribute(SPConstants.STATE_TYPE);

			el = null;

			// Full Recrawl Flag
			final NodeList flagListFullRecrawl = ((Element) nodeList.item(0)).getElementsByTagName(SPConstants.FULL_RECRAWL_FLAG);

			if ((flagListFullRecrawl == null)
					|| (flagListFullRecrawl.getLength() == 0)) {
				throw new SharepointException(
						"Unable to get lastCrawledWebStateList from nodelist");
			}

			if (flagListFullRecrawl.item(0) == null) {
				throw new SharepointException(
						"Unable to get the lastCrawledWebStateList item of the nodelist");
			}

			el = (Element) flagListFullRecrawl.item(0);

			if (el == null) {
				throw new SharepointException(
						"Unable to get the lastCrawledWebStateList element");
			}
			final String strFlagFullRecrawl = el.getAttribute(SPConstants.STATE_ID);

			lastFullCrawlDateTime = el.getAttribute(SPConstants.LAST_FULL_CRAWL_DATETIME);

			// This check indicates that if the value is null or empty it
			// implies the connector was restarted before completing the
			// traversal cycle or it was modified outside of the connector
			// traversal and hence is in inconsistent state. This value needs to
			// be initialised or else it will be lost completely when the state
			// file is overwritten during a checkpoint
			if (lastFullCrawlDateTime == null
					|| lastFullCrawlDateTime.equals("")) {
				LOGGER.warning("The value for LastFullCrawlDateTime is null implying the connector was shutdown before the first crawl cycle was completed or the state file has been modified unexpectedly");
			} else {
				LOGGER.log(Level.CONFIG, "Loading the value of last time the crawl cycle was completed from the state file : "
						+ lastFullCrawlDateTime);
			}

			if (strFlagFullRecrawl != null) {
				if (strFlagFullRecrawl.equalsIgnoreCase("true")) {
					bFullReCrawl = true;
				} else {
					bFullReCrawl = false;
				}
			}
			el = null;

			// Last Crawled Web
			final NodeList lastCrawledWebStateList = ((Element) nodeList.item(0)).getElementsByTagName(SPConstants.LAST_CRAWLED_WEB_ID);

			if ((lastCrawledWebStateList == null)
					|| (lastCrawledWebStateList.getLength() == 0)) {
				throw new SharepointException(
						"Unable to get lastCrawledWebStateList from nodelist");
			}

			if (lastCrawledWebStateList.item(0) == null) {
				throw new SharepointException(
						"Unable to get the lastCrawledWebStateList item of the nodelist");
			}

			el = (Element) lastCrawledWebStateList.item(0);

			if (el == null) {
				throw new SharepointException(
						"Unable to get the lastCrawledWebStateList item of element");
			}
			lastCrawledWebID = el.getAttribute(SPConstants.STATE_ID);
			el = null;

			// Last Crawled List
			final NodeList lastCrawledListStateList = ((Element) nodeList.item(0)).getElementsByTagName(SPConstants.LAST_CRAWLED_LIST_ID);

			if ((lastCrawledListStateList == null)
					|| (lastCrawledListStateList.getLength() == 0)) {
				throw new SharepointException(
						"Unable to get lastCrawledListStateList from nodelist");
			}

			if (lastCrawledListStateList.item(0) == null) {
				throw new SharepointException(
						"Unable to get the lastCrawledListStateList item of the nodelist");
			}

			el = (Element) lastCrawledListStateList.item(0);

			if (el == null) {
				throw new SharepointException(
						"Unable to get the lastCrawledListStateList item of element");
			}
			lastCrawledListID = el.getAttribute(SPConstants.STATE_ID);

			final NodeList children = nodeList.item(0).getChildNodes();
			if (children != null) {
				for (int i = 0; i < children.getLength(); i++) {
					final Node node = children.item(i);
					if ((node == null)
							|| (node.getNodeType() != Node.ELEMENT_NODE)) {
						continue;
					}
					final Element element = (Element) children.item(i);

					if (element == null) {
						continue;
					}

					if (!collator.equals(element.getTagName(), SPConstants.WEB_STATE)) {
						continue; // no exception; ignore xml for things we
						// don't understand
					}
					final WebState subObject = new WebState(feedType);
					subObject.setLastCrawledListID(lastCrawledListID);
					subObject.loadFromDOM(element);
					updateList(subObject);

					// now, for "currentWeb", for which so far we've only the
					// key, find the
					// actual object:
					if (lastCrawledWebID != null) {
						currentWeb = keyMap.get(lastCrawledWebID);
					}
				}
			}// null check for children
		} catch (final Throwable e) {
			LOGGER.severe(e.getMessage());
			throw new SharepointException("Unable to load state XML file");
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (final IOException e) {
					LOGGER.log(Level.SEVERE, "Unable to load state XML file", e);
					throw new SharepointException(
							"Unable to load state XML file");
				}
			}
			if (null != b) {
				try {
					b.close();
				} catch (final IOException e) {
					LOGGER.log(Level.SEVERE, "Unable to load state XML file", e);
					throw new SharepointException(
							"Unable to load state XML file");
				}
			}
		}
	}

	/**
	 * Load persistent state from our XML state.
	 * 
	 * @throws SharepointException if the XML file can't be found, or is invalid
	 *             in any way.
	 */
	public void loadState() throws SharepointException {
		final File f = getStateFileLocation();
		try {
			if (!f.exists()) {
				LOGGER.warning("state file '" + f.getCanonicalPath()
						+ "' does not exist");
				return;
			}
			LOGGER.info("loading state from " + f.getCanonicalPath());
			loadStateXML(f);
		} catch (final IOException e) {
			LOGGER.log(Level.SEVERE, "Unable to load state XML file", e);
			throw new SharepointException(e);
		}
	}

	/**
	 * Set the given List as "current" This will be remembered in the XML state.
	 * 
	 * @param inCurrentWeb ListState
	 */
	public void setCurrentWeb(final WebState inCurrentWeb) {
		currentWeb = inCurrentWeb;
	}

	/**
	 * Get the current ListState.
	 * 
	 * @return the current object, or null if none
	 */
	public WebState getCurrentWeb() {
		return currentWeb;
	}

	/**
	 * For a single StatefulObject, update the two data structures (url -> obj
	 * and time -> obj) and mark it "Existing" (if between startRecrawl() and
	 * endRecrawl()).
	 * 
	 * @param state
	 */
	public void updateList(final WebState state) {
		if (state != null) {
			keyMap.put(state.getPrimaryKey(), state);
			dateMap.add(state);
		}
	}

	/**
	 * Return the location for our state file. If we were given a
	 * googleConnectorWorkDir (the expected case), use that; else use the
	 * current working directory and log an error.
	 * 
	 * @return File
	 */
	private File getStateFileLocation() {
		File f;
		if (workDir == null) {
			LOGGER.warning("No working directory was given; using cwd");
			f = new File(SPConstants.CONNECTOR_NAME
					+ SPConstants.CONNECTOR_PREFIX);
		} else {
			f = new File(workDir, SPConstants.CONNECTOR_NAME
					+ SPConstants.CONNECTOR_PREFIX);
		}
		return f;
	}

	/**
	 * @return the list sorted list of web states
	 */
	public SortedSet getAllWebStateSet() {
		return dateMap;
	}

	/**
	 * @return the last crawled list ID
	 */
	public String getLastCrawledListID() {
		return lastCrawledListID;
	}

	/**
	 * @param inLastCrawledListState
	 */
	public void setLastCrawledListID(final String inLastCrawledListState) {
		lastCrawledListID = inLastCrawledListState;
	}

	/**
	 * @return the last crawled web ID
	 */
	public String getLastCrawledWebID() {
		if ((lastCrawledWebID == null) || (lastCrawledWebID.trim().equals(""))) {
			return null;
		}

		return lastCrawledWebID;
	}

	/**
	 * @param inLastCrawledWebID
	 */
	public void setLastCrawledWebID(final String inLastCrawledWebID) {
		lastCrawledWebID = inLastCrawledWebID;
	}

	/**
	 * @return the boolean balue depicting whther a complete crawl cycle has
	 *         completed
	 */
	public boolean isBFullReCrawl() {
		return bFullReCrawl;
	}

	/**
	 * @param fullReCrawl
	 */
	public void setBFullReCrawl(final boolean fullReCrawl) {
		bFullReCrawl = fullReCrawl;
		// If the flag is true it implies that the connector finished traversing
		// the repository once and hence is done with a complete crawl cycle
		if (bFullReCrawl) {
			lastFullCrawlDateTime = Util.formatDate(Calendar.getInstance(), Util.TIMEFORMAT_WITH_ZONE);
			LOGGER.info("Connector completed a full crawl cycle traversing all the known site collections at time : "
					+ lastFullCrawlDateTime);
		}
	}

	/**
	 * @return the feedType
	 */
	public String getFeedType() {
		return feedType;
	}
}
