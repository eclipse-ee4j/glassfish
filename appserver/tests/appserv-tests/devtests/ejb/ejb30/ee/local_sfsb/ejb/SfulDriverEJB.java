/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.ee.local_sfsb;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateful;
import javax.naming.InitialContext;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Stateful(mappedName="AAbbCCejb/SfulLocalRemoteRef")
@EJB(name="ejb/Sful", beanInterface=Sful.class)
public class SfulDriverEJB
    implements SfulDriver {

    private Sful ref1;
    private Sful ref2;
    @EJB
    private SfulGreeter sfulGreeter;

    public String sayHello() {
        System.out.println("In SfulDriverEJB:sayHello()");
        return "Hello";
    }

    public boolean initialize() {
        boolean result = false;

        ref1 = ref2 = createSful();

        ref1.setSfulRef(ref2);
        return (ref1 != null);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean doRefAliasingTest() {
        boolean result = false;
        for (int i=0; i<10; i++) {
            ref1.incrementCounter();
        }

        return (ref1.getCounter() == ref2.getCounter());
    }

    public void doCheckpoint() {
    }

    public void checkGetRef() {
        Sful ref = ref1.getSfulRef();
        ref.getCounter();
    }

    public void createManySfulEJBs(int count) {
/*
        while (count-- > 0) {
            Sful sf = (Sful) createSful();
        }
*/
    }

    private Sful createSful() {
        Sful sful = null;
        try {
            InitialContext ctx = new InitialContext();
            sful = (Sful) ctx.lookup("java:comp/env/ejb/Sful");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return sful;
    }

    public boolean useSfulGreeter() {
        return (sfulGreeter.getCounter() == ref1.getCounter());
    }
}
