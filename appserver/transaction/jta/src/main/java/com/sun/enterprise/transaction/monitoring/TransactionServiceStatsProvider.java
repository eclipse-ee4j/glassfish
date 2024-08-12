/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.monitoring;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.api.TransactionAdminBean;

import java.util.List;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.statistics.CountStatistic;
import org.glassfish.external.statistics.StringStatistic;
import org.glassfish.external.statistics.impl.CountStatisticImpl;
import org.glassfish.external.statistics.impl.StringStatisticImpl;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 * Collects the Transaction Service monitoring data and provides it to the callers.
 *
 * @author Marina Vatkina
 */
@AMXMetadata(type = "transaction-service-mon", group = "monitoring")
@ManagedObject
@Description("Transaction Service Statistics")
public class TransactionServiceStatsProvider {

    private static final int COLUMN_LENGTH = 25;
    private static final String LINE_BREAK = "%%%EOL%%%";

    private CountStatisticImpl activeCount = new CountStatisticImpl("ActiveCount", "count",
            "Provides the number of transactions that are currently active.");

    private CountStatisticImpl committedCount = new CountStatisticImpl("CommittedCount", "count",
            "Provides the number of transactions that have been committed.");

    private CountStatisticImpl rolledbackCount = new CountStatisticImpl("RolledbackCount", "count",
            "Provides the number of transactions that have been rolled back.");

    private StringStatisticImpl inflightTransactions = new StringStatisticImpl("ActiveIds", "List",
            "Provides the IDs of the transactions that are currently active a.k.a. in-flight "
                    + "transactions. Every such transaction can be rolled back after freezing the transaction " + "service.");

    private StringStatisticImpl state = new StringStatisticImpl("State", "String", "Indicates if the transaction service has been frozen.");

    private boolean isFrozen = false;

    private JavaEETransactionManager txMgr;

    private Logger _logger;

    public TransactionServiceStatsProvider(JavaEETransactionManager tm, Logger l) {
        txMgr = tm;
        _logger = l;
    }

    @ManagedAttribute(id = "activecount")
    @Description("Provides the number of transactions that are currently active.")
    public CountStatistic getActiveCount() {
        return activeCount;
    }

    @ManagedAttribute(id = "committedcount")
    @Description("Provides the number of transactions that have been committed.")
    public CountStatistic getCommittedCount() {
        return committedCount;
    }

    @ManagedAttribute(id = "rolledbackcount")
    @Description("Provides the number of transactions that have been rolled back.")
    public CountStatistic getRolledbackCount() {
        return rolledbackCount;
    }

    @ManagedAttribute(id = "state")
    @Description("Indicates if the transaction service has been frozen.")
    public StringStatistic getState() {
        state.setCurrent((isFrozen) ? "True" : "False");
        return state;
    }

    @ManagedAttribute(id = "activeids")
    @Description("List of inflight transactions.")
    public StringStatistic getActiveIds() {

        if (txMgr == null) {
            _logger.warning("transaction.monitor.tm_null");
            inflightTransactions.setCurrent("");
            return inflightTransactions;
        }

        List aList = txMgr.getActiveTransactions();
        StringBuffer strBuf = new StringBuffer(1024);
        if (!aList.isEmpty()) {
            // Set the headings for the tabular output
            int componentNameLength = COLUMN_LENGTH;
            int txIdLength = COLUMN_LENGTH + 15;
            for (int i = 0; i < aList.size(); i++) {
                TransactionAdminBean txnBean = (TransactionAdminBean) aList.get(i);
                String componentName = txnBean.getComponentName();
                if (componentName.length() > componentNameLength) {
                    componentNameLength = componentName.length() + 1;
                }
                String txnId = txnBean.getId();
                if (txnId.length() > txIdLength) {
                    txIdLength = txnId.length() + 1;
                }
            }
            if (aList.size() > 0) {

                strBuf.append(LINE_BREAK).append(LINE_BREAK);
                appendColumn(strBuf, "Transaction Id", txIdLength);
                appendColumn(strBuf, "Status", COLUMN_LENGTH);
                appendColumn(strBuf, "ElapsedTime(ms)", COLUMN_LENGTH);
                appendColumn(strBuf, "ComponentName", componentNameLength);
                strBuf.append("ResourceNames ").append(LINE_BREAK);
            }

            for (int i = 0; i < aList.size(); i++) {
                TransactionAdminBean txnBean = (TransactionAdminBean) aList.get(i);
                String txnId = txnBean.getId();

                _logger.fine("=== Processing txnId: " + txnId);
                appendColumn(strBuf, txnId, txIdLength);
                appendColumn(strBuf, txnBean.getStatus(), COLUMN_LENGTH);
                appendColumn(strBuf, String.valueOf(txnBean.getElapsedTime()), COLUMN_LENGTH);
                appendColumn(strBuf, txnBean.getComponentName(), componentNameLength);

                List<String> resourceList = txnBean.getResourceNames();
                if (resourceList != null) {
                    for (int k = 0; k < resourceList.size(); k++) {
                        if (k != 0)
                            strBuf.append(",");
                        strBuf.append(resourceList.get(k));
                    }
                }
                strBuf.append(LINE_BREAK);
            }
        }

        _logger.fine("Prepared inflightTransactions text: \n" + strBuf);

        inflightTransactions.setCurrent(strBuf.toString());
        return inflightTransactions;
    }

    @ProbeListener("glassfish:transaction:transaction-service:activated")
    public void transactionActivatedEvent() {
        _logger.fine("=== transaction-service active ++");
        activeCount.increment();
    }

    @ProbeListener("glassfish:transaction:transaction-service:deactivated")
    public void transactionDeactivatedEvent() {
        _logger.fine("=== transaction-service active --");
        activeCount.decrement();
    }

    @ProbeListener("glassfish:transaction:transaction-service:committed")
    public void transactionCommittedEvent() {
        _logger.fine("=== transaction-service committed ++");
        committedCount.increment();
        activeCount.decrement();
    }

    @ProbeListener("glassfish:transaction:transaction-service:rolledback")
    public void transactionRolledbackEvent() {
        _logger.fine("=== transaction-service rolledback ++");
        rolledbackCount.increment();
        activeCount.decrement();
    }

    @ProbeListener("glassfish:transaction:transaction-service:freeze")
    public void freezeEvent(@ProbeParam("isFrozen") boolean b) {
        isFrozen = b;
    }

    private void appendColumn(StringBuffer buf, String text, int length) {
        buf.append(text);
        for (int i = text.length(); i < length; i++) {
            buf.append(" ");
        }
    }
}
