//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.transport.http.HTTPConstants;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.authentication.Authentication;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationLocator;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationMode;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.authentication.LoginResult;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

public class AuthenticationWS {
    private String endpoint;
    private AuthenticationSoap_BindingStub stub = null;
    private final Logger LOGGER = Logger.getLogger(WebsWS.class.getName());
    private SharepointClientContext sharepointClientContext;

    /**
     * @param siteUrl SharePoint Site URL. The URL must be ending with slash and
     *            should not contain page file name e.g default.aspx
     * @throws SharepointException
     */
    public AuthenticationWS(SharepointClientContext sharePointClientContext,
            String siteUrl) throws SharepointException {
        if (null == sharePointClientContext) {
            throw new SharepointException("SharePointContext can not be null. ");
        }
        this.sharepointClientContext = sharePointClientContext;
        if (null == siteUrl) {
            siteUrl = sharePointClientContext.getSiteURL();
        }
        endpoint = Util.encodeURL(siteUrl);
        if (endpoint.endsWith("/")) {
            endpoint += SPConstants.AUTHENTICATIONENDPOINT;
        } else {
            endpoint += "/" + SPConstants.AUTHENTICATIONENDPOINT;
        }
        LOGGER.log(Level.INFO, "Endpoint set to: " + endpoint);

        final AuthenticationLocator loc = new AuthenticationLocator();
        loc.setAuthenticationSoapEndpointAddress(endpoint);
        final Authentication service = loc;

        try {
            stub = (AuthenticationSoap_BindingStub) service.getAuthenticationSoap();
        } catch (final ServiceException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            throw new SharepointException(
                    "Unable to create authentication stub");
        }
        stub.setMaintainSession(true);

        stub.setUsername(Util.getUserNameWithDomain(sharePointClientContext.getUsername(), sharePointClientContext.getDomain()));
        stub.setPassword(sharePointClientContext.getPassword());
    }

    /**
     * Checks the authentication mode applied on the web application
     *
     * @return
     */
    public AuthenticationMode mode() {
        try {
            return stub.mode();
        } catch (AxisFault af) {
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    return stub.mode();
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to detetct authentication mode using Authentication WS. ", e);
                    return null;
                }
            } else {
                LOGGER.log(Level.WARNING, "Failed to detetct authentication mode using Authentication WS. ", af);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to detetct authentication mode using Authentication WS. ", e);
            return null;
        }
    }

    /**
     * Authenticates against a form based protetcted site and returns a login
     * cookie which can be used for making further web service calls
     *
     * @return
     */
    public String login() {
        LoginResult loginResult = null;
        try {
            loginResult = stub.login(sharepointClientContext.getUsername(), sharepointClientContext.getPassword());
        } catch (AxisFault af) {
            if ((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1)
                    && (sharepointClientContext.getDomain() != null)) {
                final String username = Util.switchUserNameFormat(stub.getUsername());
                LOGGER.log(Level.INFO, "Web Service call failed for username [ "
                        + stub.getUsername() + " ].");
                LOGGER.log(Level.INFO, "Trying with " + username);
                stub.setUsername(username);
                try {
                    loginResult = stub.login(sharepointClientContext.getUsername(), sharepointClientContext.getPassword());
                } catch (final Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to detetct authentication mode using Authentication WS. ", e);
                    return null;
                }
            } else {
                LOGGER.log(Level.WARNING, "Failed to detetct authentication mode using Authentication WS. ", af);
                return null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to detetct authentication mode using Authentication WS. ", e);
            return null;
        }

        if (null == loginResult) {
            LOGGER.log(Level.WARNING, "LoginResult returned by WS is null.");
            return null;
        }

        try {
            if(null != loginResult && null != loginResult.getCookieName()) {
                Call call = stub._getCall();
                MessageContext context = call.getMessageContext();
                if (null != context) {
                    Object obj = context.getProperty(HTTPConstants.HEADER_COOKIE);
                    if (null != obj) {
                        if(obj instanceof String) {
                            String loginCookie = (String) obj;
                            String[] cookieEntry = loginCookie.split("=");
                            if (null != cookieEntry
                                    && cookieEntry.length == 2
                                    && loginResult.getCookieName().equals(cookieEntry[0])) {
                                return loginCookie;
                            } else {
                                LOGGER.log(Level.WARNING, "Cookie [ "
                                        + loginCookie
                                        + " ] is not valid. Expected cookie name was [ "
                                        + loginResult.getCookieName());
                            }
                        } else if(obj instanceof String[]) {
                            String[] allCookies = (String[]) obj;
                            for (String cookie : allCookies) {
                                String[] cookieEntry = cookie.split("=");
                                if (null != cookieEntry
                                        && cookieEntry.length == 2
                                        && loginResult.getCookieName().equals(cookieEntry[0])) {
                                    return cookie;
                                }
                                LOGGER.log(Level.WARNING, "Expected cookie [ "
                                        + loginResult.getCookieName()
                                        + " ] is not available in the current set of cookies in the current message context. ");
                            }
                        } else {
                            LOGGER.log(Level.WARNING, "Cookie's can not be parsed because its type is [ " + obj.getClass() + " ]. ");
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "Expected cookie [ "
                                + loginResult.getCookieName()
                                + " ] is not found in the current message context. ");
                    }
                } else {
                    LOGGER.log(Level.WARNING, "MessageContext is not available. ");
                }
            } else {
                LOGGER.log(Level.WARNING, "Either LoginResult is null or no cookie reference is found. ");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Problem occured while authenticating. ", e);
            return null;
        }
        LOGGER.log(Level.WARNING, "Failed to retrieve cookie from the Message context. ");
        return null;
    }
}
