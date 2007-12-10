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

package com.google.enterprise.connector.sharepoint.client;

import java.rmi.RemoteException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.sharepoint.generated.ViewsStub;

/**
 * This class holds data and methods for any call to Views Web Service.
 *
 */
public class ViewsWS {
  private static final String VIEWSENDPOINT = "/_vti_bin/Views.asmx";
  private String endpoint;
  private ViewsStub stub;
  final String URL_SEP ="://";
  private static Log logger = LogFactory.getLog(ViewsWS.class);
  
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
    if (viewFields == null || viewFields.size() == 0) {
      cachedViewFields.put(listName, EMPTYVIEWFIELDS);
    } else {
      cachedViewFields.put(listName, viewFields);
    }
  }
  
  public static void dumpCachedViewedFields() {
    for (Iterator iter = 
        cachedViewFields.entrySet().iterator(); iter.hasNext();){
    	
    	if(iter==null){//null check
    		return;
    	}
    		
      Entry entry = (Entry) iter.next();
      System.out.println(entry.getKey() + "=");
      String[] fields = (String[]) entry.getValue();
      for (int iField=0;iField< fields.length;++iField) {
    	  String field =fields[iField];
        System.out.println("\t" + field);
      }
    }
  }
  
  public ViewsWS(SharepointClientContext inSharepointClientContext)
      throws SharepointException {
	  if(inSharepointClientContext!=null){
		  endpoint = inSharepointClientContext.getProtocol()+URL_SEP+ inSharepointClientContext.getHost() + ":"+inSharepointClientContext.getPort() +inSharepointClientContext.getsiteName() + VIEWSENDPOINT;
	    try {
	      stub = new ViewsStub(endpoint);
	      inSharepointClientContext.setStubWithAuth(stub, endpoint);
	    } catch (AxisFault e) {
	      throw new SharepointException(e.toString());
	    }
	  }
  }

  public ViewsWS(SharepointClientContext inSharepointClientContext,
      String siteName) throws SharepointException {
  endpoint = siteName + VIEWSENDPOINT;
  try {
    stub = new ViewsStub(endpoint);
    inSharepointClientContext.setStubWithAuth(stub, endpoint);
  } catch (AxisFault e) {
    throw new SharepointException(e.toString());
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
    logger.debug("getViewFields for [" + listName+"]");
    if(listName==null){
    	throw new SharepointException("Unable to get the list name");
    }
    Collator collator = SharepointConnectorType.getCollator();
    List listItemsCached = getCachedViewFields(listName);
    if ((listItemsCached == EMPTYVIEWFIELDS) || (listItemsCached==null)) {
      return null;
    }
    ArrayList listItems = new ArrayList();
    ViewsStub.GetView req = new ViewsStub.GetView();
    req.setListName(listName);
    try {
      ViewsStub.GetViewResponse res = stub.GetView(req);
      try {
        XMLStreamReader reader = res.getGetViewResult().getPullParser(new QName("ViewFields"));
        boolean inViewFields = false;
        if(reader!=null){
	        while (reader.hasNext()) {
	          reader.next();
	          if (reader.isStartElement()) {
	        	  //added by amit
	        	  if(reader.getName()==null){
	        		  throw new SharepointException("Unable to get reader name");
	        	  }
	            if (collator.equals(reader.getName().getLocalPart(),"ViewFields")) {
	              inViewFields = true;
	              continue;
	            }
	            if (inViewFields && collator.equals(reader.getName().getLocalPart(),"FieldRef")) {
	              listItems.add(reader.getAttributeValue(0));
	            }
	          } else if (reader.isEndElement()) {
	            if (inViewFields) {
	              if (collator.equals(reader.getName().getLocalPart(),"ViewFields")) {
	                inViewFields = false;
	              }
	            }
	          }
	        }
        }
      } catch (XMLStreamException e) {
        throw new SharepointException(e.toString());
      }

    } catch (RemoteException e) {
      // not being able to get the viewFields should not stop things:
      logger.warn("no view fields for "+listName);
      logger.debug(e.toString());
      // remember, and don't keep hitting this List
      setCachedViewFields(listName, null); 
      return null;
    }
    setCachedViewFields(listName, listItems);
    if (listItems.size() == 0) {
      return null;
    }
    return listItems;
  }

}
