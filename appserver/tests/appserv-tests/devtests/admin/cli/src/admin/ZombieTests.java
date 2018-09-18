/*
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

package admin;

import java.io.*;
import java.net.*;

public class ZombieTests extends AdminBaseDevTest {

    public ZombieTests() {
        glassFishHome = getGlassFishHome();
        zombieWarFile = new File("apps/Zombie/target/Zombie.war").getAbsoluteFile();
        System.out.println("GF HOME = " + glassFishHome);
        System.out.println("CWD: " + System.getProperty("user.dir"));
        System.out.println("Zombie War File = " + zombieWarFile);
    }

    public static void main(String[] args) {
        new ZombieTests().run();
    }

    @Override
    public String getTestName() {
        return "Testing Forced Server Shutdown";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for forced shutdown of Undead servers";
    }

    @Override
    public void subrun() {
        validate();
        startDomain();
        undeploy(true);
        deploy();
        try {
            makeDasUndead();
            report("RunZombieApp", true);
        }
        catch (Exception e) {
            report("RunZombieApp", false);
        }
        undeploy(false);
        report("failed-stop-domain", !asadmin("stop-domain"));
        // TODO -- add common-utils so I can use ProcessManager etc.
        stopDomainForce();
        stat.printSummary();
    }

    private void validate() {
        report("Zombie War File exists", zombieWarFile.isFile());
    }

    private void undeploy(boolean ignoreError) {
        boolean success = asadmin("undeploy", "Zombie"); // it probably isn't there

        if (ignoreError)
            success = true;

        report("Undeploy Zombie", success);
    }

    private void deploy() {
        report("deploy-zombie", asadmin("deploy", zombieWarFile.getAbsolutePath()));
    }

    private void makeDasUndead() throws MalformedURLException, IOException {
        URL zombieUrl = new URL("http://localhost:8080/Zombie");
        URLConnection conn = zombieUrl.openConnection();
        conn.getInputStream().close();
    }

    private void stopDomainForce() {
        report("forced-stop", asadmin("stop-domain", "--kill"));
    }
    private final File glassFishHome;
    private final File zombieWarFile;
}
