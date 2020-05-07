/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.cdi.cases.devtests.predestroy.war;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.oracle.cdi.cases.devtests.predestroy.lib.EventLog;
import com.oracle.cdi.cases.devtests.predestroy.lib.PreDestroyConstants;
import com.oracle.cdi.cases.devtests.predestroy.lib.RequestBean;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


@SessionScoped
public class SessionBeanProducer implements Serializable {
    @Produces
    public UUID getUUID() {
        return uuidHolder.get();
    }

    @PostConstruct
    void constructed() {
        UUID uuid = UUID.randomUUID();
        uuidHolder.set(uuid);
        eventLog.add(PreDestroyConstants.CREATED + uuid);
    }

    @PreDestroy
    void destroy() {
        eventLog.add(PreDestroyConstants.PRODUCER_PRE_DESTROY_IN);
        try {
            requestBean.beanMethod();
        }
        catch (ContextNotActiveException th) {
            eventLog.add(PreDestroyConstants.EXPECTED_EXCEPTION);
        }
    }

    private final AtomicReference<UUID> uuidHolder = new AtomicReference<UUID>();
    @Inject private EventLog eventLog;
    @Inject private RequestBean requestBean;
}
