/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee7.batch.test.config;

import java.util.*;

import static org.junit.Assert.*;

import org.glassfish.javaee7.batch.test.util.CommandUtil;
import org.junit.Test;

public class CommandUtilTest {

    @Test
    public void basicTest() {
        assertTrue(true);
    }

    @Test
    public void runCommand() {
        List<String> result = CommandUtil.getInstance().executeCommandAndGetAsList("ls", "-l").result();
        for (String line : result)
            System.out.println(line);
        assertTrue(true);
    }

    @Test
    public void runCommandWithPipe() {
        CommandUtil cmdUtil = CommandUtil.getInstance().executeCommandAndGetAsList("als", "-l | wc");

        assertTrue(!cmdUtil.ranOK());
    }
}
