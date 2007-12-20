package com.google.enterprise.connector.sharepoint.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;

//import org.apache.catalina.util.URL;
import org.apache.catalina.util.URLEncoder;

import com.google.enterprise.connector.sharepoint.generated.alerts.Alert;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertInfo;
import com.google.enterprise.connector.sharepoint.generated.alerts.Alerts;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertsLocator;
import com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_BindingStub;
import com.google.enterprise.connector.spi.RepositoryException;

/**
 * This class holds data and methods for any call to Alerts Web Service.
 * @author amit_kagrawal
 * */
public class AlertsWS {
	private static final String ALERTSENDPOINT = "_vti_bin/alerts.asmx";
	private static final Logger LOGGER = Logger.getLogger(AlertsWS.class.getName());
	private String className = AlertsWS.class.getName();
	public static final String ALERTS_TYPE = "Alerts";
	private SharepointClientContext sharepointClientContext;
	static final String URL_SEP ="://";
	private String endpoint;
	static final String COLON = ":";
	AlertsSoap_BindingStub stub;
	public static URLEncoder enc  = new URLEncoder();
	
	static{
		//set the URLEncoder safe characters
		enc.addSafeCharacter('/');
		enc.addSafeCharacter(':');// required when endpoint is set using specified site
	}
	public AlertsWS(SharepointClientContext inSharepointClientContext) throws RepositoryException{
		String sFuncName = "AlertsWS(SharepointClientContext inSharepointClientContext)";
		LOGGER.entering(className, sFuncName);
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
			endpoint = sharepointClientContext.getProtocol()+URL_SEP + sharepointClientContext.getHost() + COLON 
			+sharepointClientContext.getPort() 
			+ enc.encode(sharepointClientContext.getsiteName()) + ALERTSENDPOINT;
			try {
				//System.out.println("AlertsEndpoint: "+endpoint);
				LOGGER.config("AlertsEndpoint: "+endpoint);
				AlertsLocator loc = new AlertsLocator();
				loc.setAlertsSoapEndpointAddress(endpoint);
				Alerts alertsService = loc;
				try {
					stub = (AlertsSoap_BindingStub) alertsService.getAlertsSoap();
				} catch (ServiceException e) {
					e.printStackTrace();
				}
				
				String strDomain = sharepointClientContext.getDomain();
				String strUser = sharepointClientContext.getUsername();
				String strPassword= sharepointClientContext.getPassword();
				strDomain+="\\"+strUser; // form domain/user 
				
				//set the user and pass
				stub.setUsername(strDomain);
				stub.setPassword(strPassword);
			} catch (Throwable e) {
				throw new SharepointException(e.toString());        
			}
		}
		LOGGER.exiting(className, sFuncName);
	}

	public AlertsWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
		final String sFunName = "AlertsWS(SharepointClientContext inSharepointClientContext,String siteName)";
		LOGGER.entering(className, sFunName);
		if(inSharepointClientContext!=null){
			this.sharepointClientContext = inSharepointClientContext;
		}
		if(siteName==null){
			throw new SharepointException(sFunName+": Unable to get the site name");
		}
		URL siteURL ;
		 try {
			siteURL = new URL(siteName);
		} catch (MalformedURLException e) {
			throw new SharepointException("Malformed URL: "+siteName);
		}
		int iPort = 0;
		//check if the def
		if (-1 != siteURL.getPort()) {
			iPort = siteURL.getPort();
		}else{
			iPort = siteURL.getDefaultPort();
		}
//		endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+siteURL.getPort()+enc.encode(siteURL.getPath())+ ALERTSENDPOINT;

		/*if(siteName!=null){
			if (siteName.startsWith(HTTP+URL_SEP)) {
				siteName = siteName.substring(7);
				endpoint = HTTP+URL_SEP + Util.getEscapedSiteName(siteName) + ALERTSENDPOINT;
			}else if(siteName.startsWith(HTTPS+URL_SEP)) {
				siteName = siteName.substring(8);
				endpoint = HTTPS+URL_SEP + Util.getEscapedSiteName(siteName) + ALERTSENDPOINT;
			} else {
				endpoint = Util.getEscapedSiteName(siteName) + ALERTSENDPOINT;
			}
		}*/
//		if(siteName.endsWith("/")){
		if(siteURL.getPath().endsWith("/")){
			endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort/*siteURL.getPort()*/+enc.encode(siteURL.getPath())+ ALERTSENDPOINT;
//			endpoint = siteName+ALERTSENDPOINT;
		}else{
			endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort/*siteURL.getPort()*/+enc.encode(siteURL.getPath())+ "/"+ALERTSENDPOINT;
//			endpoint = siteName+"/"+ALERTSENDPOINT;
		}
		try {
			//System.out.println("AlertsEnd: "+endpoint);
			AlertsLocator loc = new AlertsLocator();
			loc.setAlertsSoapEndpointAddress(endpoint);
			Alerts alertsService = loc;
			try {
				stub = (AlertsSoap_BindingStub) alertsService.getAlertsSoap();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
			
			String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			String strPassword= inSharepointClientContext.getPassword();
			strDomain+="\\"+strUser; // form domain/user 
			
			//set the user and pass
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		} catch (Throwable e) {
			throw new SharepointException(e.toString());        
		}
		LOGGER.exiting(className, sFunName);
	}
	
	public List getAlerts() throws SharepointException{
		String sFuncName = "getAlerts()";
		LOGGER.entering(className, sFuncName);
		ArrayList lstAllAlerts = new ArrayList();
		
		if(stub==null){
			throw new SharepointException("Unable to get the alerts stub");
		}
		AlertInfo alertsInfo=null;
		try {
			alertsInfo= stub.getAlerts();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		
		Alert[]  alerts = alertsInfo.getAlerts();
		
		if(alerts!=null){
			for(int i=0;i<alerts.length;++i){
				/*System.out.println("------------Alert["+(i+1)+"]--------------");
				System.out.println("AlertForTitle: "+alerts[i].getAlertForTitle());
				System.out.println("AlertForUrl: "+alerts[i].getAlertForUrl());
				System.out.println("Id: "+alerts[i].getId());
				System.out.println("Title: "+alerts[i].getTitle());*/
				
				//add the alert in the List
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());// Alerts do not fetch the date .. set it it current time by default
				SPDocument doc = new SPDocument(alerts[i].getId(),alerts[i].getEditAlertUrl(),c/*new Date(100)*/,ALERTS_TYPE);
				lstAllAlerts.add(doc);
			}
		}
		LOGGER.exiting(className, sFuncName);
		return lstAllAlerts;
	}

}
