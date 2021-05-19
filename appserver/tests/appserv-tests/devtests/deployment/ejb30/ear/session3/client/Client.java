/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.net.*;
import java.io.*;
import java.util.*;
import javax.naming.*;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.EntityManager;

public class Client {

    private String host;
    private String port;

    @PersistenceUnit
        private static EntityManagerFactory emf1;

    @PersistenceUnit(name="myemf", unitName="foo")
        private static EntityManagerFactory emf2;

    public Client (String[] args) {
        host = ( args.length > 0) ? args[0] : "localhost";
        port = ( args.length > 1) ? args[1] : "4848";
    }

    public static void main(String[] args) {
        Client client = new Client(args);
        client.doTest();
    }

    public void doTest() {

        String env = null;
        try {

            InitialContext ic = new InitialContext();

            if( (emf1 != null) && (emf2 != null) ) {

                emf1.isOpen();
                emf2.isOpen();

                EntityManagerFactory lookupemf1 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.Client/emf1");

                EntityManagerFactory lookupemf2 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/myemf");

                System.out.println("AppClient successful injection of EMF references!");
            } else {
                throw new Exception("One or more EMF/EM references" +
                                    " was not injected in AppClient");
            }


            String url = "http://" + host + ":" + port +
                "/ejb-ejb30-hello-session3/servlet";
            System.out.println("invoking webclient servlet at " + url);
            int code = invokeServlet(url);

            if(code != 200) {
                System.out.println("Incorrect return code: " + code);
            } else {
                System.out.println("Correct return code: " + code);
            }
        } catch (Exception ex) {
            System.out.println("Jms web test failed.");
            ex.printStackTrace();
        }

        return;

    }

    private int invokeServlet(String url) throws Exception {

        URL u = new URL(url);

        HttpURLConnection c1 = (HttpURLConnection)u.openConnection();
        int code = c1.getResponseCode();
        InputStream is = c1.getInputStream();
        BufferedReader input = new BufferedReader (new InputStreamReader(is));
        String line = null;
        while((line = input.readLine()) != null)
            System.out.println(line);
        if(code != 200) {
            System.out.println("Incorrect return code: " + code);
        }
        return code;
    }

}

