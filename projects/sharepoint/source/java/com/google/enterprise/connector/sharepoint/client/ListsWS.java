// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.generated.ListsStub;
import com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItems;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

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
    
    public ListsWS(SharepointClientContext sharepointClientContext) {
      this.sharepointClientContext = sharepointClientContext;
      endpoint = "http://" + sharepointClientContext.getHost() + ":" + 
                  sharepointClientContext.getPort() + listsEndpoint;
      try {
        stub = new ListsStub(endpoint);
        sharepointClientContext.setStubWithAuth(stub, endpoint);
      } catch (AxisFault e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    /**
     * Gets all the list items of a particular list
     * @param listName internal name of the list
     * @return list of sharepoint documents corresponding to items in the list.
     */
    public ArrayList<Document> getListItems(String listName) {
      ArrayList<Document> listItems = new ArrayList<Document>();
      String urlPrefix = "http://" + sharepointClientContext.getHost() + ":" + 
                          sharepointClientContext.getPort() + "/";
      StringBuffer url = new StringBuffer();
      ListsStub.GetListItems req = new ListsStub.GetListItems();
      req.setListName(listName);
      req.setQuery(null);
      req.setViewFields(null);
      req.setRowLimit("");
      req.setViewName("");
      req.setWebID("");
      try {
        ListsStub.GetListItemsResponse res = stub.GetListItems(req);
        OMFactory omf = OMAbstractFactory.getOMFactory();
        OMElement oe = res.getGetListItemsResult().getOMElement
                           (GetListItems.MY_QNAME, omf);
        Iterator ita = oe.getChildElements();  
        while (ita.hasNext()) {            
          OMElement resultOmElement = (OMElement) ita.next();
          Iterator resultIt = resultOmElement.getChildElements();
          OMElement dataOmElement = (OMElement) resultIt.next();  
          Iterator dataIt = dataOmElement.getChildElements();
          while (dataIt.hasNext()) {           
            OMElement rowOmElement = (OMElement) dataIt.next();                   
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
              
              Document doc;
              try {
                doc = new Document(docId, url.toString(), 
                                   Util.StringToCalendar(lastModified));
                listItems.add(doc);
              } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }                         
            }
          }
        }
      } catch (RemoteException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }     
      return listItems;
    }
}
