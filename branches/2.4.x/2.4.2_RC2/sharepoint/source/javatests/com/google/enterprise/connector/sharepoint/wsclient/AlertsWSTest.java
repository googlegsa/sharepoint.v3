//Copyright 2007 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.wsclient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.state.GlobalState;
import com.google.enterprise.connector.sharepoint.state.ListState;
import com.google.enterprise.connector.sharepoint.state.WebState;

public class AlertsWSTest extends TestCase {
    SharepointClientContext sharepointClientContext;
    AlertsWS alertWS;

    protected void setUp() throws Exception {
        System.out.println("\n...Setting Up...");
        System.out.println("Initializing SharepointClientContext ...");
        this.sharepointClientContext = new SharepointClientContext(
                TestConfiguration.sharepointUrl, TestConfiguration.domain,
                TestConfiguration.kdcserver, TestConfiguration.username, TestConfiguration.Password,
                TestConfiguration.googleConnectorWorkDir,
                TestConfiguration.includedURls, TestConfiguration.excludedURls,
                TestConfiguration.mySiteBaseURL, TestConfiguration.AliasMap,
                TestConfiguration.feedType);
        assertNotNull(this.sharepointClientContext);
        System.out.println("Initializing AlertsWS ...");
        sharepointClientContext.setIncluded_metadata(TestConfiguration.whiteList);
        sharepointClientContext.setExcluded_metadata(TestConfiguration.blackList);

        this.alertWS = new AlertsWS(this.sharepointClientContext);
    }

    public final void testAlertsWS() throws Throwable {
        System.out.println("Testing AlertsWS(SharepointClientContext, siteName)...");
        sharepointClientContext.setSiteURL(TestConfiguration.ParentWebURL);
        this.alertWS = new AlertsWS(this.sharepointClientContext);
        assertNotNull(this.alertWS);
        System.out.println("[ AlertsWS(SharepointClientContext, siteName) ] Test Passed");
    }

    public final void testAlerts() throws Throwable {
        System.out.println("Testing getAlerts()...");
        final String internalName = this.sharepointClientContext.getSiteURL();
        final Calendar cLastMod = Calendar.getInstance();
        cLastMod.setTime(new Date());
		final GlobalState state = TestConfiguration.initState();
		WebState ws = state.makeWebState(sharepointClientContext, TestConfiguration.ParentWebURL);
		final ListState currentDummyAlertList = new ListState(internalName,
				SPConstants.ALERTS_TYPE, SPConstants.ALERTS_TYPE, cLastMod,
				SPConstants.ALERTS_TYPE, internalName, ws);

        final ArrayList lstAlerts = (ArrayList) this.alertWS.getAlerts(ws, currentDummyAlertList);
        assertNotNull(lstAlerts);
        System.out.println("[ getAlerts() ] Test Passed");
    }
}
