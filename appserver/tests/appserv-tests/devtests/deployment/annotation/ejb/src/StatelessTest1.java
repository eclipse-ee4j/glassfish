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

package test.ejb.stateless;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.Local;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.ejb.TransactionManagementType;
import jakarta.interceptor.Interceptors;

/**
 * @author Shing Wai Chan
 */
@Stateless(name="myStatelessTest1")
@Local({SLHello.class})
@TransactionAttribute(value=TransactionAttributeType.REQUIRED)
@RolesAllowed(value={"staff"})
@RunAs(value="user")
@Interceptors(InterceptorB.class)
public class StatelessTest1 implements SLHello {
    public StatelessTest1() {
    }

    @RolesAllowed(value={"j2ee", "staff"})
    public String sayHello(String message) {
        return "Hello, " + message + "!";
    }

    @PermitAll
    @TransactionAttribute(value=TransactionAttributeType.REQUIRES_NEW)
    public String sayGoodMorning(String message) {
        return "Good morning, " + message + "!";
    }

    public String sayGoodAfternoon(String message) {
        return "Good afternoon, " + message + "!";
    }
}
