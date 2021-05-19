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

package com.sun.enterprise.util.zip;

import java.io.File;

/**
 * This class encapsulates the two pieces of information required to make a
 * ZipEntry -- the "real" path, and the path you want to appear in the zip file
 */
public class ZipItem
{
        /**
     * Construct a ZipItem
     *
         * @param file The actual file
         * @param name The zip entry name - i.e. the relative path in the zip file
         * @throws ZipFileException
         */
        public ZipItem(File file, String name) throws ZipFileException
        {
                //if(!file.exists())
                //        throw new ZipFileException("File doesn't exist: " + file);
                if(name == null || name.length() <= 0)
                        throw new ZipFileException("null or empty name for ZipItem");

                this.file = file;
                this.name = name;
        }

        /**
     * Returns a String represenation of the real filename and the zip entry
     * name.
     *
         * @return String with the path and the zip entry name
         */
        public String toString()
        {
                return "File: " + file.getPath() + ", name: " + name;
        }

    /**
     * Returns the zip entry name
     *
     * @return   the zip entry name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Returns the actual file
     *
     * @return  the actual file
     */
    public File getFile()
    {
        return this.file;
    }

        ///////////////////////////////////////////////////////////////////////////

        File        file;
        String        name;
}
