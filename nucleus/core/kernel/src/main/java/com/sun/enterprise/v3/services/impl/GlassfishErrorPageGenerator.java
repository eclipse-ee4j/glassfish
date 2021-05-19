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

import com.sun.appserv.server.util.Version;
import org.glassfish.grizzly.http.server.ErrorPageGenerator;
import org.glassfish.grizzly.http.server.Request;

/**
 * Glassfish default {@link ErrorPageGenerator}.
 */
public class GlassfishErrorPageGenerator implements ErrorPageGenerator {

    @Override
    public String generate(final Request request, final int status,
            final String reasonPhrase, final String description,
            final Throwable exception) {


        if (status == 404) {
            return HttpUtils.getErrorPage(Version.getVersion(),
                    "The requested resource is not available.", "404");
        } else {
            return HttpUtils.getErrorPage(Version.getVersion(),
                    "The server encountered an internal error that prevented it from fulfilling this request.", "500");
        }
   }
}

