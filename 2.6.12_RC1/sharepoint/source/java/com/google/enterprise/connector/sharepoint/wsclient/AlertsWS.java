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

package com.google.enterprise.connector.sharepoint.wsclient;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.client.SPConstants.FeedType;
import com.google.enterprise.connector.sharepoint.generated.alerts.Alert;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertInfo;
import com.google.enterprise.connector.sharepoint.generated.alerts.Alerts;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertsLocator;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants.ActionType;

import org.apache.axis.AxisFault;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

/**
 * Java Client for calling Alerts.asmx Provides a layer to talk to the Alerts
 * Web Service on the SharePoint server Any call to this Web Service must go
 * through this layer.
 *
 * @author nitendra_thakur
 */
public class AlertsWS {
    private final Logger LOGGER = Logger.getLogger(AlertsWS.class.getName());
    private SharepointClientContext sharepointClientContext;
    private String endpoint;
    private AlertsSoap_BindingStub stub;

    /**
     * @param inSharepointClientContext The Context is passed so that necessary
     *            information can be used to create the instance of current
     *            class Web Service endpoint is set to the default SharePoint
     *            URL stored in SharePointClientContext.
     * @throws SharepointException
     */
    public AlertsWS(final SharepointClientContext inSharepointClientContext)
            throws RepositoryException {
        if (inSharepointClientContext != null) {
            sharepointClientContext = inSharepointClientContext;
            endpoint = Util.encodeURL(sharepointClientContext.getSiteURL())
                    + SPConstants.ALERTSENDPOINT;
            LOGGER.log(Level.CONFIG, "Endpoint set to: " + endpoint);

            try {
                final AlertsLocator loc = new AlertsLocator();
                loc.setAlertsSoapEndpointAddress(endpoint);
                final Alerts alertsService = loc;

                try {
                    stub = (AlertsSoap_BindingStub) alertsService.getAlertsSoap();
                } catch (final ServiceException e) {
                    LOGGER.log(Level.WARNING, e.getMessage());
                    throw e;
                }

                final String strDomain = sharepointClientContext.getDomain();
                String strUser = sharepointClientContext.getUsername();
                final String strPassword = sharepointClientContext.getPassword();

                strUser = Util.getUserNameWithDomain(strUser, strDomain);
                stub.setUsername(strUser);
                stub.setPassword(strPassword);
            } catch (final Throwable e) {
                LOGGER.log(Level.WARNING, "Unable to connect to alerts service stub.", e);
                throw new SharepointException(e.toString());
            }
        }
    }

    /**
     * Get the list of alerts that the current user has access to on the web
     * represeted by WebState
     *
     * @param parentWeb From which the laerts are to be accessed
     * @param alertListState represents the list that is created for alerts
     * @return list of {@link SPDocument}
     */
    public List<SPDocument> getAlerts(final WebState parentWeb,
            final ListState alertListState) {
        final ArrayList<SPDocument> lstAllAlerts = new ArrayList<SPDocument>();
        if (stub == null) {
            LOGGER.log(Level.WARNING, "Unable to get the alerts. stub is nulll.");
            return lstAllAlerts;
        }
        if (alertListState == null) {
            LOGGER.log(Level.WARNING, "Unable to get the alerts. alertListState is nulll.");
            return lstAllAlerts;
        }

        // To keep track of those IDs which are deleted
        final StringBuffer knownAlerts = alertListState.getIDs();
        final StringBuffer currentAlerts = new StringBuffer();

        AlertInfo alertsInfo = null;
        try {
            try {
                alertsInfo = stub.getAlerts();
            } catch (final AxisFault af) { // Handling of username formats for
                                            // different authentication models.
                if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                        && (sharepointClientContext.getDomain() != null)) {
                    final String username = Util.switchUserNameFormat(stub.getUsername());
                    LOGGER.log(Level.CONFIG, "Web Service call failed for username [ "
                            + stub.getUsername()
                            + " ]. Trying with "
                            + username);
                    stub.setUsername(username);
                    try {
                        alertsInfo = stub.getAlerts();
                    } catch (final Exception e) {
                        LOGGER.log(Level.WARNING, "Unable to get alerts. endpoint [ "
                                + endpoint + " ]", e);
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Unable to get alerts. endpoint [ "
                            + endpoint + " ]", af);
                }
            } catch (final Throwable e) {
                LOGGER.log(Level.WARNING, "Unable to get alerts. endpoint [ "
                        + endpoint + " ]", e);
            }

            if (alertsInfo != null) {
                final Alert[] alerts = alertsInfo.getAlerts();
                if ((alerts == null) || (alerts.length == 0)) {
                    alertListState.setExisting(false);
                    return lstAllAlerts;
                }

                alertListState.setExisting(true);
                parentWeb.setExisting(true);

                for (Alert element : alerts) {
                    // add the alert in the List
                    final Calendar c = Calendar.getInstance();
                    c.setTime(new Date());// Alerts do not fetch the date .. set
                                            // it it current time by default

                    LOGGER.config("Fetched(Alert): ID=" + element.getId()
                            + "|AlertForTitle: " + element.getAlertForTitle()
                            + "|AlertForUrl: " + element.getAlertForUrl()
                            + "|Title: " + element.getTitle() + "|EditURL: "
                            + element.getEditAlertUrl() + "|Date: " + c);

                    String docId = element.getId();

                    // Keep track of the currently identified IDs.
                    currentAlerts.append(docId);

                    // Delete the IDs if they are found. At last, all the left
                    // IDs are considered to be deleted.
                    // Send only those alerts which are newly added.
                    final int idPos = knownAlerts.indexOf(docId);
                    if (idPos == -1) {
                        if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                            docId = SPConstants.ALERT_SUFFIX_IN_DOCID
                                    + alertListState.getListURL()
                                    + SPConstants.DOC_TOKEN + docId;
                        }
                        final SPDocument doc = new SPDocument(docId,
                                element.getEditAlertUrl(), c,
                                SPConstants.NO_AUTHOR, SPConstants.ALERTS_TYPE,
                                parentWeb.getTitle(),
                                sharepointClientContext.getFeedType(),
                                parentWeb.getSharePointType());

                        lstAllAlerts.add(doc);
                    } else {
                        knownAlerts.delete(idPos, idPos + docId.length());
                    }
                } // End of For

                // Create delete feed docs for all those alerts which have been
                // deleted.
                if (FeedType.CONTENT_FEED == sharepointClientContext.getFeedType()) {
                    final Pattern pat = Pattern.compile("\\{.+\\}");
                    final Matcher match = pat.matcher(knownAlerts);
                    if (match.find()) {
                        final String idPart = match.group();
                        final String docId = SPConstants.ALERT_SUFFIX_IN_DOCID
                                + alertListState.getListURL()
                                + SPConstants.DOC_TOKEN + idPart;
                        final SPDocument doc = new SPDocument(docId,
                                alertListState.getListURL(),
                                alertListState.getLastModCal(),
                                SPConstants.NO_AUTHOR, SPConstants.ALERTS_TYPE,
                                parentWeb.getTitle(),
                                sharepointClientContext.getFeedType(),
                                parentWeb.getSharePointType());
                        doc.setAction(ActionType.DELETE);
                        lstAllAlerts.add(doc);
                    }
                }
                alertListState.setIDs(currentAlerts);
            } else {
                alertListState.setExisting(false);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, "Problem while getting alerts", e);
        }
        return lstAllAlerts;
    }
}
