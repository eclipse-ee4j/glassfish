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

package org.glassfish.concurrent.runtime;


import com.sun.enterprise.deployment.types.ConcurrencyContextType;
import com.sun.enterprise.deployment.types.CustomContextType;
import com.sun.enterprise.deployment.types.StandardContextType;

import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;

import java.io.Serializable;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.ServiceLoader.load;

/**
 * @author David Matejcek
 */
public class ContextSetup implements Serializable {
    private static final long serialVersionUID = 7817957604183520917L;
    private static final Logger LOG = System.getLogger(ContextSetup.class.getName());

    private final Set<ConcurrencyContextType> contextPropagate;
    private final Set<ConcurrencyContextType> contextClear;
    private final Set<ConcurrencyContextType> contextUnchanged;
    private transient Map<CustomContextType, ThreadContextProvider> allThreadContextProviders;


    public ContextSetup(Set<ConcurrencyContextType> propagated, Set<ConcurrencyContextType> cleared, Set<ConcurrencyContextType> unchanged) {
        this.contextPropagate = new HashSet<>(propagated);
        this.contextClear = new HashSet<>(cleared);
        this.contextUnchanged = new HashSet<>(unchanged);
    }


    public void reloadProviders(final ClassLoader loader) {
        this.allThreadContextProviders = loadAllProviders(loader);
        addRemaining(contextPropagate, contextClear, contextUnchanged, allThreadContextProviders);
        LOG.log(Level.DEBUG, "Available contexts: {0}", this);
    }


    public boolean isPropagated(StandardContextType contextType) {
        return contextPropagate.contains(contextType);
    }


    public boolean isClear(StandardContextType contextType) {
        return contextClear.contains(contextType);
    }


    public boolean isUnchanged(StandardContextType contextType) {
        return contextUnchanged.contains(contextType);
    }


    public List<ThreadContextSnapshot> getThreadContextSnapshots(Map<String, String> executionProperties) {
        LOG.log(Level.TRACE, "getThreadContextSnapshots(executionProperties={0})", executionProperties);
        final List<ThreadContextSnapshot> snapshots = new ArrayList<>();
        contextPropagate.stream().map(allThreadContextProviders::get)
            .filter(Objects::nonNull).map(snapshot -> snapshot.currentContext(executionProperties))
            .forEach(snapshots::add);
        contextClear.stream().map(allThreadContextProviders::get)
            .filter(Objects::nonNull).map(snapshot -> snapshot.clearedContext(executionProperties))
            .forEach(snapshots::add);
        return snapshots;
    }


    @Override
    public String toString() {
        return super.toString() + "[propagated=" + contextPropagate + ", cleared=" + contextClear + ", unchanged="
            + contextUnchanged + "]";
    }


    private static Map<CustomContextType, ThreadContextProvider> loadAllProviders(ClassLoader loader) {
        LOG.log(Level.TRACE, "Using classloader: {0}", loader);
        ServiceLoader<ThreadContextProvider> services = load(ThreadContextProvider.class, loader);
        Map<CustomContextType, ThreadContextProvider> providers = new HashMap<>();
        for (ThreadContextProvider service : services) {
            CustomContextType ctxType = new CustomContextType(service.getThreadContextType());
            providers.put(ctxType, service);
        }
        LOG.log(Level.DEBUG, "Detected ThreadContextProvider implementations: {0}", providers);
        return providers;
    }


    private static void addRemaining(Set<ConcurrencyContextType> propagated, Set<ConcurrencyContextType> clear,
        Set<ConcurrencyContextType> unchanged, Map<CustomContextType, ThreadContextProvider> allThreadContextProviders) {
        Set<ConcurrencyContextType> remaining = chooseSet(propagated, clear, unchanged);
        for (StandardContextType contextType : StandardContextType.values()) {
            if (contextType == StandardContextType.Remaining) {
                continue;
            }
            addIfNotInAnotherSet(contextType, remaining, propagated, clear, unchanged);
        }
        for (CustomContextType name : allThreadContextProviders.keySet()) {
            addIfNotInAnotherSet(name, remaining, propagated, clear, unchanged);
        }
    }


    private static Set<ConcurrencyContextType> chooseSet(Set<ConcurrencyContextType> propagated,
        Set<ConcurrencyContextType> clear, Set<ConcurrencyContextType> unchanged) {
        if (clear.contains(StandardContextType.Remaining)) {
            return clear;
        } else if (unchanged.contains(StandardContextType.Remaining)) {
            return unchanged;
        } else {
            return propagated;
        }
    }


    private static void addIfNotInAnotherSet(ConcurrencyContextType name, Set<ConcurrencyContextType> remaining,
        Set<ConcurrencyContextType> propagated, Set<ConcurrencyContextType> clear,
        Set<ConcurrencyContextType> unchanged) {
        if (propagated.contains(name) || clear.contains(name) || unchanged.contains(name)) {
            return;
        }
        remaining.add(name);
    }
}

