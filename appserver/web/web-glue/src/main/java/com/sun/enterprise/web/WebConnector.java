/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web;

import com.sun.enterprise.web.connector.coyote.PECoyoteConnector;

import org.apache.catalina.LifecycleException;
import org.glassfish.internal.grizzly.V3Mapper;

/**
 * A CoyoteConnector subclass which "wraps around" an existing Grizzly
 * SelectorThread that is being started and stopped outside of the lifecycle
 * of this CoyoteConnector subclass (the SelectorThread is started and
 * stopped as part of the GrizzlyAdapter lifecycle).
 *
 * The only purpose of this WebConnector is to start and stop its associated
 * MapperListener, which populates the Catalina Mapper that is used by the
 * CoyoteAdapter which gets registered with the GrizzlyAdapter for web
 * context endpoints.
 *
 * @author jluehe
 */
public class WebConnector extends PECoyoteConnector {

    /**
     * Constructor
     */
    public WebConnector(WebContainer webContainer) {
        super(webContainer);
    }

    @Override
    public void initialize() throws LifecycleException {

        V3Mapper v3Mapper = null;
        if (mapper == null) {
            v3Mapper = new V3Mapper();
            mapper = v3Mapper;
        }

        super.initialize();

        if (v3Mapper != null) {
            v3Mapper.setHttpHandler(getHandler());
        } else if (mapper instanceof V3Mapper) {
            ((V3Mapper) mapper).setHttpHandler(getHandler());
        }
    }

}
