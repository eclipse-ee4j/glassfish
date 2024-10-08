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

package com.sun.ejte.ccl.webrunner.webtest;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
* MIME is an internet standard for communicating multimedia content over e-mail systems.MimeHeader extends HashTable so that it can store key value pairs.
 *
* @author       Deepa Singh(deepa.singh@sun.com)
 *Company: Sun Microsystems Inc
 *@see com.sun.ejte.ccl.webrunner.proxy.MimeHeader
*
*/

public class MimeHeader extends Hashtable
{
    private String RequestHeader=new String();
    private  boolean requestGET=false;
    private boolean requestPOST=false;
    private boolean cookieSet=false;
    private String postdata=new String();


    /**
     * Takes a String as a parameter and parses it.Takes a raw MIME-formatted String and enter its key/value pairs into a given instance of MimeHeader.Uses
     *StringTokenizer to split the input data into individual lines marked by CRLF(\r\n) sequence.
     *It differs in it's implementation of parse() from com.sun.ejte.ccl.webrunner.proxy.MimeHeader.parse() in that it checks the request type.
     *If it is POST then stores the post data to be sent to external web server.
     * @author       Deepa Singh(deepa.singh@sun.com)
     * @return       void
     * @param                data        The string to be parsed
     **@see com.sun.ejte.ccl.webrunner.proxy.MimeHeader
    */
    void parse(String data)
    {
        StringTokenizer st=new StringTokenizer(data,"\r\n");
        while(st.hasMoreTokens())
        {
            String s=st.nextToken();

            //first check for Cookie as they are contained in script file.This needs to be stripped from the new request string sent to server
            if(s.startsWith("Cookie"))
            {
                cookieSet=true;
            }
            else
                cookieSet=false;

            //These lines are HTTP headers.Do not contain request.
            if(!s.startsWith("GET") && (!s.startsWith("POST")))
            {
                //System.out.println("_from_mime_header"+s);
                if((s.indexOf(':'))!=-1)
                {
                    int colon=s.indexOf(':');
                    String key=s.substring(0,colon);
                    String val=s.substring(colon+2);
                    put(key,val);
                }
                //This is POST data after request and headers.Header has ended and after CRLF Content starts.
                //\r\n\r\n is CRLF.Header lines are separated by \r\n
                else
                {
                    //System.out.println("\n%%%%%%POST DATA%%%%%%");
                    postdata=s;
                    postdata.trim();
                    System.out.println(postdata);
                }
            }
            //This is the first line of HTTP request
            else
            {
                RequestHeader=s;
                if((RequestHeader.indexOf("GET"))!=-1)
                    requestGET=true;
                else if((RequestHeader.indexOf("POST"))!=-1)
                    requestPOST=true;
            }
        }
    }

        MimeHeader(){}
        public boolean ifGETRequest()
        {
            return this.requestGET;
        }
        public boolean ifPOSTRequest()
        {
            return this.requestPOST;
        }
        public String getRequestHeader()
        {
            return this.RequestHeader;
        }
        MimeHeader(String d)
        {
                parse(d);
        }

        public String toString()
        {
                String ret="";
                Enumeration e=keys();
                while(e.hasMoreElements())
                {
                String key=(String)e.nextElement();
                String val=(String)get(key);
                ret+=key + ": " + val + "\r\n";
                }
        return ret;
        }

        public String getPostData()
        {
                return postdata.trim();

        }



        /**
        *To remove the discrepancy in MIME specification for "Content-Type" and "content-type" and "Content-Length" to "content-length"
         *To avoid problems, all incoming and outgoing MimeHeader keys are converted to canonical form.
        * @author       Deepa Singh(deepa.singh@sun.com)
        * @return       String
        * @param                ms        String to be operated upon
        */
        private String fix(String ms)
        {
        char chars[]=ms.toLowerCase().toCharArray();
        boolean upcaseNext=true;
        for(int i=0;i<chars.length-1;i++)
        {
                char ch=chars[i];
                if(upcaseNext && 'a' <=ch && ch <='z')
                {
                        chars[i]=(char)(ch-('a'-'A'));
                }
                upcaseNext=ch=='-';
        }
        return new String(chars);
        }


        /*
        * @author       Deepa Singh(deepa.singh@sun.com)
       * @return        String
       * @param                String key MIME header e.g Content-Type

       **@see com.sun.ejte.ccl.webrunner.proxy.MimeHeader
       */
        public String get(String key)
        {
        return (String)super.get(fix(key));
        }


       /*
        * @author       Deepa Singh(deepa.singh@sun.com)
       * @return       void
       * @param                String key MIME header e.g Content-Type
        *@param         String value value of MIME header
       **@see com.sun.ejte.ccl.webrunner.proxy.MimeHeader
       */
        public void put(String key,String val)
        {
        super.put(fix(key),val);
        }
}
