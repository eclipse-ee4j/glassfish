/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.security.jsp2sful;

import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;

/**
 * @author hsingh
 */
public class ProfileInfoBean implements SessionBean {

    private static final long serialVersionUID = 1L;

    private SessionContext context;

    /** Creates a new instance of ProfieInfo */
    public void ejbCreate(String name) {
    }


    public String getCallerInfo() {
        return context.getCallerPrincipal().getName();
    }


    public String getSecretInfo() {
        return "Keep It Secret!";
    }


    @Override
    public void ejbActivate() {
        System.out.println("In ShoppingCart ejbActivate");
    }


    @Override
    public void ejbPassivate() {
        System.out.println("In ShoppingCart ejbPassivate");
    }


    @Override
    public void ejbRemove() {
        System.out.println("In ShoppingCart ejbRemove");
    }


    @Override
    public void setSessionContext(jakarta.ejb.SessionContext sessionContext) {
        context = sessionContext;
    }
}
