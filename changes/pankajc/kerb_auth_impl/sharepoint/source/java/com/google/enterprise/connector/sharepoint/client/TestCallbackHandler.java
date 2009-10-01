//Copyright 2009 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.client;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.google.enterprise.connector.sharepoint.spiimpl.SharepointConnectorType;

public class TestCallbackHandler implements CallbackHandler {
	private String userName;
	private String password;
	
	public TestCallbackHandler(){
		System.out.println("constructor called...");
		/*setPassword("Admin1234");
		setUserName("farmadmin@GDC-PSL.NET");*/
		Preferences prefs = Preferences.userRoot();
		this.userName = new String(prefs.getByteArray("UserName", new byte[1]));
		this.password = new String(prefs.getByteArray("Password", new byte[1]));
		
		System.out.println(prefs.get("UserName", "noUsrNme"));
	}
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    	for (int i = 0; i < callbacks.length; i++) {
    	    if (callbacks[i] instanceof TextOutputCallback) {
	     		// display the message according to the specified type
	    		TextOutputCallback toc = (TextOutputCallback)callbacks[i];
	    		switch (toc.getMessageType()) {
		    		case TextOutputCallback.INFORMATION:
		    		    System.out.println(toc.getMessage());
		    		    break;
		    		case TextOutputCallback.ERROR:
		    		    System.out.println("ERROR: " + toc.getMessage());
		    		    break;
		    		case TextOutputCallback.WARNING:
		    		    System.out.println("WARNING: " + toc.getMessage());
		    		    break;
		    		default:
		    		    throw new IOException("Unsupported message type: " +
		    					toc.getMessageType());
	    		}
    	    } else if (callbacks[i] instanceof NameCallback) {
	     		NameCallback nc = (NameCallback)callbacks[i];
//	     		nc.setName("farmadmin@GDC-PSL.NET");
//	     		nc.setName(userName);
//	     		nc.setName(SharepointConnectorType.credentials.get("Username").toString());
	     		nc.setName(SharepointClientContext.credentials.get("Username").toString());
    	    } else if (callbacks[i] instanceof PasswordCallback) {
	    		PasswordCallback pc = (PasswordCallback)callbacks[i];
//	    		pc.setPassword("Admin1234".toCharArray());
//	    		pc.setPassword(password.toCharArray());
//	    		pc.setPassword(SharepointConnectorType.credentials.get("Password").toString().toCharArray());
	    		pc.setPassword(SharepointClientContext.credentials.get("Password").toString().toCharArray());
    	    } else {
	    		throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
    	    }
    	 }
    }
	private void setPassword(String password) {
		this.password = password;
	}
	private void setUserName(String userName) {
		this.userName = userName;
	}
}