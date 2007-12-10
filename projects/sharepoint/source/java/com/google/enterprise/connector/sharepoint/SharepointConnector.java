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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;

/**
 * Implementation of the Connector interface from the spi for SharePoint.
 */
public class SharepointConnector implements Connector {
	private static final Log LOGGER = LogFactory.getLog(SharepointConnector.class);
	private SharepointClientContext sharepointClientContext = null;

	public SharepointConnector(String sharepointUrl, String domain, 
			String username, String password, String googleConnectorWorkDir,String includedURls,String excludedURls,String mySiteBaseURL,String aliasHostName,String aliasPort) { // changed by A. Mitra for URL Exclusion
		String sFunctionName = "SharepointConnector(String sharepointUrl, String domain,String username, String password, String googleConnectorWorkDir,String includedURls,String excludedURls,String mySiteBaseURLString aliasHostName,String aliasPort)";
		LOGGER.debug(sFunctionName+": sharepointUrl = [" +sharepointUrl+"] , domain = ["+domain+"] , username = ["+username+"] , googleConnectorWorkDir = ["+googleConnectorWorkDir+"] , includedURls = ["+includedURls+"] , excludedURls = ["+excludedURls+"] , mySiteBaseURL = ["+mySiteBaseURL+"] , aliasHostName = ["+aliasHostName+" ] ,aliasPort=["+aliasPort+"]");
		sharepointClientContext = new SharepointClientContext(sharepointUrl,domain, username, password, googleConnectorWorkDir,includedURls,excludedURls,mySiteBaseURL,aliasHostName,aliasPort);  // changed by A. Mitra for URL Exclusion
	}

	public void setDomain(String domain) {
		if(sharepointClientContext != null) { 
			sharepointClientContext.setDomain(domain);
		}
	}

	public void setHost(String host) {
		if(sharepointClientContext != null) {
		sharepointClientContext.setHost(host);
		}
	}

	public void setPort(int port) {
		if(sharepointClientContext != null) {
		sharepointClientContext.setPort(port);
		}
	}

	/**
	 * @author amit_kagrawal
	 * Set port number, domain, sitename and protocol
	 * */
	public void setSharepointUrl(String sharepointUrl) {
		String sFunctionName = "setSharepointUrl(String sharepointUrl)";
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
				LOGGER.warn(sFunctionName+ ": "+e.getMessage());
			}
		}
	}

	public void setUsername(String username) {
		if(sharepointClientContext != null) {
		sharepointClientContext.setUsername(username);
		}
	}

	public void setPassword(String password) {
		if(sharepointClientContext != null) {
		sharepointClientContext.setPassword(password);
		}
	}  

	public void setGoogleConnectorWorkDir(String workDir) {
		if(sharepointClientContext != null) {
		sharepointClientContext.setGoogleConnectorWorkDir(workDir);
		}
	}

	/* (non-Javadoc)
	 * @see com.google.enterprise.connector.spi.Connector#login()
	 */
	public Session login() throws RepositoryException {
		return new SharepointSession(this, sharepointClientContext);
	}
}
