/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.sqe.security.wss.ejbws.taxcal;


import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import java.rmi.RemoteException;

/**
 * FedTax EJB WebService Bean implementation. Using in Web Services Security tests.
 *
 * @version 1.1  03 May 2004
 * @author Jagadesh Munta
 */

public class FedTaxEjb implements SessionBean {

    /**
     * Implements the service business method getFedTax as defined in the
     * Service Interface.
     */
    private SessionContext sc;
    private static final double FED_TAX_RATE = 0.2;

    public FedTaxEjb() {
    }

    public void ejbCreate() throws RemoteException {
        System.out.println("In FedTaxEjb::ejbCreate !!");
    }

    // Business service method implementation.
    public double getFedTax(double income, double deductions) {
        return ((income -  deductions) * FED_TAX_RATE);
    }

    public void setSessionContext(SessionContext sc) {
        this.sc = sc;
    }

    public void ejbRemove() throws RemoteException {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

}
