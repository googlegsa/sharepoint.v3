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

package com.google.enterprise.connector.sharepoint.spiimpl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

import com.google.enterprise.connector.sharepoint.client.Attribute;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.SpiConstants.RoleType;
import com.google.enterprise.connector.spiimpl.BinaryValue;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.StringValue;

/**
 * Class to hold data regarding a sharepoint document. Anything that is sent ot
 * GSA for indexing must be represented as an instance of this class.
 *
 * @author nitendra_thakur
 */
public class SPDocument implements Document, Comparable<SPDocument> {
    private String docId;
    private String url;
    private Calendar lastMod;
    private String author = SPConstants.NO_AUTHOR;
    private String objType = SPConstants.NO_OBJTYPE;
    private String parentWebTitle = "No Title";
    private FeedType feedType;
    private SPType spType;
    private ActionType action = ActionType.ADD;
    private String folderLevel;
    // to be used for updating extraId during checkpoint
    private String fileref = null;

    // Attributes which are set at the time when SPDOcumentList is constructed.
    // No assumptions should be made based on these attributes very early during
    // traversal.
    private ListState parentList;
    private WebState parentWeb;
    private String contentDwnldURL;
    private SharepointClientContext sharepointClientContext;

    // Attributes which are set when requested by CM during the findProperty
    // call.
    // No assumptions should be made based on these attributes very early during
    // traversal.
    private InputStream content = null;
    private String content_type = null;
    private int fileSize = -1;

    private final Logger LOGGER = Logger.getLogger(SPDocument.class.getName());

    /**
     * A guess as to how many attributes we should allow for initially.
     */
    // FIXME: Do we really need such guessing?
    private final int INITIALATTRLISTSIZE = 5;
    private final ArrayList<Attribute> attrs = new ArrayList<Attribute>(
            INITIALATTRLISTSIZE);

    /**
     * Flag to indicate if this document is to be sent as a feed
     */
    private boolean toBeFed = true;

    // List of users and their permissions to be sent in document's ACL
    private Map<String, List<RoleType>> usersAclMap;

    // List of groups and their permissions to be sent in document's ACL
    private Map<String, List<RoleType>> groupsAclMap;

    // Check if the documents is discovered from ACL based crawling. An ACL
    // based crawling happens when a security change occurs on site/list which
    // affects the ACL of many list items
    private boolean forAclChange = false;

    /**
     * @return the toBeFed
     */
    public boolean isToBeFed() {
        return toBeFed;
    }

    /**
     * @param toBeFed the toBeFed to set
     */
    public void setToBeFed(boolean toBeFed) {
        this.toBeFed = toBeFed;
    }

    public ListState getParentList() {
        return parentList;
    }

    public void setParentList(final ListState list) {
        parentList = list;
    }

    public WebState getParentWeb() {
        return parentWeb;
    }

    public void setParentWeb(final WebState web) {
        parentWeb = web;
    }

    /**
     * @param inDocId
     * @param inUrl
     * @param inLastMod
     * @param inAuthor
     * @param inObjType
     * @param inParentWebTitle
     * @param inFeedType
     * @param inSpType
     */
    public SPDocument(final String inDocId, final String inUrl,
            final Calendar inLastMod, final String inAuthor,
            final String inObjType, final String inParentWebTitle,
            final FeedType inFeedType, final SPType inSpType) {
        docId = inDocId;
        url = inUrl;
        lastMod = inLastMod;
        author = inAuthor;
        objType = inObjType;
        parentWebTitle = inParentWebTitle;
        feedType = inFeedType;
        spType = inSpType;
        LOGGER.config("docid[" + inDocId + "], URL[" + inUrl + "], LastMod["
                + inLastMod + "], ObjectType[" + inObjType + "]," + "author["
                + inAuthor + "],parentWebTitle[" + parentWebTitle
                + "], feedType [" + inFeedType + "], spType [" + inSpType
                + "] ");
    }

    /**
     * To be used while loading the lastDocument from the state file.
     */
    public SPDocument(final String inDocId, final String inDocURL,
            final Calendar inLastMod,
            final String inFolderLevel, final ActionType inAction) {
        docId = inDocId;
        url = inDocURL;
        lastMod = inLastMod;
        folderLevel = inFolderLevel;
        action = inAction;
    }

    /**
     * @return last modified date
     */
    public Calendar getLastMod() {
        return lastMod;
    }

    /**
     * @return last modified date as string
     */
    public String getLastDocLastModString() {
        try {
            return Util.formatDate(lastMod);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * @return document ID
     */
    public String getDocId() {
        return docId;
    }

    /**
     * @return document URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param strUrl
     */
    public void setUrl(final String strUrl) {
        if (url != null) {
            url = strUrl;
        }
    }

    /**
     * @return document properties
     */
    public ArrayList<Attribute> getAllAttrs() {
        return attrs;
    }

    // debug routine
    public void dumpAllAttrs() {
        if (attrs == null) {
            return;
        }

        for (Object element : attrs) {
            final Attribute attr = (Attribute) element;
            System.out.println(attr.getName() + "=" + attr.getValue());
        }
    }

    /**
     * Set an attribute which may not be one of the named ones listed above.
     *
     * @param key
     * @param value
     */
    public void setAttribute(final String key, final String value) {
        if (key != null) {
            attrs.add(new Attribute(key, value));
        }
    }

    /**
     * For setting document properties
     */
    public void setAllAttributes(final List<Attribute> lstAttributes) {
        if (lstAttributes != null) {
            attrs.addAll(lstAttributes);
        }
    }

    /**
     * @return author of the document
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return the document type
     */
    public String getObjType() {
        return objType;
    }

    /**
     * @param inAuthor
     */
    public void setAuthor(final String inAuthor) {
        if (inAuthor != null) {
            author = inAuthor;
        }
    }

    /**
     * @param inObjType
     */
    public void setObjType(final String inObjType) {
        if (inObjType != null) {
            objType = inObjType;
        }
    }

    /**
     * For SPDocument equality comparison
     */
    public boolean equals(final Object obj) {
        if (obj instanceof SPDocument) {
            final SPDocument doc = (SPDocument) obj;
            if (doc != null && docId != null && docId.equals(doc.getDocId())
                    && url != null && (url.equals(doc.getUrl()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * In case of SP2003, following fields are tried in order: lastModified, ID,
     * URL In case of SP2007, ID is used for ordering. Though, in case of
     * renamed/restored folders, items under the folders are sent in order of
     * the folder level. This is required because of the special handling folder
     * rename/restoration. We must ensure that all the items under a
     * renamed/restored folder are sent first for a given changeToken. Note:
     * folderLevel info is set for a doc only in case of parent folder
     * rename/restore. If the two docs one from SP2007 and another from SP2003
     * is compared, SP2007 will always get the prefrence.
     *
     * @param doc
     */
    public int compareTo(final SPDocument doc) {

        if (doc == null) {
            return -1;
        }

        if (SPType.SP2007.equals(getSPType())
                && SPType.SP2003 == doc.getSPType()) {
            return -1;
        } else if (SPType.SP2007 == doc.getSPType()
                && SPType.SP2003 == getSPType()) {
            return 1;
        }

        // If an item and its corresponding attachment is being compared, item
        // should be after the attachment.
        // This is because we want to send all the attachments before we send
        // the item itself.
        // This way, the item can serve as a marker for the completion of all
        // the attachments. Useful during checkpoint.
        if (SPConstants.OBJTYPE_ATTACHMENT.equals(objType)
                && SPConstants.OBJTYPE_LIST_ITEM.equals(doc.getObjType())
                && docId.endsWith(doc.getDocId())) {
            return -1;
        } else if (SPConstants.OBJTYPE_ATTACHMENT.equals(doc.getObjType())
                && SPConstants.OBJTYPE_LIST_ITEM.equals(objType)
                && doc.getDocId().endsWith(docId)) {
            return 1;
        }

        int comparison = 0;

        if (SPType.SP2007.equals(getSPType())) {
            if ((folderLevel != null) || (doc.folderLevel != null)) {
                if ((folderLevel != null) && (doc.folderLevel != null)
                        && (folderLevel.length() != 0)
                        && (doc.folderLevel.length() != 0)) {
                    comparison = folderLevel.compareTo(doc.folderLevel);
                } else if (((folderLevel == null) || (folderLevel.length() == 0))
                        && ((doc.folderLevel != null) && (doc.folderLevel.length() != 0))) {
                    return 1; // incoming doc should be sent before the current
                    // doc. We always send renamed/restored folder
                    // items first
                } else if (((folderLevel != null) && (folderLevel.length() != 0))
                        && ((doc.folderLevel == null) || (doc.folderLevel.length() == 0))) {
                    return -1; // current doc should be sent before the incoming
                    // doc. We always send renamed/restored folder
                    // items first
                }
            }
        } else {
            comparison = lastMod.getTime().compareTo(doc.lastMod.getTime());
        }

        if (comparison == 0) {
            final String tmpDocID1 = Util.getOriginalDocId(docId, feedType);
            final String tmpDocID2 = Util.getOriginalDocId(doc.docId, doc.feedType);
            int id1 = 0;
            int id2 = 0;
            try {
                id1 = Integer.parseInt(tmpDocID1);
            } catch (final Exception e) {
                return 1;
            }
            try {
                id2 = Integer.parseInt(tmpDocID2);
            } catch (final Exception e) {
                return -1;
            }
            comparison = id1 - id2;
            if (comparison != 0) {
                return comparison;
            }

            // compare the URLs
            String docURL1st = new String(url);
            String docURL2nd = new String(doc.url);
            try {
                docURL1st = URLDecoder.decode(docURL1st, "UTF-8");
                docURL2nd = URLDecoder.decode(docURL2nd, "UTF-8");
            } catch (final Exception e) {
                // eatup exception. Use the original URL...
            }
            if (docURL1st != null) {
                comparison = docURL1st.compareTo(docURL2nd);
            }

        }
        return comparison;
    }

    /**
     * Returns the property object for a given property name. CM calls this to
     * gather all the information about a document. The property names that are
     * requested can be either well known properties defined under connector SPI
     * or, connector can inform them pre-hand during the call to getAll
     * propertoes.
     */
    public Property findProperty(final String strPropertyName)
            throws RepositoryException {
        final Collator collator = Util.getCollator();
        if (collator.equals(strPropertyName, SpiConstants.PROPNAME_CONTENTURL)) {
            return new SPProperty(SpiConstants.PROPNAME_CONTENTURL,
                    new StringValue(getUrl()));
        } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_CONTENT)) {
            if (FeedType.CONTENT_FEED == getFeedType()
                    && ActionType.ADD.equals(getAction())) {
                if (null == content) {
                    String status = downloadContents();
                    if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
                        LOGGER.log(Level.WARNING, "Following response received while downloading contents: "
                                + status);
                    }
                }
                return new SPProperty(SpiConstants.PROPNAME_CONTENT,
                        new BinaryValue(content));
            }
        } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_MIMETYPE)) {
            if (FeedType.CONTENT_FEED == getFeedType()
                    && ActionType.ADD.equals(getAction())) {
                if (null == content) {
                    String status = downloadContents();
                    if (!SPConstants.CONNECTIVITY_SUCCESS.equalsIgnoreCase(status)) {
                        LOGGER.log(Level.WARNING, "Following response recieved while downloading contents: "
                                + status);
                    }
                }
                return new SPProperty(SpiConstants.PROPNAME_MIMETYPE,
                        new StringValue(content_type));
            }
        } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_SEARCHURL)) {
            if (FeedType.CONTENT_FEED != getFeedType()) {
                return new SPProperty(SpiConstants.PROPNAME_SEARCHURL,
                        new StringValue(getUrl()));
            }
        } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_DISPLAYURL)) {
            return new SPProperty(SpiConstants.PROPNAME_DISPLAYURL,
                    new StringValue(getUrl()));
        } else if (collator.equals(strPropertyName, SPConstants.PARENT_WEB_TITLE)) {
            return new SPProperty(SPConstants.PARENT_WEB_TITLE,
                    new StringValue(getParentWebTitle()));
        } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_DOCID)) {
            return new SPProperty(SpiConstants.PROPNAME_DOCID, new StringValue(
                    getDocId()));
        } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_LASTMODIFIED)) {
            return new SPProperty(SpiConstants.PROPNAME_LASTMODIFIED,
                    new DateValue(getLastMod()));
        } else if (collator.equals(strPropertyName, SPConstants.LIST_GUID)) {
            if (null != getParentList()) {
                return new SPProperty(SPConstants.LIST_GUID, new StringValue(
                        getParentList().getPrimaryKey()));
            }
        } else if (collator.equals(strPropertyName, SPConstants.SPAUTHOR)) {
            return new SPProperty(SPConstants.SPAUTHOR, new StringValue(
                    getAuthor()));
        } else if (strPropertyName.equals(SPConstants.OBJECT_TYPE)) {
            return new SPProperty(SPConstants.OBJECT_TYPE, new StringValue(
                    getObjType()));
        } else if (strPropertyName.equals(SpiConstants.PROPNAME_ISPUBLIC)) {
            return new SPProperty(SpiConstants.PROPNAME_ISPUBLIC,
                    BooleanValue.makeBooleanValue(false));
        } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACTION)) {
            return new SPProperty(SpiConstants.PROPNAME_ISPUBLIC,
                    new StringValue(getAction().toString()));
        } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACLUSERS)) {
            List<Value> values = new ArrayList<Value>(usersAclMap.size());
            for (String user : usersAclMap.keySet()) {
                values.add(Value.getStringValue(user));
            }
            return new SimpleProperty(values);
        } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACLGROUPS)) {
            List<Value> values = new ArrayList<Value>(groupsAclMap.size());
            for (String group : groupsAclMap.keySet()) {
                values.add(Value.getStringValue(group));
            }
            return new SimpleProperty(values);
        } else if (strPropertyName.startsWith(SpiConstants.USER_ROLES_PROPNAME_PREFIX)) {
            String originalName = strPropertyName.substring(SpiConstants.USER_ROLES_PROPNAME_PREFIX.length());
            List<RoleType> roleTypes = usersAclMap.get(originalName);
            List<Value> values = new ArrayList<Value>(roleTypes.size());
            for (RoleType roleType : roleTypes) {
                values.add(Value.getStringValue(roleType.toString()));
            }
            return new SimpleProperty(values);
        } else if (strPropertyName.startsWith(SpiConstants.GROUP_ROLES_PROPNAME_PREFIX)) {
            String originalName = strPropertyName.substring(SpiConstants.GROUP_ROLES_PROPNAME_PREFIX.length());
            List<RoleType> roleTypes = groupsAclMap.get(originalName);
            List<Value> values = new ArrayList<Value>(roleTypes.size());
            for (RoleType roleType : roleTypes) {
                values.add(Value.getStringValue(roleType.toString()));
            }
            return new SimpleProperty(values);
        }
        // FIXME: We can get rid of this if-else-if ladder here by setting all
        // the relevant properties (in appropriate type) right at the time of
        // document creation. After doing that, all that will be required is to
        // maintain a map of all the properties with key as the prop name. This
        // will also eliminate maintaining multiple member attributes in this
        // class. All the attribute will be there in a common map.
        else {
            for (final Iterator<Attribute> iter = getAllAttrs().iterator(); iter.hasNext();) {
                final Attribute attr = (Attribute) iter.next();
                if (collator.equals(strPropertyName, attr.getName())) {
                    return new SPProperty(strPropertyName, new StringValue(
                            attr.getValue().toString()));
                }
            }
        }

        LOGGER.finer("no matches found for[" + strPropertyName + "]");
        return null;// no matches found
    }

    /**
     * Return a set of metadata that are attached with this instance of
     * SPDocument. CM will then call findProperty for each metadata to construct
     * the feed for this document.
     */
    public Set<String> getPropertyNames() throws RepositoryException {
        final Set<String> s = new HashSet<String>();
        s.add(SPConstants.OBJECT_TYPE);
        s.add(SPConstants.LIST_GUID);
        s.add(SPConstants.SPAUTHOR);
        s.add(SPConstants.PARENT_WEB_TITLE);
        if (null != usersAclMap) {
            s.add(SpiConstants.PROPNAME_ACLUSERS);
            for (Entry<String, List<RoleType>> ace : usersAclMap.entrySet()) {
                s.add(SpiConstants.USER_ROLES_PROPNAME_PREFIX + ace.getKey());
            }
        }
        if (null != groupsAclMap) {
            s.add(SpiConstants.PROPNAME_ACLGROUPS);
            for (Entry<String, List<RoleType>> ace : groupsAclMap.entrySet()) {
                s.add(SpiConstants.GROUP_ROLES_PROPNAME_PREFIX + ace.getKey());
            }
        }

        // get the "extra" metadata fields, including those added by user:
        for (final Iterator<Attribute> iter = getAllAttrs().iterator(); iter.hasNext();) {
            final Attribute attr = (Attribute) iter.next();
            s.add(attr.getName().toString());
        }
        LOGGER.log(Level.FINEST, "Document properties set: " + s
                + " for docID [ " + docId + " ], docURL [ " + url + " ]. ");
        return s;
    }

    /**
     * @return parent web title
     */
    public String getParentWebTitle() {
        return parentWebTitle;
    }

    /**
     * @param inParentWebTitle
     */
    public void setParentWebTitle(final String inParentWebTitle) {
        if (null != inParentWebTitle) {
            parentWebTitle = inParentWebTitle;
        }
    }

    /**
     * For setting document ID required while submitting document for feed
     */
    public void setDocId(final String docId) {
        this.docId = docId;
    }

    /**
     * For downloading the contents of the documents usinf its URL. USed in case
     * of content feed only.
     *
     * @return the status of download
     * @throws RepositoryException
     */
    private String downloadContents() throws RepositoryException {
        if (null == sharepointClientContext) {
            LOGGER.log(Level.SEVERE, "Failed to download document content because the connector context is not found!");
            return SPConstants.CONNECTIVITY_FAIL;
        }
        LOGGER.config("Document URL [ " + contentDwnldURL
                + " is getting processed for contents");
        int responseCode = 0;
        boolean downloadContent = true;
        if (getFileSize() > 0
                && sharepointClientContext.getTraversalContext() != null) {
            if (getFileSize() > sharepointClientContext.getTraversalContext().maxDocumentSize()) {
                // Set the flag to download content to be false so that no
                // content is downloaded as the CM itself will drop it.
                downloadContent = false;
                LOGGER.log(Level.WARNING, "Dropping content of document : "
                        + getUrl()
                        + " with docId : "
                        + docId
                        + " of size "
                        + getFileSize()
                        + " as it exceeds the allowed max document size "
                        + sharepointClientContext.getTraversalContext().maxDocumentSize());
            }
        }
        if (downloadContent) {
            final String docURL = Util.encodeURL(contentDwnldURL);
            HttpMethodBase method = null;
            try {
                method = new GetMethod(docURL);
                responseCode = sharepointClientContext.checkConnectivity(docURL, method);
                if (null == method) {
                    return SPConstants.CONNECTIVITY_FAIL;
                }
                content = method.getResponseBodyAsStream();

            } catch (Throwable t) {
                String msg = new StringBuffer(
                        "Unable to fetch contents from URL: ").append(url).toString();
                LOGGER.log(Level.WARNING, "Unable to fetch contents from URL: "
                        + url, t);
                throw new RepositoryDocumentException(msg, t);
            }

            final Header contentType = method.getResponseHeader(SPConstants.CONTENT_TYPE_HEADER);
            if (contentType != null) {
                content_type = contentType.getValue();

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.fine("The content type for doc : " + toString()
                            + " is : " + content_type);
                }

                if (sharepointClientContext.getTraversalContext() != null) {
                    // TODO : This is to be revisited later where a better
                    // approach to skip documents or only content is
                    // available
                    int mimeTypeSupport = sharepointClientContext.getTraversalContext().mimeTypeSupportLevel(content_type);
                    if (mimeTypeSupport == 0) {
                        content = null;
                        LOGGER.log(Level.WARNING, "Dropping content of document : "
                                + getUrl()
                                + " with docId : "
                                + docId
                                + " as the mimetype : "
                                + content_type
                                + " is not supported");
                    } else if (mimeTypeSupport < 0) {
                        // Since the mimetype is in list of 'ignored' mimetype
                        // list, mark it to be skipped from sending
                        String msg = new StringBuffer(
                                "Skipping the document with docId : ").append(getDocId()).append(" doc URL: ").append(getUrl()).append(" as the mimetype is in the 'ignored' mimetypes list ").toString();
                        // Log it to the excluded_url log
                        sharepointClientContext.logExcludedURL(msg);
                        throw new SkippedDocumentException(msg);
                    }
                }
            }

        }

        if (responseCode == 200) {
            return SPConstants.CONNECTIVITY_SUCCESS;
        } else {
            return "" + responseCode;
        }
    }

    /**
     * @return the content_type
     */
    public String getContent_type() {
        return content_type;
    }

    /**
     * @param content_type the content_type to set
     */
    public void setContent_type(final String content_type) {
        this.content_type = content_type;
    }

    /**
     * @return the feedType
     */
    public FeedType getFeedType() {
        return feedType;
    }

    /**
     * @return the folderLevel
     */
    public String getFolderLevel() {
        return folderLevel;
    }

    /**
     * @param folderLevel the folderLevel to set
     */
    public void setFolderLevel(final String folderLevel) {
        this.folderLevel = folderLevel;
    }

    /**
     * @return the action
     */
    public ActionType getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(final ActionType action) {
        this.action = action;
    }

    /**
     * @return the spType
     */
    public SPType getSPType() {
        return spType;
    }

    /**
     * @return the sharepointClientContext
     */
    public SharepointClientContext getSharepointClientContext() {
        return sharepointClientContext;
    }

    /**
     * @param sharepointClientContext the sharepointClientContext to set
     */
    public void setSharepointClientContext(
            SharepointClientContext sharepointClientContext) {
        this.sharepointClientContext = sharepointClientContext;
    }

    /**
     * @return the contentDwnldURL
     */
    public String getContentDwnldURL() {
        return contentDwnldURL;
    }

    /**
     * @param contentDwnldURL the contentDwnldURL to set
     */
    public void setContentDwnldURL(String contentDwnldURL) {
        this.contentDwnldURL = contentDwnldURL;
    }

    /**
     * @return the fileSize
     */
    public int getFileSize() {
        return fileSize;
    }

    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return url;
    }

    public String getFileref() {
        return fileref;
    }

    public void setFileref(String fileref) {
        this.fileref = fileref;
    }

    public Map<String, List<RoleType>> getUsersAclMap() {
        return usersAclMap;
    }

    public void setUsersAclMap(Map<String, List<RoleType>> usersAclMap) {
        this.usersAclMap = usersAclMap;
    }

    public Map<String, List<RoleType>> getGroupsAclMap() {
        return groupsAclMap;
    }

    public void setGroupsAclMap(Map<String, List<RoleType>> groupsAclMap) {
        this.groupsAclMap = groupsAclMap;
    }

    public boolean isForAclChange() {
        return forAclChange;
    }

    public void setForAclChange(boolean forAclChange) {
        this.forAclChange = forAclChange;
    }
}
