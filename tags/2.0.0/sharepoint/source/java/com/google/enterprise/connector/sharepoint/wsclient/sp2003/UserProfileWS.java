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

package com.google.enterprise.connector.sharepoint.wsclient.sp2003;

import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;

import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.UserProfileService;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.UserProfileServiceLocator;
import com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.UserProfileServiceSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

/**
 * Java Client for calling UserProfile.asmx for SharePoint 2003
 * Provides a layer to talk to the UserProfile Web Service on the SharePoint server 2003
 * Any call to this Web Service must go through this layer.
 * @author nitendra_thakur
 *
 */
public class UserProfileWS {
	private final Logger LOGGER = Logger.getLogger(UserProfileWS.class.getName());
	private SharepointClientContext sharepointClientContext;
	private UserProfileServiceSoap_BindingStub stub;
	String endpoint;
	
	private final String personalSpaceTag = "PersonalSpace";
	
	/**
	 * @param inSharepointClientContext  The Context is passed so that necessary information can be used to create the instance of current class
	 * Web Service endpoint is set to the default SharePoint URL stored in SharePointClientContext.
	 * @throws SharepointException
	 */
	public UserProfileWS(final SharepointClientContext inSharepointClientContext) throws SharepointException{
		if(inSharepointClientContext!=null){
			sharepointClientContext = inSharepointClientContext;
			endpoint = Util.encodeURL(sharepointClientContext.getSiteURL()) + SPConstants.USERPROFILEENDPOINT;			
			LOGGER.log(Level.INFO, "Endpoint set to: "+endpoint);
			
			final UserProfileServiceLocator loc = new UserProfileServiceLocator();
			loc.setUserProfileServiceSoapEndpointAddress(endpoint);

			final UserProfileService service = loc;
			try {
				stub = (UserProfileServiceSoap_BindingStub) service.getUserProfileServiceSoap();
			} catch (final ServiceException e) {
				LOGGER.log(Level.WARNING,e.getMessage(),e);
				throw new SharepointException("Unable to create the userprofile stub");
			}	

			final String strDomain = inSharepointClientContext.getDomain();
			String strUserName = inSharepointClientContext.getUsername();			
			final String strPassword = inSharepointClientContext.getPassword();

			strUserName = Util.getUserNameWithDomain(strUserName, strDomain);
			stub.setUsername(strUserName);
			stub.setPassword(strPassword);
		}
	}

	/**
	 * Checks to see if the current web to which the web service endpioint is set is an SPS site.
	 * @return if the endpoint being used is an SPS site
	 * @throws SharepointException
	 */
	public boolean isSPS() throws SharepointException{
		if(stub==null){
			throw new SharepointException("UserProfile stub not found");
		}
		try{
			stub.getUserProfileByIndex(0);
			LOGGER.info("SPS site");
			return true;
		}catch(final AxisFault fault){
			if((SPConstants.UNAUTHORIZED.indexOf(fault.getFaultString()) != -1) && (sharepointClientContext.getDomain() != null)) {
				final String username = Util.switchUserNameFormat(stub.getUsername());
				LOGGER.log(Level.INFO,"Web Service call failed for username [ " + stub.getUsername()+" ].");
				LOGGER.log(Level.INFO,"Trying with " + username);
				stub.setUsername(username);
				try {
					stub.getUserProfileByIndex(0);
					LOGGER.info("SPS site");
					return true;
				} catch(final Exception e) {
					LOGGER.log(Level.WARNING,"Unable to call getUserProfileByIndex(0). endpoint [ "+endpoint+" ].",e);
					return false;											
				}
			} else {
				LOGGER.info("WSS site");
				return false;					
			}			
		} catch (final Exception e) {
			LOGGER.warning(e.getMessage());
			return false;
		}

	}

	/**
	 * To get all the personal sites from the current web.
	 * @return the list of personal sites
	 * @throws SharepointException
	 */
	public Set<String> getPersonalSiteList() throws SharepointException {
		final Set<String> personalSitesSet = new TreeSet<String>(); //list of personal sites and subsites
		
		final Collator collator = Util.getCollator();
		if(stub==null){
			LOGGER.warning("Unable to get personal sites because userprofile stub is null");
			return personalSitesSet;
		}
		int index = 0;
		while (index >= 0) {

			GetUserProfileByIndexResult result = null;
			try {
				result = stub.getUserProfileByIndex(index);
			} catch(final AxisFault fault){
				if((SPConstants.UNAUTHORIZED.indexOf(fault.getFaultString()) != -1) && (sharepointClientContext.getDomain() != null)) {
					final String username = Util.switchUserNameFormat(stub.getUsername());
					LOGGER.log(Level.INFO,"Web Service call failed for username [ " + stub.getUsername()+" ].");
					LOGGER.log(Level.INFO,"Trying with " + username);
					stub.setUsername(username);
					try {
						result = stub.getUserProfileByIndex(index);
					} catch(final Exception e) {
						LOGGER.log(Level.WARNING,"Unable to get Personal sites as call to getUserProfileByIndex("+index+") has failed. endpoint [ "+endpoint+" ].",e);																		
					}
				} else {
					LOGGER.log(Level.WARNING,"Unable to get Personal sites as call to getUserProfileByIndex("+index+") has failed. endpoint [ "+endpoint+" ].",fault);
				}			
			} catch (final Exception e) {
				LOGGER.log(Level.WARNING,"Unable to get Personal sites as call to getUserProfileByIndex("+index+") has failed. endpoint [ "+endpoint+" ].",e);					
			} 
			
			if ((result == null) || (result.getUserProfile() == null)) {
				break;
			}

			final PropertyData[] data= result.getUserProfile();
			if (data == null) {
				break;
			}

			final String space = null;
			for (PropertyData element : data) {
				final String name = element.getName();
				if (collator.equals(personalSpaceTag,name)) {
					final String propVal = element.getValue();//e.g. /personal/administrator/
					if (propVal == null) {
						continue;
					}
					String strURL = Util.getWebApp(sharepointClientContext.getSiteURL()) + propVal;

					if (strURL.endsWith(SPConstants.SLASH)) {
						strURL = strURL.substring(0, strURL.lastIndexOf(SPConstants.SLASH));
					}
					if(sharepointClientContext.isIncludedUrl(strURL)) {
						personalSitesSet.add(strURL);
						LOGGER.log(Level.INFO, "Personal Site: " + strURL);
					}else{
						LOGGER.log(Level.WARNING, "excluding " + strURL);
					}					
				}
			}
			if (space == null) {
				break;
			}
			final String next = result.getNextValue();
			index = Integer.parseInt(next);
		}
		if(personalSitesSet!=null){
			LOGGER.info("Total personal sites returned: "+personalSitesSet.size());
		}
		return personalSitesSet;
	}

}
