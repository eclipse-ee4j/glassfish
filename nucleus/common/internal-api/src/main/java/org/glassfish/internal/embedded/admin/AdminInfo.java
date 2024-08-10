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

package org.glassfish.internal.embedded.admin;

import jakarta.inject.Inject;

import org.glassfish.internal.embedded.ContainerBuilder;
import org.glassfish.internal.embedded.Server;
import org.jvnet.hk2.annotations.Service;

/**
 * So far, the admin container does not require much configuration but we
 * could imagine that it will eventually support configuring wether or not
 * to start the AminGUI.
 *
 * @author Jerome Dochez
 */
@Service
public class AdminInfo implements ContainerBuilder<EmbeddedAdminContainer> {

    @Inject
    EmbeddedAdminContainer ctr;

    public EmbeddedAdminContainer create(Server server) {
        return ctr;
    }
}
