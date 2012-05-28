/**
 * SiteDiscoveryLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class SiteDiscoveryLocator extends org.apache.axis.client.Service implements com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscovery {

    public SiteDiscoveryLocator() {
    }


    public SiteDiscoveryLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SiteDiscoveryLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for SiteDiscoverySoap12
    private java.lang.String SiteDiscoverySoap12_address = "http://gdc04.gdc-psl.net:5555/_vti_bin/GSSiteDiscovery.asmx";

    public java.lang.String getSiteDiscoverySoap12Address() {
        return SiteDiscoverySoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SiteDiscoverySoap12WSDDServiceName = "SiteDiscoverySoap12";

    public java.lang.String getSiteDiscoverySoap12WSDDServiceName() {
        return SiteDiscoverySoap12WSDDServiceName;
    }

    public void setSiteDiscoverySoap12WSDDServiceName(java.lang.String name) {
        SiteDiscoverySoap12WSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SiteDiscoverySoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSiteDiscoverySoap12(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap12Stub(portAddress, this);
            _stub.setPortName(getSiteDiscoverySoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSiteDiscoverySoap12EndpointAddress(java.lang.String address) {
        SiteDiscoverySoap12_address = address;
    }


    // Use to get a proxy class for SiteDiscoverySoap
    private java.lang.String SiteDiscoverySoap_address = "http://gdc04.gdc-psl.net:5555/_vti_bin/GSSiteDiscovery.asmx";

    public java.lang.String getSiteDiscoverySoapAddress() {
        return SiteDiscoverySoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String SiteDiscoverySoapWSDDServiceName = "SiteDiscoverySoap";

    public java.lang.String getSiteDiscoverySoapWSDDServiceName() {
        return SiteDiscoverySoapWSDDServiceName;
    }

    public void setSiteDiscoverySoapWSDDServiceName(java.lang.String name) {
        SiteDiscoverySoapWSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(SiteDiscoverySoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getSiteDiscoverySoap(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType getSiteDiscoverySoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_BindingStub(portAddress, this);
            _stub.setPortName(getSiteDiscoverySoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setSiteDiscoverySoapEndpointAddress(java.lang.String address) {
        SiteDiscoverySoap_address = address;
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
            if (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap12Stub(new java.net.URL(SiteDiscoverySoap12_address), this);
                _stub.setPortName(getSiteDiscoverySoap12WSDDServiceName());
                return _stub;
            }
            if (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_BindingStub(new java.net.URL(SiteDiscoverySoap_address), this);
                _stub.setPortName(getSiteDiscoverySoapWSDDServiceName());
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
        if ("SiteDiscoverySoap12".equals(inputPortName)) {
            return getSiteDiscoverySoap12();
        }
        else if ("SiteDiscoverySoap".equals(inputPortName)) {
            return getSiteDiscoverySoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "SiteDiscovery");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "SiteDiscoverySoap12"));
            ports.add(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "SiteDiscoverySoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("SiteDiscoverySoap12".equals(portName)) {
            setSiteDiscoverySoap12EndpointAddress(address);
        }
        else 
if ("SiteDiscoverySoap".equals(portName)) {
            setSiteDiscoverySoapEndpointAddress(address);
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
