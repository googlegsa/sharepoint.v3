// Copyright 2011 Google Inc.
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
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationLocator;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationMode;
import com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.authentication.LoginResult;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;
import com.google.enterprise.connector.sharepoint.wsclient.client.AclWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.AlertsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.AuthenticationWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.BaseWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.BulkAuthorizationWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.ClientFactory;
import com.google.enterprise.connector.sharepoint.wsclient.client.ListsWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDataWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.SiteDiscoveryWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2003WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfile2007WS;
import com.google.enterprise.connector.sharepoint.wsclient.client.UserProfileChangeWS;
import com.google.enterprise.connector.sharepoint.wsclient.client.WebsWS;

import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.params.HttpClientParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

/**
 * A factory for the interfaces that encapsulates the SharePoint
 * webservices.
 */
public class SPClientFactory implements ClientFactory {
  private static final Logger LOGGER =
      Logger.getLogger(SPClientFactory.class.getName());
  private static final int HTTP_CLIENT_TIMEOUT_SECONDS = 300;
  
  private final Map<String, FormsAuthenticationHandler> authenticationHandlers
      = new HashMap<String, FormsAuthenticationHandler>();
  private ScheduledThreadPoolExecutor scheduledExecutor = 
      new ScheduledThreadPoolExecutor(1);
 
  private static class Resource {
    private final HttpClient httpClient;
    private final Set<String> webAppsVisited;
    
    public Resource (HttpClient inHttpClient) {
      httpClient = inHttpClient;
      webAppsVisited = new TreeSet<String>();
    }
  }
  
  private final BlockingQueue<Resource> resources =
      new ArrayBlockingQueue<Resource>(4);

  /**
   * Gets the instance of the alerts web service.
   *
   * @return a new alerts web service instance.
   */
  public AlertsWS getAlertsWS(final SharepointClientContext ctx) {
    try {    
      SPAlertsWS alert = new SPAlertsWS(ctx);
      addFormsAuthenticationCookie(ctx, alert);
      return alert;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create alerts web service instance.", e);
      return null;
    }
  }
  


  /**
   * Gets the instance of the bulk authorization web service.
   *
   * @return a new bulk authorization service instance.
   */
  public BulkAuthorizationWS getBulkAuthorizationWS(
      final SharepointClientContext ctx) {
    try {
      GSBulkAuthorizationWS bulkAuthz = new GSBulkAuthorizationWS(ctx);
      addFormsAuthenticationCookie(ctx, bulkAuthz);
      return bulkAuthz;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING, "Failed to initialize GSBulkAuthorizationWS.",
          e);
      return null;
    }
  }

  /**
   * Gets the instance of the lists web service.
   *
   * @return a new lists web service instance.
   */
  public ListsWS getListsWS(final SharepointClientContext ctx,
      final String rowLimit) {
    try {
      SPListsWS listWS = new SPListsWS(ctx, rowLimit);
      addFormsAuthenticationCookie(ctx, listWS);
      return listWS;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create lists web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the site data web service.
   *
   * @return a new site data web service instance.
   */
  public SiteDataWS getSiteDataWS(final SharepointClientContext ctx) {
    try {
      SPSiteDataWS siteData = new SPSiteDataWS(ctx);
      addFormsAuthenticationCookie(ctx, siteData);
      return siteData;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create site data web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the 2003 user profile web service.
   *
   * @return a new 2003 user profile web service instance.
   */
  public UserProfile2003WS getUserProfile2003WS(
      final SharepointClientContext ctx) {
    try {
      com.google.enterprise.connector.sharepoint.wsclient.soap.
          sp2003.SPUserProfileWS userProfile 
          = new com.google.enterprise.connector.sharepoint.wsclient.soap.
          sp2003.SPUserProfileWS(ctx);
      addFormsAuthenticationCookie(ctx, userProfile);
      return userProfile;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create user profile web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the 2007 user profile web service.
   *
   * @return a new 2007 user profile web service instance.
   */
  public UserProfile2007WS getUserProfile2007WS(
      final SharepointClientContext ctx) {
    try {
      SPUserProfileWS userProfile = new SPUserProfileWS(ctx);
      addFormsAuthenticationCookie(ctx, userProfile);
      return userProfile;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create user profile web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the user profile web service.
   *
   * @return a new user profile web service instance.
   */
  public WebsWS getWebsWS(final SharepointClientContext ctx) {
    try {
      SPWebsWS webs = new SPWebsWS(ctx);
      addFormsAuthenticationCookie(ctx, webs);
      return webs;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create webs web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the ACL web service.
   *
   * @return a new ACL service instance.
   */
  public AclWS getAclWS(final SharepointClientContext ctx, String webUrl) {
    try {
      GSAclWS aclWS = new GSAclWS(webUrl);
      addFormsAuthenticationCookie(ctx, aclWS);
      return aclWS;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create ACLs web service instance.", e);
      return null;
    }
  }

  /**
   * Gets the instance of the site discovery web service.
   *
   * @return a new site discovery service instance.
   */
  public SiteDiscoveryWS getSiteDiscoveryWS(final SharepointClientContext ctx,
      String webUrl) {
    try {
      GSSiteDiscoveryWS siteDiscovery = new GSSiteDiscoveryWS(ctx, webUrl);
      addFormsAuthenticationCookie(ctx, siteDiscovery);
      return siteDiscovery;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create site discovery web service instance.", e);
      return null;
    }
  }

  public int checkConnectivity(HttpMethodBase method, Credentials credentials,
      SharepointClientContext ctx) throws IOException {
    Resource resource = reserveResource(credentials);
    String currentWebApp = Util.getWebApp(method.getURI().getURI());
    try {
      List<String> cookie = getFormsAuthenCookie(ctx.getSiteURL(), ctx);
      if (cookie != null) {
        method.addRequestHeader(
            HTTPConstants.HEADER_COOKIE, cookie.get(0));
      }
    } catch (Exception e) {
      throw new IOException(e);
    }
    try {
      int responseCode = resource.httpClient.executeMethod(method);
      if (responseCode == 200) {
        // Add web app entry when response code is 200
        resource.webAppsVisited.add(currentWebApp);
      }
      if (responseCode != 200 && responseCode != 404 && responseCode != 400) {
        LOGGER.log(Level.WARNING,
            "Http Response Code = "+ responseCode + " for Url [ "
                + method.getURI() + " ].");

        if (responseCode == 401 &&
            resource.webAppsVisited.contains(currentWebApp)) {
          LOGGER.log(Level.WARNING, "Not reinitializing HTTP Client after "
              + "[ 401 ] response as connection to Web Application [ "
              + currentWebApp
              + " ] was successful earlier with existing HTTP Client Object.");
          return responseCode;
        }

        LOGGER.log(Level.WARNING, "Reinitializing HTTP Client as [ "
            + responseCode + " ] response received.");
        resource = new Resource(createHttpClient(credentials));        
        responseCode = resource.httpClient.executeMethod(method);
        if (responseCode == 200) {
          // Add web app entry when response code is 200
          resource.webAppsVisited.add(currentWebApp);
        }
      }
      returnResource(resource);
      return responseCode;      
    } catch(Exception ex) {    
      LOGGER.log(Level.WARNING,
          "Error Connecting Server for Url [ "
              + method.getURI() + " ]. Reinitializing HttpClient.", ex);
      resource = new Resource(createHttpClient(credentials));      
      int responseCode = resource.httpClient.executeMethod(method);
      if (responseCode == 200) {
        // Add web app entry when response code is 200
        resource.webAppsVisited.add(currentWebApp);       
      }
      returnResource(resource);
      return responseCode;
    }
  }
  
  private HttpClient createHttpClient(Credentials credentials) {
    HttpClient httpClientToUse = new HttpClient();

    HttpClientParams params = httpClientToUse.getParams();
    // Fix for the Issue[5408782] SharePoint connector fails to traverse a site,
    // circular redirect exception is observed.
    params.setBooleanParameter(HttpClientParams.ALLOW_CIRCULAR_REDIRECTS, true);
    // If ALLOW_CIRCULAR_REDIRECTS is set to true, HttpClient throws an
    // exception if a series of redirects includes the same resources more than
    // once. MAX_REDIRECTS allows you to specify a maximum number of redirects
    // to follow.
    params.setIntParameter(HttpClientParams.MAX_REDIRECTS, 10);
    
    params.setLongParameter(HttpClientParams.CONNECTION_MANAGER_TIMEOUT,
        HTTP_CLIENT_TIMEOUT_SECONDS * 1000);
    params.setIntParameter(HttpClientParams.SO_TIMEOUT,
        HTTP_CLIENT_TIMEOUT_SECONDS * 1000);
    httpClientToUse.getState().setCredentials(AuthScope.ANY, credentials);
    return httpClientToUse;
  }
  
  private Resource reserveResource(Credentials credentials) throws IOException {
    Resource resource = null;
    try {
      LOGGER.log(Level.FINEST,
          "Number of resources in resource pool = " + resources.size());
      resource = resources.poll(0, TimeUnit.SECONDS);      
    } catch (InterruptedException e) {      
      throw new IOException("Unable to reserve resource", e);      
    }
    // Create new resource.
    if (resource == null) {
      resource = new Resource(createHttpClient(credentials));
    } else {
      // Clear cookies if reusing http client from
      // resource pool.
      resource.httpClient.getState().clearCookies();
    }
    return resource;
  }
  
  private void returnResource(Resource resource) {
    resources.offer(resource);
  }
  
  public String getResponseHeader(HttpMethodBase method, String headerName) {
    String headerValue = null;
    final Header header = method.getResponseHeader(headerName);
    if (null != header) {
      headerValue = header.getValue();
    }
    return headerValue;
  }

  /**
   * Gets the instance of the User Profile Change web service.
   *
   * @return a new User Profile Change service instance.
   */
  public UserProfileChangeWS getUserProfileChangeWS(
      SharepointClientContext ctx) {  
    try {
      SPUserProfileChangeWS profileChange = new SPUserProfileChangeWS(ctx);
      addFormsAuthenticationCookie(ctx, profileChange);
      return profileChange;
    } catch (final Exception e) {
      LOGGER.log(Level.WARNING,
          "Unable to create SharePoint User" 
              +" Profile Change web service instance.", e);
      return null;
    }
  }
  
  private void addFormsAuthenticationCookie(
      SharepointClientContext ctx, BaseWS ws) throws Exception {
    List<String> cookie = getFormsAuthenCookie(ctx.getSiteURL(), ctx);
    ws.setFormsAuthenticationCookie(cookie);
  }




  private List<String> getFormsAuthenCookie(
      String url, SharepointClientContext ctx) throws Exception {
    String webApp = Util.getWebApp(url);
    FormsAuthenticationHandler handler;
    if (authenticationHandlers.containsKey(webApp)) {
      handler = authenticationHandlers.get(webApp);
    } else {
      handler = new FormsAuthenticationHandler(webApp, scheduledExecutor, ctx);
      handler.start();
      authenticationHandlers.put(webApp, handler);     
    }
    
    if (!handler.isFormsAuthentication()) {
      return null;
    }
    
    return handler.getAuthenticationCookies();
  }



  @Override
  public void shutdown() {   
    scheduledExecutor.shutdown();
    try {     
      scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }
    
    scheduledExecutor.shutdownNow();  
    scheduledExecutor = null; 
  }
 
}

