
    /**
     * UserProfileServiceCallbackHandler.java
     *
     * This file was auto-generated from WSDL
     * by the Apache Axis2 version: 1.1.1 Jan 09, 2007 (06:20:51 LKT)
     */
    package com.google.enterprise.connector.sharepoint.generated;

    /**
     *  UserProfileServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserProfileServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserProfileServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserProfileServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for GetUserProfileCount method
            *
            */
           public void receiveResultGetUserProfileCount(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserProfileCountResponse param1) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserProfileCount(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdatePinnedLink method
            *
            */
           public void receiveResultUpdatePinnedLink(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.UpdatePinnedLinkResponse param3) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdatePinnedLink(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserLinks method
            *
            */
           public void receiveResultGetUserLinks(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserLinksResponse param5) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserLinks(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CreateUserProfileByAccountName method
            *
            */
           public void receiveResultCreateUserProfileByAccountName(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.CreateUserProfileByAccountNameResponse param7) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCreateUserProfileByAccountName(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddMembership method
            *
            */
           public void receiveResultAddMembership(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.AddMembershipResponse param9) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddMembership(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserColleagues method
            *
            */
           public void receiveResultGetUserColleagues(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserColleaguesResponse param11) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserColleagues(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetPropertyChoiceList method
            *
            */
           public void receiveResultGetPropertyChoiceList(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetPropertyChoiceListResponse param13) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetPropertyChoiceList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddLink method
            *
            */
           public void receiveResultAddLink(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.AddLinkResponse param15) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddLink(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetCommonMemberships method
            *
            */
           public void receiveResultGetCommonMemberships(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetCommonMembershipsResponse param17) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetCommonMemberships(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveColleague method
            *
            */
           public void receiveResultRemoveColleague(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemoveColleagueResponse param19) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveColleague(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserMemberships method
            *
            */
           public void receiveResultGetUserMemberships(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserMembershipsResponse param21) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserMemberships(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemovePinnedLink method
            *
            */
           public void receiveResultRemovePinnedLink(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemovePinnedLinkResponse param23) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemovePinnedLink(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetInCommon method
            *
            */
           public void receiveResultGetInCommon(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetInCommonResponse param25) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetInCommon(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveMembership method
            *
            */
           public void receiveResultRemoveMembership(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemoveMembershipResponse param27) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveMembership(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveAllPinnedLinks method
            *
            */
           public void receiveResultRemoveAllPinnedLinks(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemoveAllPinnedLinksResponse param29) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveAllPinnedLinks(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetCommonColleagues method
            *
            */
           public void receiveResultGetCommonColleagues(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetCommonColleaguesResponse param31) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetCommonColleagues(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveAllMemberships method
            *
            */
           public void receiveResultRemoveAllMemberships(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemoveAllMembershipsResponse param33) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveAllMemberships(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateMembershipPrivacy method
            *
            */
           public void receiveResultUpdateMembershipPrivacy(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.UpdateMembershipPrivacyResponse param35) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateMembershipPrivacy(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserProfileByGuid method
            *
            */
           public void receiveResultGetUserProfileByGuid(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserProfileByGuidResponse param37) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserProfileByGuid(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateColleaguePrivacy method
            *
            */
           public void receiveResultUpdateColleaguePrivacy(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.UpdateColleaguePrivacyResponse param39) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateColleaguePrivacy(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for ModifyUserPropertyByAccountName method
            *
            */
           public void receiveResultModifyUserPropertyByAccountName(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.ModifyUserPropertyByAccountNameResponse param41) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorModifyUserPropertyByAccountName(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveAllLinks method
            *
            */
           public void receiveResultRemoveAllLinks(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemoveAllLinksResponse param43) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveAllLinks(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserProfileSchema method
            *
            */
           public void receiveResultGetUserProfileSchema(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserProfileSchemaResponse param45) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserProfileSchema(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for CreateMemberGroup method
            *
            */
           public void receiveResultCreateMemberGroup(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.CreateMemberGroupResponse param47) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorCreateMemberGroup(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserPinnedLinks method
            *
            */
           public void receiveResultGetUserPinnedLinks(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserPinnedLinksResponse param49) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserPinnedLinks(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveLink method
            *
            */
           public void receiveResultRemoveLink(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemoveLinkResponse param51) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveLink(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddPinnedLink method
            *
            */
           public void receiveResultAddPinnedLink(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.AddPinnedLinkResponse param53) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddPinnedLink(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for UpdateLink method
            *
            */
           public void receiveResultUpdateLink(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.UpdateLinkResponse param55) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorUpdateLink(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for RemoveAllColleagues method
            *
            */
           public void receiveResultRemoveAllColleagues(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.RemoveAllColleaguesResponse param57) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorRemoveAllColleagues(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetCommonManager method
            *
            */
           public void receiveResultGetCommonManager(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetCommonManagerResponse param59) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetCommonManager(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserProfileByName method
            *
            */
           public void receiveResultGetUserProfileByName(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserProfileByNameResponse param61) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserProfileByName(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for GetUserProfileByIndex method
            *
            */
           public void receiveResultGetUserProfileByIndex(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.GetUserProfileByIndexResponse param63) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorGetUserProfileByIndex(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for AddColleague method
            *
            */
           public void receiveResultAddColleague(
                    com.google.enterprise.connector.sharepoint.generated.UserProfileServiceStub.AddColleagueResponse param65) {
           }

          /**
           * auto generated Axis2 Error handler
           *
           */
            public void receiveErrorAddColleague(java.lang.Exception e) {
            }
                


    }
    