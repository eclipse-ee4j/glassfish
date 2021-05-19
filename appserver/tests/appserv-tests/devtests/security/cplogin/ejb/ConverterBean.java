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

package com.sun.devtest.security.plogin.converter.ejb;

import java.rmi.RemoteException;
import jakarta.ejb.SessionBean;
import jakarta.ejb.SessionContext;
import java.math.*;

/**
 * A simple stateless bean for the Converter application. This bean implements all
 * business method as declared by the remote interface, <code>Converter</code>.
 *
 * @see Converter
 * @see ConverterHome
 */
public class ConverterBean implements SessionBean {

    BigDecimal yenRate = new BigDecimal("121.6000");
    BigDecimal euroRate = new BigDecimal("0.0077");
    SessionContext mysc = null;

    public String myCallerPrincipal(){
        return mysc.getCallerPrincipal().toString();
    }
    /**
     * Returns the yen value for a given dollar amount.
     * @param dollars dollar amount to be converted to yen.
     */
    public BigDecimal dollarToYen(BigDecimal dollars) {
        BigDecimal result = dollars.multiply(yenRate);
        return result.setScale(2,BigDecimal.ROUND_UP);
    }

    /**
     * Returns the euro value for a given yen amount.
     * @param yen yen amount to be converted to euro.
     */
    public BigDecimal yenToEuro(BigDecimal yen) {
        BigDecimal result = yen.multiply(euroRate);
        return result.setScale(2,BigDecimal.ROUND_UP);
    }

    /**
     * Required by EJB spec.
     */
    public ConverterBean() {}

    /**
     * Creates a bean. Required by EJB spec.
     * @exception throws CreateException.
     */
    public void ejbCreate() {}

    /**
     * Removes the bean. Required by EJB spec.
     */
    public void ejbRemove() {}

    /**
     * Loads the state of the bean from secondary storage. Required by EJB spec.
     */
    public void ejbActivate() {}

    /**
     * Keeps the state of the bean to secondary storage. Required by EJB spec.
     */
    public void ejbPassivate() {}

    /**
     * Sets the session context. Required by EJB spec.
     * @param ctx A SessionContext object.
     */
   public void setSessionContext(SessionContext sc) {
        mysc = sc;
   }

} // ConverterBean
