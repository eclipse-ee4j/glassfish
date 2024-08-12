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

package org.glassfish.appclient.client.acc;

import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.AppClientArchivist;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.glassfish.apf.AnnotationProcessorException;

/**
 * Represents an app client specified by a .class file on the command line.
 * @author tjquinn
 */
public class ClassFileAppClientInfo extends AppClientInfo {

    /** the class file name specified on the command line */
    private String classFileFromCommandLine;

    /**
     *Creates a new instance of the class file app client info.
     *@param isJWS whether Java Web Start was used to launch the app client
     *@param logger the Logger available for writing log messages
     *@param archive the archive containing the app client (and perhaps other files as well)
     *@param archivist the archivist appropriate to the type of archive being processed
     *@param mainClassFromCommandLine the main class command-line argument value
     *@param classFileFromCommandLine the class file name from the command line arguments
     */
    protected ClassFileAppClientInfo(
            boolean isJWS, Logger logger, String mainClassFromCommandLine,
            String classFileFromCommandLine) {
        super(isJWS, logger, mainClassFromCommandLine);
        this.classFileFromCommandLine = classFileFromCommandLine;
    }

    @Override
    protected String getMainClassNameToRun(ApplicationClientDescriptor acDescr) {
        return classFileFromCommandLine;
    }

    @Override
    protected void massageDescriptor()
            throws IOException, AnnotationProcessorException {
        ApplicationClientDescriptor appClient = getDescriptor();
        appClient.setMainClassName(classFileFromCommandLine);
        appClient.getModuleDescriptor().setStandalone(true);
        FileArchive fa = new FileArchive();
        fa.open(new File(classFileFromCommandLine).toURI());
        new AppClientArchivist().processAnnotations(appClient, fa);
    }

//    @Override
//    protected ReadableArchive expand(File file)
//        throws IOException, Exception {
//        return archiveFactory.openArchive(file);
//    }

//    protected boolean deleteAppClientDir() {
//        return false;
//    }
}
