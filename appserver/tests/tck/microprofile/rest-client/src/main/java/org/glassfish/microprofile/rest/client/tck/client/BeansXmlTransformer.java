/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
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

package org.glassfish.microprofile.rest.client.tck.client;

import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;

import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.INFO;

/**
 * This extension replaces beans.xml files with ones declaring the 'all' bean discovery type.
 * This is because version 3.0.1 of the TCK still deploys an empty beans.xml due to a faulty
 * assumption that CDI < 4 is still defaulting to the 'all' type.
 */
public class BeansXmlTransformer implements ApplicationArchiveProcessor {

    private static final System.Logger LOG = System.getLogger(BeansXmlTransformer.class.getName());

    private static final String LIB_DIR_PATH = "/WEB-INF/lib";

    private static final String[] BEANS_XML_PATHS = {"/META-INF/beans.xml", "/WEB-INF/beans.xml"};

    private final URL beansXmlResource;

    public BeansXmlTransformer() {
        this.beansXmlResource = getClass().getClassLoader().getResource("beans.xml");
        if (beansXmlResource == null) {
            throw new IllegalStateException("Unable to find beans.xml resource in test dir");
        }
    }

    /**
     * Listen for and process non-testable deployments. This is required as, by default,
     * ShouldThrowException annotated deployments aren't processed by ApplicationArchiveProcessors,
     * but the beans xml may still need fixing.
     *
     * @param event deployer event
     */
    protected void onEvent(@Observes BeforeDeploy event) {
        final var deployment = event.getDeployment();

        if (!deployment.testable()) {
            new BeansXmlTransformer().process(deployment.getArchive());
        }
    }

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        process(archive);
    }

    public void process(Archive<?> archive) {
        findBeansXml(archive)
            .ifPresent(beansXml -> {
                LOG.log(INFO, () -> format("Replacing beans.xml in archive [%s]", archive.getName()));
                archive.add(new UrlAsset(beansXmlResource), beansXml.getPath());
            });
        processLibraries(archive, this::process);
    }

    private static Optional<Node> findBeansXml(Archive<?> archive) {
        for (String beansXmlPath : BEANS_XML_PATHS) {
            final var node = archive.get(beansXmlPath);
            if (node != null) {
                LOG.log(INFO, () -> format("Discovered beans.xml at path [%s]", node.getPath()));
                return Optional.of(node);
            }
        }
        return Optional.empty();
    }

    private static void processLibraries(Archive<?> archive, Consumer<Archive<?>> consumer) {
        final var libDir = archive.get(LIB_DIR_PATH);

        if (libDir != null) {
            for (var node : libDir.getChildren()) {
                final var asset = node.getAsset();
                if (asset instanceof ArchiveAsset) {
                    LOG.log(INFO, () -> format("Processing subarchive [%s]", node.getPath()));
                    consumer.accept(((ArchiveAsset) asset).getArchive());
                }
            }
        }
    }
}
