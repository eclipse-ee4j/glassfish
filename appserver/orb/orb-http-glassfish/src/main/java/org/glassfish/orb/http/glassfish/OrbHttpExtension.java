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

import java.io.IOException;

import org.glassfish.orb.http.server.ServerExchange;
import org.jvnet.hk2.annotations.Contract;

/**
 * Something else served under the endpoint's context root, by a module this
 * one does not depend on.
 *
 * <p>The endpoint finds these when it mounts, as services, so a module that
 * adds one is optional: leaving it out of the distribution leaves remote EJB
 * and JNDI over HTTP exactly as they are. An extension that fails to start is
 * logged and left out, for the same reason.
 *
 * <p>Extensions are asked before the endpoint's own routes, so a path one
 * claims never reaches the EJB dispatcher.
 */
@Contract
public interface OrbHttpExtension {

    /**
     * @param path the request URI
     * @return whether this extension answers requests for the path
     */
    boolean serves(String path);

    /**
     * Answers a request whose path {@link #serves} claimed.
     *
     * @param path the request URI, as {@link #serves} saw it
     * @param exchange the request and its response
     * @throws IOException if the response cannot be written
     */
    void dispatch(String path, ServerExchange exchange) throws IOException;
}
