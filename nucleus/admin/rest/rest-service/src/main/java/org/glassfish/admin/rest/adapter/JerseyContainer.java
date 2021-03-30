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

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 * This class represents our interface to Jersey pluggable container mechanism. This allows us to use our ReST admin
 * resources in more than containers, e.g., jersey-grizzly-container, jersey-servlet-container, etc. Ideally we should
 * have abstracted out Request and Response as well, but that will be done separately. For now, while using our ReST
 * admin resources in any other container, we will have to adapt their Request and Response interfaces to Grizzly
 * Request and Response interfaces.
 *
 * @author sanjeeb.sahoo@oracle.com
 */
public interface JerseyContainer {
    // TODO(Sahoo): Abstract out Request and Response instead of using Grizzly Request and Response.
    // It requires more effort than just interface, because many other places in GlassFish admin infrastructure,
    // we reference Grizzly directly.
    void service(Request request, Response response) throws Exception;
}
