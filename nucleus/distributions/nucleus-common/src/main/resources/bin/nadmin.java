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

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class nadmin {

    private static final boolean AS_TRACE = "true".equals(System.getenv("AS_TRACE"));
    private static final boolean INHERIT_ENV = "false".equals(System.getenv("AS_INHERIT_ENVIRONMENT"));
    private static final boolean USE_SCRIPT = "true".equals(System.getenv("AS_USE_NATIVE_SCRIPT"));
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final Path MY_JAVA_FILE = getMyJavaFile("jdk.launcher.sourcefile");
    private static final Path MY_BIN_DIR = MY_JAVA_FILE.getParent();

    private static final Path PATH_FROM_BIN_TO_GLASSFISH = Path.of("..");
    private static final ASPaths AS_PATHS = new ASPaths(MY_BIN_DIR, PATH_FROM_BIN_TO_GLASSFISH);
    private static final String ASADMIN_CLASS = "com.sun.enterprise.admin.cli.AdminMain";

    private static Path getMyJavaFile(String systemProperty) {
        String sourceFile = System.getProperty(systemProperty);
        if (sourceFile == null) {
            System.out.println("The '" + systemProperty
                + "' property is not set, you probably did not execute this program running java "
                + asadmin.class.getSimpleName() + ".java");
            System.exit(1);
        }
        return Path.of(sourceFile).toAbsolutePath();
    }

    public static void main(String... args) throws Exception {
        stderr(() -> "System.properties:\n" + System.getProperties());
        stderr(() -> "System.env:\n" + System.getenv());
        List<String> cmd = getCommand(args);
        stderr(() -> "Executing: \n" + cmd);
        ProcessBuilder processBuilder = new ProcessBuilder(cmd).directory(null).inheritIO();
        prepareEnvironment(processBuilder.environment());
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
        stderr(() -> "Finishing " + MY_JAVA_FILE.getFileName() + ".java with exit code " + exitCode);
        System.exit(exitCode);
    }


    private static List<String> getCommand(String... args) {
        final List<String> cmd;
        if (USE_SCRIPT) {
            cmd = getCommandScriptPrefix();
        } else {
            cmd = getCommandJavaPrefix();
        }
        for (String arg : args) {
            cmd.add(quote(arg));
        }
        return cmd;
    }


    private static List<String> getCommandScriptPrefix() {
        List<String> cmd = new ArrayList<>();
        cmd.add(MY_BIN_DIR.resolve(IS_WINDOWS ? "asadmin.bat" : "asadmin").toString());
        return cmd;
    }

    private static List<String> getCommandJavaPrefix() {
        List<String> cmd = new ArrayList<>();
        cmd.add(quote(Path.of(JAVA_HOME).resolve(Path.of("bin", "java")).toString()));
        cmd.add("-Djava.util.logging.manager=org.glassfish.main.jul.GlassFishLogManager");
        cmd.add("--module-path");
        cmd.add(quote(AS_PATHS.asLib.resolve("bootstrap").toString()));
        cmd.add("--add-modules");
        cmd.add("ALL-MODULE-PATH");
        cmd.add("--class-path");
        cmd.add(quote(AS_PATHS.asadminClassPath));
        cmd.add(ASADMIN_CLASS);
        return cmd;
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
        env.computeIfAbsent("AS_DERBY_INSTALL", k -> AS_PATHS.asRoot.resolve("javadb").toString());
        env.computeIfAbsent("AS_IMQ_LIB", k -> AS_PATHS.asMQ.resolve("lib").toString());
        env.computeIfAbsent("AS_IMQ_BIN", k -> AS_PATHS.asMQ.resolve("bin").toString());
        env.computeIfAbsent("AS_CONFIG", k -> AS_PATHS.asInstall.resolve("config").toString());
        env.computeIfAbsent("AS_DEF_DOMAINS_PATH", k -> AS_PATHS.asInstall.resolve("domains").toString());
        env.computeIfAbsent("AS_DEF_NODES_PATH", k -> AS_PATHS.asInstall.resolve("nodes").toString());
        env.putIfAbsent("ASADMIN_CLASSPATH", AS_PATHS.asadminClassPath);
    }

    private static String quote(String input) {
        if (IS_WINDOWS) {
            return "\"" + input + "\"";
        }
        return input;
    }

    private static void stderr(Supplier<String> log) {
        if (AS_TRACE) {
            System.err.println(log.get());
        }
    }

    private static final class ASPaths {
        final Path asInstall;
        final Path asRoot;
        final Path asModules;
        final Path asLib;
        final Path asMQ;
        final String asadminClassPath;

        ASPaths(Path binDirectory, Path relativeFromBinToInstall) {
            this.asInstall = binDirectory.resolve(relativeFromBinToInstall).normalize();
            this.asRoot = asInstall.getParent();
            this.asModules = asInstall.resolve("modules");
            this.asLib = asInstall.resolve("lib");
            this.asMQ = asRoot.resolve("mq");
            this.asadminClassPath = getAsadminClassPath();
        }

        private String getAsadminClassPath() {
            // This capacity should be enough to avoid resizes.
            StringBuilder cp = new StringBuilder(8192);
            cp.append(File.pathSeparatorChar).append(asInstall.resolve("admin-cli.jar"));
            cp.append(File.pathSeparatorChar).append(asLib.resolve("asadmin")).append(File.separatorChar).append('*');
            cp.append(File.pathSeparatorChar).append(asModules.resolve("admin-util.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("backup.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("cluster-common.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("cluster-ssh.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("config-api.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("config-types.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("common-util.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("glassfish-api.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("hk2.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("hk2-config-generator.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("internal-api.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("jackson-core.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("jakarta.activation-api.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("jakarta.validation-api.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("jakarta.xml.bind-api.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("jaxb-osgi.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("jettison.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("jsch.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("launcher.jar"));
            cp.append(File.pathSeparatorChar).append(asModules.resolve("mimepull.jar"));
            return cp.toString();
        }
    }
}
