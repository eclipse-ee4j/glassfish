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

package com.acme;

import jakarta.ejb.*;

@Stateless
public class StatelessBean {

    @EJB(beanName="MultiBean") private MultiBean multi1;
    @EJB(beanName="MultiBean2") private MultiBean multi2;
    @EJB(beanName="MultiBean3") private MultiBean multi3;

    public void foo() {

        System.out.println("In StatelessBean::foo");

               String multi1Str = multi1.foo();
        String multi2Str = multi2.foo();
        String multi3Str = multi3.foo();

               System.out.println("multi1 = " + multi1Str);
        System.out.println("multi2 = " + multi2Str);
        System.out.println("multi3 = " + multi3Str);

        if( /**!multi1Str.equals("1") || **/
            !multi2Str.equals("2") ||
            !multi3Str.equals("3") ) {
            throw new EJBException("Invalid multi values");

        }


    }

}
