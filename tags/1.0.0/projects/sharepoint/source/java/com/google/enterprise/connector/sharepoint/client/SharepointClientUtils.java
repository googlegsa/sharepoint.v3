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
import java.text.Collator;
import java.util.List;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;

/**
 * Class to hold random utility functions. 
 * @author abhijit_mitra
 *
 */

public class SharepointClientUtils {
//	private static final String POSTFIX = ".*";
	private static final String BLANK_STRING = "";
	private Collator collator = SharepointConnectorType.getCollator(); 


	/**
	 * Desc : Check if the String Value can be included or not .
	 * @param includeURLList
	 * @param excludeURLList
	 * @param strValue
	 * @return
	 * @throws SharepointException 
	 * @throws MalformedURLException 
	 */
	public boolean isIncludedUrl(String[] includeURLList, String[] excludeURLList, String strValue) throws SharepointException, MalformedURLException {
		boolean bValue = false;
		String strValueModified = "";
		URL url= null;
		if(includeURLList == null){
			throw new SharepointException("Can not find include URLs");
		}
		if (strValue !=null || ! collator.equals(strValue,BLANK_STRING)){


			//plain include
			if(match(includeURLList, strValue)) {
				if(excludeURLList == null){
					return true;
				}else if(match(excludeURLList, strValue)) {
					return false;
				}

				//	with port exclude
				url = new URL(strValue);
				strValueModified = url.getProtocol()+"://"+url.getHost()+":"+url.getDefaultPort()+url.getFile();
				//	if(match(excludeURLList, strValue) || match(excludeURLList, strValueModified)) {
				if(match(excludeURLList, strValueModified)) {
					return false;
				}

				//	without port exclude
				strValueModified = url.getProtocol()+"://"+url.getHost()+url.getFile();
				//		if(match(excludeURLList, strValue) || match(excludeURLList, strValueModified)) {
				if(match(excludeURLList, strValueModified)) {	
					return false;
				}

				return true;

			}

			//with port include
			url = new URL(strValue);
			strValueModified = url.getProtocol()+"://"+url.getHost()+":"+url.getDefaultPort()+url.getFile();
			//	if(match(includeURLList, strValue) || match(includeURLList, strValueModified)) {
			if(match(includeURLList, strValueModified)) {	
				if(excludeURLList == null){
					return true;
				}else if(match(excludeURLList, strValue)) {
					return false;
				}

				//	with port exclude
				url = new URL(strValue);
				strValueModified = url.getProtocol()+"://"+url.getHost()+":"+url.getDefaultPort()+url.getFile();
				//		if(match(excludeURLList, strValue) || match(excludeURLList, strValueModified)) {
				if(match(excludeURLList, strValueModified)) {
					return false;
				}

				//	without port exclude
				strValueModified = url.getProtocol()+"://"+url.getHost()+url.getFile();
//				if(match(excludeURLList, strValue) || match(excludeURLList, strValueModified)) {
				if(match(excludeURLList, strValueModified)) {
					return false;
				}
				return true;
			}
			//without port include
			strValueModified = url.getProtocol()+"://"+url.getHost()+url.getFile();
			//		if(match(includeURLList, strValue) || match(includeURLList, strValueModified)) {
			if(match(includeURLList, strValueModified)) {
				if(excludeURLList == null){
					return true;
				}else if(match(excludeURLList, strValue)) {
					return false;
				}

				//	with port exclude
				url = new URL(strValue);
				strValueModified = url.getProtocol()+"://"+url.getHost()+":"+url.getDefaultPort()+url.getFile();
				//			if(match(excludeURLList, strValue) || match(excludeURLList, strValueModified)) {
				if(match(excludeURLList, strValueModified)) {
					return false;
				}

				//	without port exclude
				strValueModified = url.getProtocol()+"://"+url.getHost()+url.getFile();
				//			if(match(excludeURLList, strValue) || match(excludeURLList, strValueModified)) {
				if(match(excludeURLList, strValueModified)) {
					return false;
				}
				return true;
			}
//			}
		}
		return bValue;
	}


	/**
	 * Desc : match the String Value with the string array.
	 * @param strList
	 * @param strValue
	 * @return
	 */
	public boolean match(String[]strList, String strValue){
		boolean bValue = false;
		if((strList != null) && (strValue != null)) {
			int length = strList.length;
			for(int i=0; i<length ; i++) {
				if(strList[i]!= null && ! collator.equals(strList[i],BLANK_STRING)) {
					if(strValue.matches(strList[i]) || strValue.startsWith(strList[i])) {
						return true ;
					}
				}
			}
		}
		return bValue;
	}


	/**
	 * Desc : match the String Value with the List.
	 * @param strList
	 * @param strValue
	 * @return
	 */
	public boolean match(List strList, String strValue){
		boolean bValue = false;
		if((strList != null) && (strValue != null)) {
			int length = strList.size();
			for(int i=0; i<length ; i++) {
				if(strList.get(i)!= null && ! collator.equals((String)strList.get(i),BLANK_STRING)){
					if(strValue.matches((String)strList.get(i)) || strValue.startsWith((String)strList.get(i))) {
						return true ;
					}
				}
			}
		}
		return bValue;
	}

	/**
	 * Desc : match the Value List with the array of String Key .
	 * @param strKeyList
	 * @param strValueList
	 * @return
	 */
	public List match(String[] strKeyList, List strValueList){

		if((strKeyList != null) && (strValueList != null)) {
			int lengthKeyList = strKeyList.length;
			int sizeValueList = strValueList.size();
			for(int i=0; i<lengthKeyList ; i++) {
				if(strKeyList[i]!= null && ! collator.equals(strKeyList[i],BLANK_STRING)) {
					for(int j= (sizeValueList -1) ; i >= 0  ; j--)	{
						String strValue = (String) strValueList.get(j);
						if(strValue.matches(strKeyList[i]) || strValue.startsWith(strKeyList[i])) {
							strValueList.remove(j);
						}
					}
				}
			}
		}
		return strValueList;
	}



	/**
	 * Desc : match the Value List with the List of String Key.
	 * @param strKeyList
	 * @param strValueList
	 * @return
	 */

	public List match(List strKeyList, List strValueList){

		if((strKeyList != null) && (strValueList != null)) {
			int sizeKeyList = strKeyList.size();
			int sizeValueList = strValueList.size();
			for(int i=0; i<sizeKeyList ; i++) {
				if(strKeyList.get(i)!= null && ! collator.equals((String) strKeyList.get(i),BLANK_STRING)) {
					for(int j= (sizeValueList -1) ; i >= 0  ; j--)	{
						String strValue = (String) strValueList.get(j);
						if(strValue.matches((String)strKeyList.get(i)) || strValue.startsWith((String)strKeyList.get(i))) {
							strValueList.remove(j);
						}
					}
				}
			}
		}
		return strValueList;
	}

}
