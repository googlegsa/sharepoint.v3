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

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.convert.ConverterManager;
import org.joda.time.convert.InstantConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnectorType;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * Class to hold random utility functions
 * @author nitendra_thakur
 *
 */
public final class Util {

  private static final String TIMEFORMAT1 = "yyyy-MM-dd HH:mm:ss";
  private static final String TIMEFORMAT2 = "yyyy-MM-dd HH:mm:ss'Z'";
  private static final String TIMEFORMAT3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  
  public static final String AUTHOR = "sharepoint:author";
  public static final String LIST_GUID = "sharepoint:listguid";
  
  private static final InstantConverter TIME_CONVERTER_FROM_CALENDAR = ConverterManager.getInstance().getInstantConverter(new GregorianCalendar());
  private static final InstantConverter TIME_CONVERTER_FROM_JODA = ConverterManager.getInstance().getInstantConverter(new DateTime());
  private static final Chronology CHRON = new DateTime().getChronology();
  private static final DateTimeFormatter FORMATTER = ISODateTimeFormat.basicDateTime();
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER1 = new SimpleDateFormat(TIMEFORMAT1);
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER2 = new SimpleDateFormat(TIMEFORMAT2);
  private static final SimpleDateFormat SIMPLE_DATE_FORMATTER3 = new SimpleDateFormat(TIMEFORMAT3);
  
  private static final Logger LOGGER = Logger.getLogger(SharepointConnectorType.class.getName());
  	
  /**
   * Formats last modified date (yyyy-MM-dd HH:mm:ss) to Calendar format
   * @param strDate
   * @throws ParseException
   */
  public static Calendar listItemsStringToCalendar(final String strDate) throws ParseException  {
	final Date dt = SIMPLE_DATE_FORMATTER1.parse(strDate);
    final Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return c;
  }
  
  /**
   * Formats last modified date (yyyy-MM-dd'T'HH:mm:ss'Z') to Calendar format
   * @param strDate
   * @throws ParseException
   */
  public static Calendar listItemChangesStringToCalendar(final String strDate) throws ParseException  {
	final Date dt = SIMPLE_DATE_FORMATTER3.parse(strDate);
    final Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return c;
  }
  
  /**
   * Formats last modified date (yyyy-MM-dd HH:mm:ss'Z') to Calendar format
   * @param strDate
   * 
   * @throws ParseException
   */
  public static Calendar siteDataStringToCalendar(final String strDate) throws ParseException  {
	final Date dt = SIMPLE_DATE_FORMATTER2.parse(strDate);
    final Calendar c = Calendar.getInstance();
    c.setTime(dt);
    return c;
  }
  
  /**
   * Converts a DateTime format to Calendar
   * @param date
   * 
   */
  public static Calendar jodaToCalendar(final DateTime date) {
	final long millis = TIME_CONVERTER_FROM_JODA.getInstantMillis(date, CHRON);
    final GregorianCalendar cal = new GregorianCalendar();
    cal.setTimeInMillis(millis);
    return cal;
  }
  
  /**
   * Converts a Calendar format to DateTime
   * @param cal
   * 
   */
  public static DateTime calendarToJoda(final Calendar cal) {
	final long millis = TIME_CONVERTER_FROM_CALENDAR.getInstantMillis(cal, CHRON);
    DateTime dt = new DateTime(millis, CHRON);
    return dt;
  }
  
  /**
   * Return a String formated DateTime value
   * @param date
   * 
   */
  public static String formatDate(final DateTime date) {
	String dtString = FORMATTER.print(date);
    return dtString;
  }
  
  /**
   * Return a string formated Calendar value
   * @param cal
   * 
   */
  public static String formatDate(final Calendar cal) {
	String dtString = FORMATTER.print(calendarToJoda(cal));
    return dtString;
  }
  
  /**
   * returns a DateTime value for a well-formed string
   * @param str
   * 
   */
  public static DateTime parseDate(final String str) {
	DateTime dt = FORMATTER.parseDateTime(str);
    return dt;
  }
 
  /**
   * For removing line terminators
   * @param inputStr
   * 
   */
  public static CharSequence removeLineTerminators(final CharSequence inputStr) {
	final String patternStr = "(?m)$^|[\\r\\n]+\\z";
    final String replaceStr = " ";
    final Pattern pattern = Pattern.compile(patternStr);
    final Matcher matcher = pattern.matcher(inputStr);
    CharSequence charSeq = matcher.replaceAll(replaceStr);
    return charSeq;
  }  
  
  /**
   * Returns a formatted site URL. appends / at the end and encodes various non-UTF character.
   * @param siteName
   * 
   * @throws RepositoryException
   */
  public static String getEscapedSiteName(final String siteName) throws RepositoryException {
	final StringBuffer escapedSiteName = new StringBuffer();
    final String siteNamearray[] = siteName.split(SPConstants.SLASH);
    for (final String str : siteNamearray) {
      try {
    	  escapedSiteName.append(URLEncoder.encode(str, "UTF-8")).append(SPConstants.SLASH);
      } catch (final UnsupportedEncodingException e) {
        throw new RepositoryException(e.toString());
      }
    }
    final String str = escapedSiteName.toString();
    str.replace('+', ' ');
    String strModified = str.replaceAll(" ", "%20");
    return strModified;
  }

  /**
   * return collator for default locale
   * 
   */
  public static Collator getCollator() {
	  final Collator collator = Collator.getInstance();
	  collator.setStrength(Collator.PRIMARY);
	  return collator;
  }

  /**
   * return collator for a given locale
   * @param locale
   * 
   */
  public static Collator getCollator(final Locale locale) {
	if(locale == null) {
		return getCollator();
	}		
	final Collator collator = Collator.getInstance(locale);
	collator.setStrength(Collator.PRIMARY);
	return collator;
  }	

  /**
	 * Desc : match the String Value with the string array.
	 * @param strList
	 * @param strValue
	 * 
	 */
	public static boolean match(final String[]strList, final String strValue, final StringBuffer matchedPattern){
		if((strList == null) || (strValue == null)) {
			return false;
		}
		for (final String strURLPat : strList) {
			if((strURLPat!= null) && (strURLPat.length() > 0)) {
				String strDecodedValue = strValue;
				String strDecodedURLPat = strURLPat;
				try {
					strDecodedValue = URLDecoder.decode(strValue, "UTF-8");
					strDecodedURLPat = URLDecoder.decode(strURLPat, "UTF-8");					
				} catch(final Exception e) {
					LOGGER.log(Level.FINE, e.getMessage());
					strDecodedValue = strValue;
					strDecodedURLPat = strURLPat;
				}
				if(Util.matcher(strDecodedURLPat, strDecodedValue)) {
					if(matchedPattern != null) {
						matchedPattern.append(strURLPat);
					}
					return true ;
				}
			}
		}	
		return false;
	}
	
	/**
	 * Matches a url with a pattern. Mimics GSA's pattern matching
	 * @param pattern
	 * @param strValue
	 * 
	 */
	public static boolean matcher(final String pattern,final String strValue){
		// null check for the arguments
		if((pattern==null)|(strValue==null)){
			return false;
		}

		// If the pattern starts with "#" then its a comment so ignore
		if(pattern.startsWith(SPConstants.HASH)){
			return false;
		}

		// if the pattern starts with "-", remove the "-" from begin and proceed
		if(pattern.startsWith(SPConstants.MINUS)){
			return false;			
		}

		// handle "contains:"
		// if pattern starts with "contains:" then check if the URL contains the string in pattern
		if(pattern.startsWith(SPConstants.CONTAINS)){
			final StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			final String strContainKey = new String(tempBuffer.delete(0,SPConstants.CONTAINS.length()));
			RE re;
			try {
				re = new RE(strContainKey);  // with case
				final REMatch reMatch = re.getMatch(strValue);
				if(reMatch != null){
					return true;
				}
				return false;

			} catch (final REException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}
		}


		//	handle regexp
		// if pattern starts with "regexp:", then check for regex match with case
		if(pattern.startsWith(SPConstants.REGEXP)){
			final StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			final String strRegexPattrn = new String(tempBuffer.delete(0,SPConstants.REGEXP.length()));
			RE re;
			try {
				re = new RE(strRegexPattrn);
				final REMatch reMatch = re.getMatch(strValue);
				if(reMatch!= null){
					return true;
				}
				return false;

			} catch (final REException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}
		}

		// handle regexpCase
		// if pattern starts with "regexpCase:", then check for regex match with case
		if(pattern.startsWith(SPConstants.REGEXP_CASE)){
			final StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			final String strRegexCasePattrn = new String(tempBuffer.delete(0,SPConstants.REGEXP_CASE.length()));
			RE re;
			try {
				re = new RE(strRegexCasePattrn);
				final REMatch reMatch = re.getMatch(strValue);
				if(reMatch!= null){
					return true;
				}
				return false;

			} catch (final REException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}
		}

		// handle regexpIgnoreCase
		// if pattern starts with "regexpIgnoreCase:", then check for regex match without case
		if(pattern.startsWith(SPConstants.REGEXP_IGNORE_CASE)){
			final StringBuffer tempBuffer = new StringBuffer(pattern);
			if(tempBuffer == null){
				return false;
			}
			final String strRegexIgnoreCasePattrn = new String(tempBuffer.delete(0,SPConstants.REGEXP_IGNORE_CASE.length()));
			RE re;
			try {
				re = new RE(strRegexIgnoreCasePattrn,RE.REG_ICASE); // ignore case
				final REMatch reMatch = re.getMatch(strValue);
				if(reMatch!= null){
					return true;
				}
				return false;

			} catch (final REException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}
		}

		//	handle "^" and "$"
		if(pattern.startsWith(SPConstants.CARET)||pattern.endsWith(SPConstants.DOLLAR)){
			StringBuffer tempBuffer = new StringBuffer(pattern);
			boolean bDollar =false;
			String strValueModified = strValue;
			if(pattern.startsWith(SPConstants.CARET)){
				URL urlValue;
				try {
					urlValue = new URL(strValue);
					int port = urlValue.getPort();
					if(port== -1){
						port = urlValue.getDefaultPort();
						strValueModified = urlValue.getProtocol() + SPConstants.URL_SEP + urlValue.getHost() + SPConstants.COLON + port+ urlValue.getFile();
					}

				} catch (final MalformedURLException e1) {
					LOGGER.log(Level.FINE, e1.getMessage());
					return false;
				}
				tempBuffer = new StringBuffer(pattern);
				final int indexOfStar = tempBuffer.indexOf("*");
				if(indexOfStar!= -1){
					tempBuffer.replace(indexOfStar, indexOfStar+"*".length(), "[0-9].*");
				}else{
					tempBuffer.delete(0, "^".length());
					if(pattern.endsWith(SPConstants.DOLLAR)){
						bDollar = true;
						tempBuffer.delete(tempBuffer.length()-SPConstants.DOLLAR.length(),tempBuffer.length());
					}
					try {

						final URL urlPatt = new URL(tempBuffer.toString());
						final int port = urlPatt.getPort();

						final String strHost = urlPatt.getHost().toString();

						if((port == -1) && (strHost !=null) && (strHost.length()!= 0)){
							tempBuffer = new StringBuffer("^"+urlPatt.getProtocol() + SPConstants.URL_SEP + urlPatt.getHost()+":[0-9].*"+urlPatt.getPath());
						}
						if(bDollar){
							tempBuffer.append(SPConstants.DOLLAR);
						}
					} catch (final MalformedURLException e) {
						LOGGER.log(Level.FINE, e.getMessage());
						tempBuffer = new StringBuffer(pattern);
					}
				}

			}

			RE re;
			try {
				re = new RE(tempBuffer);
				final REMatch reMatch = re.getMatch(strValueModified);
				if(reMatch != null){
					return true;
				}
				return false;				
			} catch (final REException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}
		}


		// url decode the pattern
		String patternDecoded = pattern;
		try {
			patternDecoded = URLDecoder.decode(pattern, "UTF-8");
		} catch(final Exception e) {
			LOGGER.log(Level.FINE, e.getMessage());
			patternDecoded = pattern;
		}

		if(patternDecoded==null){
			return false;
		}
		
		boolean containProtocol = false;
		try {
			final RE re = new RE(SPConstants.URL_SEP);
			final REMatch reMatch = re.getMatch(patternDecoded);
			if(reMatch != null){
				containProtocol = true; // protocol is present 
			}

		} catch (final REException e) {
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
				urlValue1stPart = urlValue.getProtocol() + SPConstants.URL_SEP + urlValue.getHost() + SPConstants.COLON + port;
				urlValue2ndPart = urlValue.getFile();

				if(urlValue2ndPart!= null){
					if(!urlValue2ndPart.startsWith(SPConstants.SLASH)){
						urlValue2ndPart=SPConstants.SLASH + urlValue2ndPart;
					}
				}
			} catch (final MalformedURLException e1) {
				LOGGER.log(Level.FINE, e1.getMessage());
				return false;
			}

			// split the pattern into two parts
			String urlPatt1stPart = null;
			String urlPatt2ndPart = null;
			boolean bPortStar = false;
			try {

				final URL urlPatt = new URL(patternDecoded);
				final int port = urlPatt.getPort();
				String strPort= "";
				if(port== -1){
					strPort = "[0-9].*";
				}else{
					strPort = port+"";
				}
				urlPatt1stPart = "^" + urlPatt.getProtocol() + SPConstants.URL_SEP + urlPatt.getHost() + SPConstants.COLON + strPort;
				if(!(urlPatt.getFile()).startsWith(SPConstants.SLASH)){ // The pattern must have "/" at after the port
					return false;
				}
				urlPatt2ndPart = "^"+urlPatt.getFile();
			} catch (final MalformedURLException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				bPortStar = true;
			}

			if(bPortStar){
				final int indexOfStar = patternDecoded.indexOf("*");
				if(indexOfStar != -1){
					urlPatt1stPart = "^"+patternDecoded.substring(0, indexOfStar)+"[0-9].*";
					if(!(patternDecoded.substring(indexOfStar+1)).startsWith(SPConstants.SLASH)){
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

			} catch (final REException e) {	
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}catch (final Exception e) {
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}
			
		}else{

			String pat1 = null;
			String pat2 = null;
			//	split the pattern into two parts
			if(patternDecoded.indexOf(SPConstants.SLASH)!= -1){
				if(patternDecoded.indexOf(SPConstants.COLON) == -1){
					pat1 = patternDecoded.substring(0,patternDecoded.indexOf(SPConstants.SLASH))+":[0-9].*";
				}else{
					pat1 = patternDecoded.substring(0,patternDecoded.indexOf(SPConstants.SLASH));	
				}
				pat2 = patternDecoded.substring(patternDecoded.indexOf(SPConstants.SLASH));
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
				final String urlValue1stPart = urlValue.getProtocol() + SPConstants.URL_SEP + urlValue.getHost() + SPConstants.COLON + port;
				String urlValue2ndPart = urlValue.getFile();

				if(urlValue2ndPart!= null){
					if(!urlValue2ndPart.startsWith(SPConstants.SLASH)){
						urlValue2ndPart=SPConstants.SLASH + urlValue2ndPart;
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

				} catch (final REException e) {
					LOGGER.log(Level.FINE, e.getMessage());
					return false;
				}
			} catch (final MalformedURLException e) {
				LOGGER.log(Level.FINE, e.getMessage());
				return false;
			}

		}
		
		return false;

	}
			
	/**
	 * return username in the format domain\\username
	 * @param userName
	 * @param domain
	 * 
	 */
	public static String getUserNameWithDomain(final String userName, final String domain){
		String tmpUsername = userName;
		if(userName==null){
			LOGGER.log(Level.FINEST, "returning username [ " + userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
			return null;
		}
		String modified_userName=null;
		if(userName.lastIndexOf(SPConstants.AT)!=-1){
			final String[] user_and_domain =userName.split(SPConstants.AT);//user@domain
			if((user_and_domain!=null) && (user_and_domain.length ==2)){
				modified_userName=user_and_domain[1] + SPConstants.DOUBLEBACKSLASH + user_and_domain[0]; //convert to domain\\user format
				LOGGER.log(Level.FINEST, "returning username [ " + modified_userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
				return modified_userName;
			}
		}else if(userName.lastIndexOf(SPConstants.SLASH) != SPConstants.MINUS_ONE){
			final String[] user_and_domain =userName.split(SPConstants.SLASH);//domain/user
			if((user_and_domain!=null) && (user_and_domain.length ==2)){
				modified_userName=user_and_domain[0] + SPConstants.DOUBLEBACKSLASH + user_and_domain[1]; //convert to domain\\user format
				LOGGER.log(Level.FINEST, "returning username [ " + modified_userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
				return modified_userName;
			}
		}else if(userName.lastIndexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE){
			LOGGER.log(Level.FINEST, "returning username [ " + userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
			return userName;
		}else if(null!=domain){
			modified_userName= domain + SPConstants.DOUBLEBACKSLASH + userName;
			LOGGER.log(Level.FINEST, "returning username [ " + modified_userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
			return modified_userName;				
		}
		LOGGER.log(Level.FINEST, "returning username [ " + userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
		return userName;
	}
	
	/**
	 * return username in the format username@domain
	 * @param userName
	 * @param domain
	 * 
	 */
	public static String getUserNameAtDomain(String userName, final String domain){
		String tmpUsername = userName;
		if(userName==null){
			LOGGER.log(Level.WARNING, "returning username [ " + userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
			return null;
		}
		String modified_userName=null;
		if(userName.lastIndexOf(SPConstants.DOUBLEBACKSLASH)!=-1){
			userName= userName.replace(SPConstants.DOUBLEBACKSLASH_CHAR, SPConstants.SLASH_CHAR);//else gives pattern exception while parsing			
		}
		
		if(userName.lastIndexOf(SPConstants.SLASH)!=-1){
			final String[] user_and_domain =userName.split(SPConstants.SLASH);
			if((user_and_domain!=null) && (user_and_domain.length ==2)){
				modified_userName=user_and_domain[1] + SPConstants.AT + user_and_domain[0]; //convert to user@domain format
				LOGGER.log(Level.FINEST, "returning username [ " + modified_userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
				return modified_userName;
			}
		}else if(userName.lastIndexOf(SPConstants.AT)!=-1){
			LOGGER.log(Level.FINEST, "returning username [ " + userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
			return userName; //when username is already in requred format
		}else if(null!=domain){
			modified_userName= userName + SPConstants.AT + domain;
			LOGGER.log(Level.FINEST, "returning username [ " + modified_userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
			return modified_userName;			
		}
		LOGGER.log(Level.FINEST, "returning username [ " + userName + " for input username [ " + tmpUsername + " ], domain [ " + domain + " ]. ");
		return userName;
	}
	
	/**
	 * finds and return domain from a username
	 * @param userName
	 * 
	 */
	public static String getDomainFromUsername(String userName) {
		String domain = null;
		if(userName!=null) {
			if(userName.indexOf(SPConstants.AT_CHAR)!=-1) {				
				final String[] cred = userName.split(SPConstants.AT);
				if((cred!=null) && (cred.length==2)) {
					domain = cred[1];
				}
			} else if(userName.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE){
				userName= userName.replace(SPConstants.DOUBLEBACKSLASH_CHAR, SPConstants.SLASH_CHAR);
				final String[] cred = userName.split(SPConstants.SLASH);
				if((cred!=null) && (cred.length==2)) {
					domain = cred[0];
				}
			}
		}
		LOGGER.log(Level.FINEST, "domain found as " + domain  + " for username [ " + userName + " ]. ");
		return domain;
	}
	
	/**
	 * Parses a username to get the username without domain info 
	 * @param userName
	 * 
	 */
	public static String getUserFromUsername(String userName) {
		String tmpUsername = userName;
		String user = userName;
		if(userName!=null) {
			if(userName.indexOf(SPConstants.AT_CHAR)!=-1) {				
				final String[] cred = userName.split(SPConstants.AT);
				if((cred!=null) && (cred.length==2)) {
					user = cred[0];
				}
			} else if(userName.indexOf(SPConstants.DOUBLEBACKSLASH) != SPConstants.MINUS_ONE){
				userName= userName.replace(SPConstants.DOUBLEBACKSLASH_CHAR, SPConstants.SLASH_CHAR);
				final String[] cred = userName.split(SPConstants.SLASH);
				if((cred!=null) && (cred.length==2)) {
					user = cred[1];
				}
			}
		}
		LOGGER.log(Level.FINEST, "input [ " + tmpUsername + " ], output [ " + user + " ]. ");
		return user;
	}
	
	/**
	 *  COnversion:
	 *  	domain\\username <=> username@domain 
	 * @param userName
	 * 
	 */
	public static String switchUserNameFormat(final String userName) {
		if(userName.indexOf(SPConstants.AT)!=-1) {
			return getUserNameWithDomain(userName, null);			
		} else {
			return getUserNameAtDomain(userName, null);			
		}
	}
	
	/**
	 * Get the web application url from a SharePoint URL
	 * @param web
	 * 
	 */
	public static String getWebApp(final String web) {
		URL url =null;
		try {
			url = new URL(web);
		} catch (final MalformedURLException e) {
			LOGGER.log(Level.FINE, web + " ... " + e.getMessage());
			return null;
		}

		final int port= url.getPort()==-1?url.getDefaultPort():url.getPort();
		final String res = url.getProtocol() + SPConstants.URL_SEP + url.getHost() + SPConstants.COLON + port;
		LOGGER.log(Level.FINEST, "input [ " + web + " ], output [ " + res + " ]. ");
		return res;
	}
	
	/**
	 * Checks if a string is numeric
	 * @param value
	 * 
	 */
	public static boolean isNumeric(final String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch(final Exception e) {
			LOGGER.log(Level.FINE, value + " ... " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Checks to see if the incoming value is a valid URL
	 * @param value
	 * 
	 */
	public static boolean isURL(final String value) {
		try {
			final URL url = new URL(value);
			if(url == null) {
				return false;
			}
		} catch(final Exception e) {
			LOGGER.log(Level.FINE, value + " ... " + e.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the original DOcID if the document ID has been modified in respect to the Content feed mode.
	 * @param docId
	 * @param feedType
	 * 
	 */
	public static String getOriginalDocId(final String docId, final String feedType) {
		String originalDocId = docId;
		if(docId == null) {
			return docId;
		}
		if(SPConstants.CONTENT_FEED.equalsIgnoreCase(feedType)) {
			final String[] parts = docId.split(SPConstants.DOUBLEBACKSLASH + SPConstants.DOC_TOKEN); // because | is a regexp character and has to be delimited.
			if(parts.length == 2) {
				originalDocId = parts[1];
			}			
		}
		LOGGER.log(Level.FINEST, "docId [ " + docId + " ], originalDocID [ " + originalDocId + " ]. ");
		return originalDocId;
	}
	
	/**
	 * return URL without default port if contained the default port.
	 * @param strUrl
	 * 
	 */
	public static String getWebURLForWSCall(final String strUrl) {
		String tmpSPURL = strUrl;
		if(strUrl != null){
			try {
				final URL url = new URL(strUrl);
				final String hostTmp = url.getHost();
				final String protocolTmp = url.getProtocol(); //to remove the hard-coded protocol
				int portTmp = -1; 
				if (-1 != url.getPort()) {
					if(url.getPort()!= url.getDefaultPort()){
						portTmp = url.getPort();
					}
				}
				String siteNameTmp = url.getPath();
				String strSPURL =  protocolTmp + SPConstants.URL_SEP + hostTmp;
				if(portTmp != -1){
					strSPURL += SPConstants.COLON + portTmp;
				}
				if(siteNameTmp.endsWith(SPConstants.SLASH)) {
					siteNameTmp = siteNameTmp.substring(0, siteNameTmp.length()-1);
				}
				strSPURL += siteNameTmp;
				return strSPURL;

			} catch (final MalformedURLException e) {
				LOGGER.log(Level.WARNING, e.toString());
			}
		}
		LOGGER.log(Level.FINEST, "input [ " + tmpSPURL + " ], output [ " + strUrl + " ]. ");
		return strUrl;
	}
	
	/**
	 * While specifying the folder level scope we have to remove the preceding parent site name. e.g, site/list/folder should be converted to list/folder
	 * @param webURL The parent Web URL 
	 * @param docPath docPath as returned by the Web Service
	 * 
	 */	
	public static String getFolderPathForWSCall(final String webURL, String docPath) {
		String tmpDocPath = docPath;
		if((webURL == null) || (docPath == null)) {
			return null;
		}
		if(docPath.startsWith(SPConstants.SLASH)) {
			docPath = docPath.replaceFirst(SPConstants.SLASH, "");
		}
		String webPath = "";
		try {
			final URL web_url = new URL(webURL);
			webPath = web_url.getPath();
			if(webPath.startsWith(SPConstants.SLASH)) {
				webPath = webPath.replaceFirst(SPConstants.SLASH, "");
			}
		} catch(final Exception e) {
			LOGGER.log(Level.WARNING, "Failed to create URL from given webURL.", e);
			return null;
		}
		if(docPath.startsWith(webPath)) {
			docPath = docPath.replaceFirst(webPath,"");
		} else {
			LOGGER.log(Level.WARNING, "Recieved docPath ["+docPath+"] is not valid as per the given webURL ["+webURL+"] ");
			return null;
		}
		if(docPath.startsWith(SPConstants.SLASH)) {
			docPath = docPath.replaceFirst(SPConstants.SLASH, "");
		}
		LOGGER.log(Level.FINEST, "input [ " + tmpDocPath + " ], output [ " + docPath + " ]. ");
		return docPath;
	}

	/**
	 * Normalizes the attribute's name to make better sense to the end user.
	 * @param metaName the attribute name as returned by the web service 
	 *  the normalized attribute name
	 */
	public static String normalizeMetadataName(String metaName) {
		String metaNameNormalized = metaName;
		if(null != metaNameNormalized) {
			if(metaNameNormalized.startsWith(SPConstants.OWS)) {
				metaNameNormalized = metaNameNormalized.replaceFirst(SPConstants.OWS, "");
			}
			if(metaNameNormalized.startsWith(SPConstants.METAINFO)) {
				metaNameNormalized = metaNameNormalized.replaceFirst(SPConstants.METAINFO, "");
			}
			if(metaNameNormalized.startsWith(SPConstants.VTI)) {
				metaNameNormalized = metaNameNormalized.replaceFirst(SPConstants.VTI, "");
			}
			metaNameNormalized = metaNameNormalized.replaceAll(SPConstants.ENCODED_SPACE, " ");
		}
		LOGGER.log(Level.FINEST, "metaName [ " + metaName + " ], metaNameNormalized [ " + metaNameNormalized + " ]. ");
		return metaNameNormalized;
	}
	
	/**
	 * Normalizes the attribute's value to make better sense to the end user.
	 * @param metaValue the attribute value as returned by the web service 
	 * @return the normalized attribute value
	 */
	public static String normalizeMetadataValue(String metaValue) {
		String metaValNormalized = metaValue;
		if(null != metaValNormalized) {
			final Matcher match = SPConstants.ATTRIBUTE_VALUE_PATTERN.matcher(metaValue);
			if(match.find()) {
				metaValNormalized = match.replaceFirst("");
			}
		}
		LOGGER.log(Level.FINEST, "metaValue [ " + metaValue + " ], metaValNormalized [ " + metaValNormalized + " ]. ");
		return metaValNormalized;
	}
	
	/**
	 * A file writter
	 * @param filePath absolute path of the file
	 * @param info message to be written
	 */
	public static void logInfo(final String filePath, final String info) {
		File file;
		file = new File(filePath);
		if(file == null) {
			return;
		}
		try{
			final FileWriter fw = new FileWriter(file,true);
			final PrintWriter pw = new PrintWriter(fw);
			
			final Calendar cal = Calendar.getInstance();
			final SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						
			pw.println(formater.format(cal.getTime()) + " : " + info);
			pw.flush();
			pw.close();			
		} catch (final IOException e) {
			LOGGER.log(Level.WARNING, "failed to log info [ " + info + " ] at filePath [ " + filePath + " ]. ", e);
		}catch(final Throwable e){
			LOGGER.log(Level.WARNING, "failed to log info [ " + info + " ] at filePath [ " + filePath + " ]. ", e);
		}
	}
	
	/**
	 * URL encoder.
	 * @param strURL
	 * 
	 */
	public static String encodeURL(final String strURL) {
		URL url = null;
		try {
			url = new URL(strURL);
			if(null == url) {
				LOGGER.log(Level.SEVERE, "Malformed URL!");
				return strURL;
			}
			//URI does the encoding of the URL automatically and also checks for the valid URL
			final URI uri = new URI(url.getProtocol(), null, url.getHost(),url.getPort(),url.getPath(),url.getQuery(),url.getRef());
			return uri.toASCIIString();			
		} catch(final Exception e) {
			LOGGER.log(Level.SEVERE, "Malformed URL [ " + strURL + " ]. ");
			return strURL;
		}
	}
	
	/**
	 * 
	 * @param strURL
	 * 
	 */
	public static String getHost(final String strURL) {
		URL url = null;
		try {
			url = new URL(strURL);
			if(null == url) {
				LOGGER.log(Level.SEVERE, "Malformed URL [ " + strURL + " ]. ");
				return null;
			}
		} catch(final Exception e) {
			LOGGER.log(Level.SEVERE, "Malformed URL [ " + strURL + " ]. ");
			return null;
		}
		return url.getHost();
	}
}
