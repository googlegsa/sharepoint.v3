/**
 * AuthenticationSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.authentication;

public interface AuthenticationSoap_PortType extends java.rmi.Remote {
    public com.google.enterprise.connector.sharepoint.generated.authentication.LoginResult login(java.lang.String username, java.lang.String password) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationMode mode() throws java.rmi.RemoteException;
}
