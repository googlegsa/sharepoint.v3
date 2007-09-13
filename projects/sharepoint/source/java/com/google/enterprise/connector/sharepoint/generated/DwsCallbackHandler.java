
    /**
     * DwsCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
     */
    package com.google.enterprise.connector.sharepoint.generated;

    /**
     *  DwsCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class DwsCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public DwsCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public DwsCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for DeleteFolder method
            *
            */
           public void receiveResultDeleteFolder(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteFolderResponse param1) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteFolder(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for FindDwsDoc method
            *
            */
           public void receiveResultFindDwsDoc(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.FindDwsDocResponse param3) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorFindDwsDoc(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CreateFolder method
            *
            */
           public void receiveResultCreateFolder(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateFolderResponse param5) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCreateFolder(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RenameDws method
            *
            */
           public void receiveResultRenameDws(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.RenameDwsResponse param7) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRenameDws(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveDwsUser method
            *
            */
           public void receiveResultRemoveDwsUser(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.RemoveDwsUserResponse param9) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveDwsUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CanCreateDwsUrl method
            *
            */
           public void receiveResultCanCreateDwsUrl(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.CanCreateDwsUrlResponse param11) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCanCreateDwsUrl(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateDwsData method
            *
            */
           public void receiveResultUpdateDwsData(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.UpdateDwsDataResponse param13) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateDwsData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetDwsData method
            *
            */
           public void receiveResultGetDwsData(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsDataResponse param15) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetDwsData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetDwsMetaData method
            *
            */
           public void receiveResultGetDwsMetaData(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.GetDwsMetaDataResponse param17) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetDwsMetaData(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for DeleteDws method
            *
            */
           public void receiveResultDeleteDws(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.DeleteDwsResponse param19) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteDws(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CreateDws method
            *
            */
           public void receiveResultCreateDws(
                    com.google.enterprise.connector.sharepoint.generated.DwsStub.CreateDwsResponse param21) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCreateDws(java.lang.Exception e) {
            }
                


    }
    