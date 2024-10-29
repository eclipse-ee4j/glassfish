/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.health.tck;

import java.util.logging.Logger;

import org.glassfish.microprofile.health.tck.client.BeansXmlTransformer;
import org.glassfish.microprofile.health.tck.client.ConfigDeploymentExceptionTransformer;
import org.glassfish.microprofile.health.tck.client.LibraryIncluder;
import org.glassfish.microprofile.health.tck.client.MicroProfileConfigPropertiesTransformer;
import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class ConfigArquillianExtension implements LoadableExtension {

    private static final Logger LOGGER = Logger.getLogger(ConfigArquillianExtension.class.getName());

    /**
     * Register this object as an Arquillian extension
     * @param extensionBuilder a context object for the extension
     */
    @Override
    public void register(ExtensionBuilder extensionBuilder) {

        LOGGER.info("Client Arquillian extension registered");

        extensionBuilder.service(ApplicationArchiveProcessor.class, BeansXmlTransformer.class);
        extensionBuilder.service(ApplicationArchiveProcessor.class, MicroProfileConfigPropertiesTransformer.class);
        extensionBuilder.service(ApplicationArchiveProcessor.class, LibraryIncluder.class);
        extensionBuilder.service(DeploymentExceptionTransformer.class, ConfigDeploymentExceptionTransformer.class);

        // Register this class as an Arquillian event observer
        extensionBuilder.observer(BeansXmlTransformer.class);
    }
}
