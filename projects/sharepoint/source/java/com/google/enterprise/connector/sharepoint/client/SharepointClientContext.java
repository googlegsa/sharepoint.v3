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
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to hold the context information for sharepoint client connection.
 */
public class SharepointClientContext implements Cloneable {

	private static Log logger = LogFactory.getLog(SharepointClientContext.class);
	private String siteName;
	private String domain;
	private String username;
	private String password;
	private int port = 0;
	private String host;
	private String protocol;
	private String googleConnectorWorkDir = null;
	private String [] excludedURlList = null; 
	private String [] includedURlList = null; 
	private String mySiteBaseURL = null; 
	private String aliasHostName = null; 
	private String aliasPort = null; 
	private static final String SEPARATOR = ",";
	private static final int SSL_DEFAULT_PORT = 443;

	//Default locale is en-US
	static ResourceBundle resourcebundle =ResourceBundle.getBundle("SharepointConnectorResources",Locale.US);
	  public Object clone() {
		    /*try {
		      return super.clone();
		    } catch (CloneNotSupportedException e) {
		      return null;
		    }*/

			try {
				SharepointClientContext spCl = new SharepointClientContext();
				
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
				
//				spCl.setIncludedURlList(this.includedURlList);
				
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
				
				return spCl;
			} catch (Throwable e) {
				e.printStackTrace();
				return null;
			}
	 }
		//added by Amit
	  private void setExcludedURlList(String[] excludedURlList2) {
		  if(excludedURlList2!=null){
			  this.excludedURlList = excludedURlList2;
		  }
	}

	//added by Amit
	  private void setIncludedURlList(String[] includedURlList2) {
		  if(includedURlList2!=null){
			  this.includedURlList = includedURlList2;
		  }
	}


	//added by Amit for cloning
	  public SharepointClientContext() {
			// TODO Auto-generated constructor stub
		}
	public SharepointClientContext(String sharepointUrl, String inDomain,
			String inUsername, String inPassword,
			String inGoogleConnectorWorkDir,String includedURls,String excludedURls,String inMySiteBaseURL,String inAliasHostName,String inAliasPort) {
		 Protocol.registerProtocol("https", new Protocol("https",new EasySSLProtocolSocketFactory(), SSL_DEFAULT_PORT));
		 if(sharepointUrl==null){
			 logger.error("SharepointClientContext: sharepoint URL is null");
			 return;
		 }
		 
		if (sharepointUrl.endsWith("/")) {
			sharepointUrl = sharepointUrl.substring(
					0, sharepointUrl.lastIndexOf("/"));
		}
		try {
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
			logger.warn("domain not set");
		}
		this.username = inUsername;
		if(username==null){
			logger.warn("User Name not found");
		}

		this.password = inPassword;
		if(password==null){
			logger.warn("password not found");
		}

		this.googleConnectorWorkDir = inGoogleConnectorWorkDir;
		
		setExcludedURlList(excludedURls,SEPARATOR);  
		setIncludedURlList(includedURls, SEPARATOR); 
		this.mySiteBaseURL = inMySiteBaseURL;  
		this.aliasHostName = inAliasHostName;  
		this.aliasPort = inAliasPort;  
		logger.debug(" sharepointUrl = [" +sharepointUrl+"] , domain = ["+inDomain+"] , username = ["+inUsername+"] , googleConnectorWorkDir = ["+inGoogleConnectorWorkDir+"] , includedURls = ["+includedURls+"] , excludedURls = ["+excludedURls+"] , mySiteBaseURL = ["+inMySiteBaseURL+"], aliasHostName = ["+inAliasHostName+" ] ,aliasPort=["+inAliasPort+"]");
	}
	
	public void setURL(String sharepointUrl){
		String sFunctionName = "setURL(String sharepointUrl)";
		try {
			URL url = new URL(sharepointUrl);
			this.host = url.getHost();
			this.protocol = url.getProtocol(); //to remove the hard-coded protocol
			if (-1 != url.getPort()) {
				this.port = url.getPort();
			}
			this.siteName = url.getPath();      
		} catch (MalformedURLException e) {
			logger.warn(sFunctionName +": "+e.toString());
		}
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
	public void setStubWithAuth(Stub stub, String endpoint) {
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
	}

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
	 */
	public void setExcludedURlList(String excludedURls , String separator) {
		if(excludedURls != null){
			excludedURlList = excludedURls.split(separator);
		}
	}

	/**
	 * added by A Mitra.
	 * @param excludedURls
	 */

	public void setExcludedURlList(String excludedURls){
		if(excludedURls != null){
			excludedURlList = excludedURls.split(SEPARATOR);
		}
	}

	/**
	 * 
	 * @param includedURls
	 * @param separator
	 */


	public void setIncludedURlList(String includedURls , String separator) {
		if(includedURls != null){
			includedURlList = includedURls.split(separator);
		}
	}

	/**
	 * added by A Mitra.
	 * @param excludedURls
	 */

	public void setIncludedURlList(String includedURls){
		if(includedURls != null){
			includedURlList = includedURls.split(SEPARATOR);
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

	public static void setResourcebundle(ResourceBundle resourcebundle) {
		if(resourcebundle!=null){
			SharepointClientContext.resourcebundle = resourcebundle;
		}
	}

}
