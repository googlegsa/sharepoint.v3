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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 * Class to hold data regarding a sharepoint list e.g. DocumentLibrary.
 * internalName is the GUID of the list and type is the type of the list 
 * @author amit_kagrawal
 */
public class BaseList implements Comparable {
  private String internalName;
  private String title;
  private String type;
  private Calendar lastMod;
  private String baseTemplate;
  private ArrayList attrs = new ArrayList();
  private String url;
  private String parentWebTitle;
//  private String nextPageToken="";
  
  private final String className = BaseList.class.getName();
  private static final Logger LOGGER = Logger.getLogger(BaseList.class.getName());
  
  private String listConst = "/Lists";
  
  public BaseList(String inInternalName, String inTitle, String inType,
      Calendar inLastMod,String inBaseTemplate,String inUrl,String inParentWebTitle) throws SharepointException {
	  
    // added by Nitendra
	final String sFunName = "BaseList(String inInternalName, String inTitle, String inType, Calendar inLastMod,String inBaseTemplate,String inUrl,String inParentWebTitle)";
	LOGGER.entering(className, sFunName);
	LOGGER.config(sFunName+": inInternalName["+inInternalName+"], inTitle["+inTitle+"], inType["+inType+"], inLastMod["+inLastMod+"], inBaseTemplate["+inBaseTemplate+"], inUrl["+inUrl+"], inParentWebTitle["+inParentWebTitle+"]");
      
    if(inInternalName==null){
	    throw new SharepointException("Unable to find Internal name");
    }
    this.internalName = inInternalName;
    if(inTitle!=null){
    	this.title = inTitle;
    }
    if(null!=inParentWebTitle){
    	parentWebTitle = inParentWebTitle;
    }
    
    if(inType!=null){
    	this.type = inType;
    }
    
    if(inLastMod!=null){
    	this.lastMod = inLastMod;
    }
    
    if(inBaseTemplate!=null){
    	this.baseTemplate = inBaseTemplate;
    }
    
    if(inUrl!=null){
		this.url = inUrl;
	}
  }

  public String getInternalName() {
    return internalName;
  }

  public String getTitle() {
    return title;
  }

  public String getType() {
    return type;
  }

  public Calendar getLastMod() {
    return lastMod;
  }
 
  public int compareTo(BaseList list){
	if(lastMod!=null){  
	    int comparison = this.lastMod.getTime().compareTo(list.lastMod.getTime());
	    if (0 == comparison) {
	      comparison = this.internalName.compareTo(list.internalName);
	    }    
	    return comparison;
	}else{
		return -1;//this.lastmodified is null
	}
  }

  public int compareTo(Object arg0) {
	return -1;
  }

	public String getBaseTemplate() {
		return baseTemplate;
	}
	
	public void setBaseTemplate(String inBaseTemplate) {
		if(inBaseTemplate!=null){
			this.baseTemplate = inBaseTemplate;
		}
	}

	public ArrayList getAttrs() {
		return attrs;
	}

	public void setAttribute(String key, String value) {
		if(attrs==null){
			attrs = new ArrayList();
		}
		if(key!=null){
			attrs.add(new Attribute(key, value));
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String inUrl) {
		if(inUrl!=null){
			this.url = inUrl;
		}
	}

	public String getListConst() {
		return listConst;
	}

	public void setListConst(String inListConst) {
		if(inListConst!=null){
			this.listConst = inListConst;
		}
	}

	public String getParentWebTitle() {
		return parentWebTitle;
	}

	public void setParentWebTitle(String inParentWebTitle) {
		if(null!=inParentWebTitle){
			this.parentWebTitle = inParentWebTitle;
		}
	}

	/*public String getNextPageToken() {
		return nextPageToken;
	}

	public void setNextPageToken(String inNextPageToken) {
		if(inNextPageToken==null){
			this.nextPageToken = "";
		}else{
			this.nextPageToken = inNextPageToken;	
		}
	}*/
}
