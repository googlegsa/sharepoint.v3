
        /**
        * DwsStub.java
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
        */
        package com.google.enterprise.connector.sharepoint.generated;

        

        /*
        *  DwsStub java implementation
        */

        
        public class DwsStub extends org.apache.axis2.client.Stub
        {
        protected org.apache.axis2.description.AxisOperation[] _operations;

        //hashmaps to keep the fault mapping
        private java.util.HashMap faultExeptionNameMap = new java.util.HashMap();
        private java.util.HashMap faultExeptionClassNameMap = new java.util.HashMap();
        private java.util.HashMap faultMessageMap = new java.util.HashMap();

    
    private void populateAxisService() throws org.apache.axis2.AxisFault {

     //creating the Service with a unique name
     _service = new org.apache.axis2.description.AxisService("Dws" + this.hashCode());
     
    

        //creating the operations
        org.apache.axis2.description.AxisOperation __operation;
    


        _operations = new org.apache.axis2.description.AxisOperation[11];
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "DeleteFolder"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[0]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "FindDwsDoc"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[1]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "CreateFolder"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[2]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "RenameDws"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[3]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "RemoveDwsUser"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[4]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "CanCreateDwsUrl"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[5]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "UpdateDwsData"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[6]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "GetDwsData"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[7]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "GetDwsMetaData"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[8]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "DeleteDws"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[9]=__operation;
            
        
                   __operation = new org.apache.axis2.description.OutInAxisOperation();
                

            __operation.setName(new javax.xml.namespace.QName("", "CreateDws"));
	    _service.addOperation(__operation);
	    
	    
	    
            _operations[10]=__operation;
            
        
        }

    //populates the faults
    private void populateFaults(){
         


    }

   /**
    Constructor that takes in a configContext
    */
   public DwsStub(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint)
        throws org.apache.axis2.AxisFault {
         //To populate AxisService
         populateAxisService();
         populateFaults();

        _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);
        
	
        configurationContext = _serviceClient.getServiceContext().getConfigurationContext();

        _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                targetEndpoint));
        
    
    }

    /**
     * Default Constructor
     */
    public DwsStub() throws org.apache.axis2.AxisFault {
        
                    this("http://entpoint05/_vti_bin/DWS.asmx" );
                
    }

    /**
     * Constructor taking the target endpoint
     */
    public DwsStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
        this(null,targetEndpoint);
    }



        
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#DeleteFolder
                        * @param param0
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse DeleteFolder(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolder param0)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/DeleteFolder");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param0,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "DeleteFolder")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#FindDwsDoc
                        * @param param2
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse FindDwsDoc(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDoc param2)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/FindDwsDoc");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param2,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "FindDwsDoc")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#CreateFolder
                        * @param param4
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse CreateFolder(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolder param4)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/CreateFolder");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param4,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "CreateFolder")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#RenameDws
                        * @param param6
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse RenameDws(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDws param6)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/RenameDws");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param6,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "RenameDws")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#RemoveDwsUser
                        * @param param8
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse RemoveDwsUser(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUser param8)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/RemoveDwsUser");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param8,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "RemoveDwsUser")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#CanCreateDwsUrl
                        * @param param10
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse CanCreateDwsUrl(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrl param10)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/CanCreateDwsUrl");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param10,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "CanCreateDwsUrl")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#UpdateDwsData
                        * @param param12
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse UpdateDwsData(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsData param12)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/UpdateDwsData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param12,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "UpdateDwsData")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#GetDwsData
                        * @param param14
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse GetDwsData(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsData param14)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/GetDwsData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param14,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "GetDwsData")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#GetDwsMetaData
                        * @param param16
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse GetDwsMetaData(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaData param16)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[8].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/GetDwsMetaData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param16,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "GetDwsMetaData")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#DeleteDws
                        * @param param18
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse DeleteDws(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDws param18)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[9].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/DeleteDws");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param18,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "DeleteDws")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            
                    /**
                    * Auto generated method signature
                    * @see com.google.enterprise.connector.sharepoint.generated.Dws#CreateDws
                        * @param param20
                    
                    */
                    public com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse CreateDws(

                    com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDws param20)
                    throws java.rmi.RemoteException
                    
                    {
              try{
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[10].getName());
              _operationClient.getOptions().setAction("http://schemas.microsoft.com/sharepoint/soap/dws/CreateDws");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);

              

              // create SOAP envelope with that payload
              org.apache.axiom.soap.SOAPEnvelope env = null;
                    
                                    //Style is Doc.
                                    
                                                    
                                                    env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                    param20,
                                                    optimizeContent(new javax.xml.namespace.QName("",
                                                    "CreateDws")));
                                                
        //adding SOAP headers
         _serviceClient.addHeadersToEnvelope(env);
        // create message context with that soap envelope
        org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext() ;
        _messageContext.setEnvelope(env);

        // add the message contxt to the operation client
        _operationClient.addMessageContext(_messageContext);

        //execute the operation client
        _operationClient.execute(true);

         
               org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                           org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                
                
                           java.lang.Object object = fromOM(
                                        _returnEnv.getBody().getFirstElement() ,
                                        com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse.class,
                                         getEnvelopeNamespaces(_returnEnv));
                           _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                           return (com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse)object;
                    
         }catch(org.apache.axis2.AxisFault f){
            org.apache.axiom.om.OMElement faultElt = f.getDetail();
            if (faultElt!=null){
                if (faultExeptionNameMap.containsKey(faultElt.getQName())){
                    //make the fault by reflection
                    try{
                        java.lang.String exceptionClassName = (java.lang.String)faultExeptionClassNameMap.get(faultElt.getQName());
                        java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                        java.lang.Exception ex=
                                (java.lang.Exception) exceptionClass.newInstance();
                        //message class
                        java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(faultElt.getQName());
                        java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                        java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                        java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                   new java.lang.Class[]{messageClass});
                        m.invoke(ex,new java.lang.Object[]{messageObject});
                        

                        throw new java.rmi.RemoteException(ex.getMessage(), ex);
                    }catch(java.lang.ClassCastException e){
                       // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.ClassNotFoundException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }catch (java.lang.NoSuchMethodException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }  catch (java.lang.IllegalAccessException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }   catch (java.lang.InstantiationException e) {
                        // we cannot intantiate the class - throw the original Axis fault
                        throw f;
                    }
                }else{
                    throw f;
                }
            }else{
                throw f;
            }
        }
        }
            

       /**
        *  A utility method that copies the namepaces from the SOAPEnvelope
        */
       private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
        }
       return returnMap;
    }

    
    
    private javax.xml.namespace.QName[] opNameArray = null;
    private boolean optimizeContent(javax.xml.namespace.QName opName) {
        

        if (opNameArray == null) {
            return false;
        }
        for (int i = 0; i < opNameArray.length; i++) {
            if (opName.equals(opNameArray[i])) {
                return true;   
            }
        }
        return false;
    }
     //http://entpoint05/_vti_bin/DWS.asmx
        public static class FindDwsDocResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "FindDwsDocResponse",
                "ns1");

            

                        /**
                        * field for FindDwsDocResult
                        */

                        protected java.lang.String localFindDwsDocResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localFindDwsDocResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getFindDwsDocResult(){
                               return localFindDwsDocResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param FindDwsDocResult
                               */
                               public void setFindDwsDocResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localFindDwsDocResultTracker = true;
                                       } else {
                                          localFindDwsDocResultTracker = false;
                                              
                                       }
                                   
                                    this.localFindDwsDocResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localFindDwsDocResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"FindDwsDocResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"FindDwsDocResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("FindDwsDocResult");
                                    }
                                

                                          if (localFindDwsDocResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("FindDwsDocResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localFindDwsDocResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localFindDwsDocResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "FindDwsDocResult"));
                            
                                        if (localFindDwsDocResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localFindDwsDocResult));
                                        } else {
                                           throw new RuntimeException("FindDwsDocResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static FindDwsDocResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            FindDwsDocResponse object = new FindDwsDocResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"FindDwsDocResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (FindDwsDocResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","FindDwsDocResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setFindDwsDocResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class UpdateDwsDataResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "UpdateDwsDataResponse",
                "ns1");

            

                        /**
                        * field for UpdateDwsDataResult
                        */

                        protected java.lang.String localUpdateDwsDataResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUpdateDwsDataResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUpdateDwsDataResult(){
                               return localUpdateDwsDataResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param UpdateDwsDataResult
                               */
                               public void setUpdateDwsDataResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localUpdateDwsDataResultTracker = true;
                                       } else {
                                          localUpdateDwsDataResultTracker = false;
                                              
                                       }
                                   
                                    this.localUpdateDwsDataResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localUpdateDwsDataResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"UpdateDwsDataResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"UpdateDwsDataResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("UpdateDwsDataResult");
                                    }
                                

                                          if (localUpdateDwsDataResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("UpdateDwsDataResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUpdateDwsDataResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localUpdateDwsDataResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "UpdateDwsDataResult"));
                            
                                        if (localUpdateDwsDataResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUpdateDwsDataResult));
                                        } else {
                                           throw new RuntimeException("UpdateDwsDataResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static UpdateDwsDataResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            UpdateDwsDataResponse object = new UpdateDwsDataResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"UpdateDwsDataResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (UpdateDwsDataResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","UpdateDwsDataResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUpdateDwsDataResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class RenameDws
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "RenameDws",
                "ns1");

            

                        /**
                        * field for Title
                        */

                        protected java.lang.String localTitle ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTitleTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getTitle(){
                               return localTitle;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Title
                               */
                               public void setTitle(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localTitleTracker = true;
                                       } else {
                                          localTitleTracker = false;
                                              
                                       }
                                   
                                    this.localTitle=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localTitleTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"title", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"title");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("title");
                                    }
                                

                                          if (localTitle==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("title cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localTitle);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localTitleTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "title"));
                            
                                        if (localTitle != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTitle));
                                        } else {
                                           throw new RuntimeException("title cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static RenameDws parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            RenameDws object = new RenameDws();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"RenameDws".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (RenameDws)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","title").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTitle(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class RemoveDwsUserResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "RemoveDwsUserResponse",
                "ns1");

            

                        /**
                        * field for RemoveDwsUserResult
                        */

                        protected java.lang.String localRemoveDwsUserResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRemoveDwsUserResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRemoveDwsUserResult(){
                               return localRemoveDwsUserResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RemoveDwsUserResult
                               */
                               public void setRemoveDwsUserResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRemoveDwsUserResultTracker = true;
                                       } else {
                                          localRemoveDwsUserResultTracker = false;
                                              
                                       }
                                   
                                    this.localRemoveDwsUserResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localRemoveDwsUserResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"RemoveDwsUserResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"RemoveDwsUserResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("RemoveDwsUserResult");
                                    }
                                

                                          if (localRemoveDwsUserResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("RemoveDwsUserResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRemoveDwsUserResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localRemoveDwsUserResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "RemoveDwsUserResult"));
                            
                                        if (localRemoveDwsUserResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRemoveDwsUserResult));
                                        } else {
                                           throw new RuntimeException("RemoveDwsUserResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static RemoveDwsUserResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            RemoveDwsUserResponse object = new RemoveDwsUserResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"RemoveDwsUserResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (RemoveDwsUserResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","RemoveDwsUserResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRemoveDwsUserResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class CanCreateDwsUrlResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "CanCreateDwsUrlResponse",
                "ns1");

            

                        /**
                        * field for CanCreateDwsUrlResult
                        */

                        protected java.lang.String localCanCreateDwsUrlResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCanCreateDwsUrlResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCanCreateDwsUrlResult(){
                               return localCanCreateDwsUrlResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CanCreateDwsUrlResult
                               */
                               public void setCanCreateDwsUrlResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localCanCreateDwsUrlResultTracker = true;
                                       } else {
                                          localCanCreateDwsUrlResultTracker = false;
                                              
                                       }
                                   
                                    this.localCanCreateDwsUrlResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localCanCreateDwsUrlResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"CanCreateDwsUrlResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"CanCreateDwsUrlResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("CanCreateDwsUrlResult");
                                    }
                                

                                          if (localCanCreateDwsUrlResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("CanCreateDwsUrlResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCanCreateDwsUrlResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localCanCreateDwsUrlResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "CanCreateDwsUrlResult"));
                            
                                        if (localCanCreateDwsUrlResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCanCreateDwsUrlResult));
                                        } else {
                                           throw new RuntimeException("CanCreateDwsUrlResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static CanCreateDwsUrlResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            CanCreateDwsUrlResponse object = new CanCreateDwsUrlResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"CanCreateDwsUrlResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (CanCreateDwsUrlResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","CanCreateDwsUrlResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCanCreateDwsUrlResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class RemoveDwsUser
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "RemoveDwsUser",
                "ns1");

            

                        /**
                        * field for Id
                        */

                        protected java.lang.String localId ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getId(){
                               return localId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Id
                               */
                               public void setId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIdTracker = true;
                                       } else {
                                          localIdTracker = false;
                                              
                                       }
                                   
                                    this.localId=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localIdTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"id", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"id");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("id");
                                    }
                                

                                          if (localId==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("id cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localIdTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "id"));
                            
                                        if (localId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localId));
                                        } else {
                                           throw new RuntimeException("id cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static RemoveDwsUser parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            RemoveDwsUser object = new RemoveDwsUser();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"RemoveDwsUser".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (RemoveDwsUser)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","id").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setId(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class CreateDwsResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "CreateDwsResponse",
                "ns1");

            

                        /**
                        * field for CreateDwsResult
                        */

                        protected java.lang.String localCreateDwsResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCreateDwsResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCreateDwsResult(){
                               return localCreateDwsResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreateDwsResult
                               */
                               public void setCreateDwsResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localCreateDwsResultTracker = true;
                                       } else {
                                          localCreateDwsResultTracker = false;
                                              
                                       }
                                   
                                    this.localCreateDwsResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localCreateDwsResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"CreateDwsResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"CreateDwsResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("CreateDwsResult");
                                    }
                                

                                          if (localCreateDwsResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("CreateDwsResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCreateDwsResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localCreateDwsResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "CreateDwsResult"));
                            
                                        if (localCreateDwsResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreateDwsResult));
                                        } else {
                                           throw new RuntimeException("CreateDwsResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static CreateDwsResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            CreateDwsResponse object = new CreateDwsResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"CreateDwsResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (CreateDwsResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","CreateDwsResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCreateDwsResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class GetDwsMetaData
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "GetDwsMetaData",
                "ns1");

            

                        /**
                        * field for Document
                        */

                        protected java.lang.String localDocument ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDocumentTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDocument(){
                               return localDocument;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Document
                               */
                               public void setDocument(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDocumentTracker = true;
                                       } else {
                                          localDocumentTracker = false;
                                              
                                       }
                                   
                                    this.localDocument=param;
                            

                               }
                            

                        /**
                        * field for Id
                        */

                        protected java.lang.String localId ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getId(){
                               return localId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Id
                               */
                               public void setId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIdTracker = true;
                                       } else {
                                          localIdTracker = false;
                                              
                                       }
                                   
                                    this.localId=param;
                            

                               }
                            

                        /**
                        * field for Minimal
                        */

                        protected boolean localMinimal ;
                        

                           /**
                           * Auto generated getter method
                           * @return boolean
                           */
                           public  boolean getMinimal(){
                               return localMinimal;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Minimal
                               */
                               public void setMinimal(boolean param){
                            
                                    this.localMinimal=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localDocumentTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"document", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"document");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("document");
                                    }
                                

                                          if (localDocument==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("document cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDocument);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localIdTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"id", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"id");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("id");
                                    }
                                

                                          if (localId==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("id cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"minimal", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"minimal");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("minimal");
                                    }
                                
                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMinimal));
                                    
                                   xmlWriter.writeEndElement();
                             
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localDocumentTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "document"));
                            
                                        if (localDocument != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDocument));
                                        } else {
                                           throw new RuntimeException("document cannot be null!!");
                                        }
                                    } if (localIdTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "id"));
                            
                                        if (localId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localId));
                                        } else {
                                           throw new RuntimeException("id cannot be null!!");
                                        }
                                    }
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "minimal"));
                            
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMinimal));
                            

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static GetDwsMetaData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetDwsMetaData object = new GetDwsMetaData();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"GetDwsMetaData".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (GetDwsMetaData)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","document").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDocument(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","id").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setId(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","minimal").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMinimal(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                                }
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class DeleteDws
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "DeleteDws",
                "ns1");

            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
               
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static DeleteDws parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            DeleteDws object = new DeleteDws();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"DeleteDws".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (DeleteDws)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class DeleteDwsResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "DeleteDwsResponse",
                "ns1");

            

                        /**
                        * field for DeleteDwsResult
                        */

                        protected java.lang.String localDeleteDwsResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDeleteDwsResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDeleteDwsResult(){
                               return localDeleteDwsResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeleteDwsResult
                               */
                               public void setDeleteDwsResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDeleteDwsResultTracker = true;
                                       } else {
                                          localDeleteDwsResultTracker = false;
                                              
                                       }
                                   
                                    this.localDeleteDwsResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localDeleteDwsResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"DeleteDwsResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"DeleteDwsResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("DeleteDwsResult");
                                    }
                                

                                          if (localDeleteDwsResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("DeleteDwsResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDeleteDwsResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localDeleteDwsResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "DeleteDwsResult"));
                            
                                        if (localDeleteDwsResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDeleteDwsResult));
                                        } else {
                                           throw new RuntimeException("DeleteDwsResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static DeleteDwsResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            DeleteDwsResponse object = new DeleteDwsResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"DeleteDwsResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (DeleteDwsResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","DeleteDwsResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDeleteDwsResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class GetDwsMetaDataResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "GetDwsMetaDataResponse",
                "ns1");

            

                        /**
                        * field for GetDwsMetaDataResult
                        */

                        protected java.lang.String localGetDwsMetaDataResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetDwsMetaDataResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getGetDwsMetaDataResult(){
                               return localGetDwsMetaDataResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetDwsMetaDataResult
                               */
                               public void setGetDwsMetaDataResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localGetDwsMetaDataResultTracker = true;
                                       } else {
                                          localGetDwsMetaDataResultTracker = false;
                                              
                                       }
                                   
                                    this.localGetDwsMetaDataResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localGetDwsMetaDataResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"GetDwsMetaDataResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"GetDwsMetaDataResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("GetDwsMetaDataResult");
                                    }
                                

                                          if (localGetDwsMetaDataResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("GetDwsMetaDataResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localGetDwsMetaDataResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localGetDwsMetaDataResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "GetDwsMetaDataResult"));
                            
                                        if (localGetDwsMetaDataResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localGetDwsMetaDataResult));
                                        } else {
                                           throw new RuntimeException("GetDwsMetaDataResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static GetDwsMetaDataResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetDwsMetaDataResponse object = new GetDwsMetaDataResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"GetDwsMetaDataResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (GetDwsMetaDataResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","GetDwsMetaDataResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setGetDwsMetaDataResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
             throw new java.lang.RuntimeException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    
        public static class CreateDws
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "CreateDws",
                "ns1");

            

                        /**
                        * field for Name
                        */

                        protected java.lang.String localName ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localNameTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getName(){
                               return localName;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Name
                               */
                               public void setName(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localNameTracker = true;
                                       } else {
                                          localNameTracker = false;
                                              
                                       }
                                   
                                    this.localName=param;
                            

                               }
                            

                        /**
                        * field for Users
                        */

                        protected java.lang.String localUsers ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUsersTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUsers(){
                               return localUsers;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Users
                               */
                               public void setUsers(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localUsersTracker = true;
                                       } else {
                                          localUsersTracker = false;
                                              
                                       }
                                   
                                    this.localUsers=param;
                            

                               }
                            

                        /**
                        * field for Title
                        */

                        protected java.lang.String localTitle ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localTitleTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getTitle(){
                               return localTitle;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Title
                               */
                               public void setTitle(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localTitleTracker = true;
                                       } else {
                                          localTitleTracker = false;
                                              
                                       }
                                   
                                    this.localTitle=param;
                            

                               }
                            

                        /**
                        * field for Documents
                        */

                        protected java.lang.String localDocuments ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDocumentsTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDocuments(){
                               return localDocuments;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Documents
                               */
                               public void setDocuments(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDocumentsTracker = true;
                                       } else {
                                          localDocumentsTracker = false;
                                              
                                       }
                                   
                                    this.localDocuments=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localNameTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"name", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"name");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("name");
                                    }
                                

                                          if (localName==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("name cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localName);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localUsersTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"users", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"users");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("users");
                                    }
                                

                                          if (localUsers==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("users cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUsers);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localTitleTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"title", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"title");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("title");
                                    }
                                

                                          if (localTitle==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("title cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localTitle);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localDocumentsTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"documents", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"documents");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("documents");
                                    }
                                

                                          if (localDocuments==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("documents cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDocuments);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localNameTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "name"));
                            
                                        if (localName != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localName));
                                        } else {
                                           throw new RuntimeException("name cannot be null!!");
                                        }
                                    } if (localUsersTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "users"));
                            
                                        if (localUsers != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUsers));
                                        } else {
                                           throw new RuntimeException("users cannot be null!!");
                                        }
                                    } if (localTitleTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "title"));
                            
                                        if (localTitle != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTitle));
                                        } else {
                                           throw new RuntimeException("title cannot be null!!");
                                        }
                                    } if (localDocumentsTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "documents"));
                            
                                        if (localDocuments != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDocuments));
                                        } else {
                                           throw new RuntimeException("documents cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static CreateDws parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            CreateDws object = new CreateDws();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"CreateDws".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (CreateDws)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","name").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setName(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","users").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUsers(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","title").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setTitle(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","documents").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDocuments(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class CreateFolderResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "CreateFolderResponse",
                "ns1");

            

                        /**
                        * field for CreateFolderResult
                        */

                        protected java.lang.String localCreateFolderResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localCreateFolderResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getCreateFolderResult(){
                               return localCreateFolderResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param CreateFolderResult
                               */
                               public void setCreateFolderResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localCreateFolderResultTracker = true;
                                       } else {
                                          localCreateFolderResultTracker = false;
                                              
                                       }
                                   
                                    this.localCreateFolderResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localCreateFolderResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"CreateFolderResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"CreateFolderResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("CreateFolderResult");
                                    }
                                

                                          if (localCreateFolderResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("CreateFolderResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localCreateFolderResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localCreateFolderResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "CreateFolderResult"));
                            
                                        if (localCreateFolderResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localCreateFolderResult));
                                        } else {
                                           throw new RuntimeException("CreateFolderResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static CreateFolderResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            CreateFolderResponse object = new CreateFolderResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"CreateFolderResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (CreateFolderResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","CreateFolderResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setCreateFolderResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class CreateFolder
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "CreateFolder",
                "ns1");

            

                        /**
                        * field for Url
                        */

                        protected java.lang.String localUrl ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUrlTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUrl(){
                               return localUrl;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Url
                               */
                               public void setUrl(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localUrlTracker = true;
                                       } else {
                                          localUrlTracker = false;
                                              
                                       }
                                   
                                    this.localUrl=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localUrlTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"url", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"url");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("url");
                                    }
                                

                                          if (localUrl==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("url cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUrl);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localUrlTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "url"));
                            
                                        if (localUrl != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUrl));
                                        } else {
                                           throw new RuntimeException("url cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static CreateFolder parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            CreateFolder object = new CreateFolder();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"CreateFolder".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (CreateFolder)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","url").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUrl(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class RenameDwsResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "RenameDwsResponse",
                "ns1");

            

                        /**
                        * field for RenameDwsResult
                        */

                        protected java.lang.String localRenameDwsResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localRenameDwsResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getRenameDwsResult(){
                               return localRenameDwsResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param RenameDwsResult
                               */
                               public void setRenameDwsResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localRenameDwsResultTracker = true;
                                       } else {
                                          localRenameDwsResultTracker = false;
                                              
                                       }
                                   
                                    this.localRenameDwsResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localRenameDwsResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"RenameDwsResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"RenameDwsResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("RenameDwsResult");
                                    }
                                

                                          if (localRenameDwsResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("RenameDwsResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localRenameDwsResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localRenameDwsResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "RenameDwsResult"));
                            
                                        if (localRenameDwsResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localRenameDwsResult));
                                        } else {
                                           throw new RuntimeException("RenameDwsResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static RenameDwsResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            RenameDwsResponse object = new RenameDwsResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"RenameDwsResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (RenameDwsResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","RenameDwsResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setRenameDwsResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class DeleteFolderResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "DeleteFolderResponse",
                "ns1");

            

                        /**
                        * field for DeleteFolderResult
                        */

                        protected java.lang.String localDeleteFolderResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDeleteFolderResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDeleteFolderResult(){
                               return localDeleteFolderResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param DeleteFolderResult
                               */
                               public void setDeleteFolderResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDeleteFolderResultTracker = true;
                                       } else {
                                          localDeleteFolderResultTracker = false;
                                              
                                       }
                                   
                                    this.localDeleteFolderResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localDeleteFolderResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"DeleteFolderResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"DeleteFolderResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("DeleteFolderResult");
                                    }
                                

                                          if (localDeleteFolderResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("DeleteFolderResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDeleteFolderResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localDeleteFolderResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "DeleteFolderResult"));
                            
                                        if (localDeleteFolderResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDeleteFolderResult));
                                        } else {
                                           throw new RuntimeException("DeleteFolderResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static DeleteFolderResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            DeleteFolderResponse object = new DeleteFolderResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"DeleteFolderResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (DeleteFolderResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","DeleteFolderResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDeleteFolderResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class UpdateDwsData
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "UpdateDwsData",
                "ns1");

            

                        /**
                        * field for Updates
                        */

                        protected java.lang.String localUpdates ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUpdatesTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUpdates(){
                               return localUpdates;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Updates
                               */
                               public void setUpdates(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localUpdatesTracker = true;
                                       } else {
                                          localUpdatesTracker = false;
                                              
                                       }
                                   
                                    this.localUpdates=param;
                            

                               }
                            

                        /**
                        * field for MeetingInstance
                        */

                        protected java.lang.String localMeetingInstance ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localMeetingInstanceTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getMeetingInstance(){
                               return localMeetingInstance;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param MeetingInstance
                               */
                               public void setMeetingInstance(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localMeetingInstanceTracker = true;
                                       } else {
                                          localMeetingInstanceTracker = false;
                                              
                                       }
                                   
                                    this.localMeetingInstance=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localUpdatesTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"updates", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"updates");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("updates");
                                    }
                                

                                          if (localUpdates==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("updates cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUpdates);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localMeetingInstanceTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"meetingInstance", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"meetingInstance");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("meetingInstance");
                                    }
                                

                                          if (localMeetingInstance==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("meetingInstance cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localMeetingInstance);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localUpdatesTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "updates"));
                            
                                        if (localUpdates != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUpdates));
                                        } else {
                                           throw new RuntimeException("updates cannot be null!!");
                                        }
                                    } if (localMeetingInstanceTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "meetingInstance"));
                            
                                        if (localMeetingInstance != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMeetingInstance));
                                        } else {
                                           throw new RuntimeException("meetingInstance cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static UpdateDwsData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            UpdateDwsData object = new UpdateDwsData();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"UpdateDwsData".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (UpdateDwsData)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","updates").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUpdates(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","meetingInstance").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setMeetingInstance(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class FindDwsDoc
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "FindDwsDoc",
                "ns1");

            

                        /**
                        * field for Id
                        */

                        protected java.lang.String localId ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localIdTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getId(){
                               return localId;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Id
                               */
                               public void setId(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localIdTracker = true;
                                       } else {
                                          localIdTracker = false;
                                              
                                       }
                                   
                                    this.localId=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localIdTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"id", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"id");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("id");
                                    }
                                

                                          if (localId==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("id cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localId);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localIdTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "id"));
                            
                                        if (localId != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localId));
                                        } else {
                                           throw new RuntimeException("id cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static FindDwsDoc parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            FindDwsDoc object = new FindDwsDoc();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"FindDwsDoc".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (FindDwsDoc)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","id").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setId(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class DeleteFolder
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "DeleteFolder",
                "ns1");

            

                        /**
                        * field for Url
                        */

                        protected java.lang.String localUrl ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUrlTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUrl(){
                               return localUrl;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Url
                               */
                               public void setUrl(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localUrlTracker = true;
                                       } else {
                                          localUrlTracker = false;
                                              
                                       }
                                   
                                    this.localUrl=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localUrlTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"url", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"url");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("url");
                                    }
                                

                                          if (localUrl==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("url cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUrl);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localUrlTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "url"));
                            
                                        if (localUrl != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUrl));
                                        } else {
                                           throw new RuntimeException("url cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static DeleteFolder parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            DeleteFolder object = new DeleteFolder();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"DeleteFolder".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (DeleteFolder)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","url").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUrl(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class CanCreateDwsUrl
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "CanCreateDwsUrl",
                "ns1");

            

                        /**
                        * field for Url
                        */

                        protected java.lang.String localUrl ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localUrlTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getUrl(){
                               return localUrl;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Url
                               */
                               public void setUrl(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localUrlTracker = true;
                                       } else {
                                          localUrlTracker = false;
                                              
                                       }
                                   
                                    this.localUrl=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localUrlTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"url", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"url");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("url");
                                    }
                                

                                          if (localUrl==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("url cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localUrl);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localUrlTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "url"));
                            
                                        if (localUrl != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localUrl));
                                        } else {
                                           throw new RuntimeException("url cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static CanCreateDwsUrl parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            CanCreateDwsUrl object = new CanCreateDwsUrl();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"CanCreateDwsUrl".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (CanCreateDwsUrl)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","url").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setUrl(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class GetDwsData
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "GetDwsData",
                "ns1");

            

                        /**
                        * field for Document
                        */

                        protected java.lang.String localDocument ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localDocumentTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getDocument(){
                               return localDocument;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param Document
                               */
                               public void setDocument(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localDocumentTracker = true;
                                       } else {
                                          localDocumentTracker = false;
                                              
                                       }
                                   
                                    this.localDocument=param;
                            

                               }
                            

                        /**
                        * field for LastUpdate
                        */

                        protected java.lang.String localLastUpdate ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localLastUpdateTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getLastUpdate(){
                               return localLastUpdate;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param LastUpdate
                               */
                               public void setLastUpdate(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localLastUpdateTracker = true;
                                       } else {
                                          localLastUpdateTracker = false;
                                              
                                       }
                                   
                                    this.localLastUpdate=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localDocumentTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"document", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"document");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("document");
                                    }
                                

                                          if (localDocument==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("document cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localDocument);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             } if (localLastUpdateTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"lastUpdate", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"lastUpdate");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("lastUpdate");
                                    }
                                

                                          if (localLastUpdate==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("lastUpdate cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localLastUpdate);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localDocumentTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "document"));
                            
                                        if (localDocument != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localDocument));
                                        } else {
                                           throw new RuntimeException("document cannot be null!!");
                                        }
                                    } if (localLastUpdateTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "lastUpdate"));
                            
                                        if (localLastUpdate != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localLastUpdate));
                                        } else {
                                           throw new RuntimeException("lastUpdate cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static GetDwsData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetDwsData object = new GetDwsData();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"GetDwsData".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (GetDwsData)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","document").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setDocument(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                            
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","lastUpdate").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setLastUpdate(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
        public static class GetDwsDataResponse
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://schemas.microsoft.com/sharepoint/soap/dws/",
                "GetDwsDataResponse",
                "ns1");

            

                        /**
                        * field for GetDwsDataResult
                        */

                        protected java.lang.String localGetDwsDataResult ;
                        
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean localGetDwsDataResultTracker = false ;
                           

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getGetDwsDataResult(){
                               return localGetDwsDataResult;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param GetDwsDataResult
                               */
                               public void setGetDwsDataResult(java.lang.String param){
                            
                                       if (param != null){
                                          //update the setting tracker
                                          localGetDwsDataResultTracker = true;
                                       } else {
                                          localGetDwsDataResultTracker = false;
                                              
                                       }
                                   
                                    this.localGetDwsDataResult=param;
                            

                               }
                            

     /**
     * isReaderMTOMAware
     * @return true if the reader supports MTOM
     */
   public static boolean isReaderMTOMAware(javax.xml.stream.XMLStreamReader reader) {
        boolean isReaderMTOMAware = false;
        
        try{
          isReaderMTOMAware = java.lang.Boolean.TRUE.equals(reader.getProperty(org.apache.axiom.om.OMConstants.IS_DATA_HANDLERS_AWARE));
        }catch(java.lang.IllegalArgumentException e){
          isReaderMTOMAware = false;
        }
        return isReaderMTOMAware;
   }
     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement(
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory){

        org.apache.axiom.om.OMDataSource dataSource = getOMDataSource(parentQName, factory);

        
               return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
               MY_QNAME,factory,dataSource);
            
       }

     /**
     *
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMDataSource getOMDataSource(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory){


        org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName){

         public void serialize(
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            
                

                java.lang.String prefix = parentQName.getPrefix();
                java.lang.String namespace = parentQName.getNamespaceURI();

                if (namespace != null) {
                    java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
                    if (writerPrefix != null) {
                        xmlWriter.writeStartElement(namespace, parentQName.getLocalPart());
                    } else {
                        if (prefix == null) {
                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                        }

                        xmlWriter.writeStartElement(prefix, parentQName.getLocalPart(), namespace);
                        xmlWriter.writeNamespace(prefix, namespace);
                        xmlWriter.setPrefix(prefix, namespace);
                    }
                } else {
                    xmlWriter.writeStartElement(parentQName.getLocalPart());
                }

                
                if (localGetDwsDataResultTracker){
                                    namespace = "http://schemas.microsoft.com/sharepoint/soap/dws/";
                                    if (! namespace.equals("")) {
                                        prefix = xmlWriter.getPrefix(namespace);

                                        if (prefix == null) {
                                            prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();

                                            xmlWriter.writeStartElement(prefix,"GetDwsDataResult", namespace);
                                            xmlWriter.writeNamespace(prefix, namespace);
                                            xmlWriter.setPrefix(prefix, namespace);

                                        } else {
                                            xmlWriter.writeStartElement(namespace,"GetDwsDataResult");
                                        }

                                    } else {
                                        xmlWriter.writeStartElement("GetDwsDataResult");
                                    }
                                

                                          if (localGetDwsDataResult==null){
                                              // write the nil attribute
                                              
                                                     throw new RuntimeException("GetDwsDataResult cannot be null!!");
                                                  
                                          }else{

                                        
                                                   xmlWriter.writeCharacters(localGetDwsDataResult);
                                            
                                          }
                                    
                                   xmlWriter.writeEndElement();
                             }
                   
               xmlWriter.writeEndElement();
            
            

        }

         /**
          * Util method to write an attribute with the ns prefix
          */
          private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
              if (xmlWriter.getPrefix(namespace) == null) {
                       xmlWriter.writeNamespace(prefix, namespace);
                       xmlWriter.setPrefix(prefix, namespace);

              }

              xmlWriter.writeAttribute(namespace,attName,attValue);

         }

         /**
          * Util method to write an attribute without the ns prefix
          */
          private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                      java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
    	  	  if (namespace.equals(""))
        	  {
        		  xmlWriter.writeAttribute(attName,attValue);
        	  }
        	  else
        	  {
                  registerPrefix(xmlWriter, namespace);
                  xmlWriter.writeAttribute(namespace,attName,attValue);
              }
          }

         /**
         * Register a namespace prefix
         */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
                java.lang.String prefix = xmlWriter.getPrefix(namespace);

                if (prefix == null) {
                    prefix = createPrefix();

                    while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                        prefix = createPrefix();
                    }

                    xmlWriter.writeNamespace(prefix, namespace);
                    xmlWriter.setPrefix(prefix, namespace);
                }

                return prefix;
            }

         /**
          * Create a prefix
          */
          private java.lang.String createPrefix() {
                return "ns" + (int)Math.random();
          }
        };

        return dataSource;
    }

  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName){


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                 if (localGetDwsDataResultTracker){
                             elementList.add(new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/",
                                                                      "GetDwsDataResult"));
                            
                                        if (localGetDwsDataResult != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localGetDwsDataResult));
                                        } else {
                                           throw new RuntimeException("GetDwsDataResult cannot be null!!");
                                        }
                                    }

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{


        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static GetDwsDataResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            GetDwsDataResponse object = new GetDwsDataResponse();
            int event;
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    if (!"GetDwsDataResponse".equals(type)){
                        //find namespace for the prefix
                        java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                        return (GetDwsDataResponse)ExtensionMapper.getTypeObject(
                             nsUri,type,reader);
                      }

                  }

                }
                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                
                    
                    reader.next();
                
                                    
                                    while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                
                                    if (reader.isStartElement() && new javax.xml.namespace.QName("http://schemas.microsoft.com/sharepoint/soap/dws/","GetDwsDataResult").equals(reader.getName())){
                                
                                    java.lang.String content = reader.getElementText();
                                    
                                              object.setGetDwsDataResult(
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                              
                                        reader.next();
                                    
                              }  // End of if for expected property start element
                              
                            while (!reader.isStartElement() && !reader.isEndElement())
                                reader.next();
                            if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new java.lang.RuntimeException("Unexpected subelement " + reader.getLocalName());
                        


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
          
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolder param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolder.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDoc param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDoc.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolder param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolder.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDws param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDws.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUser param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUser.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrl param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrl.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsData param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsData.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsData param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsData.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaData param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaData.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDws param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDws.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDws param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDws.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        
            private  org.apache.axiom.om.OMElement  toOM(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse param, boolean optimizeContent){
            
                     return param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse.MY_QNAME,
                                  org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                    

            }
        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolder param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolder.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDoc param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDoc.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolder param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolder.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDws param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDws.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUser param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUser.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrl param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrl.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsData param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsData.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsData param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsData.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaData param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaData.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDws param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDws.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        

                            
                        private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDws param, boolean optimizeContent){
                        org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                             
                                    emptyEnvelope.getBody().addChild(param.getOMElement(com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDws.MY_QNAME,factory));
                                
                         return emptyEnvelope;
                        }

                        


        /**
        *  get the default envelope
        */
        private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
        return factory.getDefaultEnvelope();
        }


        private  java.lang.Object fromOM(
        org.apache.axiom.om.OMElement param,
        java.lang.Class type,
        java.util.Map extraNamespaces){

        try {
        
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolder.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolder.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDoc.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDoc.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolder.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolder.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDws.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDws.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUser.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUser.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrl.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrl.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsData.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsData.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaData.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDws.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDws.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDws.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDws.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
                if (com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse.class.equals(type)){
                
                           return com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                    

                }
           
        } catch (Exception e) {
        throw new RuntimeException(e);
        }
           return null;
        }



    
            private void setOpNameArray(){
            opNameArray = null;
            }
           
   }
   