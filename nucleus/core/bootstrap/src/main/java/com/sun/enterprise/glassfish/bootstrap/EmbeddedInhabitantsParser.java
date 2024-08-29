/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * Kernel's decoration for embedded environment.
 *
 * @author Jerome Dochez
 */
// Note: Used in a service file!
public class EmbeddedInhabitantsParser implements PopulatorPostProcessor {
    private static final Logger LOG = LogFacade.BOOTSTRAP_LOGGER;

    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator, DescriptorImpl descriptorImpl) {
        boolean skip = false;

        if ("com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter".equals(
            descriptorImpl.getImplementation())) {
            skip = true;
        }

        String enableCLI = System.getenv("GF_EMBEDDED_ENABLE_CLI");
        if (enableCLI == null || !enableCLI.equalsIgnoreCase("true")) {

            if ("com.sun.enterprise.v3.admin.PublicAdminAdapter".equals(descriptorImpl.getImplementation())
                || "com.sun.enterprise.server.logging.LogManagerService".equals(descriptorImpl.getImplementation())
                || "com.sun.enterprise.v3.admin.PrivateAdminAdapter".equals(descriptorImpl.getImplementation())) {
                skip = true;
            }
        }

        if ("com.sun.enterprise.v3.server.GFDomainXml".equals(descriptorImpl.getImplementation())) {
            replaceImpl(descriptorImpl, "org.glassfish.kernel.embedded.EmbeddedDomainXml");
        } else if ("com.sun.enterprise.v3.server.DomainXmlPersistence".equals(descriptorImpl.getImplementation())) {
            replaceImpl(descriptorImpl, "org.glassfish.kernel.embedded.EmbeddedDomainPersistence");
        } else if ("org.glassfish.web.deployment.archivist.WebArchivist".equals(descriptorImpl.getImplementation())) {
            replaceImpl(descriptorImpl, "org.glassfish.web.embed.impl.ScatteredWebArchivist");
        } else if ("org.glassfish.web.WebEntityResolver".equals(descriptorImpl.getImplementation())) {
            replaceImpl(descriptorImpl, "org.glassfish.web.embed.impl.EmbeddedWebEntityResolver");
        }

        return skip ? null : descriptorImpl;
    }


    private void replaceImpl(DescriptorImpl descriptor, String newImplementation) {
        LOG.log(Level.CONFIG, "Replacing archivist implementation {0} with {1}",
            new Object[] {descriptor.getImplementation(), newImplementation});
        descriptor.setImplementation(newImplementation);
        descriptor.setScope(PerLookup.class.getCanonicalName());
    }
}

