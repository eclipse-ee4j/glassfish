/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.adapter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/** Package-private class to provide utilities.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @since GlassFish V3
 */
final class Utils {

    /** Reads the given file in this package and returns it as a String.
     *  If there is any problem in reading an IOException is thrown.
     * @param name representing just the complete name of file to be read, e.g. foo.html
     * @return String
     * @throws IOException
     */
    static String packageResource2String(String name) throws IOException {
        String file = Utils.class.getPackage().getName().replace('.', '/') + "/" + name;
        InputStream is=null;
        try {
            is = new BufferedInputStream(Utils.class.getClassLoader().getResourceAsStream(file));
            byte[] bytes = new byte[1024];
            int read;
            StringBuilder sb = new StringBuilder();
            while ((read = is.read(bytes)) != -1) {
                sb.append(new String(bytes, 0, read, "UTF-8"));
            }
            return ( sb.toString());
        } finally {
            if (is!=null) {
                try {
                    is.close();
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }
    }
}
