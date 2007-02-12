
    /**
     * SiteDataCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
     */
    package com.google.enterprise.connector.sharepoint.generated;

    /**
     *  SiteDataCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class SiteDataCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public SiteDataCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public SiteDataCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for GetWeb method
            *
            */
           public void receiveResultGetWeb(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetWebResponse param1) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetWeb(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetAttachments method
            *
            */
           public void receiveResultGetAttachments(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetAttachmentsResponse param3) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetAttachments(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for EnumerateFolder method
            *
            */
           public void receiveResultEnumerateFolder(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.EnumerateFolderResponse param5) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorEnumerateFolder(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListCollection method
            *
            */
           public void receiveResultGetListCollection(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetListCollectionResponse param7) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListCollection(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetSiteUrl method
            *
            */
           public void receiveResultGetSiteUrl(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetSiteUrlResponse param9) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetSiteUrl(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetContent method
            *
            */
           public void receiveResultGetContent(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetContentResponse param11) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetContent(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetChanges method
            *
            */
           public void receiveResultGetChanges(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetChangesResponse param13) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetChanges(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetURLSegments method
            *
            */
           public void receiveResultGetURLSegments(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetURLSegmentsResponse param15) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetURLSegments(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetSite method
            *
            */
           public void receiveResultGetSite(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetSiteResponse param17) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetSite(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListItems method
            *
            */
           public void receiveResultGetListItems(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetListItemsResponse param19) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListItems(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetList method
            *
            */
           public void receiveResultGetList(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetListResponse param21) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetSiteAndWeb method
            *
            */
           public void receiveResultGetSiteAndWeb(
                    com.google.enterprise.connector.sharepoint.generated.SiteDataStub.GetSiteAndWebResponse param23) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetSiteAndWeb(java.lang.Exception e) {
            }
                


    }
    