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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class Asadmin {

    private static final boolean INHERIT_ENV = "false".equals(System.getenv("AS_INHERIT_ENVIRONMENT"));

    public static void main(String... args) throws Exception {
        System.err.println("System.properties:\n" + System.getProperties());
        System.err.println("System.env:\n" + System.getenv());
        String[] envp = prepareEnvironment();
        String[] newArgs = new String[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = getAsadminScriptPath().toString();
        Process asadmin = Runtime.getRuntime().exec(newArgs, envp);
        asadmin.waitFor();
    }


    private static Path getAsadminScriptPath() {
        String fileName = isWindows() ? "asadmin.bat" : "asadmin";
        Path currentExecutable = getCurrentProcessExecutable();
        if (currentExecutable == null) {
            return Path.of(".", fileName).toAbsolutePath();
        }
        return currentExecutable.resolve(fileName);
    }


    private static Path getCurrentProcessExecutable() {
        String sourceFile = System.getProperty("jdk.launcher.sourcefile");
        return sourceFile == null ? null : Path.of(sourceFile).getParent();
    }


    private static boolean isWindows() {
        return System.getProperty("os.name").contains("windows");
    }


    private static String[] prepareEnvironment() {
        String javaHome = System.getProperty("java.home");
        String javaHomeEnv = "JAVA_HOME=" + javaHome;
        String asJavaEnv = "AS_JAVA=" + javaHome;
        if (!INHERIT_ENV) {
            return new String[] {javaHomeEnv, asJavaEnv};
        }
        List<String> envp = new ArrayList<>();
        for (Entry<String, String> entry : System.getenv().entrySet()) {
            envp.add(entry.getKey() + ':' + entry.getValue());
        }
        envp.add(javaHomeEnv);
        envp.add(asJavaEnv);
        return envp.toArray(String[]::new);
    }
}
