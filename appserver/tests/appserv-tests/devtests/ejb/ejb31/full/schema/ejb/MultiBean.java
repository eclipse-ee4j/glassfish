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
import javax.naming.InitialContext;
import jakarta.annotation.*;

@Singleton
@Startup
public class MultiBean {

    @Resource(name="multi")
    private String multi;

    @PostConstruct
    private void init() {
        System.out.println("In init()");
        System.out.println("multi = " + multi);
        try {
            String multiLookup = (String)
                new InitialContext().lookup("java:comp/env/multi");
            if( !multi.equals(multiLookup) ) {
                throw new EJBException("Non-matching values of multi" +
                                       multi + " : " + multiLookup);
            }

        } catch(Exception e) {
            throw new EJBException(e);
        }
    }

    public String foo() {
        try {
            String multiLookup = (String)
                new InitialContext().lookup("java:comp/env/multi");
            System.out.println("multiLookup = " + multiLookup);
            if( !multi.equals(multiLookup) ) {
                throw new EJBException("Non-matching values of multi" +
                                       multi + " : " + multiLookup);
            }
        } catch(Exception e) {
            throw new EJBException(e);
        }
        return multi;
    }

}
