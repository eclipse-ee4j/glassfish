/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.appserv.server.util;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.sun.enterprise.util.SystemPropertyConstants.INSTALL_ROOT_PROPERTY;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author David Matejcek
 */
public class VersionTest {

    @BeforeAll
    public static void initJvmOption() throws Exception {
        URL root = VersionTest.class.getClassLoader().getResource(".");
        File rootDir = Paths.get(root.toURI()).toFile();
        System.setProperty(INSTALL_ROOT_PROPERTY, rootDir.getAbsolutePath());
    }


    @AfterAll
    public static void clearJvmOption() throws Exception {
        System.clearProperty(INSTALL_ROOT_PROPERTY);
    }


    @Test
    public void parse() {
        assertAll(
            () -> assertEquals("Wonderful GlassFish 2023.2.27", Version.getProductId()),
            () -> assertEquals(
                "Wonderful GlassFish 2023.2.27-SNAPSHOT ("
                    + "commit: 93176e2555176091c8522e43d1d32a0a30652d4a)",
                Version.getProductIdInfo()),
            () -> assertEquals("Wonderful GlassFish", Version.getProductName()),
            () -> assertEquals("WGF", Version.getProductNameAbbreviation()),
            () -> assertEquals("2023.2.27-SNAPSHOT", Version.getVersion()),
            () -> assertEquals("2023.2.27", Version.getVersionNumber()),
            () -> assertEquals(2023, Version.getMajorVersion()),
            () -> assertEquals(2, Version.getMinorVersion()),
            () -> assertEquals(27, Version.getPatchVersion()),
            () -> assertEquals("myOwnAsAdmin", Version.getAdminClientCommandName()),
            () -> assertEquals("experimental.domain.jar", Version.getDomainTemplateDefaultJarFileName())
        );
    }
}
