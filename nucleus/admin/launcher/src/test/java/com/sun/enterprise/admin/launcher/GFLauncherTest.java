/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.launcher;

import java.io.File;

import org.glassfish.api.admin.RuntimeType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author bnevins
 */
public class GFLauncherTest {

    private static File installDir;
    private static File domainsDir;
    private GFLauncher launcher;
    private GFLauncherInfo info;


    @BeforeAll
    public static void setUpClass() throws Exception {
        ClassLoader cl = GFLauncherTest.class.getClassLoader();

        File asenv = new File(cl.getResource("config/asenv.bat").toURI());
        installDir = asenv.getParentFile().getParentFile();
        domainsDir = new File(installDir, "domains");
        assertTrue(new File(domainsDir, "domain1/config/domain.xml").exists(), "domain1 -- domain.xml is missing!!");
        assertTrue(new File(domainsDir, "domain2/config/domain.xml").exists(), "domain2 -- domain.xml is missing!!");
        assertTrue(new File(domainsDir, "domain3/config/domain.xml").exists(), "domain3 -- domain.xml is missing!!");
        assertTrue(new File(domainsDir, "baddomain/config/domain.xml").exists(),
            "baddomain -- domain.xml is missing!!");
        assertTrue(new File(domainsDir, "domainNoLog/config/domain.xml").exists(),
            "domainNoLog -- domain.xml is missing!!");
    }

    @BeforeEach
    public void initLauncher() throws GFLauncherException {
        launcher = GFLauncherFactory.getInstance(RuntimeType.DAS);
        info = launcher.getInfo();
        info.setInstallDir(installDir);
        launcher.setMode(GFLauncher.LaunchType.fake);
    }


    /**
     * First Test -- Fake Launch the default domain in the default domain dir
     * Since we have more than 1 domain in there -- it should fail!
     */
    @Test
    public void defaultDomainButMultipleDomainsExist() throws Exception {
        GFLauncherException e = assertThrows(GFLauncherException.class, launcher::launch);
        assertEquals("There is more than one domain in " + domainsDir.getAbsolutePath()
            + ".  Try again but specify the domain name as the last argument.", e.getMessage());
    }


    /**
     * Let's fake-launch domain1  -- which DOES have the jvm logging args
     */
    @Test
    public void domain1WithDiagOptions() throws Exception {
        info.setDomainName("domain1");
        launcher.launch();
        CommandLine cmdline = launcher.getCommandLine();
        // 0 --> java, 1 --> "-cp" 2 --> the classpath, 3 -->first arg
        assertThat(cmdline, hasItems(matchesPattern(".*java(.exe)?(\")?"), is("-cp"),
            is("-XX:+UnlockDiagnosticVMOptions"), is("-verbose")));
    }

    /**
     * Let's fake-launch domain2 -- which does NOT have the jvm logging args
     */
    @Test
    public void domain2WithoutDiagOptions() throws Exception {
        info.setDomainName("domain2");
        launcher.launch();
        CommandLine cmdline = launcher.getCommandLine();
        assertThat(cmdline, hasItems(matchesPattern(".*java(.exe)?(\")?"), is("-cp"),
            not(is("-XX:+UnlockDiagnosticVMOptions")), is("-verbose")));
    }


    /**
     * Let's fake-launch a domain that doesn't exist
     * it has an XML error in it.
     */
    @Test
    public void missingDomain() throws Exception {
        info.setDomainName("NoSuchDomain");
        GFLauncherException e = assertThrows(GFLauncherException.class, launcher::launch);
        assertEquals("The domain root dir is not pointing to a directory.  This is what I was looking for: ["
            + new File(domainsDir, "NoSuchDomain").getAbsolutePath() + "]", e.getMessage());
    }


    /**
     * Let's fake-launch baddomain
     * it has an XML error in it.
     */
    @Test
    public void brokenDomainXml() throws Exception {
        info.setDomainName("baddomain");
        GFLauncherException e = assertThrows(GFLauncherException.class, launcher::launch);
        assertEquals("Fatal Error encountered during launch: \"Xml Parser Error: javax.xml.stream.XMLStreamException:"
            + " ParseError at [row,col]:[57,7]\n"
            + "Message: The element type \"system-property\" must be terminated by the matching"
            + " end-tag \"</system-property>\".", e.getMessage());
    }

    /**
     * Test the logfilename handling -- log-service is in domain.xml like V2
     */
    @Test
    public void domain1FromSGES2() throws GFLauncherException {
        info.setDomainName("domain1");
        launcher.launch();
        assertTrue(launcher.getLogFilename().endsWith("server.log"));
    }

    /**
     * Test the logfilename handling -- no log-service is in domain.xml
     */
    @Test
    public void noLogService() throws GFLauncherException {
        info.setDomainName("domainNoLog");
        launcher.launch();
        assertTrue(launcher.getLogFilename().endsWith("server.log"));
    }

    @Test
    public void dropInterruptedCommands() throws GFLauncherException {
        info.setDomainName("domainNoLog");
        info.setDropInterruptedCommands(true);
        launcher.launch();
        assertTrue(launcher.getJvmOptions().contains("-Dorg.glassfish.job-manager.drop-interrupted-commands=true"));
    }
}
