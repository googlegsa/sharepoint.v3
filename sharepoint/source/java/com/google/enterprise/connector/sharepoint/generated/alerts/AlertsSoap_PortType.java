/**
 * AlertsSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.alerts;

public interface AlertsSoap_PortType extends java.rmi.Remote {
    public com.google.enterprise.connector.sharepoint.generated.alerts.AlertInfo getAlerts() throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.alerts.DeleteFailure[] deleteAlerts(java.lang.String[] IDs) throws java.rmi.RemoteException;
}
