// Copyright 2006 Google Inc.  All Rights Reserved.

package com.google.enterprise.connector.sharepoint;

import com.google.enterprise.connector.common.StringUtils;
import com.google.enterprise.connector.pusher.FeedConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class MockFileFeedConnection implements FeedConnection {

    StringBuffer buf = null;

    private final PrintStream printStream;
  
    public String getFeed() {
        String result;
        if (buf == null) {
            result = "";
        }
        result = buf.toString();
        buf = new StringBuffer(2048);
        return result;
    }

    public MockFileFeedConnection(PrintStream ps) {
        buf = new StringBuffer(2048);
        printStream = ps;
    }

    public String sendData(InputStream data) throws IOException {
        long before = System.currentTimeMillis();
        if (true) {
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = data.read(buffer)) != -1)
                printStream.write(buffer, 0, count);
            printStream.println();
        } else {
            int ch;
            while ((ch = data.read()) != -1)
                printStream.write(ch);
            printStream.println();
        }
        long after = System.currentTimeMillis();
        System.out.println("Elapsed time = " + (after - before));
        return "Mock response";
    }

}
