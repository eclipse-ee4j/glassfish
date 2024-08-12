/*
 * Copyright (c) 2006, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.i18n.StringManager;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import javax.management.MBeanServerConnection;


class SummaryReporter {

    private final MBeanServerConnection mbsc;
    private final StringManager sm = StringManager.getManager(SummaryReporter.class);
    private final static String secretProperty = "module.core.status";

    public SummaryReporter(final MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    public String getSummaryReport() throws RuntimeException {
        try {
            final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
            final OperatingSystemMXBean os = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                    ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, OperatingSystemMXBean.class);
            sb.append(getOSInfo(os));
            final RuntimeMXBean rt = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                    ManagementFactory.RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
            sb.append(getVMInfo(rt));
            return ( sb.toString(secretProperty) );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getOSInfo(final OperatingSystemMXBean os) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append(sm.getString("os.info"));
        sb.append(sm.getString("os.name", os.getName()));
        sb.append(sm.getString("os.arch", os.getArch(), os.getVersion()));
        sb.append(sm.getString("os.nproc", os.getAvailableProcessors()));
        sb.append(sm.getString("os.load", getSystemLoad(os)));
        return ( sb.toString() );
    }
    private String getVMInfo(final RuntimeMXBean rt) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        sb.append(sm.getString("rt.info", rt.getName()));
        if (rt.isBootClassPathSupported()) {
            sb.append(sm.getString("rt.bcp", rt.getBootClassPath()));
        }
        sb.append(sm.getString("rt.cp", rt.getClassPath()));
        sb.append(sm.getString("rt.libpath", rt.getLibraryPath()));
        sb.append(sm.getString("rt.nvv", rt.getVmName(), rt.getVmVendor(), rt.getVmVersion()));
        sb.append(getProperties(rt));
        return ( sb.toString() );
    }
    private String getProperties(final RuntimeMXBean rt) {
        final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
        final Map<String, String> unsorted = rt.getSystemProperties();
        // I decided to sort this for better readability -- 27 Feb 2006
        final TreeMap<String, String> props = new TreeMap<String, String>(unsorted);
        sb.append(sm.getString("rt.sysprops"));
        for (Map.Entry<String, String> entry : props.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(filterForbidden(entry.getKey(), entry.getValue()));
        }
        return ( sb.toString() );
    }

    private String getSystemLoad(OperatingSystemMXBean os) {
        //available only on 1.6
        String info = ThreadMonitor.NA;
        try {
            String METHOD = "getSystemLoadAverage";
            Method m = os.getClass().getMethod(METHOD, (Class[]) null);
            if (m != null) {
                Object ret = m.invoke(os, (Object[])null);
                return ( ret.toString() );
            }
        } catch(Exception e) {

        }
        return ( info );
    }

    private String filterForbidden(String key, String value) {
        if(StringUtils.ok(key) && key.startsWith("javax.net.ssl.") && key.indexOf("password") >= 0)
            return "********";
        else
            return value;
    }
}
