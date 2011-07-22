/**
 * AlertsLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.alerts;

public class AlertsLocator extends org.apache.axis.client.Service implements com.google.enterprise.connector.sharepoint.generated.alerts.Alerts {

    public AlertsLocator() {
    }


    public AlertsLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AlertsLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AlertsSoap12
    private java.lang.String AlertsSoap12_address = "http://ps4312.persistent.co.in:43386/am1/_vti_bin/alerts.asmx";

    public java.lang.String getAlertsSoap12Address() {
        return AlertsSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AlertsSoap12WSDDServiceName = "AlertsSoap12";

    public java.lang.String getAlertsSoap12WSDDServiceName() {
        return AlertsSoap12WSDDServiceName;
    }

    public void setAlertsSoap12WSDDServiceName(java.lang.String name) {
        AlertsSoap12WSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_PortType getAlertsSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AlertsSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAlertsSoap12(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_PortType getAlertsSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap12Stub(portAddress, this);
            _stub.setPortName(getAlertsSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAlertsSoap12EndpointAddress(java.lang.String address) {
        AlertsSoap12_address = address;
    }


    // Use to get a proxy class for AlertsSoap
    private java.lang.String AlertsSoap_address = "http://ps4312.persistent.co.in:43386/am1/_vti_bin/alerts.asmx";

    public java.lang.String getAlertsSoapAddress() {
        return AlertsSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AlertsSoapWSDDServiceName = "AlertsSoap";

    public java.lang.String getAlertsSoapWSDDServiceName() {
        return AlertsSoapWSDDServiceName;
    }

    public void setAlertsSoapWSDDServiceName(java.lang.String name) {
        AlertsSoapWSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_PortType getAlertsSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AlertsSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAlertsSoap(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_PortType getAlertsSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_BindingStub(portAddress, this);
            _stub.setPortName(getAlertsSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAlertsSoapEndpointAddress(java.lang.String address) {
        AlertsSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     * This service has multiple ports for a given interface;
     * the proxy implementation returned may be indeterminate.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap12Stub(new java.net.URL(AlertsSoap12_address), this);
                _stub.setPortName(getAlertsSoap12WSDDServiceName());
                return _stub;
            }
            if (com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.alerts.AlertsSoap_BindingStub(new java.net.URL(AlertsSoap_address), this);
                _stub.setPortName(getAlertsSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("AlertsSoap12".equals(inputPortName)) {
            return getAlertsSoap12();
        }
        else if ("AlertsSoap".equals(inputPortName)) {
            return getAlertsSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "Alerts");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertsSoap12"));
            ports.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/2002/1/alerts/", "AlertsSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("AlertsSoap12".equals(portName)) {
            setAlertsSoap12EndpointAddress(address);
        }
        else
if ("AlertsSoap".equals(portName)) {
            setAlertsSoapEndpointAddress(address);
        }
        else
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
