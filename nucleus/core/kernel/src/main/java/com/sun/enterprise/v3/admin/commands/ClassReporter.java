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

import com.sun.enterprise.util.i18n.StringManager;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServerConnection;

/**
 */
class ClassReporter {

    private final MBeanServerConnection mbsc;
    private final StringManager sm = StringManager.getManager(ClassReporter.class);
    public ClassReporter(final MBeanServerConnection mbsc) {
        this.mbsc = mbsc;
    }
    public String getClassReport() throws RuntimeException {
        try {
            final StringBuilderNewLineAppender sb = new StringBuilderNewLineAppender(new StringBuilder());
            final ClassLoadingMXBean clmb = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                    ManagementFactory.CLASS_LOADING_MXBEAN_NAME, ClassLoadingMXBean.class);
            sb.append(sm.getString("classloading.info"));
            sb.append(sm.getString("classes.loaded", clmb.getLoadedClassCount()));
            sb.append(sm.getString("classes.total", clmb.getTotalLoadedClassCount()));
            sb.append(sm.getString("classes.unloaded", clmb.getUnloadedClassCount()));

            final CompilationMXBean cmb = ManagementFactory.newPlatformMXBeanProxy(mbsc,
                    ManagementFactory.COMPILATION_MXBEAN_NAME, CompilationMXBean.class);
            sb.append(sm.getString("complilation.info"));
            sb.append(sm.getString("compilation.monitor.status", cmb.isCompilationTimeMonitoringSupported()));
            sb.append(sm.getString("jit.compilar.name", cmb.getName()));
            sb.append(sm.getString("compilation.time", JVMInformationCollector.millis2HoursMinutesSeconds(cmb.getTotalCompilationTime())));
            return ( sb.toString() );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
