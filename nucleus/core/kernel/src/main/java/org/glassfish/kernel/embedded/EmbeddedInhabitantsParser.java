/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.kernel.embedded;

import com.sun.enterprise.server.logging.LogManagerService;
import com.sun.enterprise.v3.admin.PrivateAdminAdapter;
import com.sun.enterprise.v3.admin.PublicAdminAdapter;
import com.sun.enterprise.v3.admin.adapter.AdminConsoleAdapter;
import com.sun.enterprise.v3.server.DomainXmlPersistence;
import com.sun.enterprise.v3.server.GFDomainXml;

import jakarta.inject.Inject;

import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * Kernel's decoration for embedded environment.
 *
 * @author Jerome Dochez
 */
public class EmbeddedInhabitantsParser implements PopulatorPostProcessor {

    @Inject
    private ServiceLocator serviceLocator;


    @Override
    public DescriptorImpl process(ServiceLocator serviceLocator, DescriptorImpl descriptorImpl) {

        // we don't want to reconfigure the loggers.

        boolean skip = false;

        if (AdminConsoleAdapter.class.getCanonicalName().equals(descriptorImpl.getImplementation())) {
            skip = true;
        }

        String enableCLI = System.getenv("GF_EMBEDDED_ENABLE_CLI");
        if (enableCLI == null || !enableCLI.equalsIgnoreCase("true")) {

            if (PublicAdminAdapter.class.getCanonicalName().equals(descriptorImpl.getImplementation())
                || LogManagerService.class.getCanonicalName().equals(descriptorImpl.getImplementation())
                || PrivateAdminAdapter.class.getCanonicalName().equals(descriptorImpl.getImplementation())) {
                skip = true;
            }
        }

        if (GFDomainXml.class.getCanonicalName().equals(descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation(EmbeddedDomainXml.class.getCanonicalName());
        }

        if (DomainXmlPersistence.class.getCanonicalName().equals(descriptorImpl.getImplementation())) {
            descriptorImpl.setImplementation(EmbeddedDomainPersistence.class.getCanonicalName());
        }

        if (!skip) {
            return descriptorImpl;
        }
        return null;
    }
}

