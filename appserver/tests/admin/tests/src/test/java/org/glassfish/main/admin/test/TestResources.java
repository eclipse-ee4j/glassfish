/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.main.admin.test;

import java.io.File;
import java.io.IOException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class TestResources {

    public static final String SIMPLE_HTML_PAGE
            = "<html>\n"
            + "    <head>\n"
            + "        <title>Simple test app</title>\n"
            + "        <meta charset=\"UTF-8\">\n"
            + "    </head>\n"
            + "    <body>\n"
            + "        <div>Simple test app</div>\n"
            + "    </body>\n"
            + "</html>";

    public static File createSimpleWarDeployment(String appName) {
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addAsWebResource(
                        new StringAsset(SIMPLE_HTML_PAGE),
                        "index.html");
        try {
            File tempFile = File.createTempFile(appName, ".war");
            war.as(ZipExporter.class).exportTo(tempFile, true);
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("WAR file creation failed for app " + appName, e);
        }
    }

}
