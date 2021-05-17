/*
 * Copyright (c) 2008, 2021 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FelixPrettyPrinter {

    /**
     * Prints exception messages from Felix bundle classloading in a more human readable way.
     *
     * @param message
     * @return
     */
    public static String prettyPrintExceptionMessage(String message) {
        StringBuilder messageBuilder = new StringBuilder();
        int index = message.indexOf("Unable to resolve");
        int indent = 0;
        while (index >= 0) {
            printLn(messageBuilder, indent, "Unable to resolve");
            index += "Unable to resolve".length();

            int index2 = message.indexOf("missing requirement", index);
            if (index2 >= 0) {

                indent++;

                // Module name would be e.g.
                // org.glassfish.server.internal.batch.glassfish-batch-connector [103](R 103.0):
                String module = message.substring(index, index2);

                // Remove the duplicate number
                if (module.contains("(R")) {
                    module = module.substring(0, module.indexOf("(R"));
                }

                printLn(messageBuilder, indent, module);
                printLn(messageBuilder, indent, "missing requirement");

                index = index2 + "missing requirement".length();

                // In GlassFish and in a classloader the search is always for package, so we can
                // use that as a delimiter here
                index = message.indexOf("osgi.wiring.package; ", index);
                if (index >= 0) {

                    indent++;

                    // Remainder of input now looks like this:

                    // osgi.wiring.package; (&(osgi.wiring.package=org.glassfish.grizzly)(version>=2.4.0)(!(version>=3.0.0)))

                    // Skip over "osgi.wiring.package; ", we're always searching for this so
                    // no need to print it.
                    index += "osgi.wiring.package; ".length();

                    // Now extracting this:
                    // "(&(osgi.wiring.package=org.glassfish.grizzly)(version>=2.4.0)(!(version>=3.0.0)))"
                    index2 = message.indexOf(" ", index);

                    String packageAndVersion = message.substring(index, index2);

                    // Make it a little less "cramped"
                    // "(&(package=org.glassfish.grizzly) (version>=2.4.0) (!(version>=3.0.0)))"
                    packageAndVersion = packageAndVersion.replace("osgi.wiring.package", "package");
                    packageAndVersion = packageAndVersion.replace(")(", ") (");
                    packageAndVersion = packageAndVersion.replace("=", " = ");
                    packageAndVersion = packageAndVersion.replace("> =", " >=");
                    packageAndVersion = packageAndVersion.replace("< =", " <=");

                    // Remove outer braces
                    // "&(package=org.glassfish.grizzly) (version>=2.4.0) (!(version>=3.0.0))"
                    if (packageAndVersion.startsWith("(")) {
                        packageAndVersion = packageAndVersion.substring(1);
                    }
                    if (packageAndVersion.endsWith(")")) {
                        packageAndVersion = packageAndVersion.substring(0, packageAndVersion.length()-1);
                    }

                    printLn(messageBuilder, indent, packageAndVersion);

                    // If there's a "caused by:", print it and increase the indent
                    index = message.indexOf("caused by: ", index2);
                    if (index >= 0) {

                        printLn(messageBuilder, indent, "caused by:");

                        indent++;
                        index += "caused by: ".length();
                    }
                }

            }

            index = index2;

            index = message.indexOf("Unable to resolve", index);
        }

        return messageBuilder.toString();
    }

    private static Pattern BUNDLE_PATTERN = Pattern.compile("\\[(\\d+)\\]", Pattern.MULTILINE);

    public static List<Integer> findBundleIds(String message) {
        if (message == null ||  message.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> bundleIds = new LinkedHashSet<>();

        Matcher bundlePattern = BUNDLE_PATTERN.matcher(message);
        while (bundlePattern.find()) {
            String number = bundlePattern.group(1);

            bundleIds.add(Integer.valueOf(number));
        }

        return new ArrayList<Integer>(bundleIds);
    }

    private static void printLn(StringBuilder messageBuilder, int indent, String message) {
        for (int i=0; i<(indent * 4); i++) {
            messageBuilder.append(" ");
        }

        messageBuilder.append(message.trim())
                      .append("\n");
    }

    public static void main(String[] args) {
        String test = "Caused by: org.osgi.framework.BundleException: Unable to resolve org.glassfish.server.internal.batch.glassfish-batch-connector [103](R 103.0): missing requirement [org.glassfish.server.internal.batch.glassfish-batch-connector [103](R 103.0)] osgi.wiring.package; (osgi.wiring.package=com.ibm.jbatch.spi) [caused by: Unable to resolve org.glassfish.server.internal.batch.payara-jbatch [325](R 325.0): missing requirement [org.glassfish.server.internal.batch.payara-jbatch [325](R 325.0)] osgi.wiring.package; (osgi.wiring.package=org.glassfish.weld) [caused by: Unable to resolve org.glassfish.server.internal.web.weld-integration [395](R 395.0): missing requirement [org.glassfish.server.internal.web.weld-integration [395](R 395.0)] osgi.wiring.package; (&(osgi.wiring.package=com.sun.faces.spi)(version>=2.3.0)(!(version>=3.0.0)))]] Unresolved requirements: [[org.glassfish.server.internal.batch.glassfish-batch-connector [103](R 103.0)] osgi.wiring.package; (osgi.wiring.package=com.ibm.jbatch.spi)]";

        String test1 = prettyPrintExceptionMessage(test);

        List<Integer> ids = findBundleIds(test1);

        String test2 = prettyPrintExceptionMessage(" (java.lang.String) Unable to resolve org.glassfish.jersey.containers.jersey-container-grizzly2-http [142](R 142.0): missing requirement [org.glassfish.jersey.containers.jersey-container-grizzly2-http [142](R 142.0)] osgi.wiring.package; (&(osgi.wiring.package=org.glassfish.grizzly)(version>=2.4.0)(!(version>=3.0.0))) Unresolved requirements: [[org.glassfish.jersey.containers.jersey-container-grizzly2-http [142](R 142.0)] osgi.wiring.package; (&(osgi.wiring.package=org.glassfish.grizzly)(version>=2.4.0)(!(version>=3.0.0)))]");

        System.out.println(test1);
        System.out.println(ids.toString());
        System.out.println("\n\n");
        System.out.println(test2);
    }

}
