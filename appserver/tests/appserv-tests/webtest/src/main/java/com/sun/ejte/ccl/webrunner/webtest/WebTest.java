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

import java.io.FileInputStream;
import java.io.File;

/**
*This is the main class for Web Test.It takes a text script file as an argument.
*
* @author       Deepa Singh (deepa.singh@sun.com)
 *Company       Sun Microsystems Inc.
*
*/
public class WebTest
{
    private String ws_root="appserv-tests";
    private String testsuite_id="";

    public WebTest(){}

    public void setTestSuiteID(String testsuiteid)
    {
        this.testsuite_id=testsuiteid;
    }

    public void setResultFileLocation(String workspace_root)
    {
        this.ws_root=workspace_root;
    }

    /**
     *Reads script file and converts into a byte array.Sends byte array to SendRequest class.
     *@author Deepa Singh deepa.singh@sun.com
     *@param file String fully qualified location of file
     *@param host String host name of web server where web application is to be run.
     *
     */
    public void readFile(String file,String s_host,String s_port)
    {
        try
        {
            FileInputStream fin=new FileInputStream(file);
            File f=new File(file);
            byte buffer[]=new byte[(int)f.length()];
            System.out.println("size of buffer is"+buffer.length);
            int pos=0;
            int n;
            while((n=fin.read())>=0)
            {
                if(pos>(int)f.length())
                {
                    System.out.println("EOF reached");
                    break;
                }
                buffer[pos]=(byte)n;
                pos=pos+1;
            }

            fin.close();
            SendRequest sendRequest=new SendRequest(ws_root,testsuite_id);
            int port=Integer.parseInt(s_port);
            sendRequest.setServerProperties(s_host,port);
            sendRequest.processUrl(buffer);
        }
        catch(Exception e)
        {
            System.out.println("Error in reading Script File");
            e.printStackTrace();
        }
    }



    public static void main(String [] args)
    {

        if(args.length<4)
        {
            System.err.println("usage:\t WebTest <<full_file_name>> <<web_server_host_name>> <<web_server_port>> <<outputfile>> <<testsuiteid>>");
            System.exit(0);
        }
        String file= args[0];
        String serverhost=args[1];
        String serverport=args[2];
        String ws_root=args[3];
        String testsuiteid=args[4];

        WebTest webTest=new WebTest();
        webTest.setResultFileLocation(ws_root);
        webTest.setTestSuiteID(testsuiteid);
        webTest.readFile(file,serverhost,serverport);

    }
}


