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

package com.sun.enterprise.config.util;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Strings -- Get your Strings here. One file with Strings So one class for messing with them! Nothing in here is public
 * protected. Only for use by this one java package.
 *
 * @author Byron Nevins
 */

final class Strings {
    private Strings() {
        // no instances allowed!
    }

    final static String get(String indexString) {
        return strings.get(indexString);
    }

    final static String get(String indexString, Object... objects) {
        return strings.get(indexString, objects);
    }

    final private static LocalStringsImpl strings = new LocalStringsImpl(Strings.class);
}
