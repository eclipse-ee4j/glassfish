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

import com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.OsgiPlatform;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

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
        privilegedStart();
/*
        Properties props = new Properties();
        props.setProperty(Constants.PLATFORM_PROPERTY_KEY,
                System.getProperty(Constants.PLATFORM_PROPERTY_KEY, Constants.Platform.Felix.toString()));

        long startTime = System.currentTimeMillis();

        GlassFishRuntime gfr = GlassFishRuntime.bootstrap(
                props, getClass().getClassLoader());  // don't use thread context classloader, otherwise the META-INF/services will not be found.
        long timeTaken = System.currentTimeMillis() - startTime;

        logger.info("created gfr = " + gfr + ", timeTaken = " + timeTaken);

        startTime = System.currentTimeMillis();
        GlassFish gf = gfr.newGlassFish(props);
        timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("created gf = " + gf + ", timeTaken = " + timeTaken);


        startTime = System.currentTimeMillis();
        gf.start();
        timeTaken = System.currentTimeMillis() - startTime;
        System.out.println("started gf, timeTaken = " + timeTaken);
*/

    }

    private void privilegedStart() throws Exception {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                try {
                    Properties props = new Properties();
                    props.setProperty(BootstrapKeys.PLATFORM_PROPERTY_KEY,
                            System.getProperty(BootstrapKeys.PLATFORM_PROPERTY_KEY, OsgiPlatform.Felix.name()));

                    long startTime = System.currentTimeMillis();

                    GlassFishRuntime gfr = GlassFishRuntime.bootstrap(
                            new BootstrapProperties(props), getClass().getClassLoader());  // don't use thread context classloader, otherwise the META-INF/services will not be found.
                    long timeTaken = System.currentTimeMillis() - startTime;

                    logger.info("created gfr = " + gfr + ", timeTaken = " + timeTaken);

                    startTime = System.currentTimeMillis();
                    GlassFish gf = gfr.newGlassFish(new GlassFishProperties(props));
                    timeTaken = System.currentTimeMillis() - startTime;
                    System.out.println("created gf = " + gf + ", timeTaken = " + timeTaken);


                    startTime = System.currentTimeMillis();
                    gf.start();
                    timeTaken = System.currentTimeMillis() - startTime;
                    System.out.println("started gf, timeTaken = " + timeTaken);

                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                return null;
            }
        });
    }
}
