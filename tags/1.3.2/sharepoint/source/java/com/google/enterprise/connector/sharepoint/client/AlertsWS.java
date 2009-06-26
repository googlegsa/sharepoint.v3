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
	private static final String ALERTSENDPOINT = "/_vti_bin/alerts.asmx";
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
		//Adding safe characters will prevent them to encoded while encoding the URL
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
				LOGGER.config("AlertsEndpoint: "+endpoint);
				AlertsLocator loc = new AlertsLocator();
				loc.setAlertsSoapEndpointAddress(endpoint);
				Alerts alertsService = loc;

				try {
					stub = (AlertsSoap_BindingStub) alertsService.getAlertsSoap();
				} catch (ServiceException e) {
					LOGGER.warning(sFuncName+":"+e.toString());
					throw e;
				}

				String strDomain = sharepointClientContext.getDomain();
				String strUser = sharepointClientContext.getUsername();
				String strPassword= sharepointClientContext.getPassword();

				if((strDomain==null)||(strDomain.trim().equals(""))){
					strDomain=strUser; //for user
				}else{
					strDomain+="\\"+strUser; // form domain/user
				}

				//set the user and password
				stub.setUsername(strDomain);
				stub.setPassword(strPassword);
			} catch (Throwable e) {
				LOGGER.warning(sFuncName+"Exception : Unable to connect to alerts service stub");
				throw new SharepointException(e.toString());        
			}
		}
		LOGGER.exiting(className, sFuncName);
	}

	public AlertsWS(SharepointClientContext inSharepointClientContext,String siteName) throws RepositoryException {
		final String sFunName = "AlertsWS(SharepointClientContext inSharepointClientContext,String siteName)";
		LOGGER.entering(className, sFunName);
		LOGGER.config(sFunName+": siteName["+siteName+"]"); //added by Nitendra
		
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
			throw new SharepointException(sFunName+": Malformed URL: ["+siteName+"]");
		}

		int iPort = 0;
		//check if the default port
		if (-1 != siteURL.getPort()) {
			iPort = siteURL.getPort();
		}else{
			iPort = siteURL.getDefaultPort();
		}

//		if(siteURL.getPath().endsWith("/")){
			endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPort/*siteURL.getPort()*/+enc.encode(siteURL.getPath())+ ALERTSENDPOINT;
		/*}else{
			endpoint = siteURL.getProtocol()+URL_SEP+ siteURL.getHost()+":"+iPortsiteURL.getPort()+enc.encode(siteURL.getPath())+ "/"+ALERTSENDPOINT;
		}*/
		LOGGER.config(sFunName+": AlertsEnd: ["+endpoint+"]");

		try {
			AlertsLocator loc = new AlertsLocator();
			loc.setAlertsSoapEndpointAddress(endpoint);
			Alerts alertsService = loc;
			try {
				stub = (AlertsSoap_BindingStub) alertsService.getAlertsSoap();
			} catch (ServiceException e) {
				LOGGER.warning(sFunName+"Exception: \n"+e.toString());
				throw e;
			}

			String strDomain = inSharepointClientContext.getDomain();
			String strUser = inSharepointClientContext.getUsername();
			String strPassword= inSharepointClientContext.getPassword();

			if((strDomain==null)||(strDomain.trim().equals(""))){
				strDomain=strUser; //for user
			}else{
				strDomain+="\\"+strUser; // form domain/user
			}

			//set the user and password
			stub.setUsername(strDomain);
			stub.setPassword(strPassword);
		} catch (Throwable e) {
			LOGGER.warning(sFunName+"Exception : Unable to connect to alerts service stub");
			throw new SharepointException(e.toString());        
		}
		LOGGER.exiting(className, sFunName);
	}

	public List getAlerts(String parentWebTitle) throws SharepointException{
		String sFuncName = "getAlerts()";
		LOGGER.entering(className, sFuncName);
		LOGGER.config(sFuncName+": parentWebTitle["+parentWebTitle+"]"); //added by Nitendra
		
		ArrayList lstAllAlerts = new ArrayList();

		if(stub==null){
			throw new SharepointException("Unable to get the alerts stub");
		}
		AlertInfo alertsInfo=null;
		try{
			try {
				alertsInfo= stub.getAlerts();
			} catch (RemoteException e) {
				LOGGER.warning(sFuncName+"Exception:\n"+e.toString());
				throw new SharepointException(e);
			}


			Alert[]  alerts = alertsInfo.getAlerts();

			if(alerts!=null){
				for(int i=0;i<alerts.length;++i){
					//add the alert in the List
					Calendar c = Calendar.getInstance();
					c.setTime(new Date());// Alerts do not fetch the date .. set it it current time by default

					LOGGER.config("Fetched(Alert): ID="+alerts[i].getId()+"|AlertForTitle: "+alerts[i].getAlertForTitle()+"|AlertForUrl: "+alerts[i].getAlertForUrl()+"|Title: "+alerts[i].getTitle()+"|EditURL: "+alerts[i].getEditAlertUrl()+"|Date: "+c);
					SPDocument doc = new SPDocument(alerts[i].getId(),alerts[i].getEditAlertUrl(),c/*new Date(100)*/,ALERTS_TYPE,parentWebTitle);
					lstAllAlerts.add(doc);
				}
			}
		}catch (Exception e) {
			LOGGER.warning(className+":"+sFuncName+": Problem getting alerts:"+e.getMessage());
		}
		LOGGER.exiting(className, sFuncName);
		return lstAllAlerts;
	}

}
