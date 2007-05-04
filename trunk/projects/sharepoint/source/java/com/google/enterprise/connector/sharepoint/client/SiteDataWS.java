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
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub.ArrayOf_sList;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub.ArrayOf_sWebWithTime;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub._sList;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub._sWebWithTime;
import com.google.enterprise.connector.sharepoint.generated.ViewsStub;
import org.apache.axis2.AxisFault;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * This class holds data and methods for any call to SiteData web service.
 *
 */
public class SiteDataWS {   
  
  private static final String siteDataEndpoint = "/_vti_bin/SiteData.asmx";
  public static final String DOC_LIB = "DocumentLibrary";
  public static final String GENERIC_LIST = "GenericList";
  private SharepointClientContext sharepointClientContext;
  private String endpoint;
  private SiteDataStub stub;
  private ViewsWS viewsStub;
  
  public SiteDataWS(SharepointClientContext sharepointClientContext) 
    throws SharepointException {
    this.sharepointClientContext = sharepointClientContext;
    endpoint = "http://" + sharepointClientContext.getHost() + ":" + 
                sharepointClientContext.getPort() + 
                sharepointClientContext.getsiteName() + siteDataEndpoint;
    try {
      stub = new SiteDataStub(endpoint);
      sharepointClientContext.setStubWithAuth(stub, endpoint);
      viewsStub = new ViewsWS(sharepointClientContext);
    } catch (AxisFault e) {
      throw new SharepointException(e.toString());
    }     
  }
  
  public SiteDataWS(SharepointClientContext sharepointClientContext, 
      String siteName) throws SharepointException {
    this.sharepointClientContext = sharepointClientContext;
    endpoint = siteName + siteDataEndpoint;
    try {
      stub = new SiteDataStub(endpoint);
      sharepointClientContext.setStubWithAuth(stub, endpoint);
      viewsStub = new ViewsWS(sharepointClientContext);
    } catch (AxisFault e) {
      throw new SharepointException(e.toString());
    }     
  }
  
  /**
   * Gets all the sites from the sharepoint server.
   * @return list of sharepoint documents corresponding to sites.
   */
  public List getAllChildrenSites() throws SharepointException {
    ArrayList<SPDocument> sites = new ArrayList<SPDocument>();
    try {
      SiteDataStub.GetSite req = new SiteDataStub.GetSite();
      SiteDataStub.GetSiteResponse res = stub.GetSite(req);
      ArrayOf_sWebWithTime webs = res.getVWebs();
      _sWebWithTime[] els = webs.get_sWebWithTime();
      for (int i = 0; i < els.length; ++i) {        
        String url = els[i].getUrl();      
        if (url.startsWith("http://" + sharepointClientContext.getHost() 
            + ":" + sharepointClientContext.getPort() + 
            sharepointClientContext.getsiteName()) || 
            url.startsWith("http://" + sharepointClientContext.getHost() 
                + sharepointClientContext.getsiteName())) {
          Calendar lastModified = els[i].getLastModified();   
          SPDocument doc = new SPDocument(url, url, lastModified);
          sites.add(doc);
        }
      }  
    } catch (RemoteException e) {
      throw new SharepointException(e.toString());        
    }
    return sites;      
  }
   
  /**
   * Gets the collection of all the SPDocument Libraries on the sharepoint server.
   * @return list of BaseList objects.
   * @throws SharepointException
   */
  public  List getDocumentLibraries() throws SharepointException {
    return getNamedLists(DOC_LIB);
  }
  
  /**
   * Gets the collection of all the Generic Lists on the sharepoint server.
   * @return list of BaseList objects.
   * @throws SharepointException
   */
  public  List getGenericLists() throws SharepointException {
    return getNamedLists(GENERIC_LIST);
  }
  
  /**
   * Gets the collection of all the lists on the sharepoint server which are
   * of a given type. E.g., DocumentLibrary
   * @return list of BaseList objects.
   */
  private List getNamedLists(String baseType) throws SharepointException {
    ArrayList<BaseList> listCollection = new ArrayList<BaseList>();      
    try {
      SiteDataStub.GetListCollection req = 
        new SiteDataStub.GetListCollection();
      SiteDataStub.GetListCollectionResponse res;
      res = stub.GetListCollection(req);
      ArrayOf_sList asl = res.getVLists();
      _sList[] sl = asl.get_sList();
      if (sl != null) {
        for(int i=0; i<sl.length; i++) {
          try {                     
            if(sl[i].getBaseType().equals(baseType)) {
              BaseList list = new BaseList(sl[i].getInternalName(), 
                sl[i].getTitle(), sl[i].getBaseType(), 
                Util.siteDataStringToCalendar(sl[i].getLastModified()));              
              listCollection.add(list);
              
              // find out what "columns" (metadata) are enabled on this List:
              viewsStub.getViewNames(list.getInternalName());
            }
          } catch (ParseException e) {
            throw new SharepointException(e.toString());
          }
        }
        Collections.sort(listCollection);
      }
    } catch (RemoteException e) {
      throw new SharepointException(e.toString());
    }           
    return listCollection;
  }
}



