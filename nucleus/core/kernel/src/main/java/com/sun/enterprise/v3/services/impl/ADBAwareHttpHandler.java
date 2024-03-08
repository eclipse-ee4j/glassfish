/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.http.server.util.AlternateDocBase;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;

/**
 * {@link StaticHttpHandler}, which additionally can check registered
 * {@link AlternateDocBase}s to serve requested resources.
 *
 * @author Alexey Stashok
 */
public class ADBAwareHttpHandler extends StaticHttpHandler {
    private static final Logger LOGGER = Grizzly.logger(ADBAwareHttpHandler.class);

    private final List<AlternateDocBase> alternateDocBases =
            new ArrayList<AlternateDocBase>();

    public ADBAwareHttpHandler() {
        // make sure the default "." docRoot won't be added
        super((Set<String>) null);
    }

    /**
     * Add {@link AlternateDocBase} to be checked for requested resources.
     *
     * @param urlPattern
     * @param docBase absolute path
     */
    public void addAlternateDocBase(final String urlPattern,
            final String docBase) {

        if (urlPattern == null) {
            throw new IllegalArgumentException("The urlPattern argument can't be null");
        } else if (docBase == null) {
            throw new IllegalArgumentException("The docBase argument can't be null");
        }

        AlternateDocBase alternateDocBase = new AlternateDocBase();
        alternateDocBase.setUrlPattern(urlPattern);
        alternateDocBase.setDocBase(docBase);
        alternateDocBase.setBasePath(getBasePath(docBase));

        alternateDocBases.add(alternateDocBase);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean handle(final String uri,
            final Request request, final Response response) throws Exception {
        final File file = lookupInADB(uri);
        if (file != null) {
            serveFile(file, request, response);
            return true;
        }

        return super.handle(uri, request, response);
    }

    /**
     * Get base path.
     */
    private String getBasePath(final String docBase) {
        return new File(docBase).getAbsolutePath();
    }

    private void serveFile(final File file, final Request request,
            final Response response) throws IOException {
        // If it's not HTTP GET - return method is not supported status
        if (!Method.GET.equals(request.getMethod())) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "File found {0}, but HTTP method {1} is not allowed",
                        new Object[] {file, request.getMethod()});
            }
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            response.setHeader(Header.Allow, "GET");
            return;
        }

        pickupContentType(response, file.getPath());

        addToFileCache(request, response, file);
        sendFile(response, file);
    }

    private File lookupInADB(final String uri) {
        final AlternateDocBase adb = AlternateDocBase.findMatch(
                uri, alternateDocBases);
        if (adb != null) {
            File file = new File(adb.getBasePath(), uri);
            boolean exists = file.exists();
            boolean isDirectory = file.isDirectory();

            if (exists && isDirectory) {
                file = new File(file, "/index.html");
                exists = file.exists();
                isDirectory = file.isDirectory();
            }

            if (exists && !isDirectory) {
                return file;
            }
        }

        return null;
    }
}
