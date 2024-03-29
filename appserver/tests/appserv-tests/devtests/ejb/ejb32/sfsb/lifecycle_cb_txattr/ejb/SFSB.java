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
import jakarta.persistence.*;
import jakarta.annotation.*;

import javax.naming.InitialContext;

@Stateful
@LocalBean
public class SFSB implements Hello {

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void init() {
        System.out.println("In SFSB::init()");
(new Exception("init")).printStackTrace();
    }

    public String test() throws EJBException {
        System.out.println("In SFSB::test()");
        return "SFSB";
    }

    @PreDestroy
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void destroy() {
        System.out.println("In SFSB::destroy()");
(new Exception("destroy")).printStackTrace();
    }
}
