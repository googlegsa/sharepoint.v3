//Copyright 2007-2011 Google Inc.

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

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
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
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.wsclient.handlers.InvalidXmlCharacterHandler;
import com.google.enterprise.connector.sharepoint.wsclient.util.DateUtil;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeaderElement;

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
            // The web service time-out value
            stub.setTimeout(sharepointClientContext.getWebServiceTimeOut());
            LOGGER.fine("Set time-out of : "
                    + sharepointClientContext.getWebServiceTimeOut()
                    + " milliseconds");
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
                        + stub.getUsername() + " ]. Trying with " + username);
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
            }
        }

        int countDeleted = (null == knownAttachments) ? 0
                : knownAttachments.size();

        if (listAttachments.size() > 0 || countDeleted > 0) {
            LOGGER.log(Level.INFO, "Found " + listAttachments.size()
                    + " new/updated and " + countDeleted
                    + " deleted attachments for listItem [ "
                    + listItem.getUrl() + "]. ");
        } else {
            LOGGER.log(Level.CONFIG, "Found " + listAttachments.size()
                    + " new/updated and " + countDeleted
                    + " deleted attachments for listItem [ "
                    + listItem.getUrl() + "]. ");
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
            throws ParserConfigurationException, IOException, SAXException,
            ParseException {
        String date = null;
        boolean isList = false;

        // To ensure a properList ItemID...case of List where GUID is found
        try {
            Integer.parseInt(listItemID);
        } catch (final Exception e) {
            listItemID = "0";
            // change the query
            isList = true;
        }

        if (c != null) {
            date = DateUtil.calendarToIso8601(c, DateUtil.Iso8601DateAccuracy.SECS);
            LOGGER
                .config("The ISO 8601 date passed to WS for change detection of content modified since is: "
                    + date);
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
        final MessageElement[] meArray = { getMeFromString(strMyString) };
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
            throws ParserConfigurationException, IOException, SAXException {
        try {
            Integer.parseInt(listItemID);
        } catch (final Exception e) {
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
            throws ParserConfigurationException, IOException, SAXException {
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
            throws ParserConfigurationException, IOException, SAXException {
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
    MessageElement getMeFromString(final String strMyString)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final StringReader reader = new StringReader(strMyString);
        final InputSource inputsource = new InputSource(reader);
        Document doc = docBuilder.parse(inputsource);
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
            // recursively. Else, get items only at the given scope. Scope
            // argument has the higher priority here and will be checked first.
            if ((folderLevel != null) && (folderLevel.trim().length() > 0)) {
                me.addChildElement(new MessageElement(new QName("Folder"))).addTextNode(folderLevel);
                // folder information are by deafult returned by
                // getListItemChangesSinceToken, when folder scope is given.
                // Hence, no need to use "OptimizeFor" in this case.
            } else if (recursion) {
                me.addChildElement(new MessageElement(new QName(
                        "ViewAttributes"))).addAttribute(SOAPFactory.newInstance().createName("Scope"), "Recursive");

                // added for getting folder information when recursion is being
                // used, in case of getListItemChangesSinceToken.
                me.addChildElement(new MessageElement(new QName("OptimizeFor"))).addTextNode("ItemIds");
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

        if(listItems.size() >= Integer.parseInt(rowLimit)) {
          LOGGER.finer("At least rowlimit number of documents were found, so next page might exist, " 
            + " setting the next page value to non null. rowlimit = ["
            + rowLimit + "] listitemcount = [" + listItems.size() + "]");
          list.setNextPage("next page might exist so setting to - not null");
        }
        else {
          LOGGER.finer("Less than rowlimit number of documents were found, so next page does not exist," 
            + " leaving next page value unchanged. rowlimit = ["
            + rowLimit + "] listitemcount = [" + listItems.size() + "]");
        }        
        
        Collections.sort(listItems);
        if (listItems.size() > 0) {
            LOGGER.info("found: " + listItems.size()
                    + " Items in List/Library [" + list.getListURL() + "]");
        } else {
            LOGGER.config("No Items found in List/Library ["
                    + list.getListURL() + "]");
        }
        return listItems;
    }

    /**
     * Retrieves all the folder hierarchy from a given folder level and updates
     * the ExtraIDs of the list. This operation is independent of the batch hint
     * because the discovered folders are not sent as docs.
     *
     * @param list : Specify the base list
     * @param folder : From where to discover the folder hierarchy
     * @param lastID ; If we have already identified some folders at this
     *            folderLevel, specify the lastItemID to get the next set of
     *            folders.
     * @return the list of folders in this list
     */
    public List<Folder> getSubFoldersRecursively(final ListState list,
            final Folder folder, final String lastID) {
        List<Folder> folders = new ArrayList<Folder>();
        if (!list.canContainFolders()) {
            return folders;
        }

        String folderLevel = null;
        // TODO Check if the incoming folder should be added. This is probably
        // creating duplicate entries into the result.
        if (null != folder) {
            folders.add(folder);
            folderLevel = folder.getPath();
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
                        + stub.getUsername() + " ]. Trying with " + username);
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
                                        folders.add(new Folder(folderPath,
                                                docId));
                                    }
                                } else {
                                    folders.add(new Folder(folderPath, docId));
                                }
                            }
                        }
                        if (tmpNextPage != null) {
                            folders.addAll(getSubFoldersRecursively(list, folder, lastItemID));
                        }
                    }
                }
            }
        }

        // removing duplicate entries
        folders = new ArrayList<Folder>(new HashSet<Folder>(folders));

        Collections.sort(folders);
        return folders;
    }

    /**
     * Retrieves the list items only the specified level. This required when a
     * folder is restored and we need to discover items level by level.
     *
     * @param list : Base List
     * @param lastItemID : Last Item ID that we have already identified at this
     *            level.
     * @param folder : The folder from where to discover the items.
     * @return list of documents as {@link SPDocument}
     */
    public List<SPDocument> getListItemsAtFolderLevel(final ListState list,
            final String lastItemIdAtFolderLevel, final Folder currentFolder,
            final Folder renamedFolder) {
        final List<SPDocument> listItems = new ArrayList<SPDocument>();
        if (null == currentFolder) {
            return listItems;
        }
        final String listName = list.getPrimaryKey();
        final String viewName = "";
        final GetListItemsQuery query = new GetListItemsQuery();
        final GetListItemsViewFields viewFields = new GetListItemsViewFields();
        final GetListItemsQueryOptions queryOptions = new GetListItemsQueryOptions();
        final String webID = "";
        GetListItemsResponseGetListItemsResult res = null;

        try {
            LOGGER.fine("DocId for WS call : " + lastItemIdAtFolderLevel
                    + " folder path : " + currentFolder
                    + " for renamed folder " + renamedFolder);
            query.set_any(createQuery2(lastItemIdAtFolderLevel));
            viewFields.set_any(createViewFields());
            queryOptions.set_any(createQueryOptions(false, currentFolder.getPath(), null));
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
                    LOGGER.log(Level.WARNING, "Unable to get the List Items under folder [ "
                            + currentFolder + " ], list [ " + list
                            + " ]. ", e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Unable to get the List Items under folder [ "
                        + currentFolder + " ], list [ " + list
                        + " ]. ", af);
            }
        } catch (final Throwable e) {
            LOGGER.log(Level.WARNING, "Unable to get the List Items under folder [ "
                    + currentFolder + " ], list [ " + list + " ]. ", e);
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
                                doc.setRenamedFolder(renamedFolder);
                                doc.setParentFolder(currentFolder);
                                listItems.add(doc);
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(listItems);
        LOGGER.log(Level.FINE, "found " + listItems.size() + " under folder "
                + currentFolder);

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
     * @param folder : indicates that the last batch traversal stopped at some
     *            folder. This happens when folder(s) are renamed/restored. In
     *            such cases, web service response gives information about
     *            changed folders only and not the documents underneath.
     *            Connector manually visits each renamed/resored folder to
     *            discovers sub-folders and documents under it.
     * @return the list of documents as {@link SPDocument}
     */
    // FIXME Why using List and not Set?
    public List<SPDocument> getListItemChangesSinceToken(final ListState list,
            final Set<String> allWebs) throws SharepointException {
        final ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
        if (list == null) {
            LOGGER.warning("Unable to get the list items because list is null");
            return listItems;
        }

        LOGGER.config("list [ " + list + " ], LastDoc [ "
                + list.getLastDocForWSRefresh() + " ]");

        // if there are any folder/rename/restore found during previous WS call,
        // processed the folders first
        if (null != list.getNextChangeTokenForSubsequectWSCalls()) {
            traverseChangedFolders(list, listItems);
            if (listItems.size() >= sharepointClientContext.getBatchHint()) {
                Collections.sort(listItems);
                if (null == list.getNextPage()) {
                    // There might be more documents to be crawled using the
                    // current change token.
                    list.setNextPage("not null");
                }
                return listItems;
            }
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
            if (null == token) {
                String lastDocID = "0";
                if (null != list.getLastDocForWSRefresh()) {
                    lastDocID = Util.getOriginalDocId(list.getLastDocForWSRefresh().getDocId(), sharepointClientContext.getFeedType());
                }
                // During full crawl, crawl progresses using LastDocId
                query.set_any(createQuery2(lastDocID));
            } else {
                // During incremental crawl, crawl progresses using change token
                query.set_any(createQuery3("0"));
            }

            viewFields.set_any(createViewFields());
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
                        + stub.getUsername() + " ]. Trying with " + username);
                stub.setUsername(username);
                try {
                    res = stub.getListItemChangesSinceToken(listName, viewName, query, viewFields, rowLimit, queryOptions, token, null);
                } catch (final Exception e) {
                    handleListException(list, e);
                    return listItems;
                }
            } else {
                boolean retry = handleListException(list, af);
                if(retry) {
                    LOGGER.log(Level.INFO, "Retrying WS call...");
                    return getListItemChangesSinceToken(list, allWebs);
                }
                return listItems;
            }
        } catch (final Throwable e) {
            handleListException(list, e);
            return listItems;
        }

        final Set<String> deletedIDs = new HashSet<String>();
        final Set<String> restoredIDs = new HashSet<String>();
        final Set<String> renamedIDs = new HashSet<String>();
        List<MessageElement> updatedListItems = null;
        if (res != null) {
            final MessageElement[] me = res.get_any();
            if ((me != null) && (me.length > 0)) {
                // To ensure that Changes are accessed before documents
                boolean inSequence = false;
                for (final Iterator itChilds = me[0].getChildElements(); itChilds.hasNext();) {
                    final MessageElement child = (MessageElement) itChilds.next();
                    if (SPConstants.CHANGES.equalsIgnoreCase(child.getLocalName())) {
                        inSequence = true;
                        processListChangesElement(child, list, deletedIDs, restoredIDs, renamedIDs);
                    } else if (SPConstants.DATA.equalsIgnoreCase(child.getLocalName())) {
                        if (!inSequence) {
                            LOGGER.log(Level.SEVERE, "Bad Sequence.");
                        }
                        updatedListItems = processListDataElement(child, list, deletedIDs, restoredIDs, renamedIDs, allWebs);
                    }
                }
            }
        }

        // If some folder renames are found in WS response, handle it first.
        if (renamedIDs.size() > 0 || restoredIDs.size() > 0) {
            traverseChangedFolders(list, listItems);
        }

        if (listItems.size() < sharepointClientContext.getBatchHint()
                && null != updatedListItems) {
            for (Object element : updatedListItems) {
                final MessageElement row = (MessageElement) element;
                final SPDocument doc = processListItemElement(row, list, allWebs);
                if (doc != null) {
                    listItems.add(doc);
                }
            }
        } else if (null == list.getNextPage() && null != updatedListItems
                && updatedListItems.size() > 0) {
            list.setNextPage("not null");
        }

        // Process deleted IDs
        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
            try {
                listItems.addAll(processDeletedItems(deletedIDs, list));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Problem while constructing delete feeds..", e);
            }
        }

        if (listItems.size() > 0) {
            LOGGER.info("found " + listItems.size()
                    + " Items in List/Library [" + list.getListURL() + "] ");
        } else {
            LOGGER.config("No Items found in List/Library ["
                    + list.getListURL() + "]");

            // If there are no more documents to be sent from the current window
            // of (changetoken to nextchangeToken), the completion of this
            // window must be announced to the caller.
            if (null == list.getNextPage()
                    && null != list.getNextChangeTokenForSubsequectWSCalls()) {
                list.setNewList(true);
            }
        }

        Collections.sort(listItems);

        return listItems;
    }

    /**
     * Called when the connector is not able proceed after the current state of
     * list (lastDoc+changetoken) because the web service call has failed.
     *
     * @param list List for which the exception occurred
     * @return an advice to the caller indicating whether the web service call
     *         should be re-tried
     */
    private boolean handleListException(final ListState list, Throwable te) {
        LOGGER.log(Level.WARNING, "Unable to get the List Items for list [ "
                + list.getListURL() + " ]. ", te);

        if (te.getMessage().indexOf(SPConstants.SAXPARSEEXCEPTION) != -1) {
            boolean isNew = true;
            for (SOAPHeaderElement headerelem : stub.getHeaders()) {
                if (InvalidXmlCharacterHandler.PRECONDITION_HEADER.equals(headerelem)) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                stub.setHeader(InvalidXmlCharacterHandler.PRECONDITION_HEADER);
                LOGGER.log(Level.WARNING, "Web Service response seems to contain invalid XML characters. Retry with InvalidXmlCharacterHandler");
                return true;
            }
            LOGGER.log(Level.WARNING, "Could not parse the web service SOAP response for list [ "
                    + list.getListURL()
                    + " ]. This could happen becasue of invalid XML chanracters in the web service response. "
                    + "Check if any of your document's metadata has such characters in it. ");
        }

        // If nothing can be done to recover from this exception, at least
        // ensure that the crawl for this list will not proceed so that the user
        // would not get any false impression afterwards. Following will ensure
        // that list will not be sent as document and hence can not be assumed
        // completed.
        list.setNewList(false);

        return false;
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

        if (count > 0) {
            LOGGER.info("found: " + count + " Items in List/Library ["
                    + list.getListURL() + "] for feed action=DELETE");
        } else {
            LOGGER.config("No items foudn in List/Library ["
                    + list.getListURL() + "] for feed action=DELETE");
        }

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
     * @param folder : If some folder level is specified, we will ignore the
     *            changes. This because in such cases the change info returned
     *            by WS are not consistent.
     * @return the change token being received as per the WS call
     */
    private void processListChangesElement(
            final MessageElement changeElement, final ListState list,
            final Set<String> deletedIDs, final Set<String> restoredIDs,
            final Set<String> renamedIDs) throws SharepointException {
        final String lastChangeToken = changeElement.getAttributeValue(SPConstants.LASTCHANGETOKEN);
        LOGGER.log(Level.FINE, "Change Token Recieved [ " + lastChangeToken
                + " ]. ");
        if (lastChangeToken == null) {
            LOGGER.log(Level.SEVERE, "No Change Token Found in the Web Service Response !!!! "
                    + "The current change token might have become invalid; please check the Event Cache table of SharePoint content database.");
        }

        for (final Iterator itrchild = changeElement.getChildElements(); itrchild.hasNext();) {
            final MessageElement change = (MessageElement) itrchild.next();
            if (null == change) {
                continue;
            }

            if (SPConstants.LIST.equalsIgnoreCase(change.getLocalName())) {
                list.setNewList(true);
                break;
            }

            final String changeType = change.getAttributeValue(SPConstants.CHANGETYPE);
            if (null == changeType) {
                LOGGER.log(Level.WARNING, "Unknown change type! Skipping... ");
                continue;
            } else if (changeType.equalsIgnoreCase("InvalidToken")) {
                String ct = list.getChangeTokenForWSCall();
                list.resetState();
                throw new SharepointException(
                        "Current change token [ "
                                + ct
                                + " ] of List [ "
                                + list
                                + " ] has expired or is invalid. State of the list was reset to initiate a full crawl....");
            }

            final String itemId = change.getValue();
            if (null == itemId) {
                LOGGER.log(Level.WARNING, "Unknown ItemID for change type [ "
                        + changeType + " ] Skipping... ");
                continue;
            }

            LOGGER.config("Recieved change type as: " + changeType);

            if (SPConstants.DELETE.equalsIgnoreCase(changeType)) {
                if (FeedType.CONTENT_FEED != sharepointClientContext.getFeedType()) {
                    // Delete feed processing is done only in case of
                    // content feed
                    LOGGER.fine("Ignoring change type: " + changeType
                            + " since its applicable for content feed only");
                    continue;
                }
                if (list.isInDeleteCache(itemId)) {
                    // We have already processed this.
                    LOGGER.log(Level.WARNING, "skipping deleted ItemID ["
                            + itemId
                            + "] because it has been processed in previous batch traversal(s). listURL ["
                            + list.getListURL() + " ]. ");
                    continue;
                }
                LOGGER.log(Level.INFO, "ItemID ["
                        + itemId
                        + "] has been deleted. Delete feeds will be sent for this and all the dependednt IDs. listURL ["
                        + list.getListURL()
                        + " ] ");
                try {
                    deletedIDs.addAll(list.getExtraIDs(itemId));
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Problem occured while getting the dependent IDs for deleted ID [ "
                            + itemId
                            + " ]. listURL ["
                            + list.getListURL()
                            + " ]. ");
                }
            }
            // Folder rename/restore are tracked only when the current
            // change token is being used for the first time while making WS
            // call hence, the check
            // null == list.getNextChangeTokenForSubsequectWSCalls()
            else if (SPConstants.RESTORE.equalsIgnoreCase(changeType)
                    && null == list.getNextChangeTokenForSubsequectWSCalls()) {
                restoredIDs.add(itemId);
                LOGGER.log(Level.INFO, "ItemID ["
                        + itemId
                        + "] has been restored. ADD feeds will be sent for this and all the dependednt IDs. listURL ["
                        + list.getListURL()
                        + " ] ");
                // Since the item has been restored, it becomes a candidate for
                // deletion again. This has to be reflected here by removing
                // it from the delete cache.
                // FIXME The right way of handling this is to clear the delete
                // cache when a new change token is committed just like we do
                // for ListState.changedFolders
                list.removeFromDeleteCache(itemId);
            } else if (SPConstants.RENAME.equalsIgnoreCase(changeType)
                    && null == list.getNextChangeTokenForSubsequectWSCalls()) {
                renamedIDs.add(itemId);
                LOGGER.log(Level.INFO, "ItemID ["
                        + itemId
                        + "] has been renamed. ADD feeds will be sent for this and all the dependednt IDs. listURL ["
                        + list.getListURL()
                        + " ] ");
            }
        }

        list.saveNextChangeTokenForWSCall(lastChangeToken);
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
     * @param folder : If some folder level is specified, we will ignore the
     *            changes. This because in such cases the change info returned
     *            by WS are not consistent.
     * @return the list items which WS returns as rs:rows. These do not include
     *         folders
     */
    private List<MessageElement> processListDataElement(
            final MessageElement dataElement, final ListState list,
            final Set<String> deletedIDs, final Set<String> restoredIDs,
            final Set<String> renamedIDs, final Set<String> allWebs) {

        final ArrayList<MessageElement> updatedListItems = new ArrayList<MessageElement>();
        final String receivedNextPage = dataElement.getAttribute(SPConstants.LIST_ITEM_COLLECTION_POSITION_NEXT);
        LOGGER.log(Level.FINE, "Next Page Recieved [ " + receivedNextPage
                + " ]. ");
        list.setNextPage(receivedNextPage);
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
                                    getSubFoldersRecursively(list, null, null);
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
                            if (restoredIDs.contains(docId)
                                    || renamedIDs.contains(docId)) {
                                list.addToChangedFolders(new Folder(
                                        Util.getFolderPathForWSCall(list.getParentWebState().getWebUrl(), relativeURL),
                                        docId));
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

				if (!sharepointClientContext.isFeedUnPublishedDocuments()) {
					if (null != row.getAttribute(SPConstants.MODERATION_STATUS)) {
						int docVersion = Integer.parseInt(row.getAttribute(SPConstants.MODERATION_STATUS));
						if (docVersion != 0) {
							// Added unpublished documents to delete list if
							// FeedUnPublishedDocuments set to false, so
							// that connector send delete feeds for unpublished
							// content in SharePoint to GSA.
							deletedIDs.add(docId);
							LOGGER.warning("Adding the list item or document ["
									+ row.getAttribute(SPConstants.FILEREF)
									+ "] to the deleted ID's list to send delete feeds for unpublished content in the list URL :"
									+ list.getListURL());
						} else {
							// Add only published documents to the list to send
							// add feeds if FeedUnPublishedDocuments set to
							// false.
							updatedListItems.add(row);
						}
					}
				} else {
					// Add all published and unpublished content to the
					// updatedListItems to send add feeds if
					// feedUnPublishedDocuments set to true.
					updatedListItems.add(row);
				}
            } catch (final Exception e) {
                LOGGER.log(Level.WARNING, "Problem occured while parsing the rs:data node", e);
                continue;
            }
        } // end of For

        return updatedListItems;
    }

    private void traverseChangedFolders(final ListState list,
            final List<SPDocument> allItems) {
        SPDocument lastDocument = list.getLastDocForWSRefresh();
        if (null == lastDocument || null == allItems) {
            return;
        }

        final Folder lastDocParentFolder = lastDocument.getParentFolder();
        final Folder lastDocRenamedFolder = lastDocument.getRenamedFolder();

        if (null != lastDocRenamedFolder) {
            int index = list.getChangedFolders().indexOf(lastDocRenamedFolder);
            if (index > 0) {
                list.getChangedFolders().subList(0, index).clear();
            }
        }

        Iterator<Folder> itrChangedFolders = list.getChangedFolders().iterator();
        while (itrChangedFolders.hasNext()) {
            Folder changedFolder = itrChangedFolders.next();
            if (allItems.size() >= sharepointClientContext.getBatchHint()) {
                // more folders to traverse
                if (null == list.getNextPage()) {
                    list.setNextPage("not null");
                }
                break;
            }

            final List<Folder> folders = getSubFoldersRecursively(list, changedFolder, null);
            if (null != lastDocParentFolder) {
                int index = folders.indexOf(lastDocParentFolder);
                if (index > 0) {
                    folders.subList(0, index).clear();
                }
            }

            LOGGER.log(Level.INFO, "Processing renamed/restored folder ["
                    + changedFolder + "] ");

            int docCountFromCurrentFolder = 0;
            for (Folder currentFolder : folders) {
                if (allItems.size() >= sharepointClientContext.getBatchHint()) {
                    // More sub folders to be crawled
                    if (null == list.getNextPage()) {
                        list.setNextPage("not null");
                    }
                    break;
                }
                String lastDocIdForCurrentFolder = "0";
                if (null != lastDocParentFolder
                        && lastDocParentFolder.equals(currentFolder)) {
                    lastDocIdForCurrentFolder = Util.getOriginalDocId(lastDocument.getDocId(), sharepointClientContext.getFeedType());
                }
                List<SPDocument> currentListItems = getListItemsAtFolderLevel(list, lastDocIdForCurrentFolder, currentFolder, changedFolder);
                LOGGER.log(Level.CONFIG, "found " + currentListItems.size()
                        + " items under folder [" + currentFolder + " ] ");
                docCountFromCurrentFolder += currentListItems.size();
                allItems.addAll(currentListItems);
            }

            LOGGER.log(Level.INFO, "found " + docCountFromCurrentFolder
                    + " items under restored/renamed folder [" + changedFolder
                    + " ] ");

            if (null == list.getNextPage() && docCountFromCurrentFolder == 0) {
                itrChangedFolders.remove();
            }
        }
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
		if (!sharepointClientContext.isFeedUnPublishedDocuments()) {
			if (null != listItem.getAttribute(SPConstants.MODERATION_STATUS)) {
				int docVersion = Integer.parseInt(listItem.getAttribute(SPConstants.MODERATION_STATUS));
				if (docVersion != 0) {
					// ModerationStatus="0" for approved/ published list
					// list item or document status
					// ModerationStatus="1" for rejected list item or document status
					// ModerationStatus="2" for pending list item or document status
					// ModerationStatus="3" for draft list item or document status

                    LOGGER.warning("List Item or Document is not yet published on SharePoint site, hence discarding the ID ["
							+ listItem.getAttribute(SPConstants.ID)
							+ "] under the List/Document Library URL "
							+ list.getListURL()
							+ " , and it's current version is " + docVersion);
					return null;
				}
			} else {
				LOGGER.log(Level.WARNING, SPConstants.MODERATION_STATUS
						+ " is not found for one of the items in list or document library [ "
						+ list.getListURL() + " ]. ");
			}
		}
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
        String displayUrl = null;
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
            displayUrl = url.toString();
            if (list.isInfoPathLibrary()) {
                url.append("?");
                url.append(SPConstants.NOREDIRECT);
            }
        } else {
            final String urlPrefix = Util.getWebApp(sharepointClientContext.getSiteURL());
            url.setLength(0);
            url.append(urlPrefix);
            url.append(SPConstants.SLASH);
            url.append(list.getListConst() + SPConstants.DISPFORM);
            url.append(docId);
            displayUrl = url.toString();
            if (list.isInfoPathLibrary()) {
                url.append("&");
                url.append(SPConstants.NOREDIRECT);
            }
        }

        LOGGER.config("ListItem URL :" + url);
        if (!sharepointClientContext.isIncludedUrl(url.toString())) {
            LOGGER.warning("excluding " + url.toString());
            return null;
        }

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
			LOGGER.config("The ISO 8601 date received from WS for last modified is: "
					+ lastModified
              + " It will be stored in the snapshot and used for change detection.");
            calMod = DateUtil.iso8601ToCalendar(lastModified);
        } catch (final ParseException pe) {
            LOGGER.log(Level.WARNING, "Unable to parse the document's last modified date vale. Using parent's last modified.");
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
        doc.setDisplayUrl(displayUrl);

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
        MessageElement wsElement = null;
        try {
            wsElement = getMeFromString(data);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "", e);
        }
        return parseCustomWSResponseForListItemNodes(wsElement, list);
    }
}
