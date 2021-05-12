/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.flashlight.client;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;

/**
 * @author Mahesh Kannan
 *         Date: May 30, 2008
 */
public class ProbeClient {

    @ProbeListener("glassfish:ejb:ejb-container:onDeploy")
    public void fooOnDeploy2(@ProbeParam("appName") String param1, @ProbeParam("beanName") String param2) {
        System.out.println("Got callback: " + param1 + " : " + param2);
    }

    @ProbeListener("glassfish:ejb:ejb-container:onDeploy")
    public void fooOnDeploy1(@ProbeParam("appName") String param1, @ProbeParam("beanName") String param2) {
        System.out.println("Got callback: " + param1 + " : " + param2);
    }

    @ProbeListener("glassfish:ejb:ejb-container:onDeploy")
    public void fooOnDeploy(@ProbeParam("appName") String param1, @ProbeParam("beanName") String param2) {
        System.out.println("Got callback: " + param1 + " : " + param2);
    }

    @ProbeListener("glassfish:ejb:ejb-container:onCreate")
    public void fooOnCreate(@ProbeParam("appName") String param1, @ProbeParam("beanName") String param2) {
        System.out.println("Got callback: " + param1 + " : " + param2);
    }

    @ProbeListener("glassfish:ejb:ejb-container:onShutdown")
    public void fooOnShutdown(@ProbeParam("appName") String param1, @ProbeParam("beanName") String param2) {
        System.out.println("Got callback: " + param1 + " : " + param2);
    }

    @ProbeListener("glassfish:ejb:ejb-container:onUndeploy")
    public void fooOnUnDeploy(@ProbeParam("appName") String param1, @ProbeParam("beanName") String param2) {
        System.out.println("Got callback: " + param1 + " : " + param2);
    }
}
