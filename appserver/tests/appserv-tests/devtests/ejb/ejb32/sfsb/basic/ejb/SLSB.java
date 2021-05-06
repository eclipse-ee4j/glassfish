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
import jakarta.annotation.*;

@Stateless
public class SLSB implements Hello2 {

    @EJB private SFSB bean;

    public void testRemove() {
        System.out.println("In SLSB::testRemove()");
        bean.test("XYZ", 0);
        bean.testRemove();
        try {
                bean.foo();
                throw new RuntimeException("Removed bean got invoked!");
        } catch(NoSuchEJBException nse) {
                System.out.println("SFSB has been removed, subsequent invocation got exception");
        }
    }

}
