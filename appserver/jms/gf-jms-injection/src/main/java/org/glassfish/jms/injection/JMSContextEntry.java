/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jms.injection;

import jakarta.jms.JMSContext;

import java.io.Serializable;

import org.glassfish.api.invocation.ComponentInvocation;

public class JMSContextEntry implements Serializable {

    private static final long serialVersionUID = 5250371279470306316L;

    private final String injectionPointId;
    private final JMSContext jmsContext;
    private final transient ComponentInvocation componentInvocation;

    public JMSContextEntry(String ipId, JMSContext context, ComponentInvocation inv) {
        injectionPointId = ipId;
        jmsContext = context;
        this.componentInvocation = inv;
    }

    public String getInjectionPointId() {
        return injectionPointId;
    }

    public JMSContext getCtx() {
        return jmsContext;
    }

    public ComponentInvocation getComponentInvocation() {
        return componentInvocation;
    }
}
