/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.diagnostics.context.impl;

import jakarta.inject.Inject;

import org.glassfish.diagnostics.context.ContextManager;
import org.glassfish.tests.utils.junit.HK2JUnit5Extension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(HK2JUnit5Extension.class)
public class ContextImplIntegrationTest {

    @Inject
    private ContextManager mContextManager;

    @Test
    public void testInjectionOccurred() {
        System.out.println("mContextManager instance of " + mContextManager.getClass().getName());
        assertNotNull(mContextManager, "mContextManager should have been injected, but it is still null.");
    }
}
