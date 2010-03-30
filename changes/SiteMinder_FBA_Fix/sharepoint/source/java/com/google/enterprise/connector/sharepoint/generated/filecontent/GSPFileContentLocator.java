/**
 * GSPFileContentLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.filecontent;

public class GSPFileContentLocator extends org.apache.axis.client.Service implements com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContent {

    public GSPFileContentLocator() {
    }


    public GSPFileContentLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GSPFileContentLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GSPFileContentSoap12
    private java.lang.String GSPFileContentSoap12_address = "http://localhost:4806/GSPFileContents/GSPFileContent.asmx";

    public java.lang.String getGSPFileContentSoap12Address() {
        return GSPFileContentSoap12_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GSPFileContentSoap12WSDDServiceName = "GSPFileContentSoap12";

    public java.lang.String getGSPFileContentSoap12WSDDServiceName() {
        return GSPFileContentSoap12WSDDServiceName;
    }

    public void setGSPFileContentSoap12WSDDServiceName(java.lang.String name) {
        GSPFileContentSoap12WSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_PortType getGSPFileContentSoap12() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GSPFileContentSoap12_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGSPFileContentSoap12(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_PortType getGSPFileContentSoap12(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap12Stub(portAddress, this);
            _stub.setPortName(getGSPFileContentSoap12WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGSPFileContentSoap12EndpointAddress(java.lang.String address) {
        GSPFileContentSoap12_address = address;
    }


    // Use to get a proxy class for GSPFileContentSoap
    private java.lang.String GSPFileContentSoap_address = "http://localhost:4806/GSPFileContents/GSPFileContent.asmx";

    public java.lang.String getGSPFileContentSoapAddress() {
        return GSPFileContentSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GSPFileContentSoapWSDDServiceName = "GSPFileContentSoap";

    public java.lang.String getGSPFileContentSoapWSDDServiceName() {
        return GSPFileContentSoapWSDDServiceName;
    }

    public void setGSPFileContentSoapWSDDServiceName(java.lang.String name) {
        GSPFileContentSoapWSDDServiceName = name;
    }

    public com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_PortType getGSPFileContentSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GSPFileContentSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGSPFileContentSoap(endpoint);
    }

    public com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_PortType getGSPFileContentSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_BindingStub(portAddress, this);
            _stub.setPortName(getGSPFileContentSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGSPFileContentSoapEndpointAddress(java.lang.String address) {
        GSPFileContentSoap_address = address;
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
            if (com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap12Stub _stub = new com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap12Stub(new java.net.URL(GSPFileContentSoap12_address), this);
                _stub.setPortName(getGSPFileContentSoap12WSDDServiceName());
                return _stub;
            }
            if (com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_BindingStub _stub = new com.google.enterprise.connector.sharepoint.generated.filecontent.GSPFileContentSoap_BindingStub(new java.net.URL(GSPFileContentSoap_address), this);
                _stub.setPortName(getGSPFileContentSoapWSDDServiceName());
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
        if ("GSPFileContentSoap12".equals(inputPortName)) {
            return getGSPFileContentSoap12();
        }
        else if ("GSPFileContentSoap".equals(inputPortName)) {
            return getGSPFileContentSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "GSPFileContent");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "GSPFileContentSoap12"));
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "GSPFileContentSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("GSPFileContentSoap12".equals(portName)) {
            setGSPFileContentSoap12EndpointAddress(address);
        }
        else
if ("GSPFileContentSoap".equals(portName)) {
            setGSPFileContentSoapEndpointAddress(address);
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
