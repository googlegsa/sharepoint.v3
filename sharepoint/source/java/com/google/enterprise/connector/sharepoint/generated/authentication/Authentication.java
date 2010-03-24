/**
 * Authentication.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.authentication;

public interface Authentication extends javax.xml.rpc.Service {
    public java.lang.String getAuthenticationSoapAddress();

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
    public java.lang.String getAuthenticationSoap12Address();

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap12() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
