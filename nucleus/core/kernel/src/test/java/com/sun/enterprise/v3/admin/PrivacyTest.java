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

package com.sun.enterprise.v3.admin;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test the visibility annotation
 */
public class PrivacyTest {

    @Test
    public void privacyTests() {
        AdminAdapter publicAdaper = new PublicAdminAdapter();
        AdminAdapter privateAdapter = new PrivateAdminAdapter();
        ServiceLocator habitat = Utils.getNewHabitat();
        AdminCommand adminCommand = habitat.getService(AdminCommand.class, "simple-public-command");
        assertTrue(publicAdaper.validatePrivacy(adminCommand));
        assertFalse(privateAdapter.validatePrivacy(adminCommand));
        adminCommand = habitat.getService(AdminCommand.class, "notannoated-public-command");
        assertTrue(publicAdaper.validatePrivacy(adminCommand));
        assertFalse(privateAdapter.validatePrivacy(adminCommand));
        adminCommand = habitat.getService(AdminCommand.class, "simple-private-command");
        assertFalse(publicAdaper.validatePrivacy(adminCommand));
        assertTrue(privateAdapter.validatePrivacy(adminCommand));
    }
}
