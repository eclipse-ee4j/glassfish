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

package ejb32.methodintf;

import javax.naming.InitialContext;

public class Verifier {
    static boolean verify_tx(boolean op) {
        boolean valid = true;
        try {
            jakarta.transaction.TransactionSynchronizationRegistry r = (jakarta.transaction.TransactionSynchronizationRegistry)
                   new javax.naming.InitialContext().lookup("java:comp/TransactionSynchronizationRegistry");
            System.out.println("========> TX Status for " + op + " : " + r.getTransactionStatus());
            if (op && r.getTransactionStatus() != jakarta.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: NON-Active transaction");
                valid = false;
            } else if (!op && r.getTransactionStatus() == jakarta.transaction.Status.STATUS_ACTIVE) {
                System.out.println("ERROR: Active transaction");
                valid = false;
            }
        } catch(Exception e) {
            System.out.println("handleEjbTimeout threw exception");
            e.printStackTrace();
            valid = false;
        }

        return valid;
    }

}
