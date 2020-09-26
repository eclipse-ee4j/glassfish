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

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJBContext;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import test.ejb.SFHello1;
import test.ejb.StatefulTest;

/**
 * @author Shing Wai Chan
 */
@TransactionAttribute(value=TransactionAttributeType.SUPPORTS)
@Stateful
@Remote({SFHello1.class})
@RunAs("staff")
public class StatefulTest2 extends StatefulTest implements SFHello1 {
    private EJBContext ejbContext;
    private EJBContext ejbContext2;

    public StatefulTest2() {
    }

    @TransactionAttribute(value=TransactionAttributeType.MANDATORY)
    public String sayHello(String message) {
        return super.sayHello(message);
    }

    @RolesAllowed("member")
    public String sayBye(String message) {
        return "Good bye, " + message + "!";
    }

    @Resource(name="sfContext")
    private void setEjbContext(EJBContext context) {
        ejbContext = context;
    }

    @Resource(name="sfContext2")
    void setEjbContext2(EJBContext context) {
        ejbContext = context;
    }
}
