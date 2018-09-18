/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
//import com.sun.enterprise.admin.cli.RemoteCommand;
import java.io.File;


/**
 * junit test to test RemoteCommand class
 */
public class RemoteCommandTest {
    @Test
    public void fake() {
        System.out.println("Tests Suspended Temporarily");
    }
    
  /*************
    private RemoteCommand rc = null;

    @Test
    public void getUploadFileTest() {
            //returns false  if upload option is not specified and
            //command name is not deploy
        assertFalse(rc.getUploadFile(null, "undeploy", null));
            //returns true by default if upload option is not specified
            //and command name is deploy
        assertTrue(rc.getUploadFile(null, "deploy", "RemoteCommandTest.java"));
            //returns false if upload option is not specified and
            //command name is deploy and a valid directory is provided
        assertFalse(rc.getUploadFile(null, "deploy", System.getProperty("user.dir")));
            //return false
        assertFalse(rc.getUploadFile("yes", "dummy", null));
            //return true
        assertTrue(rc.getUploadFile("true", "dummy", null));                    
    }

    @Test
    public void getFileParamTest() {
        try {
                //testing filename
            assertEquals("uploadFile=false and fileName=test", "test",
                         rc.getFileParam(false, new File("test")));
                //testing absolute path
            final String userDir = new File(System.getProperty("user.dir")).getCanonicalPath();
            assertEquals("uploadFile=false and fileName=RemoteCommandTest",
                         userDir,
                         rc.getFileParam(false, new File(System.getProperty("user.dir"))));
                //testing relative path
            assertEquals("uploadFile=false and fileName=current-directory",
                         new File(".").getCanonicalPath(),
                         rc.getFileParam(false, new File(".")));
        }
        catch(java.io.IOException ioe) {}
    }
    
    @Before
    public void setup() {
        rc = new RemoteCommand();
    }
*/
}
