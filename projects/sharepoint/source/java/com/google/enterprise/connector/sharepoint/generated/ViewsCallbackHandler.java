
    /**
     * ViewsCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
     */
    package com.google.enterprise.connector.sharepoint.generated;

    /**
     *  ViewsCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ViewsCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ViewsCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ViewsCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for UpdateView method
            *
            */
           public void receiveResultUpdateView(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.UpdateViewResponse param1) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddView method
            *
            */
           public void receiveResultAddView(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.AddViewResponse param3) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateViewHtml method
            *
            */
           public void receiveResultUpdateViewHtml(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.UpdateViewHtmlResponse param5) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateViewHtml(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetView method
            *
            */
           public void receiveResultGetView(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.GetViewResponse param7) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for DeleteView method
            *
            */
           public void receiveResultDeleteView(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.DeleteViewResponse param9) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetViewHtml method
            *
            */
           public void receiveResultGetViewHtml(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.GetViewHtmlResponse param11) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetViewHtml(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateViewHtml2 method
            *
            */
           public void receiveResultUpdateViewHtml2(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.UpdateViewHtml2Response param13) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateViewHtml2(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetViewCollection method
            *
            */
           public void receiveResultGetViewCollection(
                    com.google.enterprise.connector.sharepoint.generated.ViewsStub.GetViewCollectionResponse param15) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetViewCollection(java.lang.Exception e) {
            }
                


    }
    