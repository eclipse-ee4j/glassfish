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
package org.glassfish.tck.data.arquillian;


import java.io.File;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;


/**
 * Adds all transitive dependencies of the JNoSQL Mongo driver to the test app.
 *
 * Maven build prepares all transitive dependencies and sets the value of the
 * {@code jnosql.mongo.artifacts} system property to point to the directory with them.
 *
 * @author Ondro Mihalyi
 */
public class JNoSQLMongoProcessor implements ApplicationArchiveProcessor {

    private static final String JNOSQL_MONGO_ARTIFACTS_PATH = "jnosql.mongo.artifacts";

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        File artifactsDirectory;
        if ((artifactsDirectory = getArtifactsDirectory()) != null && archive instanceof WebArchive webArchive) {
            webArchive.addAsLibraries(artifactsDirectory.listFiles());
        }
    }

    private File getArtifactsDirectory() {
        final String artifactsDirectory = System.getProperty(JNOSQL_MONGO_ARTIFACTS_PATH);
        if (artifactsDirectory != null) {
            final File result = new File(artifactsDirectory);
            if (result.isDirectory()) {
                return result;
            }
        }
        return null;

    }


}
