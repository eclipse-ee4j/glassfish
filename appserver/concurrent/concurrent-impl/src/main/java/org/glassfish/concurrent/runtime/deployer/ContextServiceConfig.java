/*
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

package org.glassfish.concurrent.runtime.deployer;

import static org.glassfish.concurrent.runtime.ConcurrentRuntime.CONTEXT_INFO_CLASSLOADER;
import static org.glassfish.concurrent.runtime.ConcurrentRuntime.CONTEXT_INFO_JNDI;
import static org.glassfish.concurrent.runtime.ConcurrentRuntime.CONTEXT_INFO_SECURITY;
import static org.glassfish.concurrent.runtime.ConcurrentRuntime.CONTEXT_INFO_WORKAREA;
import static org.glassfish.concurrent.runtime.ContextSetupProviderImpl.CONTEXT_TYPE_CLASSLOADING;
import static org.glassfish.concurrent.runtime.ContextSetupProviderImpl.CONTEXT_TYPE_NAMING;
import static org.glassfish.concurrent.runtime.ContextSetupProviderImpl.CONTEXT_TYPE_SECURITY;
import static org.glassfish.concurrent.runtime.ContextSetupProviderImpl.CONTEXT_TYPE_WORKAREA;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.glassfish.concurrent.config.ContextService;

/**
 * Contains configuration information for a ContextService object
 */
public class ContextServiceConfig extends BaseConfig {

    private static final long serialVersionUID = 1L;

    private final Set<String> propagatedContexts;
    private final Set<String> clearedContexts;
    private final Set<String> uchangedContexts;

    public ContextServiceConfig(String jndiName) {
        super(jndiName, null, "true");
        this.propagatedContexts = parseContextInfo(this.contextInfo, this.isContextInfoEnabledBoolean());
        this.clearedContexts = new HashSet<>();
        this.uchangedContexts = new HashSet<>();
    }

    public ContextServiceConfig(ContextService config) {
        super(config.getJndiName(), config.getContextInfo(), config.getContextInfoEnabled());
        this.propagatedContexts = parseContextInfo(this.contextInfo, this.isContextInfoEnabledBoolean());
        this.clearedContexts = new HashSet<>();
        this.uchangedContexts = new HashSet<>();
    }

    public ContextServiceConfig(String jndiName, String contextInfo, String contextInfoEnabled, Set<String> propagatedContexts, Set<String> clearedContexts, Set<String> uchangedContexts) {
        super(jndiName, contextInfo, contextInfoEnabled);
        this.propagatedContexts = propagatedContexts;
        this.clearedContexts = clearedContexts;
        this.uchangedContexts = uchangedContexts;
    }

    @Override
    public TYPE getType() {
        return TYPE.CONTEXT_SERVICE;
    }

    public Set<String> getPropagatedContexts() {
        return propagatedContexts;
    }

    public Set<String> getClearedContexts() {
        return clearedContexts;
    }

    public Set<String> getUchangedContexts() {
        return uchangedContexts;
    }

    public static Set<String> parseContextInfo(String contextInfo, boolean isContextInfoEnabled) {
        Set<String> contextTypeArray = new HashSet<>();
        if (contextInfo == null) {
            // By default, if no context info is passed, we propagate all context types
            contextTypeArray.add(CONTEXT_TYPE_CLASSLOADING);
            contextTypeArray.add(CONTEXT_TYPE_NAMING);
            contextTypeArray.add(CONTEXT_TYPE_SECURITY);
            contextTypeArray.add(CONTEXT_TYPE_WORKAREA);
        } else if (isContextInfoEnabled) {
            StringTokenizer st = new StringTokenizer(contextInfo, ",", false);
            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (CONTEXT_INFO_CLASSLOADER.equalsIgnoreCase(token)) {
                    contextTypeArray.add(CONTEXT_TYPE_CLASSLOADING);
                } else if (CONTEXT_INFO_JNDI.equalsIgnoreCase(token)) {
                    contextTypeArray.add(CONTEXT_TYPE_NAMING);
                } else if (CONTEXT_INFO_SECURITY.equalsIgnoreCase(token)) {
                    contextTypeArray.add(CONTEXT_TYPE_SECURITY);
                } else if (CONTEXT_INFO_WORKAREA.equalsIgnoreCase(token)) {
                    contextTypeArray.add(CONTEXT_TYPE_WORKAREA);
                } else {
                    contextTypeArray.add(token); // custom context
                }
            }
        }

        return contextTypeArray;
    }
}
