/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.v3.services.impl;

import java.nio.ByteBuffer;

import org.glassfish.grizzly.http.util.Ascii;

/**
 * Utility class for parsing ByteBuffer
 * @author Jeanfrancois
 */
public class HttpUtils {


    private final static String CSS =
            "H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
            "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
            "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
            "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
            "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
            "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
            "A {color : black;}" +
            "HR {color : #525D76;}";

    /**
     * Specialized utility method: find a sequence of lower case bytes inside
     * a {@link ByteBuffer}.
     */
    public static int findBytes(ByteBuffer byteBuffer, byte[] b) {
        int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();

        if (byteBuffer.position() == 0){
            throw new IllegalStateException("Invalid state");
        }

        byteBuffer.position(0);
        byteBuffer.limit(curPosition);
        try {
            byte first = b[0];
            int start = 0;
            int end = curPosition;

            // Look for first char
            int srcEnd = b.length;

            for (int i = start; i <= (end - srcEnd); i++) {
                if (Ascii.toLower(byteBuffer.get(i)) != first) continue;
                // found first char, now look for a match
                int myPos = i+1;
                for (int srcPos = 1; srcPos < srcEnd; ) {
                        if (Ascii.toLower(byteBuffer.get(myPos++)) != b[srcPos++])
                    break;
                        if (srcPos == srcEnd) return i - start; // found it
                }
            }
            return -1;
        } finally {
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);
        }
    }

    public static String getErrorPage(String serverName, String message, String errorCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" ");
        sb.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><title>GlassFish v4 - Error report</title><style type=\"");
        sb.append("text/css\"><!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;}");
        sb.append(" H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;}");
        sb.append(" H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;}");
        sb.append(" BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} ");
        sb.append(" B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} P");
        sb.append("{font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}A");
        sb.append(" {color : black;}HR {color : #525D76;}--></style> </head><body><h1>HTTP Status ");
        sb.append(errorCode).append(" - ");
        sb.append("</h1><hr/><p><b>type</b> Status report</p><p><b>message</b></p><p><b>description</b>");
        sb.append(message).append("</p><hr/><h3>");
        sb.append(serverName).append("</h3></body></html>");
        return sb.toString();
    }

}

