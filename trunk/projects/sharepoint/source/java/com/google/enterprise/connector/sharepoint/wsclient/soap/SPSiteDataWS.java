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

package com.google.enterprise.connector.sharepoint.wsclient.soap;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteData;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataLocator;
import com.google.enterprise.connector.sharepoint.generated.sitedata.SiteDataSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOfStringHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sFPUrlHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sListWithTimeHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders.ArrayOf_sWebWithTimeHolder;
import com.google.enterprise.connector.sharepoint.generated.sitedata.holders._sWebMetadataHolder;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;

import org.apache.axis.holders.UnsignedIntHolder;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.holders.StringHolder;

/**
 * This class holds data and methods for any call to SiteData web service.
 *
 * @author amit_kagrawal
 */
public class SPSiteDataWS implements SiteDataWS {
  private final Logger LOGGER = Logger.getLogger(SPSiteDataWS.class.getName());
  private SharepointClientContext sharepointClientContext;
  private String endpoint;
  private SiteDataSoap_BindingStub stub = null;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public SPSiteDataWS(final SharepointClientContext inSharepointClientContext)
      throws SharepointException {
    sharepointClientContext = inSharepointClientContext;
    endpoint = Util.encodeURL(sharepointClientContext.getSiteURL())
        + SPConstants.SITEDATAENDPOINT;
    LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

    final SiteDataLocator loc = new SiteDataLocator();
    loc.setSiteDataSoapEndpointAddress(endpoint);
    final SiteData servInterface = loc;

    try {
      stub = (SiteDataSoap_BindingStub) servInterface.getSiteDataSoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, "Unable to get sitedata stub ", e);
      throw new SharepointException("Unable to get sitedata stub");
    }
  }

  /**
   * (@inheritDoc)
   */
  public String getUsername() {
    return stub.getUsername();
  }

  /**
   * (@inheritDoc)
   */
  public void setUsername(final String username) {
    stub.setUsername(username);
  }

  /**
   * (@inheritDoc)
   */
  public void setPassword(final String password) {
    stub.setPassword(password);
  }

  /**
   * (@inheritDoc)
   */
  public void setTimeout(final int timeout) {
    stub.setTimeout(timeout);
  }

  /**
   * Gets the collection of all the lists on the sharepoint server.
   *
   * @return ArrayOf_sListHolder
   * @throws RemoteException
   */
  public ArrayOf_sListHolder getListCollection() throws RemoteException {
    final ArrayOf_sListHolder vLists = new ArrayOf_sListHolder();
    final UnsignedIntHolder getListCollectionResult = new UnsignedIntHolder();
    stub.getListCollection(getListCollectionResult, vLists);
    return vLists;
  }

  /**
   * Makes a call to Site Data web service to retrieve site meta data.
   *
   * @return _sWebMetadataHolder
   * @throws RemoteException
   */
  public _sWebMetadataHolder getSiteData() throws RemoteException {
    final UnsignedIntHolder getWebResult = new UnsignedIntHolder();
    final _sWebMetadataHolder sWebMetadata = new _sWebMetadataHolder();
    final ArrayOf_sWebWithTimeHolder vWebs = new ArrayOf_sWebWithTimeHolder();
    final ArrayOf_sListWithTimeHolder vLists =
        new ArrayOf_sListWithTimeHolder();
    final ArrayOf_sFPUrlHolder vFPUrls = new ArrayOf_sFPUrlHolder();
    final StringHolder strRoles = new StringHolder();
    final ArrayOfStringHolder vRolesUsers = new ArrayOfStringHolder();
    final ArrayOfStringHolder vRolesGroups = new ArrayOfStringHolder();
    stub.getWeb(getWebResult, sWebMetadata, vWebs, vLists, vFPUrls, strRoles,
        vRolesUsers, vRolesGroups);
    return sWebMetadata;
  }
}

