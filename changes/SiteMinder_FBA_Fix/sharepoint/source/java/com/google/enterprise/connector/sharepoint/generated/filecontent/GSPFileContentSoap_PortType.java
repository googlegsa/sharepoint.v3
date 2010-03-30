/**
 * GSPFileContentSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.filecontent;

public interface GSPFileContentSoap_PortType extends java.rmi.Remote {
    public java.lang.String helloWorld() throws java.rmi.RemoteException;
    public byte[] getFileContents(java.lang.String fileURL) throws java.rmi.RemoteException;
}
