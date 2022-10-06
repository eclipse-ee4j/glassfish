/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.glassfish.concurrent.runtime.deployer.cfg.ContextInfoParser.parseContextInfo;


/**
 * @author David Matejcek
 */
public class ContextServiceCfg implements Serializable {
    private static final long serialVersionUID = -2284599070400343119L;

    private final ConcurrentServiceCfg serviceConfig;
    private final Set<String> propagatedContexts;
    private final Set<String> clearedContexts;
    private final Set<String> unchangedContexts;

    public ContextServiceCfg(ConcurrentServiceCfg serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.clearedContexts = new HashSet<>();
        this.unchangedContexts = new HashSet<>();
        this.propagatedContexts = parseContextInfo(serviceConfig.getContextInfo(), serviceConfig.isContextInfoEnabled());
    }


    public ContextServiceCfg(ConcurrentServiceCfg serviceConfig, Set<String> propagated, Set<String> cleared, Set<String> unchanged) {
        this.serviceConfig = serviceConfig;
        this.clearedContexts = cleared;
        this.unchangedContexts = unchanged;
        this.propagatedContexts = propagated;
    }


    public ConcurrentServiceCfg getServiceConfig() {
        return this.serviceConfig;
    }


    public Set<String> getPropagatedContexts() {
        return propagatedContexts;
    }


    public Set<String> getClearedContexts() {
        return clearedContexts;
    }


    public Set<String> getUnchangedContexts() {
        return unchangedContexts;
    }


    @Override
    public String toString() {
        return super.toString() + "[serviceConfig=" + serviceConfig + "]";
    }
}
