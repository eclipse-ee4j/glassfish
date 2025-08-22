/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

/**
 * Tools for obtaining readable information from the {@link org.osgi.framework.BundleException}
 */
public class FelixPrettyPrinter {

    private static final Pattern BUNDLE_PATTERN = Pattern.compile("\\[(\\d+)\\]", Pattern.MULTILINE);

    public static void main(String[] args) {
        System.out.println(prettyPrintExceptionMessage("Unable to resolve org.glassfish.main.concurrent.impl [138](R 138.0): missing requirement [org.glassfish.main.concurrent.impl [138](R 138.0)] osgi.wiring.package; (&(osgi.wiring.package=org.glassfish.enterprise.concurrent.spi)(version>=2.0.0)(!(version>=3.0.0))) [caused by: Unable to resolve org.glassfish.jakarta.enterprise.concurrent [190](R 190.0): missing requirement [org.glassfish.jakarta.enterprise.concurrent [190](R 190.0)] osgi.wiring.package; (&(osgi.wiring.package=jakarta.annotation)(version>=2.1.0)(!(version>=3.0.0)))] Unresolved requirements: [[org.glassfish.main.concurrent.impl [138](R 138.0)] osgi.wiring.package; (&(osgi.wiring.package=org.glassfish.enterprise.concurrent.spi)(version>=2.0.0)(!(version>=3.0.0)))]"));
    }

    /**
     * Prints exception messages from Felix bundle classloading in a more human readable way.
     *
     * @param message - error message from the exception
     * @return multiline human readable string
     */
    public static String prettyPrintExceptionMessage(final String message) {
        StringBuilder messageBuilder = new StringBuilder(256);
        try {
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
                    int indexPackage = message.indexOf("osgi.wiring.package; ", index);
                    int indexHost = message.indexOf("osgi.wiring.host; ", index);

                    boolean isPackage;
                    if (indexHost == -1 || indexPackage < indexHost) {
                        index = indexPackage;
                        isPackage = true;
                    } else {
                        index = indexHost;
                        isPackage = false;
                    }

                    if (index >= 0) {

                        indent++;

                        if (isPackage) {

                            // Remainder of input now looks like this:

                            // osgi.wiring.package; (&(osgi.wiring.package=org.glassfish.grizzly)(version>=2.4.0)(!(version>=3.0.0)))

                            // Skip over "osgi.wiring.package; ", we're always searching for this so
                            // no need to print it.
                            index += "osgi.wiring.package; ".length();

                            // Now extracting this:
                            // "(&(osgi.wiring.package=org.glassfish.grizzly)(version>=2.4.0)(!(version>=3.0.0)))"
                            index2 = message.indexOf(" ", index);

                            String packageAndVersion = null;
                            if (index2 != -1) {
                                packageAndVersion = message.substring(index, index2);
                            } else {
                                packageAndVersion = message.substring(index);
                            }

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
                                packageAndVersion = packageAndVersion.substring(0, packageAndVersion.length() - 1);
                            }

                            printLn(messageBuilder, indent, packageAndVersion);
                        } else {

                            // Remainder of input now looks like this:

                            // osgi.wiring.host; (&(osgi.wiring.host=org.hibernate.validator)(bundle-version>=0.0.0)

                            // Skip over "osgi.wiring.host; ", we're already searching for this so
                            // no need to print it.
                            index += "osgi.wiring.host; ".length();

                            index2 = message.indexOf("]", index);

                            String remainder = null;
                            if (index2 != -1) {
                                remainder = message.substring(index, index2);
                            } else {
                                remainder = message.substring(index);
                            }

                            printLn(messageBuilder, indent, remainder);
                        }

                        // If there's a "caused by:", print it and increase the indent
                        index = message.indexOf("caused by: ", index2);
                        if (index >= 0) {

                            printLn(messageBuilder, indent, "caused by:");

                            indent++;
                            index += "caused by: ".length();
                        }

                    }
                }

                if (index2 == -1) {
                    index = -1;
                } else {
                    index = index2;
                    index = message.indexOf("Unable to resolve", index);
                }
            }
            return messageBuilder.toString();
        } catch (Exception e) {
            // Usually we are processing another exception - if we failed, better return original.
            return message;
        }
    }


    /**
     * @param message - error message from the exception
     * @return list of bundle ids (are in square brackets in the message)
     */
    public static List<Integer> findBundleIds(final String message) {
        if (message == null || message.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Integer> bundleIds = new LinkedHashSet<>();
        Matcher bundlePattern = BUNDLE_PATTERN.matcher(message);
        while (bundlePattern.find()) {
            String number = bundlePattern.group(1);
            bundleIds.add(Integer.valueOf(number));
        }
        return new ArrayList<>(bundleIds);
    }


    private static void printLn(StringBuilder messageBuilder, int indent, String message) {
        for (int i = 0; i < (indent * 4); i++) {
            messageBuilder.append(" ");
        }
        messageBuilder.append(message.trim()).append("\n");
    }
}
