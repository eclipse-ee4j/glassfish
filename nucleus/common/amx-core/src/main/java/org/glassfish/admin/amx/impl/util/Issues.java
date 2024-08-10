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

package org.glassfish.admin.amx.impl.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.admin.amx.util.AMXLoggerInfo;

/**
 */
public final class Issues {

    private final Set<String> mIssues = Collections.synchronizedSet(new HashSet<String>());

    private Issues() {
        // disallow instantiation
    }
    private static final Issues AMX_ISSUES = new Issues();

    public static Issues getAMXIssues() {
        return AMX_ISSUES;
    }

    public void notDone(final String description) {
        final boolean wasMissing = mIssues.add(description);
        if (wasMissing) {
            AMXLoggerInfo.getLogger().log(Level.FINE, "NOT DONE: {0}", description);
        }
    }
}

































