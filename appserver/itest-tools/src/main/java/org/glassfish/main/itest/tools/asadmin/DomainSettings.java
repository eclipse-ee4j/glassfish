/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 *
 * @author Ondro Mihalyi
 */
public class DomainSettings {

    private final Asadmin asadmin;
    List<String> settingsBackup = new ArrayList<>();

    public DomainSettings(Asadmin asadmin) {
        this.asadmin = asadmin;
    }

    public void backupSettings(String getKey) {
        final AsadminResult result = asadmin.exec(5_000, "get", getKey);
        Stream.of(result.getStdOut().split("\n"))
            // Exclude "command successful
            .filter(line -> line.contains("="))
            // Exclude .name for connection pools which cannot be changed
            .filter(line -> !line.startsWith("resources.jdbc-connection-pool") || !line.contains(".name="))
            .forEach(settingsBackup::add);
    }

    public void restoreSettings() {
        String[] args = new String[settingsBackup.size() + 1];
        args[0] = "set";
        for (int i = 1; i < args.length; i++) {
            args[i] = settingsBackup.get(i - 1);
        }
        settingsBackup.clear();
        final AsadminResult result = asadmin.exec(5_000, args);
        assertThat(result, asadminOK());
    }

    public void backupDerbyPoolSettings() {
        backupSettings("resources.jdbc-connection-pool.DerbyPool.*");
    }

    /** Default is org.apache.derby.jdbc.ClientDataSource */
    public void setDerbyPoolEmbededded() {
        final AsadminResult result = asadmin.exec(5_000, "set",
            "resources.jdbc-connection-pool.DerbyPool.datasource-classname=org.apache.derby.jdbc.EmbeddedDataSource",
            "resources.jdbc-connection-pool.DerbyPool.property.PortNumber=",
            "resources.jdbc-connection-pool.DerbyPool.property.serverName=",
            "resources.jdbc-connection-pool.DerbyPool.property.URL=");
        assertThat(result, asadminOK());
        asadmin.exec(5_000, "get", "resources.jdbc-connection-pool.DerbyPool.*");
    }


}
