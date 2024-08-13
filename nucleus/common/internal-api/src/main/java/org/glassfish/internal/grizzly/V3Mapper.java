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


import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.naming.NamingContext;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * Extended that {@link Mapper} that prevent the WebContainer to unregister
 * the current {@link Mapper} configuration.
 *
 * @author Jeanfrancois Arcand
 */
@Service
@ContractsProvided({V3Mapper.class, Mapper.class})
public class V3Mapper extends ContextMapper {

    private static final String ADMIN_LISTENER = "admin-listener";
    private static final String ADMIN_VS = "__asadmin";


    public V3Mapper() {
    }


    public V3Mapper(Logger logger) {
        super(logger);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addWrapper(String hostName, String contextPath, String path,
            Object wrapper, boolean jspWildCard) {
        super.addWrapper(hostName, contextPath, path, wrapper, jspWildCard);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Wrapper-Host: {0} contextPath {1} wrapper {2} path {3} jspWildcard {4}",
                    new Object[]{hostName, contextPath, wrapper, path, jspWildCard});
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addHost(String name, String[] aliases, Object host) {

        // Prevent any admin related artifacts from being registered on a
        // non-admin listener, and vice versa
        if (ADMIN_LISTENER.equals(getId()) && !ADMIN_VS.equals(name) ||
            !ADMIN_LISTENER.equals(getId()) && ADMIN_VS.equals(name)) {
            return;
        }

        super.addHost(name, aliases, host);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addContext(String hostName, String path, Object context,
            String[] welcomeResources, NamingContext resources) {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Context-Host: {0} path {1} context {2} port {3}",
                    new Object[]{hostName, path, context, getPort()});
        }

        // Prevent any admin related artifacts from being registered on a
        // non-admin listener, and vice versa
        if (ADMIN_LISTENER.equals(getId()) && !ADMIN_VS.equals(hostName) ||
            !ADMIN_LISTENER.equals(getId()) && ADMIN_VS.equals(hostName)) {
            return;
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
}
