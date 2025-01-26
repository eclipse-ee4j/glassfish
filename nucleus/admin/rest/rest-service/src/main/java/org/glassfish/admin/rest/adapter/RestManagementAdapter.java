/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.admin.restconnector.Constants;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.jvnet.hk2.annotations.Service;

/**
 * Adapter for REST management interface
 *
 * @author Rajeshwar Patil , Ludovic Champenois
 * @author sanjeeb.sahoo@oracle.com
 */
@Service(name = Constants.REST_MANAGEMENT_ADAPTER)
public class RestManagementAdapter extends RestAdapter {

    public RestManagementAdapter() {
        super(new RestManagementResourceProvider());
    }

    @Override
    public void service(Request req, Response res) {
        req.setServerName(req.getLocalName());
        req.setServerPort(req.getLocalPort());
        super.service(req, res);
    }
}
