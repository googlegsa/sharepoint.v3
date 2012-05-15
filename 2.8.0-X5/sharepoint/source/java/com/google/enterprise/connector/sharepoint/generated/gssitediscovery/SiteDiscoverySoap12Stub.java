/**
 * SiteDiscoverySoap12Stub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.gssitediscovery;

public class SiteDiscoverySoap12Stub extends org.apache.axis.client.Stub implements com.google.enterprise.connector.sharepoint.generated.gssitediscovery.SiteDiscoverySoap_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[6];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CheckConnectivity");
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        oper.setReturnClass(java.lang.String.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "CheckConnectivityResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetAllSiteCollectionFromAllWebApps");
        oper.setReturnType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfAnyType"));
        oper.setReturnClass(java.lang.Object[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetAllSiteCollectionFromAllWebAppsResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "anyType"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetWebCrawlInfo");
        oper.setReturnType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetWebCrawlInfoResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetWebCrawlInfoInBatch");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "webUrls"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfString"), java.lang.String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfWebCrawlInfo"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetWebCrawlInfoInBatchResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetListCrawlInfo");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "listGuids"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfString"), java.lang.String[].class, false, false);
        param.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "string"));
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfListCrawlInfo"));
        oper.setReturnClass(com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[].class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetListCrawlInfoResult"));
        param = oper.getReturnParamDesc();
        param.setItemQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ListCrawlInfo"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("IsCrawlableList");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "listGUID"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"), java.lang.String.class, false, false);
        param.setOmittable(true);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        oper.setReturnClass(boolean.class);
        oper.setReturnQName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "IsCrawlableListResult"));
        oper.setStyle(org.apache.axis.constants.Style.WRAPPED);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

    }

    public SiteDiscoverySoap12Stub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public SiteDiscoverySoap12Stub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public SiteDiscoverySoap12Stub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">CheckConnectivity");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.CheckConnectivity.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">CheckConnectivityResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.CheckConnectivityResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetAllSiteCollectionFromAllWebApps");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetAllSiteCollectionFromAllWebApps.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetAllSiteCollectionFromAllWebAppsResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetAllSiteCollectionFromAllWebAppsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetListCrawlInfo");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetListCrawlInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetListCrawlInfoResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetListCrawlInfoResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetWebCrawlInfo");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetWebCrawlInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetWebCrawlInfoInBatch");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetWebCrawlInfoInBatch.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetWebCrawlInfoInBatchResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetWebCrawlInfoInBatchResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">GetWebCrawlInfoResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.GetWebCrawlInfoResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">IsCrawlableList");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.IsCrawlableList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", ">IsCrawlableListResponse");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.IsCrawlableListResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfAnyType");
            cachedSerQNames.add(qName);
            cls = java.lang.Object[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "anyType");
            qName2 = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "anyType");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfListCrawlInfo");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ListCrawlInfo");
            qName2 = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ListCrawlInfo");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfString");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "string");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ArrayOfWebCrawlInfo");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo");
            qName2 = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "ListCrawlInfo");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "WebCrawlInfo");
            cachedSerQNames.add(qName);
            cls = com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public java.lang.String checkConnectivity() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssitediscovery.generated.sharepoint.connector.enterprise.google.com/CheckConnectivity");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "CheckConnectivity"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.String) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.String) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.String.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public java.lang.Object[] getAllSiteCollectionFromAllWebApps() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetAllSiteCollectionFromAllWebApps");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetAllSiteCollectionFromAllWebApps"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (java.lang.Object[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (java.lang.Object[]) org.apache.axis.utils.JavaUtils.convert(_resp, java.lang.Object[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo getWebCrawlInfo() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetWebCrawlInfo");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetWebCrawlInfo"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[] getWebCrawlInfoInBatch(java.lang.String[] webUrls) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetWebCrawlInfoInBatch");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetWebCrawlInfoInBatch"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {webUrls});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.gssitediscovery.WebCrawlInfo[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[] getListCrawlInfo(java.lang.String[] listGuids) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssitediscovery.generated.sharepoint.connector.enterprise.google.com/GetListCrawlInfo");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "GetListCrawlInfo"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listGuids});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[]) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[]) org.apache.axis.utils.JavaUtils.convert(_resp, com.google.enterprise.connector.sharepoint.generated.gssitediscovery.ListCrawlInfo[].class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public boolean isCrawlableList(java.lang.String listGUID) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("gssitediscovery.generated.sharepoint.connector.enterprise.google.com/IsCrawlableList");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP12_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("gssitediscovery.generated.sharepoint.connector.enterprise.google.com", "IsCrawlableList"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {listGUID});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return ((java.lang.Boolean) _resp).booleanValue();
            } catch (java.lang.Exception _exception) {
                return ((java.lang.Boolean) org.apache.axis.utils.JavaUtils.convert(_resp, boolean.class)).booleanValue();
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
