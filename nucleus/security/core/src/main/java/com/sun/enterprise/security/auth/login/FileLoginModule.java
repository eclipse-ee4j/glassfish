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

package com.sun.enterprise.security.auth.login;

import java.util.*;
import java.util.logging.Level;
import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;
import javax.security.auth.spi.*;
import com.sun.enterprise.security.auth.realm.file.FileRealm;
import javax.security.auth.login.LoginException;

/**
 * File realm login module.
 *
 * <P>Provides a file-based implementation of a password login module.
 * Processing is delegated to the FileRealm class.
 *
 * @see com.sun.enterprise.security.auth.login.PasswordLoginModule
 * @see com.sun.enterprise.security.auth.realm.file.FileRealm
 *
 */
public class FileLoginModule extends PasswordLoginModule
{

    /**
     * Perform file authentication. Delegates to FileRealm.
     *
     * @throws LoginException If login fails (JAAS login() behavior).
     *
     */
    protected void authenticate()
        throws LoginException
    {
        if (!(_currentRealm instanceof FileRealm)) {
            String msg = sm.getString("filelm.badrealm");
            throw new LoginException(msg);
        }
        FileRealm fileRealm = (FileRealm)_currentRealm;

        String[] grpList = fileRealm.authenticate(_username, getPasswordChar());

        if (grpList == null) {  // JAAS behavior
            String msg = sm.getString("filelm.faillogin", _username);
            throw new LoginException(msg);
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "File login succeeded for: " + _username);
        }

        commitAuthentication(_username, getPasswordChar(),
                             _currentRealm, grpList);
    }
}
