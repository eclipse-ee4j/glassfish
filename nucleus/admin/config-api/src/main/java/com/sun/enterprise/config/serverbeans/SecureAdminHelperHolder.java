/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import org.glassfish.hk2.api.ServiceLocator;

final class SecureAdminHelperHolder {

    private static final Object lock = new Object();

    private static volatile SecureAdminHelper secureAdminHelper;

    private SecureAdminHelperHolder() {
        throw new AssertionError();
    }

    public static SecureAdminHelper getSecureAdminHelper(ServiceLocator habitat) {
        // Double-checked locking
        SecureAdminHelper localHelper = secureAdminHelper;
        if (localHelper == null) {
            synchronized (lock) {
                localHelper = secureAdminHelper;
                if (localHelper == null) {
                    secureAdminHelper = localHelper = habitat.getService(SecureAdminHelper.class);
                }
            }
        }
        return localHelper;
    }
}
