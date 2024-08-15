/*
 * Copyright (c) 1997, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.amx.j2ee;

import java.util.Map;

import org.glassfish.admin.amx.annotation.ManagedOperation;


/**
 * Base interface only (for cluster and standalone server)
 */
public interface J2EELogicalServer extends J2EEManagedObject, StateManageable {

    /**
     * Start the application on this Server.
     *
     * @param appID The application ID
     * @param optional Optional parameters supplied as name-value pairs
     */
    @ManagedOperation
    void startApp(String appID, Map<String, String> optional);


    /**
     * Stop the application on this Server.
     *
     * @param appID The application ID
     * @param optional Optional parameters supplied as name-value pairs
     */
    @ManagedOperation
    void stopApp(String appID, Map<String, String> optional);
}
