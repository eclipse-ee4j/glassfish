/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.xa;

//import com.sun.enterprise.transaction.monitor.JTSMonitorMBean;
//import com.sun.enterprise.admin.event.tx.TransactionsRecoveryEvent;
//import com.sun.enterprise.admin.event.tx.TransactionsRecoveryEventListener;
//import com.sun.enterprise.admin.event.AdminEventListenerException;

//import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.util.i18n.StringManager;

public class TransactionsRecoveryEventListenerImpl /** implements  TransactionsRecoveryEventListener **/ {
 
        // Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(TransactionsRecoveryEventListenerImpl.class);


   /**
    * Recovers taransactions for given instance
    * @param event - TransactionsRecoveryEvent containing data to recovery
    *
   public void processEvent(TransactionsRecoveryEvent event) throws AdminEventListenerException
   {
       // System.out.println("====>TransactionsRecoveryEventListener.processEvent"+
       //     "request for recovery transactions on server="+
       //     event.getServerName() + " logDir=" + event.getLogDir());

        String currentServer = ApplicationServer.getServerContext().getInstanceName();

        boolean delegated = (!currentServer.equals(event.getServerName()));

        //call recover method.
        try {
            JTSMonitorMBean.recover(delegated, event.getLogDir());
        } catch (Exception ex) {
            if (ex.getMessage() != null)
                throw new AdminEventListenerException(ex.getMessage());
            else
                throw new AdminEventListenerException(sm.getString("transaction.unexpected_exception_in_recover-transactions"));
        }
   }
    **/
}
