/*
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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/ModuleLogLevelsConfigTest.java,v 1.6 2007/05/05 05:23:55 tcfujii Exp $
* $Revision: 1.6 $
* $Date: 2007/05/05 05:23:55 $
*/
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.config.LogLevelValues;
import com.sun.appserv.management.config.ModuleLogLevelsConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Map;


/**
 */
public final class ModuleLogLevelsConfigTest
        extends AMXTestBase {
    public ModuleLogLevelsConfigTest() {
    }

    protected ModuleLogLevelsConfig
    getModuleLogLevelsConfig() {
        return getConfigConfig().getLogServiceConfig().getModuleLogLevelsConfig();
    }

/*
    public void
    testGetAll() {
        final ModuleLogLevelsConfig config = getModuleLogLevelsConfig();

        final Map<String, String> all = config.getAllLevels();
        assert (all.size() == 24);
    }

    public void
    testChangeAll() {
        final ModuleLogLevelsConfig config = getModuleLogLevelsConfig();

        config.changeAll(LogLevelValues.FINE);
        // verify that they were all changed
        Map<String, String> all = config.getAllLevels();
        for (final String value : all.values()) {
            assert (value.equals(LogLevelValues.FINE));
        }

        config.changeAll(LogLevelValues.INFO);
        // verify that they were all changed
        all = config.getAllLevels();
        for (final String value : all.values()) {
            assert (value.equals(LogLevelValues.INFO));
        }
    }
*/
}


