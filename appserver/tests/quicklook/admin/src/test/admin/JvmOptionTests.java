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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Manifest;
import org.testng.annotations.Test;
import test.admin.util.GeneralUtils;

/** Test related to creating/deleting/listing JVM options as supported by GlassFish.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish v3 Prelude
 */
public class JvmOptionTests extends BaseAsadminTest {
    private static final String TEST_JOE    = "-Dname= joe blo"; //sufficiently unique
    private static final String CJ          = "create-jvm-options";
    private static final String DJ          = "delete-jvm-options";
    private static final String LJ          = "list-jvm-options";

    @Test(groups={"pulse"}) // test method
    public void createJoe() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = TEST_JOE;
        String up = GeneralUtils.toFinalURL(adminUrl, CJ, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);
    }

    @Test(groups={"pulse"}, dependsOnMethods={"createJoe"})
    public void ensureCreatedJoeExists() {
        Manifest man = runListJoesCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        try {
            String children = URLDecoder.decode(GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN), "UTF-8");
            if (!children.contains(TEST_JOE)) {
                throw new RuntimeException("added JVM option: " + TEST_JOE + " does not exist in the list: " + children);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test(groups={"pulse"}, dependsOnMethods={"ensureCreatedJoeExists"})
    public void deleteJoe() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = TEST_JOE;
        String up = GeneralUtils.toFinalURL(adminUrl, DJ, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        GeneralUtils.handleManifestFailure(man);
    }

    @Test(groups={"pulse"}, dependsOnMethods={"deleteJoe"})
    public void deletedJoeDoesNotExist() {
        Manifest man = runListJoesCommand();
        GeneralUtils.handleManifestFailure(man);
        // we are past failure, now test the contents
        try {
            String children = URLDecoder.decode(GeneralUtils.getValueForTypeFromManifest(man, GeneralUtils.AsadminManifestKeyType.CHILDREN), "UTF-8");
            if (children.contains(TEST_JOE)) {
                throw new RuntimeException("deleted JVM option: " + TEST_JOE + " exists in the list: " + children);
            }
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

    }

    private Manifest runListJoesCommand() {
        Map<String, String> options = Collections.EMPTY_MAP;
        String operand = null;
        String up = GeneralUtils.toFinalURL(adminUrl, LJ, options, operand);
//        Reporter.log("url: " + up);
        Manifest man = super.invokeURLAndGetManifest(up);
        return ( man );
    }
}
