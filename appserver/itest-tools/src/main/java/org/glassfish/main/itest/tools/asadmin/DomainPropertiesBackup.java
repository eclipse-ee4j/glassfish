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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.glassfish.main.itest.tools.GlassFishTestEnvironment.getAsadmin;
import static org.glassfish.main.itest.tools.asadmin.AsadminResultMatcher.asadminOK;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tool to simplify properties backup so you can symmetrically set some subset of properties before
 * and after the test
 *
 * @author Ondro Mihalyi
 * @author David Matejcek
 */
public class DomainPropertiesBackup {

    private final List<String> settingsBackup;

    /**
     * Creates the backup by calling asadmin get keyFilter.
     * From retrieved list excludes entries with the specified key.
     *
     * @param keyFilter
     * @param excludedKeys
     */
    public DomainPropertiesBackup(String keyFilter, String... excludedKeys) {
        final AsadminResult result = getAsadmin(true).exec(5_000, "get", keyFilter);
        assertThat(result, asadminOK());
        Predicate<String> inclusionFilter = s -> {
            for (String key : excludedKeys) {
                if (s.startsWith(key + '=')) {
                    return false;
                }
            }
            return true;
        };
        settingsBackup = Stream.of(result.getStdOut().split("\n")).filter(inclusionFilter).collect(Collectors.toList());
    }

    /**
     * Restore properties from the backup by calling asadmin set.
     */
    public void restore() {
        String[] args = new String[settingsBackup.size() + 1];
        args[0] = "set";
        for (int i = 1; i < args.length; i++) {
            args[i] = settingsBackup.get(i - 1);
        }
        final AsadminResult result = getAsadmin(true).exec(5_000, args);
        assertThat(result, asadminOK());
    }


    /**
     * @return backup for keys starting by
     *         <code>resources.jdbc-connection-pool.DerbyPool.</code>
     */
    public static DomainPropertiesBackup backupDerbyPool() {
        return new DomainPropertiesBackup("resources.jdbc-connection-pool.DerbyPool.*",
            "resources.jdbc-connection-pool.DerbyPool.name");
    }
}
