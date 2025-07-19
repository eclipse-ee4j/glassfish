/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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
package org.glassfish.microprofile.jwt.tck;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * WebArchiveUpdater updates the web archive that is created by the JWT tests.
 *
 * <p>
 * Specifically it corrects the properties file if needed, and adds glassfish-web.xml for the role
 * mapping and context root setting.
 *
 * @author Arjan Tijms
 *
 */
public class WebArchiveUpdater implements ApplicationArchiveProcessor, LoadableExtension {

    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(ApplicationArchiveProcessor.class, WebArchiveUpdater.class);
    }

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if (archive instanceof WebArchive webArchive) {
            Node metaInfConfig = webArchive.get("/META-INF/microprofile-config.properties");

            if (metaInfConfig == null) {
                if (webArchive.get("/WEB-INF/classes/publicKey.pem") != null) {
                    webArchive.addAsResource("META-INF/public-key.properties", "META-INF/microprofile-config.properties");
                } else {
                    webArchive.addAsResource("META-INF/microprofile-config.properties");
                }
            } else {
                webArchive.addAsResource(metaInfConfig.getAsset(), "META-INF/microprofile-config.properties");
                webArchive.delete("/META-INF/microprofile-config.properties");
            }

            webArchive.addAsWebInfResource("glassfish-web.xml");

            System.out.printf("WebArchive: %s\n", webArchive.toString(true));
        }
    }

}