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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@DatabaseIdentityStoreDefinition(
    dataSourceLookup="${'jdbc/__default'}",
    callerQuery="#{'select password from caller where name = ?'}",
    groupsQuery="select group_name from caller_groups where caller_name = ?",
    hashAlgorithm = PlaintextPasswordHash.class,
    hashAlgorithmParameters = {
        "foo=bar",
        "kax=zak",
        "foox=${'iop'}",
        "${applicationConfig.dyna}"

    } // just for test / example
)
@ApplicationScoped
@Named
public class ApplicationConfig {

    public String[] getDyna() {
        return new String[] {"dyn=1","dyna=2","dynam=3"};
    }

}
