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

package com.sun.enterprise.web;

import java.util.logging.Level;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.FileStore;
import org.apache.catalina.session.PersistentManager;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.runtime.SessionManager;
import org.jvnet.hk2.annotations.Service;

@Service(name="file")
public class FileStrategyBuilder extends BasePersistenceStrategyBuilder {

    public void initializePersistenceStrategy(
            Context ctx,
            SessionManager smBean,
            ServerConfigLookup serverConfigLookup) {

        if (_logger.isLoggable(Level.INFO)) {
            _logger.log(Level.INFO, LogFacade.FILE_PERSISTENCE, ctx.getPath());
        }

        super.initializePersistenceStrategy(ctx, smBean, serverConfigLookup);

        PersistentManager mgr = new PersistentManager();
        mgr.setMaxActiveSessions(maxSessions);
        mgr.setMaxIdleBackup(0);     // FIXME: Make configurable

        FileStore store = new FileStore();
        store.setDirectory(directory);
        mgr.setStore(store);

        //START OF 6364900
        mgr.setSessionLocker(new PESessionLocker(ctx));
        //END OF 6364900

        ctx.setManager(mgr);

        if(!((StandardContext)ctx).isSessionTimeoutOveridden()) {
            mgr.setMaxInactiveInterval(sessionMaxInactiveInterval);
        }

        // Special code for Java Server Faces
        if (ctx.findParameter(JSF_HA_ENABLED) == null) {
            ctx.addParameter(JSF_HA_ENABLED, "true");
        }
    }
}
