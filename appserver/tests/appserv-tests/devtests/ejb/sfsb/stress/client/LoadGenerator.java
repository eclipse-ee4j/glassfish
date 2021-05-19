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

package com.sun.s1asdev.ejb.sfsb.stress.client;

import jakarta.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import java.util.Properties;
import java.io.FileInputStream;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSBHome;
import com.sun.s1asdev.ejb.sfsb.stress.ejb.StressSFSB;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class LoadGenerator {
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static String propsFileName =
        "/export/home/s1as/cts/ws/appserv-tests/devtests/ejb/sfsb/stress/client/jndi.properties";

    private Context ctx;
    private StressSFSBHome home;

    public LoadGenerator(String[] args)
        throws Exception
    {
        String jndiName = args[0];
        ctx = getContext(args[1]);

        Object ref = ctx.lookup(jndiName);
        this.home = (StressSFSBHome)
            PortableRemoteObject.narrow(ref, StressSFSBHome.class);
        System.out.println("LoadGenerator got home: " + home.getClass());
    }

    private InitialContext getContext(String propsFileName)
        throws Exception
    {
        InitialContext ic;

        if( propsFileName == null ) {
            ic = new InitialContext();
        } else {
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(propsFileName);
            props.load(fis);
            ic = new InitialContext(props);
        }

        return ic;
    }


    public void doTest() {
        for (int i=0; i<10; i++) {
            System.out.println("Creating StressSFSBClient[" + i + "]");
            String clientName = "client-"+i;
            StressSFSBClient client = new StressSFSBClient(clientName,
                    home, 10);
        }
    }


    public static void main(String[] args) {
        try {
            stat.addDescription("ejb-sfsb-stress");
            LoadGenerator generator = new LoadGenerator(args);
            generator.doTest();
            stat.addStatus("ejb-sfsb-stress main", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("ejb-sfsb-stress main", stat.FAIL);
        }
    }

}
