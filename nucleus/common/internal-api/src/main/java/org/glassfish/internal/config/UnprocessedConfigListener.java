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

package org.glassfish.internal.config;

import jakarta.inject.Inject;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

/**
Listens for unprocessed config changes
 */
@Service
@RunLevel(value=PostStartupRunLevel.VAL, mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public final class UnprocessedConfigListener implements PostConstruct, TransactionListener {
    @Inject
    private Transactions mTransactions;

    private final List<UnprocessedChangeEvents> mUnprocessedChangeEvents = new ArrayList();

    public UnprocessedConfigListener() {
        //debug( "UnprocessedConfigListener.UnprocessedConfigListener" );
    }

    public void postConstruct() {
        mTransactions.addTransactionsListener(this);
    }

    public void transactionCommited(final List<PropertyChangeEvent> changes) {
        // ignore, we only are interested in those that were not processed
    }

    public synchronized void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
        mUnprocessedChangeEvents.addAll(changes);
    }

    public boolean serverRequiresRestart() {
        return getUnprocessedChangeEvents().size() != 0;
    }

    public synchronized List<UnprocessedChangeEvents> getUnprocessedChangeEvents() {
        return Collections.unmodifiableList(mUnprocessedChangeEvents);
    }

}
















