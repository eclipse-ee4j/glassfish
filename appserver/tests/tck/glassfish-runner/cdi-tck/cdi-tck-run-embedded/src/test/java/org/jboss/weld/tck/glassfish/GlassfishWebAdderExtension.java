/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.jboss.weld.tck.glassfish;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;

/**
 * Add a glassfish-web.xml file to the deployment to disable classloader delegation.
 */
public class GlassfishWebAdderExtension implements ApplicationArchiveProcessor {

    @Override
    public void process(Archive<?> archive, TestClass tc) {
        archive.add(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?><glassfish-web-app><class-loader delegate=\"false\"/></glassfish-web-app>"), "WEB-INF/glassfish-web.xml");
    }

}