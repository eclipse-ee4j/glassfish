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
 * ZipFileTester.java
 *
 * Created on November 6, 2001, 9:08 PM
 */

package com.sun.enterprise.util.zip;

/**
 *
 * @author  bnevins
 * @version
 */
public class ZipFileTester {

        /** Creates new ZipFileTester */
    public ZipFileTester() {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[])
        {
                try
                {
                        ZipFile zip = new ZipFile("C:\\temp\\petstore.ear", "C:\\temp\\petstore");
                        java.util.List l = zip.explode();
                        System.out.println("**** Filelist ****\n" + l);
                }
                catch(Exception e)
                {
                        System.out.println("error: " + e);
                        e.printStackTrace();
                }
    }

}
