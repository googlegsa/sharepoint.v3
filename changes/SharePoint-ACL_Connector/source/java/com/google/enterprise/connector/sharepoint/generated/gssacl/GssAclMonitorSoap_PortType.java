/**
 * GssAclMonitorSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssacl;

public interface GssAclMonitorSoap_PortType extends java.rmi.Remote {
    public java.lang.String checkConnectivity() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclForUrlsResult getAclForUrls(java.lang.String[] urls) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssGetAclChangesSinceTokenResult getAclChangesSinceToken(java.lang.String strChangeToken) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.gssacl.GssResolveSPGroupResult resolveSPGroup(java.lang.String[] groupId) throws java.rmi.RemoteException;
    public java.lang.String[] getAffectedListIDsForChangeWeb(java.lang.String webGuId) throws java.rmi.RemoteException;
    public java.lang.String[] getAffectedItemIDsForChangeList(java.lang.String listGuId) throws java.rmi.RemoteException;
}
