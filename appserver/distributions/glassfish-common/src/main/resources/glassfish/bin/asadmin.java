/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.installroot.bin.asadmin;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class asadmin {

    private static final String SYS_LAUNCHERFILE = "jdk.launcher.sourcefile";

    private static final boolean AS_TRACE = "true".equals(System.getenv("AS_TRACE"));
    private static final boolean INHERIT_ENV = "false".equals(System.getenv("AS_INHERIT_ENVIRONMENT"));
    private static final boolean USE_SCRIPT = "true".equals(System.getenv("AS_USE_NATIVE_SCRIPT"));
    private static final String JAVA_HOME = System.getProperty("java.home");

    private static final Path MY_JAVA_FILE = getMyJavaFile();
    // This is the only difference between two asadmin.java files!
    private static final Path PATH_FROM_BIN_TO_GLASSFISH = Path.of("..");
    private static final Path AS_INSTALL = MY_JAVA_FILE.getParent().resolve(PATH_FROM_BIN_TO_GLASSFISH).normalize();
    private static final Path AS_LIB = AS_INSTALL.resolve("lib");
    private static final Path AS_MODULES = AS_INSTALL.resolve("modules");
    private static final String ASADMIN_CLASSPATH = getAsadminClassPath();
    private static final Path AS_ROOT = AS_INSTALL.getParent();
    private static final Path AS_MQ = AS_ROOT.resolve("mq");

    public static void main(String... args) throws Exception {
        stderr(() -> "System.properties:\n" + System.getProperties());
        stderr(() -> "System.env:\n" + System.getenv());
        List<String> cmd = getCommandLinePrefix();
        for (String arg : args) {
            cmd.add(arg);
        }
        stderr(() -> "Executing: \n" + cmd);
        ProcessBuilder processBuilder = new ProcessBuilder(cmd).directory(null).inheritIO();
        prepareEnvironment(processBuilder.environment());
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        stderr(() -> "Finishing " + MY_JAVA_FILE.getFileName() + ".java with exit code " + exitCode);
        System.exit(exitCode);
    }


    private static List<String> getCommandLinePrefix() {
        List<String> args = new ArrayList<>();
        if (USE_SCRIPT) {
            args.add(getScriptPath().toString());
            return args;
        }
        args.add(Path.of(JAVA_HOME).resolve(Path.of("bin", "java")).toString());
        args.add("-Djava.util.logging.manager=org.glassfish.main.jul.GlassFishLogManager");
        args.add("--module-path");
        args.add(AS_LIB.resolve("bootstrap").toString());
        args.add("--add-modules");
        args.add("ALL-MODULE-PATH");
        args.add("--class-path");
        args.add(ASADMIN_CLASSPATH);
        args.add("org.glassfish.admin.cli.AsadminMain");
        return args;
    }


    private static Path getScriptPath() {
        String fileName = isWindows() ? "asadmin.bat" : "asadmin";
        return MY_JAVA_FILE.getParent().resolve(fileName);
    }


    private static boolean isWindows() {
        return System.getProperty("os.name").contains("windows");
    }


    private static Path getMyJavaFile() {
        String sourceFile = System.getProperty(SYS_LAUNCHERFILE);
        if (sourceFile == null) {
            System.out.println("The '" + SYS_LAUNCHERFILE
                + "' property is not set, you probably did not execute this program running java "
                + MY_JAVA_FILE.getFileName() + ", right?");
            System.exit(1);
        }
        return Path.of(sourceFile).toAbsolutePath();
    }


    private static void prepareEnvironment(final Map<String, String> env) {
        if (INHERIT_ENV) {
            for (Entry<String, String> entry : System.getenv().entrySet()) {
                env.put(entry.getKey(), entry.getValue());
            }
        }
        // allways override
        env.put("JAVA_HOME", JAVA_HOME);
        env.put("AS_JAVA", JAVA_HOME);
        // always respect inherited value
        env.putIfAbsent("AS_DERBY_INSTALL", AS_ROOT.resolve("javadb").toString());
        env.putIfAbsent("AS_IMQ_LIB", AS_MQ.resolve("lib").toString());
        env.putIfAbsent("AS_IMQ_BIN", AS_MQ.resolve("bin").toString());
        env.putIfAbsent("AS_CONFIG", AS_INSTALL.resolve("config").toString());
        env.putIfAbsent("AS_DEF_DOMAINS_PATH", AS_INSTALL.resolve("domains").toString());
        env.putIfAbsent("AS_DEF_NODES_PATH", AS_INSTALL.resolve("nodes").toString());
        env.putIfAbsent("ASADMIN_CLASSPATH", ASADMIN_CLASSPATH);
    }


    private static String getAsadminClassPath() {
        StringBuilder cp = new StringBuilder(1024);
        cp.append(AS_INSTALL.resolve("appserver-cli.jar"));
        cp.append(File.pathSeparatorChar).append(AS_INSTALL.resolve("admin-cli.jar"));
        cp.append(File.pathSeparatorChar).append(AS_LIB.resolve("asadmin")).append(File.separatorChar).append('*');
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("admin-util.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("backup.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("cluster-common.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("cluster-ssh.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("config-api.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("config-types.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("common-util.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("glassfish-api.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("hk2.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("hk2-config-generator.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("internal-api.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("jackson-core.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("jakarta.activation-api.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("jakarta.validation-api.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("jakarta.xml.bind-api.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("jaxb-osgi.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("jettison.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("jsch.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("launcher.jar"));
        cp.append(File.pathSeparatorChar).append(AS_MODULES.resolve("mimepull.jar"));
        return cp.toString();
    }


    private static void stderr(Supplier<String> log) {
        if (AS_TRACE) {
            System.err.println(log.get());
        }
    }
}
