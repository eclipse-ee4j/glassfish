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

import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.PostStartupRunLevel;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.fory.grpc.ForyGrpcCatalog;
import org.glassfish.orb.http.fory.grpc.ForyGeneratedServiceRegistry;
import org.glassfish.orb.http.fory.grpc.ForyGrpcServerAdapter;
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

    @Inject
    private EjbNameIndex index;

    @Override
    public void postConstruct() {
        // Before the dispatchers are built, so their defaults are chosen from
        // everything that is installed rather than from what a ServiceLoader
        // could see from inside this bundle.
        OsgiCodecScanner.scanAndRegister();

        SessionAffinity affinity = SessionAffinity.forThisNode();
        ForyGrpcCatalog foryCatalog = new ForyGrpcCatalog();
        index.foryIdl().forEach(foryCatalog::register);
        ForyGeneratedServiceRegistry registry = index.foryRegistry();
        ForyGrpcServerAdapter foryGrpc = new ForyGrpcServerAdapter(registry,
                (path, exchange) -> {
                    EjbNameIndex.ForyRoute route = index.foryRoute(path);
                    if (route == null) throw new IllegalStateException("unknown Fory route: " + path);
                    Object securityToken = security.establish(exchange.authenticatedUser());
                    try {
                        var key = container.resolve(route.app(), route.module(), null, route.bean(), null);
                        Object target = container.getTargetObject(key, route.view());
                        return new ForyGrpcServerAdapter.Target() {
                            @Override public Object value() { return target; }
                            @Override public void close() {
                                try {
                                    container.releaseTargetObject(target);
                                } finally {
                                    security.clear(securityToken);
                                }
                            }
                        };
                    } catch (RuntimeException | Error failure) {
                        security.clear(securityToken);
                        throw failure;
                    }
                },
                16 * 1024 * 1024);
        EjbDispatcher ejb = new EjbDispatcher(container, security, transactions,
                new JavaSerializationMarshaller(), new InvocationRegistry(), affinity);
        OrbHttpHandler handler = new OrbHttpHandler(ejb,
                new NamingDispatcher(naming, security, new JavaSerializationMarshaller()),
                new TransactionDispatcher(transactions, security),
                new AffinityDispatcher(affinity), foryCatalog, foryGrpc);
        try {
            grizzly.registerEndpoint(Protocol.CONTEXT_PATH, handler, null);
            LOG.log(Level.INFO, "Remote EJB and JNDI over HTTP mounted at {0}", Protocol.CONTEXT_PATH);
        } catch (Exception e) {
            // Not fatal to the server: IIOP is unaffected, and a server that
            // starts without this endpoint is better than one that does not
            // start.
            LOG.log(Level.WARNING, "could not mount " + Protocol.CONTEXT_PATH, e);
        }
    }
}
