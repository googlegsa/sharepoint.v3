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

package com.google.enterprise.connector.sharepoint;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

/**
 * Implementation of the Connector interface from the spi for SharePoint.
 * @author amit_kagrawal
 */
public class SharepointConnector implements Connector {
	private static final Logger LOGGER =  Logger.getLogger(SharepointConnector.class.getName());
	
	private SharepointClientContext sharepointClientContext = null;
	private ArrayList whiteList=null;
    private ArrayList blackList=null;
    private boolean fqdnConversion = false;
    private String className = SharepointConnector.class.getName();
    
    /**
     * 
     * constructor.
     */
    public SharepointConnector(String sharepointUrl, String domain, 
			String username, String password, String googleConnectorWorkDir,String includedURls,String excludedURls,String mySiteBaseURL,String aliasHostName,String aliasPort,String spType) { 
		
		String sFunctionName = "SharepointConnector(String sharepointUrl, String domain,String username, String password, String googleConnectorWorkDir,String includedURls,String excludedURls,String mySiteBaseURLString aliasHostName,String aliasPort,String sharepointType)";
		LOGGER.entering(className, sFunctionName);
		LOGGER.config(sFunctionName+": sharepointUrl = [" +sharepointUrl+"] , domain = ["+domain+"] , username = ["+username+"] , googleConnectorWorkDir = ["+googleConnectorWorkDir+"] , includedURls = ["+includedURls+"] , excludedURls = ["+excludedURls+"] , mySiteBaseURL = ["+mySiteBaseURL+"] , aliasHostName = ["+aliasHostName+" ] ,aliasPort=["+aliasPort+"] ,sharepointType=["+spType+"]");
		sharepointClientContext = new SharepointClientContext(spType,sharepointUrl,domain, username, password, googleConnectorWorkDir,includedURls,excludedURls,mySiteBaseURL,aliasHostName,aliasPort,whiteList,blackList);  // changed by A. Mitra for URL Exclusion
		sharepointClientContext.setFQDNConversion(fqdnConversion);
		LOGGER.exiting(className, sFunctionName);
	}

    /**
     * 
     * @return
     */
	public boolean isFQDNConversion() {
		String sFuncName = "isFQDNConversion()";
		LOGGER.entering(className, sFuncName);
		LOGGER.config("FQDNValue: "+fqdnConversion);
		LOGGER.exiting(className, sFuncName);
		return fqdnConversion;
	}

	/**
	 * sets the FQDNConversion parameter.
	 * @param conversion
	 * If true: tries to convert the non-FQDN URLs to FQDN
	 * If false: no conversion takes place   
	 * */
	public void setFQDNConversion(boolean conversion) {
		String sFuncName = "setFQDNConversion(boolean conversion)";
		LOGGER.entering(className , sFuncName);
		LOGGER.config(className+":"+sFuncName+": FQDN Value Set to ["+conversion+"]");
		
		fqdnConversion = conversion;
		sharepointClientContext.setFQDNConversion(conversion);
		
		LOGGER.exiting(className, sFuncName);
	}

	/**
	 * 
	 * @param domain
	 */
	public void setDomain(String domain) {
		String sFuncName = "setDomain(String domain)";
		LOGGER.entering(className, sFuncName);
		if(sharepointClientContext != null) { 
			sharepointClientContext.setDomain(domain);
		}
		LOGGER.exiting(className, sFuncName);
	}

	/**
	 * 
	 * @param host
	 */
	public void setHost(String host) {
		String sFuncName = "setHost(String host)";
		LOGGER.entering(className, sFuncName);
		if(sharepointClientContext != null) {
			sharepointClientContext.setHost(host);
		}
		LOGGER.exiting(className, sFuncName);
	}

	/**
	 * 
	 * @param port
	 */
	public void setPort(int port) {
		String sFuncName = "setPort(int port)";
		LOGGER.entering(className, sFuncName);
		if(sharepointClientContext != null) {
			sharepointClientContext.setPort(port);
		}
		LOGGER.exiting(className, sFuncName);
	}

	/**
	 * @author amit_kagrawal
	 * Set port number, domain, sitename and protocol
	 * */
	public void setSharepointUrl(String sharepointUrl) {
		String sFunctionName = "setSharepointUrl(String sharepointUrl)";
		LOGGER.entering(className, sFunctionName);
		if(sharepointUrl!=null && sharepointClientContext != null){
			try {
				URL url = new URL(sharepointUrl);
				sharepointClientContext.setHost(url.getHost());
				if (-1 != url.getPort()) {
					sharepointClientContext.setPort(url.getPort());
				}else{
					sharepointClientContext.setPort(url.getDefaultPort());
				}
				sharepointClientContext.setsiteName(url.getPath());
				sharepointClientContext.setProtocol(url.getProtocol());
			} catch (MalformedURLException e) {
				LOGGER.warning(sFunctionName+ ": "+e.getMessage());
			}
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * 
	 * @param username
	 */
	public void setUsername(String username) {
		String sFunctionName = "setUsername(String username)";
		LOGGER.entering(className, sFunctionName);
		if(sharepointClientContext != null) {
			sharepointClientContext.setUsername(username);
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		String sFunctionName = "setPassword(String password)";
		LOGGER.entering(className, sFunctionName);
		if(sharepointClientContext != null) {
			sharepointClientContext.setPassword(password);
		}
		LOGGER.exiting(className, sFunctionName);
	}  

	/**
	 * 
	 * @param workDir
	 */
	public void setGoogleConnectorWorkDir(String workDir) {
		String sFunctionName = "setGoogleConnectorWorkDir(String workDir)";
		LOGGER.entering(className, sFunctionName);
		if(sharepointClientContext != null) {
			sharepointClientContext.setGoogleConnectorWorkDir(workDir);
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/** 
	 * 
	 * @see com.google.enterprise.connector.spi.Connector#login()
	 */
	public Session login() throws RepositoryException {
		String sFunctionName = "login()";
		LOGGER.entering(className, sFunctionName);
		LOGGER.info(className+":"+sFunctionName);
		LOGGER.exiting(className, sFunctionName);
		return new SharepointSession(this, sharepointClientContext);
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList getBlackList() {
		String sFunctionName = "getBlackList()";
		LOGGER.entering(className, sFunctionName);
		LOGGER.exiting(className, sFunctionName);
		return blackList;
	}

	/**
	 * 
	 * @param inBlackList
	 */
	public void setBlackList(ArrayList inBlackList) {
		String sFunctionName = "setBlackList(ArrayList blackList)";
		LOGGER.entering(className, sFunctionName);
		this.blackList = inBlackList;
		if(sharepointClientContext!=null){
			sharepointClientContext.setBlackList(blackList);
		}
		LOGGER.exiting(className, sFunctionName);
		
	}

	/**
	 * 
	 * @return
	 */
	public ArrayList getWhiteList() {
		String sFunctionName = "getWhiteList()";
		LOGGER.entering(className, sFunctionName);
		LOGGER.exiting(className, sFunctionName);
		return whiteList;
	}

	/**
	 * 
	 * @param inWhiteList
	 */
	public void setWhiteList(ArrayList inWhiteList) {
		String sFunctionName = "setWhiteList(ArrayList whiteList)";
		LOGGER.entering(className, sFunctionName);
		this.whiteList = inWhiteList;
		if(sharepointClientContext!=null){
			sharepointClientContext.setWhiteList(whiteList);
		}
		LOGGER.exiting(className, sFunctionName);
	}

}
