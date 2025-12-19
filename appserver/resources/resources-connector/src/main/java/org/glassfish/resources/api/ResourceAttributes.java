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

package org.glassfish.resources.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

/**
 * Properties of JDBC pools and other resource objects.
 * Created to ensure type safe processing instead of class guessing.
 */
public final class ResourceAttributes {

    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, String[]> stringArrays = new HashMap<>();
    private final Map<String, Properties> properties = new HashMap<>();


    /**
     * @param name must not be null.
     * @return String value registered using {@link #set(String, String)}
     */
    public String getString(String name) {
        requireNonNull(name, "name");
        return strings.get(name);
    }

    /**
     * @return unmodifiable map of all mappings registered using {@link #set(String, String)}
     */
    public Map<String, String> getStrings() {
        return Collections.unmodifiableMap(strings);
    }

    /**
     * @param name must not be null.
     * @return String value registered using {@link #set(String, String[])}
     */
    public String[] getStringArray(String name) {
        requireNonNull(name, "name");
        return stringArrays.get(name);
    }

    /**
     * @param name must not be null.
     * @return {@link Properties} value registered using {@link #set(String, Properties)}
     */
    public Properties getProperties(String name) {
        requireNonNull(name, "name");
        return properties.get(name);
    }

    /**
     * Registers mapping of name and value.
     *
     * @param name must not be null.
     * @param value must not be null.
     */
    public void set(String name, String value) {
        requireNonNull(name, "name");
        requireNonNull(name, "value");
        strings.put(name, value);
    }

    /**
     * Registers mapping of name and value.
     *
     * @param name must not be null.
     * @param value must not be null.
     */
    public void set(String name, String[] value) {
        requireNonNull(name, "name");
        requireNonNull(name, "value");
        stringArrays.put(name, value);
    }

    /**
     * Registers mapping of name and value.
     *
     * @param name must not be null.
     * @param value must not be null.
     */
    public void set(String name, Properties value) {
        requireNonNull(name, "name");
        requireNonNull(value, "value");
        properties.put(name, value);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Resource) ) {
            return false;
        }
        ResourceAttributes other = (ResourceAttributes) obj;
        return
            this.strings.equals(other.strings)
            && this.stringArrays.equals(other.stringArrays)
            && this.properties.equals(other.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strings, stringArrays, properties);
    }


    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(getClass().getSimpleName()).append('[');
        for (Entry<String, String> entry : strings.entrySet()) {
            str.append('\n').append(entry.getKey()).append('=').append(entry.getValue());
        }
        for (Entry<String, String[]> entry : stringArrays.entrySet()) {
            str.append('\n').append(entry.getKey()).append('=').append(Arrays.toString(entry.getValue()));
        }
        for (Entry<String, Properties> entry : properties.entrySet()) {
            str.append('\n').append(entry.getKey()).append("=").append(entry.getValue());
        }
        str.append('\n').append(']');
        return str.toString();
    }
}
