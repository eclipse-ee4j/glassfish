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
 * DirectoryScanner.java
 *
 *
 * Created on February 19, 2003, 10:17 AM
 */

package org.glassfish.deployment.autodeploy;

import com.sun.enterprise.util.LocalStringManagerImpl;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * Implementation of Directory scanner for autodeployment  </br>
 * Providing functionality for scanning the input source directory  </br>
 * and return the list of deployable components for autodeployment.</br>
 * Provide the list of deployable modules/application, depending upon the "type" entry </br>
 * passed to getAllDeployableEntity(File autodeployDir, String type).
 *
 *@author vikas
 */
public class AutoDeployDirectoryScanner implements DirectoryScanner{

    public static final Logger deplLogger =
        org.glassfish.deployment.autodeploy.AutoDeployer.deplLogger;

    @LogMessageInfo(message = "Error occurred: {0}", cause="An exception was caught when the operation was attempted", action="See the exception to determine how to fix the error", level="SEVERE")
    private static final String EXCEPTION_OCCURRED = "NCLS-DEPLOYMENT-00040";

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(AutoDeployDirectoryScanner.class);

    public AutoDeployDirectoryScanner() {
    }

     public void deployedEntity(File autodeployDir, File deployedEntity) {
         try {
         AutoDeployedFilesManager adfm = AutoDeployedFilesManager.loadStatus(autodeployDir);
         adfm.setDeployedFileInfo(deployedEntity);
         adfm.writeStatus();
         } catch (Exception e) {
             printException(e);
             // Do nothing
         }

     }

     public void undeployedEntity(File autodeployDir, File undeployedEntity) {
         try {
         AutoDeployedFilesManager adfm = AutoDeployedFilesManager.loadStatus(autodeployDir);
         adfm.deleteDeployedFileInfo(undeployedEntity);
         adfm.writeStatus();
         } catch (Exception e) {
             printException(e);
             // Do nothing
         }
     }

    /**
     * return true if any new deployable entity is  present in autodeployDir
     * @param autodeployDir
     * @return
     */
    public boolean hasNewDeployableEntity(File autodeployDir) {
        boolean newFilesExist=false;
            try {
                AutoDeployedFilesManager adfm = AutoDeployedFilesManager.loadStatus(autodeployDir);
                if(adfm.getFilesForDeployment(getListOfFiles(autodeployDir)).length > 0) {
                    //atleast one new file is there
                     newFilesExist=true;
                }
            } catch (Exception e) {
                printException(e);
                return false;
            }

        return newFilesExist;

    }
    // this should never be called from system dir autodeploy code...
    public File[] getAllFilesForUndeployment(File autodeployDir, boolean includeSubdir) {

        try {
            AutoDeployedFilesManager adfm = AutoDeployedFilesManager.loadStatus(autodeployDir);
            return adfm.getFilesForUndeployment(getListOfFiles(autodeployDir, includeSubdir));
            } catch (Exception e) {
                printException(e);
                return new File[0];
            }
    }

    /**
     * Get the list of all deployable files
     * @param autodeployDir
     * @return  */
    public File[] getAllDeployableModules(File autodeployDir, boolean includeSubDir) {

        AutoDeployedFilesManager adfm = null;
        try {
        adfm = AutoDeployedFilesManager.loadStatus(autodeployDir);
        } catch (Exception e) {
            printException(e);
            return new File[0];
        }

        return adfm.getFilesForDeployment(getListOfFiles(autodeployDir, includeSubDir));
    }

    protected void printException(Exception e) {
        LogRecord lr = new LogRecord(Level.SEVERE, EXCEPTION_OCCURRED);
        Object args[] = { e.getMessage() };
        lr.setParameters(args);
        lr.setThrown(e);
        deplLogger.log(lr);
    }

    protected File[] getListOfFiles(File dir) {
        return getListOfFiles(dir, false);
    }

    protected File[] getListOfFiles(File dir, boolean includeSubDir) {
        final Set<File> fileSet = getListOfFilesAsSet(dir, includeSubDir);
        return fileSet.toArray(new File[fileSet.size()]);
    }


    static Set<File> getListOfFilesAsSet(File dir, boolean includeSubDir) {
        Set<File> result = new HashSet<File>();
        File[] dirFiles = dir.listFiles();
        for (File dirFile : dirFiles) {
            String name = dirFile.getName();
            String fileType = name.substring(name.lastIndexOf(".") + 1);
            if ( ! dirFile.isDirectory()) {
                if (fileType != null && !fileType.equals("") &&
                        ! typeIsMarkerType(fileType)) {
                    result.add(dirFile);
                    continue;
                }
            } else {
                if (! dirFile.getName().equals(AutoDeployer.STATUS_SUBDIR_PATH)) {
                    if (includeSubDir) {
                        result.addAll(getListOfFilesAsSet(dirFile, true));
                    } else {
                        result.add(dirFile);
                    }
                }
            }
        }
        return result;
    }

    private static boolean typeIsMarkerType(String fileType) {
        for (String markerSuffix : AutoDeployConstants.MARKER_FILE_SUFFIXES) {
            if (fileType.endsWith(markerSuffix)) {
                return true;
            }
        }
        return false;
    }
}
