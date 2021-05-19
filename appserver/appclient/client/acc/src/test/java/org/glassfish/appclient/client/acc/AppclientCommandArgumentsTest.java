/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client.acc;

import java.io.IOException;
import java.util.Properties;
import java.io.File;
import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.logging.LogDomains;
import java.io.FileWriter;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.MissingResourceException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tjquinn
 */
public class AppclientCommandArgumentsTest {

    private static final String USER_VALUE = "joe-the-user";
    private static final String PASSWORDFILE_NAME = "topSecret.stuff";
    private static final String EXPECTED_TARGETSERVER_VALUE = "A:1234,B:5678";
    private static final String EXPECTED_PASSWORD_IN_PASSWORD_FILE = "mySecretPassword";

    public AppclientCommandArgumentsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testA() throws Exception, UserError {
        AppclientCommandArguments info = AppclientCommandArguments.newInstance(
                Arrays.asList("-textauth", "-user", USER_VALUE));

        assertEquals("text auth not set", true, info.isTextauth());
        assertEquals("noappinvoke incorrectly set", false, info.isNoappinvoke());
        assertEquals("user incorrectly set", USER_VALUE, info.getUser());
        /*
         * Make sure an unspecified argument is repoted as null.
         */
        assertEquals("mainclass wrong", null, info.getMainclass());

    }

    @Test
    public void testB() throws Exception, UserError {
        AppclientCommandArguments info = AppclientCommandArguments.newInstance(
                Arrays.asList("-mainclass", PASSWORDFILE_NAME, "-noappinvoke"));


        assertEquals("wrong main class", PASSWORDFILE_NAME, info.getMainclass());
        assertEquals("noappinvoke not set", true, info.isNoappinvoke());
        assertEquals("user should have been null", null, info.getUser());
        assertEquals("textauth found but should be absent", false, info.isTextauth());
    }

    @Ignore
    @Test
    public void invalidArgumentTest() throws Exception, UserError {
        try {
            AppclientCommandArguments.newInstance(
                Arrays.asList("-badarg"));
            fail("did not throw expected IllegalArgumentException due to an invalid arg");
        } catch (IllegalArgumentException e) {
            // no-op so test passes
        } catch (Exception e) {
            fail("expected IllegalArgumentException but got " + e.getClass().getName());
        }
    }

    @Test
    public void disallowMainclassAndName() throws Exception {
        try {
            AppclientCommandArguments.newInstance(
                    Arrays.asList("-mainclass","x.y.Main","-name","some-display-name"));
            fail("did not detect incorrect spec of mainclass and name");
        } catch (UserError e) {
            // suppress exception for a successful test
        }
    }

    @Test
    public void allowMultiValuedTargetServer() throws Exception, UserError {
        final AppclientCommandArguments cmdArgs = AppclientCommandArguments.newInstance(
                Arrays.asList("-targetserver","\"" + EXPECTED_TARGETSERVER_VALUE + "\""));
        assertEquals("did not process targetserver list correctly",
                EXPECTED_TARGETSERVER_VALUE,
                cmdArgs.getTargetServer());
    }

    @Test
    public void useTransactionLogString() {
        final Logger logger = LogDomains.getLogger(JavaEETransactionManagerSimplified.class,
                LogDomains.JTA_LOGGER);
        final String target = "enterprise_used_delegate_name";
        try {
            final String result = logger.getResourceBundle().getString(target);
            assertTrue("message key look-up failed", (result != null &&
                    ! target.equals(result)));
        } catch (MissingResourceException ex) {
            fail("could not find message key");
        }
    }

    @Test
    public void checkPasswordInFile() {
        final Properties props = new Properties();
        props.setProperty(AppclientCommandArguments.PASSWORD_FILE_PASSWORD_KEYWORD,
                EXPECTED_PASSWORD_IN_PASSWORD_FILE);
        try {
            final AppclientCommandArguments cmdArgs = prepareWithPWFile(props);
            final char[] pwInObject = cmdArgs.getPassword();
            assertTrue("Password " + EXPECTED_PASSWORD_IN_PASSWORD_FILE +
                    " in password file does not match password " + new String(pwInObject) +
                    " returned from AppclientCommandArguments object",
                    Arrays.equals(pwInObject, EXPECTED_PASSWORD_IN_PASSWORD_FILE.toCharArray()));

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }



    @Test
    public void checkErrorHandlingIfRequiredPasswordInPasswordFileIsMissing() {
        final Properties props = new Properties();
        props.setProperty("UNEXPECTED", EXPECTED_PASSWORD_IN_PASSWORD_FILE);
        try {
            final AppclientCommandArguments cmdArgs = prepareWithPWFile(props);
            fail("Missing password in password file NOT correctly detected and flagged");
        } catch (UserError ue) {
            /*
             * This is what we expect - a UserError complaining about the
             * missing password value.
             */
            return;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private AppclientCommandArguments prepareWithPWFile(
            final Properties props) throws UserError, IOException {
        final File passwordFile = createTempPWFile(props);

        return AppclientCommandArguments.newInstance(
                Arrays.asList("-passwordfile","\"" + passwordFile.getAbsolutePath() + "\""));
    }

    private File createTempPWFile(final Properties props) throws IOException {
        final File tempFile = File.createTempFile("accpw", ".txt");
        props.store(new FileWriter(tempFile), "temp file for acc unit test");
        tempFile.deleteOnExit();
        return tempFile;
    }


}
