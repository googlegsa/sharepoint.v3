/**
 * UserProfileServiceSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.google.enterprise.connector.sharepoint.generated.userprofileservice;

public interface UserProfileServiceSoap_PortType extends java.rmi.Remote {
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.GetUserProfileByIndexResult getUserProfileByIndex(int index) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] getUserProfileByName(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] createUserProfileByAccountName(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] getUserProfileByGuid(java.lang.String guid) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyInfo[] getUserProfileSchema() throws java.rmi.RemoteException;
    public java.lang.String[] getPropertyChoiceList(java.lang.String propertyName) throws java.rmi.RemoteException;
    public void modifyUserPropertyByAccountName(java.lang.String accountName, com.google.enterprise.connector.sharepoint.generated.userprofileservice.PropertyData[] newData) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] getUserMemberships(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] getUserColleagues(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.QuickLinkData[] getUserLinks(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData[] getUserPinnedLinks(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.InCommonData getInCommon(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData getCommonManager(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData[] getCommonColleagues(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData[] getCommonMemberships(java.lang.String accountName) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.ContactData addColleague(java.lang.String accountName, java.lang.String colleagueAccountName, java.lang.String group, com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy, boolean isInWorkGroup) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData addMembership(java.lang.String accountName, com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo, java.lang.String group, com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) throws java.rmi.RemoteException;
    public void createMemberGroup(com.google.enterprise.connector.sharepoint.generated.userprofileservice.MembershipData membershipInfo) throws java.rmi.RemoteException;
    public void removeColleague(java.lang.String accountName, java.lang.String colleagueAccountName) throws java.rmi.RemoteException;
    public void removeMembership(java.lang.String accountName, java.lang.String sourceInternal, java.lang.String sourceReference) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.QuickLinkData addLink(java.lang.String accountName, java.lang.String name, java.lang.String url, java.lang.String group, com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy privacy) throws java.rmi.RemoteException;
    public com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData addPinnedLink(java.lang.String accountName, java.lang.String name, java.lang.String url) throws java.rmi.RemoteException;
    public void updateLink(java.lang.String accountName, com.google.enterprise.connector.sharepoint.generated.userprofileservice.QuickLinkData data) throws java.rmi.RemoteException;
    public void updateColleaguePrivacy(java.lang.String accountName, java.lang.String colleagueAccountName, com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy newPrivacy) throws java.rmi.RemoteException;
    public void updateMembershipPrivacy(java.lang.String accountName, java.lang.String sourceInternal, java.lang.String sourceReference, com.google.enterprise.connector.sharepoint.generated.userprofileservice.Privacy newPrivacy) throws java.rmi.RemoteException;
    public void removeLink(java.lang.String accountName, int id) throws java.rmi.RemoteException;
    public void removeAllLinks(java.lang.String accountName) throws java.rmi.RemoteException;
    public void removeAllPinnedLinks(java.lang.String accountName) throws java.rmi.RemoteException;
    public void removeAllColleagues(java.lang.String accountName) throws java.rmi.RemoteException;
    public void removeAllMemberships(java.lang.String accountName) throws java.rmi.RemoteException;
    public long getUserProfileCount() throws java.rmi.RemoteException;
    public void updatePinnedLink(java.lang.String accountName, com.google.enterprise.connector.sharepoint.generated.userprofileservice.PinnedLinkData data) throws java.rmi.RemoteException;
    public void removePinnedLink(java.lang.String accountName, int id) throws java.rmi.RemoteException;
}
