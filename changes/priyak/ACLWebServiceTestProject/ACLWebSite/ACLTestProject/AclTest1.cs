using System;
using System.Text;
using System.Collections.Generic;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using ACLTestProject.GssAclTestProjectReference;
using ACLTestProject.GssAclTestProjectReference2010;
using System.ServiceModel;

namespace ACLTestProject
{
    /// <summary>
    /// Summary description for AclTest1
    /// </summary>
    [TestClass]
    public class AclTest1
    {

        string serviceReferenceName_2007 = Properties.Settings.Default.ServiceReferenceName_2007;
        string serviceReferenceName_2010 = Properties.Settings.Default.ServiceReferenceName_2010;
        string username = Properties.Settings.Default.UserName;
        string password = Properties.Settings.Default.Password;


        
        public AclTest1()
        {
        }

        private TestContext testContextInstance;

        /// <summary>
        ///Gets or sets the test context which provides
        ///information about and functionality for the current test run.
        ///</summary>
        public TestContext TestContext
        {
            get
            {
                return testContextInstance;
            }
            set
            {
                testContextInstance = value;
            }
        }

        #region Additional test attributes
        //
        // You can use the following additional attributes as you write your tests:
        //
        // Use ClassInitialize to run code before running the first test in the class
        // [ClassInitialize()]
        // public static void MyClassInitialize(TestContext testContext) { }
        //
        // Use ClassCleanup to run code after all tests in a class have run
        // [ClassCleanup()]
        // public static void MyClassCleanup() { }
        //
        // Use TestInitialize to run code before running each test 
        // [TestInitialize()]
        // public void MyTestInitialize() { }
        //
        // Use TestCleanup to run code after each test has run
        // [TestCleanup()]
        // public void MyTestCleanup() { }
        //
        #endregion

        /// <summary>
        /// Test Method for CheckConnectivity web method
        /// </summary>
        [TestMethod]
        public void CheckConnectivityTestMethod()
        {
            /*********  Code lines for SP 2007 CheckConnectivity web method *********/ 

            GssAclTestProjectReference.GssAclMonitorSoapClient client;
            InitialiseWebClient_2007(serviceReferenceName_2007,out client, username, password);
            string actual = client.CheckConnectivity();

            // Assign value from the Settings.settings file for 'expected'
            string expected = Properties.Settings.Default.Success;
            Assert.AreEqual(actual, expected);

            /********* Code lines for SP 2010 CheckConnectivity web method *********/
            actual = "";

            GssAclTestProjectReference2010.GssAclMonitorSoapClient client_2010;
            InitialiseWebClient_2010(serviceReferenceName_2010, out client_2010, username, password);
            actual = client_2010.CheckConnectivity();
            Assert.AreEqual(actual, expected);
        }

        /// <summary>
        /// Test Method for GetAclChangesSinceToken web method
        /// </summary>
        [TestMethod]
        public void GetAclChangesSinceTokenTestMethod()
        {
            /*********  Code lines for SP 2007 GetAclChangesSinceToken web method *********/ 
            GssAclTestProjectReference.GssGetAclChangesSinceTokenResult objGssGetAclChangesSinceTokenResult = null;
            GssAclTestProjectReference.GssAclMonitorSoapClient client;
            InitialiseWebClient_2007(serviceReferenceName_2007, out client, username, password);

            // Assign value from the Settings.settings file for 'fromChangeToken' and 'toChangeToken'
            string fromChangeToken = Properties.Settings.Default.FromChangeToken_2007;
            string toChangeToken = Properties.Settings.Default.ToChangeToken_2007;
            objGssGetAclChangesSinceTokenResult = client.GetAclChangesSinceToken(fromChangeToken, toChangeToken);
            Assert.IsNotNull(objGssGetAclChangesSinceTokenResult.AllChanges);
            Assert.IsNotNull(objGssGetAclChangesSinceTokenResult.SiteCollectionGuid);
            Assert.IsNotNull(objGssGetAclChangesSinceTokenResult.SiteCollectionUrl);

            /*********  Code lines for SP 2010 GetAclChangesSinceToken web method *********/

            fromChangeToken = "";
            toChangeToken = "";

            GssAclTestProjectReference2010.GssGetAclChangesSinceTokenResult objGssGetAclChangesSinceTokenResult_2010 = null;
            GssAclTestProjectReference2010.GssAclMonitorSoapClient client_2010;
            InitialiseWebClient_2010(serviceReferenceName_2010, out client_2010, username, password);
            // Assign value from the Settings.settings file for 'fromChangeToken' and 'toChangeToken'
            fromChangeToken = Properties.Settings.Default.FromChangeToken_2010;
            toChangeToken = Properties.Settings.Default.ToChangeToken_2010;
            objGssGetAclChangesSinceTokenResult_2010 = client_2010.GetAclChangesSinceToken(fromChangeToken, toChangeToken);
            Assert.IsNotNull(objGssGetAclChangesSinceTokenResult_2010.AllChanges);
            Assert.IsNotNull(objGssGetAclChangesSinceTokenResult_2010.SiteCollectionGuid);
            Assert.IsNotNull(objGssGetAclChangesSinceTokenResult_2010.SiteCollectionUrl);

        }

        /// <summary>
        /// Test Method for GetAclForUrls web method
        /// </summary>
        [TestMethod]
        public void GetAclForUrlsTestMethod()
        {
            /*********  Code lines for SP 2007 GetAclForUrls web method *********/ 
            GssAclTestProjectReference.GssGetAclForUrlsResult objGssGetAclForUrlsResult = null;
            GssAclTestProjectReference.GssAclMonitorSoapClient client;
            InitialiseWebClient_2007(serviceReferenceName_2007, out client, username, password);
            
            string[] arrayOfUrls = new string[2];
            // Assign value from the Settings.settings file for 'arrayOfUrls'
            arrayOfUrls[0] = Properties.Settings.Default.ArrayOfUrlsElement1_2007;
            arrayOfUrls[1] = Properties.Settings.Default.ArrayOfUrlsElement2_2007;
            objGssGetAclForUrlsResult = client.GetAclForUrls(arrayOfUrls);
            Assert.IsNotNull(objGssGetAclForUrlsResult);
            Assert.IsNotNull(objGssGetAclForUrlsResult.AllAcls);
            Assert.IsNotNull(objGssGetAclForUrlsResult.SiteCollectionGuid);
            Assert.IsNotNull(objGssGetAclForUrlsResult.SiteCollectionUrl);

            /*********  Code lines for SP 2010 GetAclForUrls web method *********/
            arrayOfUrls = null;
            arrayOfUrls = new string[2];

            GssAclTestProjectReference2010.GssGetAclForUrlsResult objGssGetAclForUrlsResult_2010 = null;
            GssAclTestProjectReference2010.GssAclMonitorSoapClient client_2010;
            InitialiseWebClient_2010(serviceReferenceName_2010, out client_2010, username, password);

            // Assign value from the Settings.settings file for 'arrayOfUrls'
            arrayOfUrls[0] = Properties.Settings.Default.ArrayOfUrlsElement1_2010;
            arrayOfUrls[1] = Properties.Settings.Default.ArrayOfUrlsElement2_2010;

            objGssGetAclForUrlsResult_2010 = client_2010.GetAclForUrls(arrayOfUrls);
            Assert.IsNotNull(objGssGetAclForUrlsResult_2010);
            Assert.IsNotNull(objGssGetAclForUrlsResult_2010.AllAcls);
            Assert.IsNotNull(objGssGetAclForUrlsResult_2010.SiteCollectionGuid);
            Assert.IsNotNull(objGssGetAclForUrlsResult_2010.SiteCollectionUrl);

        }

        /// <summary>
        /// Test Method for GetListItemsWithInheritingRoleAssignments web method
        /// </summary>
        [TestMethod]
        public void GetListItemsWithInheritingRoleAssignmentsTestMethod()
        {
            /*********  Code lines for SP 2007 GetListItemsWithInheritingRoleAssignments web method *********/
            GssAclTestProjectReference.GssGetListItemsWithInheritingRoleAssignments objGssGetListItemsWithInheritingRoleAssignments = null;
            GssAclTestProjectReference.GssAclMonitorSoapClient client;
            InitialiseWebClient_2007(serviceReferenceName_2007, out client, username, password);

            // Assign values from the Settings.settings file for 'listGuId', 'rowLimit' and 'lastItemId'
            string listGuId = Properties.Settings.Default.ListGUID_2007;
            int rowLimit = Properties.Settings.Default.RowLimit_2007;
            int lastItemId = Properties.Settings.Default.LastItemId_2007;

            objGssGetListItemsWithInheritingRoleAssignments = client.GetListItemsWithInheritingRoleAssignments(listGuId, rowLimit, lastItemId);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments.DocXml);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments.LastIdVisited);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments.MoreDocs);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments.SiteCollectionGuid);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments.SiteCollectionUrl);


            /*********  Code lines for SP 2010 GetListItemsWithInheritingRoleAssignments web method *********/
            listGuId = "";
            rowLimit = -1;
            lastItemId = -1;

            GssAclTestProjectReference2010.GssGetListItemsWithInheritingRoleAssignments objGssGetListItemsWithInheritingRoleAssignments_2010 = null;
            GssAclTestProjectReference2010.GssAclMonitorSoapClient client_2010;
            InitialiseWebClient_2010(serviceReferenceName_2010, out client_2010, username, password);

            // Assign values from the Settings.settings file for 'listGuId', 'rowLimit' and 'lastItemId'
            listGuId = Properties.Settings.Default.ListGUID_2010;
            rowLimit = Properties.Settings.Default.RowLimit_2010;
            lastItemId = Properties.Settings.Default.LastItemId_2010;

            objGssGetListItemsWithInheritingRoleAssignments_2010 = client_2010.GetListItemsWithInheritingRoleAssignments(listGuId, rowLimit, lastItemId);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments_2010);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments_2010.DocXml);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments_2010.LastIdVisited);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments_2010.MoreDocs);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments_2010.SiteCollectionGuid);
            Assert.IsNotNull(objGssGetListItemsWithInheritingRoleAssignments_2010.SiteCollectionUrl);
        }

        /// <summary>
        /// Test Method for GetListsWithInheritingRoleAssignments web method
        /// </summary>
        [TestMethod]
        public void GetListsWithInheritingRoleAssignmentsTestMethod()
        {
            /*********  Code lines for SP 2007 GetListItemsWithInheritingRoleAssignments web method *********/
            GssAclTestProjectReference.GssAclMonitorSoapClient client;
            InitialiseWebClient_2007(serviceReferenceName_2007, out client, username, password);
            string[] listIDs = null;
            listIDs = client.GetListsWithInheritingRoleAssignments();
            Assert.IsNotNull(listIDs);

            /*********  Code lines for SP 2010 GetListItemsWithInheritingRoleAssignments web method *********/
            listIDs = null;

            GssAclTestProjectReference2010.GssAclMonitorSoapClient client_2010;
            InitialiseWebClient_2010(serviceReferenceName_2010, out client_2010, username, password);
            listIDs = client_2010.GetListsWithInheritingRoleAssignments();
            Assert.IsNotNull(listIDs);
        }

        /// <summary>
        /// Test Method for ResolveSPGroup web method
        /// </summary>
        [TestMethod]
        public void ResolveSPGroupTestMethod()
        {
            /*********  Code lines for SP 2007 GetListItemsWithInheritingRoleAssignments web method *********/ 
            GssAclTestProjectReference.GssResolveSPGroupResult objGssResolveSPGroupResult = null;
            GssAclTestProjectReference.GssAclMonitorSoapClient client;
            InitialiseWebClient_2007(serviceReferenceName_2007, out client, username, password);
            string[] groupIds = new string[4];
            // Assign values from the Settings.settings file for groupIds array
            groupIds[0] = Properties.Settings.Default.GroupId1_2007;
            groupIds[1] = Properties.Settings.Default.GroupId2_2007;
            groupIds[2] = Properties.Settings.Default.GroupId3_2007;
            groupIds[3] = Properties.Settings.Default.GroupId4_2007;

           
            objGssResolveSPGroupResult = client.ResolveSPGroup(groupIds);
            Assert.IsNotNull(objGssResolveSPGroupResult);
            Assert.IsNotNull(objGssResolveSPGroupResult.Prinicpals);
            Assert.IsNotNull(objGssResolveSPGroupResult.SiteCollectionGuid);
            Assert.IsNotNull(objGssResolveSPGroupResult.SiteCollectionUrl);

            /*********  Code lines for SP 2010 GetListItemsWithInheritingRoleAssignments web method *********/
            groupIds = null;
            groupIds = new string[3];

            GssAclTestProjectReference2010.GssResolveSPGroupResult objGssResolveSPGroupResult_2010 = null;
            GssAclTestProjectReference2010.GssAclMonitorSoapClient client_2010;

            InitialiseWebClient_2010(serviceReferenceName_2010, out client_2010, username, password);

            // Assign values from the Settings.settings file for groupIds array
            groupIds[0] = Properties.Settings.Default.GroupId1_2010;
            groupIds[1] = Properties.Settings.Default.GroupId2_2010;
            groupIds[2] = Properties.Settings.Default.GroupId3_2010;

           
            objGssResolveSPGroupResult_2010 = client_2010.ResolveSPGroup(groupIds);
            Assert.IsNotNull(objGssResolveSPGroupResult_2010);
            Assert.IsNotNull(objGssResolveSPGroupResult_2010.Prinicpals);
            Assert.IsNotNull(objGssResolveSPGroupResult_2010.SiteCollectionGuid);
            Assert.IsNotNull(objGssResolveSPGroupResult_2010.SiteCollectionUrl);
        }

        /// <summary>
        /// Function to create the client which will call our service web methods (for SP 2007)
        /// </summary>
        /// <param name="serviceReferenceName">Reference Name used while adding the Service Reference to the ACL web service</param>
        /// <param name="client">Client Object</param>
        /// <param name="username">Username</param>
        /// <param name="password">Password</param>
        public void InitialiseWebClient_2007(string serviceReferenceName, out GssAclTestProjectReference.GssAclMonitorSoapClient client,string username, string password)
        {
            client = new ACLTestProject.GssAclTestProjectReference.GssAclMonitorSoapClient(serviceReferenceName);
            client.ClientCredentials.Windows.ClientCredential.UserName = username;
            client.ClientCredentials.Windows.ClientCredential.Password = password;
            client.ClientCredentials.Windows.AllowedImpersonationLevel = System.Security.Principal.TokenImpersonationLevel.Delegation;
        }

        /// <summary>
        ///  Function to create the client which will call our service web methods (for SP 2010)
        /// </summary>
        /// <param name="serviceReferenceName">Reference Name used while adding the Service Reference to the ACL web service</param>
        /// <param name="client">Client Object</param>
        /// <param name="username">Username</param>
        /// <param name="password">Password</param>
        public void InitialiseWebClient_2010(string serviceReferenceName, out GssAclTestProjectReference2010.GssAclMonitorSoapClient client, string username, string password)
        {
            client = new ACLTestProject.GssAclTestProjectReference2010.GssAclMonitorSoapClient(serviceReferenceName);
            client.ClientCredentials.Windows.ClientCredential.UserName = username;
            client.ClientCredentials.Windows.ClientCredential.Password = password;
            client.ClientCredentials.Windows.AllowedImpersonationLevel = System.Security.Principal.TokenImpersonationLevel.Delegation;
        }
    }
}
