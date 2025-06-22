/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.amx.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

public final class AmxTestExtension implements BeforeAllCallback, AfterAllCallback {


    @Override
    public void beforeAll(ExtensionContext context) {
        checkAssertsOn();
        setProperty(INSTANCE_ROOT.getSystemPropertyName(), "/tmp/amx-test", true);
    }


    @Override
    public void afterAll(ExtensionContext context) {
        setProperty(INSTANCE_ROOT.getSystemPropertyName(), null, true);
    }


    private void checkAssertsOn() {
        try {
            assert false;
            throw new Error("Assertions must be enabled for unit tests, because they are used in library sources.");
        } catch (AssertionError a) {
            // OK, this is the desired outcome
        }
    }
}
