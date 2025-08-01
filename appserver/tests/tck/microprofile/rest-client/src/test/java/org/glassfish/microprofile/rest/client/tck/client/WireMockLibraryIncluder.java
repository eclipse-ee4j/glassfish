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

package org.glassfish.microprofile.rest.client.tck.client;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

import static java.lang.System.Logger.Level.INFO;

public class WireMockLibraryIncluder implements ApplicationArchiveProcessor {

    private static final System.Logger LOG = System.getLogger(WireMockLibraryIncluder.class.getName());

    private static final String[] DIRECT_DEPENDENCIES = {
        "com.github.tomakehurst:wiremock",
        "org.hamcrest:hamcrest",
        "org.slf4j:slf4j-jdk14",
        "org.slf4j:jcl-over-slf4j",
        "org.apache.httpcomponents:httpclient",
        "org.eclipse.jetty:jetty-server",
        "org.eclipse.jetty:jetty-servlet",
        "org.eclipse.jetty:jetty-servlets",
    };
    private static final File[] DEPENDENCIES = initDependenciesForWar();


    @Override
    public void process(Archive<?> archive, TestClass testClass) {

        // Only process web archives
        if (!(archive instanceof WebArchive)) {
            return;
        }
        final var webArchive = (WebArchive) archive;
        webArchive.addAsLibraries(DEPENDENCIES);
        LOG.log(INFO, () -> "webArchive:\n" + webArchive.toString(true));
    }

    private static File[] initDependenciesForWar() {
        PomEquippedResolveStage stageOne = Maven.resolver().loadPomFromFile("pom.xml");
        Set<File> dependencies = new HashSet<>();
        for (String coordinate : DIRECT_DEPENDENCIES) {
            dependencies.addAll(List.of(stageOne.resolve(coordinate).withTransitivity().asFile()));
        }
        return dependencies.toArray(File[]::new);
    }
}
