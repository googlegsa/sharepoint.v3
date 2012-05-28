/**
 * BulkAuthorizationSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization;

public interface BulkAuthorizationSoap_PortType extends java.rmi.Remote {
    public java.lang.String checkConnectivity() throws java.rmi.RemoteException;
    public void authorize(com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.holders.ArrayOfAuthDataPacketHolder authDataPacketArray, java.lang.String username) throws java.rmi.RemoteException;
}
