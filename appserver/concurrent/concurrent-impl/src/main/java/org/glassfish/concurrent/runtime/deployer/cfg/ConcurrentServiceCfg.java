/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.runtime.deployer.cfg;

import com.sun.enterprise.deployment.types.ConcurrencyContextType;
import com.sun.enterprise.deployment.types.StandardContextType;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public final class ConcurrentServiceCfg implements Serializable {
    private static final long serialVersionUID = -9039607497553448223L;

    private final String jndiName;
    private final Set<ConcurrencyContextType> contextInfo;
    private final String context;

    public ConcurrentServiceCfg(String jndiName) {
        this(jndiName, Collections.emptySet(), null);
    }


    public ConcurrentServiceCfg(String jndiName, Set<ConcurrencyContextType> contextInfo) {
        this.jndiName = jndiName;
        this.contextInfo = contextInfo;
        this.context = null;
    }


    public ConcurrentServiceCfg(String jndiName, Set<ConcurrencyContextType> contextInfo, String context) {
        this.jndiName = jndiName;
        this.contextInfo = contextInfo;
        this.context = context;
    }


    public ConcurrentServiceCfg(String jndiName, StandardContextType contextInfo, String context) {
        this.jndiName = jndiName;
        this.contextInfo = Set.of(contextInfo);
        this.context = context;
    }


    public String getJndiName() {
        return jndiName;
    }


    public Set<ConcurrencyContextType> getContextInfo() {
        return contextInfo;
    }


    public String getContext() {
        return context;
    }


    @Override
    public String toString() {
        return "ConcurrentServiceCfg[" + jndiName + ", context=" + context + "]";
    }
}
