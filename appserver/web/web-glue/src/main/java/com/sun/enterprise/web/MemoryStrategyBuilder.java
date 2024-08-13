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

import com.sun.enterprise.util.uuid.UuidGenerator;
import com.sun.enterprise.web.session.PersistenceType;

import java.text.MessageFormat;
import java.util.logging.Level;

import org.apache.catalina.Context;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.StandardManager;
import org.glassfish.web.LogFacade;
import org.glassfish.web.deployment.runtime.SessionManager;
import org.jvnet.hk2.annotations.Service;

@Service(name="memory")
public class MemoryStrategyBuilder extends BasePersistenceStrategyBuilder {

    public void initializePersistenceStrategy(
            Context ctx,
            SessionManager smBean,
            ServerConfigLookup serverConfigLookup) {

        super.initializePersistenceStrategy(ctx, smBean, serverConfigLookup);

        String persistenceType = PersistenceType.MEMORY.getType();

        String ctxPath = ctx.getPath();
        if(ctxPath != null && !ctxPath.equals("")) {
            if (_logger.isLoggable(Level.FINE)) {
                Object[] params = { ctx.getPath(), persistenceType };
                _logger.log(Level.FINE, LogFacade.NO_PERSISTENCE, params);
            }
        }

        StandardManager mgr = new StandardManager();
        if (sessionFilename == null) {
            mgr.setPathname(sessionFilename);
        } else {
            mgr.setPathname(prependContextPathTo(sessionFilename, ctx));
        }

        mgr.setMaxActiveSessions(maxSessions);

        // START OF 6364900
        mgr.setSessionLocker(new PESessionLocker(ctx));
        // END OF 6364900

        ctx.setManager(mgr);

        // START CR 6275709
        if (sessionIdGeneratorClassname != null &&
                sessionIdGeneratorClassname.length() > 0) {
            try {
                UuidGenerator generator = (UuidGenerator)
                    serverConfigLookup.loadClass(
                        sessionIdGeneratorClassname).newInstance();
                mgr.setUuidGenerator(generator);
            } catch (Exception ex) {
                String msg = _rb.getString(LogFacade.UNABLE_TO_LOAD_SESSION_UUID_GENERATOR);
                msg = MessageFormat.format(msg, sessionIdGeneratorClassname);
                _logger.log(Level.SEVERE, msg, ex);
            }
        }
        // END CR 6275709

        if (!((StandardContext)ctx).isSessionTimeoutOveridden()) {
            mgr.setMaxInactiveInterval(sessionMaxInactiveInterval);
        }
    }
}
