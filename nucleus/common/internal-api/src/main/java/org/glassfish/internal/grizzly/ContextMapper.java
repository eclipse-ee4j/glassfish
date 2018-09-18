/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.grizzly;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.naming.NamingContext;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * Extended that {@link Mapper} that prevent the WebContainer to unregister the current {@link Mapper} configuration.
 *
 * @author Jeanfrancois Arcand
 */
@Service
@ContractsProvided({ContextMapper.class, Mapper.class})
public class ContextMapper extends Mapper {
    protected final Logger logger;
    protected HttpHandler adapter;
    // The id of the associated network-listener
    private String id;

    public ContextMapper() {
        this(Logger.getAnonymousLogger());
    }

    public ContextMapper(final Logger logger) {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWrapper(final String hostName, final String contextPath, final String path,
        final Object wrapper, final boolean jspWildCard, final String servletName,
        final boolean isEmptyPathSpecial) {
        super.addWrapper(hostName, contextPath, path, wrapper, jspWildCard,
                servletName, isEmptyPathSpecial);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Wrapper-Host: {0} contextPath {1} wrapper {2} "
                    + "path {3} jspWildcard {4} servletName {5} isEmptyPathSpecial {6}",
                    new Object[]{hostName, contextPath, wrapper, path, jspWildCard,
                        servletName, isEmptyPathSpecial});
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addHost(final String name, final String[] aliases,
        final Object host) {

        super.addHost(name, aliases, host);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Host-Host: {0} aliases {1} host {2}",
                    new Object[]{name, Arrays.toString(aliases), host});
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addContext(final String hostName, final String path, final Object context,
        final String[] welcomeResources, final NamingContext resources) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Context-Host: {0} path {1} context {2} port {3}",
                    new Object[]{hostName, path, context, getPort()});
        }
        // The WebContainer is registering new Context. In that case, we must
        // clean all the previously added information, specially the
        // MappingData.wrapper info as this information cannot apply
        // to this Container.
        if (adapter != null && "org.apache.catalina.connector.CoyoteAdapter".equals(adapter.getClass().getName())) {
            removeContext(hostName, path);
        }
        super.addContext(hostName, path, context, welcomeResources, resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeHost(final String name) {
        // Do let the WebContainer unconfigure us.
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Faking removal of host: {0}", name);
        }
    }

    public void setHttpHandler(final HttpHandler adapter) {
        this.adapter = adapter;
    }

    public HttpHandler getHttpHandler() {
        return adapter;
    }

    /**
     * Sets the id of the associated http-listener on this mapper.
     */
    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
