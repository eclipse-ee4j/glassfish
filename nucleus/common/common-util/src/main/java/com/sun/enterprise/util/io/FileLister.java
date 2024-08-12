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

/*
 * foo.java
 *
 * Created on November 11, 2001, 12:09 AM
 */

package com.sun.enterprise.util.io;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author  bnevins
 * @version
 */
public abstract class FileLister
{
        FileLister(File root)
        {
                mainRoot = root;
                fileList = new ArrayList<File>();
        }
    public void keepEmptyDirectories()
    {
        keepEmpty = true;
    }

        abstract protected boolean relativePath();

        public String[] getFiles()
        {
                getFilesInternal(mainRoot);
                String[] files = new String[fileList.size()];

                if(files.length <= 0)
                        return files;

                int len = 0;

                if(relativePath())
                        len = mainRoot.getPath().length() + 1;

                for(int i = 0; i < files.length; i++)
                {
                        files[i] = (fileList.get(i)).getPath().substring(len).replace('\\', '/');
                }

                Arrays.sort(files, String.CASE_INSENSITIVE_ORDER);
                return files;
        }


        public void getFilesInternal(File root)
        {
                File[] files = root.listFiles();

                for(int i = 0; i < files.length; i++)
                {
                        if(files[i].isDirectory())
                        {
                                getFilesInternal(files[i]);
                        }
                        else
                                fileList.add(files[i]);        // actual file
                }
        // add empty directory, if the option is turned on
        if(files.length <= 0 && keepEmpty)
            fileList.add(root);

        }


    private        ArrayList<File>        fileList = null;
    private File                mainRoot         = null;
    private boolean                keepEmpty         = false;
}



