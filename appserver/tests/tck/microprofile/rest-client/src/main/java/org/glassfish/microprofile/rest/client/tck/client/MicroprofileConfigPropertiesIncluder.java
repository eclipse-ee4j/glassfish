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

import java.util.Optional;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.INFO;
import static org.jboss.shrinkwrap.api.ArchivePath.SEPARATOR;

public class MicroprofileConfigPropertiesIncluder implements ApplicationArchiveProcessor {

    private static final System.Logger LOG = System.getLogger(MicroprofileConfigPropertiesIncluder.class.getName());

    private static final String MP_CONFIG_PROPERTIES_PATH =
            format("WEB-INF%sclasses%sMETA-INF%smicroprofile-config.properties", SEPARATOR, SEPARATOR, SEPARATOR);

    @Override
    public void process(Archive<?> archive, TestClass testClass) {

        // Only process web archives
        if (!(archive instanceof WebArchive)) {
            return;
        }
        final WebArchive webArchive = (WebArchive) archive;
        Optional<Node> node = findMPConfigProperties(webArchive);
        if (node.isEmpty()) {
            webArchive.add(EmptyAsset.INSTANCE, MP_CONFIG_PROPERTIES_PATH);
            LOG.log(INFO, () -> format("Adding microprofile-config.properties to archive [%s]", webArchive.getName()));
        }
    }

    private static Optional<Node> findMPConfigProperties(WebArchive archive) {
        final Node node = archive.get(MP_CONFIG_PROPERTIES_PATH);
        if (node != null) {
            LOG.log(INFO, () -> format("Discovered microprofile-config.properties at path [%s]", node.getPath()));
            return Optional.of(node);
        }
        return Optional.empty();
    }
}
