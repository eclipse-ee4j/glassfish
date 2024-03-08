/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.server.core.jws.servedcontent;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletResponse;
import org.glassfish.appclient.server.core.AppClientDeployerHelper;
import org.glassfish.appclient.server.core.jws.RestrictedContentAdapter;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

/**
 * Represents static content that is fixed in location and content over
 * time.
 *
 * @author tjquinn
 */
public class FixedContent extends Content.Adapter implements StaticContent {

    private final File file;

    private static final Logger logger = Logger.getLogger(AppClientDeployerHelper.ACC_MAIN_LOGGER,
            AppClientDeployerHelper.LOG_MESSAGE_RESOURCE);

    public FixedContent(final File file) {
        this.file = file;
    }

    public FixedContent() {
        this.file = null;
    }

    @Override
    public File file() throws IOException {
        return file;
    }

    @Override
    public void process(String relativeURIString, Request gReq, Response gResp) throws IOException {
       /*
        * The client's cache is obsolete.  Be sure to set the
        * time header values.
        */
       gResp.setDateHeader(RestrictedContentAdapter.LAST_MODIFIED_HEADER_NAME, file().lastModified());
       gResp.setDateHeader(RestrictedContentAdapter.DATE_HEADER_NAME, System.currentTimeMillis());
        /*
        * Delegate to the Grizzly implementation.
        */
       StaticHttpHandler.sendFile(gResp, file());
       final int status = gResp.getStatus();
        if (status != HttpServletResponse.SC_OK) {
            logger.log(Level.FINE, "Could not serve content for {0} - status = {1}", new Object[]{relativeURIString, status});
        } else {
            logger.log(Level.FINE, "Served fixed content for {0}:{1}", new Object[]{gReq.getMethod(), toString()});
        }
    }

    @Override
    public String toString() {
        return "FixedContent: " + file.getAbsolutePath();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FixedContent other = (FixedContent) obj;
        if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.file != null ? this.file.hashCode() : 0);
        return hash;
    }



}
