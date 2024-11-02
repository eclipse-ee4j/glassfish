/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.basic.config;

import com.sun.enterprise.config.serverbeans.Domain;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Ondro Mihalyi
 */

public class ConfigTest {

    public static final Logger logger = Logger.getLogger(ConfigTest.class.getName());

    static GlassFishRuntime runtime;

    @BeforeAll
    static void bootStrap() throws GlassFishException {
                runtime = GlassFishRuntime.bootstrap();
    }

    @ParameterizedTest
    @ArgumentsSource(ConfigFileArgumentsProvider.class)
    void testConfigFile(String configFile) throws GlassFishException, IOException, URISyntaxException {
        GlassFishProperties gfp = new GlassFishProperties();
        gfp.setConfigFileURI(configFile);

        GlassFish instance1 = runtime.newGlassFish(gfp);
        logger.info(() -> "Instance1 created" + instance1);
        instance1.start();
        logger.info("Instance1 started #1");

        final String domainName = instance1.getService(Domain.class).getName();
        assertTrue("myDomain".equals(domainName), "Domain name is " + domainName);

        instance1.stop();
        logger.info("Instance1 stopped #1");
        instance1.dispose();
        logger.info("Instance1 disposed");
        checkDisposed();
    }

    private static class ConfigFileArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext ec) throws Exception {
            return Stream.of(Arguments.of(fileInClassPath()),
                    Arguments.of("domain.xml"),
                    Arguments.of(new File("domain.xml").getAbsolutePath())
            );
        }

        private String fileInClassPath() throws URISyntaxException, IOException {
            return this.getClass().getClassLoader().getResources("domain.xml").nextElement().toURI().toString();
        }

    }
    // throws exception if the temp dir is not cleaned out.
    private void checkDisposed() {
        String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
        logger.info(() -> "Checking whether " + instanceRoot + " is disposed or not");
        if (new File(instanceRoot).exists()) {
            throw new RuntimeException("Directory " + instanceRoot +
                    " is not cleaned up after glassfish.dispose()");
        }
    }

}
