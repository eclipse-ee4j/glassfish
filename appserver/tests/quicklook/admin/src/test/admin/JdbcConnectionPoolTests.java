/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.admin;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Manifest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import test.admin.util.GeneralUtils;

/** Supposed to have JDBC connection pool and resource tests.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class JdbcConnectionPoolTests extends BaseAsadminTest {

    private File path;
    private static final String JAVADB_POOL = "javadb_pool"; //same as in resources.xml
    private static final String ADD_RES     = "add-resources";
    
    @Parameters({"resources.xml.relative.path"})
    @BeforeClass
    public void setupEnvironment(String relative) {
        String cwd = System.getProperty("BASEDIR");
        path = new File(cwd, relative);
    }
    @Test(groups={"pulse"}) // test method
    public void createPool() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = path.getAbsolutePath();
        String up = GeneralUtils.toFinalURL(adminUrl, ADD_RES, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);
    }
    @Test(groups={"pulse"}, dependsOnMethods={"createPool"})
    public void pingPool() {
        String CMD = "ping-connection-pool";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = JAVADB_POOL;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);
        //ping succeeded!
    }

    @Test(groups={"pulse"}, dependsOnMethods={"pingPool"})
    public void ensureCreatedPoolExists() {
        Manifest man = runListPoolsCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        String children = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN);
        if (!children.contains(JAVADB_POOL)) {
            throw new RuntimeException("deleted http listener: " + JAVADB_POOL + " exists in the list: " + children);
        }        
    }
    
    @Test(groups={"pulse"}, dependsOnMethods={"ensureCreatedPoolExists"})
    public void deletePool() {
        String CMD = "delete-jdbc-connection-pool";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = JAVADB_POOL;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);        
    }

    @Test(groups={"pulse"}, dependsOnMethods={"deletePool"})
    public void deletedPoolDoesNotExist() {
        Manifest man = runListPoolsCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        String children = GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN);
        if (children.contains(JAVADB_POOL)) {
            throw new RuntimeException("deleted http listener: " + JAVADB_POOL + " exists in the list: " + children);
        }         
    }

    private Manifest runListPoolsCommand() {
        String CMD = "list-jdbc-connection-pools";
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = null;
        String up = GeneralUtils.toFinalURL(adminUrl, CMD, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        return ( man );
    }    
}
