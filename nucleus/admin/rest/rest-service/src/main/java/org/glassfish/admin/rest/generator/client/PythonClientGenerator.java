/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.generator.client;

import org.glassfish.admin.rest.utils.Util;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigModel;

import com.sun.appserv.server.util.Version;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.glassfish.admin.rest.RestLogging;

public class PythonClientGenerator extends ClientGenerator {
    private File baseDirectory;
    private static String MSG_INSTALL = "To install the egg into your Python environment:  sudo easy_install " + ARTIFACT_NAME
            + "-VERSION-egg.zip";

    public PythonClientGenerator(ServiceLocator habitat) {
        super(habitat);
        baseDirectory = Util.createTempDirectory();
        messages.add(MSG_INSTALL.replace("VERSION", versionString));
    }

    @Override
    public Map<String, URI> getArtifact() {
        ZipOutputStream zip = null;
        Map<String, URI> artifacts = new HashMap<String, URI>();
        try {
            String ZIP_BASE_DIR = "glassfish-rest-client-VERSION".replace("VERSION", Version.getVersionNumber());
            String ZIP_GF_PACKAGE_DIR = ZIP_BASE_DIR + "/glassfish";
            String ZIP_REST_PACKAGE_DIR = ZIP_GF_PACKAGE_DIR + "/rest";

            File zipDir = Util.createTempDirectory();
            File zipFile = new File(zipDir, ARTIFACT_NAME + "-" + versionString + "-egg.zip");
            if (!zipFile.createNewFile()) {
                throw new RuntimeException("Unable to create new file"); //i18n
            }
            zipFile.deleteOnExit();
            zip = new ZipOutputStream(new FileOutputStream(zipFile));

            add(ZIP_GF_PACKAGE_DIR, "__init__.py", new ByteArrayInputStream("".getBytes()), zip);
            //add(ZIP_BASE_DIR, "PKG-INFO", new ByteArrayInputStream(getFileContents("PKG-INFO").getBytes()), zip);
            add(ZIP_BASE_DIR, "setup.py", new ByteArrayInputStream(getFileContents("setup.py").getBytes()), zip);
            addFileFromClasspath(ZIP_REST_PACKAGE_DIR, "__init__.py", zip);
            addFileFromClasspath(ZIP_REST_PACKAGE_DIR, "connection.py", zip);
            addFileFromClasspath(ZIP_REST_PACKAGE_DIR, "restclient.py", zip);
            addFileFromClasspath(ZIP_REST_PACKAGE_DIR, "restresponse.py", zip);
            addFileFromClasspath(ZIP_REST_PACKAGE_DIR, "restclientbase.py", zip);
            File[] files = baseDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    add(ZIP_REST_PACKAGE_DIR, file, zip);
                }
            }

            artifacts.put(zipFile.getName(), zipFile.toURI());
            Util.deleteDirectory(baseDirectory);
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.SEVERE, null, ex);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ex) {
                    RestLogging.restLogger.log(Level.SEVERE, null, ex);
                }
            }
        }

        return artifacts;
    }

    @Override
    public ClientClassWriter getClassWriter(ConfigModel model, String className, Class parent) {
        return new PythonClientClassWriter(model, className, parent, baseDirectory);
    }

    private String getFileContents(String fileName) {
        try (Scanner scanner = new Scanner(getClass().getClassLoader().getResourceAsStream("/client/python/" + fileName))) {
            String contents = scanner.useDelimiter("\\Z").next();
            return contents.replace("VERSION", Version.getVersionNumber());
        }
    }

    private void addFileFromClasspath(String targetDir, String fileName, ZipOutputStream zip) throws IOException {
        add(targetDir, fileName, getClass().getClassLoader().getResourceAsStream("/client/python/" + fileName), zip);
    }

    private void add(String dirInZip, String nameInZip, InputStream source, ZipOutputStream target) throws IOException {
        try {
            String sourcePath = dirInZip + "/" + nameInZip;

            ZipEntry entry = new ZipEntry(sourcePath);
            target.putNextEntry(entry);

            byte[] buffer = new byte[1024];
            while (true) {
                int count = source.read(buffer);
                if (count == -1) {
                    break;
                }
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        } finally {
            if (source != null) {
                source.close();
            }
        }
    }

    private void add(String dirInZip, File source, ZipOutputStream target) throws IOException {
        add(dirInZip, source.getName(), new BufferedInputStream(new FileInputStream(source)), target);
    }
}
