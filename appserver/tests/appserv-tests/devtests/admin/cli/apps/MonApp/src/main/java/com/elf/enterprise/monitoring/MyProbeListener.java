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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.elf.enterprise.monitoring;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.ManagedAttribute;

public class MyProbeListener {
    private static final String SERVLET_REQUEST_COUNT_DESCRIPTION =
        "Cumulative value of the servlet request count";

     private CountStatisticImpl servletRequestCount = new CountStatisticImpl("ServletRequestCount",
            CountStatisticImpl.UNIT_COUNT, SERVLET_REQUEST_COUNT_DESCRIPTION);

    @ManagedAttribute
    public CountStatistic getServletRequestCount(){
         return servletRequestCount;
    }

    @ProbeListener("fooblog:samples:ProbeServlet:myProbe")
    public void probe(String s) {
        servletRequestCount.increment();
        System.out.println("PROBE LISTENER HERE.  Called with this arg: " + s);
    }

    @ProbeListener("fooblog:samples:ProbeInterface:myProbe2")
    public void probe2(String s1, String s2) {
        System.out.println("PROBE INTERFACE LISTENER HERE.  Called with thes args: " + s1 + ", " + s2);
    }

}
