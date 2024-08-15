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

/*
 * ZipFilenameFilter.java
 *
 * Created on March 30, 2004, 9:40 PM
 */

package com.sun.enterprise.backup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 *
 * @author  bnevins
 * Tiny class.  It is here because it is used in 2 places --
 * RestoreManager and ListManager and it avoids code duplication.
 */

class ZipFilenameFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return name.toLowerCase(Locale.ENGLISH).endsWith(".zip");
    }
}
