/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.adapter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

import org.glassfish.admin.rest.Constants;
import org.glassfish.admin.rest.model.ResponseBody;

/**
 * This filter reformats string entities from non-success responses into arrays of message entities (when not using the
 * REST legacy mode).
 *
 * @author tmoreau
 */
@Provider
public class ExceptionFilter implements ContainerResponseFilter {

    public ExceptionFilter() {
    }

    @Override
    public void filter(ContainerRequestContext reqCtx, ContainerResponseContext resCtx) throws IOException {
        if (!Constants.MEDIA_TYPE_JSON.equals(reqCtx.getHeaderString("Accept"))) {
            // Don't wrap if using legacy mode
            return;
        }

        int status = resCtx.getStatus();
        if ((status >= 200) && (status <= 299)) {
            // don't wrap success messages
            return;
        }

        Object entity = resCtx.getEntity();
        if (!(entity instanceof String)) {
            // don't wrap null and non-String entities
            return;
        }

        // Normally the cliend sends in an X-Skip-Resource-Links header
        // to say that resource links should not be returned, and the resource
        // looks for that header in the request and, if present, tells
        // the ResponseBody constructor to ignore resource links.
        // Since we never return links from this filter, instead of looking
        // for the header, we can just always tell the ResponseBody to ignore links.
        ResponseBody rb = new ResponseBody(false);
        String errorMsg = (String) entity;
        rb.addFailure(errorMsg);
        resCtx.setEntity(rb, resCtx.getEntityAnnotations(), Constants.MEDIA_TYPE_JSON_TYPE);
    }
}
