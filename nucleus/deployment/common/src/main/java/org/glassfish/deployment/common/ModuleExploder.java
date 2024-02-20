/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

package org.glassfish.deployment.common;


import org.glassfish.api.deployment.archive.Archive;
import java.io.*;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;

import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.util.zip.ZipFile;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.util.io.FileUtils;

import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * Simple Module exploder
 *
 * @author Jerome Dochez
 *
 */
public class ModuleExploder {

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Could not expand entry {0} into destination {1}", cause="An exception was caught when the entry was expanded", action="See the exception to determine how to fix the error", level="SEVERE")
    private static final String COULD_NOT_EXPAND_ENTRY = "NCLS-DEPLOYMENT-00005";

    protected static final StringManager localStrings =
            StringManager.getManager(ModuleExploder.class );

    protected static final String PRESERVED_MANIFEST_NAME = java.util.jar.JarFile.MANIFEST_NAME + ".preserved";

    protected static final String WEB_INF_PREFIX = "WEB-INF/";


    public static void explodeJar(File source, File destination) throws IOException {
        JarFile jarFile = null;
        String fileSystemName = null; // declared outside the try block so it's available in the catch block
        try {
            jarFile = new JarFile(source);
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry entry = e.nextElement();
                fileSystemName = entry.getName().replace('/', File.separatorChar);
                File out = new File(destination, fileSystemName);

                if (entry.isDirectory()) {
                    if (!out.exists() && !out.mkdirs()) {
                      throw new IOException("Unable to create directories " + out.getAbsolutePath());
                    }
                } else {
                    if (!out.getParentFile().exists()) {
                        out.getParentFile().mkdirs();
                    }
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        FileUtils.copy(is, out, entry.getSize());
                    }
                }
            }
        } catch(Throwable e) {
            /*
             *Use the logger here, even though we rethrow the exception.  In
             *at least some cases the caller does not propagate this exception
             *further, instead replacing it with a serializable
             *IASDeployException.  The added information is then lost.
             *By logging the exception here, we make sure the log file at least
             *displays as much as we know about the problem even though the
             *exception sent to the client may not.
             */
            String msg0 = localStrings.getString(
                    "enterprise.deployment.backend.error_expanding",
                    new Object[] {source.getAbsolutePath()});
            IOException ioe = new IOException(msg0);
            ioe.initCause(e);
            LogRecord lr = new LogRecord(Level.SEVERE, COULD_NOT_EXPAND_ENTRY);
            Object args[] = { fileSystemName, destination.getAbsolutePath() };
            lr.setParameters(args);
            lr.setThrown(ioe);
            deplLogger.log(lr);
            throw ioe;
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
        }
    }


    public static void explodeModule(Archive source, File directory, boolean preserveManifest)
    throws IOException, DeploymentException {

        File explodedManifest = null;
        File preservedManifestFromArchive = null;

        FileArchive target = new FileArchive();
        target.create(directory.toURI());

        explodeJar(new File(source.getURI()), directory);

        if (preserveManifest) {
            explodedManifest = new File(directory, java.util.jar.JarFile.MANIFEST_NAME);
            if (explodedManifest.exists()) {
                /* Rename the manifest so it can be restored later. */
                preservedManifestFromArchive = new File(directory, PRESERVED_MANIFEST_NAME);
                if ( ! explodedManifest.renameTo(preservedManifestFromArchive)) {
                    throw new RuntimeException(localStrings.getString(
                            "enterprise.deployment.backend.error_saving_manifest",
                            new Object[]
                    { explodedManifest.getAbsolutePath(),
                              preservedManifestFromArchive.getAbsolutePath()
                    } ) ) ;
                }
            }
        }
        // now explode all top level jar files and delete them.
        // this cannot be done before since the optionalPkgDependency
        // require access to the manifest file of each .jar file.
        for (Enumeration itr = source.entries();itr.hasMoreElements();) {
            String fileName = (String) itr.nextElement();


            // check for optional packages depencies
            // XXX : JEROME look if this is still done
            // resolveDependencies(new File(directory, fileName));

             /*
              *Expand the file only if it is a jar and only if it does not lie in WEB-INF/lib.
              */
            if (fileName.toLowerCase(Locale.US).endsWith(".jar") &&
                ( ! fileName.replace('\\', '/').toUpperCase(Locale.getDefault()).startsWith(WEB_INF_PREFIX)) ) {

                try {
                    File f = new File(directory, fileName);

                    ZipFile zip = new ZipFile(f, directory);
                    zip.explode();
                } catch(ZipFileException e) {
                    IOException ioe = new IOException(e.getMessage());
                    ioe.initCause(e);
                    throw ioe;
                }
            }
        }
         /*
          *If the archive's manifest was renamed to protect it from being overwritten by manifests from
          *jar files, then rename it back.  Delete an existing manifest file first if needed.
          */
        if (preservedManifestFromArchive != null) {
            if (explodedManifest.exists()) {
                if ( ! explodedManifest.delete()) {
                    throw new RuntimeException(localStrings.getString(
                            "enterprise.deployment.backend.error_deleting_manifest",
                            new Object []
                    { explodedManifest.getAbsolutePath(),
                              preservedManifestFromArchive.getAbsolutePath()
                    }
                    ) );
                }
            }

            if ( ! preservedManifestFromArchive.renameTo(explodedManifest)) {
                throw new RuntimeException(localStrings.getString(
                        "enterprise.deployment.backend.error_restoring_manifest",
                        new Object []
                { preservedManifestFromArchive.getAbsolutePath(),
                          explodedManifest.getAbsolutePath()
                }
                ) );
            }
        }

        source.close();
        target.close();
    }
}
