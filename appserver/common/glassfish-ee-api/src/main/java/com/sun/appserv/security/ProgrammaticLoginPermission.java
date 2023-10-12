/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package com.sun.appserv.security;

import com.sun.enterprise.security.BaseProgrammaticLoginPermission;

/**
 * Permission for using programmatic login.
 *
 * <P>
 * This permission is used by ProgrammaticLogin to verify whether the invoking code has been granted the use of this
 * interface.
 *
 * <P>
 * The name of this permission is the name of the method being invoked.
 *
 */
public class ProgrammaticLoginPermission extends BaseProgrammaticLoginPermission {

    private static final long serialVersionUID = 1L;

    public ProgrammaticLoginPermission(String name) {
        super(name);
    }

}
