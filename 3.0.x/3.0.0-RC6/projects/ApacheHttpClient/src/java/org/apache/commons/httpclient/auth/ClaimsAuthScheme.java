package org.apache.commons.httpclient.auth;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;

public class ClaimsAuthScheme implements AuthScheme {

    private boolean complete = false;
    public URI originalUri;
    public AuthScheme innerAuthScheme;

    /* @Override */
    public String authenticate(Credentials credentials, HttpMethod httpmethod) {
        return null;
    }

    /* @Override */
    public String authenticate(Credentials credentials, String method, String uri) {
        return null;
    }

    /* @Override */
    public String getID() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @Override */
    public String getParameter(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    /* @Override */
    public String getRealm() {
        // TODO Auto-generated method stub
        return null;
    }

    /* @Override */
    public String getSchemeName() {
        return "claims";
    }

    /* @Override */
    public boolean isComplete() {
        return complete;
    }

    public void setComplete() {
        complete = true;
    }

    /* @Override */
    public boolean isConnectionBased() {
        return false;
    }

    /* @Override */
    public void processChallenge(String challenge) {
    }
}
