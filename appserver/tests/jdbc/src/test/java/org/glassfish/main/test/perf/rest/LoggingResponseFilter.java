/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.perf.rest;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

import java.lang.System.Logger;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Logs REST communication when response comes.
 */
public class LoggingResponseFilter implements ClientResponseFilter {

    private static final Logger LOG = System.getLogger(LoggingResponseFilter.class.getName());


    @Override
    public void filter(final ClientRequestContext requestContext, final ClientResponseContext responseContext) {
        if (!LOG.isLoggable(DEBUG)) {
            return;
        }
        LOG.log(DEBUG, "filter(requestContext, responseContext);" //
            + "\nrequestContext: {0}"
            + "\nrequest headers: {1}"
            + "\nrequest cookies: {2}"
            + "\nresponseContext: {3}"
            + "\nresponse headers: {4}"
            + "\nresponse cookies: {5}"
            + "\nresponse hasEntity: {6}",
            ReflectionToStringBuilder.toStringExclude(requestContext, "entity"),
            requestContext.getHeaders(),
            requestContext.getCookies(),
            responseContext,
            responseContext.getHeaders(),
            responseContext.getCookies(),
            responseContext.hasEntity());
    }
}
