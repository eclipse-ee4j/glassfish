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

package com.sun.s1asdev.ejb.ejb30.hello.session2full;

import jakarta.ejb.EJBContext;
import jakarta.ejb.SessionContext;

import java.util.Collection;
import java.util.Iterator;

public class SlessEJB3 implements SlessSub
{

    private Sful sful1;
    private Sful sful2;

    private EJBContext ejbContext;

    private SessionContext ejbContext2;

    private EJBContext ejbContext3;

    private EJBContext ejbContext4_;

    private void setEJBContext4(EJBContext context) {
        ejbContext4_ = context;
    }

    private SessionContext ejbContext5_;
    private void setEJBContext5(SessionContext context) {
        ejbContext5_ = context;
    }

    private EJBContext ejbContext6_;
    private void setEJBContext6(EJBContext context) {
        ejbContext6_ = context;
    }

    public String hello() {
        System.out.println("In SlessEJB3:hello()");

        System.out.println("Calling myself through my remote business object");
        SlessSub me = (SlessSub) ((SessionContext) ejbContext).
            getBusinessObject(SlessSub.class);
        String whoami = me.getId();
        System.out.println("i am " + whoami);

        return "hello from sless ejb3";
    }

    public String hello2() throws jakarta.ejb.CreateException {
        throw new jakarta.ejb.CreateException();
    }

    public String hello3() {

        System.out.println("in hello3()");

        System.out.println("sful1 = " + sful1);
        System.out.println("sful2 = " + sful2);

        sful1.set("1");
        sful2.set("2");
        String get1 = sful1.get();
        String get2 = sful2.get();
        if( get1.equals(get2) ) {
            System.out.println("get1 =" + get1);
            System.out.println("get2 =" +  get2);
            throw new jakarta.ejb.EJBException("SFSB get test failed");
        }

        return "hello3()";
    }

    public String getId() {
        return "SlessEJB3";
    }

    public Sless roundTrip(Sless s) {
        System.out.println("In SlessEJB3::roundTrip " + s);
        System.out.println("input Sless.getId() = " + s.getId());
        return s;
    }

    public Collection roundTrip2(Collection collectionOfSless) {
        System.out.println("In SlessEJB3::roundTrip2 " +
                           collectionOfSless);
        if( collectionOfSless.size() > 0 ) {
            Sless sless = (Sless) collectionOfSless.iterator().next();
            System.out.println("input Sless.getId() = " + sless.getId());
        }
        return collectionOfSless;
    }

}
