/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Dev tests for install/uninstall-node
 * The test can run against localhost or remote host.
 * - By default, the test will setup public key auth
 *   (see install-node target in build.xml)
 * - To verify with password auth, run the test with -Dssh.configure=false.
 *   Also, make sure that public key is not present on remote host.
 * @author Yamini K B
 */
public class InstallNodeTest extends SshBaseDevTest {

    private static final String INSTALL_DIR = "--installdir";
    private static final String LOCALHOST = "localhost";
    private static final String INSTALL = "--install";
    private static final String UNINSTALL = "--uninstall";
    private static final String SSH_ALIAS_PASS = "ssh-pass";

    private final String host;
    private final File glassFishHome;
    private String remoteHost = null;
    private String sshPass = null;
    private String sshUser = null;
    private Boolean sshConfigure = false;

    public InstallNodeTest() {
        String host0 = null;

        try {
            host0 = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception e) {
            host0 = "localhost";
        }
        host = host0;
        glassFishHome = getGlassFishHome();

        sshUser = TestUtils.getExpandedSystemProperty(SSH_USER_PROP);
        remoteHost = TestUtils.getExpandedSystemProperty(SSH_HOST_PROP);
        sshPass = TestUtils.getExpandedSystemProperty(SSH_PASSWORD_PROP);
        sshConfigure = Boolean.valueOf(TestUtils.getExpandedSystemProperty(SSH_CONFIGURE_PROP));
    }

    public static void main(String[] args) {
        new InstallNodeTest().run();
    }

    @Override
    public String getTestName() {
        return "install/uninstall-node test";
    }

    @Override
    protected String getTestDescription() {
        return "Developer tests for GlassFish provisioning";
    }

    @Override
    public void subrun() {

        boolean runTest = true;

        if (!ok(remoteHost)) {
            remoteHost=host;
        }

        if (!ok(sshUser)) {
            sshUser = System.getProperty("user.name");
        }

        if (!ok(sshPass)) {
            System.out.printf("%s requires you set the %s property\n",
                this.getClass().getName(), SSH_PASSWORD_PROP);
            runTest = false;
        }

        if (!runTest) {
            report("install-node-*", false);
            return;
        }

        System.out.printf("%s=%s\n", "Host", host);
        System.out.printf("%s=%s\n", "GlassFish Home", glassFishHome);
        System.out.printf("%s=%s\n", SSH_HOST_PROP, remoteHost);
        System.out.printf("%s=%s\n", SSH_USER_PROP,
                (ok(sshUser) ? sshUser : "<default>" ));
        System.out.printf("%s=%s\n", SSH_PASSWORD_PROP,
                (ok(sshPass) ? "<concealed>" : "<none>" ));
        System.out.printf("%s=%s\n", SSH_CONFIGURE_PROP, sshConfigure);
        System.out.println("Password file = " +  Constants.pFile);

        if (!sshConfigure) {
            //will use password auth for the tests
            addPassword(sshPass, PasswordType.SSH_PASS);
        }

        disableInteractiveMode();

        asadmin("start-domain");

        if (isLocal()) {
            System.out.println("------------------------------------------------");
            System.out.println("INFO: Running install-node tests locally.");
            System.out.println("------------------------------------------------");
            testInstallLocalNode();
            testUnInstallLocalNode();
        } else {
            System.out.println("------------------------------------------------");
            System.out.println("INFO: Running install-node tests on remote host.");
            System.out.println("------------------------------------------------");
            testInstallRemoteNode();
            testUnInstallRemoteNode();
        }

        testPasswordAlias();

        asadmin("stop-domain");
        removePasswords("SSH");
        stat.printSummary();
    }

    private void testInstallLocalNode() {

        //create a ssh node with --install
        report("create-node-ssh-with-install", asadmin("create-node-ssh", "--nodehost", LOCALHOST, INSTALL, INSTALL_DIR, "/tmp/a", "n1"));

        //cannot install if node already has an installation
        report("install-again", !asadmin("install-node", INSTALL_DIR, "/tmp/a", LOCALHOST));

        //installing at different location on same host should work
        report("install-at-different-location", asadmin("install-node", INSTALL_DIR, "/tmp/b", "127.0.0.1"));

        //try using host name alias
        report("install-same-host", !asadmin("install-node", INSTALL_DIR, "/tmp/b", LOCALHOST));

        //create a sample instance
        asadmin("create-local-instance", "--nodedir", "/tmp/b/glassfish/nodes", "i1");
        asadmin("create-local-instance", "--nodedir", "/tmp/b/glassfish/servers", "i2");
    }

    private void testUnInstallLocalNode() {

        //delete the sample instances, nodedir will remain
        //this is to test JIRA-16889
        asadmin("delete-local-instance", "--nodedir", "/tmp/b/glassfish/nodes", "i1");
        asadmin("delete-local-instance", "--nodedir", "/tmp/b/glassfish/servers", "i2");

        //simple uninstall
        report("uninstall-node-1", asadmin("uninstall-node", INSTALL_DIR, "/tmp/b", LOCALHOST));

        //delete node with --uninstall
        report("delete-node-with-uninstall", asadmin("delete-node-ssh", UNINSTALL, "n1"));
    }

    private void testInstallRemoteNode() {
        //create a ssh node with --install
        report("create-node-ssh-with-install-remote", asadmin("create-node-ssh", "--nodehost", remoteHost, "--sshuser", sshUser, INSTALL, INSTALL_DIR, "gf-test-1", "n2"));

        //cannot install if node already has an installation
        report("install-again-remote", !asadmin("install-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-1", remoteHost));

        //installing at different location on same host should work
        report("install-at-different-location-remote", asadmin("install-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-2", remoteHost));

        //authentication will fail if remote user is different from the user running this test
        String user = System.getProperty("user.name");
        if (user.equals(sshUser)) {
            //installing at same location but localhost should work
            report("install-at-same-location-different-host", asadmin("install-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-2", "localhost"));
        }
    }

    private void testUnInstallRemoteNode() {
        //should fail since there is an installation
        report("uninstall-node-remote", !asadmin("uninstall-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-1", remoteHost));

        //simple uninstall
        report("uninstall-node-1-remote", asadmin("uninstall-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-2", remoteHost));

        //delete node with --uninstall
        report("delete-node-with-uninstall-remote", asadmin("delete-node-ssh", UNINSTALL, "n2"));

        //simple uninstall
        String user = System.getProperty("user.name");
        if (user.equals(sshUser)) {
            report("uninstall-node-local", asadmin("uninstall-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-2", "localhost"));
        }
    }

    private void testPasswordAlias() {
        //remove all previous entries
        removePasswords("SSH");

        addPassword(sshPass, PasswordType.ALIAS_PASS);
        asadmin("create-password-alias", SSH_ALIAS_PASS);
        removePasswords("ALIAS");

        addPassword("${ALIAS=" + SSH_ALIAS_PASS + "}", PasswordType.SSH_PASS);

        if (isLocal()) {
            report("install-using-password-alias", asadmin("install-node", INSTALL_DIR, "/tmp/aa", LOCALHOST));
            report("uninstall-using-password-alias", asadmin("uninstall-node", INSTALL_DIR, "/tmp/aa", LOCALHOST));
        } else {
            report("install-using-password-alias-remote", asadmin("install-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-11", remoteHost));
            report("uninstall-using-password-alias-remote", asadmin("uninstall-node", "--sshuser", sshUser, INSTALL_DIR, "gf-test-11", remoteHost));
        }

        asadmin("delete-password-alias", "ssh-pass");
    }

    private boolean isLocal() {
        return (remoteHost.equals(LOCALHOST) || remoteHost.equals(host)) ? true : false;
    }
}
