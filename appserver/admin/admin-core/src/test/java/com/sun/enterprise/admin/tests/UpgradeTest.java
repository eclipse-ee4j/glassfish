/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.tests;

import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Config;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.api.admin.config.ConfigurationUpgrade;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test for the domain.xml upgrade scenario
 *
 * @author Jerome Dochez
 */
public class UpgradeTest extends ConfigApiTest {

    @Before
    public void setup() {
        Domain domain = getHabitat().getService(Domain.class);
        assertTrue(domain!=null);
        
        // perform upgrade
        for (ConfigurationUpgrade upgrade : getHabitat().<ConfigurationUpgrade>getAllServices(ConfigurationUpgrade.class)) {
            Logger.getAnonymousLogger().info("running upgrade " + upgrade.getClass());    
        }
    }

    @Test
    public void threadPools() {
        List<String> names = new ArrayList<String>();
        for (ThreadPool pool : getHabitat().<Config>getService(Config.class).getThreadPools().getThreadPool()) {
            names.add(pool.getName());
        }
        assertTrue(names.contains("http-thread-pool") && names.contains("thread-pool-1"));
    }

    private void verify(String name) {
        assertTrue("Should find thread pool named " + name, getHabitat().getService(ThreadPool.class, name) != null);
    }
    @Test
    public void applicationUpgrade() {
        Applications apps = getHabitat().getService(Applications.class);
        assertTrue(apps!=null);
        for (Application app : apps.getApplications()) {
            assertTrue(app.getEngine().isEmpty());
            assertTrue(app.getModule().size()==1);
            for (Module module : app.getModule()) {
                assertTrue(module.getName().equals(app.getName()));
                assertTrue(!module.getEngines().isEmpty());
            }
        }
    }

 }
