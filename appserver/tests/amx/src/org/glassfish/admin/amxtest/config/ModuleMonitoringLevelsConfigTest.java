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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.ModuleMonitoringLevelsConfig;
import com.sun.appserv.management.config.MonitoringServiceConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import javax.management.AttributeList;
import javax.management.JMException;
import java.io.IOException;


/**
 */
public final class ModuleMonitoringLevelsConfigTest
        extends AMXTestBase {
    public ModuleMonitoringLevelsConfigTest() {
    }

    public void
    testGetAll() throws JMException, IOException {
        final ModuleMonitoringLevelsConfig mml = getModuleMonitoringLevelsConfig();

        final String[] allNames = new String[ ModuleMonitoringLevelsConfig.ALL_LEVEL_NAMES.size() ];
        ModuleMonitoringLevelsConfig.ALL_LEVEL_NAMES.toArray( allNames );

        final AttributeList attrs = Util.getExtra(mml).getAttributes( allNames );
        assert allNames.length == attrs.size();
    }


        public void
    testCreateRemove() {
        ModuleMonitoringLevelsConfig existing = getModuleMonitoringLevelsConfig();

        final MonitoringServiceConfig mon = getConfigConfig().getMonitoringServiceConfig();
        mon.removeModuleMonitoringLevelsConfig();
        final ModuleMonitoringLevelsConfig newMM = mon.createModuleMonitoringLevelsConfig(null);
    }
}


