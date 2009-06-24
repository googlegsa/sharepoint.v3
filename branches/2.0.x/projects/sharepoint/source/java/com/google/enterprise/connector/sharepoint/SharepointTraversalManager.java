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

import java.util.Iterator;
import java.util.logging.Logger;

import com.google.enterprise.connector.sharepoint.client.SPDocumentList;
import com.google.enterprise.connector.sharepoint.client.SharepointClient;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.SharepointException;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.WebState;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.HasTimeout;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.TraversalManager;

/**
 * This class is an implementation of the TraversalManager from the spi.
 * All the traversal based logic is invoked through this class.
 * @author amit_kagrawal
 */

public class SharepointTraversalManager implements TraversalManager,HasTimeout {
	private static final  Logger LOGGER = Logger.getLogger(SharepointTraversalManager.class.getName());
	private String className = SharepointTraversalManager.class.getName();
	private SharepointClientContext sharepointClientContext;
	private SharepointClientContext sharepointClientContextOriginal=null;
	protected GlobalState globalState; // not private, so the unittest can see it
	private int hint = -1;

	/**
	 * constructor.
	 * @param inConnector
	 * @param inSharepointClientContext
	 * @throws RepositoryException
	 */
	public SharepointTraversalManager(SharepointConnector inConnector,SharepointClientContext inSharepointClientContext) 
	throws RepositoryException {
		String sFuncName = "SharepointTraversalManager(SharepointConnector inConnector,SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFuncName);
		try{
			if((inConnector!=null)&&(inSharepointClientContext!=null)){
				LOGGER.config("SharepointTraversalManager: " + inSharepointClientContext.getsiteName() + ", " + inSharepointClientContext.getGoogleConnectorWorkDir());
				this.sharepointClientContext = inSharepointClientContext;
				this.sharepointClientContextOriginal = (SharepointClientContext) inSharepointClientContext.clone();
				this.globalState = new GlobalState(inSharepointClientContext.getGoogleConnectorWorkDir());
				this.globalState.loadState();
			}
		}catch (Exception e) {
			LOGGER.warning(className+":"+sFuncName+":"+e.getMessage());
			throw new SharepointException(e);
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
		LOGGER.config("TimeoutMillis: "+Integer.MAX_VALUE);
		LOGGER.exiting(className, sFuncName);
		return Integer.MAX_VALUE;
	}

	//Note: Even though the checkpoint entry is provided, since sharepoint system does not
	//  support  checkpointing internally, we need to anyway get all the documents modified 
	//  after specific time
	public DocumentList resumeTraversal(String checkpoint) throws RepositoryException {
		String sFuncName = "resumeTraversal(String checkpoint)";
		LOGGER.entering(className, sFuncName);
		LOGGER.info(className+": resumeTraversal, checkpoint received: "+checkpoint);
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
		LOGGER.info(className+": BatchHint [" + hintNew+"]");
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
		LOGGER.info(className+": startTraversal");
				
		//delete the global state.. to simulate full crawl

		globalState=null;
		String workDir = sharepointClientContext.getGoogleConnectorWorkDir();
		GlobalState.forgetState(workDir);
		globalState = new GlobalState(sharepointClientContext.getGoogleConnectorWorkDir());

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

		SPDocumentList rsAll = null;
		try{
			SharepointClient sharepointClient = new SharepointClient(sharepointClientContext);

			if(hint==-1){
				LOGGER.warning(sFuncName+": Batch hint is -1");
				throw new SharepointException("Batch hint is -1");
			}
			
			//Set the batch hint to SPClientContext
			sharepointClientContext.setBatchHint(hint);

			//rs = sharepointClient.traverse(globalState, hint);

			////////////////////////////////////////////

			int sizeSoFar=0;
			
			String lastWeb = globalState.getLastCrawledWebID();
			if(null==lastWeb){
				globalState.setCurrentWeb(null);
			}else{
				WebState ws = globalState.lookupList(lastWeb);
				globalState.setCurrentWeb(ws);
			}
				
			for (Iterator iter = globalState.getCircularIterator();iter.hasNext();) {
				SPDocumentList rs = null;
				WebState webState = (WebState) iter.next();
				//WebState wws = globalState.lookupList(webState.getPrimaryKey());//Amit
				//globalState.setCurrentWeb(webState);//setCurrentList = setCurrentWeb
				//globalState.setCurrentWeb(wws);//setCurrentList = setCurrentWeb
				//rs = sharepointClient.traverse(globalState,wws, hint);
				
				globalState.setCurrentWeb(webState);//setCurrentList = setCurrentWeb
				rs = sharepointClient.traverse(globalState,webState, hint);

				if (rs!=null && rs.size() > 0) {
					//check for the initial condition
					if(rsAll==null){
						rsAll = rs;	
					}else{
						rsAll.addAll(rs);
					}
				}

				if(rsAll!=null){
					sizeSoFar = rsAll.size();
				}

				// we heed the batch hint, but always finish a List before checking:
				if (hint > 0 && sizeSoFar >= hint) {
					LOGGER.info(className+":"+sFuncName+":Stopping traversal because batch hint " + hint
							+ " has been reached");
					break;
				}
			}//end:for (Iterator iter = globalState.getCircularIterator();iter.hasNext();) {



			///////////////////////////////////////////
			//check for the results, If empty sweep Sharepoint again.
			// initially the rs=null. Once the global state is updated the crawl will be incremental.
			if ((rsAll==null)||(rsAll.size() == 0)) { 
				LOGGER.info(className+":"+sFuncName+": traversal returned no new docs: re-fetching ...");
				sharepointClient.updateGlobalState(globalState);
				//rsAll = sharepointClient.traverse(globalState, hint);

				////////////////////////////////////////////traverse again after updation //////////////////////
				
				lastWeb = globalState.getLastCrawledWebID();
				if(null==lastWeb){
					globalState.setCurrentWeb(null);
				}else{
					WebState ws = globalState.lookupList(lastWeb);
					globalState.setCurrentWeb(ws);
				}
				for (Iterator iter = globalState.getCircularIterator();iter.hasNext();) {
					SPDocumentList rs = null;
					WebState webState = (WebState) iter.next();
//					WebState wws = globalState.lookupList(webState.getPrimaryKey());//Amit
					globalState.setCurrentWeb(webState);//setCurrentList = setCurrentWeb
//					globalState.setCurrentWeb(wws);//setCurrentList = setCurrentWeb
					rs = sharepointClient.traverse(globalState,webState, hint);
					

					if ((rs!=null) && (rs.size() > 0)) {
						//check for the initial condition
						if(rsAll==null){
							rsAll = rs;	
						}else{
							rsAll.addAll(rs);
						}
					}

					if(rsAll!=null){
						sizeSoFar = rsAll.size();
					}

					// we heed the batch hint, but always finish a List before checking:
					if (hint > 0 && sizeSoFar >= hint) {
						LOGGER.info(className+":"+sFuncName+":Stopping traversal because batch hint " + hint
								+ " has been reached");
						break;
					}
				}//end:for (Iterator iter = globalState.getCircularIterator();iter.hasNext();) {
				////////////////////////////////////////////traverse again after updation //////////////////////


			}
			if(this.sharepointClientContextOriginal != null){
				this.sharepointClientContext = (SharepointClientContext) this.sharepointClientContextOriginal.clone();
			}
			if(rsAll!=null){
				LOGGER.info(className+":"+sFuncName+": Traversal returned ["+rsAll.size()+"] documents");
			}else{
				LOGGER.info(className+":"+sFuncName+": Traversal returned [0] documents");
			}
		}catch (Exception e) {
			LOGGER.warning(className+":"+sFuncName+":Problem in sharepoint traversal"+e.getMessage());
			throw new SharepointException(e);
		}
		LOGGER.exiting(className, sFuncName);
		return rsAll;           
	}
	
	//for debug purpose ONLY
	public void setGlobalState(GlobalState gs){
		globalState=gs;
	}

}
