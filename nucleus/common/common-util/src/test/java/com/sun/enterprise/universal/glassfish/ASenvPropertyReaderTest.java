/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.glassfish;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author bnevins
 */
public class ASenvPropertyReaderTest {

    private static File installDir;

    @BeforeAll
    public static void setUpClass() throws Exception {
        URL resource = ASenvPropertyReaderTest.class.getClassLoader().getResource("config/asenv.bat");
        installDir = new File(resource.getPath()).getParentFile().getParentFile();
        assertNotNull(installDir);
    }

    @Test
    public void test() {
        ASenvPropertyReader reader = new ASenvPropertyReader(installDir);
        Map<String, String> props = reader.getProps();
        assertThat(props.toString(), props.entrySet(), hasSize(4));
    }
}
