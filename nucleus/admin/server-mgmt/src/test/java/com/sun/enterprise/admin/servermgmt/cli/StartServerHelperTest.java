/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.util.HostAndPort;

import org.glassfish.api.admin.CommandException;
import org.junit.jupiter.api.Test;

import static com.sun.enterprise.admin.servermgmt.cli.StartServerHelper.parseCustomEndpoints;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StartServerHelperTest {

    @Test
    void parseEndpoints() throws Exception {
        assertThat(parseCustomEndpoints(null), empty());
        assertThat(parseCustomEndpoints(" "), empty());
        assertThat(parseCustomEndpoints("carrot:123,https://google.com,sftp://xy:21"),
            contains(
                new HostAndPort("carrot", 123, false),
                new HostAndPort("google.com", 443, true),
                new HostAndPort("xy", 21, false)
        ));
        assertThrows(CommandException.class, () -> parseCustomEndpoints("sftp://x"));
    }
}
