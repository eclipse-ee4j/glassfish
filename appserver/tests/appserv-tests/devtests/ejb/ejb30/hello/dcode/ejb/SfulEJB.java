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

package com.sun.s1asdev.ejb.ejb30.hello.dcode;

import jakarta.ejb.Stateful;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.ejb.Init;
import jakarta.ejb.RemoteHome;

@RemoteHome(SfulHome.class)
@Stateful
public class SfulEJB
{

    // reference to a Remote Business object from another application
    @EJB(mappedName="ejb_ejb30_hello_session2_Sless") com.sun.s1asdev.ejb.ejb30.hello.session2.Sless sless;

    // reference to a RemoteHome from another application
    @EJB(mappedName="ejb_ejb30_hello_session2_Sless") com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemoteHome slessHome;

    com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemote slessRemote;

    @Init
    public void create() {
        System.out.println("In SfulEJB::create");
        try {
            slessRemote = slessHome.create();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public String hello() {
        System.out.println("In SfulEJB:hello()");

        System.out.println("Calling sless");

        sless.hello();

        try {

            slessRemote.hello();

            com.sun.s1asdev.ejb.ejb30.hello.session2.SlessRemote
                anotherRemote = slessHome.create();

            anotherRemote.hello();

        } catch(Exception e) {
            e.printStackTrace();
            throw new EJBException(e);
        }

        System.out.println("Called sless.hello()");

        return "hello";
    }

}
