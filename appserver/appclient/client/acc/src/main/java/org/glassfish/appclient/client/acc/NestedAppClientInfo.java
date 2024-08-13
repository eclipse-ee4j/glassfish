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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ReadableArchive;

/**
 * Represents an app client that is nested inside an enterprise app.
 *
 * Note that this could be either an undeployed ear that contains one or more
 * embedded app clients or the generated jar file from the back-end that
 * intentionally resembles an application archive because of other files that
 * had to be packaged with the app client.
 *
 * @author tjquinn
 */
public class NestedAppClientInfo extends AppClientInfo {

    /** which of possibly several app clients in the app the user chose */
    private ApplicationClientDescriptor selectedAppClientDescriptor = null;

    /** display name specified (if any) on the command line to use in selecting the desired client main class */
    private String displayNameFromCommandLine;

    public NestedAppClientInfo(
            boolean isJWS, Logger logger, File archive,
            Archivist archivist, String mainClassFromCommandLine,
            String displayNameFromCommandLine) {
//        super(isJWS, logger, archive, archivist, mainClassFromCommandLine);
        super(isJWS, logger, mainClassFromCommandLine);
        this.displayNameFromCommandLine = displayNameFromCommandLine;
    }

    /**
     *Reports which app client embedded in the application archive is the
     *one the user has selected using either the main class or display name
     *arguments from the command line.
     *@return the app client descriptor for the user-selected app client
     */
    @Override
    protected ApplicationClientDescriptor getAppClient(Archivist archivist) {

        if (selectedAppClientDescriptor != null) {
            return selectedAppClientDescriptor;
        }

        Application app = Application.class.cast(archivist.getDescriptor());

        /*
         *There could be one or more app clients embedded in the enterprise app
         *in the archive.  Choose which one to run based on the user's
         *command-line input.
         */
        Set<ApplicationClientDescriptor> embeddedAppClients =
            (Set<ApplicationClientDescriptor>)
                app.getBundleDescriptors(ApplicationClientDescriptor.class);

        /*
         *Make sure the application module contains at least one app client.
         */
        if (embeddedAppClients.size() == 0) {
            throw new IllegalArgumentException(
                getLocalString(
                    "appclient.noEmbeddedAppClients",
                    "The specified application module does not contain any app clients"));
        }

        /*
         *If there is exactly one app client in the ear, then that's the app
         *client to use.
         */
        if (embeddedAppClients.size() == 1) {
            selectedAppClientDescriptor = useFirstEmbeddedAppClient(
                    embeddedAppClients, mainClassFromCommandLine);
        } else {
            selectedAppClientDescriptor = chooseFromEmbeddedAppClients(
                    embeddedAppClients, mainClassFromCommandLine,
                    displayNameFromCommandLine);

            /*
             *Make sure that we've selected an app client.
             */
            if (selectedAppClientDescriptor == null) {
                if (mainClassFromCommandLine != null) {
                    throw new IllegalArgumentException(getLocalString(
                            "appclient.noMatchingClientUsingMainClass",
                            "Could not locate an embedded app client matching the main class name {0}",
                            mainClassFromCommandLine));
                } else {
                    throw new IllegalArgumentException(getLocalString(
                            "appclient.noMatchingClientUsingDisplayName",
                            "Could not locate an embedded app client matching the display name {0}",
                            displayNameFromCommandLine));
                }
            }
        }
        return selectedAppClientDescriptor;
    }

    private ApplicationClientDescriptor chooseFromEmbeddedAppClients(
            Set<ApplicationClientDescriptor> embeddedAppClients,
            String mainClassFromCommandLine,
            String displayNameFromCommandLine) {
        ApplicationClientDescriptor result = null;

        /*
         *There are at least two app clients embedded in the ear.
         *
         *To remain compatible with earlier releases the logic below
         *exits the loop immediately upon finding a matching app client
         *using the user-provided main class name.  If there are other
         *app clients with the same main class those are ignored.
         *
         *On the other hand, if the user specified the target display name
         *then the logic below makes sure that exactly one app client
         *has that display name.
         *
         */
        for (ApplicationClientDescriptor candidate : embeddedAppClients) {
           /*
            *If the user specified a main class name, use that value to
            *match against the candiate.
            */
           if (mainClassFromCommandLine != null) {
               if (candidate.getMainClassName().equals(mainClassFromCommandLine)) {
                   result = candidate;
                   /*
                    *Because the main class name is used as the criteria,
                    *exit the loop as soon as one matching app client if found.
                    */
                   break;
               }
           } else {
               /*
                *We know at this point that the user provided a display name.
                */
               if (candidate.getName().equals(displayNameFromCommandLine)) {
                   /*
                    *Make sure no other candidate already matched the
                    *target display name.
                    */
                   if (result == null) {
                       result = candidate;
                       /*
                        *Because the display name is used as the matching
                        *criteria, continue the loop to make sure there are
                        *not multiple app clients with the same display name
                        */
                   } else {
                       throw new IllegalArgumentException(getLocalString(
                               "appclient.duplicate_display_name",
                               "More than one nested app client was found with the display name {0}",
                               displayNameFromCommandLine));
                   }
               }
           }
        }
        return result;
    }

    private ApplicationClientDescriptor useFirstEmbeddedAppClient(Set<ApplicationClientDescriptor> embeddedAppClients, String mainClassNameFromCommandLine) {
        ApplicationClientDescriptor result = null;

        /*
         *If the size is 1 then there is sure to be a non-null .next.
         *Still, may as well be sure.
         */
        Iterator<ApplicationClientDescriptor> it = embeddedAppClients.iterator();
        if ( ! it.hasNext()) {
            throw new IllegalStateException(getLocalString(
                    "appclient.unexpectedEndOfEmbeddedClients",
                    "The application module seems to contain one app client but the iterator reported no more elements prematurely"));
        }

        result = embeddedAppClients.iterator().next();

        /*
         *If, in addition, the user specified a main class on the command
         *line, then use the user's class name as the main class name, rather
         *than the class specified by the Main-Class attribute in the
         *app client archive.  This allows the user to override the Main-Class
         *setting in the app client's manifest.
         */
        if (mainClassNameFromCommandLine != null) {
            result.setMainClassName(mainClassNameFromCommandLine);
        }
        return result;
    }

//    /**
//     *Expands the contents of the source archive into a temporary
//     *directory, using the same format as backend server expansions.
//     *@param file an archive file to be expanded
//     *@return an opened FileArchive for the expanded directory archive
//     *@exception IOException in case of errors during the expansion
//     */
//    @Override
//    protected ReadableArchive expand(File file)
//        throws IOException, Exception {
//
//        File tmpDir = createTmpArchiveDir(file);
//        _logger.fine("Expanding original archive " + file.getAbsolutePath() +
//                " into " + tmpDir.getAbsolutePath());
//
//        // first explode the top level jar
//        ModuleExploder.explodeJar(file, tmpDir);
//
//        // now we need to load the application standard deployment descriptor.
//        ReadableArchive appArchive = archiveFactory.openArchive(tmpDir);
//
//        ApplicationArchivist archivist = new ApplicationArchivist();
//        if (archivist.hasStandardDeploymentDescriptor(appArchive)) {
//            appDesc = (Application)
//            archivist.readStandardDeploymentDescriptor(appArchive);
//        } else {
//            appDesc = Application.createApplication(habitat, null, new ApplicationClientDescriptor().getModuleDescriptor());
//        }
//
//        // explode the sub modules, skipping the ones that do not exist since
//        // the generated appclient jar files do not contain web content
//        for (ModuleDescriptor bundle : appDesc.getModules()) {
//
//            String moduleName = bundle.getArchiveUri();
//            File srcArchive = new File(tmpDir, moduleName);
//
//            if (srcArchive.exists()) {
//                String massagedModuleName =
//                    FileUtils.makeFriendlyFilename(moduleName);
//                File moduleDir =
//                    new File(tmpDir, massagedModuleName);
//                ModuleExploder.explodeJar(srcArchive, moduleDir);
//
//                // delete the original module file
//                srcArchive.delete();
//            }
//        }
//
//        /*
//         *Leave the new archive open so the caller can use it directly.
//         */
//        return appArchive;
//    }

    /**
     * Construct the classpaths.  The classpaths constructed here is
     * slightly different from the backend.  It does not process any
     * web module.  The paths included are:
     * 1. all the module root directory (since expansion is needed)
     * 2. all the .jar files found in the archive
     */
    @Override
    protected List<String> getClassPaths(ReadableArchive archive) {

        List<String> paths = new ArrayList();
        String appRoot = archive.getURI().toASCIIString();
        paths.add(appRoot);

        //add all jar files
        for (Enumeration en = archive.entries(); en.hasMoreElements(); ) {
            String entryName = (String) en.nextElement();
            if (entryName.endsWith(".jar")) {
                String entry = appRoot + File.separator + entryName;
                paths.add(entry);
            }
        }

        return paths;
    }

    @Override
    protected String getAppClientRoot(
        ReadableArchive archive, ApplicationClientDescriptor descriptor) {
        String appRoot = archive.getURI().toASCIIString();
        String moduleUri = descriptor.getModuleDescriptor().getArchiveUri();
        String moduleRoot = appRoot + File.separator +
                    FileUtils.makeFriendlyFilename(moduleUri);
        return moduleRoot;
    }
}
