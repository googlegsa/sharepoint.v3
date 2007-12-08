// Copyright 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint;

import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPDocumentList;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.HasTimeout;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;

/**
 * This class is an implementation of the TraversalManager from the spi.
 * All the traversal based logic is invoked through this class.
 * @author amit_kagrawal
 */

public class SharepointTraversalManager implements TraversalManager,
  HasTimeout {
  private static final  Logger LOGGER = Logger.getLogger(SharepointTraversalManager.class.getName());
  private String className = SharepointTraversalManager.class.getName();
  private SharepointClientContext sharepointClientContext;
  private SharepointClientContext sharepointClientContextOriginal=null;
  protected GlobalState globalState; // not private, so the unittest can see it
  private int hint = -1;
  
  public SharepointTraversalManager(SharepointConnector inConnector,SharepointClientContext inSharepointClientContext) 
      throws RepositoryException {
	  String sFuncName = "SharepointTraversalManager(SharepointConnector inConnector,SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFuncName);
	  if((inConnector!=null)&&(inSharepointClientContext!=null)){
	    LOGGER.config("SharepointTraversalManager: " + inSharepointClientContext.getsiteName() + ", " + inSharepointClientContext.getGoogleConnectorWorkDir());
	    this.sharepointClientContext = inSharepointClientContext;
	    
	    this.sharepointClientContextOriginal = (SharepointClientContext) inSharepointClientContext.clone();
	    this.globalState = new GlobalState(
	        inSharepointClientContext.getGoogleConnectorWorkDir());
	    this.globalState.loadState();
    }
	  LOGGER.exiting(className, sFuncName);
  }
  
  /**
   * For the HasTimeout interface: tell ConnectorManager we need the maximum
   * amount of time.
   * @return integer
   */
  public int getTimeoutMillis() {
	  String sFuncName = "getTimeoutMillis()";
		LOGGER.entering(className, sFuncName);
		LOGGER.exiting(className, sFuncName);
    return Integer.MAX_VALUE;
  }
   
  //Note: Even though the checkpoint entry is provided, since sharepoint system does not
  //  support  checkpointing internally, we need to anyway get all the documents modified 
  //  after specific time
  public DocumentList resumeTraversal(String arg0) throws RepositoryException {
	  String sFuncName = "resumeTraversal(String arg0)";
		LOGGER.entering(className, sFuncName);
	  LOGGER.config("resumeTraversal");
	  LOGGER.exiting(className, sFuncName);
    return doTraversal();
  }

  /* (non-Javadoc)
   * @see com.google.enterprise.connector.spi.TraversalManager
   * #setBatchHint(int)
   */
  public void setBatchHint(int hintNew) throws RepositoryException {
	  String sFuncName = "setBatchHint(int hintNew)";
		LOGGER.entering(className, sFuncName);
	  LOGGER.info("setBatchHint " + hintNew);
    this.hint = hintNew;
    LOGGER.exiting(className, sFuncName);
  }

 /*  (non-Javadoc)
   * @see com.google.enterprise.connector.spi.TraversalManager
   * #startTraversal()
  */ 
  public DocumentList startTraversal() throws RepositoryException {
	  String sFuncName = "startTraversal()";
		LOGGER.entering(className, sFuncName);
	  LOGGER.config("startTraversal");
	    LOGGER.exiting(className, sFuncName);
    return doTraversal();
  }
  
  /**
   * Private routine that actually does the traversal. If no docs are found
   * in the first sharepointClient.traverse() call, we go back to Sharepoint
   * and fetch a new set of stuff.
   * @return PropertyMapList
   * @throws RepositoryException
   */
  private DocumentList doTraversal() throws RepositoryException {
	  String sFuncName = "doTraversal()";
		LOGGER.entering(className, sFuncName);
	  LOGGER.config("SharepointTraversalManager::doTraversal:  " +sharepointClientContext.getsiteName() + ", " +sharepointClientContext.getGoogleConnectorWorkDir());
   /* if(this.sharepointClientContextOriginal != null){
		  this.sharepointClientContext = (SharepointClientContext) this.sharepointClientContextOriginal.clone();
	  }*/
    SharepointClient sharepointClient = new SharepointClient(sharepointClientContext);
    SPDocumentList rs = sharepointClient.traverse(globalState, hint);
    
    //check for the results, If empty sweep Sharepoint again.
    // initially the rs=null. Once the global state is updated the crawl will be incremental.
    if ((rs==null)||(rs.size() == 0)) { 
      LOGGER.info("traversal returned no new docs: re-fetching ...");
      sharepointClient.updateGlobalState(globalState);
      rs = sharepointClient.traverse(globalState, hint);
    }
    if(this.sharepointClientContextOriginal != null){
		  this.sharepointClientContext = (SharepointClientContext) this.sharepointClientContextOriginal.clone();
	  }
    LOGGER.exiting(className, sFuncName);
    return rs;           
  }

}
