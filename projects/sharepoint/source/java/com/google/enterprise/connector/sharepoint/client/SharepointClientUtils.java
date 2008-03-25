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

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.Collator;

import com.google.enterprise.connector.sharepoint.SharepointConnectorType;

/**
 * Class to hold random utility functions. 
 * @author abhijit_mitra
 *
 */

public class SharepointClientUtils {
//	private static final String POSTFIX = ".*";
	private static final String BLANK_STRING = "";
	private static final String HASH = "#";
	private static final String MINUS = "-";
	private static final String DOLLAR = "$";
	private static final String CARET = "^";
	private static final String CONTAINS = "contains:";
	private static final String REGEXP = "regexp:";
	private static final String REGEXP_CASE = "regexpCase:";
	private static final String REGEXP_IGNORE_CASE = "regexpIgnoreCase:";
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
	public boolean isIncludedUrl(String[] includeURLList, String[] excludeURLList, String strValue) throws SharepointException{
//		boolean bValue = false;
//		String strValueModified = "";
//		URL url= null;
		if(includeURLList == null){
			throw new SharepointException("Can not find include URLs");
		}
		if (strValue !=null || ! collator.equals(strValue,BLANK_STRING)){
			if(match(includeURLList, strValue)) {
				if(excludeURLList == null){
					return true;
				}else if(match(excludeURLList, strValue)) {
					return false;
				}
				return true;

			}




			/* This is no more required with the new gnu regex pattern matching



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
			 */
		}
		return false;
	}


	/**
	 * Desc : match the String Value with the string array.
	 * @param strList
	 * @param strValue
	 * @return
	 */
	public boolean match(String[]strList, String strValue){

		if((strList != null) && (strValue != null)) {
			int length = strList.length;
			for(int i=0; i<length ; i++) {
				String strURLPat = strList[i];
				if(strList[i]!= null && ! collator.equals(strList[i],BLANK_STRING)) {

					String strDecodedValue = URLDecoder.decode(strValue);
					String strDecodedURLPat = URLDecoder.decode(strURLPat);
					if(matcher(strDecodedURLPat, strDecodedValue)) {
						return true ;
					}


					/*
					 * This is not required with the new gnu regex pattern matching
					 * 
					 * else{
						String strDecodedValue = URLDecoder.decode(strValue);
						String strDecodedURLPat = URLDecoder.decode(strURLPat);
						if(strDecodedValue!=null){
							if(strDecodedValue.startsWith(strDecodedURLPat)){
								return true;
							}
						}

					}*/

				}
			}
		}
		return false;
	}



	public boolean matcher(String pattern,String strValue){

		// null check for the arguments
		if(pattern==null|strValue==null){
			return false;
		}

		// If the pattern starts with "#" then its a comment so ignore
		if(pattern.startsWith(HASH)){
			return false;
		}

		// if the pattern starts with "-", remove the "-" from begin and proceed
		if(pattern.startsWith(MINUS)){
			return false;
			/*StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			pattern = new String(tempBuffer.delete(0, MINUS.length()));*/
		}

		// handle "contains:"
		// if pattern starts with "contains:" then check if the URL contains the string in pattern
		if(pattern.startsWith(CONTAINS)){
			StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			String strContainKey = new String(tempBuffer.delete(0,CONTAINS.length()));
			RE re;
			try {
				re = new RE(strContainKey);  // with case
				REMatch reMatch = re.getMatch(strValue);
				if(reMatch != null){
					return true;
				}
				return false;

			} catch (REException e) {
				return false;
			}
		}


		//	handle regexp
		// if pattern starts with "regexp:", then check for regex match with case
		if(pattern.startsWith(REGEXP)){
			StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			String strRegexPattrn = new String(tempBuffer.delete(0,REGEXP.length()));
			RE re;
			try {
				re = new RE(strRegexPattrn);
				REMatch reMatch = re.getMatch(strValue);
				if(reMatch!= null){
					return true;
				}
				return false;

			} catch (REException e) {
				return false;
			}
		}

		// handle regexpCase
		// if pattern starts with "regexpCase:", then check for regex match with case
		if(pattern.startsWith(REGEXP_CASE)){
			StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			String strRegexCasePattrn = new String(tempBuffer.delete(0,REGEXP_CASE.length()));
			RE re;
			try {
				re = new RE(strRegexCasePattrn);
				REMatch reMatch = re.getMatch(strValue);
				if(reMatch!= null){
					return true;
				}
				return false;

			} catch (REException e) {
				return false;
			}
		}

		// handle regexpIgnoreCase
		// if pattern starts with "regexpIgnoreCase:", then check for regex match without case
		if(pattern.startsWith(REGEXP_IGNORE_CASE)){
			StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			String strRegexIgnoreCasePattrn = new String(tempBuffer.delete(0,REGEXP_IGNORE_CASE.length()));
			RE re;
			try {
				re = new RE(strRegexIgnoreCasePattrn,RE.REG_ICASE); // ignore case
				REMatch reMatch = re.getMatch(strValue);
				if(reMatch!= null){
					return true;
				}
				return false;

			} catch (REException e) {
				return false;
			}
		}

		//	handle "^" and "$"
		if(pattern.startsWith(CARET)||pattern.endsWith(DOLLAR)){
			StringBuffer tempBuffer = new StringBuffer(pattern);
			boolean bDollar =false;
			String strValueModified = strValue;
			if(pattern.startsWith(CARET)){
				URL urlValue;
				try {
					urlValue = new URL(strValue);
					int port = urlValue.getPort();
					if(port== -1){
						port = urlValue.getDefaultPort();
						strValueModified = urlValue.getProtocol()+"://"+urlValue.getHost()+":"+port+ urlValue.getFile();
					}

				} catch (MalformedURLException e1) {
					return false;
				}
				tempBuffer = new StringBuffer(pattern);
				int indexOfStar = tempBuffer.indexOf("*");
				if(indexOfStar!= -1){
					tempBuffer.replace(indexOfStar, indexOfStar+"*".length(), "[0-9].*");
				}else{
					tempBuffer.delete(0, "^".length());
					if(pattern.endsWith(DOLLAR)){
						bDollar = true;
						tempBuffer.delete(tempBuffer.length()-DOLLAR.length(),tempBuffer.length());
					}
					try {

						URL urlPatt = new URL(tempBuffer.toString());
						int port = urlPatt.getPort();

						String strHost = urlPatt.getHost().toString();

						if(port == -1 && strHost !=null && strHost.length()!= 0){
							tempBuffer = new StringBuffer("^"+urlPatt.getProtocol()+"://"+urlPatt.getHost()+":[0-9].*"+urlPatt.getPath());
						}
						if(bDollar){
							tempBuffer.append(DOLLAR);
						}
					} catch (MalformedURLException e) {
						tempBuffer = new StringBuffer(pattern);
					}
				}

			}

			RE re;
			try {
				re = new RE(tempBuffer);
				REMatch reMatch = re.getMatch(strValueModified);
				if(reMatch != null){
					return true;
				}
				return false;				
			} catch (REException e) {
				return false;
			}
		}


		// url decode the pattern
		String patternDecoded = URLDecoder.decode(pattern);

		if(patternDecoded==null){
			return false;
		}

		boolean containProtocol = false;
		try {
			RE re = new RE("://");
			REMatch reMatch = re.getMatch(patternDecoded);
			if(reMatch != null){
				containProtocol = true; // protocol is present 
			}

		} catch (REException e) {
			containProtocol = false;
		}

		if(containProtocol){

			// split the test URL into two parts
			String urlValue1stPart = null;
			String urlValue2ndPart = null;

			URL urlValue;
			try {
				urlValue = new URL(strValue);
				int port = urlValue.getPort();
				if(port== -1){
					port = urlValue.getDefaultPort();
				}
				urlValue1stPart = urlValue.getProtocol()+"://"+urlValue.getHost()+":"+port;
				urlValue2ndPart = urlValue.getFile();

				if(urlValue2ndPart!= null){
					if(!urlValue2ndPart.startsWith("/")){
						urlValue2ndPart="/"+urlValue2ndPart;
					}
				}
			} catch (MalformedURLException e1) {
				return false;
			}

			// split the pattern into two parts
			String urlPatt1stPart = null;
			String urlPatt2ndPart = null;
			boolean bPortStar = false;
			try {

				URL urlPatt = new URL(patternDecoded);
				int port = urlPatt.getPort();
				String strPort= "";
				if(port== -1){
					strPort = "[0-9].*";
				}else{
					strPort = port+"";
				}
				urlPatt1stPart = "^"+urlPatt.getProtocol()+"://"+urlPatt.getHost()+":"+strPort;
				if(!(urlPatt.getFile()).startsWith("/")){ // The pattern must have "/" at after the port
					return false;
				}
				urlPatt2ndPart = "^"+urlPatt.getFile();
			} catch (MalformedURLException e) {
				bPortStar = true;
			}

			if(bPortStar){
				int indexOfStar = patternDecoded.indexOf("*");
				if(indexOfStar != -1){
					urlPatt1stPart = "^"+patternDecoded.substring(0, indexOfStar)+"[0-9].*";
					if(!(patternDecoded.substring(indexOfStar+1)).startsWith("/")){
						return false;
					}
					urlPatt2ndPart = "^"+patternDecoded.substring(indexOfStar+1);

				}
			}

			//	check 1st part of both with ignorecase
			RE re;
			try {
				re = new RE(urlPatt1stPart,RE.REG_ICASE); // ignore case for 1st part 
				REMatch reMatch = re.getMatch(urlValue1stPart);
				if(reMatch!= null){
					//	check 2nd part of both with case
					re = new RE(urlPatt2ndPart); 
					reMatch = re.getMatch(urlValue2ndPart);
					if(reMatch!= null){
						return true;
					}
				}

			} catch (REException e) {
				return false;
			}
		}else{

			String pat1 = null;
			String pat2 = null;
			//	split the pattern into two parts
			if(patternDecoded.indexOf("/")!= -1){
				if(patternDecoded.indexOf(":") == -1){
					pat1 = patternDecoded.substring(0,patternDecoded.indexOf("/"))+":[0-9].*";
				}else{
					pat1 = patternDecoded.substring(0,patternDecoded.indexOf("/"));	
				}
				pat2 = patternDecoded.substring(patternDecoded.indexOf("/"));
			}else{
				// The pattern must have "/" at after the port
				return false;
			}

			pat1 = "^.*://.*" +pat1;
			pat2 = "^"+pat2;
			URL urlValue;
			try {
				urlValue = new URL(strValue);
				int port = urlValue.getPort();
				if(port== -1){
					port = urlValue.getDefaultPort();
				}
				String urlValue1stPart = urlValue.getProtocol()+"://"+urlValue.getHost()+":"+port;
				String urlValue2ndPart = urlValue.getFile();

				if(urlValue2ndPart!= null){
					if(!urlValue2ndPart.startsWith("/")){
						urlValue2ndPart="/"+urlValue2ndPart;
					}
				}
				
				RE re;
				try {
					re = new RE(pat1,RE.REG_ICASE); // ignore case for 1st part 
					REMatch reMatch = re.getMatch(urlValue1stPart);
					if(reMatch!= null){
						re = new RE(pat2); // with case for 2nd part
						reMatch = re.getMatch(urlValue2ndPart);
						if(reMatch!= null){
							return true;
						}
					}

				} catch (REException e) {
					return false;
				}
			} catch (MalformedURLException e) {
				return false;
			}

		}

		return false;

	}



	/**
	 * Desc : match the String Value with the List.
	 * @param strList
	 * @param strValue
	 * @return
	 *//*
	public boolean match(List strList, String strValue){
		boolean bValue = false;
		if((strList != null) && (strValue != null)) {
			int length = strList.size();
			for(int i=0; i<length ; i++) {
				String strURLPat = (String) strList.get(i);
				if(strURLPat!= null && ! collator.equals(strURLPat,BLANK_STRING)){

					if(strValue.matches((String)strURLPat)) {
						return true ;
					}else{
						String strDecodedValue = URLDecoder.decode(strValue);
						String strDecodedURLPat = URLDecoder.decode(strURLPat);
						if(strDecodedValue!=null){
							if(strDecodedValue.startsWith(strDecodedURLPat)){
								return true;
							}
						}

					}
				}
			}
		}
		return bValue;
	}*/

	/**
	 * Desc : match the Value List with the array of String Key .
	 * @param strKeyList
	 * @param strValueList
	 * @return
	 *//*
	public List match(String[] strKeyList, List strValueList){

		if((strKeyList != null) && (strValueList != null)) {
			int lengthKeyList = strKeyList.length;
			int sizeValueList = strValueList.size();
			for(int i=0; i<lengthKeyList ; i++) {
				if(strKeyList[i]!= null && ! collator.equals(strKeyList[i],BLANK_STRING)) {
					for(int j= (sizeValueList -1) ; i >= 0  ; j--)	{
						String strValue = (String) strValueList.get(j);
						if(strValue.matches(strKeyList[i]) || (URLDecoder.decode(strValue)).startsWith(URLDecoder.decode(strKeyList[i]))) {
							strValueList.remove(j);
						}
					}
				}
			}
		}
		return strValueList;
	}*/



	/**
	 * Desc : match the Value List with the List of String Key.
	 * @param strKeyList
	 * @param strValueList
	 * @return
	 */

	/*public List match(List strKeyList, List strValueList){

		if((strKeyList != null) && (strValueList != null)) {
			int sizeKeyList = strKeyList.size();
			int sizeValueList = strValueList.size();
			for(int i=0; i<sizeKeyList ; i++) {
				if(strKeyList.get(i)!= null && ! collator.equals((String) strKeyList.get(i),BLANK_STRING)) {
					for(int j= (sizeValueList -1) ; i >= 0  ; j--)	{
						String strValue = (String) strValueList.get(j);
						if(strValue.matches(URLDecoder.decode((String)strKeyList.get(i))) || (URLDecoder.decode(strValue)).startsWith(URLDecoder.decode((String)strKeyList.get(i)))) {
							strValueList.remove(j);
						}
					}
				}
			}
		}
		return strValueList;
	}*/

}
