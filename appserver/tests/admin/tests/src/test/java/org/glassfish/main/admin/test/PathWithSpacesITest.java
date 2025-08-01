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
import java.nio.file.Path;

import org.apache.commons.lang3.SystemUtils;
import org.glassfish.main.itest.tools.GlassFishTestEnvironment;
import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathWithSpacesITest {

    private static Asadmin asadmin;

    @BeforeAll
    static void copyGlassFish() throws Exception {
        File originalGF =  GlassFishTestEnvironment.getGlassFishDirectory().getParentFile();
        File gfRootWithSpaces = new File(originalGF.getParentFile(), "GlassFish With Spaces");
        FileUtils.copy(originalGF, gfRootWithSpaces);
        assertTrue(FileUtils.deleteFile(new File(gfRootWithSpaces, "domains")), "Deletion of domains directory failed");
        String asadminFileName = SystemUtils.IS_OS_WINDOWS ? "asadmin.bat" : "asadmin";
        Path asadminPath = gfRootWithSpaces.toPath().resolve(Path.of("bin", asadminFileName));
        asadmin = new Asadmin(asadminPath.toFile(), "admin", null);
        assertThat(asadmin.exec("create-domain", "--nopassword", "--portbase", "14800", "spaces"), asadminOK());
    }


    @AfterAll
    static void stopGlassFish() throws Exception {
        assertThat(asadmin.exec("stop-domain", "--kill", "spaces"), asadminOK());
    }


    @Test
    void start() throws Exception {
        assertThat(asadmin.exec(60_000, "start-domain", "spaces"), asadminOK());
    }
}
