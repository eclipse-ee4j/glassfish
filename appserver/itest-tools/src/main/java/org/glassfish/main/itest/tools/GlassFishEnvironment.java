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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.glassfish.main.itest.tools.asadmin.Asadmin;
import org.glassfish.main.itest.tools.asadmin.StartServ;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ENGLISH;

/**
 * The GlassFish environment.
 * <p>
 * This class is here to manage GlassFish directory structure and runtime environment.
 */
public class GlassFishEnvironment {

    /**
     * True if the operating system is windows. Useful to decide if an executable file should have
     * suffix usual on the operating system.
     */
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(ENGLISH).contains("windows");

    private final Path glassFishDir;
    private final Path binDir;
    private final File asadminFile;
    private final File startServFile;

    private Path javaHome;
    private Path domainsDir;
    private String username;
    private File passwordFile;

    /**
     * @param productRoot <code>glassfish[n]</code> directory containing <code>glassfish</code> directory, must exist.
     * @param autoStop true to add shutdown hook automatically stopping the domain.
     * @throws IllegalArgumentException if the productRoot doesn't contain the glassfish directory.
     */
    public GlassFishEnvironment(Path productRoot, boolean autoStop) throws IllegalArgumentException {
        this.glassFishDir = productRoot.resolve("glassfish");
        if (!Files.isDirectory(productRoot)) {
            throw new IllegalArgumentException("The path is not an existing directory: " + this.glassFishDir);
        }
        this.binDir = glassFishDir.resolve("bin");
        this.asadminFile = findAsadmin(binDir);
        this.startServFile = findStartServ(binDir);
        withJavaHome(null);
        withDomainsDirectory(null);
        if (autoStop) {
            Thread hook = new Thread(() -> {
                getAsadmin().exec(30_000, "stop-domain", "--kill", "--force");
            });
            Runtime.getRuntime().addShutdownHook(hook);
        }
    }

    /**
     * Sets the JDK home directory.
     *
     * @param javaHome if null, the value is set from the system property <code>java.home</code>
     * @return this
     */
    public GlassFishEnvironment withJavaHome(@SuppressWarnings("hiding") Path javaHome) {
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
     * Sets credentials for the {@link Asadmin} command api.
     * Creates an internal temporary password file.
     *
     * @param username
     * @param password
     * @return this
     * @throws IllegalStateException if the temporary password file could not be created.
     */
    public GlassFishEnvironment withCredentials(
        @SuppressWarnings("hiding") String username,
        String password) throws IllegalStateException {
        this.username = username;
        try {
            Path file = Files.createTempFile("gf-pw", ".txt");
            Files.writeString(file, "AS_ADMIN_PASSWORD=" + password + "\n", UTF_8);
            this.passwordFile = file.toAbsolutePath().normalize().toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create a temporary password file.", e);
        }
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
     * @return <code>.../glassfish[n]/glassfish/bin</code>, never null.
     */
    public Path getBinDir() {
        return this.binDir;
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

    /**
     * @param domainName name of the domain directory and of the domain.
     * @return PID of the running domain instance or null.
     * @throws IllegalStateException if the pid file exists but cannot be read or parsed. That can
     *             happen when the domain is starting and writes to the file right now.
     */
    public Long loadPid(String domainName) throws IllegalStateException {
        final Path pidFile = getDomainConfigDirectory(domainName).resolve("pid");
        if (!Files.exists(pidFile)) {
            return null;
        }
        try {
            return Long.parseLong(Files.readString(pidFile, ISO_8859_1));
        } catch (final IOException e) {
            throw new IllegalStateException("Could not load the pid file " + pidFile, e);
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + this.glassFishDir + "]";
    }

    private static File findAsadmin(Path binDir) {
        final File file = binDir.resolve("asadmin.java").toFile();
        if (file.isFile()) {
            return file;
        }
        // Older versions don't have asadmin.java.
        return binDir.resolve(IS_WINDOWS ? "asadmin.bat" : "asadmin").toFile();
    }

    private static File findStartServ(Path binDir) {
        return binDir.resolve(IS_WINDOWS ? "startserv.bat" : "startserv").toFile();
    }
}
