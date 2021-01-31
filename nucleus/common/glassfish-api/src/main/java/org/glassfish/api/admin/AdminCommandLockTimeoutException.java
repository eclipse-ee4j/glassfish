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

package org.glassfish.api.admin;

import java.util.Date;

/**
 * The AdminCommandLockTimeoutException is generated when a command can not acquire an AdminCommandLock within the
 * allotted time.
 *
 * @author Chris Kasso
 */
public class AdminCommandLockTimeoutException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Date timeOfAcquisition = null;
    private String lockOwner = null;

    public AdminCommandLockTimeoutException(String message, Date timeOfAcquisition, String lockOwner) {
        super(message);
        this.lockOwner = lockOwner;
        this.timeOfAcquisition = timeOfAcquisition;
    }

    public AdminCommandLockTimeoutException(String message, Throwable ex) {
        super(message, ex);
    }

    public Date getTimeOfAcquisition() {
        return this.timeOfAcquisition;
    }

    public String getLockOwner() {
        return this.lockOwner;
    }
}
