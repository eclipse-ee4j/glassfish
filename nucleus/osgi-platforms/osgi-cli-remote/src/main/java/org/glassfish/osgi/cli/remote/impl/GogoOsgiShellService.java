/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.osgi.cli.remote.impl;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.glassfish.api.ActionReport;
import org.glassfish.common.util.io.EmptyInputStream;

import static org.glassfish.osgi.cli.remote.impl.SessionOperation.EXECUTE;
import static org.glassfish.osgi.cli.remote.impl.SessionOperation.LIST;
import static org.glassfish.osgi.cli.remote.impl.SessionOperation.NEW;
import static org.glassfish.osgi.cli.remote.impl.SessionOperation.STOP;

/**
 * Service using {@link CommandProcessor} and supporting sessions.
 *
 * @author David Matejcek
 */
class GogoOsgiShellService extends OsgiShellService {
    private static final Logger LOG = Logger.getLogger(GogoOsgiShellService.class.getName());

    private static final Map<String, RemoteCommandSession> SESSIONS = new ConcurrentHashMap<>();
    private final CommandProcessor processor;
    private final SessionOperation sessionOperation;
    private final String sessionId;

    GogoOsgiShellService(Object service, SessionOperation sessionOperation, String sessionId, ActionReport report) {
        super(report);
        this.processor = (CommandProcessor) service;
        this.sessionOperation = sessionOperation;
        this.sessionId = sessionId;
    }

    @Override
    protected void execCommand(String cmdName, String cmd) throws Exception {
        final InputStream in = new EmptyInputStream();
        if (sessionOperation == null) {
            if (ASADMIN_OSGI_SHELL.equals(cmdName)) {
                stdout.println("gogo");
            } else {
                try (CommandSession session = processor.createSession(in, stdout, stderr)) {
                    final Object result = session.execute(cmd);
                    if (result instanceof String) {
                        stdout.println(result.toString());
                    }
                }
            }
        } else if (sessionOperation == NEW) {
            final CommandSession session = processor.createSession(in, stdout, stderr);
            final RemoteCommandSession remote = new RemoteCommandSession(session);
            LOG.log(Level.FINE, "Remote session established: {0}", remote.getId());
            SESSIONS.put(remote.getId(), remote);
            stdout.println(remote.getId());
        } else if (sessionOperation == LIST) {
            for (final String id : SESSIONS.keySet()) {
                stdout.println(id);
            }
        } else if (sessionOperation == EXECUTE) {
            final RemoteCommandSession remote = SESSIONS.get(sessionId);
            final CommandSession session = remote.attach(in, stdout, stderr);
            final Object result = session.execute(cmd);
            if (result instanceof String) {
                stdout.println(result.toString());
            }
            remote.detach();
        } else if (sessionOperation == STOP) {
            final RemoteCommandSession remote = SESSIONS.remove(sessionId);
            if (remote != null) {
                final CommandSession session = remote.attach(in, stdout, stderr);
                session.close();
            }
            LOG.log(Level.FINE, "Remote session closed: {0}", sessionId);
        }
    }
}
