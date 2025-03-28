/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jdke.props;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Given a file, tries to resolve given keys to absolute paths.
 *
 * @author David Matejcek
 */
public final class EnvToPropsConverter {

    private final Path anchor;

    /**
     * @param relativePathAnchor absolute path; will be normalized.
     */
    public EnvToPropsConverter(final Path relativePathAnchor) {
        if (relativePathAnchor == null || !relativePathAnchor.isAbsolute()) {
            throw new IllegalArgumentException("The path is not absolute: " + relativePathAnchor);
        }
        anchor = relativePathAnchor.normalize();
    }


    /**
     * Tries to resolve given keys.
     * <ul>
     * <li>First tries {@link System#getProperty(String)} using map value.
     * <li>Then tries {@link System#getenv(String)} using map key.
     * </ul>
     * Then if the retrieved value is relative, it is resolved against the anchor path from
     * constructor.
     * The resolved file is added to the resulting map using the input map's value as a key.
     * There is no guarantee the file does exist.
     *
     * @param envToSys - key is env name, value is system property name.
     * @return map of system property names and absolute files.
     */
    public Map<String, File> convert(final Map<String, String> envToSys) {
        final Map<String, File> files = new HashMap<>(envToSys.size());
        for (final String envPropertyName : envToSys.keySet()) {
            final String systemPropertyName = envToSys.get(envPropertyName);
            if (systemPropertyName == null) {
                continue;
            }
            final File file = convert(envPropertyName, systemPropertyName);
            if (file != null) {
                files.put(systemPropertyName, file);
            }
        }
        return files;
    }


    /**
     * Tries to resolve given keys.
     * <ul>
     * <li>First tries {@link System#getProperty(String)} using the second parameter.
     * <li>Then tries {@link System#getenv(String)} using the first parameter.
     * </ul>
     * Then if the retrieved value is relative, it is resolved against the anchor path from
     * constructor.
     *
     * @param envPropertyName
     * @param systemPropertyName
     * @return file - there is no guarantee the file does exist.
     */
    public File convert(final String envPropertyName, final String systemPropertyName) {
        final String value = evaluate(envPropertyName, systemPropertyName);
        return value == null ? null : toAbsoluteFile(new File(value));
    }


    private String evaluate(final String envPropertyName, final String systemPropertyName) {
        final String sysValue = System.getProperty(systemPropertyName);
        if (sysValue != null) {
            return sysValue;
        }
        final String envValue = System.getenv(envPropertyName);
        if (envValue != null) {
            return envValue;
        }
//        System.err.println("Missing env " + envPropertyName + " or system property " + systemPropertyName);
        return null;
    }


    private File toAbsoluteFile(final File file) {
        if (file.isAbsolute()) {
            return file.toPath().normalize().toFile();
        }
        return anchor.resolve(file.toPath()).toAbsolutePath().normalize().toFile();
    }
}
