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
import com.sun.ts.tests.ejb30.common.lite.EJBLiteClientBase;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBContext;

abstract public class ClientBase extends EJBLiteClientBase {
    protected static final String appName = null;

    protected BasicBeanBase basicBean;

    // beanName is needed since both OneInterfaceBasicBean and
    // TwoInterfacesBasicBean
    // implements Basic1IF
    @EJB(beanName = "OneInterfaceBasicBean", name = "oneInterfaceBasicBeanBoth")
    private Basic1IF oneInterfaceBasicBean;

    @EJB(beanName = "OneInterfaceBasicBean", name = "oneInterfaceBasicBeanBoth")
    private Basic1IF oneInterfaceBasicBeanAgain;

    @EJB(beanName = "TwoInterfacesBasicBean")
    private Basic1IF twoInterfacesBasicBean1;

    @EJB(name = "twoInterfacesBasicBean2Both", beanName = "TwoInterfacesBasicBean")
    private Basic2IF twoInterfacesBasicBean2;

    @EJB(name = "twoInterfacesBasicBean2Both", beanName = "TwoInterfacesBasicBean")
    private Basic2IF twoInterfacesBasicBean2Again;

    public void add() {
        int a = -2, b = -3;
        int expected = a + b;

        assertEquals("Verify BasicBean.add ", expected, basicBean.add(a, b));
        assertEquals("Verify OneInterfaceBasicBean.add ", expected, oneInterfaceBasicBean.add(a, b));
        assertEquals("Verify oneInterfaceBasicBeanAgain.add ", expected, oneInterfaceBasicBeanAgain.add(a, b));
        assertEquals("Verify TwoInterfaceBasicBean(1).add ", expected, twoInterfacesBasicBean1.add(a, b));
        assertEquals("Verify TwoInterfaceBasicBean(2).add ", expected, twoInterfacesBasicBean2.add(a, b));
        assertEquals("Verify twoInterfacesBasicBean2Again(2).add ", expected, twoInterfacesBasicBean2Again.add(a, b));
    }

    public void globalJNDI() {
        BasicBeanHelper.globalJNDI(appName, getModuleName(), basicBean.getBusinessInterface(), getReasonBuffer(),
            (EJBContext) null, getContext());
    }

    public void globalJNDI2() {
        appendReason(basicBean.globalJNDI(appName, getModuleName()));
    }

    public void appJNDI() {
        if (getContainer() != null) {
            return;
        }

        BasicBeanHelper.appJNDI(getModuleName(), basicBean.getBusinessInterface(), getReasonBuffer());
    }

    public void appJNDI2() {
        appendReason(basicBean.appJNDI(getModuleName()));
    }

    public void moduleJNDI() {
        if (getContainer() != null) {
            return;
        }

        BasicBeanHelper.moduleJNDI(basicBean.getBusinessInterface(), getReasonBuffer());
    }

    public void moduleJNDI2() {
        appendReason(basicBean.moduleJNDI());
    }

    @PostConstruct
    private void postConstruct() {
        Helper.getLogger().info("In postConstruct of " + this);
        assertNotEquals("check oneInterfaceBasicBean not null", null, oneInterfaceBasicBean);
        assertNotEquals("check oneInterfaceBasicBeanAgain not null", null, oneInterfaceBasicBeanAgain);
        assertNotEquals("check twoInterfacesBasicBean1 not null", null, twoInterfacesBasicBean1);
        assertNotEquals("check twoInterfacesBasicBean2 not null", null, twoInterfacesBasicBean2);
        assertNotEquals("check twoInterfacesBasicBean2Again not null", null, twoInterfacesBasicBean2Again);
    }

    @PreDestroy
    private void preDestroy() {
        Helper.getLogger().info("In preDestroy of " + this);
    }
}
