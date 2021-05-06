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
import jakarta.interceptor.*;

import jakarta.inject.Inject;

public class Bar {

    @Inject Bar2 bar2;

    @Resource(name="jdbc/__default") javax.sql.DataSource ds;

    public Bar() {
        System.out.println("Constructed::Bar");
    }

    @PostConstruct
    public void init() {
        System.out.println("In Bar::init()");
        if( bar2 == null ) {
            throw new EJBException("bar2 is null");
        }
        if( ds == null ) {
            throw new EJBException("ds is null");
        }
        System.out.println("bar2 inject = " + bar2);
        System.out.println("ds inject = " + ds);
    }

    public String toString() {
        return "Bar";
    }

}
