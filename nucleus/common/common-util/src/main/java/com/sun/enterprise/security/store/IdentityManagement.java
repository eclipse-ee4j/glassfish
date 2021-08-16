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

package com.sun.enterprise.security.store;

import org.jvnet.hk2.annotations.Contract;

/**
 * A @link {Contract} that satisfies credential information requirements of server. All the sub-systems should look up a service
 * that implements this contract. The name of the service implies the functionality.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Contract
public interface IdentityManagement {

    /**
     * Returns the master password as a character array. The master password is what unlocks the secure store where primary keys (and
     * trusted certificates) are stored.
     *
     * @return a character array that represents the master password.
     */
    char[] getMasterPassword();
}
