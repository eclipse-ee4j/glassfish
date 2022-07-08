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

package org.glassfish.main.jul.cfg;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Extended {@link Properties} class. Same as {@link Properties}, it uses hashcodes for providing
 * values for property names, but the difference is in storing content to a file or stream, which is
 * sorted by keys.
 * <p>
 * Also provides some methods for more comfortable work.
 * <p>
 * Warning: using other than {@link String} objects can cause unexpected behavior.
 *
 * @author David Matejcek
 */
// note: this class must not use anything from JUL or GJULE packages, because the dependency is exactly opposite.
public class LoggingProperties extends Properties {

    private static final long serialVersionUID = 8351124426913908969L;

    /**
     * Creates an empty property table.
     */
    public LoggingProperties() {
        super();
    }


    /**
     * Makes a copy of provided properties.
     *
     * @param properties
     */
    public LoggingProperties(final Properties properties) {
        super();
        putAll(properties);
    }


    /**
     * @return a sorted {@link Enumeration} of keys.
     */
    // affects storage!
    @Override
    public Enumeration<Object> keys() {
        return Collections.enumeration(Collections.list(super.keys()).stream()
            .sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList()));
    }


    /**
     * @return all property names used in the current configuration.
     */
    public SortedSet<String> getPropertyNames() {
        return keySet().stream().map(String::valueOf).collect(Collectors.toCollection(TreeSet::new));
    }


    /**
     * @return sorted synchronized set of entries.
     */
    // affects storage!
    @Override
    public synchronized Set<Map.Entry<Object, Object>> entrySet() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        final Comparator<Map.Entry<Object, Object>> comparator = (x, z) -> {
            if (x.getKey() instanceof Comparable && z.getKey() instanceof Comparable) {
                final Comparable key1 = (Comparable) x.getKey();
                final Comparable key2 = (Comparable) z.getKey();
                return key1.compareTo(key2);
            }
            return Integer.compare(x.getKey().hashCode(), z.getKey().hashCode());
        };
        return Collections.synchronizedSet(//
            super.entrySet().stream().sorted(comparator).collect(Collectors.toCollection(LinkedHashSet::new)));
    }


    /**
     * Writes this property list (key and element pairs) in this {@code Properties} table
     * to the file in a format suitable for loading into a {@code Properties} table using
     * the {@link #loadFrom(File)} method.
     *
     * @param outputFile
     * @param comments
     * @throws IOException
     */
    public void store(final File outputFile, final String comments) throws IOException {
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            store(os, comments);
        }
    }


    /**
     * Writes this property list (key and element pairs) in this {@code Properties} table
     * to the file in a format suitable for loading into a {@code Properties} table using
     * the {@link #loadFrom(InputStream)} method.
     *
     * @param comments
     * @return {@link ByteArrayInputStream}
     * @throws IOException
     */
    public ByteArrayInputStream toInputStream(final String comments) throws IOException {
        final ByteArrayOutputStream outputstream = new ByteArrayOutputStream(32768);
        this.store(outputstream, comments);
        return new ByteArrayInputStream(outputstream.toByteArray());
    }


    @Override
    public LoggingProperties clone() {
        return (LoggingProperties) super.clone();
    }


    /**
     * Loads a {@link LoggingProperties} from a {@link File}.
     *
     * @param file
     * @return {@link LoggingProperties}
     * @throws IOException
     */
    public static LoggingProperties loadFrom(final File file) throws IOException {
        if (!file.canRead()) {
            return null;
        }
        try (InputStream input = new FileInputStream(file)) {
            return loadFrom(input);
        }
    }


    /**
     * Loads a {@link LoggingProperties} from an {@link InputStream}.
     *
     * @param stream
     * @return {@link LoggingProperties}
     * @throws IOException
     */
    public static LoggingProperties loadFrom(final InputStream stream) throws IOException {
        final LoggingProperties properties = new LoggingProperties();
        properties.load(stream);
        return properties;
    }
}
