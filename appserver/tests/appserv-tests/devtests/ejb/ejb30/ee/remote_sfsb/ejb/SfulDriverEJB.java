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

package com.sun.s1asdev.ejb.ejb30.ee.remote_sfsb;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBs;
import jakarta.ejb.Stateful;
import javax.naming.InitialContext;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

@Stateful(mappedName="jndi_SfulDriver")
@EJBs({
  @EJB(name="ejb/RemoteSful", beanInterface=RemoteSful.class)
})
public class SfulDriverEJB
    implements SfulDriver, SfulFacade {

    @EJB
    private RemoteSful remoteSful;

    public String sayHello() {
        return "Hello";
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String sayRemoteHello() {
        return remoteSful.sayNamaste();
    }


    public void doCheckpoint() {
    }

}
