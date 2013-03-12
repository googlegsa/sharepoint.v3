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

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertInfo;
import com.google.enterprise.connector.sharepoint.generated.alerts.Alerts;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertsLocator;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.AlertsWS;
import com.google.enterprise.connector.spi.RepositoryException;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling Alerts.asmx Provides a layer to talk to the Alerts
 * Web Service on the SharePoint server Any call to this Web Service must go
 * through this layer.
 *
 * @author nitendra_thakur
 */
public class SPAlertsWS implements AlertsWS {
  private final Logger LOGGER = Logger.getLogger(SPAlertsWS.class.getName());
  private AlertsSoap_BindingStub stub;

  /**
   * @param inSharepointClientContext The Context is passed so that necessary
   *          information can be used to create the instance of current class
   *          Web Service endpoint is set to the default SharePoint URL stored
   *          in SharePointClientContext.
   * @throws SharepointException
   */
  public SPAlertsWS(final SharepointClientContext inSharepointClientContext)
      throws RepositoryException {
    String endpoint = Util.encodeURL(inSharepointClientContext.getSiteURL())
        + SPConstants.ALERTSENDPOINT;
    LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

    final AlertsLocator loc = new AlertsLocator();
    loc.setAlertsSoapEndpointAddress(endpoint);
    final Alerts alertsService = loc;

    try {
      stub = (AlertsSoap_BindingStub) alertsService.getAlertsSoap();
    } catch (final ServiceException e) {
      LOGGER.log(Level.WARNING, e.getMessage(), e);
      throw new SharepointException("Unable to create SPAlertsWS stub.");
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
   * Gets information about the alerts defined within the current web site.
   *
   * @return a AlertInfo
   * @throws RemoteException
   */
  public AlertInfo getAlerts() throws RemoteException {
    return stub.getAlerts();
  }
}
