/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author David Matejcek
 */
public class TckRunnerTest {

    private static TckRunner tck;

    @BeforeAll
    public static void init() {
        TckConfiguration cfg = new TckConfiguration(TckRunnerTest.class.getResourceAsStream("/tck.properties"));
        tck = new TckRunner(cfg);
        tck.prepareWorkspace();
    }

    @BeforeEach
    public void resetWorkspace() throws Exception {
        tck.deleteWorkspace();
    }

    @AfterEach
    public void stopServers() throws Exception {
        tck.stopServers();
    }


    @Test
    public void testAppClient() throws Exception {
        tck.start(Path.of("appclient"));
    }

    @Test
    public void testEjb30LiteEjbContext() throws Exception {
        tck.start(Path.of("ejb30", "lite", "ejbcontext"));
    }

    @Test
    public void testEjb30LiteEnvEntry() throws Exception {
        tck.start(Path.of("ejb30", "lite", "enventry"));
    }
}
