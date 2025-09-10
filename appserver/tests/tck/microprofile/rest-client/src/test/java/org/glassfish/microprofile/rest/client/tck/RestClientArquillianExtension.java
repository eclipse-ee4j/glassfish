/*
 * Copyright (c) 2023, 2025 Contributors to Eclipse Foundation.
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

package org.glassfish.microprofile.rest.client.tck;

import org.glassfish.microprofile.rest.client.tck.client.MicroprofileConfigPropertiesIncluder;
import org.glassfish.microprofile.rest.client.tck.client.WireMockLibraryIncluder;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

import static java.lang.System.Logger.Level.INFO;

public class RestClientArquillianExtension implements LoadableExtension {

    private static final System.Logger LOG = System.getLogger(RestClientArquillianExtension.class.getName());

    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        LOG.log(INFO, "Client Arquillian extension registered");
        extensionBuilder.service(ApplicationArchiveProcessor.class, MicroprofileConfigPropertiesIncluder.class);
        extensionBuilder.service(ApplicationArchiveProcessor.class, WireMockLibraryIncluder.class);
    }
}
