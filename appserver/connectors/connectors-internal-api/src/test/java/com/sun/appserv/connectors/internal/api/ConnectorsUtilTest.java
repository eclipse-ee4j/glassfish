/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.appserv.connectors.internal.api;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.deployment.common.JavaEEResourceType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author David Matejcek
 */
@Disabled("dmatej: see other commented out blocks in this commit. The fix broke connectors JNDI, but the direction is good, just needs standalone PR.")
public class ConnectorsUtilTest {

    @Test
    public void deriveResourceName_javaAppEnv() {
        // TODO: The current state, but I believe the resource name could be shortened. Or used original name.
        SimpleJndiName jndiName = SimpleJndiName
            .of("java:app/env/__SYSTEM/pools/__cfd/xxx-app/Servlet_ConnectionFactory");
        assertEquals(SimpleJndiName.of(
            "java:app/env/__SYSTEM/resource/__connection_factory_definition/__SYSTEM/pools/__cfd/xxx-app/Servlet_ConnectionFactory"),
            ConnectorsUtil.deriveResourceName(null, jndiName, JavaEEResourceType.CFD));
    }
}
