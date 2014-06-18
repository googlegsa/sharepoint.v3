package com.google.enterprise.connector.sharepoint.wsclient.client;

import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeDataContainer;
import com.google.enterprise.connector.sharepoint.generated.userprofilechangeservice.UserProfileChangeQuery;

import java.rmi.RemoteException;

public interface UserProfileChangeWS extends BaseWS {
  /**
   * Gets changes made to the user profiles using the specified change 
   * query and the specified change token.
   *
   * @param changeToken The change token of the changes in the user profile
   * @param changeQuery The change query specifying the requested changes
   * @return The changes made to the user profiles that match the filter 
   *         defined by the specified change token and change query.
   */
  public UserProfileChangeDataContainer getChanges(String changeToken,
      UserProfileChangeQuery changeQuery) throws RemoteException;

  /**
   * Gets latest change token from SharePoint for User Profile Changes 
   *
   * @return latest change token for user profiles
   * @throws Exception
   */
  public String getCurrentChangeToken() throws RemoteException;
}
