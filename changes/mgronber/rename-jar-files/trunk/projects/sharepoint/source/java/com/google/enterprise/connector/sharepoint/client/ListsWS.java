// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.generated.ListsStub;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetAttachmentCollection;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItemChanges;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItems;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.ViewFields_type14;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SimpleValue;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

/**
 * This class holds data and methods for any call to Lists Web Service.
 *
 */
public class ListsWS {
  private static final String listsEndpoint = "_vti_bin/Lists.asmx";
  private SharepointClientContext sharepointClientContext;
  private String endpoint;
  private ListsStub stub;
  private ViewsWS viewsWS;

  /**
   * The "blacklist" is the SharePoint meta attributes that we will NOT
   * pass to the GSA. (these all come from the ows_metaInfo attribute, which
   * actually encodes a large number of other attributes).
   * Note that these can be regular expressions, in order to catch
   * 1;#Subject and 2;#Subject
   * 
   * Also note that these are just one person's opinion about the metadata you
   * probably don't want.  Feel free to add or remove items.
   */
  private static final ArrayList<Pattern> blacklist;
  static {
    blacklist = new ArrayList<Pattern>();
    blacklist.add(Pattern.compile(".*vti_cachedcustomprops$"));
    blacklist.add(Pattern.compile(".*vti_parserversion$"));
    blacklist.add(Pattern.compile(".*ContentType$"));
    blacklist.add(Pattern.compile(".*vti_cachedtitle$"));
    blacklist.add(Pattern.compile(".*ContentTypeId$"));
    blacklist.add(Pattern.compile(".*DocIcon$"));
    blacklist.add(Pattern.compile(".*vti_cachedhastheme$"));
    blacklist.add(Pattern.compile(".*vti_metatags$"));
    blacklist.add(Pattern.compile(".*vti_charset$"));
    blacklist.add(Pattern.compile(".*vti_cachedbodystyle$"));
    blacklist.add(Pattern.compile(".*vti_cachedneedsrewrite$"));
  }
  
  /**
   * The "whitelist" is SharePoint meta attributes that we WILL
   * pass to the GSA but will treat specially, so they should not be swept
   * up into the 'attrs'.
   * There is no operational difference between blacklist and whitelist;
   * in both cases the attributes are not passed to the GSA.
   */
  private static final ArrayList<Pattern> whitelist;
  static {
    whitelist = new ArrayList<Pattern>();
    whitelist.add(Pattern.compile(".*vti_title$"));
    whitelist.add(Pattern.compile(".*vti_author$"));
  }
  
  /**
   * Determine if any entry in a given List matches the given input
   * @param list
   * @param input
   * @return boolean
   */
  private static boolean listMatches(List<Pattern> list, String input) {
    for (Pattern pattern: list) {
      Matcher matcher = pattern.matcher(input);
      if (matcher.matches()) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Extract the meaningful part of a meaningful metadata string. The two
   * "meaningful"s signify, for an example "BobAdditional:SW|SomeBobData"
   * 1) name: BobAdditional
   * 2) value: SomeBobData
   *   because "BobAdditional" is neither in the blacklist nor the whitelist
   *   The "SW|" is discarded as type info (we treat everything as strings).
   * example: "vti_author:SW|Bob" returns null, because vti_author
   *   matches an entry in the whitelist.
   * example "_Category:SW|" returns "" because there's nothing after
   *   the "|".
   * @param meta
   * @return value, or empty string if none
   */
  private static String getMetadataContent(String meta) {
    String[] parts = meta.split(":");
    if (parts.length < 2) return "";
    String name = parts[0].trim();
    if (!listMatches(blacklist, name) && !listMatches(whitelist, name)) {
      String value = parts[1].trim();
      int ix = value.indexOf('|');
      if (ix >= 0) {
        if (ix < value.length()) {
          return value.substring(ix+1);
        } else {
          return "";
        }
      } else {
        return value;
      }
    }
    return "";
  }
  
  public ListsWS(SharepointClientContext sharepointClientContext) 
      throws SharepointException, RepositoryException {
    this.sharepointClientContext = sharepointClientContext;
    endpoint = "http://" + sharepointClientContext.getHost() + ":" + 
        sharepointClientContext.getPort() + 
        Util.getEscapedSiteName(sharepointClientContext.getsiteName()) + 
        listsEndpoint;
    try {
      stub = new ListsStub(endpoint);
      sharepointClientContext.setStubWithAuth(stub, endpoint);
      viewsWS = new ViewsWS(sharepointClientContext);
    } catch (AxisFault e) {
      throw new SharepointException(e.toString());        
    }
  }
  
  public ListsWS(SharepointClientContext sharepointClientContext, 
      String siteName) throws SharepointException, RepositoryException {
  this.sharepointClientContext = sharepointClientContext;
  if (siteName.startsWith("http://")) {
    siteName = siteName.substring(7);
    endpoint = "http://" + Util.getEscapedSiteName(siteName) + listsEndpoint;
  } else {
    endpoint = Util.getEscapedSiteName(siteName) + listsEndpoint;
  }
  try {
    stub = new ListsStub(endpoint);
    sharepointClientContext.setStubWithAuth(stub, endpoint);
    viewsWS = new ViewsWS(sharepointClientContext);
  } catch (AxisFault e) {
    throw new SharepointException(e.toString());
  }     
}
  
  private ViewFields_type14 makeViewFields(String listName)
      throws SharepointException {
    List<String> viewFieldStrings = viewsWS.getViewFields(listName);
    if (viewFieldStrings == null) {
      return null;
    }
    OMFactory factory = OMAbstractFactory.getOMFactory();
    OMNamespace ms = factory.createOMNamespace(
        "http://schemas.microsoft.com/sharepoint/soap/", "ms");

    OMElement root = factory.createOMElement("ViewFields", ms);
    OMElement subRoot = factory.createOMElement("viewFields", ms);
    OMElement childTest = factory.createOMElement("FieldRef", ms);
    ViewFields_type14 viewFields = new ListsStub.ViewFields_type14();
    for (String fieldName:viewFieldStrings) {
      OMElement field = factory.createOMElement("FieldRef", ms);
      field.addAttribute("Name", fieldName, ms);
      subRoot.addChild(field);
    }

    childTest.addAttribute("Name", "Author", ms);
    viewFields.setExtraElement(subRoot);
    return viewFields;
  }
  
  /**
   * Gets all the list items of a particular list
   * @param listName internal name of the list
   * @return list of sharepoint documents corresponding to items in the list.
   * @throws SharepointException 
   */
  public List getListItems(String listName) throws SharepointException {
    ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
    String urlPrefix = "http://" + sharepointClientContext.getHost() + ":" + 
        sharepointClientContext.getPort() + "/";
      
    ListsStub.GetListItems req = new ListsStub.GetListItems();
    req.setListName(listName);
    req.setQuery(null);
    
    req.setViewFields(null);
    req.setRowLimit("");
    req.setViewName("");
    req.setWebID("");
    
    /* Setting query options to be Recursive, so that docs under folders are
     * retrieved recursively
     */
    ListsStub.QueryOptions_type36 queryOptions = 
        new ListsStub.QueryOptions_type36();
    req.setQueryOptions(queryOptions);
    OMFactory omfactory = OMAbstractFactory.getOMFactory();                  
    OMElement options = omfactory.createOMElement("QueryOptions", null);
    queryOptions.setExtraElement(options);
    OMElement va = omfactory.createOMElement("ViewAttributes", null,
            options);
    OMAttribute attr = omfactory.createOMAttribute("Scope", null,
            "Recursive");    
    va.addAttribute(attr);
    
    /* Setting the query so that the returned items are in lastModified 
     * order. 
     */    
    ListsStub.Query_type34 query = new ListsStub.Query_type34();
    req.setQuery(query);
    
    OMElement queryOM = omfactory.createOMElement("Query", null);
    query.setExtraElement(queryOM);
    OMElement orderBy = omfactory.createOMElement("OrderBy", null, queryOM);
    orderBy.addChild(omfactory.createOMText(orderBy, "ows_Modified"));
    
    try {
      ListsStub.GetListItemsResponse res = stub.GetListItems(req);
      OMFactory omf = OMAbstractFactory.getOMFactory();
      OMElement oe = res.getGetListItemsResult().getOMElement
          (GetListItems.MY_QNAME, omf);
      System.out.println(oe.toString());
      StringBuffer url = new StringBuffer();
      for (Iterator<OMElement> ita = oe.getChildElements(); ita.hasNext(); ) {
        OMElement resultOmElement = ita.next();
        Iterator<OMElement> resultIt = resultOmElement.getChildElements();
        OMElement dataOmElement = resultIt.next();
        for (Iterator<OMElement> dataIt = dataOmElement.getChildElements();
            dataIt.hasNext(); ) {
          OMElement rowOmElement = dataIt.next();            
          if (rowOmElement.getAttribute(new QName("ows_FileRef")) != null) {
            String docId = rowOmElement.getAttribute(
                new QName("ows_UniqueId")).getAttributeValue();  
            String lastModified = rowOmElement.getAttribute(
                new QName("ows_Modified")).getAttributeValue();
            String fileName = rowOmElement.getAttribute(
                new QName("ows_FileRef")).getAttributeValue();
            fileName = fileName.substring(fileName.indexOf("#") + 1);
            url.setLength(0);
            url.append(urlPrefix);
            url.append(fileName);              
                          
            try {
              SPDocument doc;
              doc = new SPDocument(docId, url.toString(), 
                  Util.listItemsStringToCalendar(lastModified));             
              listItems.add(doc);
            } catch (ParseException e) {
              throw new SharepointException(e.toString(), e);
            }                         
          }
        }
      }
    } catch (RemoteException e) {
      throw new SharepointException(e.toString(), e);
    }     
    return listItems;
  }
  
  /**
   * Gets all the list item changes of a particular generic list since 
   * a particular time. Generic lists include Discussion boards, Calendar,
   * Tasks, Links, Announcements.
   * @param list BaseList object
   * @return list of sharepoint SPDocuments corresponding to items in the list. 
   * These are ordered by last Modified time.
   * @throws SharepointException 
   */
  public List getGenericListItemChanges(BaseList list, Calendar since) 
      throws SharepointException {
    String listName = list.getInternalName();
    ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
    String urlPrefix = "http://" + sharepointClientContext.getHost() + ":" + 
    sharepointClientContext.getPort() 
        + sharepointClientContext.getsiteName() + "/" + "Lists" + "/" 
        + list.getTitle() + "/" + "DispForm.aspx?ID=";
    ListsStub.GetListItemChanges req = new ListsStub.GetListItemChanges();
    req.setListName(listName);
    req.setViewFields(makeViewFields(list.getInternalName()));  
    if (since != null) {
      req.setSince(SimpleValue.calendarToIso8601(since));
    } else {
      req.setSince(null);      
    }
    try {
      ListsStub.GetListItemChangesResponse res = stub.GetListItemChanges(req);
      OMFactory omf = OMAbstractFactory.getOMFactory();
      OMElement oe = res.getGetListItemChangesResult().getOMElement
          (GetListItemChanges.MY_QNAME, omf);
      StringBuffer url = new StringBuffer();
      for (Iterator<OMElement> ita = oe.getChildElements(); ita.hasNext(); ) {
        OMElement resultOmElement = ita.next();
        Iterator<OMElement> resultIt = resultOmElement.getChildElements();
        OMElement dataOmElement = resultIt.next();
        for (Iterator<OMElement> dataIt = dataOmElement.getChildElements();
            dataIt.hasNext(); ) {
          OMElement rowOmElement = dataIt.next();            
          String docId = rowOmElement.getAttribute(
              new QName("ows_UniqueId")).getAttributeValue();
          String itemId = rowOmElement.getAttribute(
              new QName("ows_ID")).getAttributeValue();     
          url.setLength(0);
          url.append(urlPrefix);
          url.append(itemId);                                
          SPDocument doc;
          doc = new SPDocument(docId, url.toString(), list.getLastMod());
          listItems.add(doc);
        }
      }
      Collections.sort(listItems);
    } catch (RemoteException e) {
      throw new SharepointException(e.toString(), e);
    }     
    return listItems;
  }  
  
  /**
   * Gets all the list item changes of a particular SPDocument library since 
   * a particular time.
   * @param list BaseList object
   * @return list of sharepoint SPDocuments corresponding to items in the list. 
   * These are ordered by last Modified time.
   * @throws SharepointException 
   */
  public List getDocLibListItemChanges(BaseList list, Calendar since) 
      throws SharepointException {
    String listName = list.getInternalName();
    ArrayList<SPDocument> listItems = new ArrayList<SPDocument>();
    String urlPrefix = "http://" + sharepointClientContext.getHost() + ":" + 
    sharepointClientContext.getPort() + "/";
    ListsStub.GetListItemChanges req = new ListsStub.GetListItemChanges();
    req.setListName(listName);
    req.setViewFields(makeViewFields(list.getInternalName()));   
    if (since != null) {
      req.setSince(SimpleValue.calendarToIso8601(since));
    } else {
      req.setSince(null);      
    }
    try {
      ListsStub.GetListItemChangesResponse res = stub.GetListItemChanges(req);
      OMFactory omf = OMAbstractFactory.getOMFactory();
      OMElement oe = res.getGetListItemChangesResult().getOMElement
          (GetListItemChanges.MY_QNAME, omf);
      StringBuffer url = new StringBuffer();
      for (Iterator<OMElement> ita = oe.getChildElements(); ita.hasNext(); ) {
        OMElement resultOmElement = ita.next();
        Iterator<OMElement> resultIt = resultOmElement.getChildElements();
        OMElement dataOmElement = resultIt.next();
        for (Iterator<OMElement> dataIt = dataOmElement.getChildElements();
            dataIt.hasNext(); ) {
          OMElement rowOmElement = dataIt.next();  
          if (rowOmElement.getAttribute(new QName("ows_FileRef")) != null) {
            String docId = rowOmElement.getAttribute(
                new QName("ows_UniqueId")).getAttributeValue();  
            String lastModified = rowOmElement.getAttribute(
                new QName("ows_Modified")).getAttributeValue();
            String fileName = rowOmElement.getAttribute(
                new QName("ows_FileRef")).getAttributeValue();
            /*
             * An example of ows_FileRef is 
             * 1;#unittest/Shared SPDocuments/sync.doc 
             * We need to get rid of 1;#
             */
            fileName = fileName.substring(fileName.indexOf("#") + 1);                    
            url.setLength(0);
            url.append(urlPrefix);
            url.append(fileName);    
            String metaInfo = rowOmElement.getAttribute(
                new QName("ows_MetaInfo")).getAttributeValue();
            try {
              SPDocument doc;
              doc = new SPDocument(docId, url.toString(), 
                  Util.listItemChangesStringToCalendar(lastModified));
 
              // gather up the rest of the metadata:
              String[] arrayOfMetaInfo = metaInfo.split("\n|\r\n");
              setDocLibMetadata(doc, arrayOfMetaInfo);
              listItems.add(doc);
            } catch (ParseException e) {
              throw new SharepointException(e.toString(), e);
            }                         
          }
        }
      }
      Collections.sort(listItems);
    } catch (RemoteException e) {
      throw new SharepointException(e.toString(), e);
    }     
    return listItems;
  }
  
  /**
   * Gets all the attachments of a particular list item.
   * @param baseList List to which the item belongs
   * @param listItem list item for which the attachments need to be retrieved.
   * @return list of sharepoint SPDocuments corresponding to attachments
   * for the given list item. 
   * These are ordered by last Modified time.
   * @throws SharepointException 
   */
  public List getAttachments(BaseList baseList, SPDocument listItem) 
      throws SharepointException {
    String listName = baseList.getInternalName();
    /*
     * An example of docId is 3;#{BC0E981B-FAA5-4476-A44F-83EA27155513}.
     * For listItemId, we need to pass "3". 
     */
   
    String arrayOflistItemId[] = listItem.getDocId().split(";#");
    String listItemId = arrayOflistItemId[0];
    ArrayList<SPDocument> listAttachments = new ArrayList<SPDocument>();
    ListsStub.GetAttachmentCollection req = 
        new ListsStub.GetAttachmentCollection();
    req.setListName(listName);
    req.setListItemID(listItemId);
    try {
      ListsStub.GetAttachmentCollectionResponse res = 
          stub.GetAttachmentCollection(req);
      OMFactory omf = OMAbstractFactory.getOMFactory();
      OMElement oe = res.getGetAttachmentCollectionResult().getOMElement
          (GetAttachmentCollection.MY_QNAME, omf);
      Iterator<OMElement> ita = oe.getChildElements();
      OMElement attachmentsOmElement = ita.next();
      Iterator<OMElement> attachmentsIt =
          attachmentsOmElement.getChildElements();      
      for (Iterator<OMElement> attachmentIt = 
          attachmentsOmElement.getChildElements(); attachmentsIt.hasNext();) {
        OMElement attachmentOmElement = attachmentsIt.next();        
        String url = attachmentOmElement.getText();               
        SPDocument doc;
        doc = new SPDocument(url, url, baseList.getLastMod());        
        listAttachments.add(doc);                
      }
      Collections.sort(listAttachments);
    } catch (RemoteException e) {
      throw new SharepointException(e.toString(), e);
    }
    return listAttachments;
  }
  
  /**
   * Collect all "interesting" metadata for an item from a Document Library
   * described in a GetListItemsChanges WSDL call.
   * Do not collect items which are
   * either already dealt with (the whitelist) or the ones we're configured
   * to not care about (the blacklist).  whitelist and blacklist are
   * static sets in this module, so changes should be made there, not here.
   * @param doc SPDocument
   * @param arrayOfMetaInfo array of strings derived from ows_metaInfo
   */
  private void setDocLibMetadata(SPDocument doc, String[] arrayOfMetaInfo) {
    for (String meta : arrayOfMetaInfo) {
      String[] parts = meta.split(":");
      if (parts.length < 2) continue;
      String value = getMetadataContent(meta);
      if (value.length() > 0) {
        doc.setAttribute(parts[0].trim(), value);
      }
    }
  }
}
