/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejte.ccl.webrunner.proxy;

/*
 *
* This class is a wrapper around everything associated with a reply from an HTTP server.This is used by
 *the proxy part of htppd class.
 *
 */
public class HttpResponse
{
        int statusCode;
        String reasonPhrase;
        MimeHeader mh;
        static String CRLF="\r\n";

        /*
         *When a request is sent to HTTP server, it responds with a  status code, which is stored in statusCode,
         *and a textual equivalent,which is stored in reasonPhrase.This single line response is followed by a MIME header.MimeHeader class is used
         *to parse this string.This method is called by constructor of HttpResponse.
         * @author       Deepa Singh(deepa.singh@sun.com)
         *@Company Sun Microsystems Inc.
         @param                request        String request
        */
        public void parse(String request)
        {
                int fsp=request.indexOf(' ');
                int nsp=request.indexOf(' ',fsp+1);
                int eol=request.indexOf('\n');
                String protocol=request.substring(0,fsp);
                statusCode=Integer.parseInt(request.substring(fsp+1,nsp));
                reasonPhrase=request.substring(nsp+1,eol);
                String raw_mime_header=request.substring(eol+1);
                mh=new MimeHeader(raw_mime_header);
        }

        /**
        * Default Constructor.Takes a raw request as parameter and calls parse to create MimeHeader.
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        */
        HttpResponse(String request)
        {
                parse(request);
        }

        /**
        * Constructor with three parameters
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param                code        Integer
        * @param                reason         The reason String
        * @param                m                Mimeheader
        */
        HttpResponse(int code,String reason,MimeHeader m)
        {
                statusCode=code;
                reasonPhrase=reason;
                mh=m;
        }

        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        * @return       String
        */
        public String toString()
        {
                return "HTTP/1.0 "+ statusCode +" " + reasonPhrase + CRLF + mh + CRLF;
    }
}
