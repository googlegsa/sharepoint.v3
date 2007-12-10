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

import java.util.Calendar;

/**
 * Class to hold data regarding a sharepoint list e.g. DocumentLibrary.
 * internalName is the GUID of the list and type is the type of the list 
 */
public class BaseList implements Comparable {
  private String internalName;
  private String title;
  private String type;
  private Calendar lastMod;
  private String baseTemplate;
  
  public BaseList(String inInternalName, String inTitle, String inType,
      Calendar inLastMod,String inBaseTemplate) throws SharepointException {
	  
    if(inInternalName==null){
	    throw new SharepointException("Unable to find Internal name");
    }
    this.internalName = inInternalName;
    if(inTitle!=null){
    	this.title = inTitle;
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
}
