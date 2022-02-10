/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.config.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * This extension performs the following duties for TCK runs:
 *  - Adding Hamcrest to each deployment, to prevent ClassNotFoundExceptions when running hamcrest tests
 */
public class ConfigArquillianExtension implements LoadableExtension, ApplicationArchiveProcessor {

    private static final Logger LOGGER = Logger.getLogger(ConfigArquillianExtension.class.getName());

    /**
     * Register this object as an Arquillian extension
     * @param extensionBuilder a context object for the extension
     */
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ApplicationArchiveProcessor.class, getClass());
    }

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (!(archive instanceof WebArchive)) {
            return;
        }
        addDependencies((WebArchive) archive);
    }

    private void addDependencies(WebArchive archive) {
        try {
            archive.addAsLibraries(resolveDependency("org.hamcrest:hamcrest:2.2"));
        } catch (Exception e) {
            LOGGER.log(SEVERE, "Error adding dependencies", e);
        }
    }

    private static File[] resolveDependency(String coordinates) {
        return Maven.resolver()
                .resolve(coordinates)
                .withoutTransitivity().asFile();
    }
}
