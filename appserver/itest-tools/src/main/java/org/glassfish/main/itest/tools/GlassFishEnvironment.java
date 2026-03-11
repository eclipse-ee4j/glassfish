/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.itest.tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.StartServ;

/**
 * The GlassFish environment.
 * <p>
 * This class is here to manage GlassFish directory structure and runtime environment.
 */
public class GlassFishEnvironment {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").contains("windows");

    private final Path glassFishDir;
    private final File asadminFile;
    private final File startServFile;

    private Path javaHome;
    private Path domainsDir;
    private String username;
    private File passwordFile;


    /**
     * @param productRoot <code>glassfish[n]</code> directory containing <code>glassfish</code> directory, must exist.
     * @param autoStop true to add shutdown hook automatically stopping the domain.
     */
    public GlassFishEnvironment(Path productRoot, boolean autoStop) {
        this.glassFishDir = productRoot.resolve("glassfish");
        if (!Files.isDirectory(productRoot)) {
            throw new IllegalArgumentException("The path is not an existing directory: " + this.glassFishDir);
        }
        this.asadminFile = findAsadmin(glassFishDir);
        this.startServFile = findStartServ(glassFishDir);
        withJavaHome(null);
        withDomainsDirectory(null);
        if (autoStop) {
            Thread hook = new Thread(() -> {
                getAsadmin().exec(30_000, "stop-domain", "--kill", "--force");
            });
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }


    public GlassFishEnvironment withJavaHome(Path javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    /**
     * Sets credentials for the {@link Asadmin} command api.
     *
     * @param username
     * @param passwordFile
     * @return this
     */
    public GlassFishEnvironment withCredentials(
        @SuppressWarnings("hiding") String username,
        @SuppressWarnings("hiding") File passwordFile) {
        this.username = username;
        this.passwordFile = passwordFile;
        return this;
    }

    /**
     * Sets the parent directory of domains
     *
     * @param domainsDirPath null uses default <code>domains</code> subdirectory
     * @return this
     */
    public GlassFishEnvironment withDomainsDirectory(Path domainsDirPath) {
        this.domainsDir = domainsDirPath == null ? glassFishDir.resolve("domains") : domainsDirPath;
        return this;
    }

    /**
     * @return <code>.../glassfish[n]/glassfish</code>, never null.
     */
    public Path getGlassFishDir() {
        return this.glassFishDir;
    }

    /**
     * @return <code>.../glassfish[n]/glassfish/domains</code> by default, never null.
     */
    public Path getDomainsDir() {
        return this.domainsDir;
    }

    /**
     * @return {@link Asadmin} command api for tests.
     */
    public Asadmin getAsadmin() {
        return getAsadmin(true);
    }

    /**
     * @param terse true means suitable and minimized for easy parsing.
     * @return {@link Asadmin} command api for tests.
     */
    public Asadmin getAsadmin(boolean terse) {
        return new Asadmin(asadminFile, username, passwordFile, terse).withJavaHome(javaHome);
    }

    /**
     * @return {@link StartServ} command api for tests.
     */
    public StartServ getStartServ() {
        return new StartServ(startServFile);
    }

    /**
     * @param domainName name of the domain directory and of the domain.
     * @return absolute path
     */
    public Path getDomainDirectory(String domainName) {
        return getDomainsDir().resolve(domainName);
    }

    /**
     * @param domainName name of the domain directory and of the domain.
     * @return absolute path
     */
    public Path getDomainConfigDirectory(String domainName) {
        return getDomainDirectory(domainName).resolve("config");
    }

    @Override
    public String toString() {
        return super.toString() + "[" + this.glassFishDir + "]";
    }

    private static File findAsadmin(Path glassFishDir) {
        final File file = glassFishDir.resolve(Path.of("bin", "asadmin.java")).toFile();
        if (file.isFile()) {
            return file;
        }
        return glassFishDir.resolve(Path.of("bin", IS_WINDOWS ? "asadmin.bat" : "asadmin")).toFile();
    }

    private static File findStartServ(Path glassFishDir) {
        return glassFishDir.resolve(Path.of("bin", IS_WINDOWS ? "startserv.bat" : "startserv")).toFile();
    }
}
