/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.kernel.embedded;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.v3.server.GFDomainXml;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.server.ServerEnvironmentImpl;

/**
 * Embedded domain.xml, can use externally pointed domain.xml
 *
 * @author Jerome Dochez
 * @author bhavanishankar@dev.java.net
 */
public class EmbeddedDomainXml extends GFDomainXml {

    @Inject
    StartupContext startupContext;

    @Override
    protected URL getDomainXml(ServerEnvironmentImpl env) throws IOException {
        return getDomainXml(startupContext);
    }

    static URL getDomainXml(StartupContext startupContext) throws IOException {
        String configFileURI = startupContext.getArguments().getProperty(GlassFishProperties.CONFIG_FILE_URI_PROP_NAME);
        if (configFileURI != null) { // user specified domain.xml
            return GlassFishProperties.filePathToAbsoluteURI(configFileURI).toURL();
        }
        String instanceRoot = startupContext.getArguments().getProperty(
                "com.sun.aas.instanceRoot");
        File domainXml = new File(instanceRoot, "config/domain.xml");
        if (domainXml.exists()) { // domain/config/domain.xml, if exists.
            return domainXml.toURI().toURL();
        }
        return EmbeddedDomainXml.class.getClassLoader().getResource(
                "org/glassfish/embed/domain.xml");
    }

    @Override
    protected void upgrade() {
        // for now, we don't upgrade in embedded mode...
    }

}
