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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscovery;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoveryLocator;
import com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * Java Client for calling GSSiteDiscovery.asmx.
 * Provides a layer to talk to the GSSiteDiscovery Web Service deployed on the SharePoint server 
 * Any call to this Web Service must go through this layer.
 * @author nitendra_thakur
 */
public class GSSiteDiscoveryWS {
	private final Logger LOGGER = Logger.getLogger(GSSiteDiscoveryWS.class.getName());
	private SharepointClientContext sharepointClientContext;
	private String endpoint;
	private SiteDiscoverySoap_BindingStub stub = null;
	
	/**
	 * @param inSharepointClientContext  The Context is passed so that necessary information can be used to create the instance of current class
	 * Web Service endpoint is set to the default SharePoint URL stored in SharePointClientContext.
	 * @throws SharepointException
	 */
	public GSSiteDiscoveryWS(final SharepointClientContext inSharepointClientContext)throws SharepointException {
		if(inSharepointClientContext!=null){
			sharepointClientContext = inSharepointClientContext;			
			endpoint = Util.encodeURL(sharepointClientContext.getSiteURL()) + SPConstants.GSPSITEDISCOVERYWS_END_POINT;
			LOGGER.log(Level.INFO, "Endpoint set to: "+endpoint);
			
			final SiteDiscoveryLocator loc = new SiteDiscoveryLocator();
			loc.setSiteDiscoverySoapEndpointAddress(endpoint);
			final SiteDiscovery gspSiteDiscovery = loc;
			try {
				stub = (SiteDiscoverySoap_BindingStub) gspSiteDiscovery.getSiteDiscoverySoap();
			} catch (final ServiceException e) {
				LOGGER.log(Level.WARNING,e.getMessage(),e);
				throw new SharepointException("Unable to get the GSSiteDiscovery stub");
			}

			final String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			final String strPassword= inSharepointClientContext.getPassword();

			strUser = Util.getUserNameWithDomain(strUser, strDomain);
			stub.setUsername(strUser);
			stub.setPassword(strPassword);
		}
	}

	/**
	 * Gets all the sitecollections from all the web applications for a given sharepoint installation
	 * @return the set of all site colelltions returned bu the GSSiteDiscovery
	 */
	public Set<String> getMatchingSiteCollections(){
		final Set<String> siteCollections = new TreeSet<String>();
		Object[] res = null;
		try {
			res = stub.getAllSiteCollectionFromAllWebApps();
		} catch(final AxisFault af) { // Handling of username formats for different authentication models.
			if((SPConstants.UNAUTHORIZED.indexOf(af.getFaultString()) != -1) && (sharepointClientContext.getDomain() != null)) {
				final String username = Util.switchUserNameFormat(stub.getUsername());
				LOGGER.log(Level.INFO,"Web Service call failed for username [ " + stub.getUsername()+" ].");
				LOGGER.log(Level.INFO,"Trying with " + username);
				stub.setUsername(username);
				try {
					res = stub.getAllSiteCollectionFromAllWebApps();
				} catch(final Exception e) {
					LOGGER.log(Level.WARNING,"Call to the GSSiteDiscovery web service failed with the following exception: ",e);
					return siteCollections;											
				}
			} else {
				LOGGER.log(Level.WARNING,"Call to the GSSiteDiscovery web service failed with the following exception: ",af);
				return siteCollections;					
			}
		} catch(final Throwable e){
			LOGGER.log(Level.WARNING,"Call to the GSSiteDiscovery web service failed with the following exception: ",e);
			return siteCollections;
		}

		if(null!=res){
			for (Object element : res) {
				String url =(String) element;
				URL u =null;
				try {
					u= new URL(url);
				} catch (final MalformedURLException e1) {
					LOGGER.log(Level.WARNING,"Malformed site collection URL found [ "+url+" ]",e1);
					continue;
				}
				
				int iPort = u.getPort();
				if(iPort==-1){
					iPort=u.getDefaultPort();
				}
				
				url = u.getProtocol() + SPConstants.URL_SEP + getFQDNHost(u.getHost()) + SPConstants.COLON + iPort;
				
				final String path = u.getPath();
				if((path==null) || path.equalsIgnoreCase("")) {
					url += SPConstants.SLASH;
				} else {
					url += path;
				}
				
				if (sharepointClientContext.isIncludedUrl(url)) { 
					siteCollections.add(url);
				}else{
					LOGGER.warning("excluding "+url);						
				}				
			}
		}
		LOGGER.log(Level.INFO,"GSSiteDiscovery SiteCollection URLs:" + siteCollections);
		return siteCollections;
	}
	/**
	 * 
	 * @param hostName
	 * @return the the host in FQDN format
	 */
	public String getFQDNHost(final String hostName){
		if(sharepointClientContext.isFQDNConversion()){
			InetAddress ia = null;
			try {
				ia = InetAddress.getByName(hostName);
			} catch (final UnknownHostException e) {
				LOGGER.log(Level.WARNING,"Host cannot be identified [ "+hostName+" ]",e);
			}
			if(ia!=null){
				return ia.getCanonicalHostName();
			}
		}
		return hostName;
	}
}
