/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.main.boot.osgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Capability;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

/**
 * Tools for obtaining readable information from the {@link BundleException}
 */
public class FelixPrettyUtils {

    private static final Pattern BUNDLE_PATTERN = Pattern.compile("\\[(\\d+)\\]", Pattern.MULTILINE);


    public static void addBundleInformation(BundleContext context, Set<Long> bundleIDs, StringBuilder bundleBuilder) {
        if (!bundleIDs.isEmpty()) {
            for (Long bundleId : bundleIDs) {
                Bundle bundle = context.getBundle(bundleId);
                if (bundle != null) {
                    bundleBuilder.append('[').append(bundleId).append("] \n");
                    bundleBuilder.append("jar = ").append(bundle.getLocation());
                    tryAddPomProperties(bundle, bundleBuilder);
                    bundleBuilder.append('\n');
                }
            }
        }
    }

    public static List<Long> addExportInfo(Set<Bundle> exportingBundles, String packageName, StringBuilder bundleBuilder) {
        if (exportingBundles.isEmpty()) {
            bundleBuilder.append("\nNo bundles found to export " + packageName + "\n");
            return Collections.emptyList();
        }

        List<Long> bundleIDs = new ArrayList<>();
        bundleBuilder.append("\nThe following bundles export \"" + packageName + "\"\n");
        for (Bundle bundle : exportingBundles) {
            bundleIDs.add(bundle.getBundleId());

            bundleBuilder.append(bundle.getSymbolicName())
                         .append(" ")
                         .append(bundle.getVersion())
                         .append(" [")
                         .append(bundle.getBundleId())
                         .append("]")
                         .append("\n")
                         ;
        }

        return bundleIDs;

    }

    /**
     * @param message - error message from the exception
     * @return list of bundle ids (are in square brackets in the message)
     */
    public static List<Long> findBundleIds(final String message) {
        if (message == null || message.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> bundleIds = new LinkedHashSet<>();
        Matcher bundlePattern = BUNDLE_PATTERN.matcher(message);
        while (bundlePattern.find()) {
            String number = bundlePattern.group(1);
            bundleIds.add(Long.valueOf(number));
        }
        return new ArrayList<>(bundleIds);
    }

    public static Set<Bundle> findExporters(BundleContext ctx, String packageName) {
        Set<Bundle> exporters = new HashSet<>();

        for (Bundle b : ctx.getBundles()) {
            BundleRevision rev = b.adapt(BundleRevision.class);
            if (rev == null) {
                continue;
            }

            List<Capability> caps = rev.getCapabilities(PACKAGE_NAMESPACE);
            for (Capability cap : caps) {
                Map<String, Object> attrs = cap.getAttributes();
                Object exportedPkg = attrs.get(PACKAGE_NAMESPACE);

                if (packageName.equals(exportedPkg)) {
                    exporters.add(b);
                    break; // one match is enough per bundle
                }
            }
        }

        return exporters;
    }

    private static void tryAddPomProperties(Bundle bundle, StringBuilder bundleBuilder) {
        Enumeration<URL> entries = bundle.findEntries("META-INF/maven/", "pom.properties", true);
        if (entries == null) {
            return;
        }

        while (entries.hasMoreElements()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entries.nextElement().openStream(), UTF_8))) {
                reader.lines()
                      .filter(e -> !e.startsWith("#"))
                      .forEach(e -> bundleBuilder.append('\n').append(e.replace("=", " = ")));
            } catch (IOException e1) {
                // Ignore
            }
            bundleBuilder.append('\n');
        }
    }

}
