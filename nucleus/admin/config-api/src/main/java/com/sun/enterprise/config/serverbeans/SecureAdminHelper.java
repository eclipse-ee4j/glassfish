/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.security.KeyStoreException;

import org.jvnet.hk2.annotations.Contract;

/**
 * Definition of some utility behavior that needs to be invoked from config classes in admin/config-api but
 * implemented elsewhere (in a module with dependencies that we do not want to add to admin/config-api).
 *
 * @author Tim Quinn
 */
@Contract()
public interface SecureAdminHelper {

    /**
     * Returns the DN for the given DN or alias value.
     *
     * @param value the user-specified value
     * @param isAlias whether the value is an alias or the DN itself
     * @return the DN
     */
    String getDN(String value, boolean isAlias) throws IOException, KeyStoreException;

    /**
     * Makes sure that the specified username is an admin user and that the specified password
     * alias exists. Note that implementations of this method should not make sure that
     * the username and the password pointed to by the alias actually match a valid
     * admin user in the admin realm. That check is done by the normal authorization logic
     * when the username and the actual password are used.
     *
     * @param username the username
     * @param passwordAlias a password alias
     */
    void validateInternalUsernameAndPasswordAlias(String username, String passwordAlias);

    /**
     * Reports whether any admin user exists which has an empty password.
     *
     * @return {@code true} if any admin user exists with an empty password; {@code false} otherwise
     * @throws Exception if an error occurred
     */
    boolean isAnyAdminUserWithoutPassword() throws Exception;

    /**
     * An exception indicating a user-correctable error that occurred as a secure admin command executed.
     *
     * <p>The secure admin commands can detect such errors and report just the exception's message
     * and not the exception as well (which would clutter the report back to the admin client).
     */
    class SecureAdminCommandException extends RuntimeException {

        public SecureAdminCommandException(String message) {
            super(message);
        }
    }
}
