/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.tests.utils.junit.hk2;

import org.glassfish.internal.api.Globals;
import org.glassfish.tests.utils.junit.BaseHK2JUnit5Extension;
import org.junit.jupiter.api.extension.ExtensionContext;


/**
 * This JUnit5 extension allows to use HK2 services in tests.
 *
 * Using this extension is recommended over the {@link BaseHK2JUnit5Extension} because it properly cleans up global variables. Using {@link BaseHK2JUnit5Extension) should be used only in cases when this extension is not applicable, e.g. because of dependency cycles.
 *
 */
public class HK2JUnit5Extension extends BaseHK2JUnit5Extension {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        resetGlobalServiceLocator();
        super.beforeAll(context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        super.afterAll(context);
        resetGlobalServiceLocator();
    }

    private void resetGlobalServiceLocator() {
        Globals.setDefaultHabitat(null);
    }


}
