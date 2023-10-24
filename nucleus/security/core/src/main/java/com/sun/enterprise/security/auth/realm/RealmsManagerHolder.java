/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.security.auth.realm;

import java.lang.ref.WeakReference;

import org.glassfish.internal.api.Globals;

public class RealmsManagerHolder {

    private static volatile WeakReference<RealmsManager> realmsManagerReference = new WeakReference<RealmsManager>(null);

    static RealmsManager getNonNullRealmsManager() {
        RealmsManager realmsManager = getRealmsManager();
        if (realmsManager == null) {
            throw new RuntimeException("Unable to locate RealmsManager Service");
        }

        return realmsManager;
    }

    static RealmsManager getRealmsManager() {
        if (realmsManagerReference.get() != null) {
            return realmsManagerReference.get();
        }

        return _getRealmsManager();
    }

    static synchronized RealmsManager _getRealmsManager() {
        if (realmsManagerReference.get() == null) {
            if (Globals.getDefaultHabitat() == null) {
                return null;
            }

            realmsManagerReference = new WeakReference<>(Globals.get(RealmsManager.class));
        }

        return realmsManagerReference.get();
    }

}