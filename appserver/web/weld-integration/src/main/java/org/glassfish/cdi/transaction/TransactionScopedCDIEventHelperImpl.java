/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.transaction;

import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.TransactionScoped;

/**
 * @author <a href="mailto:arjav.desai@oracle.com">Arjav Desai</a>
 */

public class TransactionScopedCDIEventHelperImpl implements TransactionScopedCDIEventHelper {

    @Inject @Initialized(TransactionScoped.class) Event<TransactionScopedCDIEventPayload> trxScopeInitializedEvent;
    @Inject @Destroyed(TransactionScoped.class) Event<TransactionScopedCDIEventPayload> trxScopeDestroyedEvent;

    @Override
    public void fireInitializedEvent(TransactionScopedCDIEventPayload payload) { trxScopeInitializedEvent.fire(payload); }

    @Override
    public void fireDestroyedEvent(TransactionScopedCDIEventPayload payload) {
        trxScopeDestroyedEvent.fire(payload);
    }

}
