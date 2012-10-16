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

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.WebsWS;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.apache.xerces.parsers.SAXParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.joda.time.DateTime;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
  private FeedType feedType;
  /**
   * To keep track of WebStates, we keep two data structures: a TreeSet relying
   * on the insertion time property of a StatefulObject, and a HashMap on the
   * primary key of the object (id for Webs). The StatefulObject interface is
   * often used to reduce our dependency on peculiarities of WebState. (At one
   * time, it was thought that there would be other instances of StatefulObject,
   * and in the future there could be again)
   */
  protected SortedSet<WebState> dateMap = new TreeSet<WebState>();
  protected Map<String, WebState> keyMap = new HashMap<String, WebState>();

  /**
   * The "currentWeb" object for WebState. The current object may be null.
   */
  protected WebState currentWeb = null;

  private WebState lastCrawledWeb = null;
  private ListState lastCrawledList = null;

  private boolean bFullReCrawl = false;
  private String lastFullCrawlDateTime = null;

  // This enum is a list of all such nodes whose values are stored as
  // inner test in the node.
  enum Nodes {
    FOLDERS_EXTRAID, ATTACHMENTS_EXTRAID, ALERTS_EXTRAID;
  }

  /**
   * Callback handler for the SAX callbacks associated with the state file
   */
  class StateHandler implements ContentHandler {
    WebState web;
    ListState list;
    private String lastCrawledWebID;
    private String lastCrawledListID;

    // Since, SAX triggers separate callbacks for the
    // inner-text and the containing node, we need to keep track of how the
    // current inner-text should be interpreted.
    Nodes currentNode = null;

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() throws SAXException {
      LOGGER.log(Level.INFO, "Parsing Begins. ");
    }

    public void endDocument() throws SAXException {
      LOGGER.log(Level.INFO, "Parsing Ends.");
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
    }

    public void startPrefixMapping(String prefix, String uri) {
    }

    public void endPrefixMapping(String prefix) {
    }

    @SuppressWarnings("deprecation")
    public void startElement(String namespaceURI, final String localName,
        String rawName, Attributes atts) throws SAXException {

      if (SPConstants.LIST_STATE.equals(localName)) {
        if (null != web) {
          try {
            list = ListState.loadStateFromXML(web, atts, feedType);
            web.AddOrUpdateListStateInWebState(list, list.getLastMod());
          } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Problem while loading ListState node from state file. ");
          }
        } else {
          LOGGER.log(Level.SEVERE, "Cannot parse the current ListState node because the expected WebState parent has not been initialized. This may occur because of the bad sequence / wrong hierarchy of stateful objects.");
        }
      } else if (SPConstants.STATE_LASTDOCCRAWLED.equals(localName)) {
        if (null != list && null != web) {
          // ID and URL are mandatory field, used in
          // SpDocument.compareTo(). These attributes must be
          // preserved.
          final String lastCrawledDocId = atts.getValue(SPConstants.STATE_ID);
          final String lastCrawledDocURL = atts.getValue(SPConstants.STATE_URL);

          Calendar lastCrawledDocLastMod = null;
          Folder lastCrawledDocParentFolder = null;
          Folder lastCrawledDocRenamedFolder = null;
          ActionType lastCrawledDocAction = null;

          if (SPType.SP2007 == web.getSharePointType()) {
            String parentFolderPath = atts.getValue(SPConstants.STATE_PARENT_FOLDER_PATH);
            String parentFolderId = atts.getValue(SPConstants.STATE_PARENT_FOLDER_ID);
            String renamedFolderPath = atts.getValue(SPConstants.STATE_RENAMED_FOLDER_PATH);
            String renamedFolderId = atts.getValue(SPConstants.STATE_RENAMED_FOLDER_ID);
            if (null == parentFolderPath || null == parentFolderId
                || null == renamedFolderId || null == renamedFolderPath) {
              // for backward compatibility. Earlier version uses
              // only FolderPath which was called FolderLevel.
              // This is why we suppress deprecation warnings.
              String folderLevel = atts.getValue(SPConstants.STATE_FOLDER_LEVEL);
              if (null != folderLevel && folderLevel.length() > 0) {
                // Force a restart of the change detection using
                // the current change token saved. This is
                // because we know that the earlier logic of
                // using only FolderLevel had a bug
                // (refer Issue 174)
                list.setLastDocProcessed(list.getDocumentInstance(feedType));
                return;
              }
            } else {
              lastCrawledDocParentFolder = new Folder(parentFolderPath,
                  parentFolderId);
              lastCrawledDocRenamedFolder = new Folder(renamedFolderPath,
                  renamedFolderId);
            }

            if (FeedType.CONTENT_FEED == feedType) {
              lastCrawledDocAction = ActionType.findActionType(atts.getValue(SPConstants.STATE_ACTION));
            }
          }
          try {
            final String strLastCrawledDocLastMod = atts.getValue(SPConstants.STATE_LASTMODIFIED);
            lastCrawledDocLastMod = Util.jodaToCalendar(Util.parseDate(strLastCrawledDocLastMod));
          } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load lastCrawledDocLastModified for List [ "
                + list.getListURL() + " ]. ", e);
          }

          SPDocument lastCrawledDoc = new SPDocument(lastCrawledDocId,
              lastCrawledDocURL, lastCrawledDocLastMod, lastCrawledDocAction);
          lastCrawledDoc.setParentFolder(lastCrawledDocParentFolder);
          lastCrawledDoc.setRenamedFolder(lastCrawledDocRenamedFolder);
          list.setLastDocProcessed(lastCrawledDoc);
        } else {
          LOGGER.log(Level.SEVERE, "Cannot parse the current LastDocCrawled node because the expected ListState/WebState parent has not been initialized. This may occur because of the bad sequence / wrong hierarchy of stateful objects.");
        }
      } else if (SPConstants.STATE_RENAMED_FOLDER_LIST.equals(localName)) {
        // Do nothing. Liststate creates an empty list of changedFolder
        // by default which corresponds to this node and hence no action
        // is required
      } else if (SPConstants.STATE_RENAMED_FOLDER_NODE.equals(localName)) {
        if (list != null) {
          // Load the renamed folder as Folder object instance and add
          // it to the list of renamed folders
          list.loadRenamedFolderList(atts);
        }
      } else if (SPConstants.STATE_EXTRAIDS_FOLDERS.equals(localName)) {
        currentNode = Nodes.FOLDERS_EXTRAID;
      } else if (SPConstants.STATE_EXTRAIDS_ATTACHMENTS.equals(localName)) {
        currentNode = Nodes.ATTACHMENTS_EXTRAID;
      } else if (SPConstants.STATE_EXTRAIDS_ALERTS.equals(localName)) {
        currentNode = Nodes.ALERTS_EXTRAID;
      } else if (SPConstants.WEB_STATE.equals(localName)) {
        try {
          web = WebState.loadStateFromXML(atts);
          addOrUpdateWebStateInGlobalState(web);
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Problem while loading WebState node from state file. ");
        }
      } else if (SPConstants.LAST_CRAWLED_WEB_ID.equals(localName)) {
        lastCrawledWebID = atts.getValue(SPConstants.STATE_ID);
      } else if (SPConstants.LAST_CRAWLED_LIST_ID.equals(localName)) {
        lastCrawledListID = atts.getValue(SPConstants.STATE_ID);
      } else if (SPConstants.FULL_RECRAWL_FLAG.equals(localName)) {
        bFullReCrawl = Boolean.valueOf(atts.getValue(SPConstants.STATE_ID));
        lastFullCrawlDateTime = atts.getValue(SPConstants.LAST_FULL_CRAWL_DATETIME);
      } else if (SPConstants.STATE_FEEDTYPE.equals(localName)) {
        feedType = FeedType.getFeedType(atts.getValue(SPConstants.STATE_TYPE));
      }
    }

    public void endElement(String namespaceURI, String localName, String rawName)
        throws SAXException {
      if (SPConstants.STATE.equals(localName)) {
        if (null != lastCrawledWebID) {
          lastCrawledWeb = currentWeb = keyMap.get(lastCrawledWebID);
        }
        if (null != lastCrawledListID && null != lastCrawledWeb) {
          lastCrawledList = lookupList(lastCrawledWebID, lastCrawledListID);
          lastCrawledWeb.setLastCrawledList(lastCrawledWeb.lookupList(lastCrawledListID));
        }
      } else if (SPConstants.LIST_STATE.equals(localName)) {
        list = null;
      } else if (SPConstants.WEB_STATE.equals(localName)) {
        web = null;
      }
    }

    public void characters(char[] ch, int start, int end) throws SAXException {
      if (null == list || null == currentNode) {
        return;
      }
      String chrs = new String(ch, start, end);
      if (chrs.trim().length() == 0) {
        return;
      }
      if (Nodes.ALERTS_EXTRAID.equals(currentNode)
          || Nodes.FOLDERS_EXTRAID.equals(currentNode)) {
        list.getIDs().append(ch, start, end);
      } else if (Nodes.ATTACHMENTS_EXTRAID.equals(currentNode)) {
        list.getAttchmnts().append(ch, start, end);
      }
    }

    public void ignorableWhitespace(char[] ch, int start, int end)
        throws SAXException {
    }

    public void skippedEntity(String name) throws SAXException {
    }
  }

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
      f1 = new File(SPConstants.CONNECTOR_NAME + SPConstants.CONNECTOR_PREFIX);
    } else {
      LOGGER.info("Work Dir: " + workDir);
      f1 = new File(workDir, SPConstants.CONNECTOR_NAME
          + SPConstants.CONNECTOR_PREFIX);
    }
    LOGGER.info("deleting state file from location...." + f1.getAbsolutePath());
    if (f1.exists()) {
      final boolean isDeleted = f1.delete();
      LOGGER.info("deleted status :" + isDeleted);
    }
  }

  /**
   * Constructor.
   *
   * @param inWorkDir the googleConnectorWorkDir (which we ask for in
   *          connectorInstance.xml). The state file is saved in this directory.
   *          If workDir is null, the current working directory is used instead.
   *          (In either case, the location of the file is saved in the system
   *          preferences, so that environmental changes don't make us lose the
   *          file.)
   */
  public GlobalState(final String inWorkDir, final FeedType inFeedType) {
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
   *          WebID
   * @return new {@link WebState} which is already indexed in GlobalState's
   *         dateMap and keyMap
   */
  public WebState makeWebState(final SharepointClientContext spContext,
      final String key) throws SharepointException {
    if (key != null) {
      final WebState obj = new WebState(spContext, key);
      final DateTime dt = new DateTime();
      obj.setInsertionTime(dt);
      addOrUpdateWebStateInGlobalState(obj);
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
      final Iterator<WebState> it = dateMap.iterator();
      while (it.hasNext()) {
        final WebState webs = it.next();
        webs.setExisting(false);
      }
    }
  }

  /**
   * Signals that the recrawl cycle is over, and GlobalState may now delete any
   * ListState which did not appear in a updateList() call since the
   * startRecrawl() call.
   */
  public void endRecrawl(final SharepointClientContext spContext) {
    if (!recrawling) {
      LOGGER.severe("called endRecrawl() when not in a recrawl state");
      return;
    }

    boolean configLogging = LOGGER.isLoggable(Level.CONFIG);
    if (bFullReCrawl == true) {
      LOGGER.config("ending recrawl ...bFullReCrawl true ... cleaning up WebStates");
      final Iterator<WebState> iter = getIterator();
      if (null != iter) {
        while (iter.hasNext()) {
          final WebState webs = iter.next();
          webs.endRecrawl(spContext);
          if (!webs.isExisting()) {
            // Case of web deletion. Delete this web State only if
            // does not contain any list State info and having a
            // single list state that represents site data.
            if (webs.getAllListStateSet().size() == 0
                || webs.getAllListStateSet().first().isSiteDefaultPage()) {
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

              if (configLogging) {
                LOGGER.log(Level.CONFIG, "Deleting the state information for web ["
                    + webs.getWebUrl() + "]. ");
              }
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
  public Iterator<WebState> getIterator() {
    return dateMap.iterator();
  }

  /**
   * Get dateMap iterator beginning at the current ListState and wrapping around
   * to finish just before the current. If there is no current, you just get an
   * ordinary iterator.
   *
   * @return Iterator which begins at getCurrentList() and wraps around the end
   */
  public Iterator<WebState> getCircularIterator() {
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
        LOGGER.log(Level.WARNING, "webWS creation failed for URL [ " + key
            + " ]. ", e);
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
   * Load persistent state from our XML state.
   *
   * @throws SharepointException if the XML file can't be found, or is invalid
   *           in any way.
   */
  public void loadState() throws SharepointException {
    final File stateFile = getStateFileLocation();
    XMLReader parser;
    InputSource inputSource = null;
    try {
      if (!stateFile.exists()) {
        LOGGER.warning("state file '" + stateFile.getCanonicalPath()
            + "' does not exist");
        return;
      }
      LOGGER.info("loading state from " + stateFile.getCanonicalPath());
      parser = new SAXParser();
      inputSource = new InputSource(new InputStreamReader(new FileInputStream(
          stateFile), "UTF-8"));
      inputSource.setEncoding("UTF-8");
      parser.setContentHandler(new StateHandler());
      parser.parse(inputSource);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Unable to load state XML file", e);
      throw new SharepointException(e);
    } catch (final Throwable t) {
      LOGGER.log(Level.SEVERE, "error/Exception while loading state file. ", t);
    } finally {
      inputSource = null;
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
   * This is the recommended method for adding a new WebState entry into the
   * GlobalState Or, updating any such attribute of a WebState which can affect
   * the ordering of WebStates in current WebState. TODO: Currently, there are
   * no use cases where any such attribute which drives the ordering is updated
   * after the WebState is added. In future if required, this method can be
   * augmented with some generic attribute informations which drives so that the
   * ordering of WebStates is maintained.
   */
  public void addOrUpdateWebStateInGlobalState(final WebState state) {
    if (state != null) {
      keyMap.put(state.getPrimaryKey(), state);
      // Deletion is required to ensure that both datastructures are
      // keeping reference to the same stateful objects
      if (dateMap.contains(state)) {
        dateMap.remove(state);
      }
      dateMap.add(state);
    }
  }

  /**
   * Return the location for our state file. If we were given a
   * googleConnectorWorkDir (the expected case), use that; else use the current
   * working directory and log an error.
   *
   * @return File
   */
  private File getStateFileLocation() {
    File f;
    if (workDir == null) {
      LOGGER.warning("No working directory was given; using cwd");
      f = new File(SPConstants.CONNECTOR_NAME + SPConstants.CONNECTOR_PREFIX);
    } else {
      f = new File(workDir, SPConstants.CONNECTOR_NAME
          + SPConstants.CONNECTOR_PREFIX);
    }
    return f;
  }

  /**
   * @return the list sorted list of web states
   */
  public SortedSet<WebState> getAllWebStateSet() {
    return dateMap;
  }

  /**
   * @return The Last Crawled list reference
   */
  public ListState getLastCrawledList() {
    return lastCrawledList;
  }

  /**
   * @param inLastCrawledListState
   */
  public void setLastCrawledList(final ListState inLastCrawledListState) {
    lastCrawledList = inLastCrawledListState;
  }

  /**
   * @return The Last Crawled web reference
   */
  public WebState getLastCrawledWeb() {
    return lastCrawledWeb;
  }

  /**
   * @param inLastCrawledWeb
   */
  public void setLastCrawledWeb(final WebState inLastCrawledWeb) {
    lastCrawledWeb = inLastCrawledWeb;
  }

  /**
   * @return the boolean value depicting whether a complete crawl cycle has
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
  public FeedType getFeedType() {
    return feedType;
  }

  public void saveState() throws SharepointException {
    try {
      FileOutputStream fos = new FileOutputStream(getStateFileLocation());
      OutputFormat of = new OutputFormat("XML", "UTF-8", true);
      of.setLineWidth(500);
      of.setIndent(2);
      XMLSerializer serializer = new XMLSerializer(fos, of);
      ContentHandler handler = serializer.asContentHandler();
      dumpStateToXML(handler);
      fos.close();
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Save State Failed", e);
      throw new SharepointException("Save state failed", e);
    } catch (final Throwable t) {
      LOGGER.log(Level.WARNING, "Save State Failed", t);
      throw new SharepointException("Save state failed", t);
    }
  }

  public void dumpStateToXML(ContentHandler handler) throws SAXException {
    AttributesImpl atts = new AttributesImpl();
    handler.startDocument();
    handler.startElement("", "", SPConstants.STATE, atts);

    // Feed Type used
    atts.clear();
    atts.addAttribute("", "", SPConstants.STATE_TYPE, SPConstants.STATE_ATTR_ID, feedType.toString());
    handler.startElement("", "", SPConstants.STATE_FEEDTYPE, atts);
    handler.endElement("", "", SPConstants.STATE_FEEDTYPE);

    // FULL_RECRAWL_FLAG
    atts.clear();
    atts.addAttribute("", "", SPConstants.STATE_ID, SPConstants.STATE_ATTR_ID, bFullReCrawl
        + "");
    atts.addAttribute("", "", SPConstants.LAST_FULL_CRAWL_DATETIME, SPConstants.STATE_ATTR_CDATA, lastFullCrawlDateTime);
    handler.startElement("", "", SPConstants.FULL_RECRAWL_FLAG, atts);
    handler.endElement("", "", SPConstants.FULL_RECRAWL_FLAG);

    // LAST_CRAWLED_WEB_ID
    if (null != getLastCrawledWeb()) {
      atts.clear();
      atts.addAttribute("", "", SPConstants.STATE_ID, SPConstants.STATE_ATTR_IDREF, getLastCrawledWeb().getPrimaryKey());
      handler.startElement("", "", SPConstants.LAST_CRAWLED_WEB_ID, atts);
      handler.endElement("", "", SPConstants.LAST_CRAWLED_WEB_ID);
    }

    // LAST_CRAWLED_LIST_ID
    if (null != lastCrawledList) {
      atts.clear();
      atts.addAttribute("", "", SPConstants.STATE_ID, SPConstants.STATE_ATTR_IDREF, getLastCrawledList().getPrimaryKey());
      handler.startElement("", "", SPConstants.LAST_CRAWLED_LIST_ID, atts);
      handler.endElement("", "", SPConstants.LAST_CRAWLED_LIST_ID);
    }

    // now dump the actual WebStates:
    if (null == dateMap) {
      LOGGER.log(Level.WARNING, "No WebStates found in the connector state.");
    } else {
      for (WebState web : dateMap) {
        web.dumpStateToXML(handler, feedType);
      }
    }
    handler.endElement("", "", SPConstants.STATE);
    handler.endDocument();
  }
}
