// Copyright 2007 Google Inc.  All Rights Reserved.
package com.google.enterprise.connector.sharepoint.client;

import com.google.enterprise.connector.sharepoint.generated.SiteDataStub;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub.ArrayOf_sWebWithTime;
import com.google.enterprise.connector.sharepoint.generated.SiteDataStub._sWebWithTime;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.ResultSet;
import com.google.enterprise.connector.spi.SimpleProperty;
import com.google.enterprise.connector.spi.SimplePropertyMap;
import com.google.enterprise.connector.spi.SimpleResultSet;
import com.google.enterprise.connector.spi.SimpleValue;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.Value;
import com.google.enterprise.connector.spi.ValueType;

import org.apache.axis2.AxisFault;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Iterator;

/**
 * 
 * Class to connect to the web services API of the sharepoint server.
 *
 */

public class SharepointClient {

  private SharepointClientContext sharepointClientContext;
  private static final String siteDataEndpoint = "/_vti_bin/SiteData.asmx";
  
  public SharepointClient(SharepointClientContext sharepointClientContext) {
    this.sharepointClientContext = sharepointClientContext;
  }
  
  /**
   * Gets all the sites on the sharepoint server.
   * @return resultSet ResultSet containing the PropertyMap for each site.
   */
  public ResultSet getSites() {
    SimpleResultSet resultSet = new SimpleResultSet();
    String endpoint = "http://" + sharepointClientContext.getHost() + ":" + 
                       sharepointClientContext.getPort() + siteDataEndpoint;
    try {
      SiteDataStub stub = new SiteDataStub(endpoint);
      sharepointClientContext.setStubWithAuth(stub, endpoint);    
      SiteDataStub.GetSite req = new SiteDataStub.GetSite();
      SiteDataStub.GetSiteResponse res = stub.GetSite(req);  
      
      ArrayOf_sWebWithTime webs = res.getVWebs();
      _sWebWithTime[] els = webs.get_sWebWithTime();
      for (int i = 0; i < els.length; ++i) {
        String url = els[i].getUrl();      
        Calendar lastModified = els[i].getLastModified();   
        SimplePropertyMap pm = new SimplePropertyMap();        
        
        Property contentUrlProp = new SimpleProperty(
          SpiConstants.PROPNAME_CONTENTURL, new SimpleValue(ValueType.STRING, url));
        pm.put(SpiConstants.PROPNAME_CONTENTURL, contentUrlProp);
        
        Property docIdProp = new SimpleProperty(
          SpiConstants.PROPNAME_DOCID, new SimpleValue(ValueType.STRING, url ));
        pm.put(SpiConstants.PROPNAME_DOCID, docIdProp);        
        
        Property searchUrlProp = new SimpleProperty(
          SpiConstants.PROPNAME_SEARCHURL, new SimpleValue(ValueType.STRING, url ));
        pm.put(SpiConstants.PROPNAME_SEARCHURL, searchUrlProp);
        
        Property lastModifyProp = new SimpleProperty(
        SpiConstants.PROPNAME_LASTMODIFY, new SimpleValue(
          ValueType.DATE, SimpleValue.calendarToIso8601(lastModified)));
        pm.put(SpiConstants.PROPNAME_LASTMODIFY, lastModifyProp);
        resultSet.add(pm);
      }      
     } catch (AxisFault e) {      
      e.printStackTrace();
    } catch (RemoteException e) {
      e.printStackTrace();
    }    
    return resultSet;
  }    
}
