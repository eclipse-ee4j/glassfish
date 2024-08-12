/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.basic.lifecycle;

import java.io.File;
import java.util.logging.Logger;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.jupiter.api.Test;

/**
 * @author bhavanishankar@dev.java.net
 */

public class LifeCycleTest {

    Logger logger = Logger.getAnonymousLogger();

    GlassFishRuntime runtime;

    @Test
    public void test() throws GlassFishException {
        runtime = GlassFishRuntime.bootstrap();

        GlassFish instance1 = runtime.newGlassFish();
        logger.info("Instance1 created" + instance1);
        instance1.start();
        logger.info("Instance1 started #1");
    sleep();
        instance1.stop();
        logger.info("Instance1 stopped #1");
        instance1.start();
        logger.info("Instance1 started #2");
    sleep();
        instance1.stop();
        logger.info("Instance1 stopped #2");
        instance1.dispose();
        logger.info("Instance1 disposed");
        checkDisposed();


        GlassFishProperties props = new GlassFishProperties();
        props.setProperty("glassfish.embedded.tmpdir", System.getProperty("user.dir"));
        GlassFish instance2 = runtime.newGlassFish(props);
        logger.info("instance2 created" + instance2);
        instance2.start();
        logger.info("Instance2 started #1");
    sleep();
        instance2.stop();
        logger.info("Instance2 stopped #1");
        instance2.start();
        logger.info("Instance2 started #2");
    sleep();
        instance2.stop();
        logger.info("Instance2 stopped #2");
        instance2.dispose();
        logger.info("Instance2 disposed");
        checkDisposed();
    }

    private void sleep() {
    try {
      Thread.sleep(1000);
    } catch(Exception ex) {
    }
    }
    // throws exception if the temp dir is not cleaned out.

    private void checkDisposed() {
        String instanceRoot = System.getProperty("com.sun.aas.instanceRoot");
        logger.info("Checking whether " + instanceRoot + " is disposed or not");
        if (new File(instanceRoot).exists()) {
            throw new RuntimeException("Directory " + instanceRoot +
                    " is not cleaned up after glassfish.dispose()");
        }
    }
}
