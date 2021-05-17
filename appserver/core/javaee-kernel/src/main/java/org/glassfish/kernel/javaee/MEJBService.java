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

package org.glassfish.kernel.javaee;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.InitRunLevel;
import org.glassfish.internal.api.Globals;
import org.glassfish.api.naming.GlassfishNamingManager;

import com.sun.logging.LogDomains;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * MEJB service to register mejb with a temporary NamingObjectProxy at server
 * start up time
 */
@Service
@RunLevel(InitRunLevel.VAL)
public class MEJBService implements PostConstruct {

    // we need to inject Globals as it used by the naming manager and
    // therefore needs to be allocated.
    @Inject
    Globals globals;

    @Inject
    ServiceLocator habitat;

    @Inject
    Provider<GlassfishNamingManager> gfNamingManagerProvider;

    private static final Logger _logger = LogDomains.getLogger(
        MEJBService.class, LogDomains.EJB_LOGGER);

    public void postConstruct() {
        GlassfishNamingManager gfNamingManager =
            gfNamingManagerProvider.get();

        MEJBNamingObjectProxy mejbProxy =
            new MEJBNamingObjectProxy(habitat);
        for(String next : MEJBNamingObjectProxy.getJndiNames()) {
            try {
                gfNamingManager.publishObject(next, mejbProxy, true);
            } catch (Exception e) {
                _logger.log(Level.WARNING, "Problem in publishing temp proxy for MEJB: " +
                    e.getMessage(), e);
            }
        }
    }
}
