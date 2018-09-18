/*
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

import com.sun.enterprise.universal.xml.MiniXmlParserException;
import java.io.*;
import java.util.*;
import org.glassfish.api.admin.RuntimeType;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author bnevins
 */
public class GFLauncherTest {

    public GFLauncherTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        ClassLoader cl = GFLauncherTest.class.getClassLoader();

        File asenv = new File(cl.getResource("config/asenv.bat").toURI());
        installDir = asenv.getParentFile().getParentFile();
        domainsDir = new File(installDir, "domains");
        assertTrue("domain1 -- domain.xml is missing!!",
                new File(domainsDir, "domain1/config/domain.xml").exists());
        assertTrue("domain2 -- domain.xml is missing!!",
                new File(domainsDir, "domain2/config/domain.xml").exists());
        assertTrue("domain3 -- domain.xml is missing!!",
                new File(domainsDir, "domain3/config/domain.xml").exists());
        assertTrue("baddomain -- domain.xml is missing!!",
                new File(domainsDir, "baddomain/config/domain.xml").exists());
        assertTrue("domainNoLog -- domain.xml is missing!!",
                new File(domainsDir, "domainNoLog/config/domain.xml").exists());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws GFLauncherException {
        launcher = GFLauncherFactory.getInstance(RuntimeType.DAS);
        info = launcher.getInfo();
        info.setInstallDir(installDir);
        launcher.setMode(GFLauncher.LaunchType.fake);
    }

    @After
    public void tearDown() {
    }


    /**
     * First Test -- Fake Launch the default domain in the default domain dir
     * Since we have more than 1 domain in there -- it should fail!
     */
    @Test(expected=GFLauncherException.class)
    public void test1() throws GFLauncherException, MiniXmlParserException {
        launcher.launch();
    }
    /**
     * Let's fake-launch domain1  -- which DOES have the jvm logging args
     */

    @Test
    public void test2() throws GFLauncherException, MiniXmlParserException {
        info.setDomainName("domain1");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();

        assertTrue(cmdline.contains("-XX:+UnlockDiagnosticVMOptions"));
        // 0 --> java, 1 --> "-cp" 2 --> the classpath, 3 -->first arg
        assertEquals(cmdline.get(3), "-XX:+UnlockDiagnosticVMOptions");
        
        /* Too noisy, todo figure out how to get it into the test report
        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
         */
    }

    /**
     * Let's fake-launch domain2 -- which does NOT have the jvm logging args
     */

    @Test
    public void test3() throws GFLauncherException, MiniXmlParserException {
        info.setDomainName("domain2");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();
        assertFalse(cmdline.contains("-XX:+UnlockDiagnosticVMOptions"));

        /*
        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
         */
    }

    /**
     * Let's fake-launch a domain that doesn't exist
     * it has an XML error in it.
     */
    @Test(expected=GFLauncherException.class)
    public void test4() throws GFLauncherException, MiniXmlParserException {
        info.setDomainName("NoSuchDomain");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();

        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
    }
    /**
     * Let's fake-launch baddomain
     * it has an XML error in it.
     */
    @Test(expected=GFLauncherException.class)
    public void test5() throws GFLauncherException, MiniXmlParserException {
        info.setDomainName("baddomain");
        launcher.launch();
        List<String> cmdline = launcher.getCommandLine();

        System.out.println("COMMANDLINE:");
        for(String s : cmdline) {
            System.out.println(s);
        }
    }

    /**
     * Test the logfilename handling -- log-service is in domain.xml like V2
     */
    @Test
    public void test6() throws GFLauncherException {
        info.setDomainName("domain1");
        launcher.launch();
        assertTrue(launcher.getLogFilename().endsWith("server.log"));
    }

    /**
     * Test the logfilename handling -- no log-service is in domain.xml
     */

    @Test
    public void test7() throws GFLauncherException {
        info.setDomainName("domainNoLog");
        launcher.launch();
        assertTrue(launcher.getLogFilename().endsWith("server.log"));
    }

    @Test
    public void testDropInterruptedCommands() throws GFLauncherException {
        info.setDomainName("domainNoLog");
        info.setDropInterruptedCommands(true);
        launcher.launch();
        assertTrue(launcher.getJvmOptions().contains("-Dorg.glassfish.job-manager.drop-interrupted-commands=true"));
    }

    //private static File domain1, domain2, domain3, domain4, domain5;
    private static File installDir;
    private static File domainsDir;
    private GFLauncher launcher;
    private GFLauncherInfo info;
}
