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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.Property;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spiimpl.BooleanValue;
import com.google.enterprise.connector.spiimpl.DateValue;
import com.google.enterprise.connector.spiimpl.StringValue;

/**
 * Class to hold data regarding a sharepoint document.
 *@author amit_kagrawal
 */
public class SPDocument implements Document, Comparable{
	private String docId;
	private String url;
	private Calendar lastMod;
	private String author = NO_AUTHOR;
	private String objType = NO_OBJTYPE;

	//list guid
	private String listguid;

	public static final String AUTHOR = "sharepoint:author";
	public static final String LIST_GUID = "sharepoint:listguid";
	public static final String OBJECT_TYPE = "google:objecttype";
	public static final String NO_OBJTYPE = "No Object Type";
	public static final String OBJTYPE_WEB = "Web";
	public static final String OBJTYPE_ATTACHMENT = "Attachment";
	public static final String OBJTYPE_LIST_ITEM = "ListItem";//when no type is recv through ws call
	public static final String NO_AUTHOR = "No author";

	// open-ended dictionary of metadata beyond the above:
	/**
	 * A guess as to how many attributes we should allow for initially.
	 */
	private static final int INITIALATTRLISTSIZE = 5;
	private ArrayList attrs = new ArrayList(INITIALATTRLISTSIZE);
	

	public String getListGuid(){
		return listguid;
	}

	public void setListGuid(String newguid){
		if(newguid!=null){
			listguid= newguid;
		}
	}

	public SPDocument(String inDocId, String inUrl, Calendar inLastMod,String inObjectType) {
		this.docId = inDocId;
		this.url = inUrl;
		this.lastMod = inLastMod;
		this.objType = inObjectType;
	}
	public SPDocument(String inDocId, String inUrl, Calendar inLastMod){
		this.docId = inDocId;
		this.url = inUrl;
		this.lastMod = inLastMod;
	}
	public SPDocument(String inDocId, String inUrl, Calendar inLastMod, String inAuthor,String inObjType) {
		this.docId = inDocId;
		this.url = inUrl;
		this.lastMod = inLastMod;
		this.author = inAuthor;
		this.objType = inObjType;
	}

	public Calendar getLastMod() {
		return lastMod;
	}

	public String getDocId() {
		return docId;
	}

	public String getUrl() {
		return url;
	}   

	public void setUrl(String strUrl) {
		if(url!=null){
			url = strUrl;
		}
	} 

	public ArrayList getAllAttrs() {
		return attrs;
	}

	// debug routine
	public void dumpAllAttrs() {
		if(attrs==null){
			return;
		}
		
		for (Iterator iter=attrs.iterator(); iter.hasNext();){
			Attribute attr = (Attribute) iter.next();
			System.out.println(attr.getName() + "=" + attr.getValue());
		}
	}
	/**
	 * Set an attribute which may not be one of the named ones listed above.
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, String value) {
		if(key!=null){
			attrs.add(new Attribute(key, value));
		}
	}

	public String getAuthor() {
		return author;
	}

	public String getObjType() {
		return objType;
	}

	public void setAuthor(String inAuthor) {
		if(inAuthor!=null){
			this.author = inAuthor;
		}
	}

	public void setObjType(String inObjType) {
		if(inObjType!=null){
			this.objType = inObjType;
		}
	}

	public int compareTo(SPDocument doc) {
		
		if(doc==null){
			return -1;
		}
		
		int comparison = this.lastMod.getTime().compareTo(doc.lastMod.getTime());
		if (0 == comparison) {
			comparison = this.docId.compareTo(doc.docId);
			if (0 == comparison) {
				comparison = this.url.compareTo(doc.url);
			}
		}    
		return comparison;
	}

	public Property findProperty(String strPropertyName) throws RepositoryException {
		if(strPropertyName==null){
			return null;
		}
		Collator collator = SharepointConnectorType.getCollator();
		if(collator.equals(strPropertyName,SpiConstants.PROPNAME_CONTENTURL)){
			return new SPProperty(SpiConstants.PROPNAME_CONTENTURL, new StringValue(getUrl()));
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_SEARCHURL)){
			return new SPProperty(SpiConstants.PROPNAME_SEARCHURL, new StringValue(getUrl()));
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_DISPLAYURL)){
			return new SPProperty(SpiConstants.PROPNAME_DISPLAYURL, new StringValue(getUrl()));
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_DOCID)){
			return new SPProperty(SpiConstants.PROPNAME_DOCID, new StringValue(getDocId()));
		}else if(collator.equals(strPropertyName,SpiConstants.PROPNAME_LASTMODIFIED)){
			return new SPProperty(SpiConstants.PROPNAME_LASTMODIFIED, new DateValue(getLastMod()));
		}else if(collator.equals(strPropertyName,LIST_GUID)){
			return new SPProperty(LIST_GUID, new StringValue(getListGuid()));
		}else if(collator.equals(strPropertyName,AUTHOR)){
			return new SPProperty(AUTHOR, new StringValue(getAuthor()));
		}else if(strPropertyName.equals(OBJECT_TYPE)){
			return new SPProperty(OBJECT_TYPE, new StringValue(getObjType()));
		}else if(strPropertyName.equals(SpiConstants.PROPNAME_ISPUBLIC)){
			return new SPProperty(SpiConstants.PROPNAME_ISPUBLIC, BooleanValue.makeBooleanValue(false));
		}else{
			//check if the property is in the name of the custom metadata
			for (Iterator iter=this.getAllAttrs().iterator();iter.hasNext();){
				Attribute attr = (Attribute) iter.next();
				if(collator.equals(strPropertyName,attr.getName())){
					return new SPProperty(strPropertyName, new StringValue(attr.getValue().toString()));
				}
			}	

		}
		return null;//no matches found
	}

	public Set getPropertyNames() throws RepositoryException {
		Set s = new HashSet();
		//add the static property from the list
		s.add(SpiConstants.PROPNAME_SEARCHURL);
		s.add(SpiConstants.PROPNAME_DISPLAYURL);
		s.add(SpiConstants.PROPNAME_DOCID);
		s.add(OBJECT_TYPE);
		s.add(SpiConstants.PROPNAME_LASTMODIFIED);
		s.add(LIST_GUID);
		s.add(AUTHOR);
		s.add(SpiConstants.PROPNAME_ISPUBLIC);

		// get the "extra" metadata fields, including those added by user:
		for (Iterator iter=this.getAllAttrs().iterator();iter.hasNext();){
			Attribute attr = (Attribute) iter.next();
			s.add(attr.getName().toString());
		}
		return s;
	}

	public int compareTo(Object arg0) {
		return -1;
	}
}
