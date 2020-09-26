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

package test.ejb.stateful;

import jakarta.ejb.EJB;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateful;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;

import test.ejb.stateless.*;

/**
 * @author Shing Wai Chan
 */
@TransactionManagement(value=TransactionManagementType.BEAN)
@Stateful(name="myStatefulTest1")
@Remote({SFHello.class})
public class StatefulTest1 implements SFHello {
    @EJB(beanName="myStatelessTest1") private SLHello slHello1;
    @EJB(beanName="myStatelessTest1") private SLHello slHello2;

    public StatefulTest1() {
    }
}
