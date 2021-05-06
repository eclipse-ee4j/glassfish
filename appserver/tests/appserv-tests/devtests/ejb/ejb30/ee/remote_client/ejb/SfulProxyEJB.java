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

package com.sun.s1asdev.ejb.ejb30.ee.remote_client;

import jakarta.ejb.Stateful;
import jakarta.ejb.EJB;
import javax.naming.InitialContext;

import com.sun.s1asdev.ejb.ejb30.ee.remote_sfsb.SfulDriver;

@Stateful
@EJB(name="ejb/Delegate",
        mappedName="corbaname:iiop:localhost:3700#mapped_jndi_name_for_SfulDriver",
        beanInterface=SfulDriver.class)
public class SfulProxyEJB
        implements SfulProxy {

    private SfulDriver delegate;

    public boolean initialize() {

/*
        try {
            InitialContext ctx = new InitialContext();
            delegate = (SfulDriver) ctx.lookup("mappedSfulDriver__3_x_Internal_RemoteBusinessHome__");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            InitialContext ctx = new InitialContext();
            delegate = (SfulDriver) ctx.lookup("mappedSfulDriver");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
*/

        try {
            InitialContext ctx = new InitialContext();
            delegate = (SfulDriver) ctx.lookup("java:comp/env/ejb/Delegate");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return (delegate != null);
    }

    public String sayHello() {
        return delegate.sayHello();
    }

    public String sayRemoteHello() {
        return delegate.sayRemoteHello();
    }

    public void doCheckpoint() {
        delegate.doCheckpoint();
    }

}
