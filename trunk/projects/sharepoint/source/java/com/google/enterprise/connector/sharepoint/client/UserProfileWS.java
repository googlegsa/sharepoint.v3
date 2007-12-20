package com.google.enterprise.connector.sharepoint.client;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.QuickLinkData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileService;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceLocator;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceSoap_BindingStub;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData;


/*import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.GetUserProfileByIndexResult;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.QuickLinkData;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileService;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceLocator;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceSoapStub;
import com.google.enterprise.connector.sharepoint.generated.userprofileservice.ValueData;*/

/**
 * @author amit_kagrawal
 * This class holds data and methods for any call to UserProfile Web Service.
 * */
public class UserProfileWS {
	private static final String USERPROFILEENDPOINT = "_vti_bin/UserProfileService.asmx";
	private static final Logger LOGGER = Logger.getLogger(UserProfileWS.class.getName());
	private SharepointClientContext sharepointClientContext;
	private String className = SiteDataWS.class.getName();
	//private UserProfileServiceSoapStub stub;
	private UserProfileServiceSoap_BindingStub stub;
	static final String URL_SEP ="://";
	private static final String SLASH = "/";
	private WebsWS websWS;
	public static String personalSpaceTag = "PersonalSpace";
	public static URLEncoder enc  = new URLEncoder();

	static{
		//set the URLEncoder safe characters
		enc.addSafeCharacter('/');
		enc.addSafeCharacter(':');// required when endpoint is set using specified site
	}
	public UserProfileWS(SharepointClientContext inSharepointClientContext) throws SharepointException{
		String sFunctionName = "UserProfileWS(SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFunctionName);
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
			String endpoint = inSharepointClientContext.getProtocol()+URL_SEP + inSharepointClientContext.getHost() + ":" +inSharepointClientContext.getPort() + /*Util.getEscapedSiteName(*/enc.encode(inSharepointClientContext.getsiteName())/*)*/
			+ SLASH+USERPROFILEENDPOINT;

//			System.out.println("UProfileEndPt: "+endpoint);
			UserProfileServiceLocator loc = new UserProfileServiceLocator();
			loc.setUserProfileServiceSoapEndpointAddress(endpoint);

			UserProfileService service = loc;
			try {
				stub = (UserProfileServiceSoap_BindingStub) service.getUserProfileServiceSoap();
				websWS = new WebsWS(inSharepointClientContext);
			} catch (ServiceException e) {
				LOGGER.finer("UserProfileWS: "+e.toString());
				throw new SharepointException("Unable to create the userprofile stub"); 
			}	

			//get the credentials
			String strDomain = inSharepointClientContext.getDomain();
			String strUserName = inSharepointClientContext.getUsername();
			strDomain+="\\"+strUserName;
			String strPassword = inSharepointClientContext.getPassword();

			//set authentication
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}
		LOGGER.exiting(className, sFunctionName);
	}

	public boolean isSPS() throws SharepointException{
		String sFunctionName = "isSPS()";
		LOGGER.entering(className, sFunctionName);
		if(stub==null){
			throw new SharepointException("UserProfile stub not found");
		}

		try{
			stub.getUserProfileByIndex(0);
			LOGGER.info("SPS site");
			LOGGER.exiting(className, sFunctionName);
			return true;
		}catch(AxisFault fault){
			LOGGER.info("WSS site");
			LOGGER.exiting(className, sFunctionName);
			return false;
		} catch (Exception e) {
			LOGGER.warning(e.toString());
			LOGGER.exiting(className, sFunctionName);
			return false;
		}

	}

	public List getPersonalSiteList() throws SharepointException {
		String sFunctionName = "getPersonalSiteList()";
		LOGGER.entering(className, sFunctionName);
		if(stub==null){
			throw new SharepointException("Unable to get the userprofile stub");
		}
		ArrayList lstAllPersonalSites = new ArrayList(); //list of personal sites and subsites
		Collator collator = SharepointConnectorType.getCollator();

		int index = 0;
		while (index >= 0) {

			GetUserProfileByIndexResult result = null;
			try {

				result = stub.getUserProfileByIndex(index);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			if (result == null || result.getUserProfile() == null) {
				break;
			}

			PropertyData[] data= result.getUserProfile();
			if (data == null) {
				break;
			}


			String /*acct = null,*/ space = null;
			for (int i = 0; i < data.length; ++i) {
				String name = data[i].getName();
				if (collator.equals(personalSpaceTag,name)) {
					ValueData[] vd = data[i].getValues();

					if ((vd == null) || (vd .length <1)) {
						continue;
					}

					space = (String) vd[0].getValue();

					String strMySiteBaseURL = sharepointClientContext.getMySiteBaseURL();
					
					//added by amit to support mysite base url even with trailing slash
					if (strMySiteBaseURL.endsWith("/")) {
						strMySiteBaseURL = strMySiteBaseURL.substring(0, strMySiteBaseURL.lastIndexOf("/"));
					}
					
					String strURL = strMySiteBaseURL+space;

					if (strURL.endsWith("/")) {
						strURL = strURL.substring(0, strURL.lastIndexOf("/"));
					}
//					System.out.println("Personal: "+strURL);
					//add filters to the list of personal sites 
					String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
					String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
					SharepointClientUtils spUtils = new SharepointClientUtils();  
					
					try {
						if(spUtils.isIncludedUrl(includedURLs,excludedURLs, strURL)) {
					//		System.out.println("--passed");
							lstAllPersonalSites.add(strURL);
						}else{
							LOGGER.warning(sFunctionName+" : excluding "+strURL.toString());
						}
					} catch (Exception e) {
						LOGGER.finer(e.toString());
					}

				}
			}
			if (space == null) {
				break;
			}
			String next = result.getNextValue();
			index = Integer.parseInt(next);
		}
		
		if(lstAllPersonalSites!=null){
			LOGGER.info("Total Personal sites returned: "+lstAllPersonalSites);
		}
		LOGGER.exiting(className, sFunctionName);
		return lstAllPersonalSites;
	}

	public List getMyLinks() throws SharepointException {
		String sFunctionName = "getMyLinks()"; 
		LOGGER.entering(className, sFunctionName);
		if(stub==null){
			throw new SharepointException("Unable to get the userprofile stub");
		}

		ArrayList lstAllMyLinks = new ArrayList(); //list of personal sites and subsites
		int index = 0;
		while (index >= 0) {

			GetUserProfileByIndexResult result = null;
			try {
				result = stub.getUserProfileByIndex(index);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			if (result == null || result.getUserProfile() == null) {
				break;
			}


			QuickLinkData[] links = result.getQuickLinks();

			if(links==null){
				break;
			}
			String url=null;
			for(int iLink=0;iLink<links.length;++iLink){
				url = links[iLink].getUrl();
//				System.out.println("MyLink: "+url);
//check for the include exclude patterns
				String[] includedURLs = sharepointClientContext.getIncludedURlList() ; 
				String[] excludedURLs = sharepointClientContext.getExcludedURlList() ; 
				SharepointClientUtils spUtils = new SharepointClientUtils();   
				try {
					if(spUtils.isIncludedUrl(includedURLs,excludedURLs, url)) {
//						System.out.println("--passed");
						try{
							String strWebURL=websWS.getWebURLFromPageURL(url);
//							System.out.println("Web URL: "+strWebURL);
							LOGGER.config("Web URL: "+strWebURL);
							//lstAllMyLinks.add(url);
							lstAllMyLinks.add(strWebURL);

						}catch(Throwable e){
							LOGGER.finer("getMyLinks(): "+e.toString());
						}
					}else{
						LOGGER.warning(sFunctionName+" : excluding "+url.toString());
					}
				} catch (Exception e) {
					LOGGER.warning(e.toString());
				}
			}

			String next = result.getNextValue();
			index = Integer.parseInt(next);
		}
		
		if(lstAllMyLinks!=null){
			LOGGER.info("Total MyLinks returned: "+lstAllMyLinks.size());
		}
		LOGGER.exiting(className, sFunctionName);
		return lstAllMyLinks;
	}




}
