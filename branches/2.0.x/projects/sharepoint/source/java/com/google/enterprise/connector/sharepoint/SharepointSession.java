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

import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.spi.AuthenticationManager;
import com.google.enterprise.connector.spi.AuthorizationManager;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.Session;
import com.google.enterprise.connector.spi.TraversalManager;

/**
 * Implements the Session interface from the spi.
 * It implements methods to return AuthenticationManager, AuthorizationManager
 * and the QueryTraversalManager for the Sharepoint connector to the caller.
 * @author amit_kagrawal
 */
public class SharepointSession implements Session {

	private SharepointConnector connector=null;
	private SharepointClientContext sharepointClientContext=null;
	private static final Logger LOGGER =  Logger.getLogger(SharepointSession.class.getName());
	private String className = SharepointSession.class.getName();

	public SharepointSession(SharepointConnector inConnector,SharepointClientContext inSharepointClientContext)/* throws RepositoryException*/ {
		String sFuncName = "SharepointSession(SharepointConnector inConnector,SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFuncName);
		if(inConnector!=null){  
			this.connector = inConnector;
		}

		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
		}
		LOGGER.exiting(className, sFuncName);    
	}

	public AuthenticationManager getAuthenticationManager()
	throws RepositoryException {
		String sFuncName = "getAuthenticationManager()";
		LOGGER.entering(className, sFuncName);
		LOGGER.exiting(className, sFuncName);
		return null;
	}

	public AuthorizationManager getAuthorizationManager()
	throws RepositoryException {
		String sFuncName = "getAuthorizationManager()";
		LOGGER.entering(className, sFuncName);
		LOGGER.exiting(className, sFuncName);
		return null;
	}

	public TraversalManager getTraversalManager() throws RepositoryException {
		String sFuncName = "getTraversalManager()";
		LOGGER.entering(className, sFuncName);
		LOGGER.exiting(className, sFuncName);
		return new SharepointTraversalManager(connector,sharepointClientContext);
	}

}
