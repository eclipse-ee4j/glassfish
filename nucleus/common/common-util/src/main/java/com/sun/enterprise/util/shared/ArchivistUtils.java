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

package com.sun.enterprise.util.shared;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class contains utility methods that handles the archives.
 *
 * @author  Deployment Dev Team
 * @version
 */
public class ArchivistUtils {

    /**
     * Utility method that eads the input stream fully and writes the bytes to
     * the current entry in the output stream.
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        copyWithoutClose(is, os);
        is.close();
        os.close();
    }

    /**
     * Utility method that eads the input stream fully and writes the bytes to
     * the current entry in the output stream.
     */
    public static void copyWithoutClose(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int len = 0;
        while (len != -1) {
            try {
                len = is.read(buf, 0, buf.length);
            } catch (EOFException eof){
                break;
            }

            if(len != -1) {
                os.write(buf, 0, len);
            }
        }
        os.flush();
    }
}
