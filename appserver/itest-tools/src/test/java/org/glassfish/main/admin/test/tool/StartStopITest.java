/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.admin.test.tool;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link GlassFishTestEnvironment} starts the domain, we execute the command and then
 * {@link GlassFishTestEnvironment} stops the server automatically.
 *
 * @author David Matejcek
 */
class StartStopITest {

    private static final Asadmin ASADMIN = GlassFishTestEnvironment.getAsadmin();

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void asadminGet_Linux() {
        assertThat(ASADMIN.exec("get", "*"), asadminOK());
    }


    @Test
    @EnabledOnOs(OS.WINDOWS)
    void asadminGet_Windows() {
        assertThat(ASADMIN.exec("get", "\"*\""), asadminOK());
    }
}
