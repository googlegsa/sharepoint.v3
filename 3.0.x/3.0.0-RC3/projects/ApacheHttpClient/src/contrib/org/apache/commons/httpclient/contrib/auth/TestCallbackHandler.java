package org.apache.commons.httpclient.contrib.auth;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class TestCallbackHandler implements CallbackHandler {
	private String userName;
	private String password;
	
	public TestCallbackHandler(String username, String password){
		this.userName = username;
		this.password = password;
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
	     		nc.setName(userName);
    	    } else if (callbacks[i] instanceof PasswordCallback) {
	    		PasswordCallback pc = (PasswordCallback)callbacks[i];
	    		pc.setPassword(password.toCharArray());
    	    } else {
	    		throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
    	    }
    	 }
    }
}