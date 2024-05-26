/*
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.persistence.transform;

import jakarta.persistence.spi.PersistenceProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.test.app.persistence.transform.provider.ClassTransformerImpl;
import org.glassfish.main.test.app.persistence.transform.provider.Enhancer;
import org.glassfish.main.test.app.persistence.transform.provider.EnhancerContext;
import org.glassfish.main.test.app.persistence.transform.provider.EntityManagerFactoryImpl;
import org.glassfish.main.test.app.persistence.transform.provider.EntityManagerImpl;
import org.glassfish.main.test.app.persistence.transform.provider.PersistenceProviderImpl;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;


public class ClassTransformerTestBase {

    private static final String PROVIDER_FILE_NAME = "provider.jar";

    protected static final Asadmin ASADMIN = getAsadmin();

    protected static JavaArchive createProvider() {
        return ShrinkWrap.create(JavaArchive.class, PROVIDER_FILE_NAME)
            .addClass(ClassTransformerImpl.class)
            .addClass(Enhancer.class)
            .addClass(EnhancerContext.class)
            .addClass(EntityManagerImpl.class)
            .addClass(EntityManagerFactoryImpl.class)
            .addClass(PersistenceProviderImpl.class)
            .addAsServiceProvider(PersistenceProvider.class, PersistenceProviderImpl.class);
    }

    protected static File createDeploymentFile(Archive<?> archive, String appName) throws IOException {
        File tempDir = Files.createTempDirectory(appName).toFile();
        tempDir.deleteOnExit();
        File deploymentFile = new File(tempDir, appName + extensionFor(archive));
        archive.as(ZipExporter.class).exportTo(deploymentFile, true);
        return deploymentFile;
    }

    private static String extensionFor(Archive<?> archive) {
        String extension = ".jar";
        if (archive instanceof WebArchive) {
            extension = ".war";
        } else if (archive instanceof EnterpriseArchive) {
            extension = ".ear";
        }
        return extension;
    }
}
