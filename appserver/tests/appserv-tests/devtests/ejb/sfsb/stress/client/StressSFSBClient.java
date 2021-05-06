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

import java.util.ArrayList;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class StressSFSBClient
    implements Runnable
{

    String            name;
    StressSFSBHome  home;
    ArrayList            list;
    int                    maxActiveCount;
    boolean            success = true;
    int                    maxIter = 5;
    Thread            thread;

    public StressSFSBClient(String name,
            StressSFSBHome home, int maxActiveCount)
    {
        thread = new Thread(this, name);
        this.name = name;
        this.home = home;
        this.maxActiveCount = maxActiveCount;
        this.list = new ArrayList(maxActiveCount);
        thread.start();
    }

    public void run() {
        System.out.println("StressSFSBClient: " + name + " started....");
        try {
            for (int i=0; i<maxActiveCount; i++) {
                list.add(home.create(name+"-"+i));
            }
            for (int count = 0; count < maxIter; count++) {
                for (int i=0; i<maxActiveCount; i++) {
                    StressSFSB sfsb = (StressSFSB) list.get(i);
                    sfsb.ping();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            success = false;
        }
    }

}
