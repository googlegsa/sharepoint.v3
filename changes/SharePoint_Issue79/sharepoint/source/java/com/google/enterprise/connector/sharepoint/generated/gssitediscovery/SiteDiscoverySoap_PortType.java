/**
 * SiteDiscoverySoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public interface SiteDiscoverySoap_PortType extends java.rmi.Remote {
    public java.lang.String checkConnectivity() throws java.rmi.RemoteException;
    public java.lang.Object[] getAllSiteCollectionFromAllWebApps() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo getWebCrawlInfo() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[] getListCrawlInfo(java.lang.String[] listGuids) throws java.rmi.RemoteException;
    public boolean isCrawlableList(java.lang.String listGUID) throws java.rmi.RemoteException;
}
