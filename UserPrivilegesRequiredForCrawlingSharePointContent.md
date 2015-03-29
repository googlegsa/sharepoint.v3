## User Privileges required for crawling SharePoint content ##

Following are the details of precise user privileges required for crawling SharePoint content

Out of all the web services that connector uses, only GetWebCollection() of Webs is the one which requires more then “Read” permission. All other web services run even with Read permission. So, because of GetWebCollection(), we always suggest users to use the next level of permission group which is Contribute.

Though, this is not the hard requirement, if user is ready to get into the details of SharePoint permissions. There are 40+ different rights that a user can be assigned to. Read, Contribute etc. are the default groupings that SharePoint provides, they are not the actual rights. The actual right, in addition to Read, that is needed to make GetWebCollection() call is “BrowseDirectory”. Hence, if a user with “Read” permission is directly assigned just one more right (“BrowseDirectory”), that would be sufficient to run the connector.

Following is the detailed steps for assigning the BrowseDirectory right to a user:

  1. Go to Site Permission -> Settings -> Permission Levels
> 2. Add a permission level
> 3. Select “Browse Directories” and click on create. The dependent rights will automatically be selected, these are “View Pages” and “Open”
> 4. Go back to Site Permission. Select the user which has only Read permission assigned. And, click on Actions -> Edit User Permission.
> 5. You will find the newly added permission level in the Permission list. Assign this permission to the user and click OK.




### Privileges as web application policies ###
> For a user who has been assigned permission through Web Application policies, “Full Read” is sufficient to call the web services and hence to run the connector. Following are the available permission categories which can be assigned to a web application policy user.

**To summarize:**

Add all the permission rights listed for "Read" permission level and the BrowseDirectory" permission right.
OR
Just assign "Full Read" permission as the web application policy.

For understanding more details of the contributor permission level you can refer to:

http://technet.microsoft.com/en-us/library/cc288314.aspx#stsk04_Contributor


### Permissions required for SharePoint 2003 ###

http://support.microsoft.com/kb/831085