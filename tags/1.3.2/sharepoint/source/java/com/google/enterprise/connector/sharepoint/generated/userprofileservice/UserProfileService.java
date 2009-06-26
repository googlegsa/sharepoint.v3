/**
 * UserProfileService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public interface UserProfileService extends javax.xml.rpc.Service {

/**
 * User Profile Service
 */
    public java.lang.String getUserProfileServiceSoapAddress();

    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceSoap_PortType getUserProfileServiceSoap() throws javax.xml.rpc.ServiceException;

    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.UserProfileServiceSoap_PortType getUserProfileServiceSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
