/*
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

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.bootstrap.PopulatorPostProcessor;
import org.glassfish.hk2.utilities.DescriptorImpl;
import jakarta.inject.Inject;

/**
 * Kernel's decoration for embedded environment.
 *
 * @author Jerome Dochez
 */
public class EmbeddedInhabitantsParser implements PopulatorPostProcessor {

    @Inject
    private ServiceLocator serviceLocator;

    public String getName() {
        return "Embedded";
    }

//    private void decorate(InhabitantsParser parser) {
//
//        // we don't want to reconfigure the loggers.
//
//        parser.drop(AdminConsoleAdapter.class);
//
//        String enableCLI = System.getenv("GF_EMBEDDED_ENABLE_CLI");
//        if (enableCLI == null || !enableCLI.equalsIgnoreCase("true")) {
//            parser.drop(PublicAdminAdapter.class);
//            parser.drop(LogManagerService.class);
//            parser.drop(PrivateAdminAdapter.class);
//        }
//        parser.replace(GFDomainXml.class, EmbeddedDomainXml.class);
//
//        parser.replace(DomainXmlPersistence.class, EmbeddedDomainPersistence.class);
//
//    }

    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator, DescriptorImpl descriptorImpl) {

        // we don't want to reconfigure the loggers.

        boolean skip = false;

        if ("com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter".equals(
            descriptorImpl.getImplementation())) {
            skip = true;
        }

        String enableCLI = System.getenv("GF_EMBEDDED_ENABLE_CLI");
        if (enableCLI == null || !enableCLI.equalsIgnoreCase("true")) {

            if ("com.sun.enterprise.v3.admin.PublicAdminAdapter".equals(
                descriptorImpl.getImplementation())
                || "com.sun.enterprise.server.logging.LogManagerService".equals(
                    descriptorImpl.getImplementation())
                || "com.sun.enterprise.v3.admin.PrivateAdminAdapter".equals(
                    descriptorImpl.getImplementation())) {
                skip = true;
            }
        }

        if ("com.sun.enterprise.v3.server.GFDomainXml".equals(
            descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation("org.glassfish.kernel.embedded.EmbeddedDomainXml");
            descriptorImpl.setScope(PerLookup.class.getCanonicalName());
        } else if ("com.sun.enterprise.v3.server.DomainXmlPersistence".equals(
            descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation("org.glassfish.kernel.embedded.EmbeddedDomainPersistence");
            descriptorImpl.setScope(PerLookup.class.getCanonicalName());
        } else if ("org.glassfish.web.deployment.archivist.WebArchivist".equals(descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation("org.glassfish.web.embed.impl.EmbeddedWebArchivist");
            descriptorImpl.setScope(PerLookup.class.getCanonicalName());
        } else if ("org.glassfish.web.WebEntityResolver".equals(descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation("org.glassfish.web.embed.impl.EmbeddedWebEntityResolver");
            descriptorImpl.setScope(PerLookup.class.getCanonicalName());
        }

        if (!skip) {
            return descriptorImpl;
        }
        return null;
    }
}

