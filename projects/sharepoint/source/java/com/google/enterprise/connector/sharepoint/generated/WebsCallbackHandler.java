
    /**
     * WebsCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
     */
    package com.google.enterprise.connector.sharepoint.generated;

    /**
     *  WebsCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class WebsCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public WebsCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public WebsCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for GetCustomizedPageStatus method
            *
            */
           public void receiveResultGetCustomizedPageStatus(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetCustomizedPageStatusResponse param1) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetCustomizedPageStatus(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetWeb method
            *
            */
           public void receiveResultGetWeb(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetWebResponse param3) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetWeb(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetWebCollection method
            *
            */
           public void receiveResultGetWebCollection(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetWebCollectionResponse param5) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetWebCollection(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RevertFileContentStream method
            *
            */
           public void receiveResultRevertFileContentStream(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.RevertFileContentStreamResponse param7) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRevertFileContentStream(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetContentTypes method
            *
            */
           public void receiveResultGetContentTypes(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetContentTypesResponse param9) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetContentTypes(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateContentTypeXmlDocument method
            *
            */
           public void receiveResultUpdateContentTypeXmlDocument(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.UpdateContentTypeXmlDocumentResponse param11) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateContentTypeXmlDocument(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetColumns method
            *
            */
           public void receiveResultGetColumns(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetColumnsResponse param13) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetColumns(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CustomizeCss method
            *
            */
           public void receiveResultCustomizeCss(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.CustomizeCssResponse param15) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCustomizeCss(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RevertCss method
            *
            */
           public void receiveResultRevertCss(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.RevertCssResponse param17) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRevertCss(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RevertAllFileContentStreams method
            *
            */
           public void receiveResultRevertAllFileContentStreams(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.RevertAllFileContentStreamsResponse param19) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRevertAllFileContentStreams(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetContentType method
            *
            */
           public void receiveResultGetContentType(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetContentTypeResponse param21) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetContentType(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveContentTypeXmlDocument method
            *
            */
           public void receiveResultRemoveContentTypeXmlDocument(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.RemoveContentTypeXmlDocumentResponse param23) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveContentTypeXmlDocument(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateContentType method
            *
            */
           public void receiveResultUpdateContentType(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.UpdateContentTypeResponse param25) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateContentType(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateColumns method
            *
            */
           public void receiveResultUpdateColumns(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.UpdateColumnsResponse param27) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateColumns(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for WebUrlFromPageUrl method
            *
            */
           public void receiveResultWebUrlFromPageUrl(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.WebUrlFromPageUrlResponse param29) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorWebUrlFromPageUrl(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetAllSubWebCollection method
            *
            */
           public void receiveResultGetAllSubWebCollection(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetAllSubWebCollectionResponse param31) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetAllSubWebCollection(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for DeleteContentType method
            *
            */
           public void receiveResultDeleteContentType(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.DeleteContentTypeResponse param33) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteContentType(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetActivatedFeatures method
            *
            */
           public void receiveResultGetActivatedFeatures(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetActivatedFeaturesResponse param35) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetActivatedFeatures(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListTemplates method
            *
            */
           public void receiveResultGetListTemplates(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.GetListTemplatesResponse param37) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListTemplates(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CreateContentType method
            *
            */
           public void receiveResultCreateContentType(
                    com.google.enterprise.connector.sharepoint.generated.WebsStub.CreateContentTypeResponse param39) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCreateContentType(java.lang.Exception e) {
            }
                


    }
    