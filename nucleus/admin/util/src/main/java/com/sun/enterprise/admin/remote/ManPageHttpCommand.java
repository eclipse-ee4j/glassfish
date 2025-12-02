/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.remote;

import com.sun.enterprise.admin.remote.reader.ProprietaryReaderFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.glassfish.api.admin.CommandException;

/**
 * HTTP Command to get the manual page.
 */
class ManPageHttpCommand implements HttpCommand<String> {

    private static final String MEDIATYPE_TXT = "text/plain";

    @Override
    public void prepareConnection(HttpURLConnection urlConnection) {
        urlConnection.setRequestProperty("Accept", MEDIATYPE_TXT);
    }

    @Override
    public String useConnection(HttpURLConnection urlConnection) throws CommandException, IOException {
        return ProprietaryReaderFactory.<String> getReader(String.class, urlConnection.getContentType())
            .readFrom(urlConnection.getInputStream(), urlConnection.getContentType());
    }
}
