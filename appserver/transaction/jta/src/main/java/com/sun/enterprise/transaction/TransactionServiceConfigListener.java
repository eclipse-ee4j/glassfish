/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ModuleMonitoringLevels;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.config.TransactionService;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.UnprocessedChangeEvent;
import org.jvnet.hk2.config.UnprocessedChangeEvents;
import org.jvnet.hk2.config.types.Property;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;

/**
 * ConfigListener class for TransactionService and TransactionService monitoring level changes
 *
 * @author Marina Vatkina
 */
@Service
public class TransactionServiceConfigListener implements ConfigListener, PostConstruct {

    private static final Logger _logger = LogDomains.getLogger(TransactionServiceConfigListener.class, LogDomains.JTA_LOGGER);

    private TransactionService ts;

    @Inject
    private ServiceLocator habitat;

    private JavaEETransactionManager javaEETransactionManager;

    // Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(TransactionServiceConfigListener.class);

    /**
     * Clears the transaction associated with the caller thread
     */
    public void setTM(JavaEETransactionManager javaEETransactionManager) {
        this.javaEETransactionManager = javaEETransactionManager;
    }

    @Override
    public void postConstruct() {
        // Listen to monitoring level changes
        Config c = habitat.getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        ts = c.getExtensionByType(TransactionService.class);
        ModuleMonitoringLevels mml = c.getMonitoringService().getModuleMonitoringLevels();
        ((ObservableBean) ConfigSupport.getImpl(mml)).addListener(this);
    }

    /****************************************************************************/
    /** Implementation of org.jvnet.hk2.config.ConfigListener *********************/
    /****************************************************************************/
    @Override
    public UnprocessedChangeEvents changed(PropertyChangeEvent[] events) {

        // Events that we can't process now because they require server restart.
        List<UnprocessedChangeEvent> unprocessedEvents = new ArrayList<UnprocessedChangeEvent>();

        for (PropertyChangeEvent event : events) {
            String eventName = event.getPropertyName();
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            boolean accepted = true;

            _logger.log(FINE, "Got TransactionService change event ==== {0} {1} {2} {3}",
                    new Object[] { event.getSource(), eventName, oldValue, newValue });

            if (oldValue != null && oldValue.equals(newValue)) {
                _logger.log(FINE, "Event {0} did not change existing value of {1}", new Object[] { eventName, oldValue });
                continue;
            }

            if (event.getSource() instanceof ModuleMonitoringLevels) {
                if (eventName.equals(ServerTags.TRANSACTION_SERVICE)) {
                    String newlevel = newValue.toString();
                    _logger.log(FINE, "Changing transaction monitoring level");
                    if ("OFF".equals(newlevel)) {
                        javaEETransactionManager.setMonitoringEnabled(false);
                    } else if ("LOW".equals(newlevel) || "HIGH".equals(newlevel)) {
                        javaEETransactionManager.setMonitoringEnabled(true);
                    }
                } // else skip
            } else if (eventName.equals(ServerTags.TIMEOUT_IN_SECONDS)) {
                try {
                    javaEETransactionManager.setDefaultTransactionTimeout(Integer.parseInt((String) newValue, 10));
                    _logger.log(FINE, " Transaction Timeout interval event processed for: {0}", newValue);
                } catch (Exception ex) {
                    _logger.log(Level.WARNING, "transaction.reconfig_txn_timeout_failed", ex);
                } // timeout-in-seconds

            } else if (eventName.equals(ServerTags.KEYPOINT_INTERVAL) || eventName.equals(ServerTags.RETRY_TIMEOUT_IN_SECONDS)) {
                javaEETransactionManager.handlePropertyUpdate(eventName, newValue);
                _logger.log(FINE, "{0} reconfig event processed for new value: {1}", new Object[] { eventName, newValue });

            } else if (event.getPropertyName().equals("value")) {
                eventName = ((Property) event.getSource()).getName();
                _logger.log(FINE, "Got Property change event for {0}", eventName);
                if (eventName.equals("purge-cancelled-transactions-after")) {
                    String v = (String) newValue;
                    if (v == null || v.length() == 0) {
                        javaEETransactionManager.setPurgeCancelledTtransactionsAfter(0);
                    } else {
                        javaEETransactionManager.setPurgeCancelledTtransactionsAfter(Integer.parseInt(v, 10));
                    }
                } else {
                    // Not handled dynamically. Restart is required.
                    accepted = false;
                }

            } else if (event.getPropertyName().equals("name") || event.getPropertyName().equals("property")) {
                // skip - means a new property added, was processed above as "value".
                _logger.log(FINE, "...skipped");

            } else {
                // Not handled dynamically. Restart is required.
                accepted = false;
            }

            if (!accepted) {
                String msg = sm.getString("enterprise_distributedtx.restart_required", eventName);
                _logger.log(INFO, msg);
                unprocessedEvents.add(new UnprocessedChangeEvent(event, msg));
            }
        }

        return (unprocessedEvents.size() > 0) ? new UnprocessedChangeEvents(unprocessedEvents) : null;
    }

}
