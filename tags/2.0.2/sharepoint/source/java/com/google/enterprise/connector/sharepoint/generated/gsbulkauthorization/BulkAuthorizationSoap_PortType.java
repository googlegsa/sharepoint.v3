/**
 * BulkAuthorizationSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization;

public interface BulkAuthorizationSoap_PortType extends java.rmi.Remote {
    public void authorize(com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData authData, java.lang.String loginId) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData[] bulkAuthorize(com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.AuthData[] authData, java.lang.String loginId) throws java.rmi.RemoteException;
    public java.lang.String checkConnectivity() throws java.rmi.RemoteException;
}
