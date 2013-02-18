// Copyright 2012 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.enterprise.connector.sharepoint.social;

public class SharepointSocialConstants {
  /**
   * SharePoint service URL for userprofile service.
   */
  public static final String SHAREPOINT_USERPROFILESERVICE = "/_vti_bin/UserProfileService.asmx";
  /**
   * Standard SharePoint property.
   */
  public static final String SHAREPOINT_USERKEYDEFAULT = "AccountName";
  /**
   * Standard SharePoint property.
   */
  public static final String SHAREPOINT_USERCONTENTDEFAULT = "PreferredName";
  /**
   * Mapped meta in GSA.
   */
  public static final String PROPNAME_ACCOUNTNAME = 
      "google.userprofile.AccountName";
  /**
   * Mapped meta in GSA.
   */
  public static final String PROPNME_PREFERREDNAME = 
      "google.userprofile.PreferredName";
  /**
   * Standard SharePoint property.
   */
  public static final String PROPNAME_RESPONSIBILITY = "SPS_Responsibility";
  /**
   * Standard SharePoint property.
   */
  public static final String PROPNAME_SKILL = "SPS_Skills";
  /**
   * Standard SharePoint property.
   */
  public static final String PROPNAME_PASTPROJECTS = "SPS_PastProjects";
  /**
   * Default port for GSA admin controller.
   */
  public static final int DEFAULT_GSAADMINPORT = 8000;
  
}
