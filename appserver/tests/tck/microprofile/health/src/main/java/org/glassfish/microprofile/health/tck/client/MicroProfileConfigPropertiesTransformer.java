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
package org.glassfish.microprofile.health.tck.client;

import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;

import static java.lang.String.format;
/**
 * This class addresses a discrepancy in MicroProfile specifications regarding the location of
 * the `microprofile-config.properties` file. Originally, MicroProfile Config specifies that
 * `microprofile-config.properties` should be located at `META-INF/` within the `classes` directory
 * of a WAR (i.e., `WEB-INF/classes/META-INF/microprofile-config.properties`). However, some implementations
 * (such as those supporting MP OpenAPI) also recognize the top-level `META-INF/microprofile-config.properties`.
 * <p>
 * The MicroProfile Health TCK, which does not explicitly require OpenAPI, still expects the file at
 * the top-level `META-INF/` location, creating inconsistency in the configuration support across
 * implementations. This divergence means implementations supporting only MP Config are restricted
 * to `WEB-INF/classes/META-INF`, while those supporting both Config and OpenAPI may support both
 * locations.
 * <p>
 * To address this in GlassFish and ensure compatibility with the Health TCK, this class leverages
 * an Arquillian `ApplicationArchiveProcessor` to move the file from the unsupported top-level
 * `META-INF` to the standard `WEB-INF/classes/META-INF` in the archive, ensuring that the tests
 * align with the expected location for all base MP APIs.
 */
public class MicroProfileConfigPropertiesTransformer implements ApplicationArchiveProcessor {
    private static final Logger LOGGER = Logger.getLogger(MicroProfileConfigPropertiesTransformer.class.getName());
    private static final String ORIGINAL_META_INF_CONFIG = "/META-INF/microprofile-config.properties";
    private static final String TARGET_META_INF_CONFIG = "/WEB-INF/classes" + ORIGINAL_META_INF_CONFIG;

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        Node node = archive.get(ORIGINAL_META_INF_CONFIG);
        if (node != null) {
            LOGGER.info(() -> format("Moving %s to %s in archive [%s]", ORIGINAL_META_INF_CONFIG, TARGET_META_INF_CONFIG, archive.getName()));
            archive.delete(ORIGINAL_META_INF_CONFIG);
            archive.add(node.getAsset(), TARGET_META_INF_CONFIG);
        }
    }


}