/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.admin.mbeanapi.monitoring.http;

import com.sun.appserv.management.monitor.HTTPServiceMonitor;
import com.sun.appserv.management.monitor.HTTPServiceVirtualServerMonitor;
import java.io.IOException;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;

import com.sun.appserv.management.monitor.ApplicationMonitor;
import com.sun.appserv.management.monitor.WebModuleMonitor;
import com.sun.appserv.management.monitor.ServletMonitor;
import com.sun.appserv.management.monitor.WebModuleVirtualServerMonitor;
import com.sun.appserv.management.monitor.NativeWebCoreVirtualServerRequestMonitor;
import com.sun.appserv.management.monitor.statistics.NativeWebCoreVirtualServerRequestStats;

import com.sun.appserv.management.util.misc.ExceptionUtil;

import com.sun.appserv.management.util.stringifier.SmartStringifier;
import com.sun.appserv.management.util.stringifier.StringifierRegistryIniterImpl;
import com.sun.appserv.management.util.stringifier.StringifierRegistryImpl;

import com.sun.enterprise.admin.mbeanapi.common.AMXConnector;
import com.sun.enterprise.admin.mbeanapi.common.AMXMonitoringTestBase;



/**
 * This test prints the statistics for ejb module and stateless bean
 *
 *  HTTPMonitoring</B>
 *
 * @author <a href=mailto:satish.viswanatham@sun.com>Satish Viswanatham</a>
 *         Date: Aug 24, 2004
 * @version $Revision: 1.1 $
 */
public class HTTPMonitoring extends AMXMonitoringTestBase {

    private String serverName;
    private static String SERVLETS = " - SERVLETS -";
    private static String VS = "VIRTUAL SERVER";

    public HTTPMonitoring(final String host, final int port, final String serverName,
            final String adminUser, final String adminPassword,
            final boolean useTLS) throws IOException {
        super(host, port, adminUser,adminPassword,useTLS);
        this.serverName = serverName;
    }


    public void  test() {
        HTTPServiceMonitor httpService =
            getServerRootMonitor(serverName).getHTTPServiceMonitor();
        assert (httpService != null) : "The http service monitor is null!";
        Map vsMap = httpService.getHTTPServiceVirtualServerMonitorMap();
        assert (vsMap.size() > 0) : "The http service monitor map is empty!";
        for(Iterator itr = vsMap.values().iterator(); itr.hasNext();) {
            HTTPServiceVirtualServerMonitor vsMtr =
                (HTTPServiceVirtualServerMonitor) vsMap.get(itr.next());
            assert (vsMtr != null) : "The http virtual server monitor is null!";
            NativeWebCoreVirtualServerRequestMonitor rMtr =
                vsMtr.getNativeWebCoreVirtualServerRequestMonitor();
            NativeWebCoreVirtualServerRequestStats wcStats =
                rMtr.getNativeWebCoreVirtualServerRequestStats();
            printStats(wcStats.getStatistics());
        }
    }


    /**
     *
     */
    public static void   main( final String[] args ) {
        new StringifierRegistryIniterImpl( StringifierRegistryImpl.DEFAULT );

        try {
            HTTPMonitoring httpMtr = new HTTPMonitoring(
                System.getProperty("HOST", "localhost"),
                Integer.parseInt(System.getProperty("PORT","8686")), args[0],
                System.getProperty("ADMIN_USER", "admin"),
                System.getProperty("ADMIN_PASSWORD", "adminadmin"),
                Boolean.getBoolean(System.getProperty("USE_TLS", "false")));

            HTTPMonitoring.printArgs(args);

            httpMtr.test();

        } catch( Throwable t ) {
            ExceptionUtil.getRootCause( t ).printStackTrace();
        }
    }

}
