/*
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

package com.sun.enterprise.security.audit;

import org.jvnet.hk2.annotations.Contract;

/**
 * Defines the behavior for audit manager implementations.
 *
 * @author tjquinn
 */
@Contract
public interface AuditManager {
    /**
     * Loads all audit modules.
     */
    void loadAuditModules();

    /**
     * Reports authentication events to registered audit modules.
     *
     * @param user
     * @param realm
     * @param success
     */
    void authentication(String user, String realm, boolean success);

    /**
     * Reports server start-up event to registered audit modules.
     */
    void serverStarted();

    /**
     * Reports server shutdown event to registered audit modules.
     */
    void serverShutdown();

    /**
     * Returns whether auditing is turned on.
     *
     * @return
     */
    boolean isAuditOn();
}
