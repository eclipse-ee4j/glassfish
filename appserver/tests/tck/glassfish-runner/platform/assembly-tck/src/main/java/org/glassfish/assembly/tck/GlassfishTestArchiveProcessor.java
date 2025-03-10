/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.assembly.tck;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ManifestContainer;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import tck.arquillian.porting.lib.spi.AbstractTestArchiveProcessor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public class GlassfishTestArchiveProcessor extends AbstractTestArchiveProcessor {

    static Logger log = Logger.getLogger(GlassfishTestArchiveProcessor.class.getName());
    static HashSet<String> sunXmlFiles = new HashSet<String>();
    static {
        sunXmlFiles.add("META-INF/sun-application-client.xml");
        sunXmlFiles.add("META-INF/sun-application.xml");
        sunXmlFiles.add("META-INF/sun-ra.xml");
        sunXmlFiles.add("WEB-INF/sun-web.xml");
        sunXmlFiles.add("META-INF/sun-ejb-jar.xml");
    }

    private Path descriptorDirRoot;

     /**
     * Called on completion of the Arquillian configuration.
     */
    public void initalize(@Observes ArquillianDescriptor descriptor) {
        // Must call to setup the ResourceProvider
        super.initalize(descriptor);
        

        // Get the descriptor path
        ExtensionDef descriptorsDef = descriptor.extension("glassfish-descriptors");
        String descriptorDir = descriptorsDef.getExtensionProperties().get("descriptorDir");
        if(descriptorDir == null) {
            String msg = "Specify the descriptorDir property in arquillian.xml as extension:\n"+
                    "<extension qualifier=\"glassfish-descriptors\">\n" +
                    "        <property name=\"descriptorDir\">path-to-descriptors-dir</property>\n" +
                    "</extension>";
            throw new IllegalStateException(msg);
        }
        this.descriptorDirRoot = Paths.get(descriptorDir);
        if(!Files.exists(this.descriptorDirRoot)) {
            throw new RuntimeException("Descriptor directory does not exist: " + this.descriptorDirRoot);
        }
    }

    @Override
    public void processClientArchive(JavaArchive clientArchive, Class<?> testClass, URL sunXmlURL) {
        String name = clientArchive.getName();
        // addDescriptors(name, clientArchive, testClass);
    }

    @Override
    public void processWebArchive(WebArchive webArchive, Class<?> testClass, URL sunXmlURL) {
        String name = webArchive.getName();
        // addDescriptors(name, webArchive, testClass);
    }

    @Override
    public void processRarArchive(JavaArchive warArchive, Class<?> testClass, URL sunXmlURL) {

    }

    @Override
    public void processParArchive(JavaArchive javaArchive, Class<?> aClass, URL url) {

    }

    @Override
    public void processEarArchive(EnterpriseArchive earArchive, Class<?> testClass, URL sunXmlURL) {
        String name = earArchive.getName();
        // addDescriptors(name, earArchive, testClass);
    }

    @Override
    public void processEjbArchive(JavaArchive ejbArchive, Class<?> testClass, URL sunXmlURL) {
        String name = ejbArchive.getName();
        // addDescriptors(name, ejbArchive, testClass);
    }

    protected void addDescriptors(String archiveName, ManifestContainer<?> archive, Class<?> testClass) {
        String pkgName = testClass.getPackageName();
        Path pkgPath = Paths.get(pkgName.replace(".", "/"));
        Path descriptorDir = descriptorDirRoot.resolve(pkgPath);
        List<File> files = findGlassfishDescriptors(descriptorDir);
        for (File f : files) {
            String name = f.getName();
            if(!name.startsWith(archiveName)) {
                continue;
            }
            try {
                URL url = f.toURL();
                // stateful_migration_threetwo_annotated.ear.jboss-deployment-structure.xml -> jboss-deployment-structure.xml
                String descriptorName = name.replace(archiveName+".", "");
                if(archive instanceof WebArchive webArchive) {
                    webArchive.addAsWebInfResource(url, descriptorName);
                } else {
                    archive.addAsManifestResource(url, descriptorName);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    protected List<File> findGlassfishDescriptors(Path pkgPath) {
        try {
            List<File> files = Files.walk(pkgPath, 1)
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .toList();
            return files;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }
}
