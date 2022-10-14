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

import com.sun.enterprise.deployment.annotation.handlers.ContextServiceDefinitionData;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.concurrent.config.ContextService;

import static com.sun.enterprise.deployment.annotation.handlers.StandardContextType.standardize;
import static org.glassfish.concurrent.runtime.deployer.cfg.CfgParser.parseContextInfo;


/**
 * @author David Matejcek
 */
public class ContextServiceCfg implements Serializable {
    private static final long serialVersionUID = -2284599070400343119L;

    private final ConcurrentServiceCfg serviceConfig;
    private final Set<String> propagatedContexts;
    private final Set<String> clearedContexts;
    private final Set<String> unchangedContexts;

    public ContextServiceCfg(ContextServiceDefinitionData data) {
        this.serviceConfig = new ConcurrentServiceCfg(data.getName());
        this.propagatedContexts = standardize(data.getPropagated());
        this.clearedContexts = standardize(data.getCleared());
        this.unchangedContexts = standardize(data.getUnchanged());
    }


    public ContextServiceCfg(ContextService data) {
        this.propagatedContexts = parseContextInfo(data.getContextInfo(), data.getContextInfoEnabled());
        this.serviceConfig = new ConcurrentServiceCfg(data.getJndiName(), propagatedContexts);
        this.clearedContexts = new HashSet<>();
        this.unchangedContexts = new HashSet<>();
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
