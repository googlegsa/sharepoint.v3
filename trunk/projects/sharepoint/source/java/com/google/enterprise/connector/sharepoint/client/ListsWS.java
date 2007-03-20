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

import com.google.enterprise.connector.sharepoint.generated.ListsStub;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItems;
import com.google.enterprise.connector.spi.SimpleValue;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

/**
 * This class holds data and methods for any call to Lists Web Service.
 *
 */
public class ListsWS {
  private static final String listsEndpoint = "/_vti_bin/Lists.asmx";
  private SharepointClientContext sharepointClientContext;
  private String endpoint;
  private ListsStub stub;
  
  public ListsWS(SharepointClientContext sharepointClientContext) 
      throws SharepointException {
    this.sharepointClientContext = sharepointClientContext;
    endpoint = "http://" + sharepointClientContext.getHost() + ":" + 
        sharepointClientContext.getPort() + 
        sharepointClientContext.getsiteName() + listsEndpoint;
    try {
      stub = new ListsStub(endpoint);
      sharepointClientContext.setStubWithAuth(stub, endpoint);
    } catch (AxisFault e) {
      throw new SharepointException(e.toString());        
    }
  }
  
  public ListsWS(SharepointClientContext sharepointClientContext, 
      String siteName) throws SharepointException {
  this.sharepointClientContext = sharepointClientContext;
  endpoint = siteName + listsEndpoint;
  try {
    stub = new ListsStub(endpoint);
    sharepointClientContext.setStubWithAuth(stub, endpoint);
  } catch (AxisFault e) {
    throw new SharepointException(e.toString());
  }     
}
  
  /**
   * Gets all the list items of a particular list
   * @param listName internal name of the list
   * @return list of sharepoint documents corresponding to items in the list.
   * @throws SharepointException 
   */
  public List getListItems(String listName) throws SharepointException {
    ArrayList<Document> listItems = new ArrayList<Document>();
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
              Document doc;
              doc = new Document(docId, url.toString(), 
                  Util.listItemsStringToCalendar(lastModified));             
              listItems.add(doc);
            } catch (ParseException e) {
              throw new SharepointException(e.toString());
            }                         
          }
        }
      }
    } catch (RemoteException e) {
      throw new SharepointException(e.toString());
    }     
    return listItems;
  }
  
  public List getListItemChanges(String listName, Calendar since) 
      throws SharepointException {
    ArrayList<Document> listItems = new ArrayList<Document>();
    String urlPrefix = "http://" + sharepointClientContext.getHost() + ":" + 
    sharepointClientContext.getPort() + "/";
    ListsStub.GetListItemChanges req = new ListsStub.GetListItemChanges();
    req.setListName(listName);
    req.setViewFields(null);   
    if (since != null) {
      req.setSince(SimpleValue.calendarToIso8601(since));
    } else {
      req.setSince(null);      
    }
    try {
      ListsStub.GetListItemChangesResponse res = stub.GetListItemChanges(req);
      OMFactory omf = OMAbstractFactory.getOMFactory();
      OMElement oe = res.getGetListItemChangesResult().getOMElement
          (GetListItems.MY_QNAME, omf);
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
              Document doc;
              doc = new Document(docId, url.toString(), 
                  Util.listItemChangesStringToCalendar(lastModified));             
              listItems.add(doc);
            } catch (ParseException e) {
              throw new SharepointException(e.toString());
            }                         
          }
        }
      }
      Collections.sort(listItems);
    } catch (RemoteException e) {
      throw new SharepointException(e.toString());
    }     
    return listItems;
  }  
}
