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

package org.glassfish.nucleus.test.tool;

import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.universal.process.ProcessManagerTimeoutException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author David Matejcek
 *
 */
public class NucleusTestUtils {
    private static final int DEFAULT_TIMEOUT_MSEC = 8 * 60 * 1000;
    public static final String ADMIN_USER = "admin";
    public static final String ADMIN_PASSWORD = "admintest";
    public static final File BASEDIR = getBasedir();
    public static final File GF_ROOT = getGlassFishRoot();
    private static final File PASSWORD_FILE_FOR_UPDATE = getPasswordFile("password_update.txt");
    private static final File PASSWORD_FILE = getPasswordFile("password.txt");

    static {
        final NadminReturn result = changePassword();
        if (result.returnValue) {
            System.out.println("Admin password changed.");
        } else {
            // probably changed by previous execution without maven clean
            System.out.println("Admin password NOT changed.");
        }
    }


    /**
     * Useful for a heuristic inside Eclipse and other environments.
     *
     * @return Absolute path to the glassfish directory.
     */
    private static File getBasedir() {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            File target = new File("target");
            if (target.exists()) {
                return target.getAbsoluteFile().getParentFile();
            }
            return new File(".").getAbsoluteFile().getParentFile();
        }
        return new File(basedir);
    }

    private static File getGlassFishRoot() {
        System.out.println("Using basedir: " + BASEDIR);
        return BASEDIR.toPath().resolve(Path.of("target", "glassfish6", "glassfish")).toFile();
    }

    private static File getPasswordFile(final String filename) {
        try {
            URL url = NucleusTestUtils.class.getResource("/" + filename);
            assertNotNull(url, filename + " not found");
            return Path.of(url.toURI()).toFile().getAbsoluteFile();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private NucleusTestUtils() {
        // All methods are static, do not allow an object to be created.
    }


    public static File getNucleusRoot() {
        return GF_ROOT;
    }


    public static void deleteSubpaths(final Path path) {
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot delete path recursively: " + path, e);
        }
    }


    /**
     * This will delete the jobs.xml file
     */
    public static void deleteJobsFile() {
        Path path = GF_ROOT.toPath().resolve(Paths.get("domains", "domain1", "config", "jobs.xml"));
        System.out.println("Deleting.. " + path);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    /**
     * osgi-cache workaround
     */
    public static void deleteOsgiDirectory() {
        Path osgiCacheDir = GF_ROOT.toPath().resolve(Paths.get("domains", "domain1", "osgi-cache"));
        try {
            Files.list(osgiCacheDir).forEach(NucleusTestUtils::deleteSubpaths);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static boolean nadmin(final String... args) {
        return nadmin(DEFAULT_TIMEOUT_MSEC, args);
    }

    /**
     * Runs the command with the args given
     *
     * @param args
     *
     * @return true if successful
     */
    public static boolean nadmin(int timeout, final String... args) {
        return nadminWithOutput(timeout, args).returnValue;
    }

    /**
     * Runs the command with the args given
     * Returns the precious output strings for further processing.
     *
     * @param args
     *
     * @return true if successful
     */
    public static NadminReturn nadminWithOutput(final String... args) {
        return nadminWithOutput(DEFAULT_TIMEOUT_MSEC, args);
    }


    public static NadminReturn nadminWithOutput(final int timeout, final String... args) {
        File cmd = new File(GF_ROOT, isWindows() ? "bin/asadmin.bat" : "bin/asadmin");
        return cmdWithOutput(cmd, timeout, args);
    }


    public static NadminReturn nadminDetachWithOutput(final String... args) {
        File cmd = new File(GF_ROOT, isWindows() ? "bin/asadmin.bat" : "bin/asadmin");
        return cmdDetachWithOutput(cmd, DEFAULT_TIMEOUT_MSEC, args);
    }


    private static NadminReturn changePassword() {
        File cmd = new File(GF_ROOT, isWindows() ? "bin/asadmin.bat" : "bin/asadmin");
        return cmdWithOutput(cmd, PASSWORD_FILE_FOR_UPDATE, DEFAULT_TIMEOUT_MSEC, "change-admin-password");
    }

    public static NadminReturn cmdWithOutput(final File cmd, final int timeout, final String... args) {
        return cmdWithOutput(cmd, PASSWORD_FILE, timeout, args);
    }

    public static NadminReturn cmdWithOutput(final File cmd, final File passwordFile, final int timeout, final String... args) {
        List<String> command = new ArrayList<>();
        command.add(cmd.getAbsolutePath());
        command.add("--user");
        command.add(ADMIN_USER);
        command.add("--passwordfile");
        command.add(passwordFile.getAbsolutePath());
        command.addAll(Arrays.asList(args));

        ProcessManager pm = new ProcessManager(command);

        // the tests may be running unattended -- don't wait forever!
        pm.setTimeoutMsec(timeout);
        pm.setEcho(false);

        int exit;
        String myErr = "";
        try {
            exit = pm.execute();
        } catch (ProcessManagerTimeoutException tex) {
            myErr = "\nProcessManagerTimeoutException: command timed out after " + timeout + " ms.";
            exit = 1;
        } catch (ProcessManagerException ex) {
            ex.printStackTrace();
            myErr = "\n" + ex.getMessage();
            exit = 1;
        }

        NadminReturn ret = new NadminReturn(exit, pm.getStdout(), pm.getStderr() + myErr, args[0]);
        write(ret.outAndErr);
        return ret;
    }


    public static NadminReturn cmdDetachWithOutput(final File cmd, final int timeout, final String... args) {
        List<String> command = new ArrayList<>();
        command.add(cmd.getAbsolutePath());
        command.add("--user");
        command.add(ADMIN_USER);
        command.add("--passwordfile");
        command.add(PASSWORD_FILE.getAbsolutePath());
        command.add("--detach");
        command.addAll(Arrays.asList(args));

        ProcessManager pm = new ProcessManager(command);

        // the tests may be running unattended -- don't wait forever!
        pm.setTimeoutMsec(timeout);
        pm.setEcho(false);

        int exit;
        String myErr = "";
        try {
            exit = pm.execute();
        } catch (ProcessManagerTimeoutException tex) {
            myErr = "\nProcessManagerTimeoutException: command timed out after " + timeout + " ms.";
            exit = 1;
        } catch (ProcessManagerException ex) {
            exit = 1;
        }

        NadminReturn ret = new NadminReturn(exit, pm.getStdout(), pm.getStderr() + myErr, args[0]);
        write(ret.outAndErr);
        return ret;
    }

    private static boolean validResults(String text, String... invalidResults) {
        for (String result : invalidResults) {
            if (text.contains(result)) {
                return false;
            }
        }
        return true;
    }

    private static void write(final String text) {
        if (!text.isEmpty()) {
            System.out.print(text);
        }
    }


    protected static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
    }


    /**
     * This methods opens a connection to the given URL and
     * returns the string that is returned from that URL.  This
     * is useful for simple servlet retrieval
     *
     * @param urlstr The URL to connect to
     * @return The string returned from that URL, or empty
     * string if there was a problem contacting the URL
     */
    public static String getURL(String urlstr) {
        URLConnection urlc = openConnection(urlstr);
        try (
            BufferedReader ir = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "ISO-8859-1"));
            StringWriter ow = new StringWriter();
        ) {
            String line;
            while ((line = ir.readLine()) != null) {
                ow.write(line);
                ow.write("\n");
            }
            return ow.getBuffer().toString();
        } catch (IOException ex) {
            System.out.println("unable to fetch URL:" + urlstr + ", reason: " + ex.getMessage());
            return "";
        }
    }

    private static URLConnection openConnection(String url) {
        try {
            return new URL(url).openConnection();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }


    public static class NadminReturn {

        public boolean returnValue;
        public String out;
        public String err;
        public String outAndErr;

        NadminReturn(int exit, String out, String err, String cmd) {
            this.returnValue = exit == 0 && validResults(out,
                String.format("Command %s failed.", cmd));
            this.out = out;
            this.err = err;
            this.outAndErr = this.out + this.err;
        }
    }

}
