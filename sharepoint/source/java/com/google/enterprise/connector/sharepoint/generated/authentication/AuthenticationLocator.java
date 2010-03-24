/**
 * AuthenticationLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.authentication;

public class AuthenticationLocator extends org.apache.axis.client.Service implements com.google.enterprise.connector.sharepoint.generated.authentication.Authentication {

    public AuthenticationLocator() {
    }


    public AuthenticationLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AuthenticationLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AuthenticationSoap
    private java.lang.String AuthenticationSoap_address = "http://gdc04.gdc-psl.net:6666/_vti_bin/Authentication.asmx";

    public java.lang.String getAuthenticationSoapAddress() {
        return AuthenticationSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AuthenticationSoapWSDDServiceName = "AuthenticationSoap";

    public java.lang.String getAuthenticationSoapWSDDServiceName() {
        return AuthenticationSoapWSDDServiceName;
    }

    public void setAuthenticationSoapWSDDServiceName(java.lang.String name) {
        AuthenticationSoapWSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AuthenticationSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAuthenticationSoap(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_BindingStub(portAddress, this);
            _stub.setPortName(getAuthenticationSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAuthenticationSoapEndpointAddress(java.lang.String address) {
        AuthenticationSoap_address = address;
    }


    // Use to get a proxy class for AuthenticationSoap12
    private java.lang.String AuthenticationSoap12_address = "http://gdc04.gdc-psl.net:6666/_vti_bin/Authentication.asmx";

    public java.lang.String getAuthenticationSoap12Address() {
        return AuthenticationSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AuthenticationSoap12WSDDServiceName = "AuthenticationSoap12";

    public java.lang.String getAuthenticationSoap12WSDDServiceName() {
        return AuthenticationSoap12WSDDServiceName;
    }

    public void setAuthenticationSoap12WSDDServiceName(java.lang.String name) {
        AuthenticationSoap12WSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AuthenticationSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAuthenticationSoap12(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType getAuthenticationSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap12Stub(portAddress, this);
            _stub.setPortName(getAuthenticationSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAuthenticationSoap12EndpointAddress(java.lang.String address) {
        AuthenticationSoap12_address = address;
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
            if (com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_BindingStub(new java.net.URL(AuthenticationSoap_address), this);
                _stub.setPortName(getAuthenticationSoapWSDDServiceName());
                return _stub;
            }
            if (com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.authentication.AuthenticationSoap12Stub(new java.net.URL(AuthenticationSoap12_address), this);
                _stub.setPortName(getAuthenticationSoap12WSDDServiceName());
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
        if ("AuthenticationSoap".equals(inputPortName)) {
            return getAuthenticationSoap();
        }
        else if ("AuthenticationSoap12".equals(inputPortName)) {
            return getAuthenticationSoap12();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "Authentication");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AuthenticationSoap"));
            ports.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/", "AuthenticationSoap12"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("AuthenticationSoap".equals(portName)) {
            setAuthenticationSoapEndpointAddress(address);
        }
        else
if ("AuthenticationSoap12".equals(portName)) {
            setAuthenticationSoap12EndpointAddress(address);
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
