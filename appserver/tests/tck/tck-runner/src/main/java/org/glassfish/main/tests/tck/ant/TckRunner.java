/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;

/**
 * @author David Matejcek
 */
public class TckRunner {

    private final TckConfiguration cfg;
    private File glassfishZip;

    public TckRunner(final TckConfiguration cfg) {
        this.cfg = cfg;
    }


    public void prepareWorkspace() {
        MavenResolverSystem resolver = Maven.configureResolver().withMavenCentralRepo(false).workOffline()
            .fromClassloaderResource("settings.xml");
        MavenFormatStage tckZip = resolver
            .resolve("org.glassfish.main.tests.tck:jakarta-ant-based-tck:zip:" + cfg.getTckVersion())
            .withoutTransitivity();
        try (ZipInputStream zis = new ZipInputStream(tckZip.asSingleInputStream())) {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    ZipEntry zipEntry = zis.getNextEntry();
                    if (zipEntry == null) {
                        break;
                    }
                    File newFile = newFile(cfg.getTargetDir(), zipEntry);
                    if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                }
            } finally {
                zis.closeEntry();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not open the file.", e);
        }
        cfg.getJakartaeetckCommand().toFile().setExecutable(true);

        this.glassfishZip = resolver
            .resolve("org.glassfish.main.distributions:glassfish:zip:" + cfg.getGlassFishVersion())
            .withoutTransitivity().asSingleFile();
    }


    public void start(Path modulePath) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
            this.cfg.getJakartaeetckCommand().toAbsolutePath() + " " + modulePath).inheritIO()
                .directory(cfg.getTargetDir());
        configureEnvironment(builder.environment());
        Process process = builder.start();
        if (process.waitFor() != 0) {
            throw new IllegalStateException("TCK execution ended with exit code " + process.exitValue());
        }
    }


    private void configureEnvironment(Map<String, String> env) {
        env.put("LC_ALL", "en_US.UTF-8");
        env.put("WORKSPACE", cfg.getTargetDir().getAbsolutePath());
        env.put("JAVA_HOME", cfg.getJdkDirectory().getAbsolutePath());
        env.put("JDK17_HOME", cfg.getJdkDirectory().getAbsolutePath());
        env.put("JDK", "JDK17");
        env.put("SMTP_PORT", "25");
        env.put("HARNESS_DEBUG", "true"); // TODO: configurable ... logging.
        env.put("AS_DEBUG", "false"); // TODO: configurable - logging.communicationWithServer.enabled=false

        env.put("PATH", cfg.getJdkDirectory().getAbsolutePath() + "/bin:" + cfg.getAntDirectory().getAbsolutePath()
            + "/bin/:/usr/bin");
        env.put("CTS_HOME", cfg.getTargetDir().getAbsolutePath());
        env.put("TS_HOME", cfg.getJakartaeeDir().getAbsolutePath());
        env.put("GF_BUNDLE_ZIP", this.glassfishZip.getAbsolutePath());
        env.put("GF_VI_BUNDLE_ZIP", this.glassfishZip.getAbsolutePath());
        env.put("GF_HOME_RI", cfg.getTargetDir().getAbsolutePath() + "/ri/glassfish7");
        env.put("GF_HOME_VI", cfg.getTargetDir().getAbsolutePath() + "/vi/glassfish7");
        env.put("DATABASE", "JavaDB");
        env.put("CLIENT_LOGGING_CFG", cfg.getTargetDir().getAbsolutePath() + "/test-classes/client-logging.properties");
    }


    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
