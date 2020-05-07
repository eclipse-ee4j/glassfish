/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.server;

import java.util.LinkedList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Service;

/**
 * This service is here to start fighterfish if it is available.  This
 * is done to maintain fighterfish compatibility with older versions
 * of glassfish
 * 
 * @author jwells
 *
 */
@Service
@RunLevel(value=StartupRunLevel.VAL, mode=RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class FighterFishStartupService {
    private final static String FIGHTERFISH_START_SERVICE = "org.glassfish.osgijpa.extension.JPAStartupService";
    
    @Inject
    private ServiceLocator locator;
    
    private List<ServiceHandle<?>> fighterFishHandles;
    
    @SuppressWarnings("unused")
    @PostConstruct
    private void postConstruct() {
        fighterFishHandles = locator.getAllServiceHandles(
                BuilderHelper.createContractFilter(FIGHTERFISH_START_SERVICE));
        
        for (ServiceHandle<?> fighterFishHandle : fighterFishHandles) {
            fighterFishHandle.getService();
        }
        
    }
    
    @SuppressWarnings("unused")
    @PreDestroy
    private void preDestroy() {
        if (fighterFishHandles == null) return;
        
        List<ServiceHandle<?>> localHandles = new LinkedList<ServiceHandle<?>>(fighterFishHandles);
        fighterFishHandles.clear();
        
        for (ServiceHandle<?> handle : localHandles) {
            handle.destroy();
        }
    }

}
