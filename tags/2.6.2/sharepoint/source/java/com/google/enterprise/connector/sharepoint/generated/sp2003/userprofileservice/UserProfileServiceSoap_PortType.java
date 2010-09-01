/**
 * UserProfileServiceSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice;

public interface UserProfileServiceSoap_PortType extends java.rmi.Remote {
    public com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.GetUserProfileByIndexResult getUserProfileByIndex(int index) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData[] getUserProfileByName(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyData[] getUserProfileByGuid(java.lang.String guid) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.sp2003.userprofileservice.PropertyInfo[] getUserProfileSchema() throws java.rmi.RemoteException;
}
