/*
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * $Id$
 */

package com.sun.ts.tests.ejb30.lite.basic.common;

import com.sun.ts.tests.ejb30.common.helper.Helper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.SessionContext;

abstract public class BasicBeanBase implements Basic1IF {

    // just to verify both SessionContext and EJBContext can be injected
    @Resource
    private SessionContext sessionContext;

    @Resource
    private EJBContext ejbContext;

    public Class<?> getBusinessInterface() {
        return sessionContext.getInvokedBusinessInterface();
    }

    // If PrePassivate or PostActivate lifecycle callbacks are defined for
    // stateless session beans or singleton beans, they are ignored.

    @PostConstruct
    @PostActivate
    private void postConstruct() {
        Helper.getLogger().info("In postConstruct of " + this);
        Helper.assertNotEquals("check sessionContext not null", null, sessionContext);
        Helper.assertNotEquals("check ejbContext not null", null, ejbContext);
    }

    @PrePassivate
    private void prePassivate() {
        Helper.getLogger().info("In prePassivate of " + this);
    }

    @PreDestroy
    private void preDestroy() {
        Helper.getLogger().info("In preDestroy of " + this);
    }

    @Override
    public int add(int a, int b) {
        return a + b;
    }

    public String globalJNDI(String appName, String modName) {
        StringBuilder reason = new StringBuilder();
        BasicBeanHelper.globalJNDI(appName, modName, getBusinessInterface(), reason, ejbContext, (javax.naming.Context) null);
        return reason.toString();
    }

    public String appJNDI(String moduleName) {
        StringBuilder reason = new StringBuilder();
        BasicBeanHelper.appJNDI(moduleName, getBusinessInterface(), reason, ejbContext);
        return reason.toString();
    }

    public String moduleJNDI() {
        StringBuilder reason = new StringBuilder();
        BasicBeanHelper.moduleJNDI(getBusinessInterface(), reason, ejbContext);
        return reason.toString();
    }
}
