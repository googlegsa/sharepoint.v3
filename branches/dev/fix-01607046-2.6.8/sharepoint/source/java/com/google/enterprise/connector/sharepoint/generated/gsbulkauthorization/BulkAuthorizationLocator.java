/**
 * BulkAuthorizationLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization;

public class BulkAuthorizationLocator extends org.apache.axis.client.Service implements com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorization {

    public BulkAuthorizationLocator() {
    }


    public BulkAuthorizationLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public BulkAuthorizationLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BulkAuthorizationSoap
    private java.lang.String BulkAuthorizationSoap_address = "http://gdc04.gdc-psl.net:6666/_vti_bin/GSBulkAuthorization.asmx";

    public java.lang.String getBulkAuthorizationSoapAddress() {
        return BulkAuthorizationSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BulkAuthorizationSoapWSDDServiceName = "BulkAuthorizationSoap";

    public java.lang.String getBulkAuthorizationSoapWSDDServiceName() {
        return BulkAuthorizationSoapWSDDServiceName;
    }

    public void setBulkAuthorizationSoapWSDDServiceName(java.lang.String name) {
        BulkAuthorizationSoapWSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_PortType getBulkAuthorizationSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BulkAuthorizationSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBulkAuthorizationSoap(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_PortType getBulkAuthorizationSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_BindingStub(portAddress, this);
            _stub.setPortName(getBulkAuthorizationSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBulkAuthorizationSoapEndpointAddress(java.lang.String address) {
        BulkAuthorizationSoap_address = address;
    }


    // Use to get a proxy class for BulkAuthorizationSoap12
    private java.lang.String BulkAuthorizationSoap12_address = "http://gdc04.gdc-psl.net:6666/_vti_bin/GSBulkAuthorization.asmx";

    public java.lang.String getBulkAuthorizationSoap12Address() {
        return BulkAuthorizationSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BulkAuthorizationSoap12WSDDServiceName = "BulkAuthorizationSoap12";

    public java.lang.String getBulkAuthorizationSoap12WSDDServiceName() {
        return BulkAuthorizationSoap12WSDDServiceName;
    }

    public void setBulkAuthorizationSoap12WSDDServiceName(java.lang.String name) {
        BulkAuthorizationSoap12WSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_PortType getBulkAuthorizationSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BulkAuthorizationSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBulkAuthorizationSoap12(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_PortType getBulkAuthorizationSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap12Stub(portAddress, this);
            _stub.setPortName(getBulkAuthorizationSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBulkAuthorizationSoap12EndpointAddress(java.lang.String address) {
        BulkAuthorizationSoap12_address = address;
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
            if (com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_BindingStub(new java.net.URL(BulkAuthorizationSoap_address), this);
                _stub.setPortName(getBulkAuthorizationSoapWSDDServiceName());
                return _stub;
            }
            if (com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gsbulkauthorization.BulkAuthorizationSoap12Stub(new java.net.URL(BulkAuthorizationSoap12_address), this);
                _stub.setPortName(getBulkAuthorizationSoap12WSDDServiceName());
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
        if ("BulkAuthorizationSoap".equals(inputPortName)) {
            return getBulkAuthorizationSoap();
        }
        else if ("BulkAuthorizationSoap12".equals(inputPortName)) {
            return getBulkAuthorizationSoap12();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "BulkAuthorization");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "BulkAuthorizationSoap"));
            ports.add(new javax.xml.namespace.QName("gsbulkauthorization.generated.sharepoint.connector.enterprise.google.com", "BulkAuthorizationSoap12"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("BulkAuthorizationSoap".equals(portName)) {
            setBulkAuthorizationSoapEndpointAddress(address);
        }
        else
if ("BulkAuthorizationSoap12".equals(portName)) {
            setBulkAuthorizationSoap12EndpointAddress(address);
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
