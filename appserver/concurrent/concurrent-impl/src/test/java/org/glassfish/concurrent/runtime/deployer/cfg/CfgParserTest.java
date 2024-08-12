/*
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

package org.glassfish.concurrent.runtime.deployer.cfg;

import com.sun.enterprise.deployment.types.CustomContextType;
import com.sun.enterprise.deployment.types.StandardContextType;

import org.junit.jupiter.api.Test;

import static com.sun.enterprise.deployment.types.StandardContextType.Classloader;
import static com.sun.enterprise.deployment.types.StandardContextType.JNDI;
import static com.sun.enterprise.deployment.types.StandardContextType.Security;
import static com.sun.enterprise.deployment.types.StandardContextType.WorkArea;
import static org.glassfish.concurrent.runtime.deployer.cfg.CfgParser.parseContextInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * @author David Matejcek
 */
public class CfgParserTest {

    @Test
    public void testParseContextInfo() {
        assertAll(
            () -> assertThat(parseContextInfo(null, "true"),
                containsInAnyOrder(StandardContextType.values())),
            () -> assertThat(parseContextInfo("Classloader, JNDI, Security, WorkArea", "true"),
                containsInAnyOrder(Classloader, JNDI, Security, WorkArea)),
            () -> assertThat(parseContextInfo("classloader, jndi, security, workarea", "true"),
                containsInAnyOrder(Classloader, JNDI, Security, WorkArea)),
            () -> assertThat(parseContextInfo("CLASSLOADER, JNDI, SECURITY, WORKAREA", "true"),
                containsInAnyOrder(Classloader, JNDI, Security, WorkArea)),
            () -> assertThat(parseContextInfo("JNDI", "false"),
                containsInAnyOrder(StandardContextType.values())),
            () -> assertThat(parseContextInfo("Classloader, JNDI, JNDI, blah, BEH, Security, WorkArea, ", "true"),
                containsInAnyOrder(Classloader, JNDI, Security, WorkArea, new CustomContextType("blah"), new CustomContextType("BEH")))        );
    }
}
