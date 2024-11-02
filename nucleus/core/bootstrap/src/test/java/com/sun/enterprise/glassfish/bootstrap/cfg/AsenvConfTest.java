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

package com.sun.enterprise.glassfish.bootstrap.cfg;

import java.io.File;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsenvConfTest {

    /**
     * This test is used to test the regex pattern of "parseAsEnv" method of "MainHelper.java".
     * <br>
     * It creates two temporary files (asenv.conf and asenv.bat) for testing purpose.
     * The "parseAsEnv()" method of "MainHelper.java" reads the "asenv.*" file line by line to
     * generate the Properties "asenvProps" whose assertion has been done in this unit test.
     */
    @Test
    void parseAsEnvTest() throws Exception {
        File resources = File.createTempFile("helperTestResources", "config");
        resources.delete(); // delete the temp file
        resources.mkdir(); // reuse the name for a directory
        resources.deleteOnExit();
        File config = new File(resources, "config");
        config.mkdir();
        config.deleteOnExit();
        File asenv_bat = new File(config, "asenv.bat"); // test resource for windows
        File asenv_conf = new File(config, "asenv.conf");// test resource for linux
        asenv_bat.deleteOnExit();
        asenv_conf.deleteOnExit();

        PrintWriter pw1 = new PrintWriter(asenv_bat, UTF_8);
        pw1.println("set AbcVar=value1");
        pw1.println("SET Avar=\"value2\"");
        pw1.println("Set Bvar=\"value3\"");
        pw1.println("set setVar=\"value4\"");
        pw1.println("set SetVar=value5");
        pw1.println("set seVar=\"value6\"");
        pw1.println("set sVar=\"value7\"");
        pw1.close();
        PrintWriter pw2 = new PrintWriter(asenv_conf, UTF_8);
        pw2.println("AbcVar=value1");
        pw2.println("Avar=\"value2\"");
        pw2.println("Bvar=\"value3\"");
        pw2.println("setVar=\"value4\"");
        pw2.println("SetVar=value5");
        pw2.println("seVar=\"value6\"");
        pw2.println("sVar=\"value7\"");
        pw2.close();

        File installRoot = new File(resources.toString());
        AsenvConf asenvProps = AsenvConf.parseAsEnv(installRoot);
        assertEquals("value1", asenvProps.getProperty("AbcVar"));
        assertEquals("value2", asenvProps.getProperty("Avar"));
        assertEquals("value3", asenvProps.getProperty("Bvar"));
        assertEquals("value4", asenvProps.getProperty("setVar"));
        assertEquals("value5", asenvProps.getProperty("SetVar"));
        assertEquals("value6", asenvProps.getProperty("seVar"));
        assertEquals("value7", asenvProps.getProperty("sVar"));
    }
}
