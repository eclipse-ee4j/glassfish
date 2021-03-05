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

package com.sun.web.security;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 *
 * @author nithyasubramanian
 */
@AMXMetadata(type = "login-mon", group = "monitoring", isSingleton = false)
@ManagedObject
@Description("Login Statistics")
public class LoginStatsProvider {

    CountStatisticImpl successLoginCount = new CountStatisticImpl("SuccessLoginCount", "count", "No of successful logins");
    CountStatisticImpl failedLoginCount = new CountStatisticImpl("FailedLoginCount", "count", "No of failed logins");

    @ManagedAttribute
    public CountStatistic getSuccessLoginCount() {
        return successLoginCount;
    }

    @ManagedAttribute
    public CountStatistic getFailedLoginCount() {
        return failedLoginCount;
    }

    @ProbeListener("glassfish:security:login:loginSuccessfulEvent")
    public void loginSuccessfulEvent(@ProbeParam("username") String userName) {
        successLoginCount.increment();
    }

    @ProbeListener("glassfish:security:login:loginFailedEvent")
    public void loginFailedEvent(@ProbeParam("username") String userName) {
        failedLoginCount.increment();
    }

}
