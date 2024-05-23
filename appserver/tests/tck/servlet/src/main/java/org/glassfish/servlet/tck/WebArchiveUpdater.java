/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation. All rights reserved.
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
package org.glassfish.servlet.tck;

import java.io.File;
import java.util.List;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.test.impl.client.deployment.AnnotationDeploymentScenarioGenerator;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * WebArchiveUpdater updates the web archive that is created by the Servlet tests.
 *
 * <p>
 * Specifically it adds SLF4J libs for a test that needs those, and adds sun-web.xml for the role
 * mapping and context root setting.
 *
 * @author Arjan Tijms
 *
 */
public class WebArchiveUpdater implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.override(
            DeploymentScenarioGenerator.class,
            AnnotationDeploymentScenarioGenerator.class, ScenarioBasedUpdater.class);
    }

    public static class ScenarioBasedUpdater extends AnnotationDeploymentScenarioGenerator {

        @Override
        public List<DeploymentDescription> generate(TestClass testClass) {
            List<DeploymentDescription> descriptions = super.generate(testClass);

            for (DeploymentDescription description : descriptions) {
                Archive<?> applicationArchive = description.getArchive();

                if (testClass.getName().contains("ClientCertAnnoTests") && applicationArchive instanceof WebArchive webArchive) {
                    webArchive
                    .addAsWebInfResource(
                        new File("src/main/resources", "sun-web.xml"),
                        "sun-web.xml")
                    .addAsLibraries(
                        Maven.configureResolver()
                             .workOffline()
                             .loadPomFromFile("pom.xml")
                             .resolve(System.getProperty("servlet.tck.slf4jimpl", "org.slf4j:slf4j-simple"))
                             .withTransitivity()
                             .as(JavaArchive.class));
                }

                if (Boolean.getBoolean("servlet.tck.archive.print")) {
                    System.out.println(applicationArchive.toString(true));
                }
            }

            return descriptions;
        }

    }



}