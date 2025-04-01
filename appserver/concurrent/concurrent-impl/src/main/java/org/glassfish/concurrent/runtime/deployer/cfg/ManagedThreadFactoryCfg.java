/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import java.io.Serializable;
import java.util.Set;

import org.glassfish.concurrent.config.ManagedThreadFactory;

import static org.glassfish.concurrent.runtime.deployer.cfg.CfgParser.parseContextInfo;
import static org.glassfish.concurrent.runtime.deployer.cfg.CfgParser.parseInt;

/**
 * Contains configuration information for a ManagedThreadFactory object
 */
public class ManagedThreadFactoryCfg implements Serializable {

    private static final long serialVersionUID = -7772066566768020144L;

    private final ConcurrentServiceCfg serviceConfig;
    private final boolean useVirtualThreads;
    private final int threadPriority;

    public ManagedThreadFactoryCfg(ManagedThreadFactory config) {
        Set<ConcurrencyContextType> propagated = parseContextInfo(config.getContextInfo(), config.getContextInfoEnabled());
        serviceConfig = new ConcurrentServiceCfg(config.getJndiName(), propagated, config.getContext());
        useVirtualThreads = Boolean.parseBoolean(config.getUseVirtualThreads());
        threadPriority = parseInt(config.getThreadPriority(), Thread.NORM_PRIORITY);
    }

    public ConcurrentServiceCfg getServiceConfig() {
        return this.serviceConfig;
    }

    public boolean getUseVirtualThreads() {
        return useVirtualThreads;
    }

    public int getThreadPriority() {
        return threadPriority;
    }
}
