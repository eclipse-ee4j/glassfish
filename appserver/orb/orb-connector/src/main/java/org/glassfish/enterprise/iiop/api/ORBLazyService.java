/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.enterprise.iiop.api;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.lang.System.Logger;
import java.nio.channels.SelectableChannel;

import org.glassfish.internal.grizzly.LazyServiceInitializer;
import org.jvnet.hk2.annotations.Service;

import static java.lang.System.Logger.Level.WARNING;

/**
 * The service is initialized by BaseContainer on the first request.
 * This class is then found by kernel using HK2 to handle the request and any other coming next.
 *
 * @author Ken Saks
 */
@Service
@Named("iiop-service")
public class ORBLazyService implements LazyServiceInitializer {
    private static final Logger LOG = System.getLogger(ORBLazyService.class.getName());

    @Inject
    private OrbInitializationNode initNode;

    @Override
    public boolean initializeService() {
        return initNode.canHandleRequest();
    }

    @Override
    public void handleRequest(SelectableChannel channel) {
        if (initNode.canHandleRequest()) {
            initNode.handleRequest(channel);
        } else {
            LOG.log(WARNING, "Cannot handle SelectableChannel request in " + ORBLazyService.class
                + ". ORB did not initialize successfully");
        }
    }
}
