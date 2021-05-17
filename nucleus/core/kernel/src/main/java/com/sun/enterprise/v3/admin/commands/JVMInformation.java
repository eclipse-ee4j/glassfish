/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import javax.management.MBeanServerConnection;

/**
 */
public class JVMInformation  implements JVMInformationMBean { //, MBeanRegistration TODO
    private final MBeanServerConnection mbsc;
    private final ThreadMonitor tm;
    private final SummaryReporter sr;
    private final MemoryReporter mr;
    private final ClassReporter cr;
    private final LogReporter lr;

    public JVMInformation(MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
        tm = new ThreadMonitor(mbsc);
        sr = new SummaryReporter(mbsc);
        mr = new MemoryReporter(mbsc);
        cr = new ClassReporter(mbsc);
        lr = new LogReporter();
    }
    public String getThreadDump(final String processName) {
        return ( tm.getThreadDump() );
    }

    public String getSummary(final String processName) {
        return ( sr.getSummaryReport() );
    }

    public String getMemoryInformation(final String processName) {
        return ( mr.getMemoryReport() );
    }

    public String getClassInformation(final String processName) {
        return ( cr.getClassReport() );
    }

    public String getLogInformation(final String processName) {
        return (lr.getLoggingReport());
    }
    /* //TODO
    public void postRegister(Boolean registrationDone) {
    }

    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        this.mbsc = server;
        final String sn = System.getProperty(SystemPropertyConstants.SERVER_NAME);
        final ObjectName on = JVMInformationCollector.formObjectName(sn, JVMInformation.class.getSimpleName());
        return ( on );
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }
    */
}
