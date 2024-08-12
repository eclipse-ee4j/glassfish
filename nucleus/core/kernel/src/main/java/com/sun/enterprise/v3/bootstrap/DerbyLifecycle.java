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

/*
 * DerbyLifecycle.java
 *
 * Created on November 3, 2006, 2:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.bootstrap;

import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.LifecyclePolicy;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.common_impl.LogHelper;

import java.util.logging.Level;

/**
 *
 * @author dochez
 */
public class DerbyLifecycle implements LifecyclePolicy {

    /** Creates a new instance of DerbyLifecycle */
    public DerbyLifecycle() {
    }

    /**
     * Callback when the module enters the {@link ModuleState#READY READY} state.
     * This is a good time to do any type of one time initialization
     * or set up access to resources
     * @param module the module instance
     */
    @Override
    public void start(HK2Module module) {

        try {
            final HK2Module myModule = module;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        try {
                            Class driverClass = myModule.getClassLoader().loadClass("org.apache.derby.jdbc.EmbeddedDriver");
                            myModule.setSticky(true);
                            driverClass.newInstance();
                        } catch(ClassNotFoundException e) {
                            LogHelper.getDefaultLogger().log(Level.SEVERE, "Cannot load Derby Driver ",e);
                        } catch(java.lang.InstantiationException e) {
                            LogHelper.getDefaultLogger().log(Level.SEVERE, "Cannot instantiate Derby Driver", e);
                        } catch(IllegalAccessException e) {
                            LogHelper.getDefaultLogger().log(Level.SEVERE, "Cannot instantiate Derby Driver", e);
                        }
                    }
                    catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }


    }

    /**
     * Callback before the module starts being unloaded. The runtime will
     * free all the module resources and returned to a {@link ModuleState#NEW NEW} state.
     * @param module the module instance
     */
    @Override
    public void stop(HK2Module module) {

    }

}
