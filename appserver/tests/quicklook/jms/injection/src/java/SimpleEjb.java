/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.jms.injection;

import jakarta.ejb.*;
import jakarta.inject.Inject;
import jakarta.jms.*;

/**
 * @author David Zhao
 */
@Stateless
public class SimpleEjb {

    @Inject
    @JMSConnectionFactory("java:comp/DefaultJMSConnectionFactory")
    @JMSSessionMode(JMSContext.AUTO_ACKNOWLEDGE)
    private JMSContext jmsContext;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public boolean testRequestScope() {
        boolean result = false;
        try {
            if (jmsContext != null) {
                String jc = jmsContext.toString();
                if (jc != null) {
                    if (jc.indexOf("RequestScoped") > -1)
                        result = true;
                    else
                        System.out.println("ERROR: Unexpected injected JMSContext instance: " + jc);
                } else {
                    System.out.println("ERROR: JMSContext.toString() is null.");
                }
            } else {
                System.out.println("ERROR: Injected JMSContext is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean testTransactionScope() {
        boolean result = false;
        try {
            if (jmsContext != null) {
                String jc = jmsContext.toString();
                if (jc != null) {
                    if (jc.indexOf("TransactionScoped") > -1)
                        result = true;
                    else
                        System.out.println("ERROR: Unexpected injected JMSContext instance: " + jc);
                } else {
                    System.out.println("ERROR: JMSContext.toString() is null.");
                }
            } else {
                System.out.println("ERROR: Injected JMSContext is null.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
