/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.itest.tools.asadmin;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

/**
 * @author David Matejcek
 */
public class DetachedTerseAsadminResult extends AsadminResult {

    private final String jobId;

    public DetachedTerseAsadminResult(String commandName, int exitCode, String stdOut, String stdErr) {
        super(commandName, exitCode, stdOut, stdErr);
        this.jobId = parseJobId(stdOut);
    }


    public String getJobId() {
        return jobId;
    }


    private static String parseJobId(String stdOut) {
        String[] lines = stdOut.split(System.lineSeparator());
        assertThat(Arrays.toString(lines), lines, arrayWithSize(1));
        return Integer.valueOf(lines[0].trim()).toString();
    }
}
