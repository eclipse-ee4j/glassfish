/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.auth.login;

import static java.util.logging.Level.SEVERE;

import com.sun.enterprise.security.auth.realm.exceptions.InvalidOperationException;
import com.sun.enterprise.security.auth.realm.exceptions.NoSuchUserException;
import java.util.Enumeration;

/**
 * @author K.Venugopal@sun.com
 */
public class JDBCDigestLoginModule extends DigestLoginModule {

    @Override
    protected Enumeration<String> getGroups(String username) {
        try {
            return getRealm().getGroupNames(username);
        } catch (InvalidOperationException | NoSuchUserException ex) {
            _logger.log(SEVERE, null, ex);
        }

        return null;
    }
}
