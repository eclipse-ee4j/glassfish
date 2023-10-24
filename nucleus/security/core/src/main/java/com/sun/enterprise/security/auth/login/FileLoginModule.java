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

package com.sun.enterprise.security.auth.login;

import static java.util.logging.Level.FINE;

import com.sun.enterprise.security.BasePasswordLoginModule;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import javax.security.auth.login.LoginException;

/**
 * File realm login module.
 *
 * <P>
 * Provides a file-based implementation of a password login module. Processing is delegated to the FileRealm class.
 *
 * @see com.sun.enterprise.security.auth.realm.file.FileRealm
 *
 */
public class FileLoginModule extends BasePasswordLoginModule {

    /**
     * Perform file authentication. Delegates to FileRealm.
     *
     * @throws LoginException If login fails (JAAS login() behavior).
     *
     */
    @Override
    protected void authenticateUser() throws LoginException {
        FileRealm fileRealm = getRealm(FileRealm.class, "filelm.badrealm");

        String[] groups = fileRealm.authenticate(_username, getPasswordChar());

        if (groups == null) { // JAAS behavior
            throw new LoginException(sm.getString("filelm.faillogin", _username));
        }

        _logger.log(FINE, () -> "File login succeeded for: " + _username);

        commitUserAuthentication(groups);
    }
}
