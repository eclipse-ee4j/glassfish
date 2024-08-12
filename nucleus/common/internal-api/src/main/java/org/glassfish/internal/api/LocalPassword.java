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

package org.glassfish.internal.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * Manage a local password, which is a cryptographically secure random number
 * stored in a file with permissions that only allow the owner to read it.
 * A new local password is generated each time the server starts.  The
 * asadmin client can use it to authenticate when executing local commands,
 * such as stop-domain, without the user needing to supply a password.
 *
 * @author Bill Shannon
 */
@Contract
public interface LocalPassword {

    /**
     * Is the given password the local password?
     *
     * @param password the password to test
     * @return true if it is a local password, false otherwise
     */
    public boolean isLocalPassword(String password);

    /**
     * Get the local password.
     *
     * @return the local password
     */
    public String getLocalPassword();
}
