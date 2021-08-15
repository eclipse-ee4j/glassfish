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
package com.sun.ts.tests.ejb30.lite.basic.singleton;

import com.sun.ts.tests.ejb30.lite.basic.common.ClientBase;

import jakarta.ejb.EJB;

public class Client extends ClientBase {

    @EJB(beanInterface = BasicBean.class, beanName = "BasicBean")
    private void setBasicBean(BasicBean basicBean) {
        this.basicBean = basicBean;
    }

    /*
     * @testName: add
     *
     * @test_Strategy: a simple no-interface local singleton bean. The bean field
     * (basicBean, of type BasicBeanBase) is declared in superclass (ClientBase),
     * and the setter injection is in subclass (Client). Besides, one-interface
     * local singleton bean, accessed with Basic1IF; two-interface local singleton
     * bean, accessed with Basic1IF; two-interface local singleton bean, accessed
     * with Basic2IF.
     */
    /*
     * @testName: globalJNDI
     *
     * @test_Strategy: lookup portable global jndi names of various beans from web
     * components or standalone client.
     */
    /*
     * @testName: globalJNDI2
     *
     * @test_Strategy: lookup portable global jndi names of various beans from ejb
     * bean class
     */
    /*
     * @testName: appJNDI
     *
     * @test_Strategy: lookup portable app jndi names of various beans from web
     * component client (not standalone client).
     */

    /*
     * @testName: appJNDI2
     *
     * @test_Strategy: lookup portable app jndi names of various beans from ejb
     * bean class
     */

    /*
     * @testName: moduleJNDI
     *
     * @test_Strategy: lookup portable module jndi names of various beans from web
     * component client (not standalone client).
     */

    /*
     * @testName: moduleJNDI2
     *
     * @test_Strategy: lookup portable module jndi names of various beans from ejb
     * bean class
     */
}
