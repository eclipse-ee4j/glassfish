/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.soteria.test;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.security.enterprise.identitystore.PasswordHash;

@Dependent
public class PlaintextPasswordHash implements PasswordHash {

    @Override
    public void initialize(Map<String, String> parameters) {

    }

    @Override
    public String generate(char[] password) {
        return new String(password);
    }

    @Override
    public boolean verify(char[] password, String hashedPassword) {
         //don't bother with constant time comparison; more portable
         //this way, and algorithm will be used only for testing.
        return (password != null && password.length > 0 &&
                hashedPassword != null && hashedPassword.length() > 0 &&
                hashedPassword.equals(new String(password)));
    }
}
