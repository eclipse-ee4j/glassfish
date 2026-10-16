/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.orb.http.glassfish;

import com.sun.enterprise.v3.services.impl.GrizzlyService;

import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.server.AffinityDispatcher;
import org.glassfish.orb.http.server.EjbDispatcher;
import org.glassfish.orb.http.server.InvocationRegistry;
import org.glassfish.orb.http.server.NamingDispatcher;
import org.glassfish.orb.http.server.SessionAffinity;
import org.glassfish.orb.http.server.TransactionDispatcher;
import org.jvnet.hk2.annotations.Service;

/**
 * Mounts the endpoint on the server's HTTP listeners at startup.
 *
 * <p>{@code GrizzlyService.registerEndpoint} is how anything that is not a
 * deployed application gets a context root on the ordinary listeners - which
 * means this endpoint is reached on the same ports, with the same TLS
 * configuration and the same HTTP/2 settings as everything else the server
 * serves. Nothing about HTTP/2 needs arranging here: Grizzly's Http2AddOn is
 * already on those listeners.
 *
 * <p>Runs at post-startup rather than startup. The container has to exist
 * before there is anything to dispatch to, and mounting an endpoint that
 * answers 404 to every invocation for the first seconds of a server's life is
 * a worse failure than mounting it slightly later.
 */
@Service
@RunLevel(value = PostStartupRunLevel.VAL, mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
public class OrbHttpEndpoint implements PostConstruct {

    private static final Logger LOG = System.getLogger(OrbHttpEndpoint.class.getName());

    @Inject
    private GrizzlyService grizzly;

    @Inject
    private GlassFishContainerBridge container;

    @Inject
    private GlassFishNamingBridge naming;

    @Inject
    private GlassFishSecurityBridge security;

    @Inject
    private GlassFishTransactionBridge transactions;

    @Override
    public void postConstruct() {
        // Everything, not just the mounting. This runs at a run level during
        // server startup, and a service that throws there does not merely fail
        // itself: the run level fails, GlassFish fires its error event, and
        // what that event closes includes the connector classloaders. The
        // server then comes up unable to create a JDBC pool, with a stack
        // trace that names the connector and never mentions this class.
        //
        // That is not hypothetical. The scanner below asks the OSGi framework
        // which modules are installed, and an embedded server has no OSGi
        // framework, so it raises NoClassDefFoundError for
        // org/osgi/framework/FrameworkUtil - which is a LinkageError, not an
        // Exception, and is why both are caught here.
        //
        // An endpoint that cannot mount has to stay its own problem: IIOP is
        // unaffected, and a server that starts without this endpoint is better
        // than one that does not start.
        try {
            mount();
        } catch (Exception | LinkageError e) {
            LOG.log(Level.WARNING, "could not mount " + Protocol.CONTEXT_PATH
                    + "; remote EJB over HTTP is not available on this server", e);
        }
    }

    private void mount() throws EndpointRegistrationException {
        // Before the dispatchers are built, so their defaults are chosen from
        // everything that is installed rather than from what a ServiceLoader
        // could see from inside this bundle.
        OsgiCodecScanner.scanAndRegister();

        SessionAffinity affinity = SessionAffinity.forThisNode();
        EjbDispatcher ejb = new EjbDispatcher(container, security, transactions,
                new JavaSerializationMarshaller(), new InvocationRegistry(), affinity);
        OrbHttpHandler handler = new OrbHttpHandler(ejb,
                new NamingDispatcher(naming, security, new JavaSerializationMarshaller()),
                new TransactionDispatcher(transactions, security),
                new AffinityDispatcher(affinity));
        grizzly.registerEndpoint(Protocol.CONTEXT_PATH, handler, null);
        LOG.log(Level.INFO, "Remote EJB and JNDI over HTTP mounted at {0}", Protocol.CONTEXT_PATH);
    }
}
