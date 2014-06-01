// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.spiimpl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.enterprise.connector.sharepoint.client.Attribute;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.client.SPConstants.SPType;
import com.google.enterprise.connector.sharepoint.state.Folder;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Principal;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryDocumentException;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SkippedDocumentException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;
import com.google.enterprise.connector.spi.SpiConstants.DocumentType;
import com.google.enterprise.connector.spi.Value;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Class to hold data regarding a sharepoint document. Anything that is sent ot
 * GSA for indexing must be represented as an instance of this class.
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

  private Folder parentFolder;
  // When a folder is renamed/restored and the current document is being sent
  // as an implication of that.
  private Folder renamedFolder;

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
  private SPContent content = null;
  private int fileSize = -1;

  private final Logger LOGGER = Logger.getLogger(SPDocument.class.getName());

  private final ArrayList<Attribute> attrs = new ArrayList<Attribute>(5);

  /**
   * Flag to indicate if this document is to be sent as a feed
   */
  private boolean toBeFed = true;

  // List of allowed users to be sent in document's ACL
  private Set<Principal> aclUsers;

  // List of allowed groups to be sent in document's ACL
  private Set<Principal> aclGroups;

  // List of denied users to be sent in document's ACL
  private Set<Principal> aclDenyUsers;

  // List of denied groups to be sent in document's ACL
  private Set<Principal> aclDenyGroups;

  // Check if the documents is discovered from ACL based crawling. An ACL
  // based crawling happens when a security change occurs on site/list which
  // affects the ACL of many list items
  private boolean forAclChange = false;

  private String displayUrl;
  private String title;
  private final String MSG_FILE_EXTENSION = ".msg";
  private final String MSG_FILE_MIMETYPE = "application/vnd.ms-outlook";

  // Url for parent object which can be a SPFolder or SPList or SPweb Url.
  private String parentUrl;

  // Flag indicating if SPDocument is having inherited 
  // permissions or unique permissions. 
  private boolean uniquePermissions;

  // Parent ID. This will be integer if parent is a folder 
  // else this will be Guid for List or Web.
  private String parentId;

  private boolean webAppPolicyDoc = false;

  // Document Type  for Document.
  private DocumentType documentType;
  
  // flag to indicate if document is public
  private boolean publicDocument;
  // flag to indicate if it is empty document
  private boolean emptyDocument;

  /**
   * @return the toBeFed
   */
  public boolean isToBeFed() {
    if (sharepointClientContext != null) {
      if (!sharepointClientContext.isPushAcls() 
          && documentType == DocumentType.ACL) {
       return false;
      }
    }
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
      final Calendar inLastMod, final String inAuthor, final String inObjType,
      final String inParentWebTitle, final FeedType inFeedType,
      final SPType inSpType) {
    docId = inDocId;
    displayUrl = url = inUrl;
    lastMod = inLastMod;
    author = inAuthor;
    objType = inObjType;
    parentWebTitle = inParentWebTitle;
    feedType = inFeedType;
    spType = inSpType;
    LOGGER.config("docid[" + inDocId + "], URL[" + inUrl + "], LastMod["
        + inLastMod + "], ObjectType[" + inObjType + "]," + "author["
        + inAuthor + "],parentWebTitle[" + parentWebTitle + "], feedType ["
        + inFeedType + "], spType [" + inSpType + "] ");
  }

  /**
   * To be used while loading the lastDocument from the state file.
   */
  public SPDocument(final String inDocId, final String inDocURL,
      final Calendar inLastMod, final ActionType inAction) {
    docId = inDocId;
    displayUrl = url = inDocURL;
    lastMod = inLastMod;
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
      if (key.equalsIgnoreCase(SPConstants.TITLE)) {
        title = value;
      }
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

  public void setAuthor(final String inAuthor) {
    if (inAuthor != null) {
      author = inAuthor;
    }
  }

  public void setObjType(final String inObjType) {
    if (inObjType != null) {
      objType = inObjType;
    }
  }

  /**
   * For SPDocument equality comparison.
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
   * renamed/restored folders, items under the folders are sent in order of the
   * folder level. This is required because of the special handling folder
   * rename/restoration. We must ensure that all the items under a
   * renamed/restored folder are sent first for a given changeToken. Note:
   * folderLevel info is set for a doc only in case of parent folder
   * rename/restore. If the two docs one from SP2007 and another from SP2003 is
   * compared, SP2007 will always get the prefrence.
   *
   * @param doc
   */
  public int compareTo(final SPDocument doc) {
    if (doc == null) {
      return -1;
    }

    int comparison = 0;

    // TODO If documents belongs to different lists, we can rely on the
    // list's
    // ordering only.
    /*
     * if (null != getParentList() && null != doc.getParentList()) { comparison
     * = getParentList().compareTo(doc.getParentList()); if (comparison != 0) {
     * return comparison; } }
     */

    if (SPType.SP2007.equals(getSPType()) && SPType.SP2003 == doc.getSPType()) {
      return -1;
    } else if (SPType.SP2007 == doc.getSPType() && SPType.SP2003 == getSPType()) {
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

    if (SPType.SP2007.equals(getSPType())) {
      if (null != getParentFolder() && null != doc.getParentFolder()) {
        if (null != getRenamedFolder() && null != doc.getRenamedFolder()) {
          comparison = getRenamedFolder().compareTo(doc.getRenamedFolder());
          if (comparison != 0) {
            return comparison;
          }
        }
        comparison = getParentFolder().compareTo(doc.getParentFolder());
        if (comparison != 0) {
          return comparison;
        }
      } else if (null != getParentFolder() && null == doc.getParentFolder()) {
        return -1;
      } else if (null == getParentFolder() && null != doc.getParentFolder()) {
        return 1;
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
      String docURL1st = url;
      String docURL2nd = doc.url;
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

  /** Returns meta-data property value associated with Attribute for Given
   *  attribute name.
   */
  public String getMetaDataAttributeValue(final String strPropertyName) {
    final Collator collator = Util.getCollator();
    for (Attribute attr : getAllAttrs()) {
      if (collator.equals(strPropertyName, attr.getName())) {
        return attr.getValue().toString();
      }
    }
    return null;
  }

  /**
   * Returns the property object for a given property name. CM calls this to
   * gather all the information about a document. The property names that are
   * requested can be either well known properties defined under connector SPI
   * or, connector can inform them pre-hand during the call to getAll
   * propertoes.
   */
  @Override
  public Property findProperty(final String strPropertyName)
      throws RepositoryException {
    if (!sharepointClientContext.isPushAcls() &&
        documentType == DocumentType.ACL) {
      throw new SkippedDocumentException(
          "This should not happen here, isToBeFed() = " + isToBeFed()
          + ". Skipping document [" + url
          + "] as DocumentType is ACL and PushAcls is false.");
    }
    final Collator collator = Util.getCollator();
    if (collator.equals(strPropertyName, SpiConstants.PROPNAME_CONTENT)) {
      if (FeedType.CONTENT_FEED == getFeedType()
          && ActionType.ADD.equals(getAction())) {
        synchronized (this) {
          if (content == null || content.isConsumed()) {
            content = downloadContents();
          }
          InputStream contentStream = content.getContentStream();
          return (contentStream == null) ? null : new SimpleProperty(
              Value.getBinaryValue(contentStream));
        }
      }
    } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_MIMETYPE)) {
      if (FeedType.CONTENT_FEED == getFeedType()
          && ActionType.ADD.equals(getAction())) {
        synchronized (this) {
          if (content == null || content.getContentType() == null) {
            content = downloadContents();
          }
          String contentType = content.getContentType();
          return (contentType == null) ? null : new SimpleProperty(
              Value.getStringValue(contentType));
        }
      }
    } else if (collator.equals(strPropertyName,
        SPConstants.HTTP_STATUS_CODE)) {
      if (FeedType.CONTENT_FEED == getFeedType()) {
        synchronized (this) {
          if (content == null) {
            content = downloadContents();
          }
          if (content.getStatusCode() != 0) {
            return new SimpleProperty(Value.getLongValue(
                content.getStatusCode()));
          } else {
            return null;
          }
        }
      }
    } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_SEARCHURL)) {
      if (FeedType.CONTENT_FEED != getFeedType()) {
        // TODO Handle ACL feed here.
        return new SimpleProperty(Value.getStringValue(getUrl()));
      }
    } else if (collator.equals(strPropertyName,
            SpiConstants.PROPNAME_ACLINHERITANCETYPE)) {
      if (!isWebAppPolicyDoc() && parentUrl == null) {
        // Returning null as ACL information is not complete.
        // Every Inherited ACL should have parentUrl other than
        // web application policy document.
        return null;
      }
      return new SimpleProperty(Value.getStringValue(
              SpiConstants.AclInheritanceType.PARENT_OVERRIDES.toString()));
    } else if (collator.equals(strPropertyName,
            SpiConstants.PROPNAME_ACLINHERITFROM_DOCID)) {
      String parentUrlToSend = getParentUrl();
      if (parentUrlToSend == null) {
        return null;
      }
      if (getFeedType() == FeedType.CONTENT_FEED) {
        parentUrlToSend = parentUrlToSend + "|" + getParentId().toUpperCase();
      }
      return new SimpleProperty(Value.getStringValue(parentUrlToSend));
    } else if (collator.equals(strPropertyName,
        SpiConstants.PROPNAME_ACLINHERITFROM)) {
      String parentUrlToSend = getParentUrl();
      if (FeedType.CONTENT_FEED == getFeedType()) {
        return null;
      }
      return new SimpleProperty(Value.getStringValue(parentUrlToSend));
    } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_FEEDTYPE)) {
      return new SimpleProperty(Value.getStringValue(feedType.toString()));
    } else if (collator.equals(strPropertyName,
        SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE)) {
      return new SimpleProperty(Value.getStringValue(feedType.toString()));
    } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_DISPLAYURL)) {
      return new SimpleProperty(Value.getStringValue(displayUrl));
    } else if (collator.equals(strPropertyName, SPConstants.PARENT_WEB_TITLE)) {
      return new SimpleProperty(Value.getStringValue(getParentWebTitle()));
    } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_DOCID)) {
      return new SimpleProperty(Value.getStringValue(getDocId()));
    } else if (collator.equals(strPropertyName, SpiConstants.PROPNAME_LASTMODIFIED)) {
      return new SimpleProperty(Value.getDateValue(getLastMod()));
    } else if (collator.equals(strPropertyName, SPConstants.LIST_GUID)) {
      if (null != getParentList()) {
        return new SimpleProperty(Value.getStringValue(
            getParentList().getPrimaryKey()));
      }
    } else if (collator.equals(strPropertyName, SPConstants.SPAUTHOR)) {
      return new SimpleProperty(Value.getStringValue(getAuthor()));
    } else if (strPropertyName.equals(SPConstants.OBJECT_TYPE)) {
      return new SimpleProperty(Value.getStringValue(getObjType()));
    } else if (strPropertyName.equals(SpiConstants.PROPNAME_ISPUBLIC)) {
      return new SimpleProperty(Value.getBooleanValue(publicDocument));
    } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACTION)) {
      return new SimpleProperty(Value.getStringValue(getAction().toString()));
    } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACLDENYUSERS)) {
      return getPrincipalProperty(aclDenyUsers);
    } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACLDENYGROUPS)) {
      return getPrincipalProperty(aclDenyGroups);
    } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACLUSERS)) {
      return getPrincipalProperty(aclUsers);
    } else if (strPropertyName.equals(SpiConstants.PROPNAME_ACLGROUPS)) {
      return getPrincipalProperty(aclGroups);
    } else if (strPropertyName.startsWith(SpiConstants.PROPNAME_TITLE)) {
      return new SimpleProperty(Value.getStringValue(title));
    } else if (strPropertyName.equals(SpiConstants.PROPNAME_DOCUMENTTYPE)) {
      if (documentType != null) {
        return new SimpleProperty(
            Value.getStringValue(documentType.toString()));
      } else {
        return null;
      }
    } else {
      // FIXME: We can get rid of this if-else-if ladder here by setting all
      // the relevant properties (in appropriate type) right at the time of
      // document creation. After doing that, all that will be required is to
      // maintain a map of all the properties with key as the prop name. This
      // will also eliminate maintaining multiple member attributes in this
      // class. All the attribute will be there in a common map.
      for (final Iterator<Attribute> iter = getAllAttrs().iterator(); iter.hasNext();) {
        final Attribute attr = iter.next();
        if (collator.equals(strPropertyName, attr.getName())) {
          String strAttrValue = attr.getValue().toString();
          // Current approach is to parse field values for ;# characters.
          // TODO Utilize SharePoint Field meta-data and process values
          // for only columns with SharePoint Field type as 
          // "LookupMulti" or "MultiChoice"
          List<Value> valuesToPass;
          if (null != strAttrValue) {
            List<String> values =  Util.processMultiValueMetadata(strAttrValue);
            valuesToPass = new ArrayList<Value>();
            for (String str : values) {
              valuesToPass.add(Value.getStringValue(str));
            }
          } else {
            valuesToPass = null;          
          }
          return new SimpleProperty(valuesToPass);
        }
      }
    }

    LOGGER.finer("no matches found for[" + strPropertyName + "]");
    return null;// no matches found
  }

  private Property getPrincipalProperty(Set<Principal> aclPrincipals) {
    if (aclPrincipals != null) {
      List<Value> values = new ArrayList<Value>(aclPrincipals.size());
      for (Principal user : aclPrincipals) {
        values.add(Value.getPrincipalValue(user));
      }
      return new SimpleProperty(values);
    } else {
      return null;
    }
  }

  /**
   * Returns true if none of the ACL properties are set for a document.
   * Note: calling method should check for "isPushAcls" before calling this.
   */
  public boolean isMissingAcls() {
    return (!isPublicDocument() && action == ActionType.ADD 
        && Strings.isNullOrEmpty(parentUrl)
        && isNullOrEmptySet(aclUsers) && isNullOrEmptySet(aclGroups)
        && isNullOrEmptySet(aclDenyUsers) && isNullOrEmptySet(aclDenyGroups));
  }

  private boolean isNullOrEmptySet(Set<?> input) {
    return (input == null || input.isEmpty());
  }

  /**
   * Return a set of metadata that are attached with this instance of
   * SPDocument. CM will then call findProperty for each metadata to construct
   * the feed for this document.
   */
  @Override
  public Set<String> getPropertyNames() throws RepositoryException {
    final Set<String> names = new HashSet<String>();
    ArrayList<String> candidates = new ArrayList<String>();
    if (!isEmptyDocument()) {
      candidates.add(SPConstants.OBJECT_TYPE);
      candidates.add(SPConstants.LIST_GUID);
      candidates.add(SPConstants.SPAUTHOR);
      candidates.add(SPConstants.PARENT_WEB_TITLE);
    }
    if (null != documentType) {
      names.add(SpiConstants.PROPNAME_DOCUMENTTYPE);
    }
    if (sharepointClientContext.isPushAcls()) {
      if (isMissingAcls()) {
        sharepointClientContext.logToFile(SPConstants.MISSING_ACL_URL_LOG,
            "Document [" + this +"] is missing ACL.");
        LOGGER.log(Level.WARNING, 
            "Missing ACL:Document [{0}] is missing ACL.", this);
      }
      names.add(SpiConstants.PROPNAME_ISPUBLIC);
      if (!isWebAppPolicyDoc()) {
        // For regular document parent Url should not be null.
        // empty parentUrl indicates error in ACL processing.
        // so no ACL related properties will be sent in this case.
        if (parentUrl != null) {
          if (feedType == FeedType.CONTENT_FEED) {
            names.add(SpiConstants.PROPNAME_ACLINHERITFROM_DOCID);
            names.add(SpiConstants.PROPNAME_ACLINHERITFROM_FEEDTYPE);
          } else {
            names.add(SpiConstants.PROPNAME_ACLINHERITFROM);  
          }
          names.add(SpiConstants.PROPNAME_ACLINHERITANCETYPE);
        }
      } else {
        // If document is web application policy document then ACL information
        // is available, as connector will not create 
        // partial web application policy document.
        names.add(SpiConstants.PROPNAME_ACLINHERITANCETYPE);
      }

      if (aclUsers != null) {
        names.add(SpiConstants.PROPNAME_ACLUSERS);
      }
      if (aclGroups != null) {
        names.add(SpiConstants.PROPNAME_ACLGROUPS);
      }
      if (aclDenyUsers != null) {
        names.add(SpiConstants.PROPNAME_ACLDENYUSERS);
      }
      if (aclDenyGroups != null) {
        names.add(SpiConstants.PROPNAME_ACLDENYGROUPS);
      }
    }
    if (!isEmptyDocument()) {
      // Add "extra" metadata fields, including those added by user to the
      // documentMetadata List for matching against patterns
      for (final Iterator<Attribute> iter = getAllAttrs().iterator(); iter.hasNext();) {
        final Attribute attr = iter.next();
        candidates.add(attr.getName().toString());
      }
      if (null != title) {
        names.add(SpiConstants.PROPNAME_TITLE);
      }
    }
    ArrayList<Pattern> excludedMetadataPatterns = sharepointClientContext.getExcluded_metadata();
    // Add only those metadata attributes which do not come under excluded
    // patterns
    if (sharepointClientContext.getExcluded_metadata().size() != 0) {
      for (String metadataName : candidates) {
        if (!matches(metadataName, excludedMetadataPatterns)) {
          names.add(metadataName);
        }
      }
    } else {
      names.addAll(candidates);
    }
    LOGGER.log(Level.FINEST, "Document properties set: " + names
        + " for docID [ " + docId + " ], docURL [ " + url + " ]. ");
    return names;
  }

  public boolean matches(String metadataName,
      List<Pattern> excludedMetadataPatterns) {
    boolean flag = false;
    for (Pattern pattern : excludedMetadataPatterns) {
      if (metadataName.matches(pattern.pattern())) {
        flag = true;
        break;
      }
    }
    return flag;
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
   * For downloading the contents of the documents using its URL. Used with
   * content feeds only.
   *
   * @return {@link SPContent} containing the status, content type and
   *    content stream.
   * @throws RepositoryException
   */
  @VisibleForTesting
  SPContent downloadContents() throws RepositoryException {
    InputStream docContentStream = null;
    String docContentType = null;

    if (null == sharepointClientContext) {
      LOGGER.log(Level.SEVERE, "Failed to download document content because the connector context is not found!");
      return new SPContent(SPConstants.CONNECTIVITY_FAIL,
          docContentType, docContentStream);
    }
    LOGGER.config("Document URL [ " + contentDwnldURL
        + " is getting processed for contents");
    if (isEmptyDocument()) {
      LOGGER.config("Document URL [" + contentDwnldURL
          + "] is empty document");
      return new SPContent("empty",
          docContentType, docContentStream); 
    }
    int responseCode = 0;
    boolean downloadContent = true;
    if (getFileSize() > 0
        && sharepointClientContext.getTraversalContext() != null) {
      if (getFileSize() > sharepointClientContext.getTraversalContext().maxDocumentSize()) {
        // Set the flag to download content to be false so that no
        // content is downloaded as the CM itself will drop it.
        downloadContent = false;
        LOGGER.log(Level.WARNING, "Dropping content of document : " + getUrl()
            + " with docId : " + docId + " of size " + getFileSize()
            + " as it exceeds the allowed max document size "
            + sharepointClientContext.getTraversalContext().maxDocumentSize());
      }
    }
    if (downloadContent) {
      final String docURL = Util.encodeURL(contentDwnldURL);
      final HttpMethodBase method;     
      try {
        method = new GetMethod(docURL);
        responseCode = sharepointClientContext.checkConnectivity(docURL, method);
        if (responseCode != 200) {
          LOGGER.warning("Unable to get contents for document '" + getUrl() +
              "'. Received the response code: " + responseCode);
          return new SPContent(Integer.toString(responseCode), responseCode,
              docContentType, docContentStream);
        }

        InputStream contentStream = method.getResponseBodyAsStream();
        if (contentStream != null) {
          docContentStream =
              new FilterInputStream(contentStream) {
                @Override
                public void close() throws IOException {
                  try {
                    super.close();
                  } finally {
                    method.releaseConnection();
                  }
                }
              };
          }
      } catch (Throwable t) {
        String msg = new StringBuffer("Unable to fetch contents from URL: ").append(url).toString();
        LOGGER.log(Level.WARNING, "Unable to fetch contents from URL: " + url, t);
        throw new RepositoryDocumentException(msg, t);
      }
      // checks if the give URL is for .msg file if true set the mimetype
      // directly to application/vnd.ms-outlook as mimetype returned by the
      // header is incorrect for .msg files
      if (!contentDwnldURL.endsWith(MSG_FILE_EXTENSION)) {
        final Header contentType = method.getResponseHeader(SPConstants.CONTENT_TYPE_HEADER);
        if (contentType != null) {
          docContentType = contentType.getValue();
        } else {
          LOGGER.info("The content type returned for doc : " + toString()
              + " is : null ");
        }
      } else {
        docContentType = MSG_FILE_MIMETYPE;
      }

      if (LOGGER.isLoggable(Level.FINEST)) {
        LOGGER.fine("The content type for doc : " + toString() + " is : "
            + docContentType);
      }

      if (sharepointClientContext.getTraversalContext() != null
          && docContentType != null) {
        // TODO : This is to be revisited later where a better
        // approach to skip documents or only content is
        // available
        int mimeTypeSupport = sharepointClientContext.getTraversalContext()
            .mimeTypeSupportLevel(docContentType);
        if (mimeTypeSupport == 0) {
          docContentStream = null;
          LOGGER.log(Level.WARNING, "Dropping content of document : "
              + getUrl() + " with docId : " + docId + " as the mimetype : "
              + docContentType + " is not supported");
        } else if (mimeTypeSupport < 0) {
          // Since the mimetype is in list of 'ignored' mimetype
          // list, mark it to be skipped from sending
          String msg = new StringBuffer("Skipping the document with docId : ").append(getDocId()).append(" doc URL: ").append(getUrl()).append(" as the mimetype is in the 'ignored' mimetypes list ").toString();
          // Log it to the excluded_url log
          sharepointClientContext.logExcludedURL(msg);
          throw new SkippedDocumentException(msg);
        }
      }
    }

    return new SPContent(SPConstants.CONNECTIVITY_SUCCESS, responseCode,
        docContentType, docContentStream);
  }

  /**
   * @return the content object for this document.
   */
  @VisibleForTesting
  SPContent getContent() {
    return content;
  }

  /**
   * @return the content type
   */
  @VisibleForTesting
  String getContentType() {
    return content == null ? null : content.getContentType();
  }

  /**
   * @return the feedType
   */
  public FeedType getFeedType() {
    return feedType;
  }

  /**
   * @return parent folder
   */
  public Folder getParentFolder() {
    return parentFolder;
  }

  /**
   * @param folder the parent folder to set
   */
  public void setParentFolder(final Folder folder) {
    this.parentFolder = folder;
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
    return "URL [ " + url + " ], DocId [ " + docId + " ], parentFolder [ "
        + parentFolder + " ] ";
  }

  public String getFileref() {
    return fileref;
  }

  public void setFileref(String fileref) {
    this.fileref = fileref;
  }

  @VisibleForTesting
  public Set<Principal> getAclUsers() {
    return aclUsers;
  }

  public void setAclUsers(Set<Principal> aclUsers) {
    this.aclUsers = aclUsers;
  }

  @VisibleForTesting
  public Set<Principal> getAclGroups() {
    return aclGroups;
  }

  public void setAclGroups(Set<Principal> aclGroups) {
    this.aclGroups = aclGroups;
  }

  public boolean isForAclChange() {
    return forAclChange;
  }

  public void setForAclChange(boolean forAclChange) {
    this.forAclChange = forAclChange;
  }

  public String getDisplayUrl() {
    return displayUrl;
  }

  public void setDisplayUrl(String displayUrl) {
    this.displayUrl = displayUrl;
  }

  public Folder getRenamedFolder() {
    return renamedFolder;
  }

  public void setRenamedFolder(Folder renamedFolder) {
    this.renamedFolder = renamedFolder;
  }

  public String getParentUrl() {
    return parentUrl;
  }

  public void setParentUrl(String parentUrl) {
    this.parentUrl = parentUrl;
  }

  public boolean isUniquePermissions() {
    return uniquePermissions;
  }

  public void setUniquePermissions(boolean uniquePermissions) {
    this.uniquePermissions = uniquePermissions;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  @VisibleForTesting
  public Set<Principal> getAclDenyUsers() {
    return aclDenyUsers;
  }

  public void setAclDenyUsers(Set<Principal> aclDenyUsers) {
    this.aclDenyUsers = aclDenyUsers;
  }

  @VisibleForTesting
  public Set<Principal> getAclDenyGroups() {
    return aclDenyGroups;
  }

  public void setAclDenyGroups(Set<Principal> aclDenyGroups) {
    this.aclDenyGroups = aclDenyGroups;
  }

  public boolean isWebAppPolicyDoc() {
    return webAppPolicyDoc;
  }

  public void setWebAppPolicyDoc(boolean webAppPolicyDoc) {
    this.webAppPolicyDoc = webAppPolicyDoc;
  }

  /**
   * @return the documentType
   */
  public DocumentType getDocumentType() {
    return documentType;
  }

  /**
   * @param documentType the documentType to set
   */
  public void setDocumentType(DocumentType documentType) {
    this.documentType = documentType;
  }

  public boolean isPublicDocument() {
    return publicDocument;
  }

  public void setPublicDocument(boolean publicDocument) {
    this.publicDocument = publicDocument;
  }

  public boolean isEmptyDocument() {
    return emptyDocument;
  }

  public void setEmptyDocument(boolean emptyDocument) {
    this.emptyDocument = emptyDocument;
  }

  // TODO: status can be CONNECTIVITY_SUCCESS, FAIL or "empty" and
  // statusCode is the HTTP status code. Refactor the code to handle
  // both of these statuses better.
  @VisibleForTesting
  class SPContent {
    private final String status;
    private final int statusCode;
    private final InputStream contentStream;
    private final String contentType;
    private boolean isConsumed;

    public SPContent(String status, String contentType,
        InputStream contentStream) {
      this(status, 0, contentType, contentStream);
    }

    public SPContent(String status, int statusCode, String contentType,
        InputStream contentStream) {
      this.status = status;
      this.statusCode = statusCode;
      this.contentType = contentType;
      this.contentStream = contentStream;
      this.isConsumed = false;
    }

    String getStatus() {
      return status;
    }

    int getStatusCode() {
      return statusCode;
    }

    String getContentType() {
      return contentType;
    }

    boolean isConsumed() {
      return isConsumed;
    }

    InputStream getContentStream() throws IllegalStateException {
      if (isConsumed) {
        throw new IllegalStateException(
            "The document stream has already been consumed.");
      }
      isConsumed = true;
      return contentStream;
    }
  }
}
