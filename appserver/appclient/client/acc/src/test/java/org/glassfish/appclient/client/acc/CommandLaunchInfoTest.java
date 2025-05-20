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


import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.glassfish.appclient.client.acc.CommandLaunchInfo.ClientLaunchType;
import org.glassfish.embeddable.client.UserError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author tjquinn
 */
public class CommandLaunchInfoTest {

    private static final String FIRST_ACC_ARG = "first arg";
    private static final String SECOND_ACC_ARG = "second=arg";

    private static final String JAR_CLIENT_NAME = "there/myClient.jar";
    private static final String DIR_CLIENT_NAME = "here/myClient";
    private static final String USER_VALUE = "joe-the-user";
    private static final String PASSWORDFILE_PATH = "/topSecret.stuff";

    private static final List<String> expectedCommandArgs = Arrays.asList(FIRST_ACC_ARG, SECOND_ACC_ARG);

    @Test
    public void testA() throws Exception, UserError {
        final AgentArguments agentArgs = AgentArguments.newInstance(
                "mode=acscript" +
                ",client=jar=" + JAR_CLIENT_NAME +
                ",arg=-textauth" +
                ",arg=-user,arg=" + USER_VALUE);
        CommandLaunchInfo info = CommandLaunchInfo.newInstance(agentArgs);
        assertEquals(ClientLaunchType.JAR, info.getClientLaunchType(), "wrong client type");
        assertEquals(JAR_CLIENT_NAME, info.getClientName(), "wrong client name");

    }

    @Test
    public void testB() throws Exception, UserError {
        URL testFileURL = getClass().getResource(PASSWORDFILE_PATH);
        assertNotNull(testFileURL, "test file URL came back null");
        File testFile = new File(testFileURL.toURI());
        final AgentArguments agentArgs = AgentArguments.newInstance(
                "mode=acscript" +
                ",client=dir=" + DIR_CLIENT_NAME +
                ",arg=-passwordfile,arg=" + testFile.getAbsolutePath() +
                ",arg=-noappinvoke");
        CommandLaunchInfo info = CommandLaunchInfo.newInstance(agentArgs);

        assertEquals(ClientLaunchType.DIR, info.getClientLaunchType(), "wrong client type");
        assertEquals(DIR_CLIENT_NAME, info.getClientName(), "wrong client name");
    }

}
