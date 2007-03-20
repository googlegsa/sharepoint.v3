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

import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SimpleResultSet;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.ValueType;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Class which maintains all the methods needed to get documents and sites 
 * from the sharepoint server. It is a layer between the connector and the 
 * actual web services calls .
 *
 */
public class SharepointClient {

  private SharepointClientContext sharepointClientContext;
  
  public SharepointClient(SharepointClientContext sharepointClientContext) {
    this.sharepointClientContext = sharepointClientContext;
  }
  
  /**
   * Gets all the sites under the current site on the sharepoint server.
   * @return resultSet ResultSet containing the PropertyMap for each site.
   */
  public ResultSet getSites() {
    SimpleResultSet resultSet = new SimpleResultSet();
    try {
      SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
      List sites = siteDataWS.getAllChildrenSites();
      for(int i=0; i<sites.size(); i++) {       
        Document doc = (Document) sites.get(i);
        SimplePropertyMap pm = buildPropertyMap((Document) sites.get(i));
        resultSet.add(pm);
      }
    } catch (SharepointException e) {
      e.printStackTrace();
    }
    return resultSet;
  } 
  
  /**
   * Calls DocsFromDocLibPerSite for all the sites under the current site.
   * @return resultSet
   */
  public ResultSet getDocsFromDocumentLibrary() {
    SimpleResultSet resultSet = new SimpleResultSet();
    try {
      SiteDataWS siteDataWS = new SiteDataWS(sharepointClientContext);
      List allSites = siteDataWS.getAllChildrenSites();
      for (int i=0; i<allSites.size(); i++) {
        Document doc = (Document) allSites.get(i);
        List listItems = DocsFromDocLibPerSite(doc.getUrl());    
        for(int j=0; j<listItems.size(); j++ ){
          Document doc1 = (Document) listItems.get(j);
          SimplePropertyMap pm = 
            buildPropertyMap((Document) listItems.get(j));          
          resultSet.add(pm);
        }
      }
    } catch (SharepointException e) {
      e.printStackTrace();
    }
    return resultSet;
  }
  
  /**
   * Gets all the docs from the Document Library in sharepoint under a 
   * given site. It first calls SiteData web service to get all the Lists.
   * And then calls Lists web service to get the list items for the 
   * lists which are of the type Document Library.
   * @return resultSet 
   */  
  private List DocsFromDocLibPerSite(String siteName) {
    List listItems = new ArrayList<Document>();
    List allListItems = new ArrayList<Document>();
    SiteDataWS siteDataWS;
    ListsWS listsWS;
    try {
      if (siteName == null) {
        siteDataWS = new SiteDataWS(sharepointClientContext);
        listsWS = new ListsWS(sharepointClientContext);
      } else {
        siteDataWS = new SiteDataWS(sharepointClientContext, 
            siteName);
        listsWS = new ListsWS(sharepointClientContext, siteName);
      }
      List listCollection = siteDataWS.getDocumentLibraries();
      for(int i=0; i<listCollection.size(); i++) {
        BaseList baseList = (BaseList) listCollection.get(i);                
        listItems = listsWS.getListItems(baseList.getInternalName());
        allListItems.addAll(listItems);                       
      }
    } catch (SharepointException e) {
      e.printStackTrace();
    }
    return allListItems;
    
  }
  
  /**
   * Build the Property Map for the connector manager from the 
   * sharepoint document.
   * @param doc sharepoint document
   * @return Property Map.
   */
  private SimplePropertyMap buildPropertyMap(Document doc) {
    SimplePropertyMap pm = new SimplePropertyMap();
    Property contentUrlProp = new SimpleProperty(
      SpiConstants.PROPNAME_CONTENTURL, new SimpleValue(ValueType.STRING, 
      doc.getUrl()));
    pm.put(SpiConstants.PROPNAME_CONTENTURL, contentUrlProp);
    
    Property docIdProp = new SimpleProperty(
      SpiConstants.PROPNAME_DOCID, new SimpleValue(ValueType.STRING, 
      doc.getDocId()));
    pm.put(SpiConstants.PROPNAME_DOCID, docIdProp);        
    
    Property searchUrlProp = new SimpleProperty(
      SpiConstants.PROPNAME_SEARCHURL, new SimpleValue(ValueType.STRING, 
      doc.getUrl()));
    pm.put(SpiConstants.PROPNAME_SEARCHURL, searchUrlProp);
    
    Property lastModifyProp = new SimpleProperty(
      SpiConstants.PROPNAME_LASTMODIFY, new SimpleValue(
        ValueType.DATE, SimpleValue.calendarToIso8601(doc.getLastMod())));  
    pm.put(SpiConstants.PROPNAME_LASTMODIFY, lastModifyProp);
    return pm;
    
  }  
}
