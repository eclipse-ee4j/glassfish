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

package com.sun.enterprise.security.auth;

import java.security.Principal;
import java.util.Properties;

/**
 *
 * Enables formatting of principal retrieved from message eg: P-Asserted-Identity values. eg: "Cullen Jennings"
 * <sip:fluffy@cisco.com> value can be mapped/formatted to "CullenJ".
 *
 * @author k.venugopal@sun.com
 */

public interface PrincipalMapper {

    void initialize(Properties props);

    /**
     *
     * converts values in to a format understood by the container/container backend eg: database, ldap etc.
     *
     * @param assrtId P-Asserted-Identity values.
     * @return P-Asserted-Identity values in a format understood by the container.
     */
    Principal[] mapIdentity(Principal[] assrtId);
}
