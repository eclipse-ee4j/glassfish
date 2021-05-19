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

package com.sun.appserv.sqe.security.wss.annotations;


import jakarta.jws.WebService;
import jakarta.jws.WebMethod;

/**
 * FedTax WebService endpoint using the Java EE 5 annotations.
 * Used for testing Web Services Security tests on Java EE 5 platform.
 *
 * @version 1.1  08 Aug 2005
 * @author Jagadesh Munta
 */

@WebService(
        name="Tax",
        serviceName="TaxService",
        targetNamespace="http://sun.com/appserv/sqe/security/taxws"
)
public class Tax {

    private static final double FED_TAX_RATE = 0.2;

    public Tax() {
    }

    @WebMethod(operationName="getFedTax", action="urn:GetFedTax")
    public double getFedTax(double income, double deductions) {
        return ((income -  deductions) * FED_TAX_RATE);
    }


}
