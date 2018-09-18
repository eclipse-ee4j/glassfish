/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

/**
 * Only accessible inside this package.
 * @author bnevins
 */
class Constants {
    private Constants() {
        // no instances are allowed
    }

    // these are all not localized because REST etc. depends on them.
    static final String NONE = "Nothing to list.";

    static final String PARTIALLY_RUNNING_DISPLAY = " partially running";
    static final String PARTIALLY_RUNNING = "PARTIALLY_RUNNING";
}
