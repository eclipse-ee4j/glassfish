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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.glassfish.api.admin.CommandException;

/**
 * Interface to enable factoring out common HTTP connection management code.
 * <p>
 * The implementation of this interface must implement
 * <ul>
 * <li>{@link #prepareConnection} - to perform all pre-connection configuration - set headers, chunking, etc. as well as
 * writing any payload to the outbound connection. In short anything needed prior to the URLConnection#connect
 * invocation.
 * <p>
 * The caller will invoke this method after it has invoked {@link URL#openConnection} but before it invokes
 * {@link URL#connect}.
 * <li>{@link #useConnection} - to read from the input stream, etc. The caller will invoke this method after it has
 * successfully invoked {@link URL#connect}.
 * </ul>
 * Because the caller might have to work with multiple URLConnection objects (as it follows redirection, for example)
 * this contract allows the caller to delegate to the HttpCommand implementation multiple times to configure each of the
 * URLConnections objects, then to invoke useConnection only once after it has the "final" URLConnection object. For
 * this reason be sure to implement prepareConnection so that it can be invoked multiple times.
 *
 */
interface HttpCommand<T> {

    /**
     * Configures the HttpURLConnection (headers, chuncking, etc.) according to the needs of this use of the connection and
     * then writes any required outbound payload to the connection.
     * <p>
     * This method might be invoked multiple times before the connection is actually connected, so it should be serially
     * reentrant. Note that the caller will
     *
     * @param urlConnection the connection to be configured
     */
    void prepareConnection(HttpURLConnection urlConnection) throws IOException;

    /**
     * Uses the configured and connected connection to read data, process it, etc.
     *
     * @param urlConnection the connection to be used
     * @throws CommandException
     * @throws IOException
     */
    T useConnection(HttpURLConnection urlConnection) throws CommandException, IOException;
}
