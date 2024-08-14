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

package org.apache.catalina.connector;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.WebConnection;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Context;

/**
 * Implementation of WebConnection for Servlet 3.1
 *
 * @author Amy Roh
 * @author Shing Wai Chan
 * @version $Revision: 1.23 $ $Date: 2007/07/09 20:46:45 $
 */
public class WebConnectionImpl implements WebConnection {

    private ServletInputStream inputStream;

    private ServletOutputStream outputStream;

    private Request request;

    private Response response;

    private final AtomicBoolean isClosed = new AtomicBoolean();

    // ----------------------------------------------------------- Constructor

    public WebConnectionImpl(ServletInputStream inputStream, ServletOutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * Returns an input stream for this web connection.
     *
     * @return a ServletInputStream for reading binary data
     *
     * @exception java.io.IOException if an I/O error occurs
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    /**
     * Returns an output stream for this web connection.
     *
     * @return a ServletOutputStream for writing binary data
     *
     * @exception IOException if an I/O error occurs
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException{
        return outputStream;
    }

    @Override
    public void close() throws Exception {
        // Make sure we run close logic only once
        if (isClosed.compareAndSet(false, true)) {
            if ((request != null) && (request.isUpgrade())) {
                Context context = request.getContext();
                HttpUpgradeHandler httpUpgradeHandler =
                        request.getHttpUpgradeHandler();
                Exception exception = null;
                try {
                    try {
                        context.fireContainerEvent(
                            ContainerEvent.BEFORE_UPGRADE_HANDLER_DESTROYED,
                            httpUpgradeHandler);
                        httpUpgradeHandler.destroy();
                    } finally {
                        context.fireContainerEvent(
                            ContainerEvent.AFTER_UPGRADE_HANDLER_DESTROYED,
                            httpUpgradeHandler);
                    }
                    request.setUpgrade(false);
                    if (response != null) {
                        response.setUpgrade(false);
                    }
                } finally {
                    try {
                        inputStream.close();
                    } catch(Exception ex) {
                        exception = ex;
                    }
                    try {
                        outputStream.close();
                    } catch(Exception ex) {
                        exception = ex;
                    }
                    context.fireContainerEvent(
                        ContainerEvent.PRE_DESTROY, httpUpgradeHandler);
                    request.resumeAfterService();
                }
                if (exception != null) {
                    throw exception;
                }
            }
        }
    }

    public void setRequest(Request req) {
        request = req;
    }

    public void setResponse(Response res) {
        response = res;
    }

}
