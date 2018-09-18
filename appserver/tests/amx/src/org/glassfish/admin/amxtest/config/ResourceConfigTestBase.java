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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.ResourceConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.util.misc.ExceptionUtil;

/**
 */
public abstract class ResourceConfigTestBase
        extends ConfigMgrTestBase {
    protected ResourceConfigTestBase() {
        super();

        StandaloneServerConfigTest.ensureDefaultInstance(getDomainRoot());
    }

    protected void
    addReference(final ResourceConfig rc) {
        final StandaloneServerConfig server =
                StandaloneServerConfigTest.ensureDefaultInstance(getDomainRoot());
        assert server != null;

        if (server.getResourceRefConfigMap().get(rc.getName()) != null) {
            warning("ResourceRefConfig already exists for: " + rc.getName());
        } else {
            try {
                server.createResourceRefConfig(rc.getName(), false);
            }
            catch (Exception e) {
                final Throwable rootCause = ExceptionUtil.getRootCause(e);

                warning("Couldn't add RefConfig to: " + Util.getObjectName(rc));
            }
        }

        assert server.getResourceRefConfigMap().get(rc.getName()) != null;
    }


}


