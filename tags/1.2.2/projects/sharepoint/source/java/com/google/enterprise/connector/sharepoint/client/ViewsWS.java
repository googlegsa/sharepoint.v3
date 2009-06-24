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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

import org.apache.axis.message.MessageElement;
//import org.apache.catalina.util.URL;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.generated.views.GetViewResponseGetViewResult;
import com.google.enterprise.connector.sharepoint.generated.views.Views;
import com.google.enterprise.connector.sharepoint.generated.views.ViewsLocator;
import com.google.enterprise.connector.sharepoint.generated.views.ViewsSoap_BindingStub;

/**
 * This class holds data and methods for any call to Views Web Service.
 *
 */
public class ViewsWS {
	private static final String VIEWSENDPOINT = "/_vti_bin/Views.asmx";
	private String endpoint;
	ViewsSoap_BindingStub stub = null;
	static final String URL_SEP ="://";
	private static final Logger LOGGER = Logger.getLogger(ViewsWS.class.getName());
	private String className = ViewsWS.class.getName();
	public static URLEncoder enc  = new URLEncoder();

	static{
		//set the URLEncoder safe characters
		enc.addSafeCharacter('/');
		enc.addSafeCharacter(':');// required when endpoint is set using specified site
	}

	/**
	 * 
	 * @param inSharepointClientContext
	 * @throws SharepointException
	 */
	public ViewsWS(SharepointClientContext inSharepointClientContext)
	throws SharepointException {
		String sFunctionName = "ViewsWS(SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFunctionName);
		if(inSharepointClientContext!=null){
			endpoint = inSharepointClientContext.getProtocol()+URL_SEP+ inSharepointClientContext.getHost() + ":"+inSharepointClientContext.getPort() +enc.encode(inSharepointClientContext.getsiteName()) + VIEWSENDPOINT;
			try{
				ViewsLocator loc = new ViewsLocator();
				//System.out.println("viwsend: "+endpoint);
				loc.setViewsSoapEndpointAddress(endpoint);
				Views service = loc;

				try {
					stub = (ViewsSoap_BindingStub) service.getViewsSoap();
				} catch (ServiceException e) {
					LOGGER.warning(className+":"+sFunctionName+": "+e.toString());
					throw new SharepointException("Unable to create views stub");
				}

				String strDomain = inSharepointClientContext.getDomain();
				String strUser = inSharepointClientContext.getUsername();
				String strPassword= inSharepointClientContext.getPassword();

				if((strDomain==null)||(strDomain.trim().equals(""))){
					strDomain=strUser; //for user
				}else{
					strDomain+="\\"+strUser; // form domain/user
				}

				//set the user and pass
				stub.setUsername(strDomain);
				stub.setPassword(strPassword);
			}catch (Exception e) {
				LOGGER.warning(className+":"+sFunctionName+": "+e.getMessage());
				throw new SharepointException(e);
			}
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * 
	 * @param inSharepointClientContext
	 * @param siteName
	 * @throws SharepointException
	 */
	public ViewsWS(SharepointClientContext inSharepointClientContext,
			String siteName) throws SharepointException {
		String sFunctionName = "ViewsWS(SharepointClientContext inSharepointClientContext,String siteName)";
		LOGGER.entering(className, sFunctionName);
		if(siteName==null){
			throw new SharepointException("Unable to get the site name");
		}
		URL siteURL ;
		try{
			try {
				siteURL = new URL(siteName);
			} catch (MalformedURLException e) {
				LOGGER.warning(className+":"+sFunctionName+": "+e.toString());
				throw new SharepointException("Malformed URL: "+siteName);
			}

			int iPort = 0;
			//check if the def
			if (-1 != siteURL.getPort()) {
				iPort = siteURL.getPort();
			}else{
				iPort = siteURL.getDefaultPort();
			}
			endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+/*siteURL.getPort()*/iPort+enc.encode(siteURL.getPath())+ VIEWSENDPOINT;
//			endpoint = siteName + VIEWSENDPOINT;
			ViewsLocator loc = new ViewsLocator();
			//loc.setViewsSoap12EndpointAddress(endpoint);
			loc.setViewsSoapEndpointAddress(endpoint);
			Views service = loc;
			//System.out.println("viwsend: "+endpoint);
			try {
//				stub = (ViewsSoap_BindingStub) service.getViewsSoap12();
				stub = (ViewsSoap_BindingStub) service.getViewsSoap();
			} catch (ServiceException e) {
				LOGGER.finer("ViewsWS(SharepointClientContext inSharepointClientContext): "+e.toString());
				throw new SharepointException("Unable to create views stub");
			}

			String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			String strPassword= inSharepointClientContext.getPassword();

			if((strDomain==null)||(strDomain.trim().equals(""))){
				strDomain=strUser; //for user
			}else{
				strDomain+="\\"+strUser; // form domain/user
			}

			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		}catch (Exception e) {
			LOGGER.warning(className+":"+sFunctionName+": "+e.getMessage());
			throw new SharepointException(e);
		}
		LOGGER.exiting(className, sFunctionName);
	}

	/**
	 * cached list of available viewFields for each Sharepoint List.
	 */ 
	private static HashMap cachedViewFields = 
		new HashMap();

	// special flag value for "there are no viewFields for this list"
	private static final ArrayList EMPTYVIEWFIELDS = new ArrayList();


	/**
	 * Find the viewFields for this List. A non-obvious twist is, when a List
	 * has been previously set to null, meaning "there are no viewFields for this
	 * List," the special value emptyViewFields is returned. It should not cause
	 * an error to pass an empty viewFields to Sharepoint, but it's preferable
	 * to use null instead.
	 * @param listName
	 * @return viewFields that was previously set for this List. If return
	 * value is emptyViewFields, null should used instead.  A null return value
	 * means that no value has been set for this List. 
	 */
	private static List  getCachedViewFields(String listName) {
		return (List) cachedViewFields.get(listName);
	}

	/**
	 * Remember the viewFields for this List.
	 * @param listName 
	 * @param viewFields  null is an acceptable value, meaning "there are no
	 * viewFields for this List."  A special flag value is substituted in this
	 * case.
	 */
	private static void setCachedViewFields(String listName, 
			ArrayList viewFields) {
		String sFunctionName = "setCachedViewFields(String listName,ArrayList viewFields)";
		LOGGER.entering(ViewsWS.class.getName(), sFunctionName);
		if (viewFields == null || viewFields.size() == 0) {
			cachedViewFields.put(listName, EMPTYVIEWFIELDS);
		} else {
			cachedViewFields.put(listName, viewFields);
		}
		LOGGER.exiting(ViewsWS.class.getName(), sFunctionName);
	}

	public static void dumpCachedViewedFields() {
		for (Iterator iter = 
			cachedViewFields.entrySet().iterator(); iter.hasNext();){

			if(iter==null){//null check
				return;
			}

			Entry entry = (Entry) iter.next();
			LOGGER.finer(entry.getKey() + "=");
			String[] fields = (String[]) entry.getValue();
			for (int iField=0;iField< fields.length;++iField) {
				String field =fields[iField];
				LOGGER.finer("\t" + field);
			}
		}
	}



	/**
	 * Gets the viewFields for a given list. This is a partial (unfortunately!)
	 * list of the metadata available for members of the list.
	 * @param listName internal name of the list
	 * @return  List of the viewFields on this list. If this has been fetched
	 *     from Sharepoint before, a cached copy will be returned.
	 * @throws SharepointException
	 */
	public List getViewFields(String listName) throws SharepointException {
		String sFunctionName = "getViewFields(String listName)";
		LOGGER.entering(className, sFunctionName);
		LOGGER.config("getViewFields for [" + listName+"]");
		if(listName==null){
			throw new SharepointException("Unable to get the list name");
		}
		if(stub==null){
			throw new SharepointException("Unable to get the views stub");
		}
//		Collator collator = SharepointConnectorType.getCollator();
		List listItemsCached = getCachedViewFields(listName);
		if ((listItemsCached == EMPTYVIEWFIELDS)/* || (listItemsCached==null)*/) {
			return null;
		}
		ArrayList listItems = new ArrayList();
		//--------modified by amit to support axis 1_4
		GetViewResponseGetViewResult res = null;
		try {
			res = stub.getView(listName, null);
			MessageElement[] me = res.get_any();

			if(me!=null){
//				System.out.println("-------------VIEW ATTR-----------");
				for(int i=0;i<me.length;++i){
					if(me[i]!=null){
						Iterator itAttr = me[i].getAllAttributes();
						//traverse the attributes 
						if(itAttr !=null){
							while(itAttr.hasNext()){
								Object attr = itAttr.next();
								if(attr!=null){
//									System.out.println("ViewAttr: "+attr.toString());
									listItems.add(attr.toString());
								}
								//System.out.println("Attr: "+attr.toString());
							}
						}//end:if(it_attr !=null){
					}
				}
//				System.out.println("-------------END: VIEW ATTR-----------");
			}//end:if(me!=null){
		} catch (Throwable e) {
			// not being able to get the viewFields should not stop things:
			LOGGER.config("no view fields for "+listName);
			LOGGER.finer(e.toString());
			// remember, and don't keep hitting this List
			setCachedViewFields(listName, null); 
			return null;
		}
		//--end: modified code

		setCachedViewFields(listName, listItems);
		if (listItems.size() == 0) {
			LOGGER.exiting(className, sFunctionName);
			return null;
		}
		LOGGER.exiting(className, sFunctionName);
		return listItems;
	}

}
