/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.recovery;

import com.sun.enterprise.transaction.api.RecoveryResourceRegistry;
import com.sun.enterprise.transaction.spi.RecoveryResourceHandler;
import com.sun.enterprise.transaction.spi.RecoveryResourceListener;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

import javax.transaction.xa.XAResource;

import org.jvnet.hk2.annotations.Service;

/**
 * RecoveryResourceHandler for third party resources
 *
 * @author Jagadish Ramu
 */
@Service
public class ThirdPartyRecoveryResourceHandler implements RecoveryResourceHandler {

    @Inject
    private RecoveryResourceRegistry recoveryResourceRegistry;

    @Override
    public void loadXAResourcesAndItsConnections(List xaresList, List connList) {
        Set<RecoveryResourceListener> listeners = recoveryResourceRegistry.getListeners();

        for (RecoveryResourceListener recoveryResourceListener : listeners) {
            for (XAResource xar : recoveryResourceListener.getXAResources()) {
                xaresList.add(xar);
            }
        }
    }

    @Override
    public void closeConnections(List connList) {
        // do nothing
    }
}
