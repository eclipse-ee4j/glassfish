/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.uberjar;

import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;

import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.PLATFORM_PROPERTY_KEY;

/**
 *
 * This is a main class for 'java -jar glassfish-uber.jar'
 *
 * @author bhavanishankar@dev.java.net
 */
public class UberJarMain {

    private static Logger logger = Logger.getLogger("embedded-glassfish");

    public static void main(String... args) throws Exception {
        new UberJarMain().start();
    }

    private void start() throws Exception {
        try {
            Properties props = new Properties();
            props.setProperty(PLATFORM_PROPERTY_KEY, System.getProperty(PLATFORM_PROPERTY_KEY, OsgiPlatform.Felix.name()));

            long startTime = System.currentTimeMillis();

            // Don't use thread context classloader, otherwise the META-INF/services will not be found.
            GlassFishRuntime glassFishRuntime = GlassFishRuntime.bootstrap(new BootstrapProperties(props), getClass().getClassLoader());
            long timeTaken = System.currentTimeMillis() - startTime;

            logger.info("created gfr = " + glassFishRuntime + ", timeTaken = " + timeTaken);

            startTime = System.currentTimeMillis();
            GlassFish glassFish = glassFishRuntime.newGlassFish(new GlassFishProperties(props));
            timeTaken = System.currentTimeMillis() - startTime;
            System.out.println("created gf = " + glassFish + ", timeTaken = " + timeTaken);

            startTime = System.currentTimeMillis();
            glassFish.start();
            timeTaken = System.currentTimeMillis() - startTime;
            System.out.println("started gf, timeTaken = " + timeTaken);

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
