/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.config;

import org.glassfish.connectors.config.BackendPrincipal;
import org.junit.Ignore;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author: Bhakti Mehta
 */
public  class PasswordAliasTest extends ConfigApiTest{

    BackendPrincipal bp1  ;
    final String ALIAS_TOKEN = "ALIAS";

    @Before
    public void setup() {
        bp1 = super.getHabitat().getService(BackendPrincipal.class);
    }

    @Test
    public void passwordAttributeTest() throws NoSuchMethodException {

        String starter = "${" + ALIAS_TOKEN + "="; //no space is allowed in starter

        String password = bp1.getPassword();
        //Currently the habitat.getByContract(MasterPassword is null)

        assertTrue(password!=null);
        //assertTrue(!password.startsWith(starter));


    }


}
