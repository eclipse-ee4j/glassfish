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

package com.sun.enterprise.transaction.spi;

/**
 * Interface implemented by the consumers that are interested in
 * recovery start and end events
 *
 * @author Marina Vatkina
 * @since 3.1
 */
public interface RecoveryEventListener  {

    /**
     * Indicate to the listener that recovery for a specific instance is about to start.
     * @param delegated identifies whether it is part of a delegated transaction recovery
     * @param instance the instance name for which transaction recovery is performed, null if unknown
     */
    void beforeRecovery(boolean delegated, String instance);

    /**
     * Indicate to the listener that recovery is over.
     * @param success <code>true</code> if the recovery operation finished successfully
     * @param delegated identifies whether it is part of a delegated transaction recovery
     * @param instance the instance name for which transaction recovery is performed, null if unknown
     */
    void afterRecovery(boolean success, boolean delegated, String instance);
}
