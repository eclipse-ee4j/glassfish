/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.admin.test;

import com.sun.enterprise.util.io.FileUtils;

import java.io.File;

import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathWithSpacesITest {

    private static File binDirectory;


    @BeforeAll
    static void copyGlassFish() throws Exception {
        File originalGF =  GlassFishTestEnvironment.getGlassFishDirectory().getParentFile();
        File gfRootWithSpaces = new File(originalGF.getParentFile(), "GlassFish With Spaces");
        FileUtils.copy(originalGF, gfRootWithSpaces);
        assertTrue(FileUtils.deleteFile(new File(gfRootWithSpaces, "domains")), "Deletion of domains directory failed");
        binDirectory = new File(gfRootWithSpaces, "bin");
        File asadminFile = new File(binDirectory, "asadmin.java");
        Asadmin asadmin = new Asadmin(asadminFile, "admin", null);
        assertThat(asadmin.exec("create-domain", "--nopassword", "--portbase", "14800", "spaces"), asadminOK());
    }


    @AfterEach
    void stopGlassFish() throws Exception {
        File asadminFile = new File(binDirectory, "asadmin.java");
        Asadmin asadmin = new Asadmin(asadminFile, "admin", null);
        assertThat(asadmin.exec("stop-domain", "--kill", "spaces"), asadminOK());
    }


    @Test
    @EnabledOnOs(OS.WINDOWS)
    void startAsadminBat() throws Exception {
        File asadminFile = new File(binDirectory, "asadmin.bat");
        Asadmin asadmin = new Asadmin(asadminFile, "admin", null);
        assertThat(asadmin.exec(60_000, "start-domain", "spaces"), asadminOK());
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void startAsadminBash() throws Exception {
        File asadminFile = new File(binDirectory, "asadmin");
        Asadmin asadmin = new Asadmin(asadminFile, "admin", null);
        assertThat(asadmin.exec(60_000, "start-domain", "spaces"), asadminOK());
    }


    @Test
    void startAsadminJava() throws Exception {
        File asadminFile = new File(binDirectory, "asadmin.java");
        Asadmin asadmin = new Asadmin(asadminFile, "admin", null);
        assertThat(asadmin.exec(60_000, "start-domain", "spaces"), asadminOK());
    }
}
