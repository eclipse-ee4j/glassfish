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

package com.sun.enterprise.security.jacc.provider;

import static java.util.logging.Level.WARNING;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;

/**
 *
 * @author monzillo
 */
public class SharedState {

    private static final Logger logger = Logger.getLogger(SharedState.class.getPackage().getName());

    // lock on the shared configTable and linkTable
    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
    private static Lock rLock = rwLock.readLock();
    private static Lock wLock = rwLock.writeLock();
    private static Map<String, SimplePolicyConfiguration> configTable = new HashMap<>();
    private static Map<String, Set<String>> linkTable = new HashMap<>();


    static Logger getLogger() {
        return logger;
    }

    static SimplePolicyConfiguration lookupConfig(String pcid) {
        wLock.lock();
        try {
            return configTable.get(pcid);
        } finally {
            wLock.unlock();
        }
    }

    static SimplePolicyConfiguration getConfig(String pcid, boolean remove) {
        SimplePolicyConfiguration simplePolicyConfiguration = null;
        wLock.lock();
        try {
            simplePolicyConfiguration = configTable.get(pcid);
            if (simplePolicyConfiguration == null) {
                simplePolicyConfiguration = new SimplePolicyConfiguration(pcid);
                SharedState.initLinks(pcid);
                configTable.put(pcid, simplePolicyConfiguration);
            } else if (remove) {
                SharedState.removeLinks(pcid);
            }
        } finally {
            wLock.unlock();
        }

        return simplePolicyConfiguration;
    }

    static SimplePolicyConfiguration getActiveConfig() throws PolicyContextException {
        String contectId = PolicyContext.getContextID();
        SimplePolicyConfiguration simplePolicyConfiguration = null;
        if (contectId != null) {
            rLock.lock();
            try {
                simplePolicyConfiguration = configTable.get(contectId);
                if (simplePolicyConfiguration == null) {
                    /*
                     * unknown policy context set on thread return null to allow checking to be performed with default context. Should
                     * repair improper setting of context by encompassing runtime.
                     */
                    SimplePolicyConfiguration.logException(WARNING, "invalid policy context id", new PolicyContextException());
                }

            } finally {
                rLock.unlock();
            }
            if (simplePolicyConfiguration != null) {
                if (!simplePolicyConfiguration.inService()) {
                    /*
                     * policy context set on thread is not in service return null to allow checking to be performed with default context.
                     * Should repair improper setting of context by encompassing runtime.
                     */
                    SimplePolicyConfiguration.logException(Level.FINEST, "invalid policy context state", new PolicyContextException());
                    simplePolicyConfiguration = null;
                }
            }
        }

        return simplePolicyConfiguration;
    }

    /**
     * Creates a relationship between this configuration and another such that they share the same principal-to-role
     * mappings. PolicyConfigurations are linked to apply a common principal-to-role mapping to multiple seperately
     * manageable PolicyConfigurations, as is required when an application is composed of multiple modules.
     * <P>
     * Note that the policy statements which comprise a role, or comprise the excluded or unchecked policy collections in a
     * PolicyConfiguration are unaffected by the configuration being linked to another.
     * <P>
     * The relationship formed by this method is symetric, transitive and idempotent.
     *
     * @param id
     * @param otherId
     * @throws jakarta.security.jacc.PolicyContextException If otherID equals receiverID. no relationship is formed.
     */
    static void link(String id, String otherId) throws jakarta.security.jacc.PolicyContextException {
        wLock.lock();
        try {
            if (otherId.equals(id)) {
                throw new IllegalArgumentException("Operation attempted to link PolicyConfiguration to itself.");
            }

            // Get the linkSet corresponding to this context
            Set<String> linkSet = linkTable.get(id);

            // Get the linkSet corresponding to the context being linked to this
            Set<String> otherLinkSet = linkTable.get(otherId);

            if (otherLinkSet == null) {
                throw new RuntimeException("Linked policy configuration (" + otherId + ") does not exist");
            }

            for (String nextid : otherLinkSet) {
                // Add the id to this linkSet
                linkSet.add(nextid);

                // Replace the linkset mapped to all the contexts being linked
                // to this context, with this linkset.
                linkTable.put(nextid, linkSet);
            }
        } finally {
            wLock.unlock();
        }
    }

    static void initLinks(String contextId) {
        // Create a new linkSet with only this context id, and put in the table.
        Set<String> linkSet = new HashSet<>();
        linkSet.add(contextId);
        linkTable.put(contextId, linkSet);
    }

    static void removeLinks(String contextId) {
        wLock.lock();
        try { // get the linkSet corresponding to this context.
            Set<String> linkSet = linkTable.get(contextId);

            // Remove this context id from the linkSet (which may be shared
            // with other contexts), and unmap the linkSet from this context.
            if (linkSet != null) {
                linkSet.remove(contextId);
                linkTable.remove(contextId);
            }

            initLinks(contextId);
        } finally {
            wLock.unlock();
        }
    }

}
