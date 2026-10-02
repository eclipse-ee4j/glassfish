/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.admin.config.ConfigurationUpgrade;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;


/**
 * Startup service to update existing domain.xml to the latest expected format
 *
 * @author Jerome Dochez
 */
@Service
public class UpgradeService implements ConfigurationUpgrade, PostConstruct {

    @Inject
    Domain domain;

    /*
     * Required to make gms changes before any changes to a cluster
     * or config can be saved. This is because GMS changed attribute
     * names from v2 to 3.1. (Issue 15195.)
     */
    @Inject @Named("gmsupgrade") @Optional
    ConfigurationUpgrade precondition = null;

    @Override
    public void postConstruct() {
        upgradeApplicationElements();
    }

    private void upgradeApplicationElements() {
        upgradeV3PreludeApplicationElements();
    }

    private void upgradeV3PreludeApplicationElements() {
        // in v3-prelude, engines were created under application directly,
        // in v3 final, engines are placed under individual modules composing the application
        // so if we have engines under application and not modules deployed, we need to upgrade
        List<Application> allApps = new ArrayList<Application>();
        allApps.addAll(domain.getApplications().getApplications());
        allApps.addAll(domain.getSystemApplications().getApplications());
        for (Application app : allApps) {
            if (app.getEngine()!=null && app.getEngine().size()>0 &&
                    (app.getModule()==null || app.getModule().size()==0)) {
                // we need to update the application declaration from v3 prelude,
                // we can safely assume this was a single module application
                try {
                    ConfigSupport.apply(new SingleConfigCode<Application>() {
                        public Object run(Application application) throws PropertyVetoException, TransactionFailure {
                            Module module = application.createChild(Module.class);
                            module.setName(application.getName());
                            for (Engine engine : application.getEngine()) {
                                module.getEngines().add(engine);
                            }
                            application.getModule().add(module);
                            application.getEngine().clear();
                            return null;
                        }
                    }, app);
                } catch(TransactionFailure tf) {
                    Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading application "
                            + app.getName() + " please redeploy", tf);
                    throw new RuntimeException(tf);
                }
            }
        }
    }
}
