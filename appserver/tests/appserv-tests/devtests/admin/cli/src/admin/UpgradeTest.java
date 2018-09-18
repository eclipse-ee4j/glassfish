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
import javax.xml.xpath.XPathConstants;

/*
 * Dev test for config upgrade from v2.1 to 3.1.
 *
 * Also copies a 3.0.1 config file into domain and upgrades it. This should
 * add the default-config element so that a cluster can be created.
 * See for more details:
 * http://java.net/jira/browse/GLASSFISH-15774
 *
 */
public class UpgradeTest extends AdminBaseDevTest {

    // the original domain.xml file will me moved to this
    private File movedDomXml;

    @Override
    protected String getTestDescription() {
        return "Unit tests for upgrade";
    }

    public static void main(String[] args) {
        new UpgradeTest().runEm();
    }

    private void runEm() {
        // before copying in our domain config, move the old one
        renameOriginalDomainConfig();

        testV3_0_1Domain();

        // after all the tests have run, move back the old domain
        restoreOriginalDomainConfig();

        // print results
        stat.printSummary();
    }

    private void testV3_0_1Domain() {
        copyDomainConfig("v3_0_1domain.xml");

        // make sure default-config isn't really there first
        String xPath = "/domain/configs/config[@name='server-config']";
        Object node = evalXPath(xPath, XPathConstants.NODE);
        report("found-server-config", node != null);
        xPath = "/domain/configs/config[@name='default-config']";
        node = evalXPath(xPath, XPathConstants.NODE);
        report("missing-default-config-expected", node == null);

        // run the upgrade
        report("run-upgrade-3_0_1", asadmin("start-domain", "--upgrade"));

        // default-config should be there
        node = evalXPath(xPath, XPathConstants.NODE);
        report("found-default-config", node != null);

        // start server
        report("start-post-upgrade-3_0_1", asadmin("start-domain"));

        // create/test/stop cluster
        report("create-cluster", asadmin("create-cluster", "clus1"));
        report("create-instance1", asadmin("create-local-instance", "in1"));
        report("create-instance2", asadmin("create-local-instance", "in2"));
        report("start-cluster", asadmin("start-cluster", "clus1"));
        report("stop-cluster", asadmin("stop-cluster", "clus1"));

        if (TestEnv.isV4Layout()) {
            report("cleanup-instance-dir-in1", deleteDirectory(TestEnv.getInstanceDir(null, "in1")));
            report("cleanup-instance-dir-in2", deleteDirectory(TestEnv.getInstanceDir(null, "in2")));
        }
        else {
            report("cleanup-nodes-dir", deleteDirectory(TestEnv.getNodesHome()));
        }
        // stop server
        report("stop-post-upgrade-3_0_1", asadmin("stop-domain"));
    }

    // run before any upgrade test
    private void renameOriginalDomainConfig() {
        File domXml = getDASDomainXML();
        movedDomXml = new File(domXml.getParentFile(), "domain.xml.moved");
        report("moved-domain", domXml.renameTo(movedDomXml));
    }

    // cleanup after upgrade test
    private void restoreOriginalDomainConfig() {
        getDASDomainXML().delete();
        report("restored-domain", movedDomXml.renameTo(getDASDomainXML()));
    }

    // run from each test method to copy a domain into the server
    private void copyDomainConfig(String fileName) {
        File source = new File(
            "resources" + File.separator +
            "configs" + File.separator +
             fileName);
        File target = getDASDomainXML();

        // we really want to catch this right away
        if (! source.exists()) {
            throw new RuntimeException(String.format(
                "File %s does not exist to copy to config dir",
                source.getAbsolutePath()));
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(source);
            to = new FileOutputStream(target);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } catch (IOException ex) {
            report("file-copy", false);
            ex.printStackTrace();
            return;
        } finally {
            try {
                from.close();
                to.close();
            } catch (Exception e) {
                /* Could be IOException or NPE if there was an error
                 * above. Either way, let someone know about it....
                 */
                System.err.println("Some problem closing file streams:");
                e.printStackTrace();
            }
        }
    }
}
