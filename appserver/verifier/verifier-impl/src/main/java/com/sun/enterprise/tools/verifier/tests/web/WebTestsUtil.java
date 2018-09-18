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

package com.sun.enterprise.tools.verifier.tests.web;

import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.web.WebCheckMgrImpl;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.EventObject;

/** 
 * Singleton Utility class to load war archive and get class loader
 * 
 * @author Jerome Dochez
 * @author Sheetal Vartak
 * @version 1.0
 */
public class WebTestsUtil implements VerifierEventsListener {

    protected final String listenerClassPath = "WEB-INF/classes";   
    protected final String libraryClassPath = "WEB-INF/lib";   
    
    private final String separator= System.getProperty("file.separator");
    private static File warFile = new File(System.getProperty("java.io.tmpdir"), "listenertmp");       
    private static WebTestsUtil util = null;
    private static ClassLoader cl = null; 
    
    
    /** 
     * <p>
     * Get the unique instance for this class
     * </p>
     */
    public static WebTestsUtil getUtil(ClassLoader cLoader) {
    
        if (util==null) {
	    util = new WebTestsUtil();
	    WebCheckMgrImpl.addVerifierEventsListener(util);
	    cl = cLoader;
        }    
        return util;
    }
    
    
    private void deleteDirectory(String oneDir) {
        
        File[] listOfFiles;
        File cleanDir;

        cleanDir = new File(oneDir);
        if (!cleanDir.exists())  // Nothing to do.  Return; 
            return;

        listOfFiles = cleanDir.listFiles();
        if(listOfFiles != null) {       
            for(int countFiles = 0; countFiles < listOfFiles.length; countFiles++) {                    
                if (listOfFiles[countFiles].isFile()) {
                    listOfFiles[countFiles].delete();
                } else { // It is a directory
                    String nextCleanDir =  cleanDir + separator + listOfFiles[countFiles].getName();
                    File newCleanDir = new File(nextCleanDir);
                    deleteDirectory(newCleanDir.getAbsolutePath());
                }                    
            }// End for loop
        } // End if statement            
 
        cleanDir.delete();
    }
    
    /**
     * <p>
     * Individual test completion notification event
     * </p>
     * @param e event object which source is the result of the individual test
     */
    public void testFinished(EventObject e) {
        // do nothing, we don't care
    }
    
    /**
     * <p>
     * Notification that all tests pertinent to a verifier check manager have 
     * been completed
     * </p>
     * @param e event object which source is the check manager for the
     * completed tests
     */
    public void allTestsFinished(EventObject e) {
        // remove tmp files
        if ((warFile != null) && (warFile.exists())) {
            deleteDirectory(warFile.getAbsolutePath());
        }
        warFile=null;
        util=null;
        cl=null;
        WebCheckMgrImpl.removeVerifierEventsListener(this);        
    }

    private ClassLoader getClassLoader() {
	return cl;
    }   
    
    /**
     * <p>
     * load a class from the war archive file
     * </p>
     * @param className the class to load
     * @return the class object if it can be loaded
     */
    public Class loadClass(String className) throws Throwable {

        if ((warFile==null || !warFile.exists()) 
            && (getClassLoader() == null)) {
            throw new ClassNotFoundException();
        }
	    Class c = getClassLoader().loadClass(className);
	    return c;
    }
}
