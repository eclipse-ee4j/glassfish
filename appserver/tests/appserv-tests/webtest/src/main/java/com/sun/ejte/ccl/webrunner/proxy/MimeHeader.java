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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
* MIME is an internet standard for communicating multimedia content over e-mail systems.MimeHeader extends HashTable so that it can store key value pairs.
* @author       Deepa Singh(deepa.singh@sun.com)
 *Company: Sun Microsystems Inc
*
*/
public class MimeHeader extends Hashtable
{
        /**
        * Takes a String as a parameter and parses it.Takes a raw MIME-formatted String and enter its key/value pairs into a given instance of MimeHeader.Uses
         *StringTokenizer to split the input data into individual lines marked by CRLF(\r\n) sequence.
        * @author       Deepa Singh(deepa.singh@sun.com)
        * @return       void
        * @param                data        The string to be parsed
        */
        void parse(String data)
        {
        StringTokenizer st=new StringTokenizer(data,"\r\n");
                while(st.hasMoreTokens())
                {
                        String s=st.nextToken();
                        int colon=s.indexOf(':');
                        String key=s.substring(0,colon);
                        String val=s.substring(colon+2);
                        put(key,val);
                }
        }

        /**
        * Default Constructor
         *Class is a subclass of HashTable so that it can conveneintly store and retreive the key/value pairs
         *associated with a MIME header.It creates a blank MimeHeader with no keys.
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param                None
        */
        MimeHeader(){}

        /**
        * This constructor takes strng formatted as MIME header and parses it for the initial contents of the objects.
        * @author       Deepa Singh(deepa.singh@sun.com)
        *
        * @param                d        The string to be parsed
        */
        MimeHeader(String d)
        {
                parse(d);
        }

        /**
        * Converts to String .It takes current key/value pairs stored in the MimeHeader and returns a string representation of them in
         *MIME format, where keys are printed followed by colon and a space, and then value followed by CRLF.
        * @author       Deepa Singh(deepa.singh@sun.com)
        * @return       String
        */
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

        /**
        * @author       Deepa Singh(deepa.singh@sun.com)
        * @return       String
        * @param                key        String to be fetched
        */
        public String get(String key)
        {
                return (String)super.get(fix(key));
        }

        /**
        *
        * @author       Deepa Singh(deepa.singh@sun.com)
        * @return       void
        * @param                key                The Key String
        * @param                val          The value String
        */
        public void put(String key,String val)
        {
                super.put(fix(key),val);
        }


}
