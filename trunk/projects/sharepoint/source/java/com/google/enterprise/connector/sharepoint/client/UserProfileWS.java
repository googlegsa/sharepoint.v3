package com.google.enterprise.connector.sharepoint.client;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.Util;
import com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub;
import com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.ArrayOfQuickLinkData;
import com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.ArrayOfValueData;
import com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.QuickLinkData;
import com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.ValueData;

/**
 * @author amit_kagrawal
 * This class holds data and methods for any call to UserProfile Web Service.
 * */
public class UserProfileWS {
	private static final String USERPROFILEENDPOINT = "_vti_bin/UserProfileService.asmx";
	private static final Log LOGGER = LogFactory.getLog(UserProfileWS.class);
	private SharepointClientContext sharepointClientContext;
	private UserProfileServiceStub stub;
	final String URL_SEP ="://";
	
	public static String personalSpaceTag = "PersonalSpace";
	
	public UserProfileWS(SharepointClientContext inSharepointClientContext){
		this.sharepointClientContext = inSharepointClientContext;

    	try {
    		String endpoint = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() + ":" +inSharepointClientContext.getPort() + Util.getEscapedSiteName(inSharepointClientContext.getsiteName())
    		+ USERPROFILEENDPOINT;

			stub = new UserProfileServiceStub(endpoint);
			inSharepointClientContext.setStubWithAuth(stub, endpoint);
		}catch (AxisFault e) {
			 //Note: The UserProfileService web service is a part of SPS and is not included in WSS.
			LOGGER.warn(e);
		}catch (Exception e) {
			LOGGER.warn(e);
	    }    
	}
	
	public boolean isSPS(){
	    try {
	    	UserProfileServiceStub.GetUserProfileByIndex getUserProfileByIndex = new UserProfileServiceStub.GetUserProfileByIndex();
	        getUserProfileByIndex.setIndex(0);
	        stub.GetUserProfileByIndex(getUserProfileByIndex);

	        LOGGER.info("SPS site");
	        return true;
	      } catch (AxisFault fault) {
	    	  LOGGER.info("WSS site");
	        return false;
	      } catch (Exception e) {
	    	  LOGGER.warn(e);
	        return false;
	      }				
	}
	
	public List getPersonalSiteList() {
		ArrayList lstAllPersonalSites = new ArrayList(); //list of personal sites and subsites
		Collator collator = SharepointConnectorType.getCollator();
	    int index = 0;
	    while (index >= 0) {
	    	UserProfileServiceStub.GetUserProfileByIndex req = new UserProfileServiceStub.GetUserProfileByIndex();
	      req.setIndex(index);
	      UserProfileServiceStub.GetUserProfileByIndexResult result = null;
	      try {
		      UserProfileServiceStub.GetUserProfileByIndexResponse response = stub.GetUserProfileByIndex(req);
		      result = response.getGetUserProfileByIndexResult();
	      } catch (Exception e) {
	    	  LOGGER.warn(e);
	    	  return lstAllPersonalSites;
	      }
	      if (result == null || result.getUserProfile() == null) {
	        break;
	      }
	      PropertyData data[] = result.getUserProfile().getPropertyData();
	      if (data == null) {
	        break;
	      }
	      
	      
	      String /*acct = null,*/ space = null;
	      for (int i = 0; i < data.length; ++i) {
	        String name = data[i].getName();
	        if (collator.equals(personalSpaceTag,name)) {
	          ArrayOfValueData values = data[i].getValues();
	          ValueData[] vd = values.getValueData();
	          if (vd == null) {
	            continue;
	          }
	          space = vd[0].getValue().getText();
	          
	          String strMySiteBaseURL = sharepointClientContext.getMySiteBaseURL();
	          String strURL = strMySiteBaseURL+space;
	          
	          if (strURL.endsWith("/")) {
	        	  strURL = strURL.substring(
	  					0, strURL.lastIndexOf("/"));
	  			}
	          LOGGER.debug("Personal URL: "+strURL);
	          lstAllPersonalSites.add(strURL);
	          
	        }
	      }
	      if (space == null) {
	        break;
	      }
	      String next = result.getNextValue();
	      index = Integer.parseInt(next);
	    }
		return lstAllPersonalSites;
	 }

	public List getMyLinks() {
		ArrayList lstAllMyLinks = new ArrayList(); //list of personal sites and subsites
		
	    int index = 0;
	    while (index >= 0) {
	     UserProfileServiceStub.GetUserProfileByIndex req = new UserProfileServiceStub.GetUserProfileByIndex();
		 if(req!=null){	
		      req.setIndex(index);
		  }
	      UserProfileServiceStub.GetUserProfileByIndexResult result = null;
	      try {
		      UserProfileServiceStub.GetUserProfileByIndexResponse response = stub.GetUserProfileByIndex(req);
		      result = response.getGetUserProfileByIndexResult();
	      } catch (Exception e) {
	    	  LOGGER.warn(e);
	    	  return lstAllMyLinks;
	      }
	      if (result == null || result.getUserProfile() == null) {
	        break;
	      }
	      
	      ArrayOfQuickLinkData arrQuickLinks = result.getQuickLinks();
	      QuickLinkData[] links = arrQuickLinks.getQuickLinkData();
	      if(links==null){
	    	  break;
	      }
	      String url=null;
	      for(int iLink=0;iLink<links.length;++iLink){
	    	  url = links[iLink].getUrl();
	    	  System.out.println("MyLink: "+url);
	    	  
	    	  //check for the include exclude patterns
	    	  String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
				String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
				SharepointClientUtils spUtils = new SharepointClientUtils();   
				try {
					if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) {
						lstAllMyLinks.add(url);
					}
				} catch (Exception e) {
					LOGGER.warn(e);
				}
	      }
	     
	      String next = result.getNextValue();
	      index = Integer.parseInt(next);
	    }
		return lstAllMyLinks;
	 }



	
}
