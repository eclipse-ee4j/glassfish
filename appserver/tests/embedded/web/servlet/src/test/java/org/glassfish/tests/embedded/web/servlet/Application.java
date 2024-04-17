/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.embedded.web.servlet;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.archive.ScatteredArchive;
import org.glassfish.tests.utils.ServerUtils;


public class Application implements Closeable {

    private static final int HTTP_PORT = ServerUtils.getFreePort();

    private final GlassFish glassfish;
    private final String name;

    private Application(final GlassFish glassfish, final String name) {
        this.glassfish = glassfish;
        this.name = name;
    }


    public URL getEndpoint() {
        try {
            return new URI("http://localhost:" + HTTP_PORT + "/" + name + "/hello").toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public void close() throws IOException {
        try {
            System.out.println("Undeploying");
            glassfish.getDeployer().undeploy(name);
            System.out.println("Stopping the server !");
            glassfish.dispose();
        } catch (final GlassFishException e) {
            throw new IllegalStateException(e);
        }
    }


    public static Application start() throws IOException, GlassFishException {
        GlassFishProperties props = new GlassFishProperties();
        props.setPort("http-listener", HTTP_PORT);
        GlassFish glassfish = GlassFishRuntime.bootstrap().newGlassFish(props);
        glassfish.start();

        final File classes = Path.of(System.getProperty("basedir")).resolve("target").resolve("classes").toFile();
        final ScatteredArchive war = new ScatteredArchive("hello", ScatteredArchive.Type.WAR);
        war.addClassPath(classes);
        System.out.println("War content: \n" + war);

        final String name = glassfish.getDeployer().deploy(war.toURI());
        return new Application(glassfish, name);
    }
}
