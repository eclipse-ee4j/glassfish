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

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;
import java.net.URL;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.logging.Level.SEVERE;

/**
 * This extension performs the following duties for TCK runs:
 *  - Adding Hamcrest to each deployment, to prevent ClassNotFoundExceptions when running hamcrest tests
 *  - Replacing beans.xml files with ones declaring the 'all' bean discovery type.
 *    This is because version 3.0.1 of the TCK still deploys an empty beans.xml due to a faulty assumption that
 *    CDI < 4 is still defaulting to the 'all' type.
 */
public class ConfigArquillianExtension implements LoadableExtension, ApplicationArchiveProcessor {

    private static final Logger LOGGER = Logger.getLogger(ConfigArquillianExtension.class.getName());
    private static final String BEANS_XML_PATH = format("/META-INF%sbeans.xml", ArchivePath.SEPARATOR);
    private static final String LIB_DIR_PATH = format("WEB-INF%slib", ArchivePath.SEPARATOR);

    private final URL beansXmlResource;

    public ConfigArquillianExtension() {
        this.beansXmlResource = getClass().getClassLoader().getResource("beans.xml");
        if (beansXmlResource == null) {
            throw new IllegalStateException("Unable to find beans.xml resource in test dir");
        }
    }

    /**
     * Register this object as an Arquillian extension
     * @param extensionBuilder a context object for the extension
     */
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ApplicationArchiveProcessor.class, getClass());
        extensionBuilder.service(DeploymentExceptionTransformer.class, ConfigDeploymentExceptionTransformer.class);
    }

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (!(archive instanceof WebArchive)) {
            return;
        }
        replaceBeansXml(archive);
        addDependencies((WebArchive) archive);
    }

    private void replaceBeansXml(Archive<?> archive) {
        final var beansXml = archive.get(BEANS_XML_PATH);
        if (beansXml != null) {
            LOGGER.info(() -> format("Replacing beans.xml in archive [%s]", archive.getName()));
            archive.add(new UrlAsset(beansXmlResource), BEANS_XML_PATH);
        }
        processLibraries(archive, this::replaceBeansXml);
    }

    private void addDependencies(WebArchive archive) {
        try {
            archive.addAsLibraries(resolveDependency("org.hamcrest:hamcrest:2.2"));
        } catch (Exception e) {
            LOGGER.log(SEVERE, "Error adding dependencies", e);
        }
    }

    private static void processLibraries(Archive<?> archive, Consumer<Archive<?>> consumer) {
        final var libDir = archive.get(LIB_DIR_PATH);

        if (libDir != null) {
            libDir.getChildren()
                    .forEach(node -> {
                        final var asset = node.getAsset();
                        if (asset instanceof ArchiveAsset) {
                            LOGGER.info(() -> format("Processing subarchive [%s]", node.getPath()));
                            consumer.accept(((ArchiveAsset) asset).getArchive());
                        }
                    });
        }
    }

    private static File[] resolveDependency(String coordinates) {
        return Maven.resolver()
                .resolve(coordinates)
                .withoutTransitivity().asFile();
    }
}
