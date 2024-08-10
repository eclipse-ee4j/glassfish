/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.jvnet.hk2.annotations.Contract;

/**
 * Listener interface for objects interested in transaction events on the config beans.
 *
 * @author Jerome Dochez
 */
@Contract
public interface TransactionListener {

    /**
     *  Notification of a transaction with the list of property changes.
     *
     * @param changes
     */
    public void transactionCommited(List<PropertyChangeEvent> changes);

    /**
     * Nofication of unprocessed events by ConfigListener, usually requiring a server
     * restart.
     *
     * @param changes
     */
    public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes);
}
