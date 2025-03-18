/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.logging.LogDomains;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.embeddable.client.UserError;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author tjquinn
 */
public class AppclientCommandArgumentsTest {

    private static final String USER_VALUE = "joe-the-user";
    private static final String PASSWORDFILE_NAME = "topSecret.stuff";
    private static final String EXPECTED_TARGETSERVER_VALUE = "A:1234,B:5678";
    private static final String EXPECTED_PASSWORD_IN_PASSWORD_FILE = "mySecretPassword";

    @Test
    public void testA() throws Exception, UserError {
        AppclientCommandArguments info = AppclientCommandArguments
            .newInstance(Arrays.asList("-textauth", "-user", USER_VALUE));

        assertTrue(info.isTextauth(), "text auth not set");
        assertFalse(info.isNoappinvoke(), "noappinvoke incorrectly set");
        assertEquals(USER_VALUE, info.getUser(), "user incorrectly set");
        assertNull(info.getMainclass(), "mainclass wrong");
    }

    @Test
    public void testB() throws Exception, UserError {
        AppclientCommandArguments info = AppclientCommandArguments
            .newInstance(Arrays.asList("-mainclass", PASSWORDFILE_NAME, "-noappinvoke"));

        assertEquals(PASSWORDFILE_NAME, info.getMainclass(), "wrong main class");
        assertEquals(true, info.isNoappinvoke(), "noappinvoke not set");
        assertEquals(null, info.getUser(), "user should have been null");
        assertEquals(false, info.isTextauth(), "textauth found but should be absent");
    }

    @Test
    @Disabled("Doesn't throw anything.")
    public void invalidArgumentTest() throws Exception {
        assertThrows(IllegalArgumentException.class,
            () -> AppclientCommandArguments.newInstance(Arrays.asList("-badarg")));
    }

    @Test
    public void disallowMainclassAndName() throws Exception {
        assertThrows(UserError.class, () -> AppclientCommandArguments
            .newInstance(Arrays.asList("-mainclass", "x.y.Main", "-name", "some-display-name")));
    }

    @Test
    public void allowMultiValuedTargetServer() throws Exception, UserError {
        final AppclientCommandArguments cmdArgs = AppclientCommandArguments.newInstance(
                Arrays.asList("-targetserver","\"" + EXPECTED_TARGETSERVER_VALUE + "\""));
        assertEquals(EXPECTED_TARGETSERVER_VALUE, cmdArgs.getTargetServer(),
            "did not process targetserver list correctly");
    }

    @Test
    public void useTransactionLogString() {
        final Logger logger = LogDomains.getLogger(JavaEETransactionManagerSimplified.class, LogDomains.JTA_LOGGER);
        final String target = "enterprise_used_delegate_name";
        final String result = logger.getResourceBundle().getString(target);
        assertEquals("DTX5019: Transaction Manager is ready. Using [{0}] as the delegate", result,
            "message key look-up failed");
    }

    @Test
    public void checkPasswordInFile() throws Exception, UserError {
        final Properties props = new Properties();
        props.setProperty(AppclientCommandArguments.PASSWORD_FILE_PASSWORD_KEYWORD, EXPECTED_PASSWORD_IN_PASSWORD_FILE);
        final AppclientCommandArguments cmdArgs = prepareWithPWFile(props);
        assertArrayEquals(EXPECTED_PASSWORD_IN_PASSWORD_FILE.toCharArray(), cmdArgs.getPassword(),
            "Password in password file does not match expected password");
    }

    @Test
    public void checkErrorHandlingIfRequiredPasswordInPasswordFileIsMissing() {
        final Properties props = new Properties();
        props.setProperty("UNEXPECTED", EXPECTED_PASSWORD_IN_PASSWORD_FILE);
        assertThrows(UserError.class, () -> prepareWithPWFile(props),
            "Missing password in password file NOT correctly detected and flagged");
    }

    private AppclientCommandArguments prepareWithPWFile(final Properties props) throws UserError, IOException {
        final File passwordFile = createTempPWFile(props);
        return AppclientCommandArguments
            .newInstance(Arrays.asList("-passwordfile", "\"" + passwordFile.getAbsolutePath() + "\""));
    }

    private File createTempPWFile(final Properties props) throws IOException {
        final File tempFile = File.createTempFile("accpw", ".txt");
        props.store(new FileWriter(tempFile), "temp file for acc unit test");
        tempFile.deleteOnExit();
        return tempFile;
    }
}
