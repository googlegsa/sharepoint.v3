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

package com.google.enterprise.connector.sharepoint.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;




/**
 * Class to hold the context information for sharepoint client connection.
 */
public class SharepointClientContext implements Cloneable {

	private static final Logger LOGGER = Logger.getLogger(SharepointClientContext.class.getName());
	private String className = SharepointClientContext.class.getName();
	private String siteName;
	private String domain;
	private String username;
	private String password;
	private int port = 0;
	private String host;
	private String protocol;
	private String strSharePointType=null;//This denotes the sharepoint Type e.g SP2003 or SP2007
	//Note: SP2003 covers SPS2003 and WSS 2.0
	//Note: SP2007 covers MOSS2007 and WSS 3.0
	private String googleConnectorWorkDir = null;
	private String [] excludedURlList = null; 
	private String [] includedURlList = null; 
	private String mySiteBaseURL = null; 
	private String aliasHostName = null; 
	private String aliasPort = null;
	private ArrayList whiteList = null;
	private ArrayList blackList = null;
	private static boolean bFQDNConversion = false;
	private static final String SEPARATOR = " ";
	private static final int SSL_DEFAULT_PORT = 443;
	private static final String FRONT_SLASH = "/";
	

	//Default locale is en-US
	static ResourceBundle resourcebundle =ResourceBundle.getBundle("SharepointConnectorResources",Locale.US);

	public Object clone() {
		String sFunctionName ="clone()";
		LOGGER.entering(className, sFunctionName);
		try {
			SharepointClientContext spCl = new SharepointClientContext();

			//sharepoint type
			if(this.strSharePointType==null){
				spCl.setSharePointType(this.strSharePointType);
			}else{
				spCl.setSharePointType(new String(this.strSharePointType));
			}

			if(this.aliasHostName==null){
				spCl.setAliasHostName(this.aliasHostName);
			}else{
				spCl.setAliasHostName(new String(this.aliasHostName));
			}

			//check for alias port
			if(this.aliasPort==null){
				spCl.setAliasPort(this.aliasPort);
			}else{
				spCl.setAliasPort(new String(this.aliasPort));
			}
			if(this.domain==null){
				spCl.setDomain(this.domain);
			}else{
				spCl.setDomain(new String(this.domain));
			}

			if(this.googleConnectorWorkDir==null){
				spCl.setGoogleConnectorWorkDir(this.googleConnectorWorkDir);
			}else{
				spCl.setGoogleConnectorWorkDir(new String(this.googleConnectorWorkDir));
			}

			if(this.host==null){
				spCl.setHost(this.host);
			}else{
				spCl.setHost(new String(this.host));
			}

			if(this.mySiteBaseURL==null){
				spCl.setMySiteBaseURL(this.mySiteBaseURL);
			}else{
				spCl.setMySiteBaseURL(new String(this.mySiteBaseURL));
			}

			if(this.password==null){
				spCl.setPassword(null);
			}else{
				spCl.setPassword(new String(this.password));
			}


			spCl.setPort(this.port);//primitive

			if(this.protocol==null){
				spCl.setProtocol(null);
			}else{
				spCl.setProtocol(new String(this.protocol));
			}

			if(this.siteName==null){
				spCl.setsiteName(null);
			}else{
				spCl.setsiteName(new String(this.siteName));
			}

//			spCl.setIncludedURlList(this.includedURlList);

			if(this.excludedURlList==null){
				spCl.setExcludedURlList(this.excludedURlList);	
			}else{
				String [] newExcList = new String[excludedURlList.length] ;
				for(int i=0;i<this.excludedURlList.length;++i){
					newExcList[i] = new String(excludedURlList[i].toString());
				}
				spCl.setExcludedURlList(newExcList);
			}

			if(this.includedURlList==null){
				spCl.setIncludedURlList(this.includedURlList);	
			}else{
				String [] newIncList = new String[includedURlList.length] ;
				for(int i=0;i<this.includedURlList.length;++i){
					newIncList[i] = new String(includedURlList[i].toString());
				}
				spCl.setIncludedURlList(newIncList);
			}

			if(username==null){
				spCl.setUsername(this.username);
			}else{
				spCl.setUsername(new String(this.username));
			}
			LOGGER.exiting(className, sFunctionName);
			return spCl;
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}

	}
	//added by Amit
	private void setExcludedURlList(String[] excludedURlList2) {
		String sFunctionName ="setExcludedURlList(String[] excludedURlList2)";
		LOGGER.entering(className, sFunctionName);
		if(excludedURlList2!=null){
			this.excludedURlList = excludedURlList2;
		}
		LOGGER.exiting(className, sFunctionName);
	}

	//added by Amit
	private void setIncludedURlList(String[] includedURlList2) {
		String sFunctionName ="setIncludedURlList(String[] includedURlList2)";
		LOGGER.entering(className, sFunctionName);
		if(includedURlList2!=null){
			this.includedURlList = includedURlList2;
		}
		LOGGER.exiting(className, sFunctionName);
	}


	//added by Amit for cloning
	public SharepointClientContext() {
		// TODO Auto-generated constructor stub
	}
	public SharepointClientContext(String sharepointType,String sharepointUrl, String inDomain,
			String inUsername, String inPassword,
			String inGoogleConnectorWorkDir,String includedURls,String excludedURls,String inMySiteBaseURL,String inAliasHostName,String inAliasPort,ArrayList inWhiteList,ArrayList inBlackList){
		String sFunctionName ="SharepointClientContext(String sharepointType,String sharepointUrl, String inDomain,String inUsername, String inPassword,String inGoogleConnectorWorkDir,String includedURls,String excludedURls,String inMySiteBaseURL,String inAliasHostName,String inAliasPort,ArrayList whiteList, ArrayList blackList)";
		LOGGER.entering(className, sFunctionName);
//		System.out.println("SharepointClientContext: "+sharepointType+":::"+sharepointUrl+":::"+ inDomain+":::"+
//		inUsername+":::"+ inPassword+":::"+inGoogleConnectorWorkDir+":::"+includedURls+":::"+excludedURls+":::"+inMySiteBaseURL+":::"+inAliasHostName+":::"+inAliasPort);
		Protocol.registerProtocol("https", new Protocol("https",new EasySSLProtocolSocketFactory(), SSL_DEFAULT_PORT));
		if(sharepointUrl==null){
			LOGGER.severe("SharepointClientContext: sharepoint URL is null");
			return;
		}

		//set the sharepointType
		if(sharepointType!=null){
			this.strSharePointType = sharepointType;
		}

		if (sharepointUrl.endsWith("/")) {
			sharepointUrl = sharepointUrl.substring(
					0, sharepointUrl.lastIndexOf("/"));
		}
		try {
//			System.out.println("clientcontext: url="+sharepointUrl);
			URL url = new URL(sharepointUrl);
			this.host = url.getHost();
			this.protocol = url.getProtocol(); //to remove the hard-coded protocol
			if (-1 != url.getPort()) {
				this.port = url.getPort();
			}else{
				this.port = url.getDefaultPort();
			}
			this.siteName = url.getPath();      
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		this.domain = inDomain;    
		if(domain==null){
			LOGGER.warning("domain not set");
		}
		this.username = inUsername;
		if(username==null){
			LOGGER.warning("User Name not found");
		}

		this.password = inPassword;
		if(password==null){
			LOGGER.warning("password not found");
		}

		this.googleConnectorWorkDir = inGoogleConnectorWorkDir;

		setExcludedURlList(excludedURls,SEPARATOR);  
		setIncludedURlList(includedURls, SEPARATOR); 
		this.mySiteBaseURL = inMySiteBaseURL;  
		this.aliasHostName = inAliasHostName;  
		this.aliasPort = inAliasPort;
		this.whiteList = inWhiteList;
		this.blackList = inBlackList;


		LOGGER.config(" sharepointUrl = [" +sharepointUrl+"] , domain = ["+inDomain+"] , username = ["+inUsername+"] , googleConnectorWorkDir = ["+inGoogleConnectorWorkDir+"] , includedURls = ["+includedURls+"] , excludedURls = ["+excludedURls+"] , mySiteBaseURL = ["+inMySiteBaseURL+"], aliasHostName = ["+inAliasHostName+" ] ,aliasPort=["+inAliasPort+"]");
		LOGGER.exiting(className, sFunctionName);
	}

	public void setURL(String sharepointUrl){
		String sFunctionName = "setURL(String sharepointUrl)";
		LOGGER.entering(className, sFunctionName);
		try {
			URL url = new URL(sharepointUrl);
			this.host = url.getHost();
			this.protocol = url.getProtocol(); //to remove the hard-coded protocol
			if (-1 != url.getPort()) {
				this.port = url.getPort();
			}
			this.siteName = url.getPath();      
		} catch (MalformedURLException e) {
			LOGGER.warning(sFunctionName +": "+e.toString());
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * @author amit_kagrawal
	 * */
	public String getProtocol(){
		return protocol;
	}

	public String getDomain() {
		return domain;
	}

	public String getHost() {
		return host;
	}

	public String getPassword() {
		return password;
	}

	public int getPort() {
		return port;
	}

	public String getsiteName() {
		return siteName;
	}

	public String getUsername() {
		return username;
	}

	public String getGoogleConnectorWorkDir() {
		return this.googleConnectorWorkDir;
	}

	public void setDomain(String indomain) {
		if(indomain!=null){
			this.domain = indomain;
		}
	}

	public void setPassword(String inPassword) {
		if(inPassword!=null){
			this.password = inPassword;
		}
	}

	/**
	 * @author amit_kagrawal
	 * */
	public void setProtocol(String inProtocol){
		if(inProtocol!=null){
			this.protocol = inProtocol;
		}
	}

	public void setsiteName(String siteNameNew) {
		if(siteNameNew!=null){
			this.siteName = siteNameNew;
		}
	}

	public void setUsername(String inUsername) {
		if(inUsername!=null){
			this.username = inUsername;
		}
	}

	public void setHost(String inHost) {
		if(inHost!=null){
			this.host = inHost;
		}
	}

	public void setPort(int inPort) {
		this.port = inPort;
	}

	public void setGoogleConnectorWorkDir(String workDir) {
		if(workDir!=null){
			this.googleConnectorWorkDir = workDir;
		}
	}

	/**
	 * Sets the stub .
	 * @param stub Axis Client Stub to call the webservices on 
	 * Sharepoint server.
	 * @param endpoint Suffix to the particular webservice to use.
	 */
	/*	public void setStubWithAuth(Stub stub, String endpoint) {
		Options options = new Options();
		EndpointReference target = new EndpointReference(endpoint);
		options.setTo(target);
		HttpTransportProperties.Authenticator auth = 
			new HttpTransportProperties.Authenticator();
		auth.setDomain(domain);
		auth.setUsername(username);
		auth.setPassword(password);
		auth.setHost(host);
		auth.setRealm(domain);
		auth.setPort(port);    
		options.setProperty(HTTPConstants.AUTHENTICATE, auth);    
		stub._getServiceClient().setOptions(options);
		return;
	}*/

	/**
	 * added by A Mitra.
	 * @return
	 */
	public String[] getExcludedURlList() {
		return excludedURlList;
	}

	/**
	 * added by A Mitra.
	 * @param excludedURls
	 * @param separator
	 */
	public void setExcludedURlList(String excludedURls , String separator) {
		if(excludedURls != null){
			excludedURlList = removeFrontSlash(excludedURls.split(separator));
		}
	}

	/**
	 * added by A Mitra.
	 * @param excludedURls
	 */

	public void setExcludedURlList(String excludedURls){
		if(excludedURls != null){
			excludedURlList = removeFrontSlash(excludedURls.split(SEPARATOR));
		}
	}

	/**
	 * 
	 * @param includedURls
	 * @param separator
	 */


	public void setIncludedURlList(String includedURls , String separator) {
		if(includedURls != null){
			includedURlList = removeFrontSlash(includedURls.split(separator));
		}
	}

	/**
	 * added by A Mitra.
	 * @param includedURls
	 */

	public void setIncludedURlList(String includedURls){
		if(includedURls != null){
			includedURlList = removeFrontSlash(includedURls.split(SEPARATOR));
		}
	}

	public String[] getIncludedURlList() {
		return includedURlList;
	}

	public String getMySiteBaseURL() {
		return mySiteBaseURL;
	}

	public void setMySiteBaseURL(String inMySiteBaseURL) {
		if(inMySiteBaseURL!=null){
			this.mySiteBaseURL = inMySiteBaseURL;
		}
	}

	public String getAliasHostName() {
		return aliasHostName;
	}

	public void setAliasHostName(String inAliasHostName) {
		if(inAliasHostName!=null){
			this.aliasHostName = inAliasHostName;
		}
	}

	public String getAliasPort() {
		return aliasPort;
	}

	public void setAliasPort(String inAliasPort) {
		if(inAliasPort!=null){
			this.aliasPort = inAliasPort;
		}
	}

	public static ResourceBundle getResourcebundle() {
		return resourcebundle;
	}

	public static void setResourcebundle(ResourceBundle resourcebundle1) {
		if(resourcebundle1!=null){
			SharepointClientContext.resourcebundle = resourcebundle1;
		}
	}
//	SharePointType e.g. SP2007 or SP2003
	public String getSharePointType() {
		return strSharePointType;
	}
	public void setSharePointType(String inSharePointType) {
		if(inSharePointType!=null){
			strSharePointType = inSharePointType;
		}
	}
	public ArrayList getBlackList() {
		return blackList;
	}
	public void setBlackList(ArrayList inBlackList) {
		this.blackList = inBlackList;
	}
	public ArrayList getWhiteList() {
		return whiteList;
	}
	public void setWhiteList(ArrayList inWhiteList) {
		this.whiteList = inWhiteList;
	}
	public boolean isFQDNConversion() {
		return bFQDNConversion;
	}
	public void setFQDNConversion(boolean conversion) {
		bFQDNConversion = conversion;
//		System.out.println("SharepointClientContext : setFQDNConversion : "+bFQDNConversion);
	}

	private String[] removeFrontSlash(String[] patterns){
		String sFunctionName = "removeFrontSlash(String[] patterns)";
		LOGGER.entering(className, sFunctionName);
		if(patterns!=null){
			int length = patterns.length;
			for(int i = 0 ; i<length ; i++) {
				String strPtrn = patterns[i];
				if(strPtrn != null){
					if(strPtrn.endsWith(FRONT_SLASH)){
						strPtrn = strPtrn.substring(0,(strPtrn.length()-FRONT_SLASH.length()));	
						if(strPtrn!=null){
							patterns[i] = strPtrn;
						}
					}

				}
			}
		}
		LOGGER.exiting(className, sFunctionName);
		return patterns;
	}
}
