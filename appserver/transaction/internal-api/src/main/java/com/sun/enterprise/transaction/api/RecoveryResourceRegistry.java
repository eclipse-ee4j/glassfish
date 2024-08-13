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

package com.sun.enterprise.transaction.api;

import com.sun.enterprise.transaction.spi.RecoveryEventListener;
import com.sun.enterprise.transaction.spi.RecoveryResourceListener;

import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

import org.jvnet.hk2.annotations.Service;


/**
 * This is a registry class that keep the recoveryresource and event listeners.
 * A module will be able to use this singleton to register
 * its recoveryresource listeners and/or event listeners.
 *
 * @author Binod PG
 * @since 9.1
 */
@Service
@Singleton
public class RecoveryResourceRegistry  {

    private final static Set<RecoveryResourceListener> resourceListeners =
            new HashSet<RecoveryResourceListener>();

    private final static Set<RecoveryEventListener> recoveryEventListeners =
            new HashSet<RecoveryEventListener>();

    public RecoveryResourceRegistry() {
    }

    public void addListener(RecoveryResourceListener rrl) {
        resourceListeners.add(rrl);
    }

    public void addEventListener(RecoveryEventListener rrl) {
        recoveryEventListeners.add(rrl);
    }

    public Set<RecoveryResourceListener> getListeners() {
        return resourceListeners;
    }

    public Set<RecoveryEventListener> getEventListeners() {
        return recoveryEventListeners;
    }
}
