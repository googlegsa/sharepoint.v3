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

package com.google.enterprise.connector.sharepoint.wsclient;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.generated.lists.GetAttachmentCollectionResponseGetAttachmentCollectionResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenContains;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQuery;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenQueryOptions;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemChangesSinceTokenViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQuery;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsQueryOptions;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsResponseGetListItemsResult;
import com.google.enterprise.connector.sharepoint.generated.lists.GetListItemsViewFields;
import com.google.enterprise.connector.sharepoint.generated.lists.Lists;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsLocator;
import com.google.enterprise.connector.sharepoint.generated.lists.ListsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

/**
 * Java Client for calling Lists.asmx Provides a layer to talk to the Lists Web
 * Service on the SharePoint server Any call to this Web Service must go through
 * this layer.
 *
 * @author nitendra_thakur
 */
public class ListsWS {
    private final Logger LOGGER = Logger.getLogger(ListsWS.class.getName());
    private SharepointClientContext sharepointClientContext;
    private String endpoint;
    private ListsSoap_BindingStub stub = null;
    private String rowLimit = SPConstants.DEFAULT_ROWLIMIT;

    /**
     * @param inSharepointClientContext The Context is passed so that necessary
     *            information can be used to create the instance of current
     *            class Web Service endpoint is set to the default SharePoint
     *            URL stored in SharePointClientContext.
     * @throws SharepointException
     */
    public ListsWS(final SharepointClientContext inSharepointClientContext)
            throws SharepointException {

        if (inSharepointClientContext != null) {
            sharepointClientContext = inSharepointClientContext;

            if (inSharepointClientContext.getBatchHint() > 0) {
                rowLimit = "" + inSharepointClientContext.getBatchHint();
            }
            LOGGER.log(Level.FINEST, "RowLimit set to: " + rowLimit);
            endpoint = Util.encodeURL(sharepointClientContext.getSiteURL())
                    + SPConstants.LISTS_END_POINT;
            LOGGER.config("endpoint set to: " + endpoint);

            final ListsLocator loc = new ListsLocator();
            loc.setListsSoapEndpointAddress(endpoint);

            final Lists listsService = loc;
            try {
                stub = (ListsSoap_BindingStub) listsService.getListsSoap();
            } catch (final ServiceException e) {
                LOGGER.log(Level.WARNING, "Unable to get the list stub", e);
                throw new SharepointException("Unable to get the list stub");
            }

            final String strDomain = inSharepointClientContext.getDomain();
            String strUser = inSharepointClientContext.getUsername();
            final String strPassword = inSharepointClientContext.getPassword();

            strUser = Util.getUserNameWithDomain(strUser, strDomain);
            stub.setUsername(strUser);
            stub.setPassword(strPassword);
        }
    }

    /**
     * Gets all the attachments of a particular list item.
     *
     * @param baseList List to which the item belongs
     * @param listItem list item for which the attachments need to be retrieved.
     * @return list of sharepoint SPDocuments corresponding to attachments for
     *         the given list item. These are ordered by last Modified time.
     * @throws SharepointException
     * @throws MalformedURLException
     */
    public List<SPDocument> getAttachments(final ListState baseList,
            final SPDocument listItem) {
        final ArrayList<SPDocument> listAttachments = new ArrayList<SPDocument>();
        if (baseList == null) {
            LOGGER.warning("Unable to get the attachments because list is null. ");
            return listAttachments;
        }

        if (listItem == null) {
            LOGGER.warning("Unable to get the attachments because listItem provided for list [ "
                    + baseList.getListURL() + " ] is null. ");
            return listAttachments;
        }

        LOGGER.config("baseList[ title=" + baseList.getListTitle() + " , Url="
                + listItem.getUrl() + "]");

        if (stub == null) {
            LOGGER.warning("Unable to get the attachments for listItem [ "
                    + listItem.getUrl() + " ], list [ " + baseList.getListURL()
                    + " ] because stub is null. ");
            return listAttachments;
        }
        final String listName = baseList.getPrimaryKey();
        final String listItemId = Util.getOriginalDocId(listItem.getDocId(), listItem.getFeedType());
        GetAttachmentCollectionResponseGetAttachmentCollectionResult res = null;

        try {
            res = stub.getAttachmentCollection(listName, listItemId);
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    res = stub.getAttachmentCollection(listName, listItemId);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get attachments for ListItemId [ "
                            + listItemId + " ], List [ " + listName + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get attachments for ListItemId [ "
                        + listItemId + " ], List [ " + listName + " ].", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get attachments for ListItemId [ "
                    + listItemId + " ], List [ " + listName + " ].", e);
        }

        // All the known attachments (discovered earlier and are their in the
        // connector's state) are first collected into the knownAttachments and
        // then all those which are still returned by the Web Service will be
        // removed. This way, we'll be able to track the deleted attachments.
        List<String> knownAttachments = null;
        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
            knownAttachments = baseList.getAttachmntURLsFor(listItemId);
        }

        if (res != null) {
            final MessageElement[] me = res.get_any();
            if (me != null) {
                if (me.length > 0) {
                    if (me[0] != null) {
                        final Iterator ita = me[0].getChildElements();
                        while ((ita != null) && (ita.hasNext())) {
                            final MessageElement attachmentsOmElement = (MessageElement) ita.next();
                            for (final Iterator attachmentsIt = attachmentsOmElement.getChildElements(); attachmentsIt.hasNext();) {
                                final String url = attachmentsIt.next().toString();
                                LOGGER.config("Attachment URL :" + url);

                                if (sharepointClientContext.isIncludedUrl(url)) {
                                    LOGGER.config("included URL [" + url + " ]");

                                    String modifiedID = listItemId;
                                    if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                                        modifiedID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID
                                                + "["
                                                + url
                                                + "]"
                                                + listItem.getDocId();

                                        if (knownAttachments.contains(url)) {
                                            knownAttachments.remove(url);
                                        }
                                    }
                                    final SPDocument doc = new SPDocument(
                                            modifiedID,
                                            url,
                                            baseList.getLastModCal(),
                                            SPConstants.NO_AUTHOR,
                                            SPConstants.OBJTYPE_ATTACHMENT,
                                            baseList.getParentWebState().getTitle(),
                                            sharepointClientContext.getFeedType(),
                                            listItem.getSPType());

                                    listAttachments.add(doc);
                                } else {
                                    LOGGER.warning("excluding " + url);
                                }
                            }
                        }
                    }
                }
            }// end: if
        }

        LOGGER.log(Level.INFO, "Found [" + listAttachments.size()
                + "] new/updated attachments for listItem [ "
                + listItem.getUrl() + "]. ");

        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
            // All the urls which have been left in knownAttachments are
            // considered to be deleted.
            for (String attchmnt_url : knownAttachments) {
                final String docID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID
                        + "[" + attchmnt_url + "]" + listItem.getDocId();
                final SPDocument attchmnt = new SPDocument(docID, attchmnt_url,
                        baseList.getLastModCal(), SPConstants.NO_AUTHOR,
                        SPConstants.OBJTYPE_ATTACHMENT,
                        baseList.getParentWebState().getTitle(),
                        sharepointClientContext.getFeedType(),
                        baseList.getParentWebState().getSharePointType());
                attchmnt.setAction(ActionType.DELETE);
                listAttachments.add(attchmnt);
                LOGGER.log(Level.INFO, "Sending attachment [" + attchmnt_url
                        + "] for listItem [ " + listItem.getUrl()
                        + "] as a delete feed. ");
            }
        }

        Collections.sort(listAttachments);
        return listAttachments;
    }

    /**
     * Used to create WS query in case of SP2003
     *
     * @param c
     * @param listItemID
     * @return the created query being used for WS call
     * @throws ParseException
     */
    private MessageElement[] createQuery(final Calendar c, String listItemID)
            throws ParseException {
        String date = null;
        boolean isList = false;

        // To ensure a properList ItemID...case of List where GUID is found
        try {
            Integer.parseInt(listItemID);
        } catch (final Exception e) {
            LOGGER.config("List Discovered.. ID: " + listItemID);
            listItemID = "0";
            // change the query
            isList = true;
        }

        if (c != null) {

            date = Value.calendarToIso8601(c);
            final Date dt = (Date) SPConstants.ISO8601_DATE_FORMAT_MILLIS.parse(date);
            date = SPConstants.ISO8601_DATE_FORMAT_SECS.format(dt);
            LOGGER.config("Time: " + date);
        }

        String strMyString = "<Query/>";// empty Query String

        if (((date == null) || (listItemID == null))) {
            LOGGER.config("Initial case ...");
            strMyString = ""
                    + "<Query>"
                    + "<OrderBy><FieldRef Name=\"Modified\" Ascending=\"TRUE\" /></OrderBy>"
                    + "</Query>";
        } else if (isList == true) {
            LOGGER.config("list case...");
            strMyString = ""
                    + "<Query>"
                    + "<Where>"
                    + "<Gt>"
                    + "<FieldRef Name=\"Modified\"/>"
                    + "<Value Type=\"DateTime\" IncludeTimeValue=\"TRUE\" StorageTZ=\"TRUE\">"
                    + date
                    + "</Value>"
                    + "</Gt>"
                    + "</Where>"
                    + "<OrderBy><FieldRef Name=\"Modified\" Ascending=\"TRUE\" /></OrderBy>"
                    + "</Query>";
        } else {
            LOGGER.config("other cases ...");
            strMyString = ""
                    + "<Query>"
                    + "<Where>"
                    + "<Or>"
                    + "<Gt>"
                    + "<FieldRef Name=\"Modified\"/>"
                    + "<Value Type=\"DateTime\" IncludeTimeValue=\"TRUE\" StorageTZ=\"TRUE\">"
                    + date
                    + "</Value>"
                    + "</Gt>"

                    + "<And>"
                    + "<Eq>"
                    + "<FieldRef Name=\"Modified\"/>"
                    + "<Value Type=\"DateTime\" IncludeTimeValue=\"TRUE\" StorageTZ=\"TRUE\">"
                    + date
                    + "</Value>"
                    + "</Eq>"
                    + "<Gt>"
                    + "<FieldRef Name=\"ID\"/>"
                    + "<Value Type=\"Text\">"
                    + listItemID
                    + "</Value>"
                    + "</Gt>"
                    + "</And>"
                    + "</Or>"
                    + "</Where>"
                    + "<OrderBy><FieldRef Name=\"Modified\" Ascending=\"TRUE\" /></OrderBy>"
                    + "</Query>";
        }
        final MessageElement[] meArray = { getMeFromString(strMyString) };// Array
        // of
        // the
        // message
        // element
        return meArray;
    }

    /**
     * For getting only folders starting from a given ID.
     *
     * @param listItemID
     * @return the created query being used for WS call
     * @throws ParseException
     */
    private MessageElement[] createQuery1(String listItemID)
            throws ParseException {
        try {
            Integer.parseInt(listItemID);
        } catch (final Exception e) {
            // Eatup the exception. This was just to check whether it is a list
            // or listItem.
            listItemID = "0";
        }
        final String strMyString = ""
                + "<Query>"
                + "<Where>"
                + "<And>"
                + "<Gt>"
                + "<FieldRef Name=\"ID\"/>"
                + "<Value Type=\"Counter\">"
                + listItemID
                + "</Value>"
                + "</Gt>"
                + "<Eq>"
                + "<FieldRef Name=\"ContentType\"/>"
                + "<Value Type=\"Text\">Folder</Value>"
                + "</Eq>"
                + "</And>"
                + "</Where>"
                + "<OrderBy><FieldRef Name=\"ID\" Ascending=\"TRUE\" /></OrderBy>"
                + "</Query>";

        final MessageElement[] meArray = { getMeFromString(strMyString) };
        return meArray;
    }

    /**
     * For getting only documents and not folders; starting from a given
     * lastItemID
     *
     * @param listItemID
     * @return the created query being used for WS call
     * @throws ParseException
     */
    private MessageElement[] createQuery2(String listItemID)
            throws ParseException {
        try {
            Integer.parseInt(listItemID);
        } catch (final Exception e) {
            // Eatup the exception. This was just to check whether it is a list
            // or listItem.
            listItemID = "0";
        }
        final String strMyString = ""
                + "<Query>"
                + "<Where>"
                + "<And>"
                + "<Gt>"
                + "<FieldRef Name=\"ID\"/>"
                + "<Value Type=\"Counter\">"
                + listItemID
                + "</Value>"
                + "</Gt>"
                + "<Neq>"
                + "<FieldRef Name=\"ContentType\"/>"
                + "<Value Type=\"Text\">Folder</Value>"
                + "</Neq>"
                + "</And>"
                + "</Where>"
                + "<OrderBy><FieldRef Name=\"ID\" Ascending=\"TRUE\" /></OrderBy>"
                + "</Query>";

        final MessageElement[] meArray = { getMeFromString(strMyString) };
        return meArray;
    }

    /**
     * For getting the documents starting from a given lastItemID, but we also
     * want to get folders which are independent of the lastItemID constraint.
     *
     * @param listItemID
     * @return the created query being used for WS call
     * @throws ParseException
     */
    private MessageElement[] createQuery3(String listItemID)
            throws ParseException {
        try {
            Integer.parseInt(listItemID);
        } catch (final Exception e) {
            // Eatup the exception. This was just to check whether it is a list
            // or listItem.
            listItemID = "0";
        }
        final String strMyString = ""
                + "<Query>"
                + "<Where>"
                + "<Or>"
                + "<Gt>"
                + "<FieldRef Name=\"ID\"/>"
                + "<Value Type=\"Counter\">"
                + listItemID
                + "</Value>"
                + "</Gt>"
                + "<Eq>"
                + "<FieldRef Name=\"ContentType\"/>"
                + "<Value Type=\"Text\">Folder</Value>"
                + "</Eq>"
                + "</Or>"
                + "</Where>"
                + "<OrderBy><FieldRef Name=\"ID\" Ascending=\"TRUE\" /></OrderBy>"
                + "</Query>";

        final MessageElement[] meArray = { getMeFromString(strMyString) };
        return meArray;
    }

    /**
     * Returns a MessageElement element object for a given string in xml format
     *
     * @param strMyString
     * @return the created query being used for WS call
     */
    MessageElement getMeFromString(final String strMyString) {
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (final ParserConfigurationException e) {

            e.printStackTrace();
        } catch (final FactoryConfigurationError e) {

            e.printStackTrace();
        }
        final StringReader reader = new StringReader(strMyString);
        final InputSource inputsource = new InputSource(reader);
        Document doc = null;
        try {
            doc = docBuilder.parse(inputsource);
        } catch (final SAXException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        final Element ele = doc.getDocumentElement();
        final MessageElement msg = new MessageElement(ele);

        return msg;
    }

    /**
     * View Fields required for making web service call
     *
     * @return the view fields being used for WS call
     */
    private MessageElement[] createViewFields() {
        final String sViewFields = "ViewFields";
        final MessageElement me = new MessageElement(new QName(sViewFields));
        me.addAttribute(null, "Properties", new QName("TRUE"));
        final MessageElement[] meArray = { me };
        return meArray;
    }

    /**
     * Query Options required while making web service calls.
     *
     * @param recursion
     * @param folderLevel
     * @param nextPage
     * @return the query options being used for WS call
     */
    private MessageElement[] createQueryOptions(final boolean recursion,
            final String folderLevel, final String nextPage) {
        final MessageElement me = new MessageElement(new QName("QueryOptions"));
        try {
            me.addChildElement(new MessageElement(new QName(
                    "IncludeMandatoryColumns"))).addTextNode("true");
            me.addChildElement(new MessageElement(new QName("DateInUtc"))).addTextNode("TRUE");

            // If no scope has been explicitly given, get the list items
            // recursively. Else, get itmes only at the given scope. Scope
            // arguement has the higher priority here and will be checked first.
            if ((folderLevel != null) && (folderLevel.trim().length() > 0)) {
                me.addChildElement(new MessageElement(new QName("Folder"))).addTextNode(folderLevel);
                // folder information are by deafult returned by
                // getListItemChangesSinceToken, when folder scope is given.
                // Hance, no need to use "OptimizeFor" in this case.
            } else if (recursion) {
                me.addChildElement(new MessageElement(new QName(
                        "ViewAttributes"))).addAttribute(SOAPFactory.newInstance().createName("Scope"), "Recursive");
                me.addChildElement(new MessageElement(new QName("OptimizeFor"))).addTextNode("ItemIds"); // added
                // for
                // getting
                // folder
                // information
                // when
                // recursion
                // is
                // being
                // used,
                // in
                // case
                // of
                // getListItemChangesSinceToken.
            }

            if (nextPage != null) {
                me.addChildElement(new MessageElement(new QName("Paging"))).addAttribute(SOAPFactory.newInstance().createName("ListItemCollectionPositionNext"), nextPage);
            }
        } catch (final SOAPException se) {
            LOGGER.log(Level.WARNING, "Problem while creating Query Options.", se);
        }
        final MessageElement[] meArray = { me };
        return meArray;
    }

    /**
     * Used to get list items under a list using getListItems() Web Method
     *
     * @param list : List whose items is to be retrieved
     * @param lastModified : serves as a base for incremental crawl
     * @param lastItemID : Serves as a base for incremental crawl
     * @param allWebs : A collection to store any webs, discovered as part of
     *            discovering list items. Foe example link sites are stored as
     *            list items.
     * @return the list of documents as {@link SPDocument}
     */
    public List<SPDocument> getListItems(final ListState list,
            final Calendar lastModified, final String lastItemID,
            final Set<String> allWebs) {
        final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
        if (list == null) {
            LOGGER.warning("Unable to get the list items because list is null");
            return listItems;
        }

        LOGGER.config("list: title [ " + list.getListTitle() + " ], URL [ "
                + list.getListURL() + " ], lastItemID [ " + lastItemID
                + " ], lastModified [ " + lastModified + " ]");

        if (sharepointClientContext == null) {
            LOGGER.warning("Unable to get the list items because client context is null");
            return listItems;
        }
        if (stub == null) {
            LOGGER.warning("Unable to get the list items because stub is null");
            return listItems;
        }

        final String listName = list.getPrimaryKey();
        final String viewName = "";
        final GetListItemsQuery query = new GetListItemsQuery();
        final GetListItemsViewFields viewFields = new GetListItemsViewFields(); // <ViewFields
        // />
        final GetListItemsQueryOptions queryOptions = new GetListItemsQueryOptions();
        final String webID = "";
        GetListItemsResponseGetListItemsResult res = null;

        try {
            query.set_any(createQuery(lastModified, lastItemID));// Create Query
            // for the
            // Lists
            viewFields.set_any(createViewFields());
            queryOptions.set_any(createQueryOptions(list.canContainFolders(), null, null));
            LOGGER.log(Level.CONFIG, "Making Web Service call with the following parameters:\n query [ "
                    + query.get_any()[0]
                    + " ], queryoptions [ "
                    + queryOptions.get_any()[0]
                    + " ], viewFields [ "
                    + viewFields.get_any()[0] + "] ");
            res = stub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    res = stub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                            + listName + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                        + listName + " ].", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                    + listName + " ].", e);
        }

        if (res != null) {
            final MessageElement[] me = res.get_any();

            if ((me != null) && (me.length > 0)) {
                for (final Iterator itChilds = me[0].getChildElements(); itChilds.hasNext();) {
                    final MessageElement child = (MessageElement) itChilds.next();
                    if ("data".equalsIgnoreCase(child.getLocalName())) {
                        for (final Iterator itrchild = child.getChildElements(); itrchild.hasNext();) {
                            final MessageElement row = (MessageElement) itrchild.next();
                            final SPDocument doc = processListItemElement(row, list, allWebs);
                            if (doc != null) {
                                listItems.add(doc);
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(listItems);
        LOGGER.info("found: " + listItems.size() + " Items in List/Library ["
                + list.getListURL() + "]");

        return listItems;
    }

    /**
     * Retrieves all the folder hierarchy from a given folder level and updates
     * the ExtraIDs of the list. This operation is independent of the batch hint
     * because the discovered folders are not sent as docs.
     *
     * @param list : Specify the base list
     * @param folderLevel : From where to discover the folder hierarchy
     * @param lastID ; If we have already identified some folders at this
     *            folderLevel, specify the lastItemID to get the next set of
     *            folders.
     * @return the list of folders in this list
     */
    public List<String> getFolderHierarchy(final ListState list,
            final String folderLevel, final String lastID) {
        List<String> folderLevels = new ArrayList<String>();
        if (!list.canContainFolders()) {
            return folderLevels; // folders are created only inside document
            // libraries.
        }
        if ((folderLevel != null) && (folderLevel.trim().length() != 0)) {
            folderLevels.add(folderLevel);
        }

        final String listName = list.getPrimaryKey();
        final String viewName = "";
        final GetListItemChangesSinceTokenQuery query = new GetListItemChangesSinceTokenQuery();
        final GetListItemChangesSinceTokenViewFields viewFields = new GetListItemChangesSinceTokenViewFields();
        final GetListItemChangesSinceTokenQueryOptions queryOptions = new GetListItemChangesSinceTokenQueryOptions();
        final String token = null;
        final GetListItemChangesSinceTokenContains contains = null;
        GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult res = null;
        try {
            query.set_any(createQuery1(lastID));
            viewFields.set_any(createViewFields());
            queryOptions.set_any(createQueryOptions(true, null, null));
            LOGGER.log(Level.CONFIG, "Making Web Service call with the following parameters:\n query [ "
                    + query.get_any()[0]
                    + " ], queryoptions [ "
                    + queryOptions.get_any()[0]
                    + " ], viewFields [ "
                    + viewFields.get_any()[0] + "] ");
            res = stub.getListItemChangesSinceToken(listName, viewName, query, viewFields, SPConstants.DEFAULT_ROWLIMIT, queryOptions, token, contains);
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    res = stub.getListItemChangesSinceToken(listName, viewName, query, viewFields, SPConstants.DEFAULT_ROWLIMIT, queryOptions, token, contains);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Problem while getting folder hierarchy at folderLevel [ "
                            + folderLevel
                            + " ], list [ "
                            + list.getListURL()
                            + " ].", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Problem while getting folder hierarchy at folderLevel [ "
                        + folderLevel
                        + " ], list [ "
                        + list.getListURL()
                        + " ].", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Problem while getting folder hierarchy at folderLevel [ "
                    + folderLevel + " ], list [ " + list.getListURL() + " ].", e);
        }

        if (res != null) {
            final MessageElement[] me = res.get_any();
            if ((me != null) && (me.length > 0)) {
                for (final Iterator itChilds = me[0].getChildElements(); itChilds.hasNext();) {
                    final MessageElement child = (MessageElement) itChilds.next();
                    if (SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
                        final String tmpNextPage = child.getAttribute(SPConstants.LIST_ITEM_COLLECTION_POSITION_NEXT);
                        String lastItemID = null;
                        for (final Iterator itrchild = child.getChildElements(); itrchild.hasNext();) {
                            final MessageElement row = (MessageElement) itrchild.next();
                            final String contentType = row.getAttribute(SPConstants.CONTENTTYPE);
                            String relativeURL = row.getAttribute(SPConstants.FILEREF);
                            final String docId = row.getAttribute(SPConstants.ID);
                            if ((contentType == null) || (relativeURL == null)
                                    || (docId == null)) {
                                continue;
                            }
                            lastItemID = docId;
                            relativeURL = relativeURL.substring(relativeURL.indexOf(SPConstants.HASH) + 1);
                            String folderPath = null;
                            if (contentType.equalsIgnoreCase(SPConstants.CONTENT_TYPE_FOLDER)) {
                                if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                                    try {
                                        list.updateExtraIDs(relativeURL, docId, true);
                                    } catch (SharepointException se) {
                                        LOGGER.log(Level.WARNING, "Problem while updating relativeURL [ "
                                                + relativeURL
                                                + " ], listURL [ "
                                                + list.getListURL() + " ]. ", se);
                                    }
                                }
                                folderPath = Util.getFolderPathForWSCall(list.getParentWebState().getWebUrl(), relativeURL);
                                if (folderPath == null) {
                                    continue;
                                }
                                if ((folderLevel != null)
                                        && (folderLevel.trim().length() != 0)) {
                                    if (folderPath.startsWith(folderLevel)) {
                                        folderLevels.add(folderPath);
                                    }
                                } else {
                                    folderLevels.add(folderPath);
                                }
                            }
                        }
                        if (tmpNextPage != null) {
                            folderLevels.addAll(getFolderHierarchy(list, folderLevel, lastItemID));
                        }
                    }
                }
            }
        }

        folderLevels = new ArrayList<String>(new TreeSet<String>(folderLevels));
        Collections.sort(folderLevels);
        LOGGER.log(Level.INFO, "Folders Discovered: " + folderLevels);
        return folderLevels;
    }

    /**
     * Retrieves the list items only the specified level. This required when a
     * folder is restored and we need to discover items level by level.
     *
     * @param list : Base List
     * @param lastItemID : Last Item ID that we have already identified at this
     *            level.
     * @param folderLevel : The folder level at which we need to discover the
     *            items.
     * @return the list of documents as {@link SPDocument}
     */
    public List<SPDocument> getListItemsAtFolderLevel(final ListState list,
            final String lastItemID, final String folderLevel) {
        final List<SPDocument> listItems = new ArrayList<SPDocument>();
        final String listName = list.getPrimaryKey();
        final String viewName = "";
        final GetListItemsQuery query = new GetListItemsQuery();
        final GetListItemsViewFields viewFields = new GetListItemsViewFields(); // <ViewFields
        // />
        final GetListItemsQueryOptions queryOptions = new GetListItemsQueryOptions();
        final String webID = "";
        GetListItemsResponseGetListItemsResult res = null;

        try {
            query.set_any(createQuery2(lastItemID));
            viewFields.set_any(createViewFields());
            queryOptions.set_any(createQueryOptions(false, folderLevel, null));
            LOGGER.log(Level.CONFIG, "Making Web Service call with the following parameters:\n query [ "
                    + query.get_any()[0]
                    + " ], queryoptions [ "
                    + queryOptions.get_any()[0]
                    + " ], viewFields [ "
                    + viewFields.get_any()[0] + "] ");
            res = stub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    res = stub.getListItems(listName, viewName, query, viewFields, rowLimit, queryOptions, webID);
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                            + listName
                            + " ] at level [ "
                            + folderLevel
                            + " ]. ", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                        + listName + " ] at level [ " + folderLevel + " ]. ", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                    + listName + " ] at level [ " + folderLevel + " ]. ", e);
        }

        if (res != null) {
            final MessageElement[] me = res.get_any();
            if ((me != null) && (me.length > 0)) {
                for (final Iterator itChilds = me[0].getChildElements(); itChilds.hasNext();) {
                    final MessageElement child = (MessageElement) itChilds.next();
                    if (SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
                        final String receivedNextPage = child.getAttribute(SPConstants.LIST_ITEM_COLLECTION_POSITION_NEXT);
                        list.setNextPage(receivedNextPage);
                        SPDocument doc = null;
                        for (final Iterator itrchild = child.getChildElements(); itrchild.hasNext();) {
                            final MessageElement row = (MessageElement) itrchild.next();
                            doc = processListItemElement(row, list, null);
                            if (doc != null) {
                                doc.setFolderLevel(folderLevel);
                                listItems.add(doc);
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(listItems);
        LOGGER.info("found: " + listItems.size() + " Items in List/Library ["
                + list.getListURL() + "] at folderLevel [ " + folderLevel
                + " ]. ");

        return listItems;
    }

    /**
     * Used to get list items under a list. If a change token is specified, we
     * only get the changed items. If no change token is specified we get all
     * the items along with the list schema. CAML query that is used while
     * calling getListItemChangesSinceToken should only be used for items that
     * are returned and not for the changes. Because if the query stops any
     * itemID to be shown under the changes, web service returns wrong
     * information about the changes on that item. For example, a rename of ID 1
     * may be shown as deleted or so. Take care of this while making the first
     * call. If you are using a token to get the changes and the CAML query
     * specified may stop some element from getting shown, do not trust the
     * change info.
     *
     * @param list : List whose items is to be retrieved
     * @param lastItemID : serves as a base for incremental crawl
     * @param allWebs : A collection to store any webs, discovered as part of
     *            discovering list items. Foe example link sites are stored as
     *            list items.
     * @param folderLevel : indicates a folder level if we have to start from
     *            that folder level. This is possible when some folder has been
     *            restored.
     * @return the list of documents as {@link SPDocument}
     */
    public List<SPDocument> getListItemChangesSinceToken(final ListState list,
            final String lastItemID, final Set<String> allWebs,
            final String folderLevel) {
        final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
        if (list == null) {
            LOGGER.warning("Unable to get the list items because list is null");
            return listItems;
        }

        LOGGER.config("list: title [ " + list.getListTitle() + " ], URL [ "
                + list.getListURL() + " ], lastItemID [ " + lastItemID
                + " ], folderLevel [ " + folderLevel + " ]");

        if (sharepointClientContext == null) {
            LOGGER.warning("Unable to get the list items because client context is null");
            return listItems;
        }
        if (stub == null) {
            LOGGER.warning("Unable to get the list items because stub is null");
            return listItems;
        }

        final String listName = list.getPrimaryKey();
        final String viewName = "";
        final GetListItemChangesSinceTokenQuery query = new GetListItemChangesSinceTokenQuery();
        final GetListItemChangesSinceTokenViewFields viewFields = new GetListItemChangesSinceTokenViewFields();
        final GetListItemChangesSinceTokenQueryOptions queryOptions = new GetListItemChangesSinceTokenQueryOptions();
        String token = list.getChangeTokenForWSCall();

        // Set token as null If it is blank, because the web service expects so,
        // otherwise it fails.
        if ((token != null) && (token.trim().length() == 0)) {
            token = null;
        }

        final GetListItemChangesSinceTokenContains contains = null;
        GetListItemChangesSinceTokenResponseGetListItemChangesSinceTokenResult res = null;
        try {
            /*
             * If we are not getting the change token, that means we have not
             * yet traversed the list completely. In that case, we need to get
             * the documents only. It is assumed that the caller would have
             * populated the folders info by calling getFolderHierarchy(), which
             * will be required in case of content feed for sending delete
             * feeds. Though, if we have a change token, then we can not skip
             * folders because they can also be changed(rename/restored) which
             * may mean sending the child documents again.
             */
            if (token != null) {
                /*
                 * Do not use lastItemID in the query, if some folderlevel is
                 * specified. folderlevel signifies that we had stopped last
                 * time while getting restored items inside a folder level.
                 * Hence, LastItemID should be used in that context only i.e,
                 * when we again start discovering the items inside the restored
                 * folder.
                 */
                if ((folderLevel == null) || (folderLevel.trim().length() == 0)
                        || (folderLevel.indexOf(SPConstants.SLASH) == -1)) {
                    query.set_any(createQuery3(lastItemID));
                } else {
                    query.set_any(createQuery3("0"));
                }
            } else {
                query.set_any(createQuery2(lastItemID));
            }

            viewFields.set_any(createViewFields());

            // Though, we have received a folderLevel, we'll not use it here
            // because that will make the WS returning incorrect change info.
            queryOptions.set_any(createQueryOptions(true, null, null));
            LOGGER.log(Level.CONFIG, "Making Web Service call with the following parameters:\n query [ "
                    + query.get_any()[0]
                    + " ], queryoptions [ "
                    + queryOptions.get_any()[0]
                    + " ], viewFields [ "
                    + viewFields.get_any()[0] + "], token [ " + token + " ] ");
            res = stub.getListItemChangesSinceToken(listName, viewName, query, viewFields, rowLimit, queryOptions, token, contains);
        } catch (final AxisFault af) { // Handling of username formats for
            // different authentication models.
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    res = stub.getListItemChangesSinceToken(listName, viewName, query, viewFields, rowLimit, queryOptions, token, null);
                } catch (final Exception e) {
                    handleListException(list, e);
                    return listItems;
                }
            } else {
                handleListException(list, af);
                return listItems;
            }
        } catch (final Throwable e) {
            handleListException(list, e);
            return listItems;
        }

        final Set<String> deletedIDs = new HashSet<String>();
        final Set<String> restoredIDs = new HashSet<String>();
        final Set<String> renamedIDs = new HashSet<String>();
        String lastChangeToken = null;

        if (res != null) {
            final MessageElement[] me = res.get_any();
            if ((me != null) && (me.length > 0)) {
                boolean inSequence = false; // To ensure that Changes are
                // retrieved always before actual
                // data.
                for (final Iterator itChilds = me[0].getChildElements(); itChilds.hasNext();) {
                    final MessageElement child = (MessageElement) itChilds.next();
                    if (SPConstants.CHANGES.equalsIgnoreCase(child.getLocalName())) {
                        inSequence = true;
                        lastChangeToken = processListChangesElement(child, list, deletedIDs, restoredIDs, renamedIDs, lastItemID, folderLevel);
                    } else if (SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
                        if (!inSequence) {
                            LOGGER.log(Level.SEVERE, "Bad Sequence.");
                        }
                        listItems.addAll(processListDataElement(child, list, deletedIDs, restoredIDs, renamedIDs, allWebs, lastItemID, folderLevel));
                    }
                }
            }
        }

        LOGGER.info("found: " + listItems.size() + " Items in List/Library ["
                + list.getListURL() + "] for feed action=ADD");

        // Process deleted IDs
        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
            try {
                listItems.addAll(processDeletedItems(deletedIDs, list));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Problem while constructing delete feeds..", e);
            }
        }

        if (null != lastChangeToken) {
            if(
            // FIRST CASE: This is the first change token we have received for
            // the first call to WS. We need to save this change token because
            // this will define the starting point for incremental crawl
            // once the initial crawl gets completed. This is important because
            // by the time we complete the initial crawl, web service would
            // returning a new change token which would be valid at that point.
            // If we use that token, we will miss any changes that have happened
            // while the initial crawl was was in progress.
            list.isCurrentChangeTokenBlank() && list.isNextChangeTokenBlank()

                    // Or, SECOOND CASE: If connector is running in an
                    // incremental crawl and all the documents are crawled for
                    // the current change token
                    || (!list.isCurrentChangeTokenBlank() && null == list.getNextPage()))
            {
                list.saveNextChangeTokenForWSCall(lastChangeToken);
            }
        }


        /*
         * If we had received a numeric lastItemID, we must send at list one doc
         * to keep the traversal on and update the state info about the complete
         * traversal.
         */
        if (Util.isNumeric(lastItemID) && (listItems.size() == 0)) {
            list.setNewList(true);
        }

        Collections.sort(listItems);

        return listItems;
    }

    /**
     * Called when the connector is not able proceed after the current state of
     * list (lastDoc+changetoken) because the web service call has failed.
     * @param list List for which the exception occured
     */
    private void handleListException(final ListState list, Throwable te) {
        if (te == null) {
            return;
        }
        // As a quick fix, marking the list as partially crawled. Crawl will not proceed for the given list
        LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                + list.getListURL()
                + " ]. The list's state can not be updated and the crawl will not proceed for this list!!", te);
        list.setNewList(false); // This will ensure that list will
                                // not be sent as document and hence
                                // can not be assumed completed.

        // TODO: With the above logic, connector will get stuck at the current
        // state of the list where the web service call has failed and
        // will proceed only when the web service call can succeed.
        // Better solutions for this is being worked out. We should
        // recover from the exception gracefully and proceed with the
        // crawl. Any problematic document should be skipped and list's
        // state should be appropriately updated.
        if (te.getMessage().indexOf(SPConstants.SAXPARSEEXCEPTION) != -1) {
            LOGGER.log(Level.WARNING, "Could not parse the web service SOAP response for list [ "
                    + list.getListURL()
                    + " ]. This could happen becasue of invalid XML chanracters in the web service response. "
                    + "Check if any of your document's metadata has such characters in it. ");
        }
    }
    /**
     * Construct SPDocument for all those items which has been deleted.
     *
     * @param deletedIDs : all the item IDs for which delete feed is to be
     *            constructed.
     * @param list : Base Lists
     * @return the list of documents as {@link SPDocument}
     */
    private List<SPDocument> processDeletedItems(final Set<String> deletedIDs,
            final ListState list) {
        final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
        int count = 0;
        final List<String> allDeletedIDs = new ArrayList<String>(deletedIDs);

        if (allDeletedIDs.size() < 1) {
            return listItems;
        }

        int threashold = 1000;
        try {
            threashold = Integer.parseInt(rowLimit);
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Unable to parse rowlimit [ " + rowLimit
                    + " ] for numeric value", e);
        }

        Collections.sort(allDeletedIDs);
        LOGGER.log(Level.FINE, "Constructing delete feeds for IDs "
                + allDeletedIDs + ". ");
        for (String currentItemID : deletedIDs) {
            if (list.isInDeleteCache(currentItemID)) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Skipping document with original ID: "
                            + currentItemID
                            + " from list : "
                            + list.getListURL());
                }
                continue;
            }
            if (count > threashold) {
                if (null == list.getNextPage()) {
                    list.setNextPage("not null");
                }
                break;
            }
            String docID = list.getListURL() + SPConstants.DOC_TOKEN
                    + currentItemID;
            final SPDocument doc = new SPDocument(docID, list.getListURL(),
                    list.getLastModCal(), SPConstants.NO_AUTHOR,
                    SPConstants.OBJTYPE_LIST_ITEM,
                    list.getParentWebState().getTitle(),
                    sharepointClientContext.getFeedType(),
                    list.getParentWebState().getSharePointType());
            doc.setAction(ActionType.DELETE);
            listItems.add(doc);
            count++;

            // If this list can contain attachments, assume that each item had
            // attachments and send delete feed for them.
            if (list.canContainAttachments()) {
                final List<String> attachments = list.getAttachmntURLsFor(currentItemID);
                final String originalDocID = docID;
                for (String attchmnt_url : attachments) {
                    docID = SPConstants.ATTACHMENT_SUFFIX_IN_DOCID + "["
                            + attchmnt_url + "]" + originalDocID;
                    final SPDocument attchmnt = new SPDocument(docID,
                            attchmnt_url, list.getLastModCal(),
                            SPConstants.NO_AUTHOR,
                            SPConstants.OBJTYPE_ATTACHMENT,
                            list.getParentWebState().getTitle(),
                            sharepointClientContext.getFeedType(),
                            list.getParentWebState().getSharePointType());
                    attchmnt.setAction(ActionType.DELETE);
                    listItems.add(attchmnt);
                    count++;
                }
            }
        }
        LOGGER.info("found: " + count + " Items in List/Library ["
                + list.getListURL() + "] for feed action=DELETE");

        return listItems;
    }

    /**
     * Process the rs:changes element as returned by
     * getListItemChangesSinceToken.
     *
     * @param changeElement : The root child node that contains all the changes
     * @param list : Base LIst
     * @param deletedIDs : Set of deleted IDs. Delete feed will be constructed
     *            for them.
     * @param restoredIDs : Set of restored IDs. New feeds are sent for these
     *            items.
     * @param renamedIDs : If it is a folder. New feeds are sent for all the
     *            items beneath it.
     * @param lastItemID : Serves as a base for incremental crawl.
     * @param folderLevel : If some folder level is specified, we will ignore
     *            the changes. This becasue in such cases the change info
     *            returned by the WS are not consistent.
     * @return the change token being received as per the WS call
     */
    private String processListChangesElement(
            final MessageElement changeElement, final ListState list,
            final Set<String> deletedIDs, final Set<String> restoredIDs,
            final Set<String> renamedIDs, final String lastItemID,
            final String folderLevel) {
        final String lastChangeToken = changeElement.getAttributeValue(SPConstants.LASTCHANGETOKEN);
        LOGGER.log(Level.FINE, "Change Token Recieved [ " + lastChangeToken
                + " ]. ");
        if (lastChangeToken == null) {
            LOGGER.log(Level.SEVERE, "No Change Token Found in the Web Service Response !!!! "
                    + "The current change token might have become invalid; please check the Event Cache table of SharePoint content database.");
        }

        for (final Iterator itrchild = changeElement.getChildElements(); itrchild.hasNext();) {
            try {
                final MessageElement change = (MessageElement) itrchild.next();
                if (SPConstants.LIST.equalsIgnoreCase(change.getLocalName())) {
                    list.setNewList(true);
                }

                final String changeType = change.getAttributeValue(SPConstants.CHANGETYPE);
                final String itemId = change.getValue();
                if ((null == changeType) || (null == itemId)) {
                    continue;
                }
                /*
                 * Ensure that the changed itemId is bigger then the received
                 * lastItemID. This is required because
                 * getListItemChnagesSinceToken always sends change info,
                 * irrespective of any conditions specified in the CAML query.
                 *
                 * Also, since we do not use lastItemID in the CAML query when
                 * some folderLevel is specified. Do the above check only if no
                 * folderLevel has been specified.
                 */
                if ((folderLevel == null) || (folderLevel.trim().length() == 0)
                        || (folderLevel.indexOf(SPConstants.SLASH) == -1)) {
                    try {
                        if (Integer.parseInt(itemId) <= Integer.parseInt(lastItemID)) {
                            LOGGER.log(Level.WARNING, "This ItemId [ "
                                    + itemId
                                    + " ] has already been tracked during change detection. skipping...");
                            continue;
                        }
                    } catch (final Exception e) {
                        // lastItemID is of the list itself.
                    }
                }
                if (SPConstants.DELETE.equalsIgnoreCase(changeType)) {
                    if (FeedType.CONTENT_FEED != sharepointClientContext.getFeedType()) {
                        // Delete feed processing is done only in case of
                        // content feed
                        continue;
                    }
                    if (list.isInDeleteCache(itemId)) {
                        // We have already processed this.
                        LOGGER.log(Level.WARNING, "skipping deleted ItemID ["
                                + itemId
                                + "] because it has been found in deletedCache. We have already sent delete feeds for this. listURL ["
                                + list.getListURL() + " ]. ");
                        continue;
                    }
                    LOGGER.log(Level.INFO, "ItemID ["
                            + itemId
                            + "] has been deleted. Delete feeds will be sent for this and all the dependednt IDs. listURL ["
                            + list.getListURL() + " ]. ");
                    try {
                        deletedIDs.addAll(list.getExtraIDs(itemId));
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Problem occured while getting the dependent IDs for deleted ID [ "
                                + itemId
                                + " ]. listURL ["
                                + list.getListURL()
                                + " ]. ");
                    }
                } else if (SPConstants.RESTORE.equalsIgnoreCase(changeType)) {
                    restoredIDs.add(itemId);
                    LOGGER.log(Level.INFO, "ItemID ["
                            + itemId
                            + "] has been restored. ADD feeds will be sent for this and all the dependednt IDs. listURL ["
                            + list.getListURL() + " ]. ");
                    // Remove this ID from the list of deleted IDs. This is
                    // important or else you might have orphaned docs in GSA's
                    // index. There are various use cases where this can happen.
                    // Like you dont remove the ID from list of deleted IDs now,
                    // send an ADD feed and later send try to send DELETE feed.
                    // Here this new delete feed will never be sent as the ID is
                    // already marked in the list of deleted ids. Think of many
                    // such use cases and this step will then make sense
                    list.removeFromDeleteCache(itemId);
                } else if (SPConstants.RENAME.equalsIgnoreCase(changeType)) {
                    renamedIDs.add(itemId);
                    LOGGER.log(Level.INFO, "ItemID ["
                            + itemId
                            + "] has been renamed. ADD feeds will be sent for this and all the dependednt IDs. listURL ["
                            + list.getListURL() + " ]. ");
                }
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Problem occured while parsing the Changes node", e);
                continue;
            }
        }

        return lastChangeToken;
    }

    /**
     * Processing of rs:data element as returned by
     * getListItemChangesSinceToken.
     *
     * @param dataElement : represents the parent node which contains all the
     *            list items node.
     * @param list : Base lIst
     * @param deletedIDs : Set of deleted IDs. Delete feed will be constructed
     *            for them.
     * @param restoredIDs : Set of restored IDs. New feeds are sent for these
     *            items.
     * @param renamedIDs : If it is a folder. New feeds are sent for all the
     *            items beneath it.
     * @param lastItemID : Serves as a base for incremental crawl.
     * @param folderLevel : If some folder level is specified, we will ignore
     *            the changes. This becasue in such cases the change info
     *            returned by the WS are not consistent.
     * @return the list of documents as {@link SPDocument}
     */
    private List<SPDocument> processListDataElement(
            final MessageElement dataElement, final ListState list,
            final Set<String> deletedIDs, final Set<String> restoredIDs,
            final Set<String> renamedIDs, final Set<String> allWebs,
            String lastItemID, final String folderLevel) {
        final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
        final ArrayList<MessageElement> updatedListItems = new ArrayList<MessageElement>();

        final String receivedNextPage = dataElement.getAttribute(SPConstants.LIST_ITEM_COLLECTION_POSITION_NEXT);
        LOGGER.log(Level.FINE, "Next Page Recieved [ " + receivedNextPage
                + " ]. ");
        /*
         * One may think of using nextPage as the backbone of page by page
         * crawling when threshold is reached. This definitely seems to be a
         * simple and straight way. But, ListItemCollectionPositionNext is not
         * very reliable. I have tested this with a volume site of around 6000
         * docs, where all the docs were under a very complex folder hierarchy.
         * The observation was "ListItemCollectionPositionNext does not actually
         * remember the pages it has returned when the docs are under a very
         * complex hierarchy of folder and very large in numbers. Hence, at the
         * completion of all the docs, when it is expected that
         * ListItemCollectionPositionNext should then be returned as null, it
         * does not happen so. Instead, ListItemCollectionPositionNext keeps
         * recrawling the same set of document again and again."
         */
        final int intRowLimit = Integer.parseInt(rowLimit);
        for (final Iterator itrchild = dataElement.getChildElements(); itrchild.hasNext();) {
            try {
                final MessageElement row = (MessageElement) itrchild.next();
                final String docId = row.getAttribute(SPConstants.ID);
                if (null == docId) {
                    LOGGER.log(Level.WARNING, "Skipping current rs:data node as docID is not found. listURL [ "
                            + list.getListURL() + " ]. ");
                    continue;
                }
                if (list.canContainFolders()) {
                    String contentType = row.getAttribute(SPConstants.CONTENTTYPE);
                    if (contentType == null) {
                        contentType = row.getAttribute(SPConstants.CONTENTTYPE_INMETA);
                    }
                    String relativeURL = row.getAttribute(SPConstants.FILEREF);

                    LOGGER.log(Level.CONFIG, "docID [ " + docId
                            + " ], relativeURL [ " + relativeURL
                            + " ], contentType [ " + contentType + " ]. ");

                    if(null == relativeURL) {
                        LOGGER.log(Level.WARNING, "No relativeURL (FILEREF) attribute found for the document, docID [ "
                                + docId
                                + " ], listURL [ "
                                + list.getListURL() + " ]. ");
                    } else if (null == contentType) {
                        LOGGER.log(Level.WARNING, "No content type found for the document, relativeURL [ "
                                + relativeURL
                                + " ], listURL [ "
                                + list.getListURL() + " ]. ");
                    } else {
                        relativeURL = relativeURL.substring(relativeURL.indexOf(SPConstants.HASH) + 1);
                        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                            /*
                             * Since we have got an entry for this item, this
                             * item can never be considered as deleted.
                             * Remember, getListItemChangesSinceToken always
                             * return the changes, irrespective of any
                             * conditions specified in the CAML query. And, if
                             * for any change the conditions becomes false, the
                             * change details returned for this item may be
                             * misleading. For Example, if item 1 is renamed,
                             * and in query we have asked to return only those
                             * items whose ID is greater then 1; Then in that
                             * case, the WS may return change info as delete
                             * along with rename for item 1.
                             */
                            deletedIDs.remove(docId);
                            list.removeFromDeleteCache(docId);

                            if (contentType.equalsIgnoreCase(SPConstants.CONTENT_TYPE_FOLDER)) {
                                try {
                                    list.updateExtraIDs(relativeURL, docId, true);
                                } catch (SharepointException se1) {
                                    // Try again after updating the folders
                                    // info.
                                    // Because, the folder might have been renamed.
                                    LOGGER.log(Level.WARNING, "Problem while updating relativeURL [ "
                                            + relativeURL
                                            + " ], listURL [ "
                                            + list.getListURL()
                                            + " ]. Retrying after updating the folders info.. ", se1.getMessage());
                                    getFolderHierarchy(list, null, null);
                                    try {
                                        list.updateExtraIDs(relativeURL, docId, true);
                                    } catch (SharepointException se2) {
                                        LOGGER.log(Level.WARNING, "Problem while updating relativeURL [ "
                                                + relativeURL
                                                + " ], listURL [ "
                                                + list.getListURL() + " ]. ", se2);
                                    }
                                }
                            }
                        }

                        if (contentType.equalsIgnoreCase(SPConstants.CONTENT_TYPE_FOLDER)) {

                            // When a folder is restored / renamed, we need to
                            // send new feeds for all the items which are
                            // beneath it.
                            if (restoredIDs.contains(docId)
                                    || renamedIDs.contains(docId)) {
                                /*
                                 * The following conditions checks if the
                                 * lastCrawledDoc info is of a top level
                                 * document or of a document which is inside a
                                 * folder, which might have been restored. Since
                                 * we always send the restored items first, if
                                 * we are getting the lastCrawledDoc info at
                                 * some folder level, this means we had not done
                                 * with getting all the restored items. In that
                                 * case only we can proceed to get the restored
                                 * items.
                                 */
                                if ((Util.isNumeric(lastItemID) && ((folderLevel == null) || (folderLevel.trim().length() == 0)))) {
                                    // We have processed this in previous
                                    // cycles.
                                    restoredIDs.remove(docId);
                                    renamedIDs.remove(docId);
                                    continue;
                                }

                                LOGGER.log(Level.INFO, "Processing the renamed/restored folder ID["
                                        + docId
                                        + "], relativeURL["
                                        + relativeURL + "] ");
                                final String folderPath = Util.getFolderPathForWSCall(list.getParentWebState().getWebUrl(), relativeURL);
                                if (folderPath == null) {
                                    continue;
                                }

                                LOGGER.log(Level.INFO, "Getting folder hierarchy at folder level [ "
                                        + folderPath + " ]. ");
                                final List<String> folderLevels = getFolderHierarchy(list, folderPath, null);

                                /*
                                 * If in the last cycle, we have stopped
                                 * somewhere in between while getting the
                                 * restored item, let's first complete that
                                 * hierarchy. Or, start from the first, it's the
                                 * next item which has been restored.
                                 */
                                int folderLevelPos = folderLevels.indexOf(folderLevel);
                                if (folderLevelPos == -1) {
                                    folderLevelPos = 0;
                                    lastItemID = "0";
                                }

                                for (int index = folderLevelPos; (index < folderLevels.size()); ++index) {
                                    if (listItems.size() >= intRowLimit) {
                                        // If there are more folders to be
                                        // crawled, and batch-hint is reached,
                                        // then ensure that nextPage is not null
                                        if (null == list.getNextPage()) {
                                            list.setNextPage("not null");
                                        }
                                        break;
                                    }
                                    final List<SPDocument> restoredItems = getListItemsAtFolderLevel(list, lastItemID, (String) folderLevels.get(index));
                                    listItems.addAll(restoredItems);
                                }

                                LOGGER.log(Level.INFO, "Discovered #"
                                        + listItems.size()
                                        + " under the restored/renamed folder ["
                                        + relativeURL + "] ");
                            }
                            continue; // do not send folders as documents.
                        }
                    }
                } // End -> if(list.isDocumentLibrary())

                /*
                 * Do not process list items i.e, rs:rows here. This is because,
                 * we need to process the renamed/restored folders cases first.
                 * If we'll not reach the batch hint with such documents then
                 * only we'll process the updated items.
                 */
                updatedListItems.add(row);

            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Problem occured while parsing the rs:data node", e);
                continue;
            }
        } // end of For

        // Process the list items which WS returns as rs:rows
        if (listItems.size() < intRowLimit) {
            for (Object element : updatedListItems) {
                final MessageElement row = (MessageElement) element;
                final SPDocument doc = processListItemElement(row, list, allWebs);
                if (doc != null) {
                    listItems.add(doc);
                }
            }

            // If in case while getting the items under renamed/restored folder,
            // we have changed the nextPage value to null, let's get the
            // original value back.
            if (list.getNextPage() == null) {
                list.setNextPage(receivedNextPage);
            }
        } else if (updatedListItems.size() > 0) {
            // Since, we have not processed the updated items yet, nextPage must
            // explicitly be set non-null to indicate further processing is
            // required with the same Change Token
            list.setNextPage("not null");
        }

        return listItems;
    }

    /**
     * Parses a typical &ltz:row..&gt node which are returned by SharePoint web
     * services for individual documents/listItems.
     *
     * @param listItem : The one rs:rows node to be parsed.
     * @param list : Base LIst
     * @param allWebs : To Store the link sites.
     * @return the constructed {@link SPDocument} from the message element
     *         returned by the WS
     */
    private SPDocument processListItemElement(final MessageElement listItem,
            final ListState list, final Set<String> allWebs) {
        // Get all the required attributes.
        String fileref = listItem.getAttribute(SPConstants.FILEREF);
        if (fileref == null) {
            LOGGER.log(Level.WARNING, SPConstants.FILEREF
                    + " is not found for one of the items in list [ "
                    + list.getListURL() + " ]. ");
        } else {
            fileref = fileref.substring(fileref.indexOf(SPConstants.HASH) + 1);
        }

        final String lastModified = listItem.getAttribute(SPConstants.MODIFIED);
        String strObjectType = listItem.getAttribute(SPConstants.CONTENTTYPE);
        String fileSize = listItem.getAttribute(SPConstants.FILE_SIZE_DISPLAY);

        if (fileSize == null) {
            // Check with the other file size attribute as back-up
            fileSize = listItem.getAttribute(SPConstants.FILE_SIZE);
        }

        String author = listItem.getAttribute(SPConstants.EDITOR);
        if (author == null) {
            author = listItem.getAttribute(SPConstants.AUTHOR);
        }
        String docId = listItem.getAttribute(SPConstants.ID);
        final Iterator itAttrs = listItem.getAllAttributes();

        // Start processing based on the above read attributes.

        // STEP1: Process link sites
        if (list.isLinkSite() && (allWebs != null)) {
            String linkSiteURL = listItem.getAttribute(SPConstants.URL);// e.g.
            // http://www.abc.com,
            // abc
            // site"
            if (linkSiteURL == null) {
                LOGGER.log(Level.WARNING, "Unable to get the link URL");
            } else {
                // filter out description
                linkSiteURL = linkSiteURL.substring(0, linkSiteURL.indexOf(SPConstants.COMMA));
                LOGGER.config("Linked Site / Site Directory URL :"
                        + linkSiteURL);
                if (sharepointClientContext.isIncludedUrl(linkSiteURL)) {
                    allWebs.add(linkSiteURL);
                } else {
                    LOGGER.warning("excluding " + linkSiteURL.toString());
                }
            }
        }

        // STEP2: Create SPDocument for found entity.
        if (SPConstants.CONTENT_TYPE_FOLDER.equalsIgnoreCase(strObjectType)) {
            LOGGER.log(Level.WARNING, SPConstants.CONTENTTYPE
                    + " is folder. Returning as we do not send folders as documents. ");
            return null;
        }

        if (docId == null) {
            LOGGER.log(Level.WARNING, SPConstants.ID
                    + " is not found for one of the items in list [ "
                    + list.getListURL() + " ]. ");
            return null;
        }

        final StringBuffer url = new StringBuffer();
        if (list.isDocumentLibrary()) {
            if (fileref == null) {
                return null;
            }
            /*
             * An example of ows_FileRef is 1;#unittest/Shared
             * SPDocuments/sync.doc We need to get rid of 1;# so that the
             * document URL can be constructed.
             */
            final String urlPrefix = Util.getWebApp(sharepointClientContext.getSiteURL());
            url.setLength(0);
            url.append(urlPrefix);
            url.append(SPConstants.SLASH);
            url.append(fileref);
        } else {
            final String urlPrefix = Util.getWebApp(sharepointClientContext.getSiteURL());
            url.setLength(0);
            url.append(urlPrefix);
            url.append(SPConstants.SLASH);
            url.append(list.getListConst() + SPConstants.DISPFORM);
            url.append(docId);
        }

        LOGGER.config("URL :" + url);
        if (!sharepointClientContext.isIncludedUrl(url.toString())) {
            LOGGER.warning("excluding " + url.toString());
            return null;
        }
        LOGGER.config("included url : " + url.toString());

        SPDocument doc;

        if (strObjectType == null) {
            if (list.isDocumentLibrary()) {
                strObjectType = SPConstants.DOCUMENT;
            } else {
                strObjectType = SPConstants.OBJTYPE_LIST_ITEM;
            }
        }

        if (author == null) {
            author = SPConstants.NO_AUTHOR;
        } else {
            author = author.substring(author.indexOf(SPConstants.HASH) + 1); // e.g.1073741823;#System
            // Account
        }

        Calendar calMod;
        try {
            calMod = Value.iso8601ToCalendar(lastModified);
        } catch (final ParseException pe) {
            LOGGER.log(Level.INFO, "Unable to parse the document's last modified date vale. Using parent's last modified.");
            calMod = list.getLastModCal();
        }

        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
            docId = list.getListURL() + SPConstants.DOC_TOKEN + docId;
        }
        doc = new SPDocument(docId, url.toString(), calMod, author,
                strObjectType, list.getParentWebState().getTitle(),
                sharepointClientContext.getFeedType(),
                list.getParentWebState().getSharePointType());
        doc.setFileref(fileref);

        if (fileSize != null && !fileSize.equals("")) {
            try {
                doc.setFileSize(Integer.parseInt(fileSize));
            } catch (NumberFormatException nfe) {
                // Just log the message in case of errors.
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Problems while parsing the file size attribute", nfe.getMessage());
                }
            }
        } else if (LOGGER.isLoggable(Level.FINER)) {
            // Just log for any doc level debugging purposes
            LOGGER.finer("No file size attribute retrieved for document : "
                    + doc.getUrl());
        }

        // iterate through all the attributes get the atribute name and value
        if (itAttrs != null) {
            while (itAttrs.hasNext()) {
                final Object oneAttr = itAttrs.next();
                if (oneAttr != null) {
                    String strAttrName = oneAttr.toString();
                    if ((strAttrName != null)
                            && (!strAttrName.trim().equals(""))) {
                        String strAttrValue = listItem.getAttribute(strAttrName);
                        // Apply the well known rules of name resolution and
                        // normalizing the values
                        strAttrName = Util.normalizeMetadataName(strAttrName);
                        strAttrValue = Util.normalizeMetadataValue(strAttrValue);
                        if (sharepointClientContext.isIncludeMetadata(strAttrName)) {
                            doc.setAttribute(strAttrName, strAttrValue);
                        } else {
                            LOGGER.log(Level.FINE, "Excluding metadata name [ "
                                    + strAttrName + " ], value [ "
                                    + strAttrValue + " ] for doc URL [ " + url
                                    + " ]. ");
                        }
                    }
                }
            }
        }

        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
            try {
                final int id = Integer.parseInt(Util.getOriginalDocId(docId, sharepointClientContext.getFeedType()));
                if (id > list.getBiggestID()) {
                    list.setBiggestID(id);
                }
            } catch (final Exception e) {
                // Eatup the exception. This was just to ensure that this is a
                // list item. If it is not, then it will ba a case of list.
            }
        }
        return doc;
    }

    public List<SPDocument> parseCustomWSResponseForListItemNodes(
            final MessageElement wsElement, ListState list) {
        final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();

        if (null == wsElement
                || !SPConstants.GSSLISTITEMS.equals(wsElement.getNodeName())) {
            return listItems;
        }
        for (final Iterator itChilds = wsElement.getChildElements(); itChilds.hasNext();) {
            Object obj = itChilds.next();
            if (null == obj || !(obj instanceof MessageElement)) {
                continue;
            }
            try {
                final MessageElement row = (MessageElement) obj;
                final SPDocument doc = processListItemElement(row, list, null);
                listItems.add(doc);
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Problem occured while parsing node", e);
                continue;
            }
        }

        return listItems;
    }

    public List<SPDocument> parseCustomWSResponseForListItemNodes(String data,
            ListState list) {
        MessageElement wsElement = getMeFromString(data);
        return parseCustomWSResponseForListItemNodes(wsElement, list);
    }
}
