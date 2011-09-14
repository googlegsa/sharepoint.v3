/**
 * SiteDiscovery.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public interface SiteDiscovery extends javax.xml.rpc.Service {
    public java.lang.String getSiteDiscoverySoap12Address();

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap12() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getSiteDiscoverySoapAddress();

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
