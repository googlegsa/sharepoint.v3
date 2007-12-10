
    /**
     * ListsCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
     */
    package com.google.enterprise.connector.sharepoint.generated;

    /**
     *  ListsCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class ListsCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public ListsCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public ListsCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for GetListCollection method
            *
            */
           public void receiveResultGetListCollection(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListCollectionResponse param1) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListCollection(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for DeleteContentTypeXmlDocument method
            *
            */
           public void receiveResultDeleteContentTypeXmlDocument(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.DeleteContentTypeXmlDocumentResponse param3) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteContentTypeXmlDocument(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListItems method
            *
            */
           public void receiveResultGetListItems(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItemsResponse param5) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListItems(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateListItems method
            *
            */
           public void receiveResultUpdateListItems(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.UpdateListItemsResponse param7) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateListItems(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListItemChangesSinceToken method
            *
            */
           public void receiveResultGetListItemChangesSinceToken(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItemChangesSinceTokenResponse param9) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListItemChangesSinceToken(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddList method
            *
            */
           public void receiveResultAddList(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.AddListResponse param11) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListContentTypes method
            *
            */
           public void receiveResultGetListContentTypes(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListContentTypesResponse param13) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListContentTypes(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddListFromFeature method
            *
            */
           public void receiveResultAddListFromFeature(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.AddListFromFeatureResponse param15) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddListFromFeature(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetAttachmentCollection method
            *
            */
           public void receiveResultGetAttachmentCollection(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetAttachmentCollectionResponse param17) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetAttachmentCollection(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for DeleteContentType method
            *
            */
           public void receiveResultDeleteContentType(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.DeleteContentTypeResponse param19) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteContentType(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListContentType method
            *
            */
           public void receiveResultGetListContentType(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListContentTypeResponse param21) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListContentType(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddDiscussionBoardItem method
            *
            */
           public void receiveResultAddDiscussionBoardItem(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.AddDiscussionBoardItemResponse param23) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddDiscussionBoardItem(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateContentTypesXmlDocument method
            *
            */
           public void receiveResultUpdateContentTypesXmlDocument(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.UpdateContentTypesXmlDocumentResponse param25) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateContentTypesXmlDocument(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for DeleteAttachment method
            *
            */
           public void receiveResultDeleteAttachment(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.DeleteAttachmentResponse param27) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UndoCheckOut method
            *
            */
           public void receiveResultUndoCheckOut(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.UndoCheckOutResponse param29) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUndoCheckOut(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for DeleteList method
            *
            */
           public void receiveResultDeleteList(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.DeleteListResponse param31) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorDeleteList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateContentTypeXmlDocument method
            *
            */
           public void receiveResultUpdateContentTypeXmlDocument(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.UpdateContentTypeXmlDocumentResponse param33) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateContentTypeXmlDocument(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for ApplyContentTypeToList method
            *
            */
           public void receiveResultApplyContentTypeToList(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.ApplyContentTypeToListResponse param35) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorApplyContentTypeToList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddAttachment method
            *
            */
           public void receiveResultAddAttachment(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.AddAttachmentResponse param37) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddAttachment(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateList method
            *
            */
           public void receiveResultUpdateList(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.UpdateListResponse param39) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListItemChanges method
            *
            */
           public void receiveResultGetListItemChanges(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListItemChangesResponse param41) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListItemChanges(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CheckOutFile method
            *
            */
           public void receiveResultCheckOutFile(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.CheckOutFileResponse param43) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCheckOutFile(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateContentType method
            *
            */
           public void receiveResultUpdateContentType(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.UpdateContentTypeResponse param45) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateContentType(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetVersionCollection method
            *
            */
           public void receiveResultGetVersionCollection(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetVersionCollectionResponse param47) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetVersionCollection(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetListAndView method
            *
            */
           public void receiveResultGetListAndView(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListAndViewResponse param49) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetListAndView(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CheckInFile method
            *
            */
           public void receiveResultCheckInFile(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.CheckInFileResponse param51) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCheckInFile(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CreateContentType method
            *
            */
           public void receiveResultCreateContentType(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.CreateContentTypeResponse param53) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCreateContentType(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetList method
            *
            */
           public void receiveResultGetList(
                    com.google.enterprise.connector.sharepoint.generated.ListsStub.GetListResponse param55) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetList(java.lang.Exception e) {
            }
                


    }
    