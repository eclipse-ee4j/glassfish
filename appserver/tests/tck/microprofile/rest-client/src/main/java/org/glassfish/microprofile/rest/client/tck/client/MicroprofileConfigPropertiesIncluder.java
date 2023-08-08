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

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static java.lang.String.format;
import static java.lang.System.Logger.Level.INFO;

public class MicroprofileConfigPropertiesIncluder implements ApplicationArchiveProcessor {

    private static final System.Logger LOG = System.getLogger(MicroprofileConfigPropertiesIncluder.class.getName());

    @Override
    public void process(Archive<?> archive, TestClass testClass) {

        // Only process web archives
        if (!(archive instanceof WebArchive)) {
            return;
        }
        final WebArchive webArchive = (WebArchive) archive;
        webArchive.addAsManifestResource(EmptyAsset.INSTANCE, "microprofile-config.properties");
        LOG.log(INFO, () -> format("Adding microprofile-config.properties to archive [%s]", archive.getName()));
    }
}
