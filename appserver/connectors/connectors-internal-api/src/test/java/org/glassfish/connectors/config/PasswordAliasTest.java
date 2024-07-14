/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.security.common.MasterPassword;
import org.glassfish.tests.utils.junit.DomainXml;
import org.glassfish.tests.utils.junit.hk2.HK2JUnit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author: Bhakti Mehta
 */
@ExtendWith(HK2JUnit5Extension.class)
@DomainXml("PasswordAliasTest.xml")
public  class PasswordAliasTest {

    private static final String ALIAS_TOKEN = "ALIAS";

    @Inject
    private ServiceLocator locator;
    @Inject
    private BackendPrincipal backendPrincipal;


    @Test
    public void passwordAttributeTest() throws NoSuchMethodException {
        assertNotNull(backendPrincipal, "BackendPrincipal");
        assertNotNull(locator.getService(MasterPassword.class), "MasterPassword service");
        final String password = backendPrincipal.getPassword();
        assertNotNull(password);
        // no space is allowed in starter
        assertThat(password, startsWith("${" + ALIAS_TOKEN + "="));
    }
}
